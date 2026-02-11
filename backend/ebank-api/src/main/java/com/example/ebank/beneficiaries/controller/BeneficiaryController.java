package com.example.ebank.beneficiaries.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.ebank.auth.security.AuthTokenParser;
import com.example.ebank.beneficiaries.dto.BeneficiaryRequest;
import com.example.ebank.beneficiaries.dto.BeneficiaryResponse;
import com.example.ebank.beneficiaries.service.BeneficiaryService;

@RestController
@RequestMapping("/api/beneficiaries")
public class BeneficiaryController {

    private final AuthTokenParser authTokenParser;
    private final BeneficiaryService beneficiaryService;

    public BeneficiaryController(AuthTokenParser authTokenParser, BeneficiaryService beneficiaryService) {
        this.authTokenParser = authTokenParser;
        this.beneficiaryService = beneficiaryService;
    }

    @GetMapping
    public List<BeneficiaryResponse> list(@RequestHeader("Authorization") String authHeader) {
        long userId = authTokenParser.extractUserId(authHeader);
        return beneficiaryService.list(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BeneficiaryResponse create(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody BeneficiaryRequest request) {
        long userId = authTokenParser.extractUserId(authHeader);
        return beneficiaryService.create(userId, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {
        long userId = authTokenParser.extractUserId(authHeader);
        beneficiaryService.delete(userId, id);
    }
}
