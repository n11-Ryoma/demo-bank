package com.example.ebank.publicinfo.dto;

public class RateItem {
    private String category;
    private String product;
    private double ratePercent;
    private String term;
    private String note;

    public RateItem(String category, String product, double ratePercent, String term, String note) {
        this.category = category;
        this.product = product;
        this.ratePercent = ratePercent;
        this.term = term;
        this.note = note;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public double getRatePercent() {
        return ratePercent;
    }

    public void setRatePercent(double ratePercent) {
        this.ratePercent = ratePercent;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
