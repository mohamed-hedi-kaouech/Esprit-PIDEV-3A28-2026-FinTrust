package org.example.Service.LoanService;

import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.util.Properties;

public class EmailService {

    private final String username = System.getenv("MAIL_USER");
    private final String password = System.getenv("MAIL_PASS");

    public void sendRepaymentConfirmation(String toEmail,
                                          int loanId,
                                          int month,
                                          double amount) {

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props,
                new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(toEmail)
            );

            message.setSubject("Paiement confirmé - Loan #" + loanId);

            message.setText("""
                Bonjour,

                Votre paiement du mois %d a été reçu.

                Montant payé: %.2f

                Merci.
                """.formatted(month, amount));

            Transport.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Error sending email", e);
        }
    }
}
