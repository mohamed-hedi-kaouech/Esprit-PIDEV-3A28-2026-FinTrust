package org.example.Service;

import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.Random;

import javax.mail.AuthenticationFailedException;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.example.Utils.SecretConfig;

public class EmailService {

    private static final Random RNG = new Random();

    public String generateVerificationCode() {
        return String.valueOf(100000 + RNG.nextInt(900000));
    }

    public void sendAuthVerificationCode(String email, String code) {
        String subject = "FinTrust - Code d'authentification";
        String body = "Bonjour,\n\n"
                + "Votre code d'authentification FinTrust est : " + code + "\n\n"
                + "Ce code expire dans 10 minutes.\n"
                + "Si vous n'etes pas a l'origine de cette demande, ignorez cet email.";
        sendEmail(email, subject, body);
    }

    public void sendPasswordResetCode(String email, String code) {
        String subject = "Code de réinitialisation FinTrust";
        String body = "Votre code de réinitialisation FinTrust est : " + code + "\n\n"
                + "Ce code expire dans 10 minutes.";
        sendEmail(email, subject, body);
    }

    public void sendEmail(String to, String subject, String body) {
        if (isBlank(to)) {
            throw new IllegalArgumentException("Destinataire email vide.");
        }

        // 1) Charger config (SecretConfig -> env -> system properties)
        String username = getCfg("FINTRUST_SMTP_USERNAME");

        String pw = getCfg("FINTRUST_SMTP_PASSWORD");
        if (isBlank(pw)) {
            pw = getCfg("FINTRUST_SMTP_APP_PASSWORD"); // compat
        }

        // Nettoyage: si l’app password est collé avec espaces
        if (!isBlank(pw)) {
            pw = pw.replace(" ", "").trim();
        }

        String from = getCfg("FINTRUST_SMTP_FROM");
        String host = getCfg("FINTRUST_SMTP_HOST");
        String port = getCfg("FINTRUST_SMTP_PORT");
        String startTls = getCfg("FINTRUST_SMTP_STARTTLS");
        String debug = getCfg("FINTRUST_SMTP_DEBUG");

        // Defaults Gmail
        if (isBlank(host)) host = "smtp.gmail.com";
        if (isBlank(port)) port = "587";          // ✅ IMPORTANT: Gmail STARTTLS = 587
        if (isBlank(startTls)) startTls = "true";
        if (isBlank(from)) from = username;

        // 2) Validation claire
        if (isBlank(username) || isBlank(pw)) {
            throw new RuntimeException(
                    "SMTP non configuré. Définissez FINTRUST_SMTP_USERNAME et FINTRUST_SMTP_PASSWORD " +
                    "(ou FINTRUST_SMTP_APP_PASSWORD) dans fintrustlocal.properties / config.properties / variables d'environnement."
            );
        }

        // 2.1) Debug utile (sans exposer le password)
        System.out.println("SMTP_USER=" + username);
        System.out.println("SMTP_HOST=" + host + ":" + port);
        System.out.println("SMTP_PASS_LEN=" + pw.length());
        System.out.println("SMTP_FROM=" + from);

        // 3) Variables finales pour l’Authenticator
        final String usernameFinal = username;
        final String passwordFinal = pw;

        // 4) Propriétés JavaMail robustes
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");

        // STARTTLS
        props.put("mail.smtp.starttls.enable", startTls);     // true
        props.put("mail.smtp.starttls.required", startTls);   // ✅ plus strict

        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);

        // timeouts
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");

        // debug optionnel
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
            throw new RuntimeException(
                    "Authentification SMTP refusée (Gmail). Vérifiez : 2FA activée + App Password (16 chars) + sans espaces.",
                    e
            );
        } catch (Exception e) {
            throw new RuntimeException("Erreur envoi email: " + e.getMessage(), e);
        }
    }

    private String getCfg(String key) {
        String v = null;

        try {
            v = SecretConfig.get(key);
        } catch (Exception ignored) {}

        if (isBlank(v)) v = System.getenv(key);
        if (isBlank(v)) v = System.getProperty(key);

        return v == null ? "" : v.trim();
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    public void sendWelcomeEmail(String email, String nom) {
        String firstName = isBlank(nom) ? "client" : nom.trim();
        String subject = "Bienvenue sur FinTrust";
        String body = "Bonjour " + firstName + ",\n\n"
                + "Bienvenue sur FinTrust.\n"
                + "Votre compte a ete cree avec succes et est en attente de validation admin.\n\n"
                + "Vous pouvez vous connecter depuis la page de login apres validation.\n\n"
                + "L'equipe FinTrust";
        sendEmail(email, subject, body);
    }
}
