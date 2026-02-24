package org.example.Service;

import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.Random;

import javax.mail.Authenticator;
import javax.mail.AuthenticationFailedException;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.example.Utils.SecretConfig;

public class EmailService {

    private static final Random RNG = new Random();
    private static final int CODE_TTL_MINUTES = 10;

    // ===== Codes =====
    public String generateVerificationCode() {
        return String.valueOf(100000 + RNG.nextInt(900000));
    }

    // ===== Public APIs =====
    public String sendPasswordResetCode(String email) {
        String code = generateVerificationCode();
        sendPasswordResetCode(email, code);
        return code;
    }

    public void sendAuthVerificationCode(String email, String code) {
        String subject = "FinTrust - Code de verification";
        sendEmail(email, subject, buildAuthText(code), buildAuthHtml(code));
    }

    public void sendPasswordResetCode(String email, String code) {
        String subject = "FinTrust - Code de reinitialisation du mot de passe";
        sendEmail(email, subject, buildResetText(code), buildResetHtml(code));
    }

    public void sendNotification(String to, String message) {
        sendEmail(to, "Notification FinTrust", message, null);
    }

    public void sendWelcomeEmail(String to, String nom) {
        String safeNom = (nom == null || nom.isBlank()) ? "client" : nom.trim();
        String subject = "Bienvenue chez FinTrust";

        String text = "Bienvenue " + safeNom + ",\n\n"
                + "Votre inscription sur FinTrust est bien enregistree.\n"
                + "Notre equipe vous souhaite la bienvenue.\n\n"
                + "FinTrust Team";

        String html = """
                <!doctype html>
                <html lang="fr">
                <body style="margin:0;background:#eaf2ff;font-family:Arial,Helvetica,sans-serif;">
                  <div style="max-width:560px;margin:0 auto;padding:24px;">
                    <div style="background:linear-gradient(180deg,#ffffff 0%%,#f7fbff 100%%);border-radius:16px;padding:22px;border:1px solid #d7e7ff;">
                      <h2 style="margin:0 0 10px;color:#0c2b58;">Bienvenue chez FinTrust</h2>
                      <p style="margin:0;color:#35537a;line-height:1.6;">
                        Bonjour <b>%s</b>,<br><br>
                        Votre inscription sur FinTrust est bien enregistree.<br>
                        Notre equipe vous souhaite la bienvenue.
                      </p>
                    </div>
                    <p style="margin:14px 8px 0;color:#6e88ad;font-size:12px;">
                      (c) FinTrust - Message automatique, merci de ne pas repondre.
                    </p>
                  </div>
                </body>
                </html>
                """.formatted(safeNom);

        sendEmail(to, subject, text, html);
    }

    // Backward compatibility
    public void sendEmail(String to, String subject, String body) {
        sendEmail(to, subject, body, null);
    }

    // ===== Core sender =====
    public void sendEmail(String to, String subject, String textBody, String htmlBody) {
        if (isBlank(to)) {
            throw new IllegalArgumentException("Destinataire email vide.");
        }

        String username = getCfg("FINTRUST_SMTP_USERNAME");
        String pw = getCfg("FINTRUST_SMTP_PASSWORD");
        if (isBlank(pw)) pw = getCfg("FINTRUST_SMTP_APP_PASSWORD");
        if (!isBlank(pw)) pw = pw.replace(" ", "").trim();

        String from = getCfg("FINTRUST_SMTP_FROM");
        String host = getCfg("FINTRUST_SMTP_HOST");
        String port = getCfg("FINTRUST_SMTP_PORT");
        String startTls = getCfg("FINTRUST_SMTP_STARTTLS");
        String debug = getCfg("FINTRUST_SMTP_DEBUG");

        if (isBlank(host)) host = "smtp.gmail.com";
        if (isBlank(port)) port = "587";
        if (isBlank(startTls)) startTls = "true";
        if (isBlank(from)) from = username;

        if (isBlank(username) || isBlank(pw)) {
            throw new RuntimeException(
                    "SMTP non configure. Definissez FINTRUST_SMTP_USERNAME et FINTRUST_SMTP_PASSWORD "
                            + "(ou FINTRUST_SMTP_APP_PASSWORD)."
            );
        }

        final String usernameFinal = username;
        final String passwordFinal = pw;

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", startTls);
        props.put("mail.smtp.starttls.required", startTls);
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");

        if ("true".equalsIgnoreCase(debug)) {
            props.put("mail.debug", "true");
        }

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(usernameFinal, passwordFinal);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from, "FinTrust", StandardCharsets.UTF_8.name()));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));
            message.setSubject(subject == null ? "" : subject, StandardCharsets.UTF_8.name());

            if (isBlank(htmlBody)) {
                message.setText(textBody == null ? "" : textBody, StandardCharsets.UTF_8.name());
            } else {
                MimeBodyPart textPart = new MimeBodyPart();
                textPart.setText(textBody == null ? "" : textBody, StandardCharsets.UTF_8.name());

                MimeBodyPart htmlPart = new MimeBodyPart();
                htmlPart.setContent(htmlBody, "text/html; charset=UTF-8");

                MimeMultipart alternative = new MimeMultipart("alternative");
                alternative.addBodyPart(textPart);
                alternative.addBodyPart(htmlPart);
                message.setContent(alternative);
            }

            Transport.send(message);

        } catch (AuthenticationFailedException e) {
            throw new RuntimeException(
                    "Authentification SMTP refusee. Verifiez 2FA + App Password Gmail.",
                    e
            );
        } catch (Exception e) {
            throw new RuntimeException("Erreur envoi email: " + e.getMessage(), e);
        }
    }

    // ===== Templates =====
    private String buildAuthText(String code) {
        return "FinTrust - Verification\n\n"
                + "Votre code de verification est : " + code + "\n\n"
                + "Ce code expire dans " + CODE_TTL_MINUTES + " minutes.\n"
                + "Si vous n'etes pas a l'origine de cette demande, ignorez cet e-mail.";
    }

    private String buildResetText(String code) {
        return "FinTrust - Reinitialisation du mot de passe\n\n"
                + "Votre code de reinitialisation est : " + code + "\n\n"
                + "Ce code expire dans " + CODE_TTL_MINUTES + " minutes.\n"
                + "Si vous n'etes pas a l'origine de cette demande, ignorez cet e-mail.";
    }

    private String buildAuthHtml(String code) {
        return baseHtml("Verification", "Voici votre code de verification :", code);
    }

    private String buildResetHtml(String code) {
        return baseHtml("Reinitialisation du mot de passe", "Voici votre code de reinitialisation :", code);
    }

    private String baseHtml(String title, String intro, String code) {
        return """
                <!doctype html>
                <html lang="fr">
                <body style="margin:0;background:#eaf2ff;font-family:Arial,Helvetica,sans-serif;">
                  <div style="max-width:560px;margin:0 auto;padding:24px;">
                    <div style="background:linear-gradient(180deg,#ffffff 0%%,#f7fbff 100%%);border-radius:16px;padding:22px;border:1px solid #d7e7ff;">
                      <h2 style="margin:0 0 10px;color:#0c2b58;">%s</h2>
                      <p style="margin:0 0 14px;color:#35537a;line-height:1.5;">%s</p>

                      <div style="font-size:28px;font-weight:700;letter-spacing:6px;text-align:center;
                                  padding:14px 12px;border-radius:12px;background:linear-gradient(90deg,#dfeeff,#cddfff);color:#173f7a;">
                        %s
                      </div>

                      <p style="margin:14px 0 0;color:#35537a;line-height:1.5;">
                        Ce code expire dans <b>%d minutes</b>.
                      </p>
                      <p style="margin:10px 0 0;color:#5c7599;line-height:1.5;font-size:13px;">
                        Si vous n'avez pas demande cette action, ignorez cet e-mail.
                      </p>
                    </div>

                    <p style="margin:14px 8px 0;color:#6e88ad;font-size:12px;">
                      (c) FinTrust - Message automatique, merci de ne pas repondre.
                    </p>
                  </div>
                </body>
                </html>
                """.formatted(title, intro, code, CODE_TTL_MINUTES);
    }

    // ===== Config / Utils =====
    private String getCfg(String key) {
        String v = null;
        try {
            v = SecretConfig.get(key);
        } catch (Exception ignored) {
        }
        if (isBlank(v)) v = System.getenv(key);
        if (isBlank(v)) v = System.getProperty(key);
        return v == null ? "" : v.trim();
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
