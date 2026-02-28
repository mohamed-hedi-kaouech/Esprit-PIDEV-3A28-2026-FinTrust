package org.example.Service.GameService;

import org.example.Utils.MaConnexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GameService {

    private static final Map<String, SessionData> SESSIONS = new ConcurrentHashMap<>();
    private static final int BRONZE_MIN = 100;
    private static final int SILVER_MIN = 300;
    private static final int GOLD_MIN = 700;
    private static final int PLATINUM_MIN = 1500;
    private final Connection cnx;

    public GameService() {
        this.cnx = MaConnexion.getInstance().getCnx();
        ensureGamificationTables();
    }

    public GameSessionStart startSession(int userId, String context) {
        String sessionId = UUID.randomUUID().toString();
        String safeContext = sanitizeContext(context);
        SESSIONS.put(sessionId, new SessionData(sessionId, userId, safeContext, LocalDateTime.now(), false));
        return new GameSessionStart(sessionId, safeContext);
    }

    public GameSessionResult endSession(int userId, String sessionId, int score, long durationMs, int moves) {
        if (sessionId == null || sessionId.isBlank()) {
            return new GameSessionResult(false, 0, "Session invalide.");
        }
        SessionData session = SESSIONS.get(sessionId);
        if (session == null || session.userId() != userId) {
            return new GameSessionResult(false, 0, "Session introuvable.");
        }
        if (session.ended()) {
            return new GameSessionResult(false, 0, "Session deja terminee.");
        }
        if (durationMs < 10_000L) {
            return new GameSessionResult(false, 0, "Session refusee: duree trop courte.");
        }
        if (durationMs > 180_000L) {
            return new GameSessionResult(false, 0, "Session refusee: duree suspecte.");
        }
        if (moves < 1) {
            return new GameSessionResult(false, 0, "Session refusee: coups invalides.");
        }

        SESSIONS.put(sessionId, session.end());
        try {
            int granted = pointsFromGameScore(score);
            String eventCode = ("GAME_SESSION_" + sessionId).replace("-", "_");
            addGameEvent(userId, eventCode, granted, "Memory Game Session");

            int totalPoints = queryTotalPoints(userId);
            String level = computeLevel(totalPoints);
            awardLevelBadge(userId, level);
            addBadgeOnce(userId, "DAILY_PLAYER", "Daily Player");

            return new GameSessionResult(true, granted,
                    "Partie valide. +" + granted + " pts. Total: " + totalPoints + " (" + level + ").");
        } catch (Exception e) {
            return new GameSessionResult(false, 0, "Partie terminee mais points non enregistres: " + e.getMessage());
        }
    }

    public void cancelSession(int userId, String sessionId) {
        if (sessionId == null || sessionId.isBlank()) return;
        SessionData session = SESSIONS.get(sessionId);
        if (session == null) return;
        if (session.userId() != userId) return;
        SESSIONS.remove(sessionId);
    }

    private int pointsFromGameScore(int score) {
        int safeScore = Math.max(0, score);
        // Base 5 + progression selon performance.
        return 5 + (safeScore / 20);
    }

    private void addGameEvent(int userId, String eventCode, int points, String label) {
        String sql = """
                INSERT INTO gamification_events(user_id, event_code, event_label, points, created_at)
                VALUES(?,?,?,?,NOW())
                """;
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, eventCode);
            ps.setString(3, label);
            ps.setInt(4, points);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Erreur enregistrement points jeu: " + e.getMessage(), e);
        }
    }

    private int queryTotalPoints(int userId) {
        String sql = """
                SELECT COALESCE(SUM(points),0)
                FROM gamification_events
                WHERE user_id = ?
                """;
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur lecture total points: " + e.getMessage(), e);
        }
    }

    private void awardLevelBadge(int userId, String level) {
        switch (level) {
            case "BRONZE" -> addBadgeOnce(userId, "LEVEL_BRONZE", "Medaille Bronze");
            case "SILVER" -> {
                addBadgeOnce(userId, "LEVEL_BRONZE", "Medaille Bronze");
                addBadgeOnce(userId, "LEVEL_SILVER", "Medaille Silver");
            }
            case "GOLD" -> {
                addBadgeOnce(userId, "LEVEL_BRONZE", "Medaille Bronze");
                addBadgeOnce(userId, "LEVEL_SILVER", "Medaille Silver");
                addBadgeOnce(userId, "LEVEL_GOLD", "Medaille Gold");
            }
            case "PLATINUM" -> {
                addBadgeOnce(userId, "LEVEL_BRONZE", "Medaille Bronze");
                addBadgeOnce(userId, "LEVEL_SILVER", "Medaille Silver");
                addBadgeOnce(userId, "LEVEL_GOLD", "Medaille Gold");
                addBadgeOnce(userId, "LEVEL_PLATINUM", "Medaille Platinum");
            }
            default -> {
            }
        }
    }

    private void addBadgeOnce(int userId, String badgeCode, String badgeLabel) {
        String sql = """
                INSERT INTO user_badges(user_id, badge_code, badge_label, awarded_at)
                VALUES(?,?,?,NOW())
                ON DUPLICATE KEY UPDATE badge_label=VALUES(badge_label)
                """;
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, badgeCode);
            ps.setString(3, badgeLabel);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Erreur attribution badge: " + e.getMessage(), e);
        }
    }

    private String computeLevel(int points) {
        if (points >= PLATINUM_MIN) return "PLATINUM";
        if (points >= GOLD_MIN) return "GOLD";
        if (points >= SILVER_MIN) return "SILVER";
        if (points >= BRONZE_MIN) return "BRONZE";
        return "STARTER";
    }

    private String sanitizeContext(String context) {
        if (context == null) return "PROFILE";
        String c = context.trim().toUpperCase();
        if ("KYC".equals(c) || "CHATBOT".equals(c) || "PROFILE".equals(c)) return c;
        return "PROFILE";
    }

    private void ensureGamificationTables() {
        String events = """
                CREATE TABLE IF NOT EXISTS gamification_events (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    user_id INT NOT NULL,
                    event_code VARCHAR(80) NOT NULL,
                    event_label VARCHAR(160) NOT NULL,
                    points INT NOT NULL,
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    UNIQUE KEY uq_gamification_event (user_id, event_code),
                    INDEX idx_gamification_user (user_id),
                    CONSTRAINT fk_gamification_event_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                )
                """;
        String badges = """
                CREATE TABLE IF NOT EXISTS user_badges (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    user_id INT NOT NULL,
                    badge_code VARCHAR(80) NOT NULL,
                    badge_label VARCHAR(160) NOT NULL,
                    awarded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    UNIQUE KEY uq_user_badge (user_id, badge_code),
                    INDEX idx_user_badges_user (user_id),
                    CONSTRAINT fk_user_badge_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                )
                """;
        try (PreparedStatement ps1 = cnx.prepareStatement(events);
             PreparedStatement ps2 = cnx.prepareStatement(badges)) {
            ps1.execute();
            ps2.execute();
        } catch (Exception e) {
            throw new RuntimeException("Erreur creation tables gamification jeu: " + e.getMessage(), e);
        }
    }

    private record SessionData(
            String sessionId,
            int userId,
            String context,
            LocalDateTime startedAt,
            boolean ended
    ) {
        private SessionData end() {
            return new SessionData(sessionId, userId, context, startedAt, true);
        }
    }

}