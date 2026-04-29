package com.family.myfamily.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostDTO {
    private Long id;
    private Long familyId;
    private Long authorId;
    private String authorName;
    private String title;
    private String content;
    private String images;
    private String type;
    private String status;
    private Integer viewCount;
    private Integer likeCount;
    private Boolean isLiked;
    private LocalDateTime createdAt;
}
