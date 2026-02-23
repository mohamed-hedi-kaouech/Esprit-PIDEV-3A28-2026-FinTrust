package org.example.Service.AdminOps;

import org.example.Model.AdminOps.AdminTask;
import org.example.Model.AdminOps.AdminTaskPriority;
import org.example.Model.AdminOps.AdminTaskStatus;
import org.example.Model.User.User;
import org.example.Repository.UserRepository;
import org.example.Utils.MaConnexion;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AdminOpsService {

    private final Connection cnx;
    private final UserRepository userRepository;

    public AdminOpsService() {
        this.cnx = MaConnexion.getInstance().getCnx();
        this.userRepository = new UserRepository();
        ensureTables();
    }

    public AdminOpsSnapshot getSnapshot(int adminId, String filter) {
        syncOperationalTasks(adminId);
        List<AdminTask> tasks = listTasks(adminId, filter);
        List<AdminTask> todo = new ArrayList<>();
        List<AdminTask> doing = new ArrayList<>();
        List<AdminTask> done = new ArrayList<>();
        for (AdminTask task : tasks) {
            if (task.getStatus() == AdminTaskStatus.DONE) done.add(task);
            else if (task.getStatus() == AdminTaskStatus.DOING) doing.add(task);
            else todo.add(task);
        }

        return new AdminOpsSnapshot(
                todo,
                doing,
                done,
                countUrgent(adminId),
                countOverdue(adminId),
                countCreatedToday(adminId),
                computeBeforeDeadlineRate(adminId),
                getReward(adminId),
                listAudit(adminId, 25)
        );
    }

    public void createTask(int actorAdminId, String title, String description, AdminTaskPriority priority, String tags, LocalDate dueDate) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Le titre de la tache est obligatoire.");
        }
        String sql = """
                INSERT INTO admin_tasks(
                    title, description, status, priority, tags, due_date, created_by, assigned_to,
                    stars_earned, position_idx, auto_generated, template_code, external_ref, created_at, updated_at
                )
                VALUES(?, ?, 'TODO', ?, ?, ?, ?, ?, 0, ?, 0, NULL, NULL, NOW(), NOW())
                """;
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, title.trim());
            ps.setString(2, sanitize(description));
            ps.setString(3, priority == null ? AdminTaskPriority.MEDIUM.name() : priority.name());
            ps.setString(4, sanitize(tags));
            if (dueDate == null) ps.setNull(5, java.sql.Types.DATE);
            else ps.setDate(5, Date.valueOf(dueDate));
            ps.setInt(6, actorAdminId);
            ps.setInt(7, actorAdminId);
            ps.setInt(8, nextPosition("TODO"));
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Erreur creation tache: " + e.getMessage(), e);
        }

        int taskId = queryInt("SELECT LAST_INSERT_ID()");
        addHistory(taskId, actorAdminId, "CREATE", null, "TODO", "Creation manuelle", 0);
    }

    public void moveTask(int taskId, int actorAdminId, AdminTaskStatus targetStatus) {
        AdminTask current = findById(taskId);
        if (current == null) throw new IllegalArgumentException("Tache introuvable.");
        if (targetStatus == null || current.getStatus() == targetStatus) return;

        String sql = """
                UPDATE admin_tasks
                SET status = ?, position_idx = ?, updated_at = NOW()
                WHERE id = ?
                """;
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, targetStatus.name());
            ps.setInt(2, nextPosition(targetStatus.name()));
            ps.setInt(3, taskId);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Erreur changement statut tache: " + e.getMessage(), e);
        }

        addHistory(taskId, actorAdminId, "MOVE", current.getStatus().name(), targetStatus.name(), "Changement colonne kanban", 0);
    }

    public int completeTask(int taskId, int actorAdminId) {
        AdminTask task = findById(taskId);
        if (task == null) throw new IllegalArgumentException("Tache introuvable.");
        if (task.getStatus() == AdminTaskStatus.DONE) {
            return task.getStarsEarned();
        }

        int stars = calculateStars(task);
        String sql = """
                UPDATE admin_tasks
                SET status='DONE', stars_earned=?, completed_at=NOW(), updated_at=NOW()
                WHERE id=?
                """;
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, stars);
            ps.setInt(2, taskId);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Erreur completion tache: " + e.getMessage(), e);
        }

        int points = stars * 20;
        updateRewardsOnCompletion(actorAdminId, stars, points);
        addHistory(taskId, actorAdminId, "COMPLETE", task.getStatus().name(), "DONE", "Tache terminee", stars);
        return stars;
    }

    private List<AdminTask> listTasks(int adminId, String filter) {
        String normalized = filter == null ? "MY" : filter.toUpperCase().trim();
        boolean onlyMine = !"ALL".equals(normalized);
        boolean onlyUrgent = "URGENT".equals(normalized);
        boolean onlyOverdue = "OVERDUE".equals(normalized);

        String sql = """
                SELECT t.id, t.title, t.description, t.status, t.priority, t.tags, t.due_date,
                       t.created_by, t.assigned_to, t.stars_earned, t.completed_at, t.position_idx,
                       t.auto_generated, u.email AS assigned_email
                FROM admin_tasks t
                LEFT JOIN users u ON u.id = t.assigned_to
                WHERE (? = 0 OR t.assigned_to = ?)
                  AND (? = 0 OR t.priority = 'URGENT')
                  AND (? = 0 OR (t.status <> 'DONE' AND t.due_date IS NOT NULL AND t.due_date < CURDATE()))
                ORDER BY FIELD(t.status, 'TODO','DOING','DONE'), t.position_idx ASC, t.updated_at DESC
                """;
        List<AdminTask> rows = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, onlyMine ? 1 : 0);
            ps.setInt(2, adminId);
            ps.setInt(3, onlyUrgent ? 1 : 0);
            ps.setInt(4, onlyOverdue ? 1 : 0);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) rows.add(mapTask(rs));
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur lecture taches admin: " + e.getMessage(), e);
        }
        return rows;
    }

    private List<AdminTaskAuditEntry> listAudit(int adminId, int limit) {
        String sql = """
                SELECT h.task_id, h.action, h.from_status, h.to_status, h.stars_earned, h.created_at,
                       u.email AS actor_email
                FROM admin_task_history h
                LEFT JOIN users u ON u.id = h.actor_admin_id
                WHERE h.actor_admin_id = ?
                ORDER BY h.created_at DESC
                LIMIT ?
                """;
        List<AdminTaskAuditEntry> rows = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, adminId);
            ps.setInt(2, Math.max(5, Math.min(100, limit)));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp ts = rs.getTimestamp("created_at");
                    rows.add(new AdminTaskAuditEntry(
                            rs.getInt("task_id"),
                            rs.getString("action"),
                            rs.getString("from_status"),
                            rs.getString("to_status"),
                            rs.getString("actor_email"),
                            rs.getInt("stars_earned"),
                            ts == null ? null : ts.toLocalDateTime()
                    ));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur lecture audit taches: " + e.getMessage(), e);
        }
        return rows;
    }

    private AdminTask mapTask(ResultSet rs) throws Exception {
        AdminTask t = new AdminTask();
        t.setId(rs.getInt("id"));
        t.setTitle(rs.getString("title"));
        t.setDescription(rs.getString("description"));
        t.setStatus(AdminTaskStatus.fromDb(rs.getString("status")));
        t.setPriority(AdminTaskPriority.fromDb(rs.getString("priority")));
        t.setTags(rs.getString("tags"));
        Date due = rs.getDate("due_date");
        t.setDueDate(due == null ? null : due.toLocalDate());
        t.setCreatedBy(rs.getInt("created_by"));
        t.setAssignedTo(rs.getInt("assigned_to"));
        t.setAssignedEmail(rs.getString("assigned_email"));
        t.setStarsEarned(rs.getInt("stars_earned"));
        Timestamp completed = rs.getTimestamp("completed_at");
        t.setCompletedAt(completed == null ? null : completed.toLocalDateTime());
        t.setPosition(rs.getInt("position_idx"));
        t.setAutoGenerated(rs.getBoolean("auto_generated"));
        return t;
    }

    private int calculateStars(AdminTask task) {
        int stars = 1;
        if (task.getDueDate() != null && !LocalDate.now().isAfter(task.getDueDate())) stars += 1;
        String tags = task.getTags() == null ? "" : task.getTags().toUpperCase();
        if (task.getPriority() == AdminTaskPriority.URGENT || tags.contains("SECURITY")) stars += 1;
        return Math.min(3, stars);
    }

    private void updateRewardsOnCompletion(int adminId, int stars, int points) {
        int currentStars = 0;
        int currentPoints = 0;
        int streak = 0;
        LocalDate lastDate = null;
        boolean badge = false;

        String select = "SELECT total_stars, total_points, streak_days, last_completion_date, task_finisher_badge FROM admin_rewards WHERE admin_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(select)) {
            ps.setInt(1, adminId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    currentStars = rs.getInt("total_stars");
                    currentPoints = rs.getInt("total_points");
                    streak = rs.getInt("streak_days");
                    Date d = rs.getDate("last_completion_date");
                    lastDate = d == null ? null : d.toLocalDate();
                    badge = rs.getBoolean("task_finisher_badge");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur lecture reward admin: " + e.getMessage(), e);
        }

        LocalDate today = LocalDate.now();
        int newStreak;
        if (lastDate == null) newStreak = 1;
        else if (lastDate.isEqual(today)) newStreak = Math.max(1, streak);
        else if (lastDate.plusDays(1).isEqual(today)) newStreak = Math.max(1, streak) + 1;
        else newStreak = 1;

        int doneCount = queryIntWithAdmin(adminId, "SELECT COUNT(*) FROM admin_tasks WHERE assigned_to=? AND status='DONE'");
        boolean newBadge = badge || doneCount >= 1;

        String upsert = """
                INSERT INTO admin_rewards(admin_id, total_stars, total_points, streak_days, last_completion_date, task_finisher_badge, updated_at)
                VALUES(?,?,?,?,?,?,NOW())
                ON DUPLICATE KEY UPDATE
                    total_stars=VALUES(total_stars),
                    total_points=VALUES(total_points),
                    streak_days=VALUES(streak_days),
                    last_completion_date=VALUES(last_completion_date),
                    task_finisher_badge=VALUES(task_finisher_badge),
                    updated_at=NOW()
                """;
        try (PreparedStatement ps = cnx.prepareStatement(upsert)) {
            ps.setInt(1, adminId);
            ps.setInt(2, currentStars + stars);
            ps.setInt(3, currentPoints + points);
            ps.setInt(4, newStreak);
            ps.setDate(5, Date.valueOf(today));
            ps.setBoolean(6, newBadge);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Erreur mise a jour reward admin: " + e.getMessage(), e);
        }
    }

    private AdminRewardSnapshot getReward(int adminId) {
        String sql = """
                SELECT total_stars, total_points, streak_days, task_finisher_badge
                FROM admin_rewards
                WHERE admin_id = ?
                """;
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, adminId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new AdminRewardSnapshot(
                            rs.getInt("total_stars"),
                            rs.getInt("total_points"),
                            rs.getInt("streak_days"),
                            rs.getBoolean("task_finisher_badge")
                    );
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur lecture reward: " + e.getMessage(), e);
        }
        return new AdminRewardSnapshot(0, 0, 0, false);
    }

    private int countUrgent(int adminId) {
        return queryIntWithAdmin(adminId, """
                SELECT COUNT(*)
                FROM admin_tasks
                WHERE assigned_to = ?
                  AND status <> 'DONE'
                  AND priority = 'URGENT'
                """);
    }

    private int countOverdue(int adminId) {
        return queryIntWithAdmin(adminId, """
                SELECT COUNT(*)
                FROM admin_tasks
                WHERE assigned_to = ?
                  AND status <> 'DONE'
                  AND due_date IS NOT NULL
                  AND due_date < CURDATE()
                """);
    }

    private int countCreatedToday(int adminId) {
        return queryIntWithAdmin(adminId, """
                SELECT COUNT(*)
                FROM admin_tasks
                WHERE assigned_to = ?
                  AND DATE(created_at) = CURDATE()
                """);
    }

    private double computeBeforeDeadlineRate(int adminId) {
        int withDeadline = queryIntWithAdmin(adminId, """
                SELECT COUNT(*)
                FROM admin_tasks
                WHERE assigned_to = ?
                  AND status = 'DONE'
                  AND due_date IS NOT NULL
                """);
        if (withDeadline == 0) return 0.0;
        int doneInTime = queryIntWithAdmin(adminId, """
                SELECT COUNT(*)
                FROM admin_tasks
                WHERE assigned_to = ?
                  AND status = 'DONE'
                  AND due_date IS NOT NULL
                  AND DATE(completed_at) <= due_date
                """);
        return (doneInTime * 100.0) / withDeadline;
    }

    private void syncOperationalTasks(int adminId) {
        int kycPending = 0;
        try {
            kycPending = queryInt("SELECT COUNT(*) FROM kyc WHERE statut='EN_ATTENTE'");
        } catch (Exception ignored) {
        }

        if (kycPending > 0) {
            upsertAutoTask(
                    "TPL_KYC_REVIEW",
                    "KYC_PENDING_REVIEW",
                    "Verifier KYC en attente (" + kycPending + ")",
                    "Des dossiers KYC sont en attente. Ouvrir Validation KYC pour traitement.",
                    AdminTaskPriority.HIGH,
                    "KYC,COMPLIANCE",
                    LocalDate.now().plusDays(1),
                    adminId
            );
        }

        int suspicious = 0;
        try {
            suspicious = queryInt("""
                    SELECT COUNT(*)
                    FROM (
                        SELECT user_id
                        FROM user_login_audit
                        WHERE success = 0
                          AND created_at >= NOW() - INTERVAL 24 HOUR
                        GROUP BY user_id
                        HAVING COUNT(*) >= 5
                    ) x
                    """);
        } catch (Exception ignored) {
        }

        if (suspicious > 0) {
            upsertAutoTask(
                    "TPL_SECURITY_ALERT",
                    "SECURITY_REVIEW",
                    "Revoir alertes securite (" + suspicious + ")",
                    "Des utilisateurs ont plusieurs echecs de connexion. Verifier le risque.",
                    AdminTaskPriority.URGENT,
                    "SECURITY,RISK",
                    LocalDate.now(),
                    adminId
            );
        }
    }

    private void upsertAutoTask(String templateCode, String externalRef, String title, String description, AdminTaskPriority priority, String tags, LocalDate dueDate, int adminId) {
        String sql = """
                INSERT INTO admin_tasks(
                    title, description, status, priority, tags, due_date, created_by, assigned_to, stars_earned,
                    position_idx, auto_generated, template_code, external_ref, created_at, updated_at
                )
                VALUES(?, ?, 'TODO', ?, ?, ?, ?, ?, 0, ?, 1, ?, ?, NOW(), NOW())
                ON DUPLICATE KEY UPDATE
                    title = VALUES(title),
                    description = VALUES(description),
                    priority = VALUES(priority),
                    tags = VALUES(tags),
                    due_date = VALUES(due_date),
                    assigned_to = VALUES(assigned_to),
                    status = IF(status='DONE', status, 'TODO'),
                    updated_at = NOW()
                """;
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, description);
            ps.setString(3, priority.name());
            ps.setString(4, tags);
            ps.setDate(5, dueDate == null ? null : Date.valueOf(dueDate));
            ps.setInt(6, adminId);
            ps.setInt(7, adminId);
            ps.setInt(8, nextPosition("TODO"));
            ps.setString(9, templateCode);
            ps.setString(10, externalRef);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Erreur tache auto: " + e.getMessage(), e);
        }
    }

    private AdminTask findById(int taskId) {
        String sql = """
                SELECT t.id, t.title, t.description, t.status, t.priority, t.tags, t.due_date,
                       t.created_by, t.assigned_to, t.stars_earned, t.completed_at, t.position_idx,
                       t.auto_generated, u.email AS assigned_email
                FROM admin_tasks t
                LEFT JOIN users u ON u.id = t.assigned_to
                WHERE t.id = ?
                """;
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, taskId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapTask(rs);
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur lecture tache: " + e.getMessage(), e);
        }
        return null;
    }

    private void addHistory(int taskId, int actorAdminId, String action, String fromStatus, String toStatus, String note, int stars) {
        String sql = """
                INSERT INTO admin_task_history(task_id, actor_admin_id, action, from_status, to_status, note, stars_earned, created_at)
                VALUES(?,?,?,?,?,?,?,NOW())
                """;
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, taskId);
            ps.setInt(2, actorAdminId);
            ps.setString(3, action);
            ps.setString(4, fromStatus);
            ps.setString(5, toStatus);
            ps.setString(6, sanitize(note));
            ps.setInt(7, stars);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Erreur audit tache: " + e.getMessage(), e);
        }
    }

    private int nextPosition(String status) {
        String sql = "SELECT COALESCE(MAX(position_idx),0)+1 FROM admin_tasks WHERE status = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 1;
            }
        } catch (Exception e) {
            return 1;
        }
    }

    private int queryIntWithAdmin(int adminId, String sql) {
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, adminId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur statistiques taches: " + e.getMessage(), e);
        }
    }

    private int queryInt(String sql) {
        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (Exception e) {
            throw new RuntimeException("Erreur query taches: " + e.getMessage(), e);
        }
    }

    private String sanitize(String value) {
        if (value == null) return "";
        String v = value.trim();
        return v.replace("<", "").replace(">", "");
    }

    private void ensureTables() {
        String tasks = """
                CREATE TABLE IF NOT EXISTS admin_tasks (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    title VARCHAR(180) NOT NULL,
                    description TEXT NULL,
                    status ENUM('TODO','DOING','DONE') NOT NULL DEFAULT 'TODO',
                    priority ENUM('LOW','MEDIUM','HIGH','URGENT') NOT NULL DEFAULT 'MEDIUM',
                    tags VARCHAR(180) NULL,
                    due_date DATE NULL,
                    created_by INT NOT NULL,
                    assigned_to INT NOT NULL,
                    stars_earned INT NOT NULL DEFAULT 0,
                    completed_at DATETIME NULL,
                    position_idx INT NOT NULL DEFAULT 1,
                    auto_generated TINYINT(1) NOT NULL DEFAULT 0,
                    template_code VARCHAR(60) NULL,
                    external_ref VARCHAR(120) NULL,
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    UNIQUE KEY uq_admin_task_external_ref (external_ref),
                    INDEX idx_admin_task_status (status),
                    INDEX idx_admin_task_assigned (assigned_to),
                    CONSTRAINT fk_admin_task_creator FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE,
                    CONSTRAINT fk_admin_task_assigned FOREIGN KEY (assigned_to) REFERENCES users(id) ON DELETE CASCADE
                )
                """;

        String history = """
                CREATE TABLE IF NOT EXISTS admin_task_history (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    task_id INT NOT NULL,
                    actor_admin_id INT NOT NULL,
                    action VARCHAR(40) NOT NULL,
                    from_status VARCHAR(20) NULL,
                    to_status VARCHAR(20) NULL,
                    note VARCHAR(255) NULL,
                    stars_earned INT NOT NULL DEFAULT 0,
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    INDEX idx_admin_task_history_task (task_id),
                    INDEX idx_admin_task_history_actor (actor_admin_id),
                    CONSTRAINT fk_admin_task_history_task FOREIGN KEY (task_id) REFERENCES admin_tasks(id) ON DELETE CASCADE,
                    CONSTRAINT fk_admin_task_history_actor FOREIGN KEY (actor_admin_id) REFERENCES users(id) ON DELETE CASCADE
                )
                """;

        String rewards = """
                CREATE TABLE IF NOT EXISTS admin_rewards (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    admin_id INT NOT NULL UNIQUE,
                    total_stars INT NOT NULL DEFAULT 0,
                    total_points INT NOT NULL DEFAULT 0,
                    streak_days INT NOT NULL DEFAULT 0,
                    last_completion_date DATE NULL,
                    task_finisher_badge TINYINT(1) NOT NULL DEFAULT 0,
                    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    CONSTRAINT fk_admin_rewards_admin FOREIGN KEY (admin_id) REFERENCES users(id) ON DELETE CASCADE
                )
                """;

        try (PreparedStatement ps1 = cnx.prepareStatement(tasks);
             PreparedStatement ps2 = cnx.prepareStatement(history);
             PreparedStatement ps3 = cnx.prepareStatement(rewards)) {
            ps1.execute();
            ps2.execute();
            ps3.execute();
        } catch (Exception e) {
            throw new RuntimeException("Erreur creation tables Admin Ops: " + e.getMessage(), e);
        }
    }

    public User findAdminById(int id) {
        List<User> users = userRepository.findAll();
        for (User user : users) {
            if (user.getId() == id) return user;
        }
        return null;
    }
}

