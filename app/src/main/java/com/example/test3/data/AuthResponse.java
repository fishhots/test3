package com.example.test3.data;

import androidx.annotation.Nullable;

public class AuthResponse {
    private String msg;
    @Nullable
    private UserData user;

    // Getters
    public String getMsg() { return msg; }
    @Nullable
    public UserData getUser() { return user; }


    // Setters might be needed if you construct this object manually,
    // but Gson typically handles deserialization without them if getters are present.
}