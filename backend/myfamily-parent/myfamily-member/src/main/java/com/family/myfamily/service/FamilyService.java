package com.family.myfamily.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.family.myfamily.common.Constants;
import com.family.myfamily.dto.FamilyDTO;
import com.family.myfamily.dto.MemberDTO;
import com.family.myfamily.entity.Family;
import com.family.myfamily.entity.GenealogyPath;
import com.family.myfamily.entity.Member;
import com.family.myfamily.exception.BusinessException;
import com.family.myfamily.mapper.FamilyMapper;
import com.family.myfamily.mapper.GenealogyPathMapper;
import com.family.myfamily.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FamilyService extends ServiceImpl<FamilyMapper, Family> {

    private final FamilyMapper familyMapper;

    public FamilyDTO getFamilyById(Long id) {
        Family family = familyMapper.selectById(id);
        if (family == null || family.getDeleted() == 1) {
            throw new BusinessException("家族不存在");
        }
        FamilyDTO dto = new FamilyDTO();
        BeanUtils.copyProperties(family, dto);
        return dto;
    }

    public List<FamilyDTO> getFamilyList() {
        LambdaQueryWrapper<Family> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Family::getDeleted, 0)
                .orderByDesc(Family::getCreatedAt);
        List<Family> families = familyMapper.selectList(wrapper);
        return families.stream().map(family -> {
            FamilyDTO dto = new FamilyDTO();
            BeanUtils.copyProperties(family, dto);
            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional
    public FamilyDTO createFamily(FamilyDTO dto, Long userId) {
        log.info("创建家族: name={}, userId={}", dto.getName(), userId);

        Family family = new Family();
        BeanUtils.copyProperties(dto, family);
        family.setCreatedBy(userId);
        family.setStatus(Constants.Status.ENABLED);

        familyMapper.insert(family);

        log.info("创建家族成功: id={}", family.getId());

        FamilyDTO result = new FamilyDTO();
        BeanUtils.copyProperties(family, result);
        return result;
    }

    @Transactional
    public FamilyDTO updateFamily(Long id, FamilyDTO dto, Long userId) {
        log.info("更新家族: id={}, userId={}", id, userId);

        Family family = familyMapper.selectById(id);
        if (family == null || family.getDeleted() == 1) {
            throw new BusinessException("家族不存在");
        }

        family.setName(dto.getName());
        family.setDescription(dto.getDescription());
        family.setLogoUrl(dto.getLogoUrl());
        family.setUpdatedBy(userId);

        familyMapper.updateById(family);

        FamilyDTO result = new FamilyDTO();
        BeanUtils.copyProperties(family, result);
        return result;
    }
}
