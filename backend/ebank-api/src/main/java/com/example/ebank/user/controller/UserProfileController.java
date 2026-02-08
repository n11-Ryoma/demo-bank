package com.example.ebank.user.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ebank.auth.jwt.JwtUtil;
import com.example.ebank.user.entity.UserProfile;
import com.example.ebank.user.service.UserProfileService;

@RestController
@RequestMapping("/api/user/profile")
public class UserProfileController {

    private final UserProfileService service;
    private final JwtUtil jwtUtil;
    public UserProfileController(UserProfileService service,JwtUtil jwtUtil) {
        this.service = service;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping
    public UserProfile getMyProfile(@RequestHeader("Authorization") String authHeader) {
        // JwtAuthenticationFilter で入れた userId を取り出す想定
        //Long userId = extractUserId(auth);
        
        String token = authHeader.replace("Bearer ", "").trim();
        long userid = jwtUtil.extractUserId(token);    	
    		return service.getProfile(userid);
    }
    /*
    @PostMapping("/profile")
    public void updateProfile(
        @RequestBody UserProfileRequest req,
        Authentication auth
    ) {
        Long userId = extractUserId(auth);
        service.updateProfile(userId, req);
    }*/

}

