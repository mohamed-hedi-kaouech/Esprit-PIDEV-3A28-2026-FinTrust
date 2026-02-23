package org.example.Service;

import org.example.Utils.SecretConfig;

import javax.mail.Authenticator;
import javax.mail.AuthenticationFailedException;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.Random;

public class EmailService {

    private static final Random RNG = new Random();

    public String generateVerificationCode() {
        return String.valueOf(100000 + RNG.nextInt(900000));
    }

    public String sendPasswordResetCode(String email) {
        String code = generateVerificationCode();
        sendPasswordResetCode(email, code);
        return code;
    }

    public void sendPasswordResetCode(String email, String code) {
        String subject = "Code de reinitialisation FinTrust";
        String body = "Votre code de reinitialisation FinTrust est : " + code +
                "\n\nCe code expire dans 10 minutes.";
        sendEmail(email, subject, body);
    }

    public void sendNotification(String to, String message) {
        sendEmail(to, "Notification FinTrust", message);
    }

    public void sendWelcomeEmail(String to, String nom) {
        String safeNom = (nom == null || nom.isBlank()) ? "client" : nom.trim();
        String subject = "Bienvenue chez FinTrust";
        String body = "Bienvenue " + safeNom + ",\n\n"
                + "Votre inscription sur FinTrust est bien enregistree.\n"
                + "Notre equipe vous souhaite la bienvenue.\n\n"
                + "FinTrust Team";
        sendEmail(to, subject, body);
    }

    public void sendEmail(String to, String subject, String body) {
        if (to == null || to.isBlank()) {
            throw new IllegalArgumentException("Destinataire email vide.");
        }

        String username = cfg("FINTRUST_SMTP_USERNAME");
        String password = cfg("FINTRUST_SMTP_PASSWORD");
        if (isBlank(password)) {
            password = cfg("FINTRUST_SMTP_APP_PASSWORD");
        }

        if (isBlank(username) || isBlank(password)) {
            throw new RuntimeException("SMTP non configure. Verifiez config.properties.");
        }

        String host = cfg("FINTRUST_SMTP_HOST");
        String port = cfg("FINTRUST_SMTP_PORT");
        String from = cfg("FINTRUST_SMTP_FROM");
        String startTls = cfg("FINTRUST_SMTP_STARTTLS");
        String debug = cfg("FINTRUST_SMTP_DEBUG");

        if (isBlank(host)) host = "smtp.gmail.com";
        if (isBlank(port)) port = "587";
        if (isBlank(from)) from = username;
        if (isBlank(startTls)) startTls = "true";

        final String usernameFinal = username;
        final String passwordFinal = password;

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", startTls);
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
            message.setText(body == null ? "" : body, StandardCharsets.UTF_8.name());
            Transport.send(message);
        } catch (AuthenticationFailedException e) {
            throw new RuntimeException("Authentification Gmail echouee. Utilisez un App Password.", e);
        } catch (Exception e) {
            throw new RuntimeException("Erreur envoi email: " + e.getMessage(), e);
        }
    }

    private String cfg(String key) {
        String value = SecretConfig.get(key);
        return value == null ? "" : value.trim();
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
