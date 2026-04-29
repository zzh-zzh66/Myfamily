package com.family.myfamily.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.family.myfamily.dto.MailDTO;
import com.family.myfamily.entity.Mail;
import com.family.myfamily.entity.Member;
import com.family.myfamily.entity.User;
import com.family.myfamily.exception.BusinessException;
import com.family.myfamily.mapper.MailMapper;
import com.family.myfamily.mapper.MemberMapper;
import com.family.myfamily.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService extends ServiceImpl<MailMapper, Mail> {

    private final MailMapper mailMapper;
    private final UserMapper userMapper;
    private final MemberMapper memberMapper;

    public Page<MailDTO> getMailList(Long userId, String folder, int page, int size) {
        Page<Mail> pageParam = new Page<>(page, size);

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        Long memberId = user.getMemberId();

        LambdaQueryWrapper<Mail> wrapper = new LambdaQueryWrapper<>();

        if ("sent".equals(folder)) {
            wrapper.eq(Mail::getFromUserId, userId)
                    .eq(Mail::getIsDeleted, 0);
        } else if ("trash".equals(folder)) {
            wrapper.eq(Mail::getFromUserId, userId)
                    .eq(Mail::getIsDeleted, 1);
        } else {
            if (memberId == null) {
                throw new BusinessException("用户未关联成员信息");
            }
            wrapper.eq(Mail::getToMemberId, memberId)
                    .eq(Mail::getDeletedByReceiver, 0);
        }

        wrapper.orderByDesc(Mail::getCreatedAt);

        Page<Mail> result = mailMapper.selectPage(pageParam, wrapper);

        Page<MailDTO> dtoPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        dtoPage.setRecords(result.getRecords().stream().map(this::convertToDTO).collect(Collectors.toList()));

        return dtoPage;
    }

    public MailDTO getMailById(Long id, Long userId) {
        Mail mail = mailMapper.selectById(id);
        if (mail == null) {
            throw new BusinessException("邮件不存在");
        }

        User user = userMapper.selectById(userId);
        Long memberId = user != null ? user.getMemberId() : null;
        if (memberId != null && mail.getToMemberId() != null
                && mail.getToMemberId().equals(memberId) && mail.getDeletedByReceiver() == 1) {
            throw new BusinessException("邮件不存在");
        }
        if (mail.getFromUserId().equals(userId) && mail.getIsDeleted() == 1) {
            throw new BusinessException("邮件不存在");
        }

        return convertToDTO(mail);
    }

    @Transactional
    public MailDTO sendMail(MailDTO dto, Long userId) {
        Long toMemberId = dto.getToMemberId() != null ? dto.getToMemberId() : dto.getReceiverId();
        log.info("发送邮件: fromUserId={}, toMemberId={}", userId, toMemberId);

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        Member toMember = memberMapper.selectById(toMemberId);
        if (toMember == null) {
            throw new BusinessException("收件人不存在");
        }

        Mail mail = new Mail();
        BeanUtils.copyProperties(dto, mail);
        mail.setFromUserId(userId);
        mail.setToMemberId(toMemberId);
        mail.setFamilyId(toMember.getFamilyId());

        mailMapper.insert(mail);

        log.info("发送邮件成功: id={}", mail.getId());

        return convertToDTO(mail);
    }

    @Transactional
    public void markAsRead(Long id, Long userId) {
        Mail mail = mailMapper.selectById(id);
        if (mail != null) {
            mail.setIsRead(1);
            mailMapper.updateById(mail);
        }
    }

    @Transactional
    public void markAsUnread(Long id, Long userId) {
        Mail mail = mailMapper.selectById(id);
        if (mail != null) {
            mail.setIsRead(0);
            mailMapper.updateById(mail);
        }
    }

    @Transactional
    public void deleteMail(Long id, Long userId, String folder) {
        log.info("删除邮件: id={}, userId={}, folder={}", id, userId, folder);

        Mail mail = mailMapper.selectById(id);
        if (mail == null) {
            throw new BusinessException("邮件不存在");
        }

        User user = userMapper.selectById(userId);

        if ("sent".equals(folder)) {
            if (!mail.getFromUserId().equals(userId)) {
                throw new BusinessException("无权删除此邮件");
            }
            mail.setIsDeleted(1);
        } else {
            Long memberId = user != null ? user.getMemberId() : null;
            if (memberId == null || !mail.getToMemberId().equals(memberId)) {
                throw new BusinessException("无权删除此邮件");
            }
            mail.setDeletedByReceiver(1);
        }

        mailMapper.updateById(mail);

        log.info("删除邮件成功: id={}", id);
    }

    public Long getUnreadCount(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getMemberId() == null) {
            return 0L;
        }

        LambdaQueryWrapper<Mail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Mail::getToMemberId, user.getMemberId())
                .eq(Mail::getIsRead, 0)
                .eq(Mail::getDeletedByReceiver, 0);

        return mailMapper.selectCount(wrapper);
    }

    public List<Member> searchReceivers(String keyword) {
        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(Member::getName, keyword)
                .eq(Member::getDeleted, 0);
        return memberMapper.selectList(wrapper);
    }

    private MailDTO convertToDTO(Mail mail) {
        MailDTO dto = new MailDTO();
        BeanUtils.copyProperties(mail, dto);

        User fromUser = userMapper.selectById(mail.getFromUserId());
        if (fromUser != null) {
            dto.setFromUserName(fromUser.getUsername());
        }

        Member toMember = memberMapper.selectById(mail.getToMemberId());
        if (toMember != null) {
            dto.setToMemberName(toMember.getName());
        }

        return dto;
    }
}
