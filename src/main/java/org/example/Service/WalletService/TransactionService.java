package org.example.Service.WalletService;

import org.example.Interfaces.InterfaceGlobal;
import org.example.Model.Wallet.Transaction;
import org.example.Utils.MaConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionService implements InterfaceGlobal<Transaction> {

    Connection cnx = MaConnexion.getInstance().getCnx();

    // ===== ADD =====
    @Override
    public void Add(Transaction t) {
        String req = "INSERT INTO transaction (wallet_id, montant, type, description, date_transaction) VALUES (?,?,?,?,?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, t.getWalletId());
            ps.setDouble(2, t.getMontant());
            ps.setString(3, t.getType());
            ps.setString(4, t.getDescription());
            ps.setTimestamp(5, Timestamp.valueOf(t.getDateTransaction()));
            ps.executeUpdate();
            System.out.println("Transaction ajoutée avec succès !");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ===== UPDATE =====
    @Override
    public void Update(Transaction t) {
        String req = "UPDATE transaction SET wallet_id=?, montant=?, type=?, description=? WHERE id_transaction=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, t.getWalletId());
            ps.setDouble(2, t.getMontant());
            ps.setString(3, t.getType());
            ps.setString(4, t.getDescription());
            ps.setInt(5, t.getIdTransaction());
            ps.executeUpdate();
            System.out.println("Transaction modifiée avec succès !");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ===== DELETE =====
    @Override
    public void Delete(Integer id) {
        String req = "DELETE FROM transaction WHERE id_transaction=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Transaction supprimée avec succès !");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ===== READ ALL =====
    @Override
    public List<Transaction> ReadAll() {
        List<Transaction> transactions = new ArrayList<>();
        String req = "SELECT * FROM transaction";

        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(req);

            while (rs.next()) {
                Transaction t = new Transaction();
                t.setIdTransaction(rs.getInt("id_transaction"));
                t.setWalletId(rs.getInt("wallet_id"));
                t.setMontant(rs.getDouble("montant"));
                t.setType(rs.getString("type"));
                t.setDescription(rs.getString("description"));
                t.setDateTransaction(rs.getTimestamp("date_transaction").toLocalDateTime());

                transactions.add(t);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return transactions;
    }

    // ===== READ BY ID =====
    @Override
    public Transaction ReadId(Integer id) {
        Transaction t = new Transaction();
        String req = "SELECT * FROM transaction WHERE id_transaction=?";

        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                t.setIdTransaction(rs.getInt("id_transaction"));
                t.setWalletId(rs.getInt("wallet_id"));
                t.setMontant(rs.getDouble("montant"));
                t.setType(rs.getString("type"));
                t.setDescription(rs.getString("description"));
                t.setDateTransaction(rs.getTimestamp("date_transaction").toLocalDateTime());
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return t;
    }

    // ===== BONUS : récupérer les transactions d’un wallet =====
    public List<Transaction> getByWallet(int walletId) {
        List<Transaction> transactions = new ArrayList<>();
        String req = "SELECT * FROM transaction WHERE wallet_id=?";

        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, walletId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Transaction t = new Transaction();
                t.setIdTransaction(rs.getInt("id_transaction"));
                t.setWalletId(rs.getInt("wallet_id"));
                t.setMontant(rs.getDouble("montant"));
                t.setType(rs.getString("type"));
                t.setDescription(rs.getString("description"));
                t.setDateTransaction(rs.getTimestamp("date_transaction").toLocalDateTime());

                transactions.add(t);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return transactions;
    }
}
