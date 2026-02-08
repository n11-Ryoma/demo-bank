package com.example.ebank.user.service;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.example.ebank.accounts.dto.CashOperationRequest;
import com.example.ebank.auth.jwt.JwtUtil;
import com.example.ebank.user.entity.UserProfile;
import com.example.ebank.user.repository.jdbc.UserProfileRepositoryJdbc;

@Service
public class UserProfileService {

    private final UserProfileRepositoryJdbc repo;
    private final JwtUtil jwtUtil;
    public UserProfileService(UserProfileRepositoryJdbc repo,JwtUtil jwtUtil) {
        this.repo = repo;
        this.jwtUtil = jwtUtil;
    }

    public UserProfile getProfile(
    		@RequestHeader("Authorization") String authHeader,
        @RequestBody CashOperationRequest request) {
        String token = authHeader.replace("Bearer ", "").trim();
        String userid = jwtUtil.extractUsername(token);
        return repo.findByUserId(userid);
    }

	public UserProfile getProfile(long userid) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}
}

