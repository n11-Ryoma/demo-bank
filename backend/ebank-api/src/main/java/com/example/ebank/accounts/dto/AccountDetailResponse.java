package com.example.ebank.accounts.dto;

import java.time.OffsetDateTime;

public class AccountDetailResponse {

    private Long accountId;
    private String branchCode;
    private String accountType;
    private String accountNumberMasked;
    private String status;
    private OffsetDateTime openedAt;
    private Long balance;

    public AccountDetailResponse() {
    }

    public AccountDetailResponse(Long accountId, String branchCode, String accountType, String accountNumberMasked,
                                 String status, OffsetDateTime openedAt, Long balance) {
        this.accountId = accountId;
        this.branchCode = branchCode;
        this.accountType = accountType;
        this.accountNumberMasked = accountNumberMasked;
        this.status = status;
        this.openedAt = openedAt;
        this.balance = balance;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public OffsetDateTime getOpenedAt() {
        return openedAt;
    }

    public void setOpenedAt(OffsetDateTime openedAt) {
        this.openedAt = openedAt;
    }

    public Long getBalance() {
        return balance;
    }

    public void setBalance(Long balance) {
        this.balance = balance;
    }
}
