package com.example.ebank.onboarding.dto;

import java.time.LocalDate;

public class OpenAccountRequest {
    private String username;
    private String password;
    private String email;

    private String nameKanji;
    private String nameKana;
    private LocalDate birthDate;
    private String gender;
    private String phone;
    private String postalCode;
    private String address;
    private String myNumber; // マイナンバー

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

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
