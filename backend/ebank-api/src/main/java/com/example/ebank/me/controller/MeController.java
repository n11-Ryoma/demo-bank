package com.example.ebank.me.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.example.ebank.auth.security.AuthTokenParser;
import com.example.ebank.me.dto.MeResponse;
import com.example.ebank.me.service.MeService;

@RestController
public class MeController {

    private final AuthTokenParser authTokenParser;
    private final MeService meService;

    public MeController(AuthTokenParser authTokenParser, MeService meService) {
        this.authTokenParser = authTokenParser;
        this.meService = meService;
    }

    @GetMapping("/api/me")
    public MeResponse getMe(@RequestHeader("Authorization") String authHeader) {
        String username = authTokenParser.extractUsername(authHeader);
        return meService.getByUsername(username);
    }
}
