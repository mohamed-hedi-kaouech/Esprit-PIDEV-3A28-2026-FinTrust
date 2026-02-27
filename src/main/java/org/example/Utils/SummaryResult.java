package org.example.Utils;

public class SummaryResult {
    private final String summary;
    private final String sentiment;
    private final String ratingLabel;

    public SummaryResult(String summary, String sentiment, String ratingLabel) {
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
