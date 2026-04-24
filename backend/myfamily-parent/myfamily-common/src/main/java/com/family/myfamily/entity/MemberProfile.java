package com.family.myfamily.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t_member_profile")
public class MemberProfile {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long memberId;

    private String avatarUrl;

    private String nativePlace;

    private String birthPlace;

    private String education;

    private String occupation;

    private String achievement;

    private String biography;

    private String contactEmail;

    private String contactPhone;

    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    private Long updatedBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
