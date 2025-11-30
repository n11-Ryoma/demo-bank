package com.example.ebank.accounts.dto;

//com.example.eshop.accounts.dto.BalanceResponse
public class BalanceResponse {
	 private String accountNumber;
	 private Long balance;
	 
	 public BalanceResponse(String accountNumber, Long balance) {
	     this.accountNumber = accountNumber;
	     this.balance = balance;
	 }
	 public String getAccountNumber() {
		return accountNumber;
	 }
	 public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	 }
	 public Long getBalance() {
		return balance;
	 }
	 public void setBalance(Long balance) {
		this.balance = balance;
	 }
}

