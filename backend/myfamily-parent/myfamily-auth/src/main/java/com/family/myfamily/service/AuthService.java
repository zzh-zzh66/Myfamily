package com.family.myfamily.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.family.myfamily.common.Constants;
import com.family.myfamily.config.JwtUtils;
import com.family.myfamily.dto.LoginRequest;
import com.family.myfamily.dto.LoginResponse;
import com.family.myfamily.dto.RegisterRequest;
import com.family.myfamily.dto.UserDTO;
import com.family.myfamily.entity.User;
import com.family.myfamily.exception.BusinessException;
import com.family.myfamily.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService extends ServiceImpl<UserMapper, User> {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public LoginResponse login(LoginRequest request) {
        log.info("用户登录: username={}", request.getUsername());

        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, request.getUsername())
                        .eq(User::getDeleted, 0)
        );

        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        if (user.getStatus().equals(Constants.Status.DISABLED)) {
            throw new BusinessException("账号已被禁用");
        }

        String token = jwtUtils.generateToken(user.getId(), user.getRole());

        // 更新最后登录信息
        user.setLastLoginAt(LocalDateTime.now());
        userMapper.updateById(user);

        log.info("用户登录成功: userId={}", user.getId());

        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);

        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(86400L)
                .user(userDTO)
                .build();
    }

    public UserDTO register(RegisterRequest request) {
        log.info("用户注册: username={}", request.getUsername());

        // 检查用户名是否存在
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, request.getUsername())
                        .eq(User::getDeleted, 0)
        );

        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        // 检查邮箱是否存在
        if (request.getEmail() != null) {
            count = userMapper.selectCount(
                    new LambdaQueryWrapper<User>()
                            .eq(User::getEmail, request.getEmail())
                            .eq(User::getDeleted, 0)
            );
            if (count > 0) {
                throw new BusinessException("邮箱已被注册");
            }
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setMemberId(request.getMemberId());
        user.setRole(Constants.Role.MEMBER);
        user.setStatus(Constants.Status.ENABLED);

        userMapper.insert(user);

        log.info("用户注册成功: userId={}", user.getId());

        UserDTO dto = new UserDTO();
        BeanUtils.copyProperties(user, dto);
        return dto;
    }

    public User getCurrentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getDeleted() == 1) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }
}
