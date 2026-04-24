package com.family.myfamily.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("t_member")
public class Member {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long familyId;

    private String name;

    private String gender;

    private LocalDate birthDate;

    private LocalDate deathDate;

    private Integer generation;

    private Long fatherId;

    private Long motherId;

    private String spouseName;

    private Integer status;

    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    private Long updatedBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
