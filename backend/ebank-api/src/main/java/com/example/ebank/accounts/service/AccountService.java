package com.example.ebank.accounts.service;

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
    		/*
        // username → user 情報取得
	    	List<User> users = userRepository.findByUsername(username);
	
	    	if (users.isEmpty()) {
	    	    throw new RuntimeException("User not found");
	    	}
	
	    	User user = users.get(0);
		*/
        // 口座取得
        Account acc = accountRepository.findByAccountNumber(accountNumber);
        if (acc == null) {
            throw new RuntimeException("Account not found");
        }
        	/*
        // 所有者チェック（IDOR対策）
        if (!acc.getUserId().equals(user.getId())) {
            throw new RuntimeException("Access denied: not your account");
        }
		*/
        return new BalanceResponse(acc.getAccountNumber(), acc.getBalance());
    }
}

