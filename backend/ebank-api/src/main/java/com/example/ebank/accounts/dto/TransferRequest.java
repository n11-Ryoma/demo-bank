package com.example.ebank.accounts.dto;

//com.example.ebank.account.dto.TransferRequest
public class TransferRequest {
 private String toAccountNumber;
 private long amount;
 private String description; // 任意（メモ）

 public String getToAccountNumber() { return toAccountNumber; }
 public void setToAccountNumber(String toAccountNumber) { this.toAccountNumber = toAccountNumber; }

 public long getAmount() { return amount; }
 public void setAmount(long amount) { this.amount = amount; }

 public String getDescription() { return description; }
 public void setDescription(String description) { this.description = description; }
}

