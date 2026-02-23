package org.example.Repository;

import org.example.Model.User.User;
import org.example.Model.User.UserRole;
import org.example.Model.User.UserStatus;
import org.example.Utils.MaConnexion;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepository {

    private final Connection cnx;

    public UserRepository() {
        this.cnx = MaConnexion.getInstance().getCnx();
    }

    public Optional<User> findByEmail(String email) {
        String sql = "SELECT id, nom, prenom, email, numTel, password, role, status, " +
                "COALESCE(created_at, createdAt) AS created_date " +
                "FROM users WHERE email = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche utilisateur", e);
        }
        return Optional.empty();
    }

    public Optional<User> findById(int id) {
        String sql = "SELECT id, nom, prenom, email, numTel, password, role, status, " +
                "COALESCE(created_at, createdAt) AS created_date " +
                "FROM users WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
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
        String sql = "INSERT INTO users (nom, prenom, email, numTel, password, role, status, createdAt, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getNom());
            ps.setString(2, user.getPrenom());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getNumTel());
            ps.setString(5, user.getPasswordHash());
            ps.setString(6, user.getRole().name());
            ps.setString(7, user.getStatus().name());
            ps.setTimestamp(8, Timestamp.valueOf(user.getCreatedAt()));
            ps.setTimestamp(9, Timestamp.valueOf(user.getCreatedAt()));
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) user.setId(keys.getInt(1));
            }
            return user;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la creation utilisateur", e);
        }
    }

    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, nom, prenom, email, numTel, password, role, status, " +
                "COALESCE(created_at, createdAt) AS created_date " +
                "FROM users ORDER BY COALESCE(created_at, createdAt) DESC";
        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) users.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du chargement des utilisateurs", e);
        }
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
            if (rs.next()) return rs.getLong(1);
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du comptage des admins", e);
        }
    }

    public void seedDefaultAdminIfMissing(String nom, String email, String passwordHash) {
        if (countAdmins() > 0) return;

        User admin = new User(nom, email, passwordHash, UserRole.ADMIN, UserStatus.ACCEPTE, LocalDateTime.now());
        save(admin);
    }

    // ✅ Profil client (nom/email/tel)
    public void updateProfile(int id, String nom, String email, String numTel) {
        String sql = "UPDATE users SET nom = ?, email = ?, numTel = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, nom);
            ps.setString(2, email);
            ps.setString(3, numTel);
            ps.setInt(4, id);

            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new RuntimeException("Aucun utilisateur mis à jour (ID introuvable).");
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

    // ✅ Update admin (nom/email/tel/status)
    public void updateByAdmin(int id, String nom, String email, String numTel, UserStatus status) {
        String sql = "UPDATE users SET nom = ?, email = ?, numTel = ?, status = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, nom);
            ps.setString(2, email);
            ps.setString(3, numTel);
            ps.setString(4, status.name());
            ps.setInt(5, id);

            int rows = ps.executeUpdate();
            if (rows == 0) throw new RuntimeException("Utilisateur introuvable (ID=" + id + ")");
        } catch (SQLException e) {
            throw new RuntimeException("Erreur update admin: " + e.getMessage(), e);
        }
    }

    // ✅ Delete user + suppression KYC avant
    public void deleteById(int userId) {
        try {
            cnx.setAutoCommit(false);

            // si tu as kyc_files liées à kyc_id
            try (PreparedStatement ps = cnx.prepareStatement(
                    "DELETE FROM kyc_files WHERE kyc_id IN (SELECT id FROM kyc WHERE user_id = ?)")) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            }

            // table kyc
            try (PreparedStatement ps = cnx.prepareStatement("DELETE FROM kyc WHERE user_id = ?")) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            }

            // enfin users
            try (PreparedStatement ps = cnx.prepareStatement("DELETE FROM users WHERE id = ?")) {
                ps.setInt(1, userId);
                int rows = ps.executeUpdate();
                if (rows == 0) throw new RuntimeException("Utilisateur introuvable (ID=" + userId + ")");
            }

            cnx.commit();
        } catch (Exception e) {
            try { cnx.rollback(); } catch (SQLException ignored) {}
            throw new RuntimeException("Erreur suppression utilisateur: " + e.getMessage(), e);
        } finally {
            try { cnx.setAutoCommit(true); } catch (SQLException ignored) {}
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setNom(rs.getString("nom"));
        user.setPrenom(rs.getString("prenom"));
        user.setEmail(rs.getString("email"));
        user.setNumTel(rs.getString("numTel"));
        user.setPasswordHash(rs.getString("password"));
        user.setRole(UserRole.valueOf(rs.getString("role")));
        user.setStatus(UserStatus.valueOf(rs.getString("status")));
        Timestamp createdAt = rs.getTimestamp("created_date");
        user.setCreatedAt(createdAt == null ? LocalDateTime.now() : createdAt.toLocalDateTime());
        return user;
    }
}
