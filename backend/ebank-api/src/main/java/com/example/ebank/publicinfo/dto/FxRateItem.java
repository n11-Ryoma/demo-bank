package com.example.ebank.publicinfo.dto;

public class FxRateItem {
    private String base;
    private String quote;
    private double rate;

    public FxRateItem(String base, String quote, double rate) {
        this.base = base;
        this.quote = quote;
        this.rate = rate;
    }

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public String getQuote() {
        return quote;
    }

    public void setQuote(String quote) {
        this.quote = quote;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }
}
