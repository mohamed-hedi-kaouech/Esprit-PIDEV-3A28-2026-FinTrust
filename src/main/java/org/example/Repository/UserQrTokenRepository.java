package org.example.Repository;

import org.example.Utils.MaConnexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;

public class UserQrTokenRepository {

    private final Connection cnx;

    public UserQrTokenRepository() {
        this.cnx = MaConnexion.getInstance().getCnx();
        ensureTable();
    }

    public Optional<Integer> consumeActiveToken(String token) {
        String lookupSql = """
                SELECT user_id
                FROM user_qr_tokens
                WHERE token = ?
                  AND active = 1
                  AND expires_at > NOW()
                LIMIT 1
                """;
        try (PreparedStatement lookup = cnx.prepareStatement(lookupSql)) {
            lookup.setString(1, token);
            try (ResultSet rs = lookup.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                int userId = rs.getInt("user_id");
                deactivateToken(token);
                return Optional.of(userId);
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur verification token QR: " + e.getMessage(), e);
        }
    }

    private void deactivateToken(String token) {
        String sql = "UPDATE user_qr_tokens SET active = 0 WHERE token = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, token);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Erreur desactivation token QR: " + e.getMessage(), e);
        }
    }

    private void ensureTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS user_qr_tokens (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    user_id INT NOT NULL,
                    token VARCHAR(120) NOT NULL UNIQUE,
                    active TINYINT(1) NOT NULL DEFAULT 1,
                    expires_at DATETIME NOT NULL,
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    INDEX idx_qr_token_user (user_id),
                    INDEX idx_qr_token_active_exp (active, expires_at),
                    CONSTRAINT fk_qr_token_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                )
                """;
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.execute();
        } catch (Exception e) {
            throw new RuntimeException("Erreur creation table user_qr_tokens: " + e.getMessage(), e);
        }
    }
}