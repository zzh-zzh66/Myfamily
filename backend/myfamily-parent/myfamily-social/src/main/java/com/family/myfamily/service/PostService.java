package com.family.myfamily.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.family.myfamily.common.Constants;
import com.family.myfamily.dto.PostDTO;
import com.family.myfamily.entity.Post;
import com.family.myfamily.entity.User;
import com.family.myfamily.exception.BusinessException;
import com.family.myfamily.mapper.PostMapper;
import com.family.myfamily.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService extends ServiceImpl<PostMapper, Post> {

    private final PostMapper postMapper;
    private final UserMapper userMapper;

    public PostDTO getPostById(Long id) {
        Post post = postMapper.selectById(id);
        if (post == null || post.getDeleted() == 1) {
            throw new BusinessException("动态不存在");
        }
        return convertToDTO(post);
    }

    public List<PostDTO> getPostList(Long familyId, String type) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getDeleted, 0)
                .eq(Post::getStatus, Constants.PostStatus.APPROVED);
        if (familyId != null) {
            wrapper.eq(Post::getFamilyId, familyId);
        }
        if (type != null) {
            wrapper.eq(Post::getType, type);
        }
        wrapper.orderByDesc(Post::getCreatedAt);

        List<Post> posts = postMapper.selectList(wrapper);
        return posts.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public Page<PostDTO> getPostPage(Long familyId, String type, int page, int size) {
        Page<Post> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getDeleted, 0)
                .eq(Post::getStatus, Constants.PostStatus.APPROVED);
        if (familyId != null) {
            wrapper.eq(Post::getFamilyId, familyId);
        }
        if (type != null) {
            wrapper.eq(Post::getType, type);
        }
        wrapper.orderByDesc(Post::getCreatedAt);

        Page<Post> result = postMapper.selectPage(pageParam, wrapper);

        Page<PostDTO> dtoPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        dtoPage.setRecords(result.getRecords().stream().map(this::convertToDTO).collect(Collectors.toList()));

        return dtoPage;
    }

    @Transactional
    public PostDTO createPost(PostDTO dto, Long userId) {
        log.info("创建动态: title={}, userId={}", dto.getTitle(), userId);

        Post post = new Post();
        BeanUtils.copyProperties(dto, post);
        post.setAuthorId(userId);
        post.setStatus(Constants.PostStatus.PENDING);

        postMapper.insert(post);

        log.info("创建动态成功: id={}", post.getId());

        return convertToDTO(post);
    }

    @Transactional
    public void approvePost(Long id, Long reviewerId) {
        log.info("审核通过动态: id={}, reviewerId={}", id, reviewerId);

        Post post = postMapper.selectById(id);
        if (post == null || post.getDeleted() == 1) {
            throw new BusinessException("动态不存在");
        }

        post.setStatus(Constants.PostStatus.APPROVED);
        post.setReviewerId(reviewerId);
        post.setReviewedAt(java.time.LocalDateTime.now());

        postMapper.updateById(post);

        log.info("审核通过动态成功: id={}", id);
    }

    @Transactional
    public void rejectPost(Long id, Long reviewerId, String reason) {
        log.info("审核拒绝动态: id={}, reviewerId={}", id, reviewerId);

        Post post = postMapper.selectById(id);
        if (post == null || post.getDeleted() == 1) {
            throw new BusinessException("动态不存在");
        }

        post.setStatus(Constants.PostStatus.REJECTED);
        post.setReviewerId(reviewerId);
        post.setReviewedAt(java.time.LocalDateTime.now());
        post.setRejectReason(reason);

        postMapper.updateById(post);

        log.info("审核拒绝动态成功: id={}", id);
    }

    @Transactional
    public void deletePost(Long id, Long userId) {
        log.info("删除动态: id={}, userId={}", id, userId);

        Post post = postMapper.selectById(id);
        if (post == null || post.getDeleted() == 1) {
            throw new BusinessException("动态不存在");
        }

        // 检查是否是作者或管理员
        User user = userMapper.selectById(userId);
        if (!post.getAuthorId().equals(userId) && (user == null || !Constants.Role.ADMIN.equals(user.getRole()))) {
            throw new BusinessException("无权删除此动态");
        }

        post.setDeleted(1);
        postMapper.updateById(post);

        log.info("删除动态成功: id={}", id);
    }

    @Transactional
    public void incrementViewCount(Long id) {
        Post post = postMapper.selectById(id);
        if (post != null) {
            post.setViewCount(post.getViewCount() + 1);
            postMapper.updateById(post);
        }
    }

    @Transactional
    public void incrementLikeCount(Long id) {
        Post post = postMapper.selectById(id);
        if (post != null) {
            post.setLikeCount(post.getLikeCount() + 1);
            postMapper.updateById(post);
        }
    }

    private PostDTO convertToDTO(Post post) {
        PostDTO dto = new PostDTO();
        BeanUtils.copyProperties(post, dto);

        User author = userMapper.selectById(post.getAuthorId());
        if (author != null) {
            dto.setAuthorName(author.getUsername());
        }

        return dto;
    }
}
