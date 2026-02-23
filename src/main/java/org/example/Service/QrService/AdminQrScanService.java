package org.example.Service.QrService;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import org.example.Repository.UserRepository;
import org.example.Repository.UserQrTokenRepository;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdminQrScanService {

    private static final Pattern TOKEN_JSON_PATTERN = Pattern.compile("\"token\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern USER_ID_JSON_PATTERN = Pattern.compile("\"userId\"\\s*:\\s*\"?(\\d+)\"?");
    private static final Pattern ID_JSON_PATTERN = Pattern.compile("\"id\"\\s*:\\s*\"?(\\d+)\"?");
    private static final Pattern EMAIL_JSON_PATTERN = Pattern.compile("\"email\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern NUMBER_ONLY_PATTERN = Pattern.compile("^\\d+$");
    private final UserQrTokenRepository tokenRepository = new UserQrTokenRepository();
    private final UserRepository userRepository = new UserRepository();

    public String decodeTokenFromImage(File imageFile) {
        if (imageFile == null) {
            throw new IllegalArgumentException("Image QR manquante.");
        }
        try {
            BufferedImage image = ImageIO.read(imageFile);
            if (image == null) {
                throw new IllegalArgumentException("Format image invalide.");
            }
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(image)));
            Result result = new MultiFormatReader().decode(bitmap);
            if (result == null || result.getText() == null || result.getText().isBlank()) {
                throw new IllegalArgumentException("Contenu QR vide.");
            }
            return result.getText().trim();
        } catch (NotFoundException e) {
            throw new IllegalArgumentException("QR non detecte dans cette image.");
        } catch (Exception e) {
            throw new RuntimeException("Erreur lecture QR: " + e.getMessage(), e);
        }
    }

    public int resolveUserIdAndConsume(String rawPayload) {
        if (rawPayload == null || rawPayload.isBlank()) {
            throw new IllegalArgumentException("Contenu QR vide.");
        }
        String payload = rawPayload.trim();

        if (NUMBER_ONLY_PATTERN.matcher(payload).matches()) {
            return Integer.parseInt(payload);
        }

        if (payload.startsWith("{")) {
            Integer directId = extractUserIdFromJson(payload);
            if (directId != null) return directId;

            String email = extractEmailFromJson(payload);
            if (email != null && !email.isBlank()) {
                return userRepository.findByEmail(email.trim().toLowerCase(Locale.ROOT))
                        .map(u -> u.getId())
                        .orElseThrow(() -> new IllegalArgumentException("Aucun utilisateur pour cet email QR."));
            }

            String tokenFromJson = extractTokenFromJson(payload);
            if (tokenFromJson != null) {
                return resolveByToken(tokenFromJson);
            }
            throw new IllegalArgumentException("QR JSON invalide: token, userId ou email manquant.");
        }

        if (payload.contains("@")) {
            return userRepository.findByEmail(payload.trim().toLowerCase(Locale.ROOT))
                    .map(u -> u.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Aucun utilisateur pour cet email QR."));
        }

        return resolveByToken(payload);
    }

    private int resolveByToken(String token) {
        String cleaned = token == null ? "" : token.trim();
        if (cleaned.length() < 10) {
            throw new IllegalArgumentException("Token QR invalide.");
        }
        Optional<Integer> userId = tokenRepository.consumeActiveToken(cleaned);
        return userId.orElseThrow(() ->
                new IllegalArgumentException("Token QR invalide, expire, ou deja utilise.")
        );
    }

    private Integer extractUserIdFromJson(String json) {
        Matcher mUserId = USER_ID_JSON_PATTERN.matcher(json);
        if (mUserId.find()) return Integer.parseInt(mUserId.group(1));
        Matcher mId = ID_JSON_PATTERN.matcher(json);
        if (mId.find()) return Integer.parseInt(mId.group(1));
        return null;
    }

    private String extractEmailFromJson(String json) {
        Matcher m = EMAIL_JSON_PATTERN.matcher(json);
        return m.find() ? m.group(1) : null;
    }

    private String extractTokenFromJson(String json) {
        Matcher m = TOKEN_JSON_PATTERN.matcher(json);
        return m.find() ? m.group(1) : null;
    }
}
