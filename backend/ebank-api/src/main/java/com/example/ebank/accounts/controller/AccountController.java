/**
 * 
 */
package com.example.ebank.accounts.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ebank.accounts.dto.BalanceResponse;
import com.example.ebank.accounts.service.AccountService;
import com.example.ebank.auth.jwt.JwtUtil;

/**
 * 
 */
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

    @GetMapping("/balance")
    public BalanceResponse getBalance(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam String accountNumber) {

        // "Bearer xxxxx.yyyyy.zzzzz" からトークンだけ取り出す
        String token = authHeader.replace("Bearer ", "");
        // JwtUtil でユーザ名を取り出す
        String username = jwtUtil.extractUsername(token);
        // username と accountNumber をサービスへ渡す
        return accountService.getBalance(username, accountNumber);
        
    }
}


