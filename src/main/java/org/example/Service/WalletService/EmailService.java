package org.example.Service.WalletService;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class EmailService {

    private final String username = "Hajji.Feryel@esprit.tn";  // ← Ton email
    private final String password = "ilxorjddhbvgocrq";           // ← Mot de passe d'application

    /**
     * Envoie un email avec un code de validation (pour 2FA)
     */
    public void envoyerCodeParEmail(String destinataire, String code) {
        Properties props = getMailProperties();
        Session session = getMailSession(props);

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinataire));
            message.setSubject("Bienvenue sur FinTrust - Votre code d'accès");

            String contenu = "Bonjour,\n\n" +
                    "Votre wallet a été créé avec succès.\n" +
                    "Votre code d'accès personnel est : " + code + "\n\n" +
                    "Utilisez ce code avec votre email pour vous connecter à votre espace client.\n\n" +
                    "Cordialement,\nL'équipe FinTrust";

            message.setText(contenu);

            Transport.send(message);
            System.out.println("✅ Email envoyé à " + destinataire);

        } catch (MessagingException e) {
            e.printStackTrace();
            System.out.println("❌ Échec de l'envoi d'email");
        }
    }

    /**
     * ✅ NOUVELLE MÉTHODE : Envoie un email simple (pour notifications, alertes, etc.)
     */
    public void envoyerEmailSimple(String destinataire, String sujet, String contenu) {
        Properties props = getMailProperties();
        Session session = getMailSession(props);

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinataire));
            message.setSubject(sujet);
            message.setText(contenu);

            Transport.send(message);
            System.out.println("✅ Email simple envoyé à " + destinataire);

        } catch (MessagingException e) {
            System.err.println("❌ Échec de l'envoi d'email simple à " + destinataire);
            e.printStackTrace();
        }
    }

    /**
     * ✅ Méthode utilitaire pour les propriétés SMTP
     */
    private Properties getMailProperties() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.debug", "false"); // Mettre "true" pour déboguer
        return props;
    }

    /**
     * ✅ Méthode utilitaire pour créer la session mail
     */
    private Session getMailSession(Properties props) {
        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }
}