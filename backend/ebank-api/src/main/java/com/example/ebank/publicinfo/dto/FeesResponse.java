package com.example.ebank.publicinfo.dto;

import java.util.List;

public class FeesResponse {
    private String asOf;
    private List<FeeItem> items;

    public FeesResponse(String asOf, List<FeeItem> items) {
        this.asOf = asOf;
        this.items = items;
    }

    public String getAsOf() {
        return asOf;
    }

    public void setAsOf(String asOf) {
        this.asOf = asOf;
    }

    public List<FeeItem> getItems() {
        return items;
    }

    public void setItems(List<FeeItem> items) {
        this.items = items;
    }
}
