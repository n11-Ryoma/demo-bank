package com.example.ebank.publicinfo.dto;

import java.util.List;

public class FxRatesResponse {
    private String asOf;
    private String base;
    private List<FxRateItem> items;

    public FxRatesResponse(String asOf, String base, List<FxRateItem> items) {
        this.asOf = asOf;
        this.base = base;
        this.items = items;
    }

    public String getAsOf() {
        return asOf;
    }

    public void setAsOf(String asOf) {
        this.asOf = asOf;
    }

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public List<FxRateItem> getItems() {
        return items;
    }

    public void setItems(List<FxRateItem> items) {
        this.items = items;
    }
}
