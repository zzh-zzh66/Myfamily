package com.family.myfamily.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.family.myfamily.common.Constants;
import com.family.myfamily.config.JwtUtils;
import com.family.myfamily.dto.LoginRequest;
import com.family.myfamily.dto.LoginResponse;
import com.family.myfamily.dto.RegisterRequest;
import com.family.myfamily.dto.UserDTO;
import com.family.myfamily.entity.User;
import com.family.myfamily.exception.BusinessException;
import com.family.myfamily.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("$2a$10$encodedPassword");
        testUser.setEmail("test@example.com");
        testUser.setRole(Constants.Role.MEMBER);
        testUser.setStatus(Constants.Status.ENABLED);
        testUser.setDeleted(0);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setLastLoginAt(LocalDateTime.now());

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setPassword("password123");
        registerRequest.setEmail("new@example.com");
    }

    @Nested
    @DisplayName("登录测试")
    class LoginTests {

        @Test
        @DisplayName("成功登录 - 返回Token和用户信息")
        void login_Success() {
            when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);
            when(passwordEncoder.matches("password123", testUser.getPassword())).thenReturn(true);
            when(jwtUtils.generateToken(testUser.getId(), testUser.getRole())).thenReturn("test.jwt.token");
            when(userMapper.updateById(any(User.class))).thenReturn(1);

            LoginResponse response = authService.login(loginRequest);

            assertNotNull(response);
            assertEquals("test.jwt.token", response.getToken());
            assertEquals("Bearer", response.getTokenType());
            assertEquals(86400L, response.getExpiresIn());
            assertNotNull(response.getUser());
            assertEquals("testuser", response.getUser().getUsername());

            verify(userMapper).selectOne(any(LambdaQueryWrapper.class));
            verify(passwordEncoder).matches("password123", testUser.getPassword());
            verify(jwtUtils).generateToken(testUser.getId(), testUser.getRole());
            verify(userMapper).updateById(any(User.class));
        }

        @Test
        @DisplayName("登录失败 - 用户名不存在")
        void login_UserNotFound() {
            when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> authService.login(loginRequest));

            assertEquals("用户名或密码错误", exception.getMessage());
            assertEquals(400, exception.getCode());

            verify(userMapper).selectOne(any(LambdaQueryWrapper.class));
            verify(passwordEncoder, never()).matches(anyString(), anyString());
        }

        @Test
        @DisplayName("登录失败 - 密码错误")
        void login_WrongPassword() {
            when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);
            when(passwordEncoder.matches("password123", testUser.getPassword())).thenReturn(false);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> authService.login(loginRequest));

            assertEquals("用户名或密码错误", exception.getMessage());
            assertEquals(400, exception.getCode());

            verify(passwordEncoder).matches("password123", testUser.getPassword());
        }

        @Test
        @DisplayName("登录失败 - 账号已被禁用")
        void login_AccountDisabled() {
            testUser.setStatus(Constants.Status.DISABLED);
            when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);
            when(passwordEncoder.matches("password123", testUser.getPassword())).thenReturn(true);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> authService.login(loginRequest));

            assertEquals("账号已被禁用", exception.getMessage());
            assertEquals(400, exception.getCode());
        }

        @Test
        @DisplayName("登录失败 - 用户已删除")
        void login_UserDeleted() {
            testUser.setDeleted(1);
            when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> authService.login(loginRequest));

            assertEquals("用户名或密码错误", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("注册测试")
    class RegisterTests {

        @Test
        @DisplayName("成功注册 - 返回用户DTO")
        void register_Success() {
            when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
            when(passwordEncoder.encode("password123")).thenReturn("$2a$10$encodedNewPassword");
            when(userMapper.insert(any(User.class))).thenReturn(1);

            UserDTO result = authService.register(registerRequest);

            assertNotNull(result);
            assertEquals("newuser", result.getUsername());
            assertEquals("new@example.com", result.getEmail());
            assertEquals(Constants.Role.MEMBER, result.getRole());

            verify(userMapper, times(2)).selectCount(any(LambdaQueryWrapper.class));
            verify(passwordEncoder).encode("password123");
            verify(userMapper).insert(any(User.class));
        }

        @Test
        @DisplayName("注册失败 - 用户名已存在")
        void register_UsernameExists() {
            when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> authService.register(registerRequest));

            assertEquals("用户名已存在", exception.getMessage());
            assertEquals(400, exception.getCode());

            verify(userMapper).selectCount(any(LambdaQueryWrapper.class));
            verify(userMapper, never()).insert(any(User.class));
        }

        @Test
        @DisplayName("注册失败 - 邮箱已被注册")
        void register_EmailExists() {
            registerRequest.setMemberId(1L);
            when(userMapper.selectCount(any(LambdaQueryWrapper.class)))
                    .thenReturn(0L)
                    .thenReturn(1L);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> authService.register(registerRequest));

            assertEquals("邮箱已被注册", exception.getMessage());
            assertEquals(400, exception.getCode());

            verify(userMapper, times(2)).selectCount(any(LambdaQueryWrapper.class));
            verify(userMapper, never()).insert(any(User.class));
        }

        @Test
        @DisplayName("注册成功 - 无邮箱")
        void register_SuccessWithoutEmail() {
            registerRequest.setEmail(null);
            when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
            when(passwordEncoder.encode("password123")).thenReturn("$2a$10$encodedNewPassword");
            when(userMapper.insert(any(User.class))).thenReturn(1);

            UserDTO result = authService.register(registerRequest);

            assertNotNull(result);
            assertEquals("newuser", result.getUsername());

            verify(userMapper, times(1)).selectCount(any(LambdaQueryWrapper.class));
            verify(userMapper).insert(any(User.class));
        }
    }

    @Nested
    @DisplayName("获取当前用户测试")
    class GetCurrentUserTests {

        @Test
        @DisplayName("成功获取当前用户")
        void getCurrentUser_Success() {
            when(userMapper.selectById(1L)).thenReturn(testUser);

            User user = authService.getCurrentUser(1L);

            assertNotNull(user);
            assertEquals(1L, user.getId());
            assertEquals("testuser", user.getUsername());

            verify(userMapper).selectById(1L);
        }

        @Test
        @DisplayName("获取失败 - 用户不存在")
        void getCurrentUser_NotFound() {
            when(userMapper.selectById(999L)).thenReturn(null);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> authService.getCurrentUser(999L));

            assertEquals("用户不存在", exception.getMessage());
            assertEquals(400, exception.getCode());
        }

        @Test
        @DisplayName("获取失败 - 用户已删除")
        void getCurrentUser_Deleted() {
            testUser.setDeleted(1);
            when(userMapper.selectById(1L)).thenReturn(testUser);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> authService.getCurrentUser(1L));

            assertEquals("用户不存在", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("边界条件和异常处理测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("登录 - 空用户名")
        void login_EmptyUsername() {
            loginRequest.setUsername("");

            when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> authService.login(loginRequest));

            assertEquals("用户名或密码错误", exception.getMessage());
        }

        @Test
        @DisplayName("登录 - 空密码")
        void login_EmptyPassword() {
            loginRequest.setPassword("");

            when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);
            when(passwordEncoder.matches("", testUser.getPassword())).thenReturn(false);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> authService.login(loginRequest));

            assertEquals("用户名或密码错误", exception.getMessage());
        }

        @Test
        @DisplayName("注册 - 空用户名")
        void register_EmptyUsername() {
            registerRequest.setUsername("");

            when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
            when(passwordEncoder.encode("")).thenReturn("$2a$10$emptyEncoded");
            when(userMapper.insert(any(User.class))).thenReturn(1);

            UserDTO result = authService.register(registerRequest);

            assertNotNull(result);
            verify(userMapper).insert(any(User.class));
        }

        @Test
        @DisplayName("注册 - 空密码")
        void register_EmptyPassword() {
            registerRequest.setPassword("");

            when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
            when(passwordEncoder.encode("")).thenReturn("$2a$10$emptyEncoded");
            when(userMapper.insert(any(User.class))).thenReturn(1);

            UserDTO result = authService.register(registerRequest);

            assertNotNull(result);
            verify(passwordEncoder).encode("");
        }

        @Test
        @DisplayName("注册 - 特殊字符用户名")
        void register_SpecialCharacterUsername() {
            registerRequest.setUsername("user@#$%");

            when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
            when(passwordEncoder.encode("password123")).thenReturn("$2a$10$encoded");
            when(userMapper.insert(any(User.class))).thenReturn(1);

            UserDTO result = authService.register(registerRequest);

            assertNotNull(result);
            assertEquals("user@#$%", result.getUsername());
        }

        @Test
        @DisplayName("获取用户 - Null用户ID")
        void getCurrentUser_NullUserId() {
            when(userMapper.selectById(null)).thenReturn(null);

            assertThrows(Exception.class, () -> authService.getCurrentUser(null));
        }
    }
}
