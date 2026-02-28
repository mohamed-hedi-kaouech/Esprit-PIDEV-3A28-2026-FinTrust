package org.example.Service.AnalyticsService;

import java.util.Locale;
import java.util.Map;

public class AnalyticsChatService {

    private final AnalyticsService analyticsService;

    public AnalyticsChatService() {
        this.analyticsService = new AnalyticsService();
    }

    public String ask(String prompt) {
        String q = prompt == null ? "" : prompt.toLowerCase(Locale.ROOT);

        if (q.contains("actif") || q.contains("active")) {
            Map<UserSegmentType, Integer> counters = analyticsService.getSegmentCounters();
            int active = counters.getOrDefault(UserSegmentType.ACTIVE, 0);
            int veryActive = counters.getOrDefault(UserSegmentType.VERY_ACTIVE, 0);
            return "Utilisateurs actifs: " + active + " et tres actifs: " + veryActive + " (30 derniers jours).";
        }

        if (q.contains("churn") || q.contains("inactif") || q.contains("dormant")) {
            int size = analyticsService.getChurnRisk().size();
            return "Utilisateurs a risque de churn (>=45 jours sans login): " + size + ".";
        }

        if (q.contains("otp")) {
            OtpAnalyticsSnapshot otp = analyticsService.getOtpAnalytics();
            return "OTP requests: " + otp.totalRequests() +
                    ", succes: " + String.format(Locale.US, "%.1f", otp.requestSuccessRate()) + "%" +
                    ", validations: " + otp.totalValidations() +
                    ", taux validation: " + String.format(Locale.US, "%.1f", otp.validationSuccessRate()) + "%.";
        }

        if (q.contains("failed") || q.contains("echec")) {
            int top = analyticsService.getTopFailedLogins(10).size();
            return "Top utilisateurs avec echecs de login calcules. Entrees disponibles: " + top + ".";
        }

        return "Question non reconnue. Essayez: actifs, churn, otp, echecs login.";
    }
}