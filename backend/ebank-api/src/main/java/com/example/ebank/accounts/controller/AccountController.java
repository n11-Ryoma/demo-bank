package com.example.ebank.accounts.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ebank.accounts.dto.BalanceResponse;
import com.example.ebank.accounts.dto.CashOperationRequest;
import com.example.ebank.accounts.dto.TransactionHistoryItem;
import com.example.ebank.accounts.dto.TransferRequest;
import com.example.ebank.accounts.dto.TransferResponse;
import com.example.ebank.accounts.service.AccountService;
import com.example.ebank.auth.jwt.JwtUtil;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;
    private final JwtUtil jwtUtil;

    public AccountController(AccountService accountService,
                             JwtUtil jwtUtil) {
        this.accountService = accountService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/balance")
    public BalanceResponse getBalance(
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "").trim();
        String username = jwtUtil.extractUsername(token);

        return accountService.getMyMainBalance(username);
    }
    @PostMapping("/deposit")
    public BalanceResponse deposit(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody CashOperationRequest request) {
        String token = authHeader.replace("Bearer ", "").trim();
        String username = jwtUtil.extractUsername(token);
        return accountService.deposit(username, request);
    }

    @PostMapping("/withdraw")
    public BalanceResponse withdraw(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody CashOperationRequest request) {
        String token = authHeader.replace("Bearer ", "").trim();
        String username = jwtUtil.extractUsername(token);
        return accountService.withdraw(username, request);
    }

    @PostMapping("/transfer")
    public TransferResponse transfer(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody TransferRequest request) {
        String token = authHeader.replace("Bearer ", "").trim();
        String username = jwtUtil.extractUsername(token);
        return accountService.transfer(username, request);
    }

    @GetMapping("/transactions")
    public List<TransactionHistoryItem> getHistory(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset
    			) {
        String token = authHeader.replace("Bearer ", "").trim();
        String username = jwtUtil.extractUsername(token);
        return accountService.getMyHistory(username, limit, offset);
    }
}



