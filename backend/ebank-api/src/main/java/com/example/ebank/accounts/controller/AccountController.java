package com.example.ebank.accounts.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ebank.accounts.dto.BalanceRequest;
import com.example.ebank.accounts.dto.BalanceResponse;
import com.example.ebank.accounts.service.AccountService;
import com.example.ebank.auth.jwt.JwtUtil;

//com.example.eshop.accounts.controller.AccountController
@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;
    private final JwtUtil jwtUtil;

    public AccountController(AccountService accountService, JwtUtil jwtUtil) {
        this.accountService = accountService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/balance")
    public BalanceResponse getBalance(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody BalanceRequest request) {

        String token = authHeader.replace("Bearer ", "");
        String username = jwtUtil.extractUsername(token);

        return accountService.getBalance(username, request.getAccountNumber());
    }

}


