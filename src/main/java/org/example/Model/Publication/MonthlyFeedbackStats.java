package org.example.Model.Publication;

public class MonthlyFeedbackStats {
    private final String month;
    private final int likes;
    private final int dislikes;
    private final int comments;
    private final double avgRating;

    public MonthlyFeedbackStats(String month, int likes, int dislikes, int comments, double avgRating) {
        this.month = month;
        this.likes = likes;
        this.dislikes = dislikes;
        this.comments = comments;
        this.avgRating = avgRating;
    }

    public String getMonth() {
        return month;
    }

    public int getLikes() {
        return likes;
    }

    public int getDislikes() {
        return dislikes;
    }

    public int getComments() {
        return comments;
    }

    public double getAvgRating() {
        return avgRating;
    }
}
