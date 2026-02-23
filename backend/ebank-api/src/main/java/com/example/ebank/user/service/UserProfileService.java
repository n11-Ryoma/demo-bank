package com.example.ebank.user.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.example.ebank.accounts.dto.CashOperationRequest;
import com.example.ebank.auth.jwt.JwtUtil;
import com.example.ebank.user.entity.UserProfile;
import com.example.ebank.user.repository.jdbc.UserProfileRepositoryJdbc;

@Service
public class UserProfileService {

    private static final Logger log = LogManager.getLogger(UserProfileService.class);
    private final UserProfileRepositoryJdbc repo;
    private final JwtUtil jwtUtil;

    public UserProfileService(UserProfileRepositoryJdbc repo, JwtUtil jwtUtil) {
        this.repo = repo;
        this.jwtUtil = jwtUtil;
    }

    public UserProfile getProfile(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody CashOperationRequest request) {
        log.info("getProfile called");
        String token = authHeader.replace("Bearer ", "").trim();
        String userid = jwtUtil.extractUsername(token);
        return repo.findByUserId(userid);
    }

    public UserProfile getProfile(long userid) {
        log.info("getProfile called: userId={}", userid);
        return null;
    }
}
