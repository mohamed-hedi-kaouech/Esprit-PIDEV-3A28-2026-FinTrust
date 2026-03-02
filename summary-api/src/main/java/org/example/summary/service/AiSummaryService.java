package org.example.summary.service;

import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AiSummaryService {

    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";
    private static final String MODEL = "gpt-4o-mini";
    private static final Pattern JSON_BLOCK = Pattern.compile("\\{.*\\}", Pattern.DOTALL);
    private static final Pattern SUMMARY_PATTERN = Pattern.compile("\"summary\"\\s*:\\s*\"(.*?)\"", Pattern.DOTALL);
    private static final Pattern SENTIMENT_PATTERN = Pattern.compile("\"sentiment\"\\s*:\\s*\"(POSITIVE|NEUTRAL|NEGATIVE)\"", Pattern.CASE_INSENSITIVE);
    private static final Pattern LABEL_PATTERN = Pattern.compile("\"ratingLabel\"\\s*:\\s*\"(.*?)\"", Pattern.DOTALL);

    private final HttpClient client = HttpClient.newHttpClient();
    private final String apiKey = System.getenv("OPENAI_API_KEY");

    public SummaryResult summarize(int publicationId, String title, List<String> comments) {
        if (comments == null || comments.isEmpty()) {
            return new SummaryResult(
                    "Les clients n'ont pas encore laissé suffisamment d'avis pour établir un résumé fiable.",
                    "NEUTRAL",
                    "Neutre"
            );
        }

        if (apiKey == null || apiKey.isBlank()) {
            return fallback(comments);
        }

        try {
            String prompt = buildPrompt(publicationId, title, comments);
            String requestBody = "{"
                    + "\"model\":\"" + MODEL + "\","
                    + "\"temperature\":0.2,"
                    + "\"messages\":["
                    + "{\"role\":\"system\",\"content\":\"Tu es un analyste qualité banque. Réponds strictement en JSON.\"},"
                    + "{\"role\":\"user\",\"content\":\"" + escapeJson(prompt) + "\"}"
                    + "]"
                    + "}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(OPENAI_URL))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200 || response.body() == null) {
                return fallback(comments);
            }

            String body = response.body();
            String modelText = extractModelText(body);
            SummaryResult parsed = parseSummary(modelText);
            return parsed != null ? parsed : fallback(comments);
        } catch (Exception e) {
            return fallback(comments);
        }
    }

    private String buildPrompt(int publicationId, String title, List<String> comments) {
        StringBuilder sb = new StringBuilder();
        sb.append("Publication #").append(publicationId).append(" - ").append(title == null ? "" : title).append("\n");
        sb.append("Commentaires:\n");
        int max = Math.min(20, comments.size());
        for (int i = 0; i < max; i++) {
            sb.append("- ").append(comments.get(i)).append("\n");
        }
        sb.append("\nDonne une réponse JSON stricte avec ce format exact:\n");
        sb.append("{\"summary\":\"3 a 5 phrases\",\"sentiment\":\"POSITIVE|NEUTRAL|NEGATIVE\",\"ratingLabel\":\"Positif|Neutre|Negatif\"}");
        return sb.toString();
    }

    private String extractModelText(String body) {
        // naive extraction of first message content
        int idx = body.indexOf("\"content\":");
        if (idx < 0) return body;
        String sub = body.substring(idx);
        int start = sub.indexOf('"');
        start = sub.indexOf('"', start + 1);
        int end = sub.indexOf("\"}", start + 1);
        if (start < 0 || end < 0) return body;
        return sub.substring(start + 1, end).replace("\\n", "\n").replace("\\\"", "\"");
    }

    private SummaryResult parseSummary(String text) {
        if (text == null || text.isBlank()) return null;

        Matcher blockMatcher = JSON_BLOCK.matcher(text);
        String candidate = blockMatcher.find() ? blockMatcher.group() : text;

        String summary = extract(SUMMARY_PATTERN, candidate);
        String sentiment = extract(SENTIMENT_PATTERN, candidate);
        String label = extract(LABEL_PATTERN, candidate);

        if (summary == null || sentiment == null || label == null) return null;
        return new SummaryResult(unescape(summary), sentiment.toUpperCase(Locale.ROOT), unescape(label));
    }

    private String extract(Pattern p, String s) {
        Matcher m = p.matcher(s);
        if (!m.find()) return null;
        return m.group(1);
    }

    private SummaryResult fallback(List<String> comments) {
        int total = comments.size();
        String summary = "Les clients ont laissé " + total + " avis récents. "
                + "Les retours mettent en avant des points positifs mais aussi des attentes d'amélioration. "
                + "Un suivi régulier des commentaires est recommandé pour maintenir la satisfaction globale.";
        return new SummaryResult(summary, "NEUTRAL", "Neutre");
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "");
    }

    private String unescape(String s) {
        return s.replace("\\n", " ")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }
}
