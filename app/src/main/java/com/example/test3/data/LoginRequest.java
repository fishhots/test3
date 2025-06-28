package com.example.test3.data;

public class LoginRequest {
    private String username; // 或者 username
    private String password;

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
}