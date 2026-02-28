package org.example.Service.AnalyticsService;

import java.util.List;

public record GamificationSnapshot(
        int totalPointsDistributed,
        int totalBadges,
        int challengesCompleted,
        int challengesPending,
        int bronzeCount,
        int silverCount,
        int goldCount,
        int platinumCount,
        List<GamificationLeaderboardItem> leaderboard,
        List<GamificationChallengeItem> challenges,
        List<GamificationBadgeStat> badgeStats
) {
}