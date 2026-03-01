package org.example.Utils;

import org.example.Model.Budget.Alerte;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * Sends alert emails via SMTP.
 * Supports test mode for development/debugging.
 */
public class EmailSender {

    /**
     * Send an alert email when a budget threshold is exceeded.
     * @param alerte the alert that was triggered
     * @param categoryName the category name for better readability
     * @return true if email was sent, false otherwise
     */
    public static boolean sendAlerteEmail(Alerte alerte, String categoryName) {
        try {
            // Build email content
            String subject = "Alerte Budget: " + (categoryName != null ? categoryName : ("Catégorie " + alerte.getIdCategorie()));
            StringBuilder body = new StringBuilder();
            body.append("⚠️ Alerte de dépassement de budget\n\n");
            body.append("Catégorie: ").append(categoryName != null ? categoryName : alerte.getIdCategorie()).append("\n");
            body.append("Message: ").append(alerte.getMessage()).append("\n");
            body.append("Seuil: ").append(String.format("%.2f DT", alerte.getSeuil())).append("\n");
            if (alerte.getCreatedAt() != null) {
                body.append("Date: ").append(alerte.getCreatedAt()).append("\n");
            }

            // Log configuration for debugging
            System.out.println("[EmailSender] Config host=" + EmailConfig.SMTP_HOST + " port=" + EmailConfig.SMTP_PORT + " user=" + (EmailConfig.SMTP_USER.isEmpty()?"(none)":EmailConfig.SMTP_USER) + " testMode=" + EmailConfig.TEST_MODE);

            // Test mode: just log the email instead of sending
            if (EmailConfig.TEST_MODE) {
                System.out.println("\n" + "=".repeat(70));
                System.out.println("📧 [EMAIL TEST MODE] Would send email:");
                System.out.println("=".repeat(70));
                System.out.println("To: " + EmailConfig.DEFAULT_RECIPIENT);
                System.out.println("From: " + EmailConfig.SMTP_FROM);
                System.out.println("Subject: " + subject);
                System.out.println("-".repeat(70));
                System.out.println(body.toString());
                System.out.println("=".repeat(70) + "\n");
                return true;
            }

            // Production mode: actually send via SMTP
            Properties props = new Properties();
            props.put("mail.smtp.host", EmailConfig.SMTP_HOST);
            props.put("mail.smtp.port", EmailConfig.SMTP_PORT);
            props.put("mail.smtp.auth", !EmailConfig.SMTP_USER.isEmpty());
            props.put("mail.smtp.starttls.enable", String.valueOf(EmailConfig.USE_STARTTLS));
            props.put("mail.smtp.starttls.required", String.valueOf(EmailConfig.USE_STARTTLS));
            props.put("mail.smtp.ssl.enable", String.valueOf(EmailConfig.USE_SSL));
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");

            Session session;
            if (!EmailConfig.SMTP_USER.isEmpty()) {
                session = Session.getInstance(props, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(EmailConfig.SMTP_USER, EmailConfig.SMTP_PASS);
                    }
                });
            } else {
                session = Session.getInstance(props);
            }

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EmailConfig.SMTP_FROM));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(EmailConfig.DEFAULT_RECIPIENT));
            message.setSubject(subject);
            message.setText(body.toString());

            Transport.send(message);
            System.out.println("[EmailSender] ✓ Alert email sent to: " + EmailConfig.DEFAULT_RECIPIENT);
            return true;

        } catch (MessagingException e) {
            System.err.println("[EmailSender] ✗ Failed to send alert email: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.err.println("[EmailSender] ✗ Unexpected error sending email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
