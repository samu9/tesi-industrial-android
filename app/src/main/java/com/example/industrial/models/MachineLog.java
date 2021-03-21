package com.example.industrial.models;

public class MachineLog {
    private String user;
    private String action;
    private String timestamp;

    public MachineLog(String user, String action, String timestamp) {
        this.user = user;
        this.action = action;
        this.timestamp = timestamp;
    }

    public String getUser() {
        return user;
    }

    public String getAction() {
        return action;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
