package com.family.myfamily.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.family.myfamily.common.Result;
import com.family.myfamily.dto.PostDTO;
import com.family.myfamily.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            return (Long) authentication.getPrincipal();
        }
        return null;
    }

    @GetMapping("/{id}")
    public Result<PostDTO> getPostById(@PathVariable Long id) {
        postService.incrementViewCount(id);
        PostDTO post = postService.getPostById(id, getCurrentUserId());
        return Result.success(post);
    }

    @GetMapping
    public Result<Page<PostDTO>> getPostList(
            @RequestParam(value = "familyId", required = false) Long familyId,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        Page<PostDTO> list = postService.getPostPage(familyId, type, page, size, getCurrentUserId());
        return Result.success(list);
    }

    @PostMapping
    public Result<PostDTO> createPost(
            @RequestBody PostDTO dto,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        PostDTO result = postService.createPost(dto, userId);
        return Result.created("发布成功", result);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deletePost(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        postService.deletePost(id, userId);
        return Result.success("删除成功", null);
    }

    @PostMapping("/{id}/like")
    public Result<Void> likePost(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        postService.likePost(id, userId);
        return Result.success("点赞成功", null);
    }

    @DeleteMapping("/{id}/like")
    public Result<Void> unlikePost(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        postService.unlikePost(id, userId);
        return Result.success("取消点赞成功", null);
    }
}
