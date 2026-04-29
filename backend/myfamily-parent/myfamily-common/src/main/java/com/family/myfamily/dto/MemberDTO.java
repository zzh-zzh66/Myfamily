package com.family.myfamily.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberDTO {
    private Long id;
    private Long familyId;
    private String name;
    private String gender;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate deathDate;

    private Integer generation;
    private Long fatherId;
    private Long motherId;
    private Long spouseId;
    private String spouseName;
    private Integer status;
    private Boolean isVirtual;
    private LocalDateTime createdAt;
}
