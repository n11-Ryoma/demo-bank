package com.example.ebank.limits.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ebank.auth.security.AuthTokenParser;
import com.example.ebank.limits.dto.LimitsResponse;
import com.example.ebank.limits.dto.LimitsUpdateRequest;
import com.example.ebank.limits.service.LimitsService;

@RestController
@RequestMapping("/api/limits")
public class LimitsController {

    private final AuthTokenParser authTokenParser;
    private final LimitsService limitsService;

    public LimitsController(AuthTokenParser authTokenParser, LimitsService limitsService) {
        this.authTokenParser = authTokenParser;
        this.limitsService = limitsService;
    }

    @GetMapping
    public LimitsResponse getLimits(@RequestHeader("Authorization") String authHeader) {
        Long userId = authTokenParser.extractUserId(authHeader);
        return limitsService.get(userId);
    }

    @PutMapping
    public LimitsResponse updateLimits(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody LimitsUpdateRequest request) {
        Long userId = authTokenParser.extractUserId(authHeader);
        return limitsService.update(userId, request);
    }
}
