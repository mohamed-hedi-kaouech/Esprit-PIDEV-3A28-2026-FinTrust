package org.example.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import org.example.Utils.SecretConfig;

public class SmsService {

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public boolean isConfigured() {
        String sid = SecretConfig.get("FINTRUST_TWILIO_ACCOUNT_SID");
        String token = SecretConfig.get("FINTRUST_TWILIO_AUTH_TOKEN");
        String verifySid = SecretConfig.get("FINTRUST_TWILIO_VERIFY_SERVICE_SID");
        return !isBlank(sid) && !isBlank(token) && !isBlank(verifySid);
    }

    public void sendPasswordResetCode(String phone, String ignoredCode) {
        sendOtpSms(phone);
    }

    public void sendOtpSms(String phone) {
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("Numero de telephone vide.");
        }

        String sid = SecretConfig.get("FINTRUST_TWILIO_ACCOUNT_SID");
        String token = SecretConfig.get("FINTRUST_TWILIO_AUTH_TOKEN");
        String verifySid = SecretConfig.get("FINTRUST_TWILIO_VERIFY_SERVICE_SID");
        if (isBlank(sid) || isBlank(token) || isBlank(verifySid)) {
            throw new RuntimeException(
                    "Gateway SMS non configure. Definissez FINTRUST_TWILIO_ACCOUNT_SID, FINTRUST_TWILIO_AUTH_TOKEN, FINTRUST_TWILIO_VERIFY_SERVICE_SID."
            );
        }

        String to = normalizePhone(phone);
        String form = "To=" + url(to) + "&Channel=sms";

        String auth = Base64.getEncoder()
                .encodeToString((sid + ":" + token).getBytes(StandardCharsets.UTF_8));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://verify.twilio.com/v2/Services/" + verifySid + "/Verifications"))
                .timeout(Duration.ofSeconds(15))
                .header("Authorization", "Basic " + auth)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();

        try {
            HttpResponse<String> response = client
                    .send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            if (status < 200 || status >= 300) {
                throw new RuntimeException("Twilio Verify error HTTP " + status + ": " + response.body());
            }
        } catch (Exception e) {
            throw new RuntimeException("Envoi OTP SMS impossible: " + e.getMessage(), e);
        }
    }

    public boolean checkOtp(String phone, String code) {
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("Numero de telephone vide.");
        }
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Code OTP vide.");
        }

        String sid = SecretConfig.get("FINTRUST_TWILIO_ACCOUNT_SID");
        String token = SecretConfig.get("FINTRUST_TWILIO_AUTH_TOKEN");
        String verifySid = SecretConfig.get("FINTRUST_TWILIO_VERIFY_SERVICE_SID");
        if (isBlank(sid) || isBlank(token) || isBlank(verifySid)) {
            throw new RuntimeException(
                    "Gateway SMS non configure. Definissez FINTRUST_TWILIO_ACCOUNT_SID, FINTRUST_TWILIO_AUTH_TOKEN, FINTRUST_TWILIO_VERIFY_SERVICE_SID."
            );
        }

        String to = normalizePhone(phone);
        String form = "To=" + url(to) + "&Code=" + url(code);

        String auth = Base64.getEncoder()
                .encodeToString((sid + ":" + token).getBytes(StandardCharsets.UTF_8));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://verify.twilio.com/v2/Services/" + verifySid + "/VerificationCheck"))
                .timeout(Duration.ofSeconds(15))
                .header("Authorization", "Basic " + auth)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();

        try {
            HttpResponse<String> response = client
                    .send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            if (status < 200 || status >= 300) {
                throw new RuntimeException("Twilio Verify check HTTP " + status + ": " + response.body());
            }
            String body = response.body();
            return body != null && body.contains("\"status\":\"approved\"");
        } catch (Exception e) {
            throw new RuntimeException("Verification OTP SMS impossible: " + e.getMessage(), e);
        }
    }

    private static String normalizePhone(String phone) {
        String value = phone.trim().replace(" ", "").replace("-", "");
        if (!value.startsWith("+")) {
            // Default Tunisia country code if user saved local format.
            value = "+216" + value;
        }
        return value;
    }

    private static String url(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
