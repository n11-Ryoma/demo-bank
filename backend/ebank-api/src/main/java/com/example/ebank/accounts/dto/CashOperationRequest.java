package com.example.ebank.accounts.dto;

//com.example.ebank.account.dto.CashOperationRequest
public class CashOperationRequest {
 private long amount;
 private String description; // 任意（ATM入金、テスト入金など）

 public long getAmount() { return amount; }
 public void setAmount(long amount) { this.amount = amount; }

 public String getDescription() { return description; }
 public void setDescription(String description) { this.description = description; }
}

