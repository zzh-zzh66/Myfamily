package com.family.myfamily.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.family.myfamily.common.Constants;
import com.family.myfamily.dto.MemberDTO;
import com.family.myfamily.entity.GenealogyPath;
import com.family.myfamily.entity.Member;
import com.family.myfamily.exception.BusinessException;
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
public class MemberService extends ServiceImpl<MemberMapper, Member> {

    private final MemberMapper memberMapper;
    private final GenealogyPathMapper genealogyPathMapper;

    public Member getMemberById(Long id) {
        Member member = memberMapper.selectById(id);
        if (member == null || member.getDeleted() == 1) {
            throw new BusinessException("成员不存在");
        }
        return member;
    }

    public MemberDTO getMemberDTOById(Long id) {
        Member member = getMemberById(id);
        MemberDTO dto = new MemberDTO();
        BeanUtils.copyProperties(member, dto);
        return dto;
    }

    public List<MemberDTO> getMemberList(Long familyId) {
        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Member::getDeleted, 0);
        if (familyId != null) {
            wrapper.eq(Member::getFamilyId, familyId);
        }
        wrapper.orderByAsc(Member::getGeneration, Member::getId);

        List<Member> members = memberMapper.selectList(wrapper);
        return members.stream().map(member -> {
            MemberDTO dto = new MemberDTO();
            BeanUtils.copyProperties(member, dto);
            return dto;
        }).collect(Collectors.toList());
    }

    public Page<MemberDTO> getMemberPage(Long familyId, int page, int size) {
        Page<Member> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Member::getDeleted, 0);
        if (familyId != null) {
            wrapper.eq(Member::getFamilyId, familyId);
        }
        wrapper.orderByAsc(Member::getGeneration, Member::getId);

        Page<Member> result = memberMapper.selectPage(pageParam, wrapper);

        Page<MemberDTO> dtoPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        dtoPage.setRecords(result.getRecords().stream().map(member -> {
            MemberDTO dto = new MemberDTO();
            BeanUtils.copyProperties(member, dto);
            return dto;
        }).collect(Collectors.toList()));

        return dtoPage;
    }

    public List<MemberDTO> getFamilyMembers(Long familyId) {
        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Member::getFamilyId, familyId)
                .eq(Member::getDeleted, 0)
                .orderByAsc(Member::getGeneration);
        List<Member> members = memberMapper.selectList(wrapper);
        return members.stream().map(member -> {
            MemberDTO dto = new MemberDTO();
            BeanUtils.copyProperties(member, dto);
            return dto;
        }).collect(Collectors.toList());
    }

    public List<Member> getGenealogyTree(Long familyId) {
        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Member::getDeleted, 0);
        if (familyId != null) {
            wrapper.eq(Member::getFamilyId, familyId);
        }
        wrapper.orderByAsc(Member::getGeneration, Member::getId);
        return memberMapper.selectList(wrapper);
    }

    public List<Member> getRootMembers(Long familyId) {
        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Member::getDeleted, 0)
                .isNull(Member::getFatherId);
        if (familyId != null) {
            wrapper.eq(Member::getFamilyId, familyId);
        }
        wrapper.orderByAsc(Member::getGeneration);
        return memberMapper.selectList(wrapper);
    }

    public List<Member> getChildren(Long memberId) {
        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Member::getFatherId, memberId)
                .eq(Member::getDeleted, 0);
        return memberMapper.selectList(wrapper);
    }

    @Transactional
    public MemberDTO createMember(MemberDTO dto, Long userId) {
        log.info("创建成员: name={}, userId={}", dto.getName(), userId);

        // 验证父亲存在
        if (dto.getFatherId() != null) {
            Member father = memberMapper.selectById(dto.getFatherId());
            if (father == null || father.getDeleted() == 1) {
                throw new BusinessException("父亲不存在");
            }
            // 设置辈分比父亲低一代
            dto.setGeneration(father.getGeneration() + 1);
        }

        Member member = new Member();
        BeanUtils.copyProperties(dto, member);
        member.setCreatedBy(userId);
        member.setStatus(Constants.Status.ENABLED);

        memberMapper.insert(member);

        // 创建族谱路径关系
        createGenealogyPaths(member);

        log.info("创建成员成功: id={}", member.getId());

        MemberDTO result = new MemberDTO();
        BeanUtils.copyProperties(member, result);
        return result;
    }

    @Transactional
    public MemberDTO updateMember(Long id, MemberDTO dto, Long userId) {
        log.info("更新成员: id={}, userId={}", id, userId);

        Member member = memberMapper.selectById(id);
        if (member == null || member.getDeleted() == 1) {
            throw new BusinessException("成员不存在");
        }

        member.setName(dto.getName());
        member.setGender(dto.getGender());
        member.setBirthDate(dto.getBirthDate());
        member.setDeathDate(dto.getDeathDate());
        member.setSpouseName(dto.getSpouseName());
        member.setFatherId(dto.getFatherId());
        member.setMotherId(dto.getMotherId());
        member.setUpdatedBy(userId);

        memberMapper.updateById(member);

        // 更新族谱路径
        if (dto.getFatherId() != null) {
            // 删除旧的路径关系，重新创建
            LambdaQueryWrapper<GenealogyPath> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(GenealogyPath::getDescendantId, id);
            genealogyPathMapper.delete(wrapper);

            createGenealogyPaths(member);
        }

        MemberDTO result = new MemberDTO();
        BeanUtils.copyProperties(member, result);
        return result;
    }

    @Transactional
    public void deleteMember(Long id, Long userId) {
        log.info("删除成员: id={}, userId={}", id, userId);

        Member member = memberMapper.selectById(id);
        if (member == null || member.getDeleted() == 1) {
            throw new BusinessException("成员不存在");
        }

        member.setDeleted(1);
        member.setUpdatedBy(userId);
        memberMapper.updateById(member);

        log.info("删除成员成功: id={}", id);
    }

    public List<MemberDTO> getAncestors(Long memberId) {
        // 查询成员的所有祖先
        LambdaQueryWrapper<GenealogyPath> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GenealogyPath::getDescendantId, memberId)
                .gt(GenealogyPath::getDepth, 0)
                .orderByAsc(GenealogyPath::getDepth);

        List<GenealogyPath> paths = genealogyPathMapper.selectList(wrapper);
        List<Long> ancestorIds = paths.stream()
                .map(GenealogyPath::getAncestorId)
                .collect(Collectors.toList());

        if (ancestorIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<Member> ancestors = memberMapper.selectBatchIds(ancestorIds);
        return ancestors.stream().map(member -> {
            MemberDTO dto = new MemberDTO();
            BeanUtils.copyProperties(member, dto);
            return dto;
        }).collect(Collectors.toList());
    }

    public List<MemberDTO> getDescendants(Long memberId) {
        // 查询成员的所有后代
        LambdaQueryWrapper<GenealogyPath> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GenealogyPath::getAncestorId, memberId)
                .gt(GenealogyPath::getDepth, 0)
                .orderByAsc(GenealogyPath::getDepth);

        List<GenealogyPath> paths = genealogyPathMapper.selectList(wrapper);
        List<Long> descendantIds = paths.stream()
                .map(GenealogyPath::getDescendantId)
                .collect(Collectors.toList());

        if (descendantIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<Member> descendants = memberMapper.selectBatchIds(descendantIds);
        return descendants.stream().map(member -> {
            MemberDTO dto = new MemberDTO();
            BeanUtils.copyProperties(member, dto);
            return dto;
        }).collect(Collectors.toList());
    }

    private void createGenealogyPaths(Member member) {
        // 创建自身到自身的路径
        GenealogyPath selfPath = new GenealogyPath();
        selfPath.setAncestorId(member.getId());
        selfPath.setDescendantId(member.getId());
        selfPath.setDepth(0);
        genealogyPathMapper.insert(selfPath);

        // 如果有父亲，复制父亲的祖先路径，并添加新路径
        if (member.getFatherId() != null) {
            LambdaQueryWrapper<GenealogyPath> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(GenealogyPath::getDescendantId, member.getFatherId());

            List<GenealogyPath> fatherPaths = genealogyPathMapper.selectList(wrapper);

            for (GenealogyPath path : fatherPaths) {
                GenealogyPath newPath = new GenealogyPath();
                newPath.setAncestorId(path.getAncestorId());
                newPath.setDescendantId(member.getId());
                newPath.setDepth(path.getDepth() + 1);
                genealogyPathMapper.insert(newPath);
            }
        }
    }
}
