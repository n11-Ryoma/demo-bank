package com.example.ebank.accounts.dto;

public class AccountSummaryItem {

    private Long accountId;
    private String accountType;
    private String accountNumberMasked;
    private Long balance;
    private String status;

    public AccountSummaryItem() {
    }

    public AccountSummaryItem(Long accountId, String accountType, String accountNumberMasked, Long balance, String status) {
        this.accountId = accountId;
        this.accountType = accountType;
        this.accountNumberMasked = accountNumberMasked;
        this.balance = balance;
        this.status = status;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getAccountNumberMasked() {
        return accountNumberMasked;
    }

    public void setAccountNumberMasked(String accountNumberMasked) {
        this.accountNumberMasked = accountNumberMasked;
    }

    public Long getBalance() {
        return balance;
    }

    public void setBalance(Long balance) {
        this.balance = balance;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
