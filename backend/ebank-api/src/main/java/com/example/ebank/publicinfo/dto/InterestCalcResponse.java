package com.example.ebank.publicinfo.dto;

public class InterestCalcResponse {
    private long principal;
    private double ratePercent;
    private int days;
    private double interest;
    private double total;

    public InterestCalcResponse(long principal, double ratePercent, int days, double interest, double total) {
        this.principal = principal;
        this.ratePercent = ratePercent;
        this.days = days;
        this.interest = interest;
        this.total = total;
    }

    public long getPrincipal() {
        return principal;
    }

    public void setPrincipal(long principal) {
        this.principal = principal;
    }

    public double getRatePercent() {
        return ratePercent;
    }

    public void setRatePercent(double ratePercent) {
        this.ratePercent = ratePercent;
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }

    public double getInterest() {
        return interest;
    }

    public void setInterest(double interest) {
        this.interest = interest;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }
}
