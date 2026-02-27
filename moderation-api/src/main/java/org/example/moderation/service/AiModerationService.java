package org.example.moderation.service;

import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AiModerationService {

    private static final String OPENAI_URL = "https://api.openai.com/v1/moderations";
    private static final String MODEL = "omni-moderation-latest";

    private static final Pattern FLAGGED_PATTERN = Pattern.compile("\"flagged\"\\s*:\\s*(true|false)", Pattern.CASE_INSENSITIVE);
    private static final Pattern CATEGORY_TRUE_PATTERN = Pattern.compile("\"([a-z_\\-/]+)\"\\s*:\\s*true", Pattern.CASE_INSENSITIVE);

    private final HttpClient client = HttpClient.newHttpClient();
    private final String apiKey = System.getenv("OPENAI_API_KEY");

    public ModerationResult check(String text) {
        if (apiKey == null || apiKey.isBlank()) {
            return new ModerationResult(false, "OPENAI_API_KEY manquante", "CONFIG", List.of());
        }

        try {
            String json = "{"
                    + "\"model\":\"" + MODEL + "\","
                    + "\"input\":" + toJsonString(text)
                    + "}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(OPENAI_URL))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                return new ModerationResult(false, "Erreur moderation API: " + response.statusCode(), "API_ERROR", List.of());
            }

            String body = response.body() == null ? "" : response.body();
            boolean flagged = parseFlagged(body);
            List<String> categories = extractTrueCategories(body);

            if (flagged) {
                return new ModerationResult(false, "Contenu inapproprie detecte", "FLAGGED", categories);
            }

            return new ModerationResult(true, "OK", "OK", categories);
        } catch (Exception e) {
            return new ModerationResult(false, "Exception moderation: " + e.getMessage(), "EXCEPTION", List.of());
        }
    }

    private boolean parseFlagged(String body) {
        Matcher matcher = FLAGGED_PATTERN.matcher(body);
        if (!matcher.find()) {
            return false;
        }
        return Boolean.parseBoolean(matcher.group(1).toLowerCase(Locale.ROOT));
    }

    private List<String> extractTrueCategories(String body) {
        List<String> categories = new ArrayList<>();
        Matcher matcher = CATEGORY_TRUE_PATTERN.matcher(body);
        while (matcher.find()) {
            String category = matcher.group(1);
            if (!"flagged".equalsIgnoreCase(category) && !categories.contains(category)) {
                categories.add(category);
            }
        }
        return categories;
    }

    private String toJsonString(String s) {
        if (s == null) s = "";
        return "\"" + s
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "")
                + "\"";
    }
}
