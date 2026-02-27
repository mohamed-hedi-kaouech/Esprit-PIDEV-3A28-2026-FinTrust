package org.example.Service.WalletService;

import org.example.Model.Wallet.ClassWallet.ClientRisk;
import org.example.Model.Wallet.ClassWallet.Transaction;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SurveillanceService {

    private ScoreService scoreService;
    private EmailService emailService;

    public SurveillanceService() {
        this.scoreService = new ScoreService();
        this.emailService = new EmailService();
    }

    public List<ClientRisk> getClientsASurveiller() {
        List<ClientRisk> tousClients = scoreService.getAllClientsWithScores();
        return tousClients.stream()
                .filter(ClientRisk::estASurveiller)
                .collect(Collectors.toList());
    }

    public SurveillanceStats getStatistiquesSurveillance() {
        List<ClientRisk> tousClients = scoreService.getAllClientsWithScores();
        List<ClientRisk> aSurveiller = getClientsASurveiller();

        SurveillanceStats stats = new SurveillanceStats();
        stats.totalClients = tousClients.size();
        stats.clientsSurveilles = aSurveiller.size();
        stats.pourcentageSurveilles = tousClients.isEmpty() ? 0 :
                (aSurveiller.size() * 100.0) / tousClients.size();

        stats.nbScoreFaible = 0;
        stats.nbChequesRefuses = 0;
        stats.nbSoldeNegatif = 0;
        stats.nbRetraitsEleves = 0;

        for (ClientRisk client : aSurveiller) {
            if (client.getScore() < 50) stats.nbScoreFaible++;
            if (client.getNbChequesRefuses() > 2) stats.nbChequesRefuses++;
            if (client.getSolde() < 0) stats.nbSoldeNegatif++;
            if (client.getNbRetraitsEleves() > 3) stats.nbRetraitsEleves++;
        }

        return stats;
    }

    public boolean transactionEstRisquee(Transaction transaction, ClientRisk client) {
        if (client == null) return false;

        if (client.getScore() < 50 && transaction.getMontant() > 500) {
            return true;
        }

        if (transaction.getType().equals("RETRAIT") &&
                transaction.getMontant() > 1000 &&
                client.getNbRetraitsEleves() > 3) {
            return true;
        }

        return false;
    }

    public void declencherAlerte(ClientRisk client, String raison) {
        String message = String.format(
                "🚨 ALERTE SURVEILLANCE\nClient: %s %s\nRaison: %s\nScore: %d/100\nAction requise: Validation manuelle",
                client.getPrenom(), client.getNom(), raison, client.getScore()
        );

        // OPTION 1: Utiliser System.out pour les tests
        System.out.println("📧 EMAIL À ADMIN: admin@fintrust.com");
        System.out.println("Sujet: ALERTE Surveillance - " + client.getNomComplet());
        System.out.println("Message: " + message);

        System.out.println("📧 EMAIL À CLIENT: " + client.getEmail());
        System.out.println("Sujet: Sécurité compte");
        System.out.println("Message: 🔒 Sécurité renforcée sur votre compte. Veuillez contacter votre conseiller.");

        // OPTION 2: Si votre EmailService a une autre méthode, décommentez et adaptez
        // emailService.sendEmail("admin@fintrust.com", "ALERTE Surveillance - " + client.getNomComplet(), message);
        // emailService.sendEmail(client.getEmail(), "Sécurité compte", "🔒 Sécurité renforcée sur votre compte.");

        System.out.println("🚨 ALERTE DÉCLENCHÉE: " + message);
    }

    // Classe interne pour les statistiques
    public static class SurveillanceStats {
        public int totalClients;
        public int clientsSurveilles;
        public double pourcentageSurveilles;
        public int nbScoreFaible;
        public int nbChequesRefuses;
        public int nbSoldeNegatif;
        public int nbRetraitsEleves;

        public String toString() {
            return String.format(
                    "Surveillance: %d/%d clients (%.1f%%) - Score faible:%d, Chèques:%d, Solde négatif:%d, Retraits:%d",
                    clientsSurveilles, totalClients, pourcentageSurveilles,
                    nbScoreFaible, nbChequesRefuses, nbSoldeNegatif, nbRetraitsEleves
            );
        }
    }
}