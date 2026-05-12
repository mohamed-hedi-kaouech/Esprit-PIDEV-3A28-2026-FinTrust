package org.example.Repository;

import org.example.Model.User.User;
import org.example.Model.User.UserRole;
import org.example.Model.User.UserStatus;
import org.example.Utils.MaConnexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class UserRepository {

    private final Connection cnx;

    public UserRepository() {
        this.cnx = MaConnexion.getInstance().getCnx();
    }

    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche utilisateur", e);
        }
        return Optional.empty();
    }

    public Optional<User> findById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche utilisateur par id", e);
        }
        return Optional.empty();
    }

    public boolean existsByEmail(String email) {
        String sql = "SELECT 1 FROM users WHERE email = ? LIMIT 1";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la verification email", e);
        }
    }

    public boolean existsByEmailExceptUserId(String email, int userId) {
        String sql = "SELECT 1 FROM users WHERE email = ? AND id <> ? LIMIT 1";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la verification email", e);
        }
    }

    public User save(User user) {
        LocalDateTime createdAt = user.getCreatedAt() != null ? user.getCreatedAt() : LocalDateTime.now();
        user.setCreatedAt(createdAt);

        Set<String> columns = loadTableColumns("users");
        List<String> insertColumns = new ArrayList<>();
        List<Object> values = new ArrayList<>();

        addInsertValue(columns, insertColumns, values, user.getNom(), "nom");
        addInsertValue(columns, insertColumns, values, user.getPrenom(), "prenom");
        addInsertValue(columns, insertColumns, values, user.getEmail(), "email");
        addInsertValue(columns, insertColumns, values, user.getNumTel(), "numTel", "num_tel", "telephone", "phone");
        addInsertValue(columns, insertColumns, values, user.getPasswordHash(), "password", "password_hash", "mot_de_passe");
        addInsertValue(columns, insertColumns, values, user.getRole() == null ? null : user.getRole().name(), "role");
        addInsertValue(columns, insertColumns, values, user.getStatus() == null ? null : user.getStatus().name(), "status");
        addInsertValue(columns, insertColumns, values, Timestamp.valueOf(createdAt), "createdAt", "created_at", "created_date");
        addInsertValue(columns, insertColumns, values, Timestamp.valueOf(LocalDateTime.now()), "updatedAt", "updated_at", "updated_date");

        if (insertColumns.isEmpty()) {
            throw new RuntimeException("Impossible de creer l'utilisateur: aucune colonne compatible detectee dans la table users.");
        }

        String placeholders = String.join(", ", java.util.Collections.nCopies(insertColumns.size(), "?"));
        String sql = "INSERT INTO users (" + String.join(", ", insertColumns) + ") VALUES (" + placeholders + ")";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < values.size(); i++) {
                ps.setObject(i + 1, values.get(i));
            }
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    user.setId(keys.getInt(1));
                }
            }
            return user;
        } catch (SQLException e) {
            String detail = e.getMessage() == null ? "" : e.getMessage();
            if (detail.toLowerCase().contains("duplicate")) {
                throw new RuntimeException("Email deja existant: " + user.getEmail(), e);
            }
            throw new RuntimeException(
                    "Erreur SQL creation utilisateur [state=" + e.getSQLState()
                            + ", code=" + e.getErrorCode()
                            + "]: " + detail,
                    e
            );
        }
    }

    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du chargement des utilisateurs", e);
        }
        users.sort((a, b) -> {
            LocalDateTime left = a.getCreatedAt() != null ? a.getCreatedAt() : LocalDateTime.MIN;
            LocalDateTime right = b.getCreatedAt() != null ? b.getCreatedAt() : LocalDateTime.MIN;
            return right.compareTo(left);
        });
        return users;
    }

    public void updateStatus(int userId, UserStatus status) {
        String sql = "UPDATE users SET status = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise a jour du statut", e);
        }
    }

    public long countAdmins() {
        String sql = "SELECT COUNT(*) FROM users WHERE role = 'ADMIN'";
        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du comptage des admins", e);
        }
    }

    public void seedDefaultAdminIfMissing(String nom, String email, String passwordHash) {
        if (countAdmins() > 0) {
            return;
        }

        User admin = new User(nom, email, passwordHash, UserRole.ADMIN, UserStatus.ACCEPTE, LocalDateTime.now());
        save(admin);
    }

    public void updateProfile(int id, String nom, String email, String numTel) {
        String sql = "UPDATE users SET nom = ?, email = ?, numTel = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, nom);
            ps.setString(2, email);
            ps.setString(3, numTel);
            ps.setInt(4, id);

            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new RuntimeException("Aucun utilisateur mis a jour (ID introuvable).");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur update profile: " + e.getMessage(), e);
        }
    }

    public void updatePassword(int id, String passwordHash) {
        String sql = "UPDATE users SET password = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, passwordHash);
            ps.setInt(2, id);

            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new RuntimeException("Aucun utilisateur mis a jour (ID introuvable).");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur update password: " + e.getMessage(), e);
        }
    }

    public void updateByAdmin(int id, String nom, String email, String numTel, UserStatus status) {
        String sql = "UPDATE users SET nom = ?, email = ?, numTel = ?, status = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, nom);
            ps.setString(2, email);
            ps.setString(3, numTel);
            ps.setString(4, status.name());
            ps.setInt(5, id);

            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new RuntimeException("Utilisateur introuvable (ID=" + id + ")");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur update admin: " + e.getMessage(), e);
        }
    }

    public void deleteById(int userId) {
        try {
            cnx.setAutoCommit(false);

            try (PreparedStatement ps = cnx.prepareStatement("DELETE FROM kyc_files WHERE kyc_id IN (SELECT id FROM kyc WHERE user_id = ?)")) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = cnx.prepareStatement("DELETE FROM kyc WHERE user_id = ?")) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = cnx.prepareStatement("DELETE FROM users WHERE id = ?")) {
                ps.setInt(1, userId);
                int rows = ps.executeUpdate();
                if (rows == 0) {
                    throw new RuntimeException("Utilisateur introuvable (ID=" + userId + ")");
                }
            }

            cnx.commit();
        } catch (Exception e) {
            try {
                cnx.rollback();
            } catch (SQLException ignored) {
            }
            throw new RuntimeException("Erreur suppression utilisateur: " + e.getMessage(), e);
        } finally {
            try {
                cnx.setAutoCommit(true);
            } catch (SQLException ignored) {
            }
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt(firstAvailableColumn(rs, "id")));
        user.setNom(readString(rs, "nom"));
        user.setPrenom(readString(rs, "prenom"));
        user.setEmail(readString(rs, "email"));
        user.setNumTel(readString(rs, "numTel", "num_tel", "telephone", "phone"));
        user.setPasswordHash(readString(rs, "password", "password_hash", "mot_de_passe"));

        String roleValue = readString(rs, "role");
        if (roleValue != null && !roleValue.isBlank()) {
            user.setRole(parseRole(roleValue));
        }

        String statusValue = readString(rs, "status");
        if (statusValue != null && !statusValue.isBlank()) {
            user.setStatus(parseStatus(statusValue));
        }

        Timestamp createdAt = readTimestamp(rs, "created_date", "created_at", "createdAt");
        user.setCreatedAt(createdAt == null ? LocalDateTime.now() : createdAt.toLocalDateTime());
        return user;
    }

    private Set<String> loadTableColumns(String tableName) {
        String sql = "SELECT * FROM " + tableName + " WHERE 1 = 0";
        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            ResultSetMetaData metaData = rs.getMetaData();
            Set<String> columns = new LinkedHashSet<>();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                String label = metaData.getColumnLabel(i);
                String name = metaData.getColumnName(i);
                if (label != null && !label.isBlank()) {
                    columns.add(label);
                }
                if (name != null && !name.isBlank()) {
                    columns.add(name);
                }
            }
            return columns;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la lecture du schema users: " + e.getMessage(), e);
        }
    }

    private void addInsertValue(Set<String> availableColumns,
                                List<String> insertColumns,
                                List<Object> values,
                                Object value,
                                String... candidates) {
        String column = firstMatchingColumn(availableColumns, candidates);
        if (column == null) {
            return;
        }
        insertColumns.add(column);
        values.add(value);
    }

    private String firstMatchingColumn(Set<String> availableColumns, String... candidates) {
        for (String candidate : candidates) {
            for (String available : availableColumns) {
                if (candidate.equalsIgnoreCase(available)) {
                    return available;
                }
            }
        }
        return null;
    }

    private String readString(ResultSet rs, String... candidates) throws SQLException {
        String column = firstAvailableColumn(rs, candidates);
        return column == null ? null : rs.getString(column);
    }

    private Timestamp readTimestamp(ResultSet rs, String... candidates) throws SQLException {
        String column = firstAvailableColumn(rs, candidates);
        return column == null ? null : rs.getTimestamp(column);
    }

    private String firstAvailableColumn(ResultSet rs, String... candidates) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        for (String candidate : candidates) {
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                String label = metaData.getColumnLabel(i);
                String name = metaData.getColumnName(i);
                if (candidate.equalsIgnoreCase(label) || candidate.equalsIgnoreCase(name)) {
                    return label != null && !label.isBlank() ? label : name;
                }
            }
        }
        return null;
    }

    private UserRole parseRole(String rawValue) {
        String value = rawValue == null ? "" : rawValue.trim().toUpperCase();
        return switch (value) {
            case "ADMIN", "ADMINISTRATEUR" -> UserRole.ADMIN;
            case "CLIENT", "USER", "UTILISATEUR" -> UserRole.CLIENT;
            default -> UserRole.valueOf(value);
        };
    }

    public static UserStatus normalizeStatusValue(String rawValue) {
        String value = rawValue == null ? "" : rawValue.trim().toUpperCase();
        return switch (value) {
            case "ACTIF", "ACTIVE", "ACCEPTE", "APPROUVE", "VALIDE" -> UserStatus.ACCEPTE;
            case "EN_ATTENTE", "PENDING", "ATTENTE", "EN COURS", "EN_COURS" -> UserStatus.EN_ATTENTE;
            case "REFUSE", "REFUSED", "REJETE", "INACTIF", "INACTIVE", "BLOQUE", "BLOQUEE", "SUSPENDU", "SUSPENDUE", "SUSPENDED" -> UserStatus.REFUSE;
            default -> UserStatus.valueOf(value);
        };
    }

    private UserStatus parseStatus(String rawValue) {
        return normalizeStatusValue(rawValue);
    }
}
