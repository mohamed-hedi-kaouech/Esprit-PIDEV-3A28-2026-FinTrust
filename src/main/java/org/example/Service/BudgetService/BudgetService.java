package org.example.Service.BudgetService;

import org.example.Interfaces.InterfaceGlobal;
import org.example.Model.Budget.Categorie;
import org.example.Utils.MaConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BudgetService implements InterfaceGlobal<Categorie> {

    Connection cnx = MaConnexion.getInstance().getCnx();

    // ADD
    @Override
    public void Add(Categorie c) {
        String req = "INSERT INTO categorie(nomCategorie, budgetPrevu, seuilAlerte) VALUES (?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, c.getNomCategorie());
            ps.setDouble(2, c.getBudgetPrevu());
            ps.setDouble(3, c.getSeuilAlerte());
            ps.executeUpdate();
            System.out.println("✅ Catégorie ajoutée avec succès !");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    // DELETE
    @Override
    public void Delete(Integer id) {
        String req = "DELETE FROM categorie WHERE idCategorie = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("✅ Catégorie supprimée avec succès !");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    // UPDATE
    @Override
    public void Update(Categorie c) {
        String req = "UPDATE categorie SET nomCategorie=?, budgetPrevu=?, seuilAlerte=? WHERE idCategorie=?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, c.getNomCategorie());
            ps.setDouble(2, c.getBudgetPrevu());
            ps.setDouble(3, c.getSeuilAlerte());
            ps.setInt(4, c.getIdCategorie());
            ps.executeUpdate();
            System.out.println("✅ Catégorie modifiée avec succès !");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    // READ ALL
    @Override
    public List<Categorie> ReadAll() {
        List<Categorie> categories = new ArrayList<>();
        String req = "SELECT * FROM categorie";
        try (Statement st = cnx.createStatement();
             ResultSet res = st.executeQuery(req)) {
            while (res.next()) {
                Categorie c = new Categorie();
                c.setIdCategorie(res.getInt("idCategorie"));
                c.setNomCategorie(res.getString("nomCategorie"));
                c.setBudgetPrevu(res.getDouble("budgetPrevu"));
                c.setSeuilAlerte(res.getDouble("seuilAlerte"));
                categories.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return categories;
    }

    // READ BY ID
    @Override
    public Categorie ReadId(Integer id) {
        String req = "SELECT * FROM categorie WHERE idCategorie = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, id);
            try (ResultSet res = ps.executeQuery()) {
                if (res.next()) {
                    Categorie c = new Categorie();
                    c.setIdCategorie(res.getInt("idCategorie"));
                    c.setNomCategorie(res.getString("nomCategorie"));
                    c.setBudgetPrevu(res.getDouble("budgetPrevu"));
                    c.setSeuilAlerte(res.getDouble("seuilAlerte"));
                    return c;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Helper methods that return boolean for controllers
    public boolean delete(int id) {
        try {
            Delete(id);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(Categorie c) {
        try {
            Update(c);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}