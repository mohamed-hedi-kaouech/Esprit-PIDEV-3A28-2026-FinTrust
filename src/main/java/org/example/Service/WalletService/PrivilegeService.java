package org.example.Service.WalletService;

import org.example.Model.Wallet.ClassWallet.ClientRisk;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class PrivilegeService {

    private EmailService emailService;
    private Connection conn;

    public PrivilegeService() {
        this.emailService = new EmailService();
        // this.conn = MaConnexion.getInstance().getCnx(); // À décommenter avec votre connexion
    }

    public void mettreAJourPrivileges(ClientRisk client) {
        String ancienPrivilege = client.getPrivilege();
        String nouveauPrivilege = determinerPrivilege(client.getScore());

        if (!ancienPrivilege.equals(nouveauPrivilege)) {
            client.setPrivilege(nouveauPrivilege);
            sauvegarderPrivilege(client);
            notifierChangementPrivilege(client, ancienPrivilege, nouveauPrivilege);
        }
    }

    private String determinerPrivilege(int score) {
        if (score >= 80) return "VIP";
        if (score >= 50) return "STANDARD";
        return "SURVEILLE";
    }

    private void sauvegarderPrivilege(ClientRisk client) {
        String query = "UPDATE user SET privilege = ? WHERE id = ?";
        try {
            // PreparedStatement pstmt = conn.prepareStatement(query);
            // pstmt.setString(1, client.getPrivilege());
            // pstmt.setInt(2, client.getUserId());
            // pstmt.executeUpdate();
            System.out.println("✅ Privilège mis à jour: " + client.getNomComplet() + " -> " + client.getPrivilege());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void notifierChangementPrivilege(ClientRisk client, String ancien, String nouveau) {
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

        // OPTION 1: Utiliser System.out pour les tests (pas d'erreur)
        System.out.println("📧 EMAIL À: " + client.getEmail());
        System.out.println("   Sujet: " + sujet);
        System.out.println("   Message: " + message);
        System.out.println("----------------------------------------");

        // OPTION 2: Si votre EmailService a une méthode sendEmail (à adapter)
        // emailService.sendEmail(client.getEmail(), sujet, message);

        // OPTION 3: Si votre EmailService a envoyerMail (à adapter)
        // emailService.envoyerMail(client.getEmail(), sujet, message);
    }

    public void appliquerRestrictions(ClientRisk client) {
        if (!"SURVEILLE".equals(client.getPrivilege())) return;

        if (client.getNbChequesRefuses() > 3) {
            bloquerChequier(client);
        }

        alerterAdmin(client);
    }

    private void bloquerChequier(ClientRisk client) {
        String query = "UPDATE wallet SET chequier_bloque = true WHERE user_id = ?";
        try {
            // PreparedStatement pstmt = conn.prepareStatement(query);
            // pstmt.setInt(1, client.getUserId());
            // pstmt.executeUpdate();
            System.out.println("🚫 Chéquier bloqué pour " + client.getNomComplet());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void alerterAdmin(ClientRisk client) {
        String message = String.format(
                "🚨 ALERTE ADMIN - Client à risque\n\n" +
                        "Client: %s %s\n" +
                        "Score: %d/100\n" +
                        "Chèques refusés: %d\n" +
                        "Solde: %.2f€\n" +
                        "Raisons: %s\n\n" +
                        "Action requise: Vérification manuelle immédiate",
                client.getPrenom(), client.getNom(),
                client.getScore(),
                client.getNbChequesRefuses(),
                client.getSolde(),
                client.getRaisonsSurveillance()
        );

        // OPTION 1: System.out pour les tests
        System.out.println("🚨 ALERTE ADMIN");
        System.out.println(message);
        System.out.println("----------------------------------------");

        // OPTION 2: Si votre EmailService a une méthode
        // emailService.sendEmail("admin@fintrust.com", "URGENT - Client à risque", message);
    }
}