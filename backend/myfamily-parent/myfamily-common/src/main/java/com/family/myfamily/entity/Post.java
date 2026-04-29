package com.family.myfamily.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t_post")
public class Post {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long familyId;

    private Long authorId;

    private String title;

    private String content;

    private String images;

    private String type;

    private String status;

    private String rejectReason;

    private Integer viewCount;

    private Integer likeCount;

    private Long reviewerId;

    private LocalDateTime reviewedAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
