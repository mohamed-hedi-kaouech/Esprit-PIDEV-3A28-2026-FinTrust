package org.example.Model.Publication;

public class GlobalFeedbackStats {
    private final int totalLikes;
    private final int totalDislikes;
    private final int totalComments;
    private final double avgRating;

    public GlobalFeedbackStats(int totalLikes, int totalDislikes, int totalComments, double avgRating) {
        this.totalLikes = totalLikes;
        this.totalDislikes = totalDislikes;
        this.totalComments = totalComments;
        this.avgRating = avgRating;
    }

    public int getTotalLikes() {
        return totalLikes;
    }

    public int getTotalDislikes() {
        return totalDislikes;
    }

    public int getTotalComments() {
        return totalComments;
    }

    public double getAvgRating() {
        return avgRating;
    }

    public double getLikeRatioPercent() {
        int total = totalLikes + totalDislikes;
        if (total == 0) {
            return 0.0;
        }
        return (totalLikes * 100.0) / total;
    }
}
