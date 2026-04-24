package com.family.myfamily.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.family.myfamily.common.Result;
import com.family.myfamily.dto.MemberDTO;
import com.family.myfamily.entity.Member;
import com.family.myfamily.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/{id}")
    public Result<MemberDTO> getMemberById(@PathVariable Long id) {
        MemberDTO member = memberService.getMemberDTOById(id);
        return Result.success(member);
    }

    @GetMapping
    public Result<List<MemberDTO>> getMemberList(
            @RequestParam(value = "familyId", required = false) Long familyId) {
        List<MemberDTO> list = memberService.getMemberList(familyId);
        return Result.success(list);
    }

    @GetMapping("/page")
    public Result<Page<MemberDTO>> getMemberPage(
            @RequestParam(value = "familyId", required = false) Long familyId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Page<MemberDTO> result = memberService.getMemberPage(familyId, page, size);
        return Result.success(result);
    }

    @GetMapping("/family/{familyId}")
    public Result<List<MemberDTO>> getFamilyMembers(@PathVariable Long familyId) {
        List<MemberDTO> list = memberService.getFamilyMembers(familyId);
        return Result.success(list);
    }

    @GetMapping("/genealogy/tree")
    public Result<List<Member>> getGenealogyTree(@RequestParam(value = "familyId", required = false) Long familyId) {
        List<Member> tree = memberService.getGenealogyTree(familyId);
        return Result.success(tree);
    }

    @GetMapping("/{id}/ancestors")
    public Result<List<MemberDTO>> getAncestors(@PathVariable Long id) {
        List<MemberDTO> ancestors = memberService.getAncestors(id);
        return Result.success(ancestors);
    }

    @GetMapping("/{id}/descendants")
    public Result<List<MemberDTO>> getDescendants(@PathVariable Long id) {
        List<MemberDTO> descendants = memberService.getDescendants(id);
        return Result.success(descendants);
    }

    @PostMapping
    public Result<MemberDTO> createMember(
            @RequestBody MemberDTO dto,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        MemberDTO result = memberService.createMember(dto, userId);
        return Result.created("创建成功", result);
    }

    @PutMapping("/{id}")
    public Result<MemberDTO> updateMember(
            @PathVariable Long id,
            @RequestBody MemberDTO dto,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        MemberDTO result = memberService.updateMember(id, dto, userId);
        return Result.success("更新成功", result);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteMember(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        memberService.deleteMember(id, userId);
        return Result.success("删除成功", null);
    }
}
