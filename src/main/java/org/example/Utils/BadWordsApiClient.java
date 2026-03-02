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
import java.util.Set;

public class BadWordsApiClient {

    private static final String API_URL = "http://localhost:8080/api/moderation/check";
    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    private static final Pattern ALLOWED_PATTERN = Pattern.compile("\"allowed\"\\s*:\\s*(true|false)", Pattern.CASE_INSENSITIVE);
    private static final Pattern HATE_PATTERN = Pattern.compile(
            "(retourne\\s+dans\\s+ton\\s+pays|sale\\s+(arabe|juif|noir|blanc|etranger)|" +
            "je\\s+deteste\\s+les\\s+(arabes|juifs|noirs|etrangers)|" +
            "(race|ethnie)\\s+inferieure|vous\\s+etes\\s+des\\s+animaux|" +
            "tous\\s+les\\s+(arabes|juifs|noirs|etrangers)\\s+sont)",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern HARASS_PATTERN = Pattern.compile(
            "(tu\\s+ne\\s+vaux\\s+rien|ferme\\s+ta\\s+gueule|je\\s+vais\\s+te\\s+detruire|" +
            "tu\\s+devrais\\s+mourir|je\\s+vais\\s+te\\s+tuer|pauvre\\s+merde)",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern VERBAL_ABUSE_PATTERN = Pattern.compile(
            "(tu\\s+es\\s+(vraiment\\s+)?(nul|nulle|idiot|idiote|stupide|minable|inutile|dechet)|" +
            "tu\\s+sers\\s+a\\s+rien|tu\\s+ne\\s+sers\\s+a\\s+rien|arrete\\s+de\\s+parler|" +
            "ferme\\s+la|ta\\s+vie\\s+ne\\s+vaut\\s+rien|personne\\s+ne\\s+t\\s+aime)",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern THREAT_PATTERN = Pattern.compile(
            "(je\\s+vais\\s+m\\s+en\\s+prendre\\s+a\\s+toi|je\\s+vais\\s+m\\s+en\\s+prendre\\s+a\\s+vous|" +
            "je\\s+vais\\s+te\\s+faire\\s+payer|je\\s+vais\\s+vous\\s+faire\\s+payer|" +
            "si\\s+tu\\s+ne\\s+.*\\s+pas\\s+.*\\s+je\\s+vais\\s+.*|" +
            "si\\s+vous\\s+ne\\s+.*\\s+pas\\s+.*\\s+je\\s+vais\\s+.*|" +
            "je\\s+vais\\s+te\\s+frapper|je\\s+vais\\s+vous\\s+frapper|" +
            "je\\s+vais\\s+te\\s+casser|je\\s+vais\\s+vous\\s+casser)",
            Pattern.CASE_INSENSITIVE
    );
    private static final Set<String> ATTACK_PRONOUNS = Set.of(
            "tu", "toi", "t", "te", "ton", "ta", "tes", "vous", "votre", "vos"
    );
    private static final Set<String> ATTACK_WORDS = Set.of(
            "nul", "nulle", "minable", "idiot", "idiote", "stupide", "debile", "dechet",
            "merde", "con", "connard", "connasse", "salope", "pute", "inutile", "honte",
            "deteste", "pourri", "raté", "rate", "abruti"
    );

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
                    // Safety net: if API allows but local toxicity patterns match, block.
                    if (allowed && containsToxicIntent(text)) {
                        return false;
                    }
                    return allowed;
                }
            }
        } catch (Exception e) {
            System.out.println("Moderation API unreachable: " + e.getMessage());
        }

        // Fallback local: block obvious profanity if API is down/misconfigured.
        return !containsBadWord(text) && !containsToxicIntent(text);
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

    private static boolean containsToxicIntent(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        String normalized = normalize(text);
        return HATE_PATTERN.matcher(normalized).find()
                || HARASS_PATTERN.matcher(normalized).find()
                || VERBAL_ABUSE_PATTERN.matcher(normalized).find()
                || THREAT_PATTERN.matcher(normalized).find()
                || containsPersonalAttack(normalized);
    }

    private static boolean containsPersonalAttack(String normalizedText) {
        if (normalizedText == null || normalizedText.isBlank()) {
            return false;
        }
        String[] tokens = normalizedText.split("\\s+");
        boolean hasTarget = false;
        boolean hasAttackWord = false;

        for (String token : tokens) {
            if (ATTACK_PRONOUNS.contains(token)) {
                hasTarget = true;
            }
            if (ATTACK_WORDS.contains(token)) {
                hasAttackWord = true;
            }
        }
        if (hasTarget && hasAttackWord) {
            return true;
        }

        // Catch patterns like "tu es ...", "vous etes ..." with negative tail.
        if (normalizedText.matches(".*\\b(tu|vous)\\b\\s+\\b(es|etes)\\b.*\\b(nul|nulle|minable|idiot|stupide|debile|inutile|pourri)\\b.*")) {
            return true;
        }
        return normalizedText.matches(".*\\b(tu|vous)\\b.*\\b(ne\\s+sers\\s+a\\s+rien|honte|degage)\\b.*");
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
