package org.example.Service.WalletService;

import org.example.Model.Wallet.ClassWallet.Cheque;
import org.example.Model.Wallet.ClassWallet.Wallet;
import org.example.Utils.MaConnexion;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ChequeService {
    private Connection connection;
    private WalletService walletService;

    public ChequeService() {
        connection = MaConnexion.getInstance().getCnx();
        walletService = new WalletService();
    }

    // ✅ Émettre un nouveau chèque
    public boolean emettreCheque(Cheque cheque) {
        System.out.println("📝 Émission chèque: " + cheque.getNumero_cheque());
        System.out.println("   Montant: " + cheque.getMontant());
        System.out.println("   Bénéficiaire: " + cheque.getBeneficiaire());
        System.out.println("   Wallet ID: " + cheque.getId_wallet());

        Wallet wallet = walletService.getWalletById(cheque.getId_wallet());

        // Vérifier le solde
        if (wallet.getSolde() < cheque.getMontant()) {
            // Solde insuffisant → statut RESERVE (découvert)
            cheque.setStatut("RESERVE");
            System.out.println("⚠️ Solde insuffisant - Statut RESERVE");
        }

        String query = "INSERT INTO cheque (numero_cheque, montant, date_emission, statut, id_wallet, beneficiaire) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, cheque.getNumero_cheque());
            pstmt.setDouble(2, cheque.getMontant());
            pstmt.setTimestamp(3, Timestamp.valueOf(cheque.getDate_emission()));
            pstmt.setString(4, cheque.getStatut());
            pstmt.setInt(5, cheque.getId_wallet());
            pstmt.setString(6, cheque.getBeneficiaire());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    cheque.setId_cheque(rs.getInt(1));
                    System.out.println("✅ Chèque inséré avec ID: " + cheque.getId_cheque());
                }

                // Mettre à jour le solde (le montant est "réservé")
                if (cheque.getStatut().equals("RESERVE")) {
                    walletService.mettreAJourSolde(cheque.getId_wallet(),
                            wallet.getSolde() - cheque.getMontant());
                    System.out.println("💰 Solde mis à jour (réservation)");
                }

                return true;
            } else {
                System.out.println("❌ Échec insertion chèque");
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // ✅ Présenter un chèque (quand il est encaissé)
    public boolean presenterCheque(int idCheque) {
        Cheque cheque = getChequeById(idCheque);
        if (cheque == null) {
            System.out.println("❌ Chèque non trouvé: " + idCheque);
            return false;
        }

        Wallet wallet = walletService.getWalletById(cheque.getId_wallet());

        try {
            connection.setAutoCommit(false);
            System.out.println("🔄 Présentation du chèque: " + cheque.getNumero_cheque());

            // Mettre à jour le statut du chèque
            String updateCheque = "UPDATE cheque SET statut = 'PAYE', date_presentation = ? WHERE id_cheque = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(updateCheque)) {
                pstmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                pstmt.setInt(2, idCheque);
                pstmt.executeUpdate();
                System.out.println("✅ Statut mis à jour: PAYE");
            }

            // Mettre à jour le solde (si ce n'est pas déjà fait)
            if (cheque.getStatut().equals("EMIS") && wallet.getSolde() >= cheque.getMontant()) {
                walletService.mettreAJourSolde(cheque.getId_wallet(),
                        wallet.getSolde() - cheque.getMontant());
                System.out.println("💰 Solde mis à jour");
            }

            connection.commit();
            connection.setAutoCommit(true);
            return true;

        } catch (SQLException e) {
            try {
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            System.err.println("❌ Erreur présentation: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ✅ Rejeter un chèque (provision insuffisante)
    public boolean rejeterCheque(int idCheque, String motif) {
        System.out.println("🔄 Rejet du chèque: " + idCheque + " - Motif: " + motif);
        String query = "UPDATE cheque SET statut = 'REJETE', motif_rejet = ? WHERE id_cheque = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, motif);
            pstmt.setInt(2, idCheque);
            int result = pstmt.executeUpdate();
            if (result > 0) {
                System.out.println("✅ Chèque rejeté avec succès");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur rejet: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // ✅ Récupérer les chèques d'un wallet
    public List<Cheque> getChequesByWallet(int idWallet) {
        List<Cheque> cheques = new ArrayList<>();
        String query = "SELECT * FROM cheque WHERE id_wallet = ? ORDER BY date_emission DESC";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, idWallet);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                cheques.add(mapResultSetToCheque(rs));
            }
            System.out.println("📊 Chèques pour wallet " + idWallet + ": " + cheques.size());
        } catch (SQLException e) {
            System.err.println("❌ Erreur getChequesByWallet: " + e.getMessage());
            e.printStackTrace();
        }
        return cheques;
    }

    // ✅ Récupérer un chèque par ID
    public Cheque getChequeById(int id) {
        String query = "SELECT * FROM cheque WHERE id_cheque = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToCheque(rs);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur getChequeById: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // ✅ Mapper ResultSet → Cheque (avec nom du propriétaire)
    private Cheque mapResultSetToCheque(ResultSet rs) throws SQLException {
        Cheque cheque = new Cheque();
        cheque.setId_cheque(rs.getInt("id_cheque"));
        cheque.setNumero_cheque(rs.getString("numero_cheque"));
        cheque.setMontant(rs.getDouble("montant"));
        cheque.setDate_emission(rs.getTimestamp("date_emission").toLocalDateTime());
        cheque.setStatut(rs.getString("statut"));
        cheque.setId_wallet(rs.getInt("id_wallet"));
        cheque.setBeneficiaire(rs.getString("beneficiaire"));

        // ✅ Récupérer le nom du propriétaire depuis la jointure (si présente)
        try {
            String nom = rs.getString("nom_proprietaire");
            if (nom != null && !nom.isEmpty()) {
                cheque.setNomProprietaire(nom);
            }
        } catch (SQLException e) {
            // La colonne n'existe pas dans cette requête - ignorer
        }

        Timestamp datePresentation = rs.getTimestamp("date_presentation");
        if (datePresentation != null) {
            cheque.setDate_presentation(datePresentation.toLocalDateTime());
        }

        cheque.setMotif_rejet(rs.getString("motif_rejet"));
        return cheque;
    }

    private void showAlert(String title, String message) {
        System.out.println(title + " : " + message);
    }

    // ✅ Pour l'admin : récupérer TOUS les chèques avec le nom du propriétaire
    public List<Cheque> getAllCheques() {
        List<Cheque> cheques = new ArrayList<>();
        String query = "SELECT c.*, w.nom_proprietaire FROM cheque c " +
                "JOIN wallet w ON c.id_wallet = w.id_wallet " +
                "ORDER BY c.date_emission DESC";

        System.out.println("🔍 Récupération de tous les chèques pour l'admin...");

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Cheque cheque = mapResultSetToCheque(rs);
                cheques.add(cheque);
                System.out.println("✅ Chèque chargé: " + cheque.getNumero_cheque() +
                        " | " + cheque.getMontant() +
                        " | " + cheque.getBeneficiaire() +
                        " | Propriétaire: " + cheque.getNomProprietaire());
            }
            System.out.println("📊 Total chèques chargés: " + cheques.size());

        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL dans getAllCheques: " + e.getMessage());
            e.printStackTrace();
        }
        return cheques;
    }
}