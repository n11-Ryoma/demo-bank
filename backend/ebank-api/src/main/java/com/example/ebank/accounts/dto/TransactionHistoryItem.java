package com.example.ebank.accounts.dto;

//com.example.ebank.account.dto.TransactionHistoryItem
import java.time.OffsetDateTime;

import com.example.ebank.model.TransactionType;

public class TransactionHistoryItem {
 public String getAccountNumber() {
		return accountNumber;
	}
	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}
	public TransactionType getType() {
		return type;
	}
	public void setType(TransactionType type) {
		this.type = type;
	}
	public long getAmount() {
		return amount;
	}
	public void setAmount(long amount) {
		this.amount = amount;
	}
	public long getBalanceAfter() {
		return balanceAfter;
	}
	public void setBalanceAfter(long balanceAfter) {
		this.balanceAfter = balanceAfter;
	}
	public String getRelatedAccountNumber() {
		return relatedAccountNumber;
	}
	public void setRelatedAccountNumber(String relatedAccountNumber) {
		this.relatedAccountNumber = relatedAccountNumber;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(OffsetDateTime createdAt) {
		this.createdAt = createdAt;
	}
 private String accountNumber;
 private TransactionType type;
 private long amount;
 private long balanceAfter;
 private String relatedAccountNumber;
 private String description;
 private OffsetDateTime createdAt;

 // コンストラクタ & getter だけでOK
}

