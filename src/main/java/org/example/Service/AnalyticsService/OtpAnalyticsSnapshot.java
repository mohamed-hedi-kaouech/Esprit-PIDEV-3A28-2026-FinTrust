package org.example.Service.AnalyticsService;

public record OtpAnalyticsSnapshot(
        int totalRequests,
        int successfulRequests,
        int failedRequests,
        int totalValidations,
        int successfulValidations,
        int failedValidations,
        double requestSuccessRate,
        double validationSuccessRate,
        double averageValidationSeconds
) {
}