package org.example.Service.AnalyticsService;

public record GamificationChallengeItem(
        int userId,
        String email,
        String challengeTitle,
        String status,
        int progress,
        int target
) {
}