package com.family.myfamily.controller;

import com.family.myfamily.common.Result;
import com.family.myfamily.dto.PostDTO;
import com.family.myfamily.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping("/{id}")
    public Result<PostDTO> getPostById(@PathVariable Long id) {
        postService.incrementViewCount(id);
        PostDTO post = postService.getPostById(id);
        return Result.success(post);
    }

    @GetMapping
    public Result<List<PostDTO>> getPostList(
            @RequestParam(required = false) Long familyId,
            @RequestParam(required = false) String type) {
        List<PostDTO> list = postService.getPostList(familyId, type);
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
    public Result<Void> likePost(@PathVariable Long id) {
        postService.incrementLikeCount(id);
        return Result.success("点赞成功", null);
    }
}
