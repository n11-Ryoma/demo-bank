package com.example.ebank.accounts.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.ebank.accounts.dto.BalanceResponse;
import com.example.ebank.accounts.entity.Account;
import com.example.ebank.accounts.repository.jdbc.AccountRepositoryJdbc;
import com.example.ebank.auth.repository.jdbc.UserRepositoryJdbc;

@Service
public class AccountService {

    private final AccountRepositoryJdbc accountRepository;
    private final UserRepositoryJdbc userRepository;

    public AccountService(AccountRepositoryJdbc accountRepository,
                          UserRepositoryJdbc userRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    public BalanceResponse getBalance(String username, String accountNumber) {

        // ★ 返り値が List<Account> になる
        List<Account> accounts = accountRepository.findByAccountNumber(accountNumber);

        if (accounts.isEmpty()) {
            throw new RuntimeException("Account not found");
        }

        // ★ よわよわ：1件目だけ利用（SQLi で大量に返っても対応）
        Account acc = accounts.get(0);

        // ★ IDOR のためユーザチェックは削除
        return new BalanceResponse(acc.getAccountNumber(), acc.getBalance());
    }

}

