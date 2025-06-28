package com.example.test3.data;

public class RegisterRequest {
    private String username;
    private String email;
    private String password;

    public RegisterRequest(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    // Getters (Gson needs them, or public fields)
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
}