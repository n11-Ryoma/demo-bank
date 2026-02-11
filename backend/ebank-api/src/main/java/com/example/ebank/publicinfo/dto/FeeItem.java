package com.example.ebank.publicinfo.dto;

public class FeeItem {
    private String service;
    private String channel;
    private int amountYen;
    private String note;

    public FeeItem(String service, String channel, int amountYen, String note) {
        this.service = service;
        this.channel = channel;
        this.amountYen = amountYen;
        this.note = note;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public int getAmountYen() {
        return amountYen;
    }

    public void setAmountYen(int amountYen) {
        this.amountYen = amountYen;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
