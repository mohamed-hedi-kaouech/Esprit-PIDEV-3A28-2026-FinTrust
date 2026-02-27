package org.example.Controlleurs.WalletControlleur;

import org.example.Model.Wallet.ClassWallet.ClientRisk;
import org.example.Service.WalletService.EmailService;
import javafx.scene.control.Alert;

public class PrivilegeManager {

    private EmailService emailService;

    public PrivilegeManager() {
        this.emailService = new EmailService();
    }

    public void mettreAJourPrivileges(ClientRisk client) {
        String ancienPrivilege = client.getPrivilege();
        String nouveauPrivilege = determinerPrivilege(client.getScore());

        if (!ancienPrivilege.equals(nouveauPrivilege)) {
            client.setPrivilege(nouveauPrivilege);
            notifierChangement(client, ancienPrivilege, nouveauPrivilege);
            afficherNotification(client, nouveauPrivilege);
        }
    }

    private String determinerPrivilege(int score) {
        if (score >= 80) return "VIP";
        if (score >= 50) return "STANDARD";
        return "SURVEILLE";
    }

    private void notifierChangement(ClientRisk client, String ancien, String nouveau) {
        String sujet = "FinTrust - Évolution de votre statut";
        String message;

        if ("VIP".equals(nouveau)) {
            message = "🎉 Félicitations " + client.getPrenom() + " !\n\n" +
                    "Votre comportement bancaire exemplaire vous fait passer au statut VIP.\n\n" +
                    "✅ Vos nouveaux avantages :\n" +
                    "- Plafond de retrait augmenté à 5000€\n" +
                    "- Validation automatique des chèques\n" +
                    "- Frais bancaires réduits de 50%\n" +
                    "- Accès à un conseiller dédié\n\n" +
                    "Merci de votre confiance !";
        } else if ("SURVEILLE".equals(nouveau)) {
            message = "🔒 Sécurité renforcée sur votre compte\n\n" +
                    "Bonjour " + client.getPrenom() + ",\n\n" +
                    "Suite à une analyse de vos récentes activités, nous renforçons temporairement " +
                    "la sécurité de votre compte.\n\n" +
                    "📋 Mesures appliquées :\n" +
                    "- Validation manuelle des transactions\n" +
                    "- Plafond temporairement réduit à 200€\n" +
                    "- Vérification d'identité renforcée\n\n" +
                    "Contactez votre conseiller pour plus d'informations.";
        } else {
            message = "Votre statut a été mis à jour : " + ancien + " → " + nouveau;
        }

        // SOLUTION: Utiliser System.out au lieu d'envoyerEmail
        System.out.println("📧 EMAIL À: " + client.getEmail());
        System.out.println("   Sujet: " + sujet);
        System.out.println("   Message: " + message);
        System.out.println("----------------------------------------");

        // Optionnel: Si vous voulez utiliser EmailService avec la bonne méthode
        // emailService.sendEmail(client.getEmail(), sujet, message); // Si la méthode est sendEmail
        // emailService.envoyerMail(client.getEmail(), sujet, message); // Si la méthode est envoyerMail
    }

    private void afficherNotification(ClientRisk client, String nouveauPrivilege) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Changement de privilège");
        alert.setHeaderText("Client : " + client.getNomComplet());
        alert.setContentText("Le client passe au statut : " + nouveauPrivilege);
        alert.showAndWait();
    }

    public static String getDescriptionPrivilege(String privilege) {
        switch (privilege) {
            case "VIP":
                return "✅ Plafond: 5000€\n✅ Validation auto\n✅ Frais réduits";
            case "STANDARD":
                return "📊 Plafond: 1000€\n📊 Validation normale";
            case "SURVEILLE":
                return "⚠️ Plafond: 200€\n⚠️ Validation manuelle\n⚠️ Surveillance renforcée";
            default:
                return "";
        }
    }
}