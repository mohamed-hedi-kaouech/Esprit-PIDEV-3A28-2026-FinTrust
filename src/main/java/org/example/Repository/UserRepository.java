package org.example.Repository;

import org.example.Model.User.User;
import org.example.Model.User.UserRole;
import org.example.Model.User.UserStatus;
import org.example.Utils.MaConnexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
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
        String sql = "SELECT id, nom, prenom, email, numTel, password, role, status, COALESCE(created_at, createdAt) AS created_date FROM users WHERE email = ?";
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

    public User save(User user) {
        String sql = "INSERT INTO users (nom, prenom, email, numTel, password, role, status, createdAt, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
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
                if (keys.next()) {
                    user.setId(keys.getInt(1));
                }
            }
            return user;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la creation utilisateur", e);
        }
    }

    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, nom, prenom, email, numTel, password, role, status, COALESCE(created_at, createdAt) AS created_date FROM users ORDER BY COALESCE(created_at, createdAt) DESC";
        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(mapRow(rs));
            }
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
