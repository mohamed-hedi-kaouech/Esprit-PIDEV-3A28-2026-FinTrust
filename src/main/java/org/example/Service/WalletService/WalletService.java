package org.example.Service.WalletService;

// CORRECTION DES IMPORTS - Ajout de "Wallet" dans le chemin
import org.example.Model.Wallet.ClassWallet.Wallet;
import org.example.Model.Wallet.EnumWallet.WalletDevise;
import org.example.Model.Wallet.EnumWallet.WalletStatut;
import org.example.Utils.MaConnexion;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class WalletService {
    private Connection connection;

    public WalletService() {
        // Utilisation de MaConnexion pour obtenir la connexion
        connection = MaConnexion.getInstance().getCnx();
    }

    // Create
    public boolean ajouterWallet(Wallet wallet) {
        String query = "INSERT INTO wallet (nom_proprietaire, solde, devise, statut, date_creation) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, wallet.getNom_proprietaire());
            pstmt.setDouble(2, wallet.getSolde());
            pstmt.setString(3, wallet.getDevise().getCode());
            pstmt.setString(4, wallet.getStatut().getCode());
            pstmt.setTimestamp(5, Timestamp.valueOf(wallet.getDate_creation()));

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    wallet.setId_wallet(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Read - Tous les wallets
    public List<Wallet> getAllWallets() {
        List<Wallet> wallets = new ArrayList<>();
        String query = "SELECT * FROM wallet ORDER BY date_creation DESC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Wallet wallet = new Wallet();
                wallet.setId_wallet(rs.getInt("id_wallet"));
                wallet.setNom_proprietaire(rs.getString("nom_proprietaire"));
                wallet.setSolde(rs.getDouble("solde"));
                wallet.setDevise(WalletDevise.valueOf(rs.getString("devise")));
                wallet.setStatut(WalletStatut.valueOf(rs.getString("statut")));
                wallet.setDate_creation(rs.getTimestamp("date_creation").toLocalDateTime());

                wallets.add(wallet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return wallets;
    }

    // Read - Wallet par ID
    public Wallet getWalletById(int id) {
        String query = "SELECT * FROM wallet WHERE id_wallet = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Wallet wallet = new Wallet();
                wallet.setId_wallet(rs.getInt("id_wallet"));
                wallet.setNom_proprietaire(rs.getString("nom_proprietaire"));
                wallet.setSolde(rs.getDouble("solde"));
                wallet.setDevise(WalletDevise.valueOf(rs.getString("devise")));
                wallet.setStatut(WalletStatut.valueOf(rs.getString("statut")));
                wallet.setDate_creation(rs.getTimestamp("date_creation").toLocalDateTime());

                return wallet;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Update
    public boolean modifierWallet(Wallet wallet) {
        String query = "UPDATE wallet SET nom_proprietaire = ?, solde = ?, devise = ?, statut = ? WHERE id_wallet = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, wallet.getNom_proprietaire());
            pstmt.setDouble(2, wallet.getSolde());
            pstmt.setString(3, wallet.getDevise().getCode());
            pstmt.setString(4, wallet.getStatut().getCode());
            pstmt.setInt(5, wallet.getId_wallet());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Delete
    public boolean supprimerWallet(int id) {
        String query = "DELETE FROM wallet WHERE id_wallet = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Vérifier si un wallet existe
    public boolean walletExiste(int id) {
        String query = "SELECT COUNT(*) FROM wallet WHERE id_wallet = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Mettre à jour le solde
    public boolean mettreAJourSolde(int id_wallet, double nouveauSolde) {
        String query = "UPDATE wallet SET solde = ? WHERE id_wallet = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setDouble(1, nouveauSolde);
            pstmt.setInt(2, id_wallet);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean walletExistePourProprietaire(String nomProprietaire) {
        if (nomProprietaire == null || nomProprietaire.trim().isEmpty()) {
            return false;
        }
        String query = "SELECT 1 FROM wallet WHERE nom_proprietaire = ? LIMIT 1";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, nomProprietaire.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean creerWalletParDefautSiAbsent(String nomProprietaire) {
        String owner = nomProprietaire == null ? "" : nomProprietaire.trim();
        if (owner.isEmpty()) {
            return false;
        }
        if (walletExistePourProprietaire(owner)) {
            return true;
        }

        Wallet wallet = new Wallet();
        wallet.setNom_proprietaire(owner);
        wallet.setSolde(0.0);
        wallet.setDevise(WalletDevise.TND);
        wallet.setStatut(WalletStatut.ACTIVE);
        wallet.setDate_creation(LocalDateTime.now());
        return ajouterWallet(wallet);
    }
}
