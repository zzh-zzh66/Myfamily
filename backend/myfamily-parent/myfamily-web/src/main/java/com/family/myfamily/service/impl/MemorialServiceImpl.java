package com.family.myfamily.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.family.myfamily.entity.Member;
import com.family.myfamily.entity.MemberProfile;
import com.family.myfamily.entity.Memorial;
import com.family.myfamily.mapper.MemberMapper;
import com.family.myfamily.mapper.MemberProfileMapper;
import com.family.myfamily.mapper.MemorialMapper;
import com.family.myfamily.service.MemorialService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class MemorialServiceImpl extends ServiceImpl<MemorialMapper, Memorial> implements MemorialService {

    private final MemberMapper memberMapper;
    private final MemberProfileMapper memberProfileMapper;

    public Page<Memorial> pageWithMemberInfo(Page<Memorial> page, LambdaQueryWrapper<Memorial> wrapper) {
        Page<Memorial> memorialPage = this.page(page, wrapper);
        List<Memorial> records = memorialPage.getRecords();

        for (Memorial memorial : records) {
            if (memorial.getMemberId() != null) {
                Member member = memberMapper.selectById(memorial.getMemberId());
                if (member != null) {
                    memorial.setName(member.getName());
                    memorial.setBirthDate(member.getBirthDate());
                    memorial.setDeathDate(member.getDeathDate());
                }

                MemberProfile profile = memberProfileMapper.selectOne(
                    new LambdaQueryWrapper<MemberProfile>().eq(MemberProfile::getMemberId, memorial.getMemberId())
                );
                if (profile != null) {
                    memorial.setAvatar(profile.getAvatarUrl());
                }
            }
        }
        return memorialPage;
    }

    public Memorial getByIdWithMemberInfo(Long id) {
        Memorial memorial = this.getById(id);
        if (memorial != null && memorial.getMemberId() != null) {
            Member member = memberMapper.selectById(memorial.getMemberId());
            if (member != null) {
                memorial.setName(member.getName());
                memorial.setBirthDate(member.getBirthDate());
                memorial.setDeathDate(member.getDeathDate());
            }

            MemberProfile profile = memberProfileMapper.selectOne(
                new LambdaQueryWrapper<MemberProfile>().eq(MemberProfile::getMemberId, memorial.getMemberId())
            );
            if (profile != null) {
                memorial.setAvatar(profile.getAvatarUrl());
            }
        }
        return memorial;
    }
}