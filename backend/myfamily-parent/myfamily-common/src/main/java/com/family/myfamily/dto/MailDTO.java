package com.family.myfamily.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MailDTO {
    private Long id;
    private Long familyId;
    private Long fromUserId;
    private String fromUserName;
    private String fromUserAvatar;
    private Long toMemberId;
    private String toMemberName;
    private String toMemberAvatar;
    private String subject;
    private String content;
    private String attachments;
    private Integer isRead;
    private Integer isDeleted;
    private Integer deletedByReceiver;
    private Integer isDraft;
    private LocalDateTime createdAt;

    private Long receiverId;
}
