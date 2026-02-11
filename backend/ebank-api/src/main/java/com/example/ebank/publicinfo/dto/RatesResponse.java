package com.example.ebank.publicinfo.dto;

import java.util.List;

public class RatesResponse {
    private String asOf;
    private List<RateItem> items;

    public RatesResponse(String asOf, List<RateItem> items) {
        this.asOf = asOf;
        this.items = items;
    }

    public String getAsOf() {
        return asOf;
    }

    public void setAsOf(String asOf) {
        this.asOf = asOf;
    }

    public List<RateItem> getItems() {
        return items;
    }

    public void setItems(List<RateItem> items) {
        this.items = items;
    }
}
