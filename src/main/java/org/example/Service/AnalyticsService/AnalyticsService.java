package org.example.Service.AnalyticsService;

import org.example.Service.AuditService.AuditService;
import org.example.Utils.MaConnexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class AnalyticsService {

    private final Connection cnx;
    private final AuditService auditService;

    public AnalyticsService() {
        this.cnx = MaConnexion.getInstance().getCnx();
        this.auditService = new AuditService();
    }

    public List<UserSegmentItem> getUserSegments() {
        String sql = """
                SELECT u.id,
                       u.email,
                       MAX(CASE WHEN a.success = 1 THEN a.created_at END) AS last_success,
                       COALESCE(SUM(CASE WHEN a.success = 1 AND a.created_at >= NOW() - INTERVAL 30 DAY THEN 1 ELSE 0 END), 0) AS success_30,
                       COALESCE(SUM(CASE WHEN a.success = 0 AND a.created_at >= NOW() - INTERVAL 30 DAY THEN 1 ELSE 0 END), 0) AS failed_30
                FROM users u
                LEFT JOIN user_login_audit a ON a.user_id = u.id
                GROUP BY u.id, u.email
                ORDER BY u.id
                """;

        List<UserSegmentItem> items = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int userId = rs.getInt("id");
                String email = rs.getString("email");
                int success30 = rs.getInt("success_30");
                int failed30 = rs.getInt("failed_30");
                Timestamp lastTs = rs.getTimestamp("last_success");
                LocalDateTime lastSuccess = lastTs == null ? null : lastTs.toLocalDateTime();

                UserSegmentType segment;
                if (failed30 >= 5) {
                    segment = UserSegmentType.AT_RISK;
                } else if (success30 >= 20) {
                    segment = UserSegmentType.VERY_ACTIVE;
                } else if (lastSuccess == null || ChronoUnit.DAYS.between(lastSuccess, LocalDateTime.now()) >= 30) {
                    segment = UserSegmentType.DORMANT;
                } else {
                    segment = UserSegmentType.ACTIVE;
                }

                items.add(new UserSegmentItem(userId, email, segment, success30, failed30, lastSuccess));
            }
        } catch (SQLException e) {
            // Fallback robuste: si les tables d'audit ne sont pas disponibles, on retourne
            // au moins la liste des users avec des compteurs a zero.
            return fallbackUserSegmentsWithoutAudit();
        }
        return items;
    }

    public Map<UserSegmentType, Integer> getSegmentCounters() {
        Map<UserSegmentType, Integer> counters = new EnumMap<>(UserSegmentType.class);
        for (UserSegmentType t : UserSegmentType.values()) counters.put(t, 0);

        for (UserSegmentItem item : getUserSegments()) {
            counters.put(item.segment(), counters.get(item.segment()) + 1);
        }
        return counters;
    }

    public List<ChurnRiskItem> getChurnRisk() {
        List<ChurnRiskItem> risks = new ArrayList<>();
        for (UserSegmentItem item : getUserSegments()) {
            int days;
            if (item.lastSuccessfulLogin() == null) {
                days = 999;
            } else {
                days = (int) ChronoUnit.DAYS.between(item.lastSuccessfulLogin(), LocalDateTime.now());
            }
            if (days >= 45) {
                risks.add(new ChurnRiskItem(item.userId(), item.email(), item.lastSuccessfulLogin(), days, days >= 60));
            }
        }
        return risks;
    }

    public List<FailedLoginUser> getTopFailedLogins(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 100));
        String sql = """
                SELECT u.id, u.email, COUNT(*) AS failed_30
                FROM user_login_audit a
                JOIN users u ON u.id = a.user_id
                WHERE a.success = 0
                  AND a.created_at >= NOW() - INTERVAL 30 DAY
                GROUP BY u.id, u.email
                ORDER BY failed_30 DESC
                LIMIT ?
                """;
        List<FailedLoginUser> rows = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, safeLimit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new FailedLoginUser(
                            rs.getInt("id"),
                            rs.getString("email"),
                            rs.getInt("failed_30")
                    ));
                }
            }
        } catch (SQLException e) {
            return rows;
        }
        return rows;
    }

    public List<HeatmapPoint> getLoginHeatmapByHour() {
        String sql = """
                SELECT DATE_FORMAT(created_at, '%H:00') AS h, COUNT(*) AS c
                FROM user_login_audit
                WHERE success = 1
                  AND created_at >= NOW() - INTERVAL 30 DAY
                GROUP BY DATE_FORMAT(created_at, '%H')
                ORDER BY DATE_FORMAT(created_at, '%H')
                """;
        return queryHeatmap(sql, "h");
    }

    public List<HeatmapPoint> getLoginHeatmapByDay() {
        String sql = """
                SELECT DATE_FORMAT(created_at, '%Y-%m-%d') AS d, COUNT(*) AS c
                FROM user_login_audit
                WHERE success = 1
                  AND created_at >= NOW() - INTERVAL 30 DAY
                GROUP BY DATE_FORMAT(created_at, '%Y-%m-%d')
                ORDER BY DATE_FORMAT(created_at, '%Y-%m-%d')
                """;
        return queryHeatmap(sql, "d");
    }

    public OtpAnalyticsSnapshot getOtpAnalytics() {
        String sql = """
                SELECT
                    COALESCE(SUM(CASE WHEN event_type='REQUEST' THEN 1 ELSE 0 END),0) AS total_requests,
                    COALESCE(SUM(CASE WHEN event_type='REQUEST' AND success=1 THEN 1 ELSE 0 END),0) AS successful_requests,
                    COALESCE(SUM(CASE WHEN event_type='REQUEST' AND success=0 THEN 1 ELSE 0 END),0) AS failed_requests,
                    COALESCE(SUM(CASE WHEN event_type='VALIDATE' THEN 1 ELSE 0 END),0) AS total_validations,
                    COALESCE(SUM(CASE WHEN event_type='VALIDATE' AND success=1 THEN 1 ELSE 0 END),0) AS successful_validations,
                    COALESCE(SUM(CASE WHEN event_type='VALIDATE' AND success=0 THEN 1 ELSE 0 END),0) AS failed_validations,
                    COALESCE(AVG(CASE WHEN event_type='VALIDATE' AND success=1 THEN validation_seconds END),0) AS avg_validation_seconds
                FROM otp_audit
                WHERE created_at >= NOW() - INTERVAL 30 DAY
                """;
        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) {
                return new OtpAnalyticsSnapshot(0, 0, 0, 0, 0, 0, 0, 0, 0);
            }
            int totalRequests = rs.getInt("total_requests");
            int successfulRequests = rs.getInt("successful_requests");
            int failedRequests = rs.getInt("failed_requests");
            int totalValidations = rs.getInt("total_validations");
            int successfulValidations = rs.getInt("successful_validations");
            int failedValidations = rs.getInt("failed_validations");
            double avgValidationSeconds = rs.getDouble("avg_validation_seconds");

            double requestRate = totalRequests == 0 ? 0 : (successfulRequests * 100.0 / totalRequests);
            double validationRate = totalValidations == 0 ? 0 : (successfulValidations * 100.0 / totalValidations);

            return new OtpAnalyticsSnapshot(
                    totalRequests, successfulRequests, failedRequests,
                    totalValidations, successfulValidations, failedValidations,
                    requestRate, validationRate, avgValidationSeconds
            );
        } catch (SQLException e) {
            return new OtpAnalyticsSnapshot(0, 0, 0, 0, 0, 0, 0, 0, 0);
        }
    }

    public boolean hasSuccessfulOtpForUser(int userId) {
        String sql = """
                SELECT 1
                FROM otp_audit
                WHERE user_id = ?
                  AND event_type = 'VALIDATE'
                  AND success = 1
                LIMIT 1
                """;
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    private List<HeatmapPoint> queryHeatmap(String sql, String bucketColumn) {
        List<HeatmapPoint> points = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                points.add(new HeatmapPoint(rs.getString(bucketColumn), rs.getInt("c")));
            }
        } catch (SQLException e) {
            return points;
        }
        return points;
    }

    private List<UserSegmentItem> fallbackUserSegmentsWithoutAudit() {
        String sql = "SELECT id, email FROM users ORDER BY id";
        List<UserSegmentItem> items = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                items.add(new UserSegmentItem(
                        rs.getInt("id"),
                        rs.getString("email"),
                        UserSegmentType.DORMANT,
                        0,
                        0,
                        null
                ));
            }
            return items;
        } catch (SQLException ignored) {
            return items;
        }
    }
}