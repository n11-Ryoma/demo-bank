package com.example.ebank.auth.controller;

import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ebank.auth.dto.AuthResponse;
import com.example.ebank.auth.dto.LoginRequest;
import com.example.ebank.auth.dto.RegisterRequest;
import com.example.ebank.auth.dto.RegisterResponse;
import com.example.ebank.auth.entity.User;
import com.example.ebank.auth.jwt.JwtUtil;
import com.example.ebank.auth.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LogManager.getLogger(AuthController.class);

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    public AuthController(AuthService authService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }

    // ==========================
    //  脆弱なログイン（SQLi OK）
    // ==========================
    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {

        log.info("Login attempt: {}", request.getUsername());

        // よわよわ構成（SQLiするならこの構造が大正解）
        List<User> users =
                authService.loginWeak(request.getUsername(), request.getPassword());

        if (users.isEmpty()) {
            log.warn("Login failed: no user");
            throw new RuntimeException("Invalid username or password");
        }

        // ★ 脆弱点：
        // 複数ユーザが返ったら勝手に先頭を採用
        User user = users.get(0);

        // JWT 発行
        //String token = jwtUtil.generateToken(user.getUsername());
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), Collections.emptyList());
        return new AuthResponse(token, "success");
    }
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest request) {
        RegisterResponse res = authService.register(request);
        return ResponseEntity.ok(res);
    }
}
