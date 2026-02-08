package com.example.ebank.user.dto;


import java.time.LocalDate;

public class UserProfileRequest {

    // 氏名
    private String nameKanji;
    private String nameKana;

    // 生年月日・性別
    private LocalDate birthDate;
    private String gender;

    // 連絡先
    private String phone;

    // 住所（確定住所）
    private String postalCode;
    private String address;

    // マイナンバー（デモ用）
    private String myNumber;

    // --- getter / setter ---

    public String getNameKanji() {
        return nameKanji;
    }
    public void setNameKanji(String nameKanji) {
        this.nameKanji = nameKanji;
    }

    public String getNameKana() {
        return nameKana;
    }
    public void setNameKana(String nameKana) {
        this.nameKana = nameKana;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }
    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getGender() {
        return gender;
    }
    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPostalCode() {
        return postalCode;
    }
    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }

    public String getMyNumber() {
        return myNumber;
    }
    public void setMyNumber(String myNumber) {
        this.myNumber = myNumber;
    }
}

