package org.example.Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class MaConnexion {

    // 🔹 Paramètres de connexion
    private final String URL = "jdbc:mysql://localhost:3306/PIDEV";
    private final String USER = "root";
    private final String PASSWORD = "";

    // 🔹 Instance unique (Singleton)
    private static MaConnexion instance;

    // 🔹 Objet Connection
    private Connection cnx;

    // 🔹 Constructeur privé (empêche new MaConnexion())
    private MaConnexion() {

        try {
            // Charger le driver MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Créer la connexion
            cnx = DriverManager.getConnection(URL, USER, PASSWORD);

            System.out.println("✅ Connexion établie avec succès !");

        } catch (ClassNotFoundException e) {
            System.err.println("❌ Driver MySQL introuvable : " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("❌ Erreur de connexion : " + e.getMessage());
        }
    }

    // 🔹 Méthode pour obtenir l'instance unique
    public static MaConnexion getInstance() {
        if (instance == null) {
            instance = new MaConnexion();
        }
        return instance;
    }

    // 🔹 Getter de la connexion
    public Connection getCnx() {
        return cnx;
    }


    // 🔥 Méthode pour tester la base de données
    public boolean loadDatabase() {

        if (cnx == null) {
            throw new RuntimeException("❌ Connexion non initialisée !");
        }

        try (Statement stmt = cnx.createStatement()) {

            // Petite requête simple pour tester la connexion
            stmt.execute("SELECT 1");

            System.out.println("✅ Base de données accessible.");
            return true;

        } catch (SQLException e) {
            throw new RuntimeException("❌ Erreur lors du test de la base : " + e.getMessage());
        }
    }

}
