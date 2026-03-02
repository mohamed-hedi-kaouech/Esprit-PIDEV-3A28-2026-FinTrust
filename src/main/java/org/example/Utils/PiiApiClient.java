package org.example.Utils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PiiApiClient {

    private static final String API_URL = "http://localhost:8081/api/pii/check";
    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    private static final Pattern ALLOWED_PATTERN = Pattern.compile("\"allowed\"\\s*:\\s*(true|false)", Pattern.CASE_INSENSITIVE);
    private static final Pattern EMAIL_PATTERN = Pattern.compile("\\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern IBAN_PATTERN = Pattern.compile("\\b[A-Z]{2}\\d{2}[A-Z0-9]{11,30}\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern RIB_PATTERN = Pattern.compile("\\b\\d{20}\\b");
    private static final Pattern PHONE_PATTERN = Pattern.compile("(\\+\\d{1,3}[\\s.-]?)?(\\(?\\d{2,4}\\)?[\\s.-]?)?\\d{6,10}");
    private static final Pattern CARD_PATTERN = Pattern.compile("\\b(?:\\d[ -]*?){13,19}\\b");
    private static final Pattern CIN_PATTERN = Pattern.compile("\\b\\d{8}\\b");
    private static final Pattern PASSPORT_PATTERN = Pattern.compile("\\b[A-Z]{1,2}\\d{6,9}\\b", Pattern.CASE_INSENSITIVE);

    private PiiApiClient() {
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
            System.out.println("PII API unreachable: " + e.getMessage());
        }

        // Fallback local: block obvious sensitive data if API is down/misconfigured.
        return !containsSensitive(text);
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

    private static boolean containsSensitive(String text) {
        if (text == null || text.isBlank()) return false;
        String lower = text.toLowerCase(Locale.ROOT);

        if (EMAIL_PATTERN.matcher(text).find()) return true;
        if (IBAN_PATTERN.matcher(text).find()) return true;
        if (RIB_PATTERN.matcher(text).find() || lower.contains("rib")) return true;
        if (CIN_PATTERN.matcher(text).find()) return true;
        if (PASSPORT_PATTERN.matcher(text).find() || lower.contains("passport") || lower.contains("passeport")) return true;

        Matcher phone = PHONE_PATTERN.matcher(text);
        while (phone.find()) {
            String digits = phone.group().replaceAll("\\D", "");
            if (digits.length() >= 8 && digits.length() <= 15) return true;
        }

        Matcher card = CARD_PATTERN.matcher(text);
        while (card.find()) {
            String digits = card.group().replaceAll("\\D", "");
            if (digits.length() >= 13 && digits.length() <= 19 && passesLuhn(digits)) return true;
        }

        return false;
    }

    private static boolean passesLuhn(String digits) {
        int sum = 0;
        boolean alt = false;
        for (int i = digits.length() - 1; i >= 0; i--) {
            int n = digits.charAt(i) - '0';
            if (alt) {
                n *= 2;
                if (n > 9) n -= 9;
            }
            sum += n;
            alt = !alt;
        }
        return sum % 10 == 0;
    }
}
