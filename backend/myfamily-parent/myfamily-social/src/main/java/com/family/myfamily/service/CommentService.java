package com.family.myfamily.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.family.myfamily.dto.CommentDTO;
import com.family.myfamily.entity.Comment;
import com.family.myfamily.entity.User;
import com.family.myfamily.exception.BusinessException;
import com.family.myfamily.mapper.CommentMapper;
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
public class CommentService extends ServiceImpl<CommentMapper, Comment> {

    private final CommentMapper commentMapper;
    private final UserMapper userMapper;

    public List<CommentDTO> getCommentListByPost(Long postId) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getPostId, postId)
                .eq(Comment::getDeleted, 0)
                .orderByAsc(Comment::getCreatedAt);

        List<Comment> comments = commentMapper.selectList(wrapper);
        return comments.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Transactional
    public CommentDTO createComment(CommentDTO dto, Long userId) {
        log.info("创建评论: postId={}, userId={}", dto.getPostId(), userId);

        Comment comment = new Comment();
        BeanUtils.copyProperties(dto, comment);
        comment.setAuthorId(userId);

        commentMapper.insert(comment);

        log.info("创建评论成功: id={}", comment.getId());

        return convertToDTO(comment);
    }

    @Transactional
    public void deleteComment(Long id, Long userId) {
        log.info("删除评论: id={}, userId={}", id, userId);

        Comment comment = commentMapper.selectById(id);
        if (comment == null || comment.getDeleted() == 1) {
            throw new BusinessException("评论不存在");
        }

        // 检查是否是评论作者
        if (!comment.getAuthorId().equals(userId)) {
            User user = userMapper.selectById(userId);
            if (user == null || !"ADMIN".equals(user.getRole())) {
                throw new BusinessException("无权删除此评论");
            }
        }

        comment.setDeleted(1);
        commentMapper.updateById(comment);

        log.info("删除评论成功: id={}", id);
    }

    @Transactional
    public void incrementLikeCount(Long id) {
        Comment comment = commentMapper.selectById(id);
        if (comment != null) {
            comment.setLikeCount(comment.getLikeCount() + 1);
            commentMapper.updateById(comment);
        }
    }

    private CommentDTO convertToDTO(Comment comment) {
        CommentDTO dto = new CommentDTO();
        BeanUtils.copyProperties(comment, dto);

        User author = userMapper.selectById(comment.getAuthorId());
        if (author != null) {
            dto.setAuthorName(author.getUsername());
        }

        return dto;
    }
}
