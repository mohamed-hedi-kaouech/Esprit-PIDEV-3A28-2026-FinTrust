package org.example.Model.Chatbot;

import java.time.LocalDateTime;

public class ChatMessage {
    private final int id;
    private final int userId;
    private final String message;
    private final String sender; // USER or BOT
    private final LocalDateTime createdAt;

    public ChatMessage(int id, int userId, String message, String sender, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.message = message;
        this.sender = sender;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public String getMessage() {
        return message;
    }

    public String getSender() {
        return sender;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}