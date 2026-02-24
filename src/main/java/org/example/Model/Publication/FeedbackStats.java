package org.example.Model.Publication;

public class FeedbackStats {
    private final int publicationId;
    private final String titre;
    private final int likes;
    private final int dislikes;
    private final int comments;
    private final double avgRating;
    private final int ratingCount;

    public FeedbackStats(int publicationId, String titre, int likes, int dislikes, int comments, double avgRating, int ratingCount) {
        this.publicationId = publicationId;
        this.titre = titre;
        this.likes = likes;
        this.dislikes = dislikes;
        this.comments = comments;
        this.avgRating = avgRating;
        this.ratingCount = ratingCount;
    }

    public int getPublicationId() {
        return publicationId;
    }

    public String getTitre() {
        return titre;
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

    public int getRatingCount() {
        return ratingCount;
    }

    public double getRatioLike() {
        int total = likes + dislikes;
        if (total == 0) {
            return 0.0;
        }
        return likes / (double) total;
    }

    public double getScoreGlobal() {
        return (likes - dislikes) + (avgRating * 5.0) + (comments * 0.3);
    }
}
