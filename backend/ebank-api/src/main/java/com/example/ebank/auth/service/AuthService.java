package com.example.ebank.auth.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.ebank.auth.dto.RegisterRequest;
import com.example.ebank.auth.dto.RegisterResponse;
import com.example.ebank.auth.entity.User;
import com.example.ebank.auth.jwt.JwtUtil;
import com.example.ebank.auth.repository.jdbc.RoleRepositoryJdbc;
import com.example.ebank.auth.repository.jdbc.UserRepositoryJdbc;

@Service
public class AuthService {

    private final UserRepositoryJdbc userRepository;
    private final RoleRepositoryJdbc roleRepository;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepositoryJdbc userRepository,
                       RoleRepositoryJdbc roleRepository,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.jwtUtil = jwtUtil;
    }

    // ★ よわよわログイン（SQLiがそのまま効く）
    public List<User> loginWeak(String username, String password) {
        return userRepository.findByUsernameAndPasswordVuln(username, password);
    }
    
    public RegisterResponse register(RegisterRequest request) {

        // username だけで検索する
        List<User> existing = userRepository.findByUsername(request.getUsername());

        if (!existing.isEmpty()) {
            return new RegisterResponse("error", "Username already exists");
        }

        // 新規ユーザー作成
        int userId = userRepository.insertUser(
                request.getUsername(),
                request.getPassword()
        );

        // USER ロール付与
        int roleId = roleRepository.findRoleIdByName("USER");
        roleRepository.insertUserRole(userId, roleId);

        return new RegisterResponse("success", "User registered successfully");
    }


}



