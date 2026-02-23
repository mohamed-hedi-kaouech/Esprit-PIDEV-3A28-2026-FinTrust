package org.example.Service.QrService;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.example.Model.Kyc.Kyc;
import org.example.Model.User.User;
import org.example.Repository.KycRepository;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class ClientQrService {

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final KycRepository kycRepository = new KycRepository();

    public Path generateClientQrImage(User user, int size) {
        if (user == null) {
            throw new IllegalArgumentException("Utilisateur non connecte.");
        }
        int qrSize = Math.max(220, Math.min(size, 900));
        String payload = buildClientPayload(user);

        try {
            QRCodeWriter writer = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.MARGIN, 1);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

            BitMatrix matrix = writer.encode(payload, BarcodeFormat.QR_CODE, qrSize, qrSize, hints);
            Path file = Files.createTempFile("fintrust_qr_user_" + user.getId() + "_", ".png");
            MatrixToImageWriter.writeToPath(matrix, "PNG", file);
            file.toFile().deleteOnExit();
            return file;
        } catch (WriterException e) {
            throw new RuntimeException("Generation QR impossible: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Erreur creation image QR: " + e.getMessage(), e);
        }
    }

    public String buildClientPayload(User user) {
        Kyc kyc = kycRepository.findByUserId(user.getId()).orElse(null);
        StringBuilder json = new StringBuilder();
        json.append('{');
        append(json, "type", "FINTRUST_CLIENT_KYC_QR").append(',');
        append(json, "userId", String.valueOf(user.getId())).append(',');
        append(json, "nom", safe(user.getNom())).append(',');
        append(json, "prenom", safe(user.getPrenom())).append(',');
        append(json, "email", safe(user.getEmail())).append(',');
        append(json, "numTel", safe(user.getNumTel())).append(',');
        append(json, "role", user.getRole() == null ? "" : user.getRole().name()).append(',');
        append(json, "statusCompte", user.getStatus() == null ? "" : user.getStatus().name()).append(',');
        append(json, "kycStatus", kyc == null || kyc.getStatut() == null ? "" : kyc.getStatut().name()).append(',');
        append(json, "cin", kyc == null ? "" : safe(kyc.getCin())).append(',');
        append(json, "adresse", kyc == null ? "" : safe(kyc.getAdresse())).append(',');
        append(json, "dateNaissance", kyc == null || kyc.getDateNaissance() == null ? "" : kyc.getDateNaissance().toString()).append(',');
        append(json, "issuedAt", LocalDateTime.now().format(DT));
        json.append('}');
        return json.toString();
    }

    private StringBuilder append(StringBuilder sb, String key, String value) {
        sb.append('"').append(escape(key)).append('"').append(':');
        sb.append('"').append(escape(value)).append('"');
        return sb;
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String escape(String s) {
        String value = s == null ? "" : s;
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", " ")
                .replace("\r", " ")
                .trim();
    }
}
