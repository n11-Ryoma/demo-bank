package com.example.ebank.accounts.entity;

//com.example.eshop.accounts.entity.Account
public class Account {
 private Long id;
 private Long userId;
 private String accountNumber;
 private Long balance; // とりあえず long で
 public Long getId() {
	return id;
 }
 public void setId(Long id) {
	this.id = id;
 }
 public Long getUserId() {
	return userId;
 }
 public void setUserId(Long userId) {
	this.userId = userId;
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

 // getter/setter...
}
