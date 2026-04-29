package com.family.myfamily.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.family.myfamily.common.Result;
import com.family.myfamily.dto.MailDTO;
import com.family.myfamily.entity.Member;
import com.family.myfamily.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/mails")
@RequiredArgsConstructor
public class MailController {

    private final MailService mailService;

    @GetMapping
    public Result<Page<MailDTO>> getMailList(
            @RequestParam(value = "folder", defaultValue = "inbox") String folder,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        Page<MailDTO> list = mailService.getMailList(userId, folder, page, size);
        return Result.success(list);
    }

    @GetMapping("/{id}")
    public Result<MailDTO> getMailDetail(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        mailService.markAsRead(id, userId);
        MailDTO mail = mailService.getMailById(id, userId);
        return Result.success(mail);
    }

    @PostMapping("/send")
    public Result<MailDTO> sendMail(@RequestBody MailDTO dto, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        MailDTO result = mailService.sendMail(dto, userId);
        return Result.created("发送成功", result);
    }

    @PostMapping("/draft")
    public Result<MailDTO> saveDraft(@RequestBody MailDTO dto, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        MailDTO result = mailService.saveDraft(dto, userId);
        return Result.success("草稿保存成功", result);
    }

    @PutMapping("/{id}/read")
    public Result<Void> markAsRead(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        mailService.markAsRead(id, userId);
        return Result.success("标记已读成功", null);
    }

    @PutMapping("/{id}/unread")
    public Result<Void> markAsUnread(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        mailService.markAsUnread(id, userId);
        return Result.success("标记未读成功", null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteMail(
            @PathVariable Long id,
            @RequestParam(value = "folder", defaultValue = "inbox") String folder,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        mailService.deleteMail(id, userId, folder);
        return Result.success("删除成功", null);
    }

    @GetMapping("/unread-count")
    public Result<Map<String, Long>> getUnreadCount(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        Long count = mailService.getUnreadCount(userId);
        Map<String, Long> result = new HashMap<>();
        result.put("inbox", count);
        result.put("total", count);
        return Result.success(result);
    }

    @GetMapping("/search-receivers")
    public Result<List<Member>> searchReceivers(@RequestParam String keyword) {
        List<Member> list = mailService.searchReceivers(keyword);
        return Result.success(list);
    }
}
