package com.example.ebank.limits.dto;

public class LimitsUpdateRequest {

    private Long transferLimitYen;
    private Long atmWithdrawLimitYen;

    public Long getTransferLimitYen() {
        return transferLimitYen;
    }

    public void setTransferLimitYen(Long transferLimitYen) {
        this.transferLimitYen = transferLimitYen;
    }

    public Long getAtmWithdrawLimitYen() {
        return atmWithdrawLimitYen;
    }

    public void setAtmWithdrawLimitYen(Long atmWithdrawLimitYen) {
        this.atmWithdrawLimitYen = atmWithdrawLimitYen;
    }
}
