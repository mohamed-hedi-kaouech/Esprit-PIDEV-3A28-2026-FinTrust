package org.example.Service;

import org.example.Utils.SecretConfig;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WeatherService {

    private static final Pattern TEMP_PATTERN = Pattern.compile("\"temperature\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)");
    private static final Pattern WIND_PATTERN = Pattern.compile("\"windspeed\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)");
    private static final Pattern CODE_PATTERN = Pattern.compile("\"weathercode\"\\s*:\\s*(\\d+)");

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(6))
            .build();

    public String getTodayWeatherSummary() {
        try {
            String lat = readOrDefault("FINTRUST_WEATHER_LAT", "36.8065");
            String lon = readOrDefault("FINTRUST_WEATHER_LON", "10.1815");
            String city = readOrDefault("FINTRUST_WEATHER_CITY", "Tunis");

            String url = "https://api.open-meteo.com/v1/forecast?latitude="
                    + URLEncoder.encode(lat, StandardCharsets.UTF_8)
                    + "&longitude=" + URLEncoder.encode(lon, StandardCharsets.UTF_8)
                    + "&current_weather=true&timezone=auto";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                return "Meteo indisponible (API " + response.statusCode() + ").";
            }

            String body = response.body() == null ? "" : response.body();
            Double temp = findDouble(TEMP_PATTERN, body);
            Double wind = findDouble(WIND_PATTERN, body);
            Integer code = findInt(CODE_PATTERN, body);

            if (temp == null || wind == null || code == null) {
                return "Meteo du jour indisponible (reponse API incomplete).";
            }

            return String.format(
                    Locale.ROOT,
                    "Meteo %s aujourd'hui: %.1f°C, %s, vent %.1f km/h.",
                    city,
                    temp,
                    weatherLabel(code),
                    wind
            );
        } catch (Exception e) {
            return "Meteo indisponible: " + shortMsg(e.getMessage());
        }
    }

    private String weatherLabel(int code) {
        return switch (code) {
            case 0 -> "ciel degage";
            case 1, 2 -> "partiellement nuageux";
            case 3 -> "nuageux";
            case 45, 48 -> "brouillard";
            case 51, 53, 55, 56, 57 -> "bruine";
            case 61, 63, 65, 66, 67 -> "pluie";
            case 71, 73, 75, 77 -> "neige";
            case 80, 81, 82 -> "averses";
            case 85, 86 -> "averses neigeuses";
            case 95, 96, 99 -> "orage";
            default -> "etat meteo " + code;
        };
    }

    private String readOrDefault(String key, String fallback) {
        try {
            String v = SecretConfig.get(key);
            if (v != null && !v.trim().isEmpty()) return v.trim();
        } catch (Exception ignored) {
        }
        return fallback;
    }

    private Double findDouble(Pattern pattern, String text) {
        Matcher m = pattern.matcher(text);
        return m.find() ? Double.parseDouble(m.group(1)) : null;
    }

    private Integer findInt(Pattern pattern, String text) {
        Matcher m = pattern.matcher(text);
        return m.find() ? Integer.parseInt(m.group(1)) : null;
    }

    private String shortMsg(String msg) {
        if (msg == null || msg.isBlank()) return "erreur inconnue";
        return msg.length() > 120 ? msg.substring(0, 120) + "..." : msg;
    }
}
