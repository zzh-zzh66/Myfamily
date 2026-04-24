package com.family.myfamily.controller;

import com.family.myfamily.common.Result;
import com.family.myfamily.dto.FamilyDTO;
import com.family.myfamily.service.FamilyService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/families")
@RequiredArgsConstructor
public class FamilyController {

    private final FamilyService familyService;

    @GetMapping("/{id}")
    public Result<FamilyDTO> getFamilyById(@PathVariable Long id) {
        FamilyDTO family = familyService.getFamilyById(id);
        return Result.success(family);
    }

    @GetMapping
    public Result<List<FamilyDTO>> getFamilyList() {
        List<FamilyDTO> list = familyService.getFamilyList();
        return Result.success(list);
    }

    @PostMapping
    public Result<FamilyDTO> createFamily(
            @RequestBody FamilyDTO dto,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        FamilyDTO result = familyService.createFamily(dto, userId);
        return Result.created("创建成功", result);
    }

    @PutMapping("/{id}")
    public Result<FamilyDTO> updateFamily(
            @PathVariable Long id,
            @RequestBody FamilyDTO dto,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        FamilyDTO result = familyService.updateFamily(id, dto, userId);
        return Result.success("更新成功", result);
    }
}
