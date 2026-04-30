package com.family.myfamily.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.family.myfamily.common.Result;
import com.family.myfamily.entity.Member;
import com.family.myfamily.entity.Memorial;
import com.family.myfamily.mapper.MemberMapper;
import com.family.myfamily.service.MemorialService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/memorials")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MemorialController {

    private final MemorialService memorialService;
    private final MemberMapper memberMapper;

    private static final String UPLOAD_DIR = "e:/MyFamily/Myfamily/backend/uploads/memorial/";

    @GetMapping
    public Result<Page<Memorial>> list(
            @RequestParam(required = false) Long familyId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        LambdaQueryWrapper<Memorial> wrapper = new LambdaQueryWrapper<>();
        if (familyId != null) {
            wrapper.eq(Memorial::getFamilyId, familyId);
        }
        wrapper.orderByDesc(Memorial::getCreatedAt);
        Page<Memorial> result = memorialService.pageWithMemberInfo(new Page<>(page, size), wrapper);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<Memorial> getById(@PathVariable Long id) {
        Memorial memorial = memorialService.getByIdWithMemberInfo(id);
        if (memorial == null) {
            return Result.error(404, "纪念人物不存在");
        }
        return Result.success(memorial);
    }

    @PostMapping
    public Result<Memorial> create(@RequestBody Memorial memorial) {
        if (memorial.getMemberId() != null) {
            Member member = memberMapper.selectById(memorial.getMemberId());
            if (member != null && member.getFamilyId() != null) {
                memorial.setFamilyId(member.getFamilyId());
            }
        }
        memorialService.save(memorial);
        return Result.success(memorial);
    }

    @PutMapping("/{id}")
    public Result<Memorial> update(@PathVariable Long id, @RequestBody Memorial memorial) {
        memorial.setId(id);
        memorialService.updateById(memorial);
        return Result.success(memorial);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        memorialService.removeById(id);
        return Result.success(null);
    }

    @PostMapping("/upload-image")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("请选择图片");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.error("只能上传图片文件");
        }

        if (file.getSize() > 10 * 1024 * 1024) {
            return Result.error("图片大小不能超过10MB");
        }

        try {
            String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String uploadPath = UPLOAD_DIR + datePath;
            File dir = new File(uploadPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = UUID.randomUUID().toString() + extension;

            Path filePath = Paths.get(uploadPath, newFilename);
            Files.write(filePath, file.getBytes());

            String imageUrl = "/uploads/memorial/" + datePath + "/" + newFilename;
            log.info("纪念堂图片上传成功: {}", imageUrl);

            return Result.success(imageUrl);
        } catch (IOException e) {
            log.error("纪念堂图片上传失败", e);
            return Result.error("上传失败: " + e.getMessage());
        }
    }

    @GetMapping("/search-members")
    public Result<List<Member>> searchMembers(@RequestParam String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Result.success(List.of());
        }
        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(Member::getName, keyword.trim())
               .eq(Member::getDeleted, 0)
               .orderByAsc(Member::getGeneration)
               .last("LIMIT 20");
        List<Member> members = memberMapper.selectList(wrapper);

        List<Member> result = members.stream().map(member -> {
            Member m = new Member();
            m.setId(member.getId());
            m.setName(member.getName());
            m.setGender(member.getGender());
            m.setBirthDate(member.getBirthDate());
            m.setDeathDate(member.getDeathDate());
            m.setGeneration(member.getGeneration());
            return m;
        }).collect(Collectors.toList());

        return Result.success(result);
    }
}