package com.family.myfamily.controller;

import com.family.myfamily.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/test")
@RequiredArgsConstructor
public class TestController {

    @GetMapping("/bcrypt")
    public Result<String> generateBCrypt(@RequestParam String password) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode(password);
        return Result.success(hash);
    }

    @GetMapping("/bcrypt/verify")
    public Result<Boolean> verifyBCrypt(@RequestParam String raw, @RequestParam String encoded) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        boolean matches = encoder.matches(raw, encoded);
        return Result.success(matches);
    }
}
