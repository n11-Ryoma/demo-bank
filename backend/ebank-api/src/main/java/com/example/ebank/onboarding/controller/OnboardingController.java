package com.example.ebank.onboarding.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ebank.onboarding.dto.OpenAccountRequest;
import com.example.ebank.onboarding.dto.OpenAccountResponse;
import com.example.ebank.onboarding.service.OnboardingService;

@RestController
@RequestMapping("/api/onboarding")
public class OnboardingController {

    private final OnboardingService service;

    public OnboardingController(OnboardingService service) {
        this.service = service;
    }

    @PostMapping("/open-account")
    public OpenAccountResponse openAccount(@RequestBody OpenAccountRequest req) {
        return service.openAccount(req);
    }
}
