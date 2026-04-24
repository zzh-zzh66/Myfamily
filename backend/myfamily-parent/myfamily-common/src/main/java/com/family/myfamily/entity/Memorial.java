package com.family.myfamily.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t_memorial")
public class Memorial {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long familyId;

    private Long memberId;

    private String title;

    private String bio;

    private String photoUrl;

    private String achievement;

    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    private Long updatedBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
