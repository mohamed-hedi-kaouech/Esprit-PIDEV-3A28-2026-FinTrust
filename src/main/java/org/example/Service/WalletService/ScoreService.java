package org.example.Service.WalletService;

import org.example.Model.Wallet.ClassWallet.ClientRisk;
import org.example.Model.Wallet.ClassWallet.ScoreConfiance;
import org.example.Model.Wallet.ClassWallet.Wallet;
import org.example.Model.Wallet.ClassWallet.Transaction;
import org.example.Utils.MaConnexion;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ScoreService {

    private Connection conn;
    private ScoreConfianceService scoreConfianceService;
    private WalletService walletService;
    private TransactionService transactionService;

    public ScoreService() {
        this.scoreConfianceService = new ScoreConfianceService();
        this.walletService = new WalletService();
        this.transactionService = new TransactionService();
        this.conn = MaConnexion.getInstance().getCnx();
    }

    public void sauvegarderScore(ScoreConfiance score) {
        String query = "INSERT INTO score_confiance (wallet_id, score_global, anciennete, " +
                "transactions, stabilite, penalites, niveau, recommandation, date_calcul) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, score.getWalletId());
            pstmt.setInt(2, score.getScoreGlobal());
            pstmt.setInt(3, score.getAnciennete());
            pstmt.setInt(4, score.getTransactions());
            pstmt.setInt(5, score.getStabilite());

            try {
                pstmt.setInt(6, score.getPenalites());
            } catch (Exception e) {
                pstmt.setInt(6, 0);
            }

            pstmt.setString(7, score.getNiveau());
            pstmt.setString(8, score.getRecommandation());
            pstmt.setTimestamp(9, Timestamp.valueOf(score.getDateCalcul()));
            pstmt.executeUpdate();
            System.out.println("✅ Score sauvegardé pour wallet " + score.getWalletId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ✅ VERSION AVEC DONNÉES DES CHÈQUES
    public List<ClientRisk> getAllClientsWithScores() {
        List<ClientRisk> clients = new ArrayList<>();

        try {
            // Récupérer tous les wallets
            List<Wallet> wallets = walletService.getAllWallets();

            for (Wallet wallet : wallets) {
                int walletId = wallet.getIdWallet();

                // ✅ 1. Récupérer les transactions du wallet
                List<Transaction> transactions = getTransactionsByWalletId(walletId);

                // ✅ 2. Récupérer les statistiques des chèques
                ChequeStats chequeStats = getChequeStatsByWalletId(walletId);

                // ✅ 3. Mettre à jour les compteurs dans le wallet
                try {
                    // Utiliser la réflexion pour mettre à jour les champs
                    java.lang.reflect.Field fieldCheques = wallet.getClass().getDeclaredField("nbChequesRefuses");
                    fieldCheques.setAccessible(true);
                    fieldCheques.set(wallet, chequeStats.nbRejetes);

                    java.lang.reflect.Field fieldRetraits = wallet.getClass().getDeclaredField("nbRetraitsEleves");
                    fieldRetraits.setAccessible(true);
                    fieldRetraits.set(wallet, chequeStats.nbRetraitsEleves);

                    java.lang.reflect.Field fieldJours = wallet.getClass().getDeclaredField("nbJoursNegatifs");
                    fieldJours.setAccessible(true);
                    fieldJours.set(wallet, wallet.isEnDecouvert() ? chequeStats.nbJoursNegatifs : 0);
                } catch (Exception e) {
                    // Si les champs n'existent pas, on continue
                }

                // ✅ 4. Calculer le score
                ScoreConfiance score = scoreConfianceService.calculerScore(wallet, transactions);

                // ✅ 5. Récupérer les informations du client
                String nom = wallet.getNomProprietaire();
                String prenom = extractPrenom(nom);
                String email = wallet.getEmail() != null ? wallet.getEmail() : "email@inconnu.com";
                String telephone = wallet.getTelephone() != null ? wallet.getTelephone() : "00000000";

                // ✅ 6. Utiliser les VRAIES valeurs des chèques
                int nbChequesRefuses = chequeStats.nbRejetes;  // ← DEPUIS TABLE CHEQUE
                int nbRetraitsEleves = chequeStats.nbRetraitsEleves;
                int nbJoursNegatifs = wallet.isEnDecouvert() ? chequeStats.nbJoursNegatifs : 0;

                // ✅ 7. Déterminer le privilège
                String privilege = determinerPrivilege(score.getScoreGlobal(), nbChequesRefuses, wallet.getSolde());

                // ✅ 8. Créer l'objet ClientRisk
                ClientRisk client = new ClientRisk(
                        wallet.getIdWallet(), // userId (à adapter)
                        nom,
                        prenom,
                        email,
                        telephone,
                        score.getScoreGlobal(),
                        nbChequesRefuses,  // ← Valeur réelle !
                        wallet.getSolde(),
                        wallet.getDateCreation().toLocalDate(),
                        nbRetraitsEleves,
                        nbJoursNegatifs,
                        privilege
                );
                client.setWalletId(walletId);

                clients.add(client);

                // ✅ 9. Debug
                System.out.println("📊 Wallet " + walletId +
                        " | Chèques rejetés: " + nbChequesRefuses +
                        " | Total chèques: " + chequeStats.nbTotal +
                        " | Montant total chèques: " + chequeStats.montantTotal + "€");
            }

            System.out.println("✅ " + clients.size() + " clients chargés depuis la BDD");

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("⚠️ Utilisation des données simulées");
            clients = getDonneesSimulees();
        }

        return clients;
    }

    // ✅ Classe interne pour les statistiques des chèques
    private static class ChequeStats {
        int nbTotal = 0;
        int nbRejetes = 0;
        int nbPayes = 0;
        int nbEnAttente = 0;
        double montantTotal = 0;
        int nbRetraitsEleves = 0;
        int nbJoursNegatifs = 0;
    }

    // ✅ Méthode pour récupérer les statistiques des chèques
    private ChequeStats getChequeStatsByWalletId(int walletId) {
        ChequeStats stats = new ChequeStats();

        String query = "SELECT statut, COUNT(*) as nb, SUM(montant) as total, " +
                "MAX(montant) as max_montant " +
                "FROM cheque WHERE id_wallet = ? GROUP BY statut";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, walletId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String statut = rs.getString("statut");
                int nb = rs.getInt("nb");
                double total = rs.getDouble("total");

                stats.nbTotal += nb;
                stats.montantTotal += total;

                switch (statut) {
                    case "REJETE":
                        stats.nbRejetes = nb;
                        break;
                    case "PAYE":
                        stats.nbPayes = nb;
                        break;
                    case "EMIS":
                    case "RESERVE":
                        stats.nbEnAttente += nb;
                        break;
                }

                // Détecter les retraits élevés (chèques de montant important)
                if (total > 1000) {
                    stats.nbRetraitsEleves += nb;
                }
            }

            // Compter aussi les transactions de type RETRAIT élevé
            String retraitQuery = "SELECT COUNT(*) as nb FROM transaction " +
                    "WHERE id_wallet = ? AND type = 'RETRAIT' AND montant > 1000";
            try (PreparedStatement pstmt2 = conn.prepareStatement(retraitQuery)) {
                pstmt2.setInt(1, walletId);
                ResultSet rs2 = pstmt2.executeQuery();
                if (rs2.next()) {
                    stats.nbRetraitsEleves += rs2.getInt("nb");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return stats;
    }

    // ✅ Méthode pour déterminer le privilège
    private String determinerPrivilege(int score, int nbChequesRefuses, double solde) {
        if (score >= 80 && nbChequesRefuses == 0 && solde > 10000) {
            return "VIP";
        } else if (score >= 60 && nbChequesRefuses <= 1) {
            return "STANDARD";
        } else if (nbChequesRefuses > 2) {
            return "SURVEILLE";
        } else if (score < 40) {
            return "SURVEILLE";
        } else {
            return "STANDARD";
        }
    }

    // ✅ Méthode pour extraire prénom du nom complet
    private String extractPrenom(String nomComplet) {
        if (nomComplet == null || nomComplet.isEmpty()) return "Inconnu";
        String[] parts = nomComplet.split(" ");
        return parts.length > 1 ? parts[1] : parts[0];
    }

    // ✅ Méthode pour récupérer les transactions
    private List<Transaction> getTransactionsByWalletId(int walletId) {
        List<Transaction> transactions = new ArrayList<>();

        try {
            String query = "SELECT * FROM transaction WHERE id_wallet = ? ORDER BY date_transaction DESC";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, walletId);
                ResultSet rs = pstmt.executeQuery();

                while(rs.next()) {
                    Transaction t = new Transaction();
                    t.setId_transaction(rs.getInt("id_transaction"));
                    t.setId_wallet(rs.getInt("id_wallet"));
                    t.setMontant(rs.getDouble("montant"));
                    t.setType(rs.getString("type"));
                    t.setDescription(rs.getString("description"));
                    t.setDate_transaction(rs.getTimestamp("date_transaction").toLocalDateTime());
                    transactions.add(t);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return transactions;
    }

    // ✅ Méthode pour obtenir les clients à risque
    public List<ClientRisk> getClientsRisques() {
        return getAllClientsWithScores().stream()
                .filter(ClientRisk::estASurveiller)
                .collect(Collectors.toList());
    }

    // ✅ Mise à jour du privilège
    public void updatePrivilege(int walletId, String privilege) {
        String query = "UPDATE wallet SET privilege = ? WHERE id_wallet = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, privilege);
            pstmt.setInt(2, walletId);
            pstmt.executeUpdate();
            System.out.println("✅ Privilège mis à jour pour wallet " + walletId + " -> " + privilege);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ✅ Données simulées (fallback)
    private List<ClientRisk> getDonneesSimulees() {
        List<ClientRisk> clients = new ArrayList<>();

        clients.add(new ClientRisk(1, "Dupont", "Jean", "jean@email.com", "50123456",
                85, 0, 25000.0, LocalDate.now().minusMonths(24), 0, 0, "VIP"));
        clients.add(new ClientRisk(2, "Martin", "Sophie", "sophie@email.com", "51234567",
                45, 3, -500.0, LocalDate.now().minusMonths(3), 5, 2, "STANDARD"));
        clients.add(new ClientRisk(3, "Bernard", "Pierre", "pierre@email.com", "52345678",
                92, 0, 50000.0, LocalDate.now().minusMonths(36), 0, 0, "VIP"));
        clients.add(new ClientRisk(4, "Petit", "Marie", "marie@email.com", "53456789",
                30, 5, -1200.0, LocalDate.now().minusMonths(1), 12, 4, "SURVEILLE"));
        clients.add(new ClientRisk(5, "Moreau", "Lucas", "lucas@email.com", "54567890",
                65, 1, 3500.0, LocalDate.now().minusMonths(12), 2, 1, "STANDARD"));

        return clients;
    }

    // ✅ Récupérer l'historique des scores
    public List<ScoreConfiance> getHistoriqueScores(int walletId) {
        List<ScoreConfiance> historique = new ArrayList<>();
        String query = "SELECT * FROM score_confiance WHERE wallet_id = ? ORDER BY date_calcul DESC LIMIT 10";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, walletId);
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {
                ScoreConfiance score = new ScoreConfiance();
                score.setId(rs.getInt("id"));
                score.setScoreGlobal(rs.getInt("score_global"));
                score.setDateCalcul(rs.getTimestamp("date_calcul").toLocalDateTime());
                historique.add(score);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return historique;
    }
}