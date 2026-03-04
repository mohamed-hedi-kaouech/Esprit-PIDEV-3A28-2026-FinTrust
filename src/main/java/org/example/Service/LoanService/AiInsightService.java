package org.example.Service.LoanService;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class AiInsightService {

    private final String apiKey = "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";

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public String generateInsight(String prompt) {

        if (apiKey == null || apiKey.isBlank()) {
            return "AI service unavailable (Missing API key).";
        }

        try {

            String requestBody = """
            {
              "model": "openai/gpt-4o-mini",
              "messages": [
                {
                  "role": "system",
                  "content": "You are a professional financial risk analyst. Be concise and executive."
                },
                {
                  "role": "user",
                  "content": "%s"
                }
              ],
              "temperature": 0.3
            }
            """.formatted(prompt.replace("\"", "\\\""));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://openrouter.ai/api/v1/chat/completions"))
                    .timeout(Duration.ofSeconds(15))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .header("HTTP-Referer", "http://localhost")
                    .header("X-Title", "LoanManagementSystem")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return "AI error: HTTP " + response.statusCode();
            }

            return extractMessage(response.body());

        } catch (Exception e) {
            return "AI service temporarily unavailable.";
        }
    }

    private String extractMessage(String json) {

        try {
            int contentIndex = json.indexOf("\"content\":\"");

            if (contentIndex == -1) {
                return "AI response format unexpected.";
            }

            int start = contentIndex + 11;
            int end = json.indexOf("\"", start);

            if (end == -1) {
                return "AI response incomplete.";
            }

            return json.substring(start, end)
                    .replace("\\n", "\n")
                    .replace("\\\"", "\"");

        } catch (Exception e) {
            return "Failed to parse AI response.";
        }
    }
}