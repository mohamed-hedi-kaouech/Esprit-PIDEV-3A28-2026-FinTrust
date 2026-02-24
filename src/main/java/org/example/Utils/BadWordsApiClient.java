package org.example.Utils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class BadWordsApiClient {

    private static final String API_URL = "http://localhost:8080/api/moderation/check";
    private static final HttpClient CLIENT = HttpClient.newHttpClient();

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
            if (response.statusCode() != 200) {
                return false;
            }

            String body = response.body();
            if (body == null) {
                return false;
            }

            String compact = body.replaceAll("\\s+", "");
            return compact.contains("\"allowed\":true");
        } catch (Exception e) {
            System.out.println("Moderation API unreachable: " + e.getMessage());
            return false;
        }
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
