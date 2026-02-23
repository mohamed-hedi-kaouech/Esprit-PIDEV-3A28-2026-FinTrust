package org.example.Service.AnalyticsService;

public record UserHealthScore(
        int userId,
        String email,
        int score,
        UserHealthCategory category,
        String explanation
) {
}
