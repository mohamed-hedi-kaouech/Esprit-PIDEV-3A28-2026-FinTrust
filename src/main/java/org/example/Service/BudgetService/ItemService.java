package org.example.Service.BudgetService;

import org.example.Interfaces.InterfaceGlobal;
import org.example.Model.Budget.Categorie;
import org.example.Model.Budget.Item;
import org.example.Utils.MaConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ItemService implements InterfaceGlobal<Item> {

    Connection cnx = MaConnexion.getInstance().getCnx();

    // ADD
    @Override
    public void Add(Item item) {
        String req = "INSERT INTO item(libelle, montant, idCategorie) VALUES (?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, item.getLibelle());
            ps.setDouble(2, item.getMontant());
            ps.setInt(3, item.getCategorie().getIdCategorie());
            ps.executeUpdate();
            System.out.println("✅ Item ajouté avec succès !");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    // DELETE
    @Override
    public void Delete(Integer id) {
        String req = "DELETE FROM item WHERE idItem = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("✅ Item supprimé avec succès !");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // UPDATE
    @Override
    public void Update(Item item) {
        String req = "UPDATE item SET libelle=?, montant=?, idCategorie=? WHERE idItem=?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, item.getLibelle());
            ps.setDouble(2, item.getMontant());
            ps.setInt(3, item.getCategorie().getIdCategorie());
            ps.setInt(4, item.getIdItem());
            ps.executeUpdate();
            System.out.println("✅ Item modifié avec succès !");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    // READ ALL
    @Override
    public List<Item> ReadAll() {
        List<Item> items = new ArrayList<>();
        String req = "SELECT * FROM item";
        try (Statement st = cnx.createStatement();
             ResultSet res = st.executeQuery(req)) {
            while (res.next()) {
                Item item = new Item();
                item.setIdItem(res.getInt("idItem"));
                item.setLibelle(res.getString("libelle"));
                item.setMontant(res.getDouble("montant"));
                int idCategorie = res.getInt("idCategorie");
                item.setIdCategorie(idCategorie);
                // Fetch and set Categorie object
                Categorie categorie = new BudgetService().ReadId(idCategorie);
                item.setCategorie(categorie);
                items.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return items;
    }

    // READ BY ID
    @Override
    public Item ReadId(Integer id) {
        String req = "SELECT * FROM item WHERE idItem = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, id);
            try (ResultSet res = ps.executeQuery()) {
                if (res.next()) {
                    Item item = new Item();
                    item.setIdItem(res.getInt("idItem"));
                    item.setLibelle(res.getString("libelle"));
                    item.setMontant(res.getDouble("montant"));
                    int idCategorie = res.getInt("idCategorie");
                    item.setIdCategorie(idCategorie);
                    Categorie categorie = new BudgetService().ReadId(idCategorie);
                    item.setCategorie(categorie);
                    return item;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return null;
    }

    // READ BY CATEGORY
    public List<Item> ReadByCategory(Integer idCategorie) {
        List<Item> items = new ArrayList<>();
        String req = "SELECT * FROM item WHERE idCategorie = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, idCategorie);
            try (ResultSet res = ps.executeQuery()) {
                while (res.next()) {
                    Item item = new Item();
                    item.setIdItem(res.getInt("idItem"));
                    item.setLibelle(res.getString("libelle"));
                    item.setMontant(res.getDouble("montant"));
                    item.setIdCategorie(idCategorie);
                    Categorie categorie = new BudgetService().ReadId(idCategorie);
                    item.setCategorie(categorie);
                    items.add(item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return items;
    }
}