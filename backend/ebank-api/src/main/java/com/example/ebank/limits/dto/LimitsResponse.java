package com.example.ebank.limits.dto;

import java.time.OffsetDateTime;

public class LimitsResponse {

    private long transferLimitYen;
    private long atmWithdrawLimitYen;
    private OffsetDateTime updatedAt;

    public long getTransferLimitYen() {
        return transferLimitYen;
    }

    public void setTransferLimitYen(long transferLimitYen) {
        this.transferLimitYen = transferLimitYen;
    }

    public long getAtmWithdrawLimitYen() {
        return atmWithdrawLimitYen;
    }

    public void setAtmWithdrawLimitYen(long atmWithdrawLimitYen) {
        this.atmWithdrawLimitYen = atmWithdrawLimitYen;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
