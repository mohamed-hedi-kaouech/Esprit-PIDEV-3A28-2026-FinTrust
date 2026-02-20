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

    public TransactionService() {
        connection = MaConnexion.getInstance().getCnx();
        walletService = new WalletService();
    }

    public boolean ajouterTransaction(Transaction transaction) {
        // Vérifier si le wallet existe
        if (!walletService.walletExiste(transaction.getId_wallet())) {
            throw new RuntimeException("Le wallet n'existe pas");
        }

        // Récupérer le wallet pour vérifier le solde
        Wallet wallet = walletService.getWalletById(transaction.getId_wallet());

        // Vérifier le type de transaction et le solde
        if (transaction.getType().equals("RETRAIT") || transaction.getType().equals("TRANSFERT")) {
            if (wallet.getSolde() < transaction.getMontant()) {
                throw new RuntimeException("Solde insuffisant pour effectuer cette transaction");
            }
            // Mettre à jour le solde (retrait)
            walletService.mettreAJourSolde(transaction.getId_wallet(),
                    wallet.getSolde() - transaction.getMontant());
        } else if (transaction.getType().equals("DEPOT")) {
            // Mettre à jour le solde (dépôt)
            walletService.mettreAJourSolde(transaction.getId_wallet(),
                    wallet.getSolde() + transaction.getMontant());
        }

        // Insérer la transaction
        String query = "INSERT INTO transaction (montant, type, date_transaction, id_wallet) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setDouble(1, transaction.getMontant());
            pstmt.setString(2, transaction.getType());
            pstmt.setTimestamp(3, Timestamp.valueOf(transaction.getDate_transaction()));
            pstmt.setInt(4, transaction.getId_wallet());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    transaction.setId_transaction(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Transaction> getTransactionsByWallet(int id_wallet) {
        List<Transaction> transactions = new ArrayList<>();
        String query = "SELECT * FROM transaction WHERE id_wallet = ? ORDER BY date_transaction DESC";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, id_wallet);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Transaction transaction = new Transaction();
                transaction.setId_transaction(rs.getInt("id_transaction"));
                transaction.setMontant(rs.getDouble("montant"));
                transaction.setType(rs.getString("type"));
                transaction.setDate_transaction(rs.getTimestamp("date_transaction").toLocalDateTime());
                transaction.setId_wallet(rs.getInt("id_wallet"));

                transactions.add(transaction);
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
                Transaction transaction = new Transaction();
                transaction.setId_transaction(rs.getInt("id_transaction"));
                transaction.setMontant(rs.getDouble("montant"));
                transaction.setType(rs.getString("type"));
                transaction.setDate_transaction(rs.getTimestamp("date_transaction").toLocalDateTime());
                transaction.setId_wallet(rs.getInt("id_wallet"));

                transactions.add(transaction);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactions;
    }

    public boolean modifierTransaction(Transaction transaction) {
        String query = "UPDATE transaction SET montant = ?, type = ? WHERE id_transaction = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setDouble(1, transaction.getMontant());
            pstmt.setString(2, transaction.getType());
            pstmt.setInt(3, transaction.getId_transaction());

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
}