package com.family.myfamily.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.family.myfamily.common.Constants;
import com.family.myfamily.dto.MemberDTO;
import com.family.myfamily.entity.GenealogyPath;
import com.family.myfamily.entity.Member;
import com.family.myfamily.exception.BusinessException;
import com.family.myfamily.mapper.GenealogyPathMapper;
import com.family.myfamily.mapper.MemberMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberMapper memberMapper;

    @Mock
    private GenealogyPathMapper genealogyPathMapper;

    @InjectMocks
    private MemberService memberService;

    private Member testMember;
    private Member fatherMember;
    private MemberDTO testMemberDTO;
    private GenealogyPath testPath;

    @BeforeEach
    void setUp() {
        testMember = new Member();
        testMember.setId(1L);
        testMember.setFamilyId(1L);
        testMember.setName("张三");
        testMember.setGender(Constants.Gender.MALE);
        testMember.setBirthDate(LocalDate.of(1990, 1, 1));
        testMember.setGeneration(2);
        testMember.setFatherId(2L);
        testMember.setStatus(Constants.Status.ENABLED);
        testMember.setDeleted(0);
        testMember.setCreatedAt(LocalDateTime.now());
        testMember.setCreatedBy(1L);

        fatherMember = new Member();
        fatherMember.setId(2L);
        fatherMember.setFamilyId(1L);
        fatherMember.setName("张父");
        fatherMember.setGender(Constants.Gender.MALE);
        fatherMember.setBirthDate(LocalDate.of(1960, 1, 1));
        fatherMember.setGeneration(1);
        fatherMember.setStatus(Constants.Status.ENABLED);
        fatherMember.setDeleted(0);

        testMemberDTO = new MemberDTO();
        testMemberDTO.setName("张三");
        testMemberDTO.setGender(Constants.Gender.MALE);
        testMemberDTO.setBirthDate(LocalDate.of(1990, 1, 1));
        testMemberDTO.setFatherId(2L);

        testPath = new GenealogyPath();
        testPath.setId(1L);
        testPath.setAncestorId(1L);
        testPath.setDescendantId(1L);
        testPath.setDepth(0);
    }

    @Nested
    @DisplayName("查询成员测试")
    class QueryMemberTests {

        @Test
        @DisplayName("根据ID获取成员成功")
        void getMemberById_Success() {
            when(memberMapper.selectById(1L)).thenReturn(testMember);

            Member member = memberService.getMemberById(1L);

            assertNotNull(member);
            assertEquals(1L, member.getId());
            assertEquals("张三", member.getName());
            verify(memberMapper).selectById(1L);
        }

        @Test
        @DisplayName("根据ID获取成员 - 成员不存在")
        void getMemberById_NotFound() {
            when(memberMapper.selectById(999L)).thenReturn(null);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> memberService.getMemberById(999L));

            assertEquals("成员不存在", exception.getMessage());
            assertEquals(400, exception.getCode());
        }

        @Test
        @DisplayName("根据ID获取成员 - 成员已删除")
        void getMemberById_Deleted() {
            testMember.setDeleted(1);
            when(memberMapper.selectById(1L)).thenReturn(testMember);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> memberService.getMemberById(1L));

            assertEquals("成员不存在", exception.getMessage());
        }

        @Test
        @DisplayName("获取成员DTO成功")
        void getMemberDTOById_Success() {
            when(memberMapper.selectById(1L)).thenReturn(testMember);

            MemberDTO dto = memberService.getMemberDTOById(1L);

            assertNotNull(dto);
            assertEquals(1L, dto.getId());
            assertEquals("张三", dto.getName());
            assertEquals(Constants.Gender.MALE, dto.getGender());
        }

        @Test
        @DisplayName("获取成员列表 - 按家族ID筛选")
        void getMemberList_ByFamilyId() {
            List<Member> members = Arrays.asList(testMember, fatherMember);
            when(memberMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(members);

            List<MemberDTO> result = memberService.getMemberList(1L);

            assertNotNull(result);
            assertEquals(2, result.size());
            verify(memberMapper).selectList(any(LambdaQueryWrapper.class));
        }

        @Test
        @DisplayName("获取成员列表 - 无家族ID筛选")
        void getMemberList_NoFamilyId() {
            List<Member> members = Arrays.asList(testMember, fatherMember);
            when(memberMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(members);

            List<MemberDTO> result = memberService.getMemberList(null);

            assertNotNull(result);
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("获取成员分页")
        void getMemberPage_Success() {
            Page<Member> page = new Page<>(1, 10);
            page.setRecords(Arrays.asList(testMember));
            page.setTotal(1);
            when(memberMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

            Page<MemberDTO> result = memberService.getMemberPage(1L, 1, 10);

            assertNotNull(result);
            assertEquals(1, result.getTotal());
            assertEquals(1, result.getRecords().size());
        }

        @Test
        @DisplayName("获取家族成员")
        void getFamilyMembers_Success() {
            List<Member> members = Arrays.asList(testMember, fatherMember);
            when(memberMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(members);

            List<MemberDTO> result = memberService.getFamilyMembers(1L);

            assertNotNull(result);
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("获取族谱树")
        void getGenealogyTree_Success() {
            List<Member> members = Arrays.asList(testMember, fatherMember);
            when(memberMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(members);

            List<Member> result = memberService.getGenealogyTree(1L);

            assertNotNull(result);
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("获取根成员")
        void getRootMembers_Success() {
            List<Member> members = Arrays.asList(fatherMember);
            when(memberMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(members);

            List<Member> result = memberService.getRootMembers(1L);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertNull(fatherMember.getFatherId());
        }

        @Test
        @DisplayName("获取子成员")
        void getChildren_Success() {
            List<Member> children = Arrays.asList(testMember);
            when(memberMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(children);

            List<Member> result = memberService.getChildren(2L);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(2L, testMember.getFatherId());
        }
    }

    @Nested
    @DisplayName("创建成员测试")
    class CreateMemberTests {

        @Test
        @DisplayName("创建成员成功 - 有父亲")
        void createMember_WithFather_Success() {
            testMemberDTO.setFatherId(2L);
            when(memberMapper.selectById(2L)).thenReturn(fatherMember);
            when(memberMapper.insert(any(Member.class))).thenReturn(1);
            when(genealogyPathMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Arrays.asList(testPath));
            when(genealogyPathMapper.insert(any(GenealogyPath.class))).thenReturn(1);

            MemberDTO result = memberService.createMember(testMemberDTO, 1L);

            assertNotNull(result);
            assertEquals("张三", result.getName());
            assertEquals(fatherMember.getGeneration() + 1, result.getGeneration());
            verify(memberMapper).selectById(2L);
            verify(memberMapper).insert(any(Member.class));
            verify(genealogyPathMapper, atLeastOnce()).insert(any(GenealogyPath.class));
        }

        @Test
        @DisplayName("创建成员成功 - 无父亲（根成员）")
        void createMember_NoFather_Success() {
            testMemberDTO.setFatherId(null);
            when(memberMapper.insert(any(Member.class))).thenReturn(1);
            when(genealogyPathMapper.insert(any(GenealogyPath.class))).thenReturn(1);

            MemberDTO result = memberService.createMember(testMemberDTO, 1L);

            assertNotNull(result);
            assertEquals("张三", result.getName());
            verify(memberMapper).insert(any(Member.class));
            verify(genealogyPathMapper).insert(any(GenealogyPath.class));
        }

        @Test
        @DisplayName("创建成员失败 - 父亲不存在")
        void createMember_FatherNotFound() {
            testMemberDTO.setFatherId(999L);
            when(memberMapper.selectById(999L)).thenReturn(null);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> memberService.createMember(testMemberDTO, 1L));

            assertEquals("父亲不存在", exception.getMessage());
            verify(memberMapper, never()).insert(any(Member.class));
        }

        @Test
        @DisplayName("创建成员失败 - 父亲已删除")
        void createMember_FatherDeleted() {
            testMemberDTO.setFatherId(2L);
            fatherMember.setDeleted(1);
            when(memberMapper.selectById(2L)).thenReturn(fatherMember);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> memberService.createMember(testMemberDTO, 1L));

            assertEquals("父亲不存在", exception.getMessage());
        }

        @Test
        @DisplayName("创建成员 - 设置正确的辈分")
        void createMember_CorrectGeneration() {
            testMemberDTO.setFatherId(2L);
            fatherMember.setGeneration(3);
            when(memberMapper.selectById(2L)).thenReturn(fatherMember);
            when(memberMapper.insert(any(Member.class))).thenReturn(1);
            when(genealogyPathMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Arrays.asList(testPath));
            when(genealogyPathMapper.insert(any(GenealogyPath.class))).thenReturn(1);

            MemberDTO result = memberService.createMember(testMemberDTO, 1L);

            assertEquals(4, result.getGeneration());
        }
    }

    @Nested
    @DisplayName("更新成员测试")
    class UpdateMemberTests {

        @Test
        @DisplayName("更新成员成功")
        void updateMember_Success() {
            when(memberMapper.selectById(1L)).thenReturn(testMember);
            when(memberMapper.updateById(any(Member.class))).thenReturn(1);
            when(genealogyPathMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(new ArrayList<>());
            when(genealogyPathMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(1);

            MemberDTO updateDTO = new MemberDTO();
            updateDTO.setName("张三更新");
            updateDTO.setGender(Constants.Gender.MALE);

            MemberDTO result = memberService.updateMember(1L, updateDTO, 1L);

            assertNotNull(result);
            verify(memberMapper).updateById(any(Member.class));
        }

        @Test
        @DisplayName("更新成员失败 - 成员不存在")
        void updateMember_NotFound() {
            when(memberMapper.selectById(999L)).thenReturn(null);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> memberService.updateMember(999L, testMemberDTO, 1L));

            assertEquals("成员不存在", exception.getMessage());
            verify(memberMapper, never()).updateById(any(Member.class));
        }

        @Test
        @DisplayName("更新成员失败 - 成员已删除")
        void updateMember_Deleted() {
            testMember.setDeleted(1);
            when(memberMapper.selectById(1L)).thenReturn(testMember);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> memberService.updateMember(1L, testMemberDTO, 1L));

            assertEquals("成员不存在", exception.getMessage());
        }

        @Test
        @DisplayName("更新成员 - 更改父亲时重建族谱路径")
        void updateMember_ChangeFather() {
            testMember.setFatherId(2L);
            when(memberMapper.selectById(1L)).thenReturn(testMember);
            when(memberMapper.updateById(any(Member.class))).thenReturn(1);
            when(genealogyPathMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Arrays.asList(testPath));
            when(genealogyPathMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(1);
            when(genealogyPathMapper.insert(any(GenealogyPath.class))).thenReturn(1);

            MemberDTO updateDTO = new MemberDTO();
            updateDTO.setFatherId(3L);

            memberService.updateMember(1L, updateDTO, 1L);

            verify(genealogyPathMapper).delete(any(LambdaQueryWrapper.class));
        }
    }

    @Nested
    @DisplayName("删除成员测试")
    class DeleteMemberTests {

        @Test
        @DisplayName("删除成员成功")
        void deleteMember_Success() {
            when(memberMapper.selectById(1L)).thenReturn(testMember);
            when(memberMapper.updateById(any(Member.class))).thenReturn(1);

            memberService.deleteMember(1L, 1L);

            verify(memberMapper).updateById(any(Member.class));
            assertEquals(1, testMember.getDeleted());
        }

        @Test
        @DisplayName("删除成员失败 - 成员不存在")
        void deleteMember_NotFound() {
            when(memberMapper.selectById(999L)).thenReturn(null);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> memberService.deleteMember(999L, 1L));

            assertEquals("成员不存在", exception.getMessage());
        }

        @Test
        @DisplayName("删除成员失败 - 成员已删除")
        void deleteMember_AlreadyDeleted() {
            testMember.setDeleted(1);
            when(memberMapper.selectById(1L)).thenReturn(testMember);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> memberService.deleteMember(1L, 1L));

            assertEquals("成员不存在", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("祖先和后代查询测试")
    class AncestorDescendantTests {

        @Test
        @DisplayName("获取祖先成功")
        void getAncestors_Success() {
            GenealogyPath path1 = new GenealogyPath();
            path1.setAncestorId(2L);
            path1.setDescendantId(1L);
            path1.setDepth(1);

            GenealogyPath path2 = new GenealogyPath();
            path2.setAncestorId(3L);
            path2.setDescendantId(1L);
            path2.setDepth(2);

            when(genealogyPathMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Arrays.asList(path1, path2));
            when(memberMapper.selectBatchIds(Arrays.asList(2L, 3L)))
                    .thenReturn(Arrays.asList(fatherMember, new Member()));

            List<MemberDTO> ancestors = memberService.getAncestors(1L);

            assertNotNull(ancestors);
            assertEquals(2, ancestors.size());
        }

        @Test
        @DisplayName("获取祖先 - 无祖先")
        void getAncestors_NoAncestors() {
            when(genealogyPathMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(new ArrayList<>());

            List<MemberDTO> ancestors = memberService.getAncestors(1L);

            assertNotNull(ancestors);
            assertTrue(ancestors.isEmpty());
        }

        @Test
        @DisplayName("获取后代成功")
        void getDescendants_Success() {
            GenealogyPath path = new GenealogyPath();
            path.setAncestorId(2L);
            path.setDescendantId(1L);
            path.setDepth(1);

            when(genealogyPathMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Arrays.asList(path));
            when(memberMapper.selectBatchIds(Arrays.asList(1L)))
                    .thenReturn(Arrays.asList(testMember));

            List<MemberDTO> descendants = memberService.getDescendants(2L);

            assertNotNull(descendants);
            assertEquals(1, descendants.size());
        }

        @Test
        @DisplayName("获取后代 - 无后代")
        void getDescendants_NoDescendants() {
            when(genealogyPathMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(new ArrayList<>());

            List<MemberDTO> descendants = memberService.getDescendants(2L);

            assertNotNull(descendants);
            assertTrue(descendants.isEmpty());
        }
    }

    @Nested
    @DisplayName("边界条件和异常处理测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("创建成员 - 极长姓名")
        void createMember_VeryLongName() {
            String longName = "张".repeat(100);
            testMemberDTO.setName(longName);
            testMemberDTO.setFatherId(null);
            when(memberMapper.insert(any(Member.class))).thenReturn(1);
            when(genealogyPathMapper.insert(any(GenealogyPath.class))).thenReturn(1);

            MemberDTO result = memberService.createMember(testMemberDTO, 1L);

            assertNotNull(result);
            assertEquals(longName, result.getName());
        }

        @Test
        @DisplayName("创建成员 - 特殊字符姓名")
        void createMember_SpecialCharName() {
            testMemberDTO.setName("John O'Connor");
            testMemberDTO.setFatherId(null);
            when(memberMapper.insert(any(Member.class))).thenReturn(1);
            when(genealogyPathMapper.insert(any(GenealogyPath.class))).thenReturn(1);

            MemberDTO result = memberService.createMember(testMemberDTO, 1L);

            assertNotNull(result);
            assertEquals("John O'Connor", result.getName());
        }

        @Test
        @DisplayName("更新成员 - 空姓名")
        void updateMember_EmptyName() {
            when(memberMapper.selectById(1L)).thenReturn(testMember);
            when(memberMapper.updateById(any(Member.class))).thenReturn(1);

            MemberDTO updateDTO = new MemberDTO();
            updateDTO.setName("");

            MemberDTO result = memberService.updateMember(1L, updateDTO, 1L);

            assertNotNull(result);
        }

        @Test
        @DisplayName("分页查询 - 第一页")
        void getMemberPage_FirstPage() {
            Page<Member> page = new Page<>(1, 10);
            page.setRecords(Arrays.asList(testMember));
            page.setTotal(100);
            when(memberMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

            Page<MemberDTO> result = memberService.getMemberPage(null, 1, 10);

            assertEquals(1, result.getCurrent());
            assertEquals(10, result.getSize());
            assertEquals(100, result.getTotal());
        }

        @Test
        @DisplayName("分页查询 - 空结果")
        void getMemberPage_EmptyResult() {
            Page<Member> page = new Page<>(1, 10);
            page.setRecords(new ArrayList<>());
            page.setTotal(0);
            when(memberMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

            Page<MemberDTO> result = memberService.getMemberPage(null, 1, 10);

            assertEquals(0, result.getTotal());
            assertTrue(result.getRecords().isEmpty());
        }
    }
}
