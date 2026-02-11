package com.example.ebank.publicinfo.dto;

import java.util.List;

public class AtmLocation {
    private String id;
    private String name;
    private String pref;
    private String city;
    private String address;
    private double lat;
    private double lng;
    private boolean openNow;
    private boolean cash;
    private List<String> services;
    private String hours;
    private String mapLink;
    private String updatedAt;

    public AtmLocation(String id, String name, String pref, String city, String address,
                       double lat, double lng, boolean openNow, boolean cash,
                       List<String> services, String hours, String mapLink, String updatedAt) {
        this.id = id;
        this.name = name;
        this.pref = pref;
        this.city = city;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
        this.openNow = openNow;
        this.cash = cash;
        this.services = services;
        this.hours = hours;
        this.mapLink = mapLink;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPref() {
        return pref;
    }

    public void setPref(String pref) {
        this.pref = pref;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public boolean isOpenNow() {
        return openNow;
    }

    public void setOpenNow(boolean openNow) {
        this.openNow = openNow;
    }

    public boolean isCash() {
        return cash;
    }

    public void setCash(boolean cash) {
        this.cash = cash;
    }

    public List<String> getServices() {
        return services;
    }

    public void setServices(List<String> services) {
        this.services = services;
    }

    public String getHours() {
        return hours;
    }

    public void setHours(String hours) {
        this.hours = hours;
    }

    public String getMapLink() {
        return mapLink;
    }

    public void setMapLink(String mapLink) {
        this.mapLink = mapLink;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
