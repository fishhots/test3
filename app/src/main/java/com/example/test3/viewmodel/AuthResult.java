package com.example.test3.viewmodel;

import androidx.annotation.Nullable;

import com.example.test3.data.UserData;

public class AuthResult {
    public enum Status {
        SUCCESS,
        ERROR,
        LOADING
    }

    public final Status status;
    @Nullable
    public final String successMessage;
    @Nullable
    public final String token; // For success
    @Nullable
    public final String errorMessage; // For error

    public  final UserData user;
    private AuthResult(Status status, @Nullable String successMessage, @Nullable String token, @Nullable String errorMessage, UserData user) {
        this.status = status;
        this.successMessage = successMessage;
        this.token = token;
        this.errorMessage = errorMessage;
        this.user = user;
    }

    public static AuthResult success(String message, @Nullable String token, UserData user) {
        return new AuthResult(Status.SUCCESS, message, token, null,user );
    }

    public static AuthResult error(String errorMsg) {
        return new AuthResult(Status.ERROR, null, null, errorMsg, null);
    }

    public static AuthResult loading() {
        return new AuthResult(Status.LOADING, null, null, null,null );
    }
}