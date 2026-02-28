package org.example.Utils;

import org.example.Model.Budget.Categorie;
import org.example.Model.Budget.Item;
import org.example.Model.Budget.Alerte;
import org.example.Model.Budget.Categorie;
import org.example.Model.Product.ClassProduct.ProductSubscription;
import org.example.Model.Product.ClassProduct.Product;
import org.example.Model.Wallet.Transaction;
import org.example.Model.Wallet.Wallet;
import org.example.Model.Loan.LoanClass.Loan;
import org.example.Model.Loan.LoanClass.Repayment;
import org.example.Model.Kyc.Kyc;
import org.example.Model.Kyc.KycFile;
import org.example.Model.User.User;

// ✅ IMPORTS MANQUANTS (corrige ton erreur)



import org.mindrot.jbcrypt.BCrypt;



import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.*;

public class MaConnexion {

    // DB
    private static final String URL = "jdbc:mysql://localhost:3306/PIDEV";
    private static final String USR = "root";
    private static final String PWD = "";

    private Connection cnx;
    private static MaConnexion instance;

    private MaConnexion() {
        try {
            cnx = DriverManager.getConnection(URL, USR, PWD);
            System.out.println("Connexion Etablie avec succes!");
            loadDatabase();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur connexion DB: " + e.getMessage(), e);
        }
    }

    public Connection getCnx() {
        return cnx;
    }

    public static MaConnexion getInstance() {
        if (instance == null) instance = new MaConnexion();
        return instance;
    }

    // ✅ pour ton UserRepository.updateProfile() qui appelle getConnection()
    public Connection getConnection() {
        return cnx;
    }

    public void loadDatabase() {

        try {
            cnx = DriverManager.getConnection(URL, USR, PWD);
            try (Statement st = cnx.createStatement()) {
                //Users table
                st.executeUpdate(User.SQLTable());
                reconcileUsersTable();

                // TABLES
                st.executeUpdate(Product.SQLTable());

                //PRODUCT SUBSCRIPTION TABLE
                st.executeUpdate(ProductSubscription.SQLTable());

                //Categorie TABLE
                st.executeUpdate(Categorie.SQLTable());
                st.executeUpdate(Item.SQLTable());
                st.executeUpdate(Alerte.SQLTable());


                //Wallet TABLE
                st.executeUpdate(Wallet.SQLTable());
                //Transaction TABLE
                st.executeUpdate(Transaction.SQLTable());

                //Loan Table
                st.executeUpdate(Loan.SQLTable());

                //Repayment Table
                st.executeUpdate(Repayment.SQLTable());


                //KYC tables
                st.executeUpdate(Kyc.SQLTable());
                reconcileKycTable();
                st.executeUpdate(KycFile.SQLTable());

                seedDefaultAdmin();

                System.out.println("Tables checked/created successfully.");

            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur loadDatabase: " + e.getMessage(), e);
        }
    }

    private void seedDefaultAdmin() throws SQLException {
        String countSql = "SELECT COUNT(*) FROM users WHERE role = 'ADMIN'";
        try (PreparedStatement countPs = cnx.prepareStatement(countSql);
             ResultSet rs = countPs.executeQuery()) {
            if (rs.next() && rs.getLong(1) > 0) return;
        }

        String tempPassword = "Admin1234";
        String hash = BCrypt.hashpw(tempPassword, BCrypt.gensalt(12));

        String insertSql = "INSERT INTO users (nom, prenom, email, password, role, status, createdAt, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, NOW(), NOW())";

        try (PreparedStatement insertPs = cnx.prepareStatement(insertSql)) {
            insertPs.setString(1, "Administrateur");
            insertPs.setString(2, "");
            insertPs.setString(3, "admin@pidev.local");
            insertPs.setString(4, hash);
            insertPs.setString(5, "ADMIN");
            insertPs.setString(6, "ACCEPTE");
            insertPs.executeUpdate();
        }

        System.out.println("Admin seed created: admin@pidev.local / Admin1234");
    }

    private void reconcileUsersTable() throws SQLException {
        try (Statement st = cnx.createStatement()) {
            if (!hasColumn("users", "status")) {
                st.executeUpdate("ALTER TABLE users ADD COLUMN status ENUM('EN_ATTENTE','ACCEPTE','REFUSE') NOT NULL DEFAULT 'EN_ATTENTE'");
            }
            if (!hasColumn("users", "created_at")) {
                st.executeUpdate("ALTER TABLE users ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP");
            }
            if (!hasColumn("users", "prenom")) {
                st.executeUpdate("ALTER TABLE users ADD COLUMN prenom VARCHAR(120) NOT NULL DEFAULT ''");
            }
            if (!hasColumn("users", "numTel")) {
                st.executeUpdate("ALTER TABLE users ADD COLUMN numTel VARCHAR(20) DEFAULT NULL");
            }
            if (!hasColumn("users", "createdAt")) {
                st.executeUpdate("ALTER TABLE users ADD COLUMN createdAt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP");
            }
            st.executeUpdate("UPDATE users SET role='CLIENT' WHERE UPPER(role) NOT IN ('ADMIN','CLIENT')");
        }
    }

    private void reconcileKycTable() throws SQLException {
        try (Statement st = cnx.createStatement()) {
            if (!hasColumn("kyc", "cin")) {
                st.executeUpdate("ALTER TABLE kyc ADD COLUMN cin VARCHAR(20) NULL");
            }
            if (!hasColumn("kyc", "adresse")) {
                st.executeUpdate("ALTER TABLE kyc ADD COLUMN adresse VARCHAR(255) NULL");
            }
            if (!hasColumn("kyc", "date_naissance")) {
                st.executeUpdate("ALTER TABLE kyc ADD COLUMN date_naissance DATE NULL");
            }
            if (!hasColumn("kyc", "signature_path")) {
                st.executeUpdate("ALTER TABLE kyc ADD COLUMN signature_path VARCHAR(255) NULL");
            }
            if (!hasColumn("kyc", "signature_uploaded_at")) {
                st.executeUpdate("ALTER TABLE kyc ADD COLUMN signature_uploaded_at DATETIME NULL");
            }

            st.executeUpdate("UPDATE kyc SET cin = CONCAT('TMP-KYC-', user_id) WHERE cin IS NULL OR TRIM(cin) = ''");
            st.executeUpdate("UPDATE kyc SET adresse = 'Adresse non renseignee' WHERE adresse IS NULL OR TRIM(adresse) = ''");
            st.executeUpdate("UPDATE kyc SET date_naissance = '1970-01-01' WHERE date_naissance IS NULL");

            st.executeUpdate("ALTER TABLE kyc MODIFY COLUMN cin VARCHAR(20) NOT NULL");
            st.executeUpdate("ALTER TABLE kyc MODIFY COLUMN adresse VARCHAR(255) NOT NULL");
            st.executeUpdate("ALTER TABLE kyc MODIFY COLUMN date_naissance DATE NOT NULL");

            if (!hasIndex("kyc", "ux_kyc_cin")) {
                st.executeUpdate("ALTER TABLE kyc ADD UNIQUE KEY ux_kyc_cin (cin)");
            }
        }
    }

    private boolean hasColumn(String tableName, String columnName) throws SQLException {
        String sql = """
                SELECT COUNT(*) FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?
                """;
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, tableName);
            ps.setString(2, columnName);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    private boolean hasIndex(String tableName, String indexName) throws SQLException {
        String sql = """
                SELECT COUNT(*) FROM information_schema.STATISTICS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND INDEX_NAME = ?
                """;
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, tableName);
            ps.setString(2, indexName);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }
}
