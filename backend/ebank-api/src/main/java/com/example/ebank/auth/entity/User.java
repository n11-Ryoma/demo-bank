package com.example.ebank.auth.entity;

import java.util.ArrayList;
import java.util.List;

public class User {

    private Long id;
    private String username;
    private String password;

    // 追加
    private List<Role> roles = new ArrayList<>();

    public User() {}

    public User(Long id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }

    // ====== getter / setter ======

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    // JWT に使うためのロール名だけのリストを返す
    public List<String> getRoleNames() {
        List<String> names = new ArrayList<>();
        for (Role r : roles) {
            names.add(r.getName());
        }
        return names;
    }

}