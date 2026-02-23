package org.example.Service.WalletService;

import org.example.Interfaces.InterfaceGlobal;
import org.example.Model.Wallet.Wallet;
import org.example.Utils.MaConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WalletService implements InterfaceGlobal<Wallet> {

    Connection cnx = MaConnexion.getInstance().getCnx();

    // ===== ADD =====
    @Override
    public void Add(Wallet w) {
        String req = "INSERT INTO wallet (nom_proprietaire, solde, devise, statut, date_creation) VALUES (?,?,?,?,?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, w.getNomProprietaire());
            ps.setDouble(2, w.getSolde());
            ps.setString(3, w.getDevise());
            ps.setString(4, w.getStatut());
            ps.setTimestamp(5, Timestamp.valueOf(w.getDateCreation()));
            ps.executeUpdate();
            System.out.println("Wallet ajouté avec succès !");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ===== UPDATE =====
    @Override
    public void Update(Wallet w) {
        String req = "UPDATE wallet SET nom_proprietaire=?, solde=?, devise=?, statut=? WHERE id_wallet=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, w.getNomProprietaire());
            ps.setDouble(2, w.getSolde());
            ps.setString(3, w.getDevise());
            ps.setString(4, w.getStatut());
            ps.setInt(5, w.getIdWallet());
            ps.executeUpdate();
            System.out.println("Wallet modifié avec succès !");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ===== DELETE =====
    @Override
    public void Delete(Integer id) {
        String req = "DELETE FROM wallet WHERE id_wallet=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Wallet supprimé avec succès !");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ===== READ ALL =====
    @Override
    public List<Wallet> ReadAll() {
        List<Wallet> wallets = new ArrayList<>();
        String req = "SELECT * FROM wallet";
        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(req);

            while (rs.next()) {
                Wallet w = new Wallet();
                w.setIdWallet(rs.getInt("id_wallet"));
                w.setNomProprietaire(rs.getString("nom_proprietaire"));
                w.setSolde(rs.getDouble("solde"));
                w.setDevise(rs.getString("devise"));
                w.setStatut(rs.getString("statut"));
                w.setDateCreation(rs.getTimestamp("date_creation").toLocalDateTime());

                wallets.add(w);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return wallets;
    }

    // ===== READ BY ID =====
    @Override
    public Wallet ReadId(Integer id) {
        Wallet w = new Wallet();
        String req = "SELECT * FROM wallet WHERE id_wallet=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                w.setIdWallet(rs.getInt("id_wallet"));
                w.setNomProprietaire(rs.getString("nom_proprietaire"));
                w.setSolde(rs.getDouble("solde"));
                w.setDevise(rs.getString("devise"));
                w.setStatut(rs.getString("statut"));
                w.setDateCreation(rs.getTimestamp("date_creation").toLocalDateTime());
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return w;
    }
}
