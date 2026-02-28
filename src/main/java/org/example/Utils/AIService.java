package org.example.Utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple "AI" service that can rephrase or analyse text.  For the purposes of
 * this sample project we provide a very lightweight implementation that either
 * invokes the OpenAI completion API (when an API key is set via
 * OPENAI_API_KEY environment variable) or falls back to a trivial local
 * transformation.  The top level {@link #process} method returns both a
 * rephrased version of the input and a small analysis summary.
 */
public class AIService {

    public static AIResult process(String original) {
        if (original == null) original = "";
        String rephrased = rephrase(original);
        String analysis = analyze(original);
        return new AIResult(rephrased, analysis);
    }

    private static String rephrase(String input) {
        String key = System.getenv("OPENAI_API_KEY");
        if (key != null && !key.isBlank()) {
            try {
                // very basic OpenAI text completion call
                URL url = new URL("https://api.openai.com/v1/completions");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + key);
                conn.setDoOutput(true);
                String payload = "{\"model\":\"text-davinci-003\",\"prompt\":\"Rewrite the following text in clear French:\n" +
                        escapeForJson(input) + "\",\"max_tokens\":1000}";
                conn.getOutputStream().write(payload.getBytes(StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                }
                // crude extraction of the text field from the response
                String body = sb.toString();
                int idx = body.indexOf("\"text\"");
                if (idx != -1) {
                    int start = body.indexOf('"', idx + 7) + 1;
                    int end = body.indexOf('"', start);
                    if (start > 0 && end > start) {
                        return body.substring(start, end).trim().replaceAll("\\\\n", "\n");
                    }
                }
            } catch (Exception e) {
                // fall through to local fallback
            }
        }
        // local fallback: just prefix and truncate
        String truncated = input.length() > 1000 ? input.substring(0, 1000) + "..." : input;
        return "[rephrased] " + truncated;
    }

    private static String analyze(String input) {
        if (input.isBlank()) {
            return "(no content)";
        }
        int words = input.split("\\s+").length;
        int lines = input.split("\\r?\\n").length;
        Map<String, Integer> freq = new HashMap<>();
        for (String w : input.toLowerCase().replaceAll("[^a-zàâçéèêëîïôûùüÿñæœ0-9 ]", " ").split("\\s+")) {
            if (w.isBlank()) continue;
            freq.put(w, freq.getOrDefault(w, 0) + 1);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("words=").append(words).append(", lines=").append(lines);
        sb.append("; top terms: ");
        freq.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(5)
                .forEach(e -> sb.append(e.getKey()).append("(").append(e.getValue()).append(") "));
        return sb.toString();
    }

    private static String escapeForJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }

    public static class AIResult {
        private final String rephrased;
        private final String analysis;

        public AIResult(String rephrased, String analysis) {
            this.rephrased = rephrased;
            this.analysis = analysis;
        }

        public String getRephrased() { return rephrased; }
        public String getAnalysis() { return analysis; }
    }
}
