package com.example.test3.data;

import java.io.Serializable;

public class UserData implements Serializable {
    private String id; // 或者 int
    private String username;
    private String email;

    // Getters
    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
}