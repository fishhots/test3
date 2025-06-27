package com.example.test3.data;

import androidx.annotation.Nullable;
import java.util.List;

public class ErrorResponse {
    @Nullable
    private String msg;
    @Nullable
    private List<String> errors;

    @Nullable
    public String getMsg() { return msg; }
    @Nullable
    public List<String> getErrors() { return errors; }
}