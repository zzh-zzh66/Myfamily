package com.family.myfamily.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FamilyDTO {
    private Long id;
    private String name;
    private String description;
    private String logoUrl;
    private Long adminId;
    private Integer status;
}
