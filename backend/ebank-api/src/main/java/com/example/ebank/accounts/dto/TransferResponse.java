package com.example.ebank.accounts.dto;

//com.example.ebank.account.dto.TransferResponse
public class TransferResponse {
 private String fromAccountNumber;
 private String toAccountNumber;
 private long amount;
 private long newBalance; // from側の取引後残高

 public TransferResponse(String fromAccountNumber,
                         String toAccountNumber,
                         long amount,
                         long newBalance) {
     this.fromAccountNumber = fromAccountNumber;
     this.toAccountNumber = toAccountNumber;
     this.amount = amount;
     this.newBalance = newBalance;
 }

 public String getFromAccountNumber() { return fromAccountNumber; }
 public String getToAccountNumber() { return toAccountNumber; }
 public long getAmount() { return amount; }
 public long getNewBalance() { return newBalance; }
}

