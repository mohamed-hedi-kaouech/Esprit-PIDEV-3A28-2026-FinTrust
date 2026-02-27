package org.example.Service.WalletService;

import org.example.Model.Wallet.ClassWallet.Wallet;
import org.example.Model.Wallet.ClassWallet.Transaction;
import org.example.Model.Wallet.ClassWallet.ScoreConfiance;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class ScoreConfianceService {

    private static final int MAX_SCORE = 100;

    public ScoreConfiance calculerScore(Wallet wallet, List<Transaction> transactions) {

        // 1. Ancienneté du compte (max 15 points)
        long moisAnciennete = ChronoUnit.MONTHS.between(
                wallet.getDateCreation(), LocalDateTime.now());
        int anciennetePoints = (int) Math.min(moisAnciennete, 15);

        // 🔴 CORRECTION : Si ancienneté < 1 mois, donner au moins 1 point
        if (anciennetePoints == 0 && moisAnciennete > 0) {
            anciennetePoints = 1;
        }

        // 2. Volume de transactions (max 20 points)
        int nbTransactions = transactions.size();
        int transactionsPoints = Math.min(nbTransactions * 2, 20); // 2 points par transaction

        // 3. Stabilité du solde (max 25 points)
        int stabilitePoints = calculerStabilite(wallet, transactions);

        // 4. Incidents (pénalités)
        int penalites = calculerPenalites(wallet, transactions);

        // ✅ Score total (ancienneté + transactions + stabilité) - pénalités
        int scoreTotal = anciennetePoints + transactionsPoints + stabilitePoints - penalites;

        // ✅ Garantir que le score est entre 0 et 100
        scoreTotal = Math.max(10, Math.min(scoreTotal, MAX_SCORE)); // Minimum 10

        // Niveau de confiance
        String niveau = determinerNiveau(scoreTotal);
        String recommandation = genererRecommandation(scoreTotal, penalites, wallet, transactions);

        return new ScoreConfiance(
                wallet.getIdWallet(),
                scoreTotal,
                anciennetePoints,
                transactionsPoints,
                stabilitePoints,
                niveau,
                recommandation
        );
    }

    private int calculerStabilite(Wallet wallet, List<Transaction> transactions) {
        if (transactions.isEmpty()) {
            return wallet.getSolde() > 0 ? 20 : 15; // Solde positif donne plus de points
        }

        double soldeInitial = wallet.getSolde();
        double sommeEcart = 0;
        int compteur = 0;

        double soldeCourant = soldeInitial;
        for (Transaction t : transactions) {
            if (t.getType().equals("RETRAIT") || t.getType().equals("TRANSFERT")) {
                soldeCourant += t.getMontant(); // Retrait = diminution
            } else if (t.getType().equals("DEPOT")) {
                soldeCourant -= t.getMontant(); // Dépôt = augmentation
            }

            sommeEcart += Math.abs(soldeCourant - soldeInitial);
            compteur++;
        }

        if (compteur == 0) return 25;

        double ecartMoyen = sommeEcart / compteur;
        double ratio = ecartMoyen / Math.max(1, Math.abs(soldeInitial));

        if (ratio < 0.1) return 25;
        if (ratio < 0.3) return 20;
        if (ratio < 0.5) return 15;
        if (ratio < 0.8) return 10;
        return 5;
    }

    private int calculerPenalites(Wallet wallet, List<Transaction> transactions) {
        int penalites = 0;

        // ✅ Pénalité pour découvert (max 15 points)
        if (wallet.isEnDecouvert()) {
            double montantDecouvert = Math.abs(wallet.getSolde());
            if (montantDecouvert > 1000) penalites += 15;
            else if (montantDecouvert > 500) penalites += 10;
            else penalites += 5;
        }

        // ✅ Pénalité pour chèques refusés (10 points par chèque)
        long nbRejets = transactions.stream()
                .filter(t -> "CHEQUE_REJETE".equals(t.getType()) ||
                        (t.getDescription() != null && t.getDescription().toLowerCase().contains("rejet")))
                .count();
        penalites += nbRejets * 10;

        // ✅ Pénalité pour compte bloqué
        if (wallet.isEstBloque()) {
            penalites += 20;
        }

        // ✅ Pénalité pour tentatives échouées (max 10 points)
        int tentatives = wallet.getTentativesEchouees();
        penalites += Math.min(tentatives * 2, 10);

        return penalites;
    }

    private String determinerNiveau(int score) {
        if (score >= 90) return "🌟🌟🌟🌟🌟 Exceptionnel";
        if (score >= 75) return "🌟🌟🌟🌟 Très bon";
        if (score >= 60) return "🌟🌟🌟 Bon";
        if (score >= 40) return "🌟🌟 Moyen";
        if (score >= 20) return "🌟 Fragile";
        return "⚠️ Risqué";
    }

    private String genererRecommandation(int score, int penalites, Wallet wallet, List<Transaction> transactions) {
        StringBuilder recommandation = new StringBuilder();

        if (score >= 80) {
            recommandation.append("✅ Client fiable - Aucune restriction");
        } else if (score >= 50) {
            recommandation.append("📊 Client à surveiller - Vérifications aléatoires");
        } else {
            recommandation.append("🔒 Surveillance renforcée recommandée");

            // Ajouter des détails spécifiques
            if (wallet.isEnDecouvert()) {
                recommandation.append(" (Compte à découvert)");
            }

            long nbRejets = transactions.stream()
                    .filter(t -> "CHEQUE_REJETE".equals(t.getType()))
                    .count();
            if (nbRejets > 0) {
                recommandation.append(" - ").append(nbRejets).append(" chèque(s) rejeté(s)");
            }

            if (wallet.getTentativesEchouees() > 3) {
                recommandation.append(" - Tentatives de connexion échouées");
            }
        }

        return recommandation.toString();
    }
}