package org.example.Service.WalletService;

import org.example.Model.Wallet.ClassWallet.Transaction;
import org.example.Model.Wallet.ClassWallet.Wallet;
import org.example.Utils.MaConnexion;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransactionService {
    private Connection connection;
    private WalletService walletService;
    private EmailService emailService;
    private SmsService smsService;

    public TransactionService() {
        this.connection = MaConnexion.getInstance().getCnx();
        this.walletService = new WalletService();
        this.emailService = new EmailService();
        this.smsService = new SmsService();
    }

    public boolean ajouterTransaction(Transaction transaction) {
        if (!walletService.walletExiste(transaction.getId_wallet())) {
            throw new RuntimeException("Le wallet n'existe pas");
        }

        Wallet wallet = walletService.getWalletById(transaction.getId_wallet());
        boolean etaitEnDecouvert = wallet.isEnDecouvert();
        double ancienSolde = wallet.getSolde();

        if (transaction.getType().equals("RETRAIT") || transaction.getType().equals("TRANSFERT")) {
            double nouveauSolde = ancienSolde - transaction.getMontant();

            if (nouveauSolde < -wallet.getPlafondDecouvert()) {
                throw new RuntimeException(
                        String.format("Opération refusée. Découvert maximum : %.2f %s",
                                wallet.getPlafondDecouvert(), wallet.getDevise())
                );
            }

            walletService.mettreAJourSolde(transaction.getId_wallet(), nouveauSolde);

            if (!etaitEnDecouvert && nouveauSolde < 0) {
                notifierDecouvert(wallet, nouveauSolde);
            }

        } else if (transaction.getType().equals("DEPOT")) {
            double nouveauSolde = ancienSolde + transaction.getMontant();
            walletService.mettreAJourSolde(transaction.getId_wallet(), nouveauSolde);

            if (etaitEnDecouvert && nouveauSolde >= 0) {
                notifierSortieDecouvert(wallet, nouveauSolde);
            }
        }

        // ✅ CORRIGÉ : Requête AVEC description
        String query = "INSERT INTO transaction (montant, type, description, date_transaction, id_wallet) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setDouble(1, transaction.getMontant());
            pstmt.setString(2, transaction.getType());
            pstmt.setString(3, transaction.getDescription());  // ← AJOUT DE LA DESCRIPTION
            pstmt.setTimestamp(4, Timestamp.valueOf(transaction.getDate_transaction()));
            pstmt.setInt(5, transaction.getId_wallet());

            System.out.println("📝 Insertion transaction: " + transaction.getMontant() +
                    " TND - Description: '" + transaction.getDescription() + "'");

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    transaction.setId_transaction(rs.getInt(1));
                }

                wallet = walletService.getWalletById(transaction.getId_wallet());
                if (wallet.isEnDecouvert()) {
                    appliquerAgiosSiNecessaire(wallet);
                }

                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void appliquerAgiosSiNecessaire(Wallet wallet) {
        AgiosService agiosService = new AgiosService();
        double agios = agiosService.calculerAgiosMensuels(wallet);

        if (agios > 0) {
            String query = "INSERT INTO transaction (montant, type, description, date_transaction, id_wallet) VALUES (?, 'AGIOS', ?, ?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                pstmt.setDouble(1, agios);
                pstmt.setString(2, "Agios sur découvert - " + wallet.getNomProprietaire());  // ← Description
                pstmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                pstmt.setInt(4, wallet.getId_wallet());
                pstmt.executeUpdate();
                System.out.println("💰 Agios appliqués : " + agios + " " + wallet.getDevise());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public List<Transaction> getTransactionsByWallet(int id_wallet) {
        List<Transaction> transactions = new ArrayList<>();
        String query = "SELECT * FROM transaction WHERE id_wallet = ? ORDER BY date_transaction DESC";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, id_wallet);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                transactions.add(mapResultSetToTransaction(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactions;
    }

    public List<Transaction> getAllTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        String query = "SELECT t.*, w.nom_proprietaire FROM transaction t " +
                "JOIN wallet w ON t.id_wallet = w.id_wallet " +
                "ORDER BY t.date_transaction DESC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                transactions.add(mapResultSetToTransaction(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactions;
    }

    public boolean modifierTransaction(Transaction transaction) {
        String query = "UPDATE transaction SET montant = ?, type = ?, description = ? WHERE id_transaction = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setDouble(1, transaction.getMontant());
            pstmt.setString(2, transaction.getType());
            pstmt.setString(3, transaction.getDescription());  // ← AJOUT DE LA DESCRIPTION
            pstmt.setInt(4, transaction.getId_transaction());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean supprimerTransaction(int id) {
        String query = "DELETE FROM transaction WHERE id_transaction = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public double getSoldeWallet(int id_wallet) {
        Wallet wallet = walletService.getWalletById(id_wallet);
        return wallet != null ? wallet.getSolde() : 0;
    }

    // ✅ CORRIGÉ : mapResultSetToTransaction avec description
    private Transaction mapResultSetToTransaction(ResultSet rs) throws SQLException {
        Transaction transaction = new Transaction();
        transaction.setId_transaction(rs.getInt("id_transaction"));
        transaction.setMontant(rs.getDouble("montant"));
        transaction.setType(rs.getString("type"));
        transaction.setDescription(rs.getString("description"));  // ← AJOUT DE LA DESCRIPTION
        transaction.setDate_transaction(rs.getTimestamp("date_transaction").toLocalDateTime());
        transaction.setId_wallet(rs.getInt("id_wallet"));
        return transaction;
    }

    public boolean effectuerTransfert(int expediteurId, int destinataireId, double montant) {
        try {
            connection.setAutoCommit(false);

            Wallet expediteur = walletService.getWalletById(expediteurId);
            boolean etaitEnDecouvert = expediteur.isEnDecouvert();

            double nouveauSoldeExpediteur = expediteur.getSolde() - montant;
            if (nouveauSoldeExpediteur < -expediteur.getPlafondDecouvert()) {
                throw new RuntimeException(
                        String.format("Transfert refusé. Découvert maximum : %.2f %s",
                                expediteur.getPlafondDecouvert(), expediteur.getDevise())
                );
            }

            // Transaction de retrait pour l'expéditeur (AVEC description)
            String queryRetrait = "INSERT INTO transaction (montant, type, description, date_transaction, id_wallet) VALUES (?, 'RETRAIT', ?, ?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(queryRetrait)) {
                pstmt.setDouble(1, montant);
                pstmt.setString(2, "Transfert sortant vers wallet " + destinataireId);
                pstmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                pstmt.setInt(4, expediteurId);
                pstmt.executeUpdate();
            }

            // Transaction de dépôt pour le destinataire (AVEC description)
            String queryDepot = "INSERT INTO transaction (montant, type, description, date_transaction, id_wallet) VALUES (?, 'DEPOT', ?, ?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(queryDepot)) {
                pstmt.setDouble(1, montant);
                pstmt.setString(2, "Transfert entrant depuis wallet " + expediteurId);
                pstmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                pstmt.setInt(4, destinataireId);
                pstmt.executeUpdate();
            }

            walletService.mettreAJourSolde(expediteurId, nouveauSoldeExpediteur);

            Wallet destinataire = walletService.getWalletById(destinataireId);
            walletService.mettreAJourSolde(destinataireId, destinataire.getSolde() + montant);

            connection.commit();
            connection.setAutoCommit(true);

            if (!etaitEnDecouvert && nouveauSoldeExpediteur < 0) {
                notifierDecouvert(expediteur, nouveauSoldeExpediteur);
            }

            if (nouveauSoldeExpediteur < 0) {
                appliquerAgiosSiNecessaire(walletService.getWalletById(expediteurId));
            }

            return true;

        } catch (SQLException | RuntimeException e) {
            try {
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return false;
        }
    }

    private void notifierDecouvert(Wallet wallet, double nouveauSolde) {
        String message = String.format(
                "⚠️ ALERTE DÉCOUVERT : Votre compte %s est entré en découvert.\n" +
                        "Nouveau solde : %.2f %s\n" +
                        "Plafond autorisé : %.2f %s\n" +
                        "Marge restante : %.2f %s",
                wallet.getNomProprietaire(),
                nouveauSolde, wallet.getDevise(),
                wallet.getPlafondDecouvert(), wallet.getDevise(),
                wallet.getMargeDisponible(), wallet.getDevise()
        );

        System.out.println(message);

        if (wallet.getEmail() != null && !wallet.getEmail().isEmpty()) {
            emailService.envoyerEmailSimple(
                    wallet.getEmail(),
                    "⚠️ Alerte découvert - FinTrust",
                    message
            );
        }

        if (wallet.getTelephone() != null && !wallet.getTelephone().isEmpty()) {
            smsService.envoyerCodeSms(
                    wallet.getTelephone(),
                    "Découvert: " + String.format("%.2f %s", nouveauSolde, wallet.getDevise())
            );
        }
    }

    private void notifierSortieDecouvert(Wallet wallet, double nouveauSolde) {
        String message = String.format(
                "✅ Votre compte %s n'est plus à découvert.\n" +
                        "Nouveau solde : %.2f %s",
                wallet.getNomProprietaire(),
                nouveauSolde, wallet.getDevise()
        );

        System.out.println(message);

        if (wallet.getEmail() != null && !wallet.getEmail().isEmpty()) {
            emailService.envoyerEmailSimple(
                    wallet.getEmail(),
                    "✅ Sortie de découvert - FinTrust",
                    message
            );
        }
    }
}