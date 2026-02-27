package org.example.Service.ProductService;
import org.example.Interfaces.InterfaceGlobal;
import org.example.Model.Product.ClassProduct.Product;
import org.example.Model.Product.EnumProduct.ProductCategory;
import org.example.Utils.MaConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductService implements InterfaceGlobal<Product> {

    Connection cnx = MaConnexion.getInstance().getCnx();
    @Override
    public void Add(Product p) {

        String req = "INSERT INTO product (category, price, description, createdAt) " +
                "VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = cnx.prepareStatement(req)) {

            ps.setString(1, p.getCategory().name());
            ps.setDouble(2, p.getPrice());
            ps.setString(3, p.getDescription());
            ps.setTimestamp(4, Timestamp.valueOf(p.getCreatedAt()));

            ps.executeUpdate();

            System.out.println("Produit ajouté avec succès!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public boolean add(Product p) {
        String req = "INSERT INTO `product`(`category`, `price`, `description`, `createdAt`)" +
                " VALUES (?,?,?,?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, p.getCategory().name());
            ps.setDouble(2, p.getPrice());
            ps.setString(3, p.getDescription());
            ps.setTimestamp(4, Timestamp.valueOf(p.getCreatedAt()));
            ps.executeUpdate();
            System.out.println("Produit ajoutée avec succes 2");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

    }


    @Override
    public void Delete(Integer id) {
        String req = "DELETE FROM `product` WHERE productId = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Produit Supprimer avec succes");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean delete(Integer id) {
        String req = "DELETE FROM `product` WHERE productId = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Produit Supprimer avec succes");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void Update(Product p) {
        String req = "UPDATE `product` SET category = ?, price=?, description = ?, createdAt = ? WHERE productId = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, p.getCategory().name());
            ps.setDouble(2, p.getPrice());
            ps.setString(3, p.getDescription());
            ps.setTimestamp(4, Timestamp.valueOf(p.getCreatedAt()));
            ps.setInt(5, p.getProductId());
            ps.executeUpdate();
            System.out.println("Produit modifier avec succes 2");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public boolean update(Product p) {
        String req = "UPDATE `product` SET category = ?, price=?, description = ?, createdAt = ? WHERE productId = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, p.getCategory().name());
            ps.setDouble(2, p.getPrice());
            ps.setString(3, p.getDescription());
            ps.setTimestamp(4, Timestamp.valueOf(p.getCreatedAt()));
            ps.setInt(5, p.getProductId());
            ps.executeUpdate();
            System.out.println("Produit modifier avec succes ");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    @Override
    public List<Product> ReadAll() {
        List<Product> products = new ArrayList<>();
        String req = "SELECT * FROM `product`";
        try {
            Statement st = cnx.createStatement();
            ResultSet res = st.executeQuery(req);
            while (res.next()){
                Product p =new Product();
                p.setProductId(res.getInt(1));
                p.setCategory(ProductCategory.valueOf(res.getString(2)));
                p.setPrice(res.getDouble(3));
                p.setDescription(res.getString(4));
                p.setCreatedAt(res.getTimestamp(5).toLocalDateTime());
                products.add(p);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return products;
    }


    @Override
    public Product ReadId(Integer id) {

        String req = "SELECT * FROM product WHERE productId = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, id);
            try (ResultSet res = ps.executeQuery()) {

                if (res.next()) {
                    Product p = new Product();
                    p.setProductId(res.getInt("productId"));
                    p.setCategory(ProductCategory.valueOf(res.getString("category")));
                    p.setPrice(res.getDouble("price"));
                    p.setDescription(res.getString("description"));
                    p.setCreatedAt(res.getTimestamp("createdAt").toLocalDateTime());
                    return p;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


}