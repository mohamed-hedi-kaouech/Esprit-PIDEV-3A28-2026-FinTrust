package org.example.Service.AnalyticsService;

import java.time.LocalDateTime;

public record UserSegmentItem(
        int userId,
        String email,
        UserSegmentType segment,
        int successLogins30Days,
        int failedLogins30Days,
        LocalDateTime lastSuccessfulLogin
) {
}