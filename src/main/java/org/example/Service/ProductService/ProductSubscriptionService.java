package org.example.Service.ProductService;

import org.example.Interfaces.InterfaceGlobal;
import org.example.Model.Product.ClassProduct.Product;
import org.example.Model.Product.ClassProduct.ProductSubscription;
import org.example.Model.Product.EnumProduct.ProductCategory;
import org.example.Model.Product.EnumProduct.SubscriptionStatus;
import org.example.Model.Product.EnumProduct.SubscriptionType;
import org.example.Utils.MaConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductSubscriptionService implements InterfaceGlobal<ProductSubscription> {

    Connection cnx = MaConnexion.getInstance().getCnx();


    @Override
    public void Add(ProductSubscription p) {

        String req = "INSERT INTO ProductSubscription " +
                "(client, product, type, subscriptionDate, expirationDate, status) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = cnx.prepareStatement(req)) {

            ps.setInt(1, p.getClient());
            ps.setInt(2, p.getProduct());
            ps.setString(3, p.getType().name());   // ENUM safe
            ps.setTimestamp(4, Timestamp.valueOf(p.getSubscriptionDate()));
            ps.setTimestamp(5, Timestamp.valueOf(p.getExpirationDate()));
            ps.setString(6, p.getStatus().name()); // ENUM safe

            ps.executeUpdate();

            System.out.println("ProductSubscription ajouté avec succès!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void Update(ProductSubscription p) {
        String req = "UPDATE `ProductSubscription` SET type = ?, subscriptionDate=?, expirationDate = ?, status = ? WHERE subscriptionId = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, p.getType().name());
            ps.setTimestamp(2, Timestamp.valueOf(p.getSubscriptionDate()));
            ps.setTimestamp(3, Timestamp.valueOf(p.getExpirationDate()));
            ps.setString(4, p.getStatus().name());
            ps.setInt(5, p.getSubscriptionId());
            ps.executeUpdate();
            System.out.println("ProduitSubscription changée avec succes");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void Delete(Integer id) {
        String req = "DELETE FROM `ProductSubscription` WHERE subscriptionId = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("ProductSubscription Supprimer avec succes");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ProductSubscription> ReadAll() {
        List<ProductSubscription> products = new ArrayList<>();
        String req = "SELECT * FROM ProductSubscription";

        try (Statement st = cnx.createStatement();
             ResultSet res = st.executeQuery(req)) {

            while (res.next()) {
                ProductSubscription ps = new ProductSubscription();
                ps.setSubscriptionId(res.getInt("subscriptionId"));
                ps.setClient(res.getInt("client"));
                ps.setProduct(res.getInt("product"));
                ps.setType(SubscriptionType.valueOf(res.getString("type")));
                ps.setSubscriptionDate(res.getTimestamp("subscriptionDate").toLocalDateTime());
                ps.setExpirationDate(res.getTimestamp("expirationDate").toLocalDateTime());
                ps.setStatus(SubscriptionStatus.valueOf(res.getString("status")));
                products.add(ps);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return products;
    }

    @Override
    public ProductSubscription ReadId(Integer id) {
        String req = "SELECT * FROM ProductSubscription WHERE subscriptionId = ?";

        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, id);

            try (ResultSet res = ps.executeQuery()) {
                if (res.next()) {
                    ProductSubscription psObj = new ProductSubscription();
                    psObj.setSubscriptionId(res.getInt("subscriptionId"));
                    psObj.setClient(res.getInt("client"));
                    psObj.setProduct(res.getInt("product"));
                    psObj.setType(SubscriptionType.valueOf(res.getString("type")));
                    psObj.setSubscriptionDate(res.getTimestamp("subscriptionDate").toLocalDateTime());
                    psObj.setExpirationDate(res.getTimestamp("expirationDate").toLocalDateTime());
                    psObj.setStatus(SubscriptionStatus.valueOf(res.getString("status")));
                    return psObj;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<ProductSubscription> getParClient(int clientId) {
        List<ProductSubscription> products = new ArrayList<>();

        String req = "SELECT * FROM productsubscription WHERE client = ?";

        try (PreparedStatement p = cnx.prepareStatement(req)) {
            p.setInt(1, clientId);
            ResultSet res = p.executeQuery();
            while (res.next()) {
                ProductSubscription ps = new ProductSubscription();
                ps.setSubscriptionId(res.getInt("subscriptionId"));
                ps.setClient(res.getInt("client"));
                ps.setProduct(res.getInt("product"));
                ps.setType(SubscriptionType.valueOf(res.getString("type")));
                ps.setSubscriptionDate(
                        res.getDate("subscriptionDate").toLocalDate().atStartOfDay()
                );
                ps.setExpirationDate(
                        res.getDate("expirationDate").toLocalDate().atStartOfDay()
                );
                ps.setStatus(SubscriptionStatus.valueOf(res.getString("status")));

                products.add(ps);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return products;
    }







}
