package org.example.Service.AnalyticsService;

import java.time.LocalDateTime;

public record ChurnRiskItem(
        int userId,
        String email,
        LocalDateTime lastSuccessfulLogin,
        int inactiveDays,
        boolean highRisk
) {
}
