package org.example.Utils;

import org.example.Model.Product.ClassProduct.ProductSubscription;
import org.example.Model.Product.ClassProduct.Product;
import org.example.Model.Wallet.ClassWallet.Transaction;
import org.example.Model.Wallet.ClassWallet.Wallet;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class MaConnexion {
    //DB
    final String URL = "jdbc:mysql://localhost:3307/PIDEV";
    final String USR = "root";
    final String PWD = "";

    //var
    Connection cnx;
    static MaConnexion instance;

    //Constructeur
    private MaConnexion(){
        try {
            cnx = DriverManager.getConnection(URL, USR, PWD);
            System.out.println("Connexion Etablie avec succes!");
            loadDatabase();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Connection getCnx() {
        return cnx;
    }

    public static MaConnexion getInstance() {
        if(instance == null)
            instance = new MaConnexion();
        return instance;
    }

    public void loadDatabase() {
        try {
            // Vérifier si la connexion est fermée et la rouvrir si nécessaire
            if (cnx == null || cnx.isClosed()) {
                cnx = DriverManager.getConnection(URL, USR, PWD);
            }

            try (Statement st = cnx.createStatement()) {
                // PRODUCT TABLE
                st.executeUpdate(Product.SQLTable());

                // PRODUCT SUBSCRIPTION TABLE
                st.executeUpdate(ProductSubscription.SQLTable());

                // WALLET TABLE (créer d'abord wallet car transaction dépend de wallet)
                st.executeUpdate(Wallet.getSQLCreateTable());  // CORRIGÉ: getSQLCreateTable() au lieu de SQLTable()

                // TRANSACTION TABLE
                st.executeUpdate(Transaction.getSQLCreateTable());  // CORRIGÉ: getSQLCreateTable() au lieu de SQLTable()

                System.out.println("Tables checked/created successfully.");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la création des tables:");
            e.printStackTrace();
        }
    }
}
