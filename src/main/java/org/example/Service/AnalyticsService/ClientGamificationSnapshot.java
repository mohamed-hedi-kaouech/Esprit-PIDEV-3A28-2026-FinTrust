package org.example.Service.AnalyticsService;

import java.util.List;

public record ClientGamificationSnapshot(
        int points,
        String level,
        String medalLabel,
        List<String> badges
) {
}