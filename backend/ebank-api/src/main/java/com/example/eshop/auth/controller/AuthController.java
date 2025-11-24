package com.example.eshop.auth.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.eshop.auth.dto.AuthResponse;
import com.example.eshop.auth.dto.LoginRequest;
import com.example.eshop.auth.dto.RegisterRequest;
import com.example.eshop.auth.dto.RegisterResponse;
import com.example.eshop.auth.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LogManager.getLogger(AuthController.class);

    //private final UserRepositoryJdbc userRepository;
    //private final RoleRepositoryJdbc roleRepository;

    //private final JwtUtil jwtUtil;   // ★ 追加

    private final AuthService authService;

    // ★ AuthService だけをDIする
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.warn("Login failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("Login failed (exception)", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        
    	
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            RegisterResponse response = authService.register(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Register failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RegisterResponse("error", "Internal server error"));
        }
    }

}

