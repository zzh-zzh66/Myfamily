package com.family.myfamily.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t_mail")
public class Mail {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long familyId;

    private Long fromUserId;

    private Long toMemberId;

    private String subject;

    private String content;

    private String attachments;

    private Integer isRead;

    private Integer isDeleted;

    private Integer deletedByReceiver;

    private Integer isDraft;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
