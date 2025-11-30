package com.example.ebank.accounts.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ebank.accounts.dto.BalanceResponse;
import com.example.ebank.accounts.dto.CashOperationRequest;
import com.example.ebank.accounts.dto.TransactionHistoryItem;
import com.example.ebank.accounts.dto.TransferRequest;
import com.example.ebank.accounts.dto.TransferResponse;
import com.example.ebank.accounts.entity.Account;
import com.example.ebank.accounts.repository.jdbc.AccountRepositoryJdbc;
import com.example.ebank.model.TransactionType;

@Service
public class AccountService {

    private final AccountRepositoryJdbc accountRepository;

    public AccountService(AccountRepositoryJdbc accountRepository) {
        this.accountRepository = accountRepository;
    }

    // ← このメソッドを新しく追加
    public BalanceResponse getMyMainBalance(String username) {

        // ① username からメイン口座を1件取得（0件なら例外）
        Account account = accountRepository.findMainByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException("Main account not found for user: " + username));

        // ② BalanceResponse に詰めて返す
        return new BalanceResponse(
                account.getAccountNumber(),
                account.getBalance()
        );
    }
    // ── 入金 ─────────────────────
    @Transactional
    public BalanceResponse deposit(String username, CashOperationRequest req) {
        if (req.getAmount() <= 0) {
            throw new RuntimeException("Amount must be positive");
        }

        Account acc = accountRepository.findMainByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException("Main account not found for user: " + username));

        long newBalance = acc.getBalance() + req.getAmount();
        accountRepository.updateBalance(acc.getId(), newBalance);

        accountRepository.insertTransaction(
                acc.getId(),
                TransactionType.DEPOSIT,
                req.getAmount(),
                newBalance,
                null,
                req.getDescription()
        );

        return new BalanceResponse(acc.getAccountNumber(), newBalance);
    }

    // ── 出金 ─────────────────────
    @Transactional
    public BalanceResponse withdraw(String username, CashOperationRequest req) {
        if (req.getAmount() <= 0) {
            throw new RuntimeException("Amount must be positive");
        }

        Account acc = accountRepository.findMainByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException("Main account not found for user: " + username));

        if (acc.getBalance() < req.getAmount()) {
            throw new RuntimeException("Insufficient balance");
        }

        long newBalance = acc.getBalance() - req.getAmount();
        accountRepository.updateBalance(acc.getId(), newBalance);

        accountRepository.insertTransaction(
                acc.getId(),
                TransactionType.WITHDRAW,
                req.getAmount(),
                newBalance,
                null,
                req.getDescription()
        );

        return new BalanceResponse(acc.getAccountNumber(), newBalance);
    }

    // ── 振込 ─────────────────────
    @Transactional
    public TransferResponse transfer(String username, TransferRequest request) {

        if (request.getAmount() <= 0) {
            throw new RuntimeException("Amount must be positive");
        }

        // 自分のメイン口座
        Account from = accountRepository.findMainByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException("Main account not found for user: " + username));

        // 宛先口座
        Account to = accountRepository.findByAccountNumber(request.getToAccountNumber())
                .orElseThrow(() ->
                        new RuntimeException("Destination account not found: " + request.getToAccountNumber()));

        if (from.getId() == to.getId()) {
            throw new RuntimeException("Cannot transfer to same account");
        }

        if (from.getBalance() < request.getAmount()) {
            throw new RuntimeException("Insufficient balance");
        }

        long newFromBalance = from.getBalance() - request.getAmount();
        long newToBalance   = to.getBalance() + request.getAmount();

        // 残高更新
        accountRepository.updateBalance(from.getId(), newFromBalance);
        accountRepository.updateBalance(to.getId(), newToBalance);

        // 取引履歴（送金側）
        accountRepository.insertTransaction(
                from.getId(),
                TransactionType.TRANSFER_OUT,
                request.getAmount(),
                newFromBalance,
                to.getAccountNumber(),
                request.getDescription()
        );

        // 取引履歴（受取側）
        accountRepository.insertTransaction(
                to.getId(),
                TransactionType.TRANSFER_IN,
                request.getAmount(),
                newToBalance,
                from.getAccountNumber(),
                request.getDescription()
        );

        return new TransferResponse(
                from.getAccountNumber(),
                to.getAccountNumber(),
                request.getAmount(),
                newFromBalance
        );
    }

    // ── 履歴取得 ─────────────────────
    public List<TransactionHistoryItem> getMyHistory(String username, int limit, int offset) {
        return accountRepository.findHistoryByUsername(username, limit, offset);
    }

}


