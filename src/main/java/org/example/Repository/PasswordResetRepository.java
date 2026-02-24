package org.example.Repository;

import org.example.Utils.MaConnexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

public class PasswordResetRepository {

    private final Connection cnx;

    public PasswordResetRepository() {
        this.cnx = MaConnexion.getInstance().getCnx();
        ensureTable();
    }

    public void invalidateActiveByUserId(int userId) {
        String sql = "UPDATE password_reset SET used_at = NOW() WHERE user_id = ? AND used_at IS NULL";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Erreur invalidation password_reset: " + e.getMessage(), e);
        }
    }

    public void createResetCode(int userId, String codeHash, LocalDateTime expiresAt) {
        String sql = "INSERT INTO password_reset(user_id, code_hash, expires_at) VALUES (?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, codeHash);
            ps.setTimestamp(3, Timestamp.valueOf(expiresAt));
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Erreur insertion password_reset: " + e.getMessage(), e);
        }
    }

    public Optional<ResetCodeRow> findLatestActiveByUserId(int userId) {
        String sql = """
                SELECT id, code_hash, expires_at, attempts
                FROM password_reset
                WHERE user_id = ? AND used_at IS NULL
                ORDER BY created_at DESC, id DESC
                LIMIT 1
                """;
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(new ResetCodeRow(
                        rs.getInt("id"),
                        rs.getString("code_hash"),
                        rs.getTimestamp("expires_at").toLocalDateTime(),
                        rs.getInt("attempts")
                ));
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur lecture password_reset: " + e.getMessage(), e);
        }
    }

    public void incrementAttempts(int resetId) {
        String sql = "UPDATE password_reset SET attempts = attempts + 1 WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, resetId);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Erreur increment attempts password_reset: " + e.getMessage(), e);
        }
    }

    public void markUsed(int resetId) {
        String sql = "UPDATE password_reset SET used_at = NOW() WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, resetId);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Erreur update used_at password_reset: " + e.getMessage(), e);
        }
    }

    private void ensureTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS password_reset (
                    id INT NOT NULL AUTO_INCREMENT,
                    user_id INT NOT NULL,
                    code_hash VARCHAR(255) NOT NULL,
                    expires_at DATETIME NOT NULL,
                    used_at DATETIME DEFAULT NULL,
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    attempts INT NOT NULL DEFAULT 0,
                    PRIMARY KEY (id),
                    INDEX idx_password_reset_user_created (user_id, created_at),
                    INDEX idx_password_reset_expires (expires_at),
                    CONSTRAINT fk_password_reset_user
                        FOREIGN KEY (user_id) REFERENCES users(id)
                        ON DELETE CASCADE
                        ON UPDATE CASCADE
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """;
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.execute();
        } catch (Exception e) {
            throw new RuntimeException("Erreur creation table password_reset: " + e.getMessage(), e);
        }
    }

    public record ResetCodeRow(int id, String codeHash, LocalDateTime expiresAt, int attempts) {
    }
}

