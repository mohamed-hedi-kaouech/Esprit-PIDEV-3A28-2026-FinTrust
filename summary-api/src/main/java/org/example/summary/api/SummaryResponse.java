package org.example.summary.api;

public class SummaryResponse {
    private final String summary;
    private final String sentiment;
    private final String ratingLabel;

    public SummaryResponse(String summary, String sentiment, String ratingLabel) {
        this.summary = summary;
        this.sentiment = sentiment;
        this.ratingLabel = ratingLabel;
    }

    public String getSummary() {
        return summary;
    }

    public String getSentiment() {
        return sentiment;
    }

    public String getRatingLabel() {
        return ratingLabel;
    }
}
