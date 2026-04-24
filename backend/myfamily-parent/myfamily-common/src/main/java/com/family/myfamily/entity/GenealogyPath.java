package com.family.myfamily.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t_genealogy_path")
public class GenealogyPath {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long ancestorId;

    private Long descendantId;

    private Integer depth;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
