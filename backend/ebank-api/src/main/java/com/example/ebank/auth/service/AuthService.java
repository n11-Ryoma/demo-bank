package com.example.ebank.auth.service;

import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ebank.accounts.repository.jdbc.AccountRepositoryJdbc;
import com.example.ebank.auth.dto.RegisterRequest;
import com.example.ebank.auth.dto.RegisterResponse;
import com.example.ebank.auth.entity.User;
import com.example.ebank.auth.jwt.JwtUtil;
import com.example.ebank.auth.repository.jdbc.UserRepositoryJdbc;

@Service
public class AuthService {

    private static final Logger log = LogManager.getLogger(AuthService.class);
    private final UserRepositoryJdbc userRepository;
    private final JwtUtil jwtUtil;
    private final AccountRepositoryJdbc accountRepository;

    public AuthService(UserRepositoryJdbc userRepository,
                       JwtUtil jwtUtil,
                       AccountRepositoryJdbc accountRepository) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.accountRepository = accountRepository;
    }
    public List<User> loginWeak(String username, String password) {
        log.info("loginWeak called: username={}", username);
        return userRepository.findByUsernameAndPasswordVuln(username, password);
    }

    public Optional<Long> findUserIdByUsername(String username) {
        log.info("findUserIdByUsername called: username={}", username);
        return userRepository.findIdByUsernameSafe(username);
    }
    

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        log.info("register called: username={}", request == null ? null : request.getUsername());

        String username = request.getUsername();
        String password = request.getPassword();

        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists: " + username);
        }

        String email = request.getEmail();
        Long userId = userRepository.createUser(username, password, email);

        String accountNumber = accountRepository.createMainAccountForUser(userId).getAccountNumber();

        RegisterResponse res = new RegisterResponse();
        res.setUsername(username);
        res.setAccountNumber(accountNumber);
        res.setMessage("User registered and main account created");

        return res;
    }


}




