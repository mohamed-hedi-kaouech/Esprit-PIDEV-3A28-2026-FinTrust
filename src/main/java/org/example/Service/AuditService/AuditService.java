package org.example.Service.AuditService;

import org.example.Utils.MaConnexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AuditService {

    private final Connection cnx;

    public AuditService() {
        this.cnx = MaConnexion.getInstance().getCnx();
        ensureTables();
    }

    public void logLoginAttempt(Integer userId, String email, boolean success, String reason) {
        String sql = "INSERT INTO user_login_audit(user_id,email,success,reason,created_at) VALUES(?,?,?,?,NOW())";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            if (userId == null) {
                ps.setNull(1, java.sql.Types.INTEGER);
            } else {
                ps.setInt(1, userId);
            }
            ps.setString(2, safe(email));
            ps.setBoolean(3, success);
            ps.setString(4, trimReason(reason));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur audit login: " + e.getMessage(), e);
        }
    }

    public void logOtpRequest(Integer userId, String email, String channel, String requestId, boolean success, String reason) {
        String sql = "INSERT INTO otp_audit(user_id,email,channel,event_type,request_id,success,reason,created_at) " +
                "VALUES(?,?,?,'REQUEST',?,?,?,NOW())";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            if (userId == null) {
                ps.setNull(1, java.sql.Types.INTEGER);
            } else {
                ps.setInt(1, userId);
            }
            ps.setString(2, safe(email));
            ps.setString(3, safe(channel));
            ps.setString(4, safe(requestId));
            ps.setBoolean(5, success);
            ps.setString(6, trimReason(reason));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur audit OTP request: " + e.getMessage(), e);
        }
    }

    public void logOtpValidation(Integer userId, String email, String channel, String requestId, boolean success, String reason, Integer validationSeconds) {
        String sql = "INSERT INTO otp_audit(user_id,email,channel,event_type,request_id,success,reason,validation_seconds,created_at) " +
                "VALUES(?,?,?,'VALIDATE',?,?,?,?,NOW())";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            if (userId == null) {
                ps.setNull(1, java.sql.Types.INTEGER);
            } else {
                ps.setInt(1, userId);
            }
            ps.setString(2, safe(email));
            ps.setString(3, safe(channel));
            ps.setString(4, safe(requestId));
            ps.setBoolean(5, success);
            ps.setString(6, trimReason(reason));
            if (validationSeconds == null) {
                ps.setNull(7, java.sql.Types.INTEGER);
            } else {
                ps.setInt(7, validationSeconds);
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur audit OTP validate: " + e.getMessage(), e);
        }
    }

    private void ensureTables() {
        String loginAuditSql = """
                CREATE TABLE IF NOT EXISTS user_login_audit (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    user_id INT NULL,
                    email VARCHAR(190) NULL,
                    success TINYINT(1) NOT NULL,
                    reason VARCHAR(255) NULL,
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    INDEX idx_login_audit_user (user_id),
                    INDEX idx_login_audit_email (email),
                    INDEX idx_login_audit_created (created_at),
                    CONSTRAINT fk_login_audit_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
                )
                """;

        String otpAuditSql = """
                CREATE TABLE IF NOT EXISTS otp_audit (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    user_id INT NULL,
                    email VARCHAR(190) NULL,
                    channel VARCHAR(20) NOT NULL,
                    event_type VARCHAR(20) NOT NULL,
                    request_id VARCHAR(64) NULL,
                    success TINYINT(1) NOT NULL,
                    reason VARCHAR(255) NULL,
                    validation_seconds INT NULL,
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    INDEX idx_otp_audit_user (user_id),
                    INDEX idx_otp_audit_email (email),
                    INDEX idx_otp_audit_type (event_type),
                    INDEX idx_otp_audit_created (created_at),
                    CONSTRAINT fk_otp_audit_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
                )
                """;

        try (PreparedStatement ps1 = cnx.prepareStatement(loginAuditSql);
             PreparedStatement ps2 = cnx.prepareStatement(otpAuditSql)) {
            ps1.execute();
            ps2.execute();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur creation tables audit: " + e.getMessage(), e);
        }
    }

    private String safe(String v) {
        return v == null ? "" : v.trim();
    }

    private String trimReason(String reason) {
        String value = safe(reason);
        if (value.length() <= 255) return value;
        return value.substring(0, 255);
    }
}