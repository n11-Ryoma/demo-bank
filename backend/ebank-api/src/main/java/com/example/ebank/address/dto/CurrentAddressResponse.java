// package はプロジェクトに合わせて
package com.example.ebank.address.dto;

public class CurrentAddressResponse {

    private String postalCode;
    private String prefecture;
    private String city;
    private String addressLine1;
    private String addressLine2;

    public CurrentAddressResponse() {}

    public CurrentAddressResponse(String postalCode, String prefecture,
                                  String city, String addressLine1, String addressLine2) {
        this.postalCode = postalCode;
        this.prefecture = prefecture;
        this.city = city;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
    }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

    public String getPrefecture() { return prefecture; }
    public void setPrefecture(String prefecture) { this.prefecture = prefecture; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getAddressLine1() { return addressLine1; }
    public void setAddressLine1(String addressLine1) { this.addressLine1 = addressLine1; }

    public String getAddressLine2() { return addressLine2; }
    public void setAddressLine2(String addressLine2) { this.addressLine2 = addressLine2; }
}
