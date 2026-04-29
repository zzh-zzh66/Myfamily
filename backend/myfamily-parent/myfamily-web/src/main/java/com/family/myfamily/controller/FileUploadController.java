package com.family.myfamily.controller;

import com.family.myfamily.common.Result;
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
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/posts")
@CrossOrigin(origins = "*")
public class FileUploadController {

    private static final String UPLOAD_DIR = "e:/MyFamily/Myfamily/backend/uploads/";

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

            String imageUrl = "/uploads/" + datePath + "/" + newFilename;
            log.info("图片上传成功: {}", imageUrl);

            return Result.success("上传成功", imageUrl);
        } catch (IOException e) {
            log.error("图片上传失败", e);
            return Result.error("上传失败: " + e.getMessage());
        }
    }
}