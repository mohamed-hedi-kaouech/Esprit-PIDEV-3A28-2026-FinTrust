package org.example.Model.Notification;

import java.time.LocalDateTime;

public class Notification {
    private int id;
    private int userId;
    private String type;
    private String message;
    private boolean read;
    private LocalDateTime createdAt;

    public Notification(int id, int userId, String type, String message, boolean read, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.message = message;
        this.read = read;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getType() { return type; }
    public String getMessage() { return message; }
    public boolean isRead() { return read; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}