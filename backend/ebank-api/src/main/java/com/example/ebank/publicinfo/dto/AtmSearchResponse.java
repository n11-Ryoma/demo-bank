package com.example.ebank.publicinfo.dto;

import java.util.List;

public class AtmSearchResponse {
    private List<AtmLocation> items;
    private int page;
    private int size;
    private long total;
    private String sort;

    public AtmSearchResponse(List<AtmLocation> items, int page, int size, long total, String sort) {
        this.items = items;
        this.page = page;
        this.size = size;
        this.total = total;
        this.sort = sort;
    }

    public List<AtmLocation> getItems() {
        return items;
    }

    public void setItems(List<AtmLocation> items) {
        this.items = items;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }
}
