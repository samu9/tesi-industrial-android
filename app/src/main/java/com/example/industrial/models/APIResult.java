package com.example.industrial.models;

public class APIResult {

    private boolean result;

    private String message;

    public APIResult(boolean result, String message) {
        this.result = result;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public boolean getResult() {
        return result;
    }
}
