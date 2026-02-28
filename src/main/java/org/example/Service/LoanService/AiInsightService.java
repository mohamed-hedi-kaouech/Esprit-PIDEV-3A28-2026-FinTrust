package org.example.Service.LoanService;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class AiInsightService {

    private final String apiKey = System.getenv("OPENROUTER_API_KEY");

    public String generateInsight(String prompt) throws Exception {

        String requestBody = """
        {
          "model": "openai/gpt-4o-mini",
          "messages": [
            {
              "role": "system",
              "content": "You are a financial risk analyst."
            },
            {
              "role": "user",
              "content": "%s"
            }
          ]
        }
        """.formatted(prompt.replace("\"", "\\\""));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://openrouter.ai/api/v1/chat/completions"))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .header("HTTP-Referer", "http://localhost")
                .header("X-Title", "LoanManagementSystem")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        return extractMessage(response.body());
    }

    private String extractMessage(String json) {
        try {
            int start = json.indexOf("\"content\":\"") + 11;
            int end = json.indexOf("\"", start);
            return json.substring(start, end)
                    .replace("\\n", "\n")
                    .replace("\\\"", "\"");
        } catch (Exception e) {
            return "Unable to parse AI response.\n\nRaw response:\n" + json;
        }
    }
}