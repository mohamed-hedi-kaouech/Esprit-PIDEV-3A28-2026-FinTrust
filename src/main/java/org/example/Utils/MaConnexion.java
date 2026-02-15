package org.example.Utils;

import org.example.Model.Product.ClassProduct.ProductSubscription;
import org.example.Model.Product.ClassProduct.Product;
import org.example.Model.Wallet.Transaction;
import org.example.Model.Wallet.Wallet;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class MaConnexion {
    //DB
    final String URL = "jdbc:mysql://localhost:3306/PIDEV";
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
            cnx = DriverManager.getConnection(URL, USR, PWD);
            try (Statement st = cnx.createStatement()) {

                //PRODUCT TABLE
                st.executeUpdate(Product.SQLTable());

                //PRODUCT SUBSCRIPTION TABLE
                st.executeUpdate(ProductSubscription.SQLTable());

                //Transaction TABLE
                st.executeUpdate(Transaction.SQLTable());

                //Wallet TABLE
                st.executeUpdate(Wallet.SQLTable());

                System.out.println("Tables checked/created successfully.");

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
