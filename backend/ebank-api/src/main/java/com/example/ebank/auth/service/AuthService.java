package com.example.ebank.auth.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ebank.accounts.repository.jdbc.AccountRepositoryJdbc;
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
    private final AccountRepositoryJdbc accountRepository;

    public AuthService(UserRepositoryJdbc userRepository,
                       RoleRepositoryJdbc roleRepository,
                       JwtUtil jwtUtil,
                       AccountRepositoryJdbc accountRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.jwtUtil = jwtUtil;
        this.accountRepository = accountRepository;
    }

    // ★ よわよわログイン（SQLiがそのまま効く）
    public List<User> loginWeak(String username, String password) {
        return userRepository.findByUsernameAndPasswordVuln(username, password);
    }
    
    // ★ 登録処理
    @Transactional
    public RegisterResponse register(RegisterRequest request) {

        String username = request.getUsername();
        String password = request.getPassword();

        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists: " + username);
        }

        // ① usersテーブルにユーザ作成 → userIdが返る
        Long userId = userRepository.createUser(username, password);

        // ② accountsテーブルにメイン口座作成
        String accountNumber = accountRepository.createMainAccountForUser(userId);

        // ③ 結果返す
        RegisterResponse res = new RegisterResponse();
        res.setUsername(username);
        res.setAccountNumber(accountNumber);
        res.setMessage("User registered and main account created");

        return res;
    }

}



