package com.example.ebank.address.dto;

public class AddressChangeCommitRequest {

    private String postalCode;
    private String prefecture;
    private String city;
    private String addressLine1;
    private String addressLine2;

    // 以前の tempFilePath はあってもなくてもOK（使わないなら消してよい）
    // private String tempFilePath;

    private String fileName;    // 追加
    private String fileBase64;  // 追加

    // getter / setter …

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

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFileBase64() { return fileBase64; }
    public void setFileBase64(String fileBase64) { this.fileBase64 = fileBase64; }
}
