package org.example.Service.WalletService;

import org.example.Model.Wallet.ClassWallet.Wallet;
import org.example.Model.Wallet.EnumWallet.WalletDevise;
import org.example.Model.Wallet.EnumWallet.WalletStatut;
import org.example.Utils.MaConnexion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WalletService {

    private Connection connection;
    private CodeService codeService;

    public WalletService() {
        this.connection = MaConnexion.getInstance().getCnx();
        this.codeService = new CodeService();
    }

    // ✅ AJOUTER UN WALLET (avec code généré automatiquement)
    public boolean ajouterWallet(Wallet wallet) {
        // Générer un code unique
        String code = codeService.genererCode();
        wallet.setCodeAcces(code);
        wallet.setEstActif(false);

        // ✅ REQUÊTE AVEC id_user
        String query = "INSERT INTO wallet (id_user, nom_proprietaire, telephone, email, code_acces, est_actif, solde, devise, statut, date_creation) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, wallet.getIdUser());
            pstmt.setString(2, wallet.getNomProprietaire());
            pstmt.setString(3, wallet.getTelephone());
            pstmt.setString(4, wallet.getEmail());
            pstmt.setString(5, wallet.getCodeAcces());
            pstmt.setBoolean(6, wallet.isEstActif());
            pstmt.setDouble(7, wallet.getSolde());
            pstmt.setString(8, wallet.getDevise().name());
            pstmt.setString(9, wallet.getStatut().name());
            pstmt.setTimestamp(10, Timestamp.valueOf(wallet.getDateCreation()));

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    wallet.setIdWallet(rs.getInt(1));
                }

                codeService.envoyerCode(wallet.getTelephone(), wallet.getEmail(), wallet.getCodeAcces());

                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ✅ RÉCUPÉRER TOUS LES WALLETS
    public List<Wallet> getAllWallets() {
        List<Wallet> wallets = new ArrayList<>();
        String query = "SELECT * FROM wallet";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                wallets.add(mapResultSetToWallet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return wallets;
    }

    // ✅ RÉCUPÉRER UN WALLET PAR ID
    public Wallet getWalletById(int id) {
        String query = "SELECT * FROM wallet WHERE id_wallet = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToWallet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ✅ RÉCUPÉRER UN WALLET PAR TÉLÉPHONE OU EMAIL
    public Wallet getWalletByIdentifiant(String identifiant) {
        String query = "SELECT * FROM wallet WHERE telephone = ? OR email = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, identifiant);
            pstmt.setString(2, identifiant);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToWallet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ✅ VÉRIFIER SI UN WALLET EXISTE
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

    // ✅ VÉRIFIER LE CODE D'ACCÈS
    public boolean verifierCode(String identifiant, String code) {
        String query = "SELECT * FROM wallet WHERE (telephone = ? OR email = ?) AND code_acces = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, identifiant);
            pstmt.setString(2, identifiant);
            pstmt.setString(3, code);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ✅ ACTIVER LE COMPTE APRÈS PREMIÈRE CONNEXION
    public boolean activerCompte(int idWallet) {
        String query = "UPDATE wallet SET est_actif = TRUE WHERE id_wallet = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, idWallet);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ✅ METTRE À JOUR LE SOLDE
    public boolean mettreAJourSolde(int idWallet, double nouveauSolde) {
        String query = "UPDATE wallet SET solde = ? WHERE id_wallet = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setDouble(1, nouveauSolde);
            pstmt.setInt(2, idWallet);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ✅ MODIFIER UN WALLET
    public boolean modifierWallet(Wallet wallet) {
        String query = "UPDATE wallet SET nom_proprietaire = ?, telephone = ?, email = ?, solde = ?, devise = ?, statut = ? WHERE id_wallet = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, wallet.getNomProprietaire());
            pstmt.setString(2, wallet.getTelephone());
            pstmt.setString(3, wallet.getEmail());
            pstmt.setDouble(4, wallet.getSolde());
            pstmt.setString(5, wallet.getDevise().name());
            pstmt.setString(6, wallet.getStatut().name());
            pstmt.setInt(7, wallet.getIdWallet());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ✅ SUPPRIMER UN WALLET (avec gestion des transactions liées)
    public boolean supprimerWallet(int idWallet) {
        String deleteTransactions = "DELETE FROM transaction WHERE id_wallet = ?";
        String deleteWallet = "DELETE FROM wallet WHERE id_wallet = ?";

        try {
            connection.setAutoCommit(false);

            try (PreparedStatement pstmt1 = connection.prepareStatement(deleteTransactions)) {
                pstmt1.setInt(1, idWallet);
                pstmt1.executeUpdate();
            }

            try (PreparedStatement pstmt2 = connection.prepareStatement(deleteWallet)) {
                pstmt2.setInt(1, idWallet);
                int result = pstmt2.executeUpdate();
                connection.commit();
                connection.setAutoCommit(true);
                return result > 0;
            }
        } catch (SQLException e) {
            try {
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        }
        return false;
    }

    // ✅ MAPPER ResultSet → Wallet (avec tous les champs)
    private Wallet mapResultSetToWallet(ResultSet rs) throws SQLException {
        Wallet wallet = new Wallet();
        wallet.setIdWallet(rs.getInt("id_wallet"));

        // ✅ Lire id_user
        try {
            wallet.setIdUser(rs.getInt("id_user"));
        } catch (SQLException e) {
            // La colonne n'existe pas, on ignore
        }

        wallet.setNomProprietaire(rs.getString("nom_proprietaire"));
        wallet.setTelephone(rs.getString("telephone"));
        wallet.setEmail(rs.getString("email"));
        wallet.setCodeAcces(rs.getString("code_acces"));
        wallet.setEstActif(rs.getBoolean("est_actif"));

        double solde = rs.getDouble("solde");
        double plafond = rs.getDouble("plafond_decouvert");
        if (rs.wasNull()) plafond = 0;
        wallet.setPlafondDecouvert(plafond);
        wallet.setSolde(solde);

        wallet.setTentativesEchouees(rs.getInt("tentatives_echouees"));

        Timestamp dateTentative = rs.getTimestamp("date_derniere_tentative");
        if (dateTentative != null) {
            wallet.setDateDerniereTentative(dateTentative.toLocalDateTime());
        }

        int bloqueInt = rs.getInt("est_bloque");
        wallet.setEstBloque(bloqueInt == 1);

        System.out.println("📊 Wallet " + wallet.getIdWallet() +
                " - " + wallet.getNomProprietaire() +
                " - Bloqué: " + wallet.isEstBloque() +
                " (DB: " + bloqueInt + ")" +
                " - User ID: " + wallet.getIdUser());

        wallet.setDevise(WalletDevise.valueOf(rs.getString("devise")));
        wallet.setStatut(WalletStatut.valueOf(rs.getString("statut")));
        wallet.setDateCreation(rs.getTimestamp("date_creation").toLocalDateTime());

        return wallet;
    }

    // ✅ RÉCUPÉRER UN WALLET PAR TÉLÉPHONE ET DEVISE
    public Wallet getWalletByTelephoneAndDevise(String telephone, String devise) {
        String query = "SELECT * FROM wallet WHERE telephone = ? AND devise = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, telephone);
            pstmt.setString(2, devise);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToWallet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ✅ METTRE À JOUR UN WALLET COMPLET
    public boolean mettreAJourWallet(Wallet wallet) {
        String query = "UPDATE wallet SET nom_proprietaire = ?, telephone = ?, email = ?, " +
                "solde = ?, plafond_decouvert = ?, devise = ?, statut = ?, " +
                "tentatives_echouees = ?, date_derniere_tentative = ?, est_bloque = ? " +
                "WHERE id_wallet = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, wallet.getNomProprietaire());
            pstmt.setString(2, wallet.getTelephone());
            pstmt.setString(3, wallet.getEmail());
            pstmt.setDouble(4, wallet.getSolde());
            pstmt.setDouble(5, wallet.getPlafondDecouvert());
            pstmt.setString(6, wallet.getDevise().name());
            pstmt.setString(7, wallet.getStatut().name());
            pstmt.setInt(8, wallet.getTentativesEchouees());
            pstmt.setTimestamp(9, wallet.getDateDerniereTentative() != null ?
                    Timestamp.valueOf(wallet.getDateDerniereTentative()) : null);
            pstmt.setInt(10, wallet.isEstBloque() ? 1 : 0);
            pstmt.setInt(11, wallet.getIdWallet());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ✅ VÉRIFIER SI UN UTILISATEUR A DÉJÀ UN WALLET (NOUVELLE MÉTHODE)
    public boolean userHasWallet(int userId) {
        String query = "SELECT COUNT(*) FROM wallet WHERE id_user = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ✅ CRÉER UN WALLET PAR DÉFAUT POUR UN UTILISATEUR
    public boolean creerWalletParDefautSiAbsent(String proprietaire) {
        try {
            // Vérifier si l'utilisateur a déjà un wallet
            String checkQuery = "SELECT COUNT(*) FROM wallet WHERE nom_proprietaire = ? OR email = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(checkQuery)) {
                pstmt.setString(1, proprietaire);
                pstmt.setString(2, proprietaire);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    System.out.println("👤 " + proprietaire + " a déjà un wallet");
                    return false; // Wallet déjà existant
                }
            }

            // Créer un nouveau wallet par défaut
            Wallet wallet = new Wallet();
            wallet.setNomProprietaire(proprietaire);
            wallet.setSolde(0.0); // Solde initial à 0
            wallet.setDevise(WalletDevise.TND); // Devise par défaut
            wallet.setStatut(WalletStatut.ACTIVE);
            wallet.setEstActif(true);
            wallet.setPlafondDecouvert(0); // Pas de découvert par défaut

            // Générer un code d'accès
            String code = codeService.genererCode();
            wallet.setCodeAcces(code);

            // Insérer dans la base
            String query = "INSERT INTO wallet (nom_proprietaire, telephone, email, code_acces, est_actif, solde, plafond_decouvert, devise, statut, date_creation) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, wallet.getNomProprietaire());
                pstmt.setString(2, wallet.getTelephone()); // Peut être null
                pstmt.setString(3, wallet.getEmail());     // Peut être null
                pstmt.setString(4, wallet.getCodeAcces());
                pstmt.setBoolean(5, wallet.isEstActif());
                pstmt.setDouble(6, wallet.getSolde());
                pstmt.setDouble(7, wallet.getPlafondDecouvert());
                pstmt.setString(8, wallet.getDevise().name());
                pstmt.setString(9, wallet.getStatut().name());
                pstmt.setTimestamp(10, Timestamp.valueOf(wallet.getDateCreation()));

                int affectedRows = pstmt.executeUpdate();

                if (affectedRows > 0) {
                    ResultSet rs = pstmt.getGeneratedKeys();
                    if (rs.next()) {
                        wallet.setIdWallet(rs.getInt(1));
                    }

                    // Envoyer le code par SMS/email
                    codeService.envoyerCode(wallet.getTelephone(), wallet.getEmail(), wallet.getCodeAcces());

                    System.out.println("✅ Wallet créé pour " + proprietaire);
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}