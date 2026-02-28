package org.example.Service.AnalyticsService;

import org.example.Model.User.User;
import org.example.Repository.UserRepository;
import org.example.Utils.MaConnexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class GamificationService {

    private static final int BRONZE_MIN = 100;
    private static final int SILVER_MIN = 300;
    private static final int GOLD_MIN = 700;
    private static final int PLATINUM_MIN = 1500;

    private final Connection cnx;
    private final UserRepository userRepository;
    private final AnalyticsService analyticsService;

    public GamificationService() {
        this.cnx = MaConnexion.getInstance().getCnx();
        this.userRepository = new UserRepository();
        this.analyticsService = new AnalyticsService();
        ensureTables();
    }

    public GamificationSnapshot refreshAndGetSnapshot() {
        refreshModel();
        return buildSnapshot();
    }

    public ClientGamificationSnapshot getClientSnapshot(int userId) {
        refreshModel();
        int points = queryIntWithUser(userId, """
                SELECT COALESCE(SUM(points),0)
                FROM gamification_events
                WHERE user_id = ?
                """);
        List<String> badges = getClientBadges(userId);
        String level = levelFor(points);
        return new ClientGamificationSnapshot(points, level, medalLabelFor(level), badges);
    }

    private void refreshModel() {
        List<User> users = userRepository.findAll();
        for (User user : users) {
            if (user.getRole() == null || !"CLIENT".equalsIgnoreCase(user.getRole().name())) {
                continue;
            }

            int userId = user.getId();
            int success30 = getSuccessLogins30(userId);
            int failed30 = getFailedLogins30(userId);
            boolean hasSecureLogin = hasAnySuccessfulLogin(userId);
            boolean hasMfa = analyticsService.hasSuccessfulOtpForUser(userId);
            boolean profileComplete = isProfileComplete(user);

            if (profileComplete) addEventOnce(userId, "PROFILE_COMPLETED", 10, "Profil complete a 100%");
            if (hasMfa) addEventOnce(userId, "MFA_ENABLED", 20, "Activation MFA");
            if (hasSecureLogin) addEventOnce(userId, "FIRST_SECURE_LOGIN", 15, "Premiere connexion securisee");
            if (success30 >= 10) addEventOnce(userId, "ACTIVE_PARTICIPATION", 50, "Participation active");

            if (hasMfa) addBadgeOnce(userId, "SECURITY_CHAMPION", "Security Champion");
            if (failed30 == 0 && hasSecureLogin) addBadgeOnce(userId, "GUARDIAN", "Guardian");
            if (success30 >= 5) addBadgeOnce(userId, "EARLY_ADOPTER", "Early Adopter");

            upsertChallenge(userId, "CH_PROFILE_COMPLETE", "Completer profil", profileComplete ? 1 : 0, 1);
            upsertChallenge(userId, "CH_ENABLE_MFA", "Activer MFA", hasMfa ? 1 : 0, 1);
            upsertChallenge(userId, "CH_STRONG_PASSWORD", "Connexion securisee sans risque", failed30 == 0 && hasSecureLogin ? 1 : 0, 1);
        }
    }

    private GamificationSnapshot buildSnapshot() {
        List<GamificationLeaderboardItem> leaderboard = getLeaderboard(15);
        List<GamificationChallengeItem> challenges = getChallenges(20);
        List<GamificationBadgeStat> badgeStats = getBadgeStats();

        int bronze = 0;
        int silver = 0;
        int gold = 0;
        int platinum = 0;
        for (GamificationLeaderboardItem row : leaderboard) {
            switch (row.level()) {
                case "PLATINUM" -> platinum++;
                case "GOLD" -> gold++;
                case "SILVER" -> silver++;
                case "BRONZE" -> bronze++;
                default -> {
                }
            }
        }

        int totalPoints = queryInt("SELECT COALESCE(SUM(points),0) FROM gamification_events");
        int totalBadges = queryInt("SELECT COUNT(*) FROM user_badges");
        int completed = queryInt("SELECT COUNT(*) FROM user_security_challenges WHERE status='DONE'");
        int pending = queryInt("SELECT COUNT(*) FROM user_security_challenges WHERE status='PENDING'");

        return new GamificationSnapshot(
                totalPoints,
                totalBadges,
                completed,
                pending,
                bronze,
                silver,
                gold,
                platinum,
                leaderboard,
                challenges,
                badgeStats
        );
    }

    private List<GamificationLeaderboardItem> getLeaderboard(int limit) {
        String sql = """
                SELECT u.id,
                       u.email,
                       COALESCE(SUM(e.points),0) AS points_total,
                       COALESCE(COUNT(DISTINCT b.id),0) AS badges_count
                FROM users u
                LEFT JOIN gamification_events e ON e.user_id = u.id
                LEFT JOIN user_badges b ON b.user_id = u.id
                WHERE u.role='CLIENT'
                GROUP BY u.id, u.email
                ORDER BY points_total DESC, badges_count DESC, u.id ASC
                LIMIT ?
                """;
        List<GamificationLeaderboardItem> rows = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, Math.max(1, Math.min(limit, 100)));
            try (ResultSet rs = ps.executeQuery()) {
                int rank = 1;
                while (rs.next()) {
                    int points = rs.getInt("points_total");
                    rows.add(new GamificationLeaderboardItem(
                            rank++,
                            rs.getInt("id"),
                            rs.getString("email"),
                            points,
                            levelFor(points),
                            rs.getInt("badges_count")
                    ));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur leaderboard gamification: " + e.getMessage(), e);
        }
        return rows;
    }

    private List<GamificationChallengeItem> getChallenges(int limit) {
        String sql = """
                SELECT c.user_id, u.email, c.challenge_title, c.status, c.progress, c.target
                FROM user_security_challenges c
                JOIN users u ON u.id = c.user_id
                WHERE u.role='CLIENT'
                ORDER BY CASE WHEN c.status='PENDING' THEN 0 ELSE 1 END, c.updated_at DESC
                LIMIT ?
                """;
        List<GamificationChallengeItem> rows = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, Math.max(1, Math.min(limit, 200)));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new GamificationChallengeItem(
                            rs.getInt("user_id"),
                            rs.getString("email"),
                            rs.getString("challenge_title"),
                            rs.getString("status"),
                            rs.getInt("progress"),
                            rs.getInt("target")
                    ));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur challenges gamification: " + e.getMessage(), e);
        }
        return rows;
    }

    private List<GamificationBadgeStat> getBadgeStats() {
        String sql = """
                SELECT badge_code, badge_label, COUNT(*) AS holders
                FROM user_badges
                GROUP BY badge_code, badge_label
                ORDER BY holders DESC, badge_label ASC
                """;
        List<GamificationBadgeStat> rows = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rows.add(new GamificationBadgeStat(
                        rs.getString("badge_code"),
                        rs.getString("badge_label"),
                        rs.getInt("holders")
                ));
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur badges gamification: " + e.getMessage(), e);
        }
        return rows;
    }

    private List<String> getClientBadges(int userId) {
        String sql = """
                SELECT badge_label
                FROM user_badges
                WHERE user_id = ?
                ORDER BY awarded_at DESC
                """;
        List<String> rows = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(rs.getString("badge_label"));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur badges client: " + e.getMessage(), e);
        }
        return rows;
    }

    private void addEventOnce(int userId, String eventCode, int points, String eventLabel) {
        String sql = """
                INSERT INTO gamification_events(user_id, event_code, event_label, points, created_at)
                VALUES(?,?,?,?,NOW())
                ON DUPLICATE KEY UPDATE event_label=VALUES(event_label)
                """;
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, eventCode);
            ps.setString(3, eventLabel);
            ps.setInt(4, points);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Erreur gamification event: " + e.getMessage(), e);
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
            throw new RuntimeException("Erreur gamification badge: " + e.getMessage(), e);
        }
    }

    private void upsertChallenge(int userId, String code, String title, int progress, int target) {
        String status = progress >= target ? "DONE" : "PENDING";
        String sql = """
                INSERT INTO user_security_challenges(user_id, challenge_code, challenge_title, status, progress, target, updated_at)
                VALUES(?,?,?,?,?,?,NOW())
                ON DUPLICATE KEY UPDATE
                    challenge_title=VALUES(challenge_title),
                    status=VALUES(status),
                    progress=VALUES(progress),
                    target=VALUES(target),
                    updated_at=NOW()
                """;
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, code);
            ps.setString(3, title);
            ps.setString(4, status);
            ps.setInt(5, progress);
            ps.setInt(6, target);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Erreur gamification challenge: " + e.getMessage(), e);
        }
    }

    private int getSuccessLogins30(int userId) {
        return queryIntWithUser(userId, """
                SELECT COALESCE(COUNT(*),0)
                FROM user_login_audit
                WHERE user_id = ?
                  AND success = 1
                  AND created_at >= NOW() - INTERVAL 30 DAY
                """);
    }

    private int getFailedLogins30(int userId) {
        return queryIntWithUser(userId, """
                SELECT COALESCE(COUNT(*),0)
                FROM user_login_audit
                WHERE user_id = ?
                  AND success = 0
                  AND created_at >= NOW() - INTERVAL 30 DAY
                """);
    }

    private boolean hasAnySuccessfulLogin(int userId) {
        String sql = """
                SELECT 1
                FROM user_login_audit
                WHERE user_id = ?
                  AND success = 1
                LIMIT 1
                """;
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur check login securise: " + e.getMessage(), e);
        }
    }

    private int queryIntWithUser(int userId, String sql) {
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur query gamification user: " + e.getMessage(), e);
        }
    }

    private int queryInt(String sql) {
        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (Exception e) {
            throw new RuntimeException("Erreur query gamification: " + e.getMessage(), e);
        }
    }

    private boolean isProfileComplete(User user) {
        return notBlank(user.getNom())
                && notBlank(user.getPrenom())
                && notBlank(user.getEmail())
                && notBlank(user.getNumTel());
    }

    private boolean notBlank(String v) {
        return v != null && !v.trim().isEmpty();
    }

    private String levelFor(int points) {
        if (points >= PLATINUM_MIN) return "PLATINUM";
        if (points >= GOLD_MIN) return "GOLD";
        if (points >= SILVER_MIN) return "SILVER";
        if (points >= BRONZE_MIN) return "BRONZE";
        return "STARTER";
    }

    private String medalLabelFor(String level) {
        if ("PLATINUM".equals(level)) return "Medaille Platinum";
        if ("GOLD".equals(level)) return "Medaille Gold";
        if ("SILVER".equals(level)) return "Medaille Silver";
        if ("BRONZE".equals(level)) return "Medaille Bronze";
        return "Niveau Starter";
    }

    private void ensureTables() {
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
        String challenges = """
                CREATE TABLE IF NOT EXISTS user_security_challenges (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    user_id INT NOT NULL,
                    challenge_code VARCHAR(80) NOT NULL,
                    challenge_title VARCHAR(160) NOT NULL,
                    status VARCHAR(20) NOT NULL,
                    progress INT NOT NULL DEFAULT 0,
                    target INT NOT NULL DEFAULT 1,
                    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    UNIQUE KEY uq_user_challenge (user_id, challenge_code),
                    INDEX idx_user_challenges_user (user_id),
                    CONSTRAINT fk_user_challenge_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                )
                """;
        try (PreparedStatement ps1 = cnx.prepareStatement(events);
             PreparedStatement ps2 = cnx.prepareStatement(badges);
             PreparedStatement ps3 = cnx.prepareStatement(challenges)) {
            ps1.execute();
            ps2.execute();
            ps3.execute();
        } catch (Exception e) {
            throw new RuntimeException("Erreur creation tables gamification: " + e.getMessage(), e);
        }
    }
}