package org.example.Utils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BadWordsApiClient {

    private static final String API_URL = "http://localhost:8080/api/moderation/check";
    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    private static final Pattern ALLOWED_PATTERN = Pattern.compile("\"allowed\"\\s*:\\s*(true|false)", Pattern.CASE_INSENSITIVE);

    private static final Set<String> BLOCKLIST = Set.of(
            "con", "connard", "connasse", "salope", "pute", "merde", "encule", "enculé", "fdp", "batard", "bâtard",
            "fuck", "fucking", "shit", "bitch", "asshole", "motherfucker", "stfu",
            "zebi", "zb", "nik", "nikk", "nikou", "kosom", "hmar", "bhim"
    );

    private BadWordsApiClient() {
    }

    public static boolean isAllowed(String text) {
        try {
            String json = "{\"text\":\"" + escapeJson(text == null ? "" : text) + "\"}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                Boolean allowed = parseAllowed(response.body());
                if (allowed != null) {
                    return allowed;
                }
            }
        } catch (Exception e) {
            System.out.println("Moderation API unreachable: " + e.getMessage());
        }

        // Fallback local: block obvious profanity if API is down/misconfigured.
        return !containsBadWord(text);
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static Boolean parseAllowed(String body) {
        if (body == null) return null;
        Matcher m = ALLOWED_PATTERN.matcher(body);
        if (!m.find()) return null;
        return Boolean.parseBoolean(m.group(1).toLowerCase(Locale.ROOT));
    }

    private static boolean containsBadWord(String text) {
        String normalized = normalize(text);
        if (normalized.isBlank()) return false;

        for (String w : BLOCKLIST) {
            String token = normalize(w);
            if (token.contains(" ")) {
                if (normalized.contains(token)) return true;
            } else if (Pattern.compile("\\b" + Pattern.quote(token) + "\\b").matcher(normalized).find()) {
                return true;
            }
        }
        return false;
    }

    private static String normalize(String s) {
        if (s == null) return "";
        String n = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        n = n.toLowerCase(Locale.ROOT);
        n = n.replaceAll("(.)\\1{2,}", "$1$1");
        n = n.replaceAll("[^a-z0-9\\s]", " ");
        n = n.replaceAll("\\s+", " ").trim();
        return n;
    }
}
