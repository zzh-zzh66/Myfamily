package com.family.myfamily.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.family.myfamily.common.Constants;
import com.family.myfamily.dto.PostDTO;
import com.family.myfamily.entity.Post;
import com.family.myfamily.entity.PostLike;
import com.family.myfamily.entity.User;
import com.family.myfamily.exception.BusinessException;
import com.family.myfamily.mapper.PostLikeMapper;
import com.family.myfamily.mapper.PostMapper;
import com.family.myfamily.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostMapper postMapper;

    @Mock
    private PostLikeMapper postLikeMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private PostService postService;

    private Post testPost;
    private User testUser;
    private User adminUser;
    private PostDTO testPostDTO;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setRole(Constants.Role.MEMBER);
        testUser.setStatus(Constants.Status.ENABLED);

        adminUser = new User();
        adminUser.setId(2L);
        adminUser.setUsername("admin");
        adminUser.setRole(Constants.Role.ADMIN);
        adminUser.setStatus(Constants.Status.ENABLED);

        testPost = new Post();
        testPost.setId(1L);
        testPost.setFamilyId(1L);
        testPost.setAuthorId(1L);
        testPost.setTitle("测试动态");
        testPost.setContent("这是测试内容");
        testPost.setType(Constants.PostType.EVENT);
        testPost.setStatus(Constants.PostStatus.PENDING);
        testPost.setViewCount(0);
        testPost.setLikeCount(0);
        testPost.setDeleted(0);
        testPost.setCreatedAt(LocalDateTime.now());

        testPostDTO = new PostDTO();
        testPostDTO.setTitle("测试动态");
        testPostDTO.setContent("这是测试内容");
        testPostDTO.setType(Constants.PostType.EVENT);
        testPostDTO.setFamilyId(1L);
    }

    @Nested
    @DisplayName("查询动态测试")
    class QueryPostTests {

        @Test
        @DisplayName("根据ID获取动态成功")
        void getPostById_Success() {
            when(postMapper.selectById(1L)).thenReturn(testPost);
            when(userMapper.selectById(1L)).thenReturn(testUser);
            when(postLikeMapper.exists(any(LambdaQueryWrapper.class))).thenReturn(false);

            PostDTO result = postService.getPostById(1L, null);

            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals("测试动态", result.getTitle());
            assertEquals("testuser", result.getAuthorName());
        }

        @Test
        @DisplayName("根据ID获取动态 - 动态不存在")
        void getPostById_NotFound() {
            when(postMapper.selectById(999L)).thenReturn(null);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> postService.getPostById(999L, null));

            assertEquals("动态不存在", exception.getMessage());
        }

        @Test
        @DisplayName("根据ID获取动态 - 动态已删除")
        void getPostById_Deleted() {
            testPost.setDeleted(1);
            when(postMapper.selectById(1L)).thenReturn(testPost);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> postService.getPostById(1L, null));

            assertEquals("动态不存在", exception.getMessage());
        }

        @Test
        @DisplayName("获取动态列表 - 按家族ID和类型筛选")
        void getPostList_WithFilters() {
            List<Post> posts = Arrays.asList(testPost);
            when(postMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(posts);
            when(userMapper.selectById(1L)).thenReturn(testUser);
            when(postLikeMapper.exists(any(LambdaQueryWrapper.class))).thenReturn(false);

            List<PostDTO> result = postService.getPostList(1L, Constants.PostType.EVENT, null);

            assertNotNull(result);
            assertEquals(1, result.size());
            verify(postMapper).selectList(any(LambdaQueryWrapper.class));
        }

        @Test
        @DisplayName("获取动态列表 - 无筛选条件")
        void getPostList_NoFilters() {
            List<Post> posts = Arrays.asList(testPost);
            when(postMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(posts);
            when(userMapper.selectById(1L)).thenReturn(testUser);
            when(postLikeMapper.exists(any(LambdaQueryWrapper.class))).thenReturn(false);

            List<PostDTO> result = postService.getPostList(null, null, null);

            assertNotNull(result);
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("获取动态列表 - 只按家族ID筛选")
        void getPostList_OnlyFamilyId() {
            List<Post> posts = Arrays.asList(testPost);
            when(postMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(posts);
            when(userMapper.selectById(1L)).thenReturn(testUser);
            when(postLikeMapper.exists(any(LambdaQueryWrapper.class))).thenReturn(false);

            List<PostDTO> result = postService.getPostList(1L, null, null);

            assertNotNull(result);
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("获取动态分页")
        void getPostPage_Success() {
            Page<Post> page = new Page<>(1, 10);
            page.setRecords(Arrays.asList(testPost));
            page.setTotal(1);
            when(postMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);
            when(userMapper.selectById(1L)).thenReturn(testUser);
            when(postLikeMapper.exists(any(LambdaQueryWrapper.class))).thenReturn(false);

            Page<PostDTO> result = postService.getPostPage(1L, Constants.PostType.EVENT, 1, 10, null);

            assertNotNull(result);
            assertEquals(1, result.getTotal());
            assertEquals(1, result.getRecords().size());
        }

        @Test
        @DisplayName("获取动态分页 - 空结果")
        void getPostPage_EmptyResult() {
            Page<Post> page = new Page<>(1, 10);
            page.setRecords(Arrays.asList());
            page.setTotal(0);
            when(postMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

            Page<PostDTO> result = postService.getPostPage(1L, null, 1, 10, null);

            assertNotNull(result);
            assertEquals(0, result.getTotal());
            assertTrue(result.getRecords().isEmpty());
        }

        @Test
        @DisplayName("获取动态列表 - 作者信息为空")
        void getPostList_AuthorNotFound() {
            List<Post> posts = Arrays.asList(testPost);
            when(postMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(posts);
            when(userMapper.selectById(1L)).thenReturn(null);
            when(postLikeMapper.exists(any(LambdaQueryWrapper.class))).thenReturn(false);

            List<PostDTO> result = postService.getPostList(1L, null, null);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertNull(result.get(0).getAuthorName());
        }
    }

    @Nested
    @DisplayName("创建动态测试")
    class CreatePostTests {

        @Test
        @DisplayName("创建动态成功")
        void createPost_Success() {
            when(postMapper.insert(any(Post.class))).thenReturn(1);
            when(postMapper.selectById(any())).thenReturn(testPost);
            when(userMapper.selectById(1L)).thenReturn(testUser);

            PostDTO result = postService.createPost(testPostDTO, 1L);

            assertNotNull(result);
            assertEquals("测试动态", result.getTitle());
            assertEquals(Constants.PostStatus.PENDING, result.getStatus());
            verify(postMapper).insert(any(Post.class));
        }

        @Test
        @DisplayName("创建动态 - 事件类型")
        void createPost_EventType() {
            testPostDTO.setType(Constants.PostType.EVENT);
            when(postMapper.insert(any(Post.class))).thenReturn(1);
            when(postMapper.selectById(any())).thenReturn(testPost);
            when(userMapper.selectById(1L)).thenReturn(testUser);

            PostDTO result = postService.createPost(testPostDTO, 1L);

            assertNotNull(result);
            assertEquals(Constants.PostType.EVENT, result.getType());
        }

        @Test
        @DisplayName("创建动态 - 成就类型")
        void createPost_AchievementType() {
            testPostDTO.setType(Constants.PostType.ACHIEVEMENT);
            when(postMapper.insert(any(Post.class))).thenReturn(1);
            when(postMapper.selectById(any())).thenReturn(testPost);
            when(userMapper.selectById(1L)).thenReturn(testUser);

            PostDTO result = postService.createPost(testPostDTO, 1L);

            assertNotNull(result);
            assertEquals(Constants.PostType.ACHIEVEMENT, result.getType());
        }
    }

    @Nested
    @DisplayName("审核动态测试")
    class ReviewPostTests {

        @Test
        @DisplayName("审核通过动态成功")
        void approvePost_Success() {
            testPost.setStatus(Constants.PostStatus.PENDING);
            when(postMapper.selectById(1L)).thenReturn(testPost);
            when(postMapper.updateById(any(Post.class))).thenReturn(1);

            postService.approvePost(1L, 2L);

            assertEquals(Constants.PostStatus.APPROVED, testPost.getStatus());
            assertEquals(2L, testPost.getReviewerId());
            assertNotNull(testPost.getReviewedAt());
            verify(postMapper).updateById(testPost);
        }

        @Test
        @DisplayName("审核通过动态 - 动态不存在")
        void approvePost_NotFound() {
            when(postMapper.selectById(999L)).thenReturn(null);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> postService.approvePost(999L, 2L));

            assertEquals("动态不存在", exception.getMessage());
        }

        @Test
        @DisplayName("审核通过动态 - 动态已删除")
        void approvePost_Deleted() {
            testPost.setDeleted(1);
            when(postMapper.selectById(1L)).thenReturn(testPost);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> postService.approvePost(1L, 2L));

            assertEquals("动态不存在", exception.getMessage());
        }

        @Test
        @DisplayName("审核拒绝动态成功")
        void rejectPost_Success() {
            testPost.setStatus(Constants.PostStatus.PENDING);
            when(postMapper.selectById(1L)).thenReturn(testPost);
            when(postMapper.updateById(any(Post.class))).thenReturn(1);

            postService.rejectPost(1L, 2L, "内容不符合规范");

            assertEquals(Constants.PostStatus.REJECTED, testPost.getStatus());
            assertEquals(2L, testPost.getReviewerId());
            assertEquals("内容不符合规范", testPost.getRejectReason());
            assertNotNull(testPost.getReviewedAt());
            verify(postMapper).updateById(testPost);
        }

        @Test
        @DisplayName("审核拒绝动态 - 动态不存在")
        void rejectPost_NotFound() {
            when(postMapper.selectById(999L)).thenReturn(null);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> postService.rejectPost(999L, 2L, "原因"));

            assertEquals("动态不存在", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("删除动态测试")
    class DeletePostTests {

        @Test
        @DisplayName("删除动态 - 作者本人删除")
        void deletePost_ByAuthor() {
            when(postMapper.selectById(1L)).thenReturn(testPost);
            when(userMapper.selectById(1L)).thenReturn(testUser);
            when(postMapper.updateById(any(Post.class))).thenReturn(1);

            postService.deletePost(1L, 1L);

            assertEquals(1, testPost.getDeleted());
            verify(postMapper).updateById(testPost);
        }

        @Test
        @DisplayName("删除动态 - 管理员删除")
        void deletePost_ByAdmin() {
            when(postMapper.selectById(1L)).thenReturn(testPost);
            when(userMapper.selectById(2L)).thenReturn(adminUser);
            when(postMapper.updateById(any(Post.class))).thenReturn(1);

            postService.deletePost(1L, 2L);

            assertEquals(1, testPost.getDeleted());
            verify(postMapper).updateById(testPost);
        }

        @Test
        @DisplayName("删除动态失败 - 无权限")
        void deletePost_NoPermission() {
            when(postMapper.selectById(1L)).thenReturn(testPost);
            when(userMapper.selectById(3L)).thenReturn(null);

            assertThrows(Exception.class,
                    () -> postService.deletePost(1L, 3L));
        }

        @Test
        @DisplayName("删除动态 - 非作者且非管理员")
        void deletePost_NotAuthorNotAdmin() {
            User anotherUser = new User();
            anotherUser.setId(3L);
            anotherUser.setUsername("another");
            anotherUser.setRole(Constants.Role.MEMBER);

            when(postMapper.selectById(1L)).thenReturn(testPost);
            when(userMapper.selectById(3L)).thenReturn(anotherUser);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> postService.deletePost(1L, 3L));

            assertEquals("无权删除此动态", exception.getMessage());
        }

        @Test
        @DisplayName("删除动态 - 动态不存在")
        void deletePost_NotFound() {
            when(postMapper.selectById(999L)).thenReturn(null);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> postService.deletePost(999L, 1L));

            assertEquals("动态不存在", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("计数功能测试")
    class CountTests {

        @Test
        @DisplayName("增加浏览次数")
        void incrementViewCount_Success() {
            testPost.setViewCount(10);
            when(postMapper.selectById(1L)).thenReturn(testPost);
            when(postMapper.updateById(any(Post.class))).thenReturn(1);

            postService.incrementViewCount(1L);

            assertEquals(11, testPost.getViewCount());
            verify(postMapper).updateById(testPost);
        }

        @Test
        @DisplayName("增加浏览次数 - 动态不存在")
        void incrementViewCount_NotFound() {
            when(postMapper.selectById(999L)).thenReturn(null);

            assertDoesNotThrow(() -> postService.incrementViewCount(999L));
        }
    }

    @Nested
    @DisplayName("点赞功能测试")
    class LikeTests {

        @Test
        @DisplayName("点赞成功")
        void likePost_Success() {
            testPost.setStatus(Constants.PostStatus.APPROVED);
            when(postMapper.selectById(1L)).thenReturn(testPost);
            when(postLikeMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
            when(postMapper.updateById(any(Post.class))).thenReturn(1);

            postService.likePost(1L, 1L);

            assertEquals(1, testPost.getLikeCount());
            verify(postLikeMapper).insert(any(PostLike.class));
        }

        @Test
        @DisplayName("点赞 - 用户已点赞")
        void likePost_AlreadyLiked() {
            testPost.setLikeCount(1);
            PostLike existingLike = new PostLike();
            when(postMapper.selectById(1L)).thenReturn(testPost);
            when(postLikeMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existingLike);

            postService.likePost(1L, 1L);

            assertEquals(1, testPost.getLikeCount());
            verify(postLikeMapper, never()).insert(any(PostLike.class));
        }

        @Test
        @DisplayName("点赞 - 未登录")
        void likePost_NotLoggedIn() {
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> postService.likePost(1L, null));

            assertEquals("请先登录", exception.getMessage());
        }

        @Test
        @DisplayName("取消点赞成功")
        void unlikePost_Success() {
            testPost.setLikeCount(1);
            PostLike existingLike = new PostLike();
            existingLike.setId(1L);
            when(postMapper.selectById(1L)).thenReturn(testPost);
            when(postLikeMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existingLike);
            when(postMapper.updateById(any(Post.class))).thenReturn(1);

            postService.unlikePost(1L, 1L);

            assertEquals(0, testPost.getLikeCount());
            assertEquals(1, existingLike.getDeleted());
        }

        @Test
        @DisplayName("取消点赞 - 用户未点赞")
        void unlikePost_NotLiked() {
            when(postMapper.selectById(1L)).thenReturn(testPost);
            when(postLikeMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            postService.unlikePost(1L, 1L);

            assertEquals(0, testPost.getLikeCount());
            verify(postLikeMapper, never()).updateById(any(PostLike.class));
        }

        @Test
        @DisplayName("取消点赞 - 未登录")
        void unlikePost_NotLoggedIn() {
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> postService.unlikePost(1L, null));

            assertEquals("请先登录", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("边界条件和异常处理测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("创建动态 - 极长标题")
        void createPost_VeryLongTitle() {
            String longTitle = "测试".repeat(100);
            testPostDTO.setTitle(longTitle);
            when(postMapper.insert(any(Post.class))).thenReturn(1);
            when(postMapper.selectById(any())).thenReturn(testPost);
            when(userMapper.selectById(1L)).thenReturn(testUser);

            PostDTO result = postService.createPost(testPostDTO, 1L);

            assertNotNull(result);
        }

        @Test
        @DisplayName("创建动态 - 空内容")
        void createPost_EmptyContent() {
            testPostDTO.setContent("");
            when(postMapper.insert(any(Post.class))).thenReturn(1);
            when(postMapper.selectById(any())).thenReturn(testPost);
            when(userMapper.selectById(1L)).thenReturn(testUser);

            PostDTO result = postService.createPost(testPostDTO, 1L);

            assertNotNull(result);
        }

        @Test
        @DisplayName("创建动态 - 特殊字符内容")
        void createPost_SpecialCharContent() {
            testPostDTO.setContent("<script>alert('xss')</script>");
            when(postMapper.insert(any(Post.class))).thenReturn(1);
            when(postMapper.selectById(any())).thenReturn(testPost);
            when(userMapper.selectById(1L)).thenReturn(testUser);

            PostDTO result = postService.createPost(testPostDTO, 1L);

            assertNotNull(result);
        }

        @Test
        @DisplayName("审核拒绝 - 空原因")
        void rejectPost_EmptyReason() {
            testPost.setStatus(Constants.PostStatus.PENDING);
            when(postMapper.selectById(1L)).thenReturn(testPost);
            when(postMapper.updateById(any(Post.class))).thenReturn(1);

            postService.rejectPost(1L, 2L, "");

            assertEquals(Constants.PostStatus.REJECTED, testPost.getStatus());
            assertEquals("", testPost.getRejectReason());
        }

        @Test
        @DisplayName("分页查询 - 超出总页数")
        void getPostPage_ExceedTotalPages() {
            Page<Post> page = new Page<>(100, 10);
            page.setRecords(Arrays.asList());
            page.setTotal(50);
            when(postMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

            Page<PostDTO> result = postService.getPostPage(null, null, 100, 10, null);

            assertNotNull(result);
            assertEquals(100, result.getCurrent());
            assertTrue(result.getRecords().isEmpty());
        }
    }
}