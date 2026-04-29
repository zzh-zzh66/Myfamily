package com.family.myfamily.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.family.myfamily.common.Constants;
import com.family.myfamily.dto.LoginRequest;
import com.family.myfamily.dto.LoginResponse;
import com.family.myfamily.dto.RegisterRequest;
import com.family.myfamily.dto.UserDTO;
import com.family.myfamily.entity.User;
import com.family.myfamily.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private ObjectMapper objectMapper;
    private User testUser;
    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        objectMapper = new ObjectMapper();

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setRole(Constants.Role.MEMBER);
        testUser.setStatus(Constants.Status.ENABLED);
        testUser.setCreatedAt(LocalDateTime.now());

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setPassword("password123");
        registerRequest.setEmail("new@example.com");
    }

    @Nested
    @DisplayName("登录接口测试")
    class LoginTests {

        @Test
        @DisplayName("登录成功 - 返回200和Token")
        void login_Success() throws Exception {
            LoginResponse response = LoginResponse.builder()
                    .token("test.jwt.token")
                    .tokenType("Bearer")
                    .expiresIn(86400L)
                    .user(UserDTO.builder()
                            .id(1L)
                            .username("testuser")
                            .email("test@example.com")
                            .role(Constants.Role.MEMBER)
                            .build())
                    .build();

            when(authService.login(any(LoginRequest.class))).thenReturn(response);

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("登录成功"))
                    .andExpect(jsonPath("$.data.token").value("test.jwt.token"))
                    .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                    .andExpect(jsonPath("$.data.expiresIn").value(86400))
                    .andExpect(jsonPath("$.data.user.username").value("testuser"));
        }

        @Test
        @DisplayName("登录失败 - 用户名不存在")
        void login_UserNotFound() throws Exception {
            when(authService.login(any(LoginRequest.class)))
                    .thenThrow(new com.family.myfamily.exception.BusinessException("用户名或密码错误"));

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(400))
                    .andExpect(jsonPath("$.message").value("用户名或密码错误"));
        }

        @Test
        @DisplayName("登录失败 - 密码错误")
        void login_WrongPassword() throws Exception {
            when(authService.login(any(LoginRequest.class)))
                    .thenThrow(new com.family.myfamily.exception.BusinessException("用户名或密码错误"));

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(400));
        }

        @Test
        @DisplayName("登录失败 - 账号已被禁用")
        void login_AccountDisabled() throws Exception {
            when(authService.login(any(LoginRequest.class)))
                    .thenThrow(new com.family.myfamily.exception.BusinessException("账号已被禁用"));

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(400))
                    .andExpect(jsonPath("$.message").value("账号已被禁用"));
        }

        @Test
        @DisplayName("登录失败 - 空请求体")
        void login_EmptyBody() throws Exception {
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("注册接口测试")
    class RegisterTests {

        @Test
        @DisplayName("注册成功 - 返回201")
        void register_Success() throws Exception {
            UserDTO userDTO = UserDTO.builder()
                    .id(1L)
                    .username("newuser")
                    .email("new@example.com")
                    .role(Constants.Role.MEMBER)
                    .build();

            when(authService.register(any(RegisterRequest.class))).thenReturn(userDTO);

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.code").value(201))
                    .andExpect(jsonPath("$.message").value("注册成功"))
                    .andExpect(jsonPath("$.data.username").value("newuser"))
                    .andExpect(jsonPath("$.data.email").value("new@example.com"));
        }

        @Test
        @DisplayName("注册失败 - 用户名已存在")
        void register_UsernameExists() throws Exception {
            when(authService.register(any(RegisterRequest.class)))
                    .thenThrow(new com.family.myfamily.exception.BusinessException("用户名已存在"));

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(400))
                    .andExpect(jsonPath("$.message").value("用户名已存在"));
        }

        @Test
        @DisplayName("注册失败 - 邮箱已被注册")
        void register_EmailExists() throws Exception {
            when(authService.register(any(RegisterRequest.class)))
                    .thenThrow(new com.family.myfamily.exception.BusinessException("邮箱已被注册"));

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(400))
                    .andExpect(jsonPath("$.message").value("邮箱已被注册"));
        }

        @Test
        @DisplayName("注册失败 - 空请求体")
        void register_EmptyBody() throws Exception {
            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("获取当前用户接口测试")
    class GetCurrentUserTests {

        @Test
        @DisplayName("获取当前用户成功")
        void getCurrentUser_Success() throws Exception {
            when(authService.getCurrentUser(1L)).thenReturn(testUser);

            mockMvc.perform(get("/api/v1/auth/me")
                            .principal(() -> "1")
                            .requestAttr("userId", 1L))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("获取当前用户 - 用户不存在")
        void getCurrentUser_NotFound() throws Exception {
            when(authService.getCurrentUser(999L))
                    .thenThrow(new com.family.myfamily.exception.BusinessException("用户不存在"));

            mockMvc.perform(get("/api/v1/auth/me")
                            .principal(() -> "999"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("登录 - 用户名过长")
        void login_LongUsername() throws Exception {
            loginRequest.setUsername("a".repeat(100));
            when(authService.login(any(LoginRequest.class)))
                    .thenThrow(new com.family.myfamily.exception.BusinessException("用户名或密码错误"));

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("登录 - 密码过短")
        void login_ShortPassword() throws Exception {
            loginRequest.setPassword("123");
            when(authService.login(any(LoginRequest.class)))
                    .thenThrow(new com.family.myfamily.exception.BusinessException("用户名或密码错误"));

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("注册 - 无效邮箱格式")
        void register_InvalidEmail() throws Exception {
            registerRequest.setEmail("invalid-email");
            when(authService.register(any(RegisterRequest.class)))
                    .thenThrow(new com.family.myfamily.exception.BusinessException("邮箱格式不正确"));

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andExpect(status().isOk());
        }
    }
}
