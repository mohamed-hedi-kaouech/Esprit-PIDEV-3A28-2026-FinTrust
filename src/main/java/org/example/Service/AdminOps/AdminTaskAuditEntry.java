package org.example.Service.AdminOps;

import java.time.LocalDateTime;

public record AdminTaskAuditEntry(
        int taskId,
        String action,
        String fromStatus,
        String toStatus,
        String actorEmail,
        int starsEarned,
        LocalDateTime createdAt
) {
}