package org.example.Utils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SummaryApiClient {

    private static final String API_URL = "http://localhost:8082/api/summary/publication";
    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    private static final Pattern SUMMARY_PATTERN = Pattern.compile("\"summary\"\\s*:\\s*\"(.*?)\"", Pattern.DOTALL);
    private static final Pattern SENTIMENT_PATTERN = Pattern.compile("\"sentiment\"\\s*:\\s*\"(.*?)\"");
    private static final Pattern RATING_LABEL_PATTERN = Pattern.compile("\"ratingLabel\"\\s*:\\s*\"(.*?)\"");

    private SummaryApiClient() {
    }

    public static SummaryResult summarize(int publicationId, String title, List<String> comments) {
        try {
            String payload = buildPayload(publicationId, title, comments);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200 && response.body() != null) {
                SummaryResult parsed = parseResponse(response.body());
                if (parsed != null) {
                    return parsed;
                }
            }
        } catch (Exception e) {
            System.out.println("Summary API unreachable: " + e.getMessage());
        }

        return localFallback(comments);
    }

    private static String buildPayload(int publicationId, String title, List<String> comments) {
        StringBuilder commentsJson = new StringBuilder("[");
        for (int i = 0; i < comments.size(); i++) {
            if (i > 0) commentsJson.append(",");
            commentsJson.append("\"").append(escapeJson(comments.get(i))).append("\"");
        }
        commentsJson.append("]");

        return "{"
                + "\"publicationId\":" + publicationId + ","
                + "\"title\":\"" + escapeJson(title == null ? "" : title) + "\","
                + "\"comments\":" + commentsJson
                + "}";
    }

    private static SummaryResult parseResponse(String body) {
        String summary = extract(SUMMARY_PATTERN, body);
        String sentiment = extract(SENTIMENT_PATTERN, body);
        String ratingLabel = extract(RATING_LABEL_PATTERN, body);

        if (summary == null || summary.isBlank()) return null;
        if (sentiment == null || sentiment.isBlank()) sentiment = "NEUTRAL";
        if (ratingLabel == null || ratingLabel.isBlank()) ratingLabel = "Neutre";
        return new SummaryResult(unescapeJson(summary), sentiment, unescapeJson(ratingLabel));
    }

    private static String extract(Pattern p, String body) {
        Matcher m = p.matcher(body);
        if (!m.find()) return null;
        return m.group(1);
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static String unescapeJson(String s) {
        return s.replace("\\n", "\n")
                .replace("\\r", "")
                .replace("\\t", "\t")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }

    private static SummaryResult localFallback(List<String> comments) {
        if (comments == null || comments.isEmpty()) {
            return new SummaryResult(
                    "Les clients n'ont pas encore laissé suffisamment d'avis pour un résumé fiable.",
                    "NEUTRAL",
                    "Neutre"
            );
        }

        int total = comments.size();
        String summary = "Les clients partagent " + total + " avis récents sur cette publication. "
                + "Globalement, les retours sont mitigés selon les besoins de chacun. "
                + "Consultez les commentaires détaillés pour mieux comprendre les points forts et limites.";
        return new SummaryResult(summary, "NEUTRAL", "Neutre");
    }

    public static String computeSentimentFromRating(double avgRating) {
        if (avgRating >= 4.0) return "POSITIVE";
        if (avgRating >= 3.0) return "NEUTRAL";
        return "NEGATIVE";
    }

    public static String computeRatingLabel(double avgRating) {
        if (avgRating >= 4.0) return "Positif";
        if (avgRating >= 3.0) return "Neutre";
        return "Negatif";
    }
}
