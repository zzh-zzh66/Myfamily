package com.family.myfamily.controller;

import com.family.myfamily.common.Result;
import com.family.myfamily.dto.MemberDTO;
import com.family.myfamily.dto.PostDTO;
import com.family.myfamily.service.MemberService;
import com.family.myfamily.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final PostService postService;
    private final MemberService memberService;

    @GetMapping("/posts/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<PostDTO>> getPendingPosts() {
        List<PostDTO> list = postService.getPendingPosts();
        return Result.success(list);
    }

    @PostMapping("/posts/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> approvePost(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        postService.approvePost(id, userId);
        return Result.success("审核通过", null);
    }

    @PostMapping("/posts/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> rejectPost(
            @PathVariable Long id,
            @RequestParam String reason,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        postService.rejectPost(id, userId, reason);
        return Result.success("审核拒绝", null);
    }

    @DeleteMapping("/members/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteMember(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        memberService.deleteMember(id, userId);
        return Result.success("删除成功", null);
    }
}
