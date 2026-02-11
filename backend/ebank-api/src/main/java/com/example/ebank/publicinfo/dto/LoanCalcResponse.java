package com.example.ebank.publicinfo.dto;

public class LoanCalcResponse {
    private long amount;
    private double ratePercent;
    private int months;
    private double monthlyPayment;
    private double totalPayment;
    private double totalInterest;

    public LoanCalcResponse(long amount, double ratePercent, int months,
                            double monthlyPayment, double totalPayment, double totalInterest) {
        this.amount = amount;
        this.ratePercent = ratePercent;
        this.months = months;
        this.monthlyPayment = monthlyPayment;
        this.totalPayment = totalPayment;
        this.totalInterest = totalInterest;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public double getRatePercent() {
        return ratePercent;
    }

    public void setRatePercent(double ratePercent) {
        this.ratePercent = ratePercent;
    }

    public int getMonths() {
        return months;
    }

    public void setMonths(int months) {
        this.months = months;
    }

    public double getMonthlyPayment() {
        return monthlyPayment;
    }

    public void setMonthlyPayment(double monthlyPayment) {
        this.monthlyPayment = monthlyPayment;
    }

    public double getTotalPayment() {
        return totalPayment;
    }

    public void setTotalPayment(double totalPayment) {
        this.totalPayment = totalPayment;
    }

    public double getTotalInterest() {
        return totalInterest;
    }

    public void setTotalInterest(double totalInterest) {
        this.totalInterest = totalInterest;
    }
}
