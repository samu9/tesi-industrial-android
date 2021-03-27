package com.example.industrial.models;

public class InstructionMessage {
    private String message;

    private User assistant;

    public InstructionMessage(String message) {
        this.message = message;
    }

    public InstructionMessage(String message, User assistant) {
        this.message = message;
        this.assistant = assistant;
    }

    public String getMessage() {
        return message;
    }

    public User getAssistant() {
        return assistant;
    }
}
