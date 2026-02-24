package org.example.Model.Publication;

import java.time.LocalDate;

public class FeedbackTrendPoint {
    private final LocalDate day;
    private final int likes;
    private final int dislikes;
    private final int comments;

    public FeedbackTrendPoint(LocalDate day, int likes, int dislikes, int comments) {
        this.day = day;
        this.likes = likes;
        this.dislikes = dislikes;
        this.comments = comments;
    }

    public LocalDate getDay() {
        return day;
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
}
