package org.example.Service.AnalyticsService;

public record FailedLoginUser(
        int userId,
        String email,
        int failedLogins30Days
) {
}