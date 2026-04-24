package com.family.myfamily.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
    private LocalDate birthDate;
    private LocalDate deathDate;
    private Integer generation;
    private Long fatherId;
    private Long motherId;
    private String spouseName;
    private Integer status;
    private LocalDateTime createdAt;
}
