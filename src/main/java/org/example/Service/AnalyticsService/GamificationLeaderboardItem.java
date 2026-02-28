package org.example.Service.AnalyticsService;

public record GamificationLeaderboardItem(
        int rank,
        int userId,
        String email,
        int points,
        String level,
        int badgesCount
) {
}