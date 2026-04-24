package com.family.myfamily.controller;

import com.family.myfamily.common.Result;
import com.family.myfamily.dto.LoginRequest;
import com.family.myfamily.dto.LoginResponse;
import com.family.myfamily.dto.RegisterRequest;
import com.family.myfamily.dto.UserDTO;
import com.family.myfamily.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return Result.success("登录成功", response);
    }

    @PostMapping("/register")
    public Result<UserDTO> register(@Valid @RequestBody RegisterRequest request) {
        UserDTO user = authService.register(request);
        return Result.created("注册成功", user);
    }

    @GetMapping("/me")
    public Result<UserDTO> getCurrentUser(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        var user = authService.getCurrentUser(userId);
        UserDTO dto = new UserDTO();
        org.springframework.beans.BeanUtils.copyProperties(user, dto);
        return Result.success(dto);
    }
}
