package com.example.ebank.accounts.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ebank.accounts.dto.AccountDetailResponse;
import com.example.ebank.accounts.dto.AccountSummaryItem;
import com.example.ebank.accounts.dto.BalanceResponse;
import com.example.ebank.accounts.dto.CashOperationRequest;
import com.example.ebank.accounts.dto.TransactionHistoryItem;
import com.example.ebank.accounts.dto.TransferRequest;
import com.example.ebank.accounts.dto.TransferResponse;
import com.example.ebank.accounts.service.AccountService;
import com.example.ebank.auth.jwt.JwtUtil;
import com.example.ebank.auth.security.AuthTokenParser;
import com.example.ebank.observability.AuditLogger;
import com.example.ebank.observability.HttpMeta;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;
    private final JwtUtil jwtUtil;
    private final AuthTokenParser authTokenParser;
    private final AuditLogger audit;

    public AccountController(AccountService accountService,
                             JwtUtil jwtUtil,
                             AuthTokenParser authTokenParser,
                             AuditLogger audit) {
        this.accountService = accountService;
        this.jwtUtil = jwtUtil;
        this.authTokenParser = authTokenParser;
        this.audit = audit;
    }

    @GetMapping("/balance")
    public BalanceResponse getBalance(
            @RequestHeader("Authorization") String authHeader,
            HttpServletRequest httpReq) {

        long start = System.nanoTime();
        String token = authHeader.replace("Bearer ", "").trim();
        String username = jwtUtil.extractUsername(token);
        String ip = httpReq.getRemoteAddr();
        String ua = httpReq.getHeader("User-Agent");

        try {
            BalanceResponse res = accountService.getMyMainBalance(username);
            long latencyMs = (System.nanoTime() - start) / 1_000_000;
            audit.success(
                    "ACCOUNT_BALANCE",
                    username,
                    res.getAccountNumber(),
                    null,
                    new HttpMeta("/api/accounts/balance", "GET", 200, ip, ua == null ? "" : ua, latencyMs),
                    Map.of()
            );
            return res;
        } catch (RuntimeException e) {
            long latencyMs = (System.nanoTime() - start) / 1_000_000;
            audit.fail(
                    "ACCOUNT_BALANCE",
                    username,
                    null,
                    null,
                    e.getClass().getSimpleName(),
                    new HttpMeta("/api/accounts/balance", "GET", 500, ip, ua == null ? "" : ua, latencyMs),
                    Map.of()
            );
            throw e;
        }
    }
    @PostMapping("/deposit")
    public BalanceResponse deposit(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody CashOperationRequest request,
            HttpServletRequest httpReq) {
        long start = System.nanoTime();
        String token = authHeader.replace("Bearer ", "").trim();
        String username = jwtUtil.extractUsername(token);
        String ip = httpReq.getRemoteAddr();
        String ua = httpReq.getHeader("User-Agent");
        try {
            BalanceResponse res = accountService.deposit(username, request);
            long latencyMs = (System.nanoTime() - start) / 1_000_000;
            audit.success(
                    "ACCOUNT_DEPOSIT",
                    username,
                    res.getAccountNumber(),
                    request.getAmount(),
                    new HttpMeta("/api/accounts/deposit", "POST", 200, ip, ua == null ? "" : ua, latencyMs),
                    Map.of()
            );
            return res;
        } catch (RuntimeException e) {
            long latencyMs = (System.nanoTime() - start) / 1_000_000;
            audit.fail(
                    "ACCOUNT_DEPOSIT",
                    username,
                    null,
                    request.getAmount(),
                    e.getClass().getSimpleName(),
                    new HttpMeta("/api/accounts/deposit", "POST", 500, ip, ua == null ? "" : ua, latencyMs),
                    Map.of()
            );
            throw e;
        }
    }

    @PostMapping("/withdraw")
    public BalanceResponse withdraw(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody CashOperationRequest request,
            HttpServletRequest httpReq) {
        long start = System.nanoTime();
        String token = authHeader.replace("Bearer ", "").trim();
        String username = jwtUtil.extractUsername(token);
        String ip = httpReq.getRemoteAddr();
        String ua = httpReq.getHeader("User-Agent");
        try {
            BalanceResponse res = accountService.withdraw(username, request);
            long latencyMs = (System.nanoTime() - start) / 1_000_000;
            audit.success(
                    "ACCOUNT_WITHDRAW",
                    username,
                    res.getAccountNumber(),
                    request.getAmount(),
                    new HttpMeta("/api/accounts/withdraw", "POST", 200, ip, ua == null ? "" : ua, latencyMs),
                    Map.of()
            );
            return res;
        } catch (RuntimeException e) {
            long latencyMs = (System.nanoTime() - start) / 1_000_000;
            audit.fail(
                    "ACCOUNT_WITHDRAW",
                    username,
                    null,
                    request.getAmount(),
                    e.getClass().getSimpleName(),
                    new HttpMeta("/api/accounts/withdraw", "POST", 500, ip, ua == null ? "" : ua, latencyMs),
                    Map.of()
            );
            throw e;
        }
    }

    @PostMapping("/transfer")
    public TransferResponse transfer(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody TransferRequest request,
            HttpServletRequest httpReq) {
        long start = System.nanoTime();
        String token = authHeader.replace("Bearer ", "").trim();
        String username = jwtUtil.extractUsername(token);
        String ip = httpReq.getRemoteAddr();
        String ua = httpReq.getHeader("User-Agent");
        try {
            TransferResponse res = accountService.transfer(username, request);
            long latencyMs = (System.nanoTime() - start) / 1_000_000;
            audit.success(
                    "ACCOUNT_TRANSFER",
                    username,
                    res.getToAccountNumber(),
                    request.getAmount(),
                    new HttpMeta("/api/accounts/transfer", "POST", 200, ip, ua == null ? "" : ua, latencyMs),
                    Map.of("fromAccount", res.getFromAccountNumber())
            );
            return res;
        } catch (RuntimeException e) {
            long latencyMs = (System.nanoTime() - start) / 1_000_000;
            audit.fail(
                    "ACCOUNT_TRANSFER",
                    username,
                    request.getToAccountNumber(),
                    request.getAmount(),
                    e.getClass().getSimpleName(),
                    new HttpMeta("/api/accounts/transfer", "POST", 500, ip, ua == null ? "" : ua, latencyMs),
                    Map.of()
            );
            throw e;
        }
    }

    @GetMapping("/transactions")
    public List<TransactionHistoryItem> getHistory(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "") String findStr,
            HttpServletRequest httpReq
    			) {
        long start = System.nanoTime();
        String token = authHeader.replace("Bearer ", "").trim();
        String username = jwtUtil.extractUsername(token);
        String ip = httpReq.getRemoteAddr();
        String ua = httpReq.getHeader("User-Agent");
        if("".equals(findStr)) { 
        		List<TransactionHistoryItem> res = accountService.getMyHistory(username, limit, offset);
                long latencyMs = (System.nanoTime() - start) / 1_000_000;
                audit.success(
                        "ACCOUNT_HISTORY",
                        username,
                        null,
                        null,
                        new HttpMeta("/api/accounts/transactions", "GET", 200, ip, ua == null ? "" : ua, latencyMs),
                        Map.of("limit", limit, "offset", offset, "findStr", findStr)
                );
        		return res;
        }
        else {
        		List<TransactionHistoryItem> res = accountService.getMyHistoryFindStr(username, limit, offset,findStr);
                long latencyMs = (System.nanoTime() - start) / 1_000_000;
                audit.success(
                        "ACCOUNT_HISTORY",
                        username,
                        null,
                        null,
                        new HttpMeta("/api/accounts/transactions", "GET", 200, ip, ua == null ? "" : ua, latencyMs),
                        Map.of("limit", limit, "offset", offset, "findStr", findStr)
                );
        		return res;
        }
    }

    @GetMapping
    public List<AccountSummaryItem> getMyAccounts(@RequestHeader("Authorization") String authHeader) {
        String username = authTokenParser.extractUsername(authHeader);
        return accountService.getMyAccounts(username);
    }

    @GetMapping("/{accountId}")
    public AccountDetailResponse getMyAccountDetail(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable long accountId) {
        String username = authTokenParser.extractUsername(authHeader);
        return accountService.getMyAccountDetail(username, accountId);
    }
}



