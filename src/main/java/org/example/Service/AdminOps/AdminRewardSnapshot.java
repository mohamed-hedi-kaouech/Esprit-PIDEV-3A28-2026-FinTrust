package org.example.Service.AdminOps;

public record AdminRewardSnapshot(
        int totalStars,
        int totalPoints,
        int streakDays,
        boolean taskFinisherBadge
) {
}