package org.example.Service.NotificationService;

import org.example.Model.Notification.Notification;
import org.example.Utils.MaConnexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NotificationService {

    private static volatile boolean schemaReady = false;

    private Connection connection() {
        return MaConnexion.getInstance().getCnx();
    }

    private void ensureSchema() {
        if (schemaReady) return;
        synchronized (NotificationService.class) {
            if (schemaReady) return;
            String sql = "CREATE TABLE IF NOT EXISTS notifications (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT," +
                    "user_id INT NOT NULL," +
                    "type VARCHAR(50) NOT NULL," +
                    "message TEXT NOT NULL," +
                    "is_read TINYINT(1) NOT NULL DEFAULT 0," +
                    "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "INDEX idx_notifications_user_id (user_id)," +
                    "CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                    ")";
            try (PreparedStatement ps = connection().prepareStatement(sql)) {
                ps.executeUpdate();
                schemaReady = true;
            } catch (SQLException e) {
                throw new RuntimeException("Erreur creation table notifications: " + e.getMessage(), e);
            }
        }
    }

    public int countUnread(int userId) {
        ensureSchema();
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id=? AND is_read=0";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur count notifications: " + e.getMessage(), e);
        }
    }

    public List<Notification> listForUser(int userId) {
        ensureSchema();
        String sql = "SELECT id,user_id,type,message,is_read,created_at FROM notifications WHERE user_id=? ORDER BY created_at DESC";
        List<Notification> out = new ArrayList<>();
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new Notification(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            rs.getString("type"),
                            rs.getString("message"),
                            rs.getInt("is_read") == 1,
                            rs.getTimestamp("created_at").toLocalDateTime()
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lecture notifications: " + e.getMessage(), e);
        }
        return out;
    }

    public void markAllRead(int userId) {
        ensureSchema();
        String sql = "UPDATE notifications SET is_read=1 WHERE user_id=?";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur mark notifications read: " + e.getMessage(), e);
        }
    }

    public void create(int userId, String type, String message) {
        ensureSchema();
        String sql = "INSERT INTO notifications(user_id,type,message,is_read,created_at) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, type == null || type.isBlank() ? "SYSTEM" : type);
            ps.setString(3, message == null ? "" : message);
            ps.setInt(4, 0);
            ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur creation notification: " + e.getMessage(), e);
        }
    }
}