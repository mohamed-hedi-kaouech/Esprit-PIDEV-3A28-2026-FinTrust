package org.example.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.Random;

public class EmailService {
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String USERNAME = "fintrust@gmail.com"; // Compte FinTrust
    private static final String PASSWORD = "fintrustpassword"; // App password sécurisé

    public String sendPasswordResetCode(String email) {
        String code = String.valueOf(100000 + new Random().nextInt(900000));
        String subject = "Code de réinitialisation FinTrust";
        String body = "Votre code de réinitialisation est : " + code;
        sendEmail(email, subject, body);
        return code;
    }

    public void sendNotification(String to, String message) {
        sendEmail(to, "Notification FinTrust", message);
    }

    public void sendEmail(String to, String subject, String body) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USERNAME, PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(USERNAME, "FinTrust"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(body);
            Transport.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
