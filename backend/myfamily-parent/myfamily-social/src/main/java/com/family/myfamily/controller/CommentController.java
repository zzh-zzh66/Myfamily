package com.family.myfamily.controller;

import com.family.myfamily.common.Result;
import com.family.myfamily.dto.CommentDTO;
import com.family.myfamily.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/{postId}/comments")
    public Result<List<CommentDTO>> getCommentListByPost(@PathVariable Long postId) {
        List<CommentDTO> list = commentService.getCommentListByPost(postId);
        return Result.success(list);
    }

    @PostMapping("/{postId}/comments")
    public Result<CommentDTO> createComment(
            @PathVariable Long postId,
            @RequestBody CommentDTO dto,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        dto.setPostId(postId);
        CommentDTO result = commentService.createComment(dto, userId);
        return Result.created("评论成功", result);
    }

    @DeleteMapping("/{postId}/comments/{id}")
    public Result<Void> deleteComment(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        commentService.deleteComment(id, userId);
        return Result.success("删除成功", null);
    }

    @PostMapping("/{postId}/comments/{id}/like")
    public Result<Void> likeComment(@PathVariable Long id) {
        commentService.incrementLikeCount(id);
        return Result.success("点赞成功", null);
    }
}
