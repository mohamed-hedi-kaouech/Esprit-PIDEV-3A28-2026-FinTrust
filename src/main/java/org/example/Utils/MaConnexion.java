package org.example.Utils;

import org.example.Model.Product.ClassProduct.Product;
import org.example.Model.Product.ClassProduct.ProductSubscription;
import org.example.Model.Wallet.ClassWallet.Transaction;
import org.example.Model.Wallet.ClassWallet.Wallet;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class MaConnexion {
    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "";
    private static final String[] COMMON_PORTS = {"3307", "3306"};
    private static final String[] COMMON_DATABASES = {"PIDEV1", "PIDEV", "pidev1", "pidev"};
    private static final boolean DEFAULT_AUTO_SCHEMA_INIT = false;

    private Connection cnx;
    private static MaConnexion instance;

    private MaConnexion() {
        try {
            loadMysqlDriver();
            cnx = openConnection();
            System.out.println("Connexion Etablie avec succes!");
            if (isSchemaAutoInitEnabled()) {
                loadDatabase();
            } else {
                System.out.println("Schema auto-init desactive. Aucune table ne sera creee ou modifiee automatiquement.");
            }
        } catch (SQLException e) {
            throw new RuntimeException(buildDatabaseErrorMessage(e), e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(
                    "MySQL driver introuvable dans le classpath. Verifiez la dependance Maven mysql connector.",
                    e
            );
        }
    }

    public Connection getCnx() {
        return cnx;
    }

    public static MaConnexion getInstance() {
        if (instance == null) {
            instance = new MaConnexion();
        }
        return instance;
    }

    public static String getDatabaseLabel() {
        return buildConfiguredLabel();
    }

    public void loadDatabase() {
        try {
            if (cnx == null || cnx.isClosed()) {
                loadMysqlDriver();
                cnx = openConnection();
            }

            try (Statement st = cnx.createStatement()) {
                st.executeUpdate(Product.SQLTable());
                st.executeUpdate(ProductSubscription.SQLTable());
                st.executeUpdate(Wallet.getSQLCreateTable());
                st.executeUpdate(Transaction.getSQLCreateTable());
                System.out.println("Tables checked/created successfully.");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la creation des tables:");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(
                    "MySQL driver introuvable dans le classpath. Verifiez la dependance Maven mysql connector.",
                    e
            );
        }
    }

    private void loadMysqlDriver() throws ClassNotFoundException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException firstError) {
            Class.forName("com.mysql.jdbc.Driver");
        }
    }

    private Connection openConnection() throws SQLException {
        String configuredHost = readConfig("FINTRUST_DB_HOST", DEFAULT_HOST);
        String configuredPort = readConfig("FINTRUST_DB_PORT", "3307");
        String configuredDatabase = readConfig("FINTRUST_DB_NAME", "PIDEV1");
        String configuredUser = readConfig("FINTRUST_DB_USER", DEFAULT_USER);
        String configuredPassword = readConfig("FINTRUST_DB_PASSWORD", DEFAULT_PASSWORD);

        SQLException lastException = null;
        for (DbTarget target : buildTargets(configuredHost, configuredPort, configuredDatabase)) {
            try {
                return DriverManager.getConnection(
                        buildJdbcUrl(target.host, target.port, target.database),
                        configuredUser,
                        configuredPassword
                );
            } catch (SQLException e) {
                lastException = e;
            }
        }

        throw lastException == null ? new SQLException("Impossible de contacter MySQL.") : lastException;
    }

    private List<DbTarget> buildTargets(String configuredHost, String configuredPort, String configuredDatabase) {
        Set<String> orderedTargets = new LinkedHashSet<>();
        orderedTargets.add(configuredHost + "|" + configuredPort + "|" + configuredDatabase);

        for (String port : COMMON_PORTS) {
            orderedTargets.add(configuredHost + "|" + port + "|" + configuredDatabase);
        }
        for (String database : COMMON_DATABASES) {
            orderedTargets.add(configuredHost + "|" + configuredPort + "|" + database);
        }
        for (String port : COMMON_PORTS) {
            for (String database : COMMON_DATABASES) {
                orderedTargets.add(configuredHost + "|" + port + "|" + database);
            }
        }

        List<DbTarget> targets = new ArrayList<>();
        for (String value : orderedTargets) {
            String[] parts = value.split("\\|", 3);
            targets.add(new DbTarget(parts[0], parts[1], parts[2]));
        }
        return targets;
    }

    private String buildJdbcUrl(String host, String port, String database) {
        return "jdbc:mysql://" + host + ":" + port + "/" + database
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    }

    private String buildDatabaseErrorMessage(SQLException e) {
        String baseMessage = "Base de donnees indisponible. Verifiez MySQL (" + buildConfiguredLabel() + ").";
        if (e.getMessage() == null) {
            return baseMessage;
        }
        if (e.getMessage().contains("Communications link failure")
                || e.getMessage().contains("Connection refused")) {
            return baseMessage + " Ports testes: 3307 et 3306.";
        }
        return baseMessage + " Details: " + e.getMessage();
    }

    private static String buildConfiguredLabel() {
        String host = readConfig("FINTRUST_DB_HOST", DEFAULT_HOST);
        String port = readConfig("FINTRUST_DB_PORT", "3307");
        String database = readConfig("FINTRUST_DB_NAME", "PIDEV1");
        return host + ":" + port + " / " + database;
    }

    private static String readConfig(String key, String fallback) {
        String value = SecretConfig.get(key);
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    public static boolean isSchemaAutoInitEnabled() {
        String value = SecretConfig.get("FINTRUST_DB_AUTO_INIT");
        if (value == null || value.isBlank()) {
            return DEFAULT_AUTO_SCHEMA_INIT;
        }
        return Boolean.parseBoolean(value.trim());
    }

    private static final class DbTarget {
        private final String host;
        private final String port;
        private final String database;

        private DbTarget(String host, String port, String database) {
            this.host = host;
            this.port = port;
            this.database = database;
        }
    }
}
