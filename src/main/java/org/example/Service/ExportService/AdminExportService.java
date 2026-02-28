package org.example.Service.ExportService;

import org.example.Model.User.User;
import org.example.Model.User.UserStatus;
import org.example.Service.AnalyticsService.AnalyticsService;
import org.example.Service.AnalyticsService.FailedLoginUser;
import org.example.Service.AnalyticsService.OtpAnalyticsSnapshot;
import org.example.Service.AnalyticsService.UserSegmentType;
import org.example.Utils.MaConnexion;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AdminExportService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter FILE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm");
    private final AnalyticsService analyticsService = new AnalyticsService();
    private final Connection cnx = MaConnexion.getInstance().getCnx();

    public String buildDefaultName(String prefix, String ext) {
        return prefix + "_" + LocalDateTime.now().format(FILE_DATE_FORMAT) + "." + ext;
    }

    public void exportUsersCsv(Path path, List<User> users) {
        StringBuilder sb = new StringBuilder();
        sb.append("ID,Nom,Prenom,Email,Telephone,Role,Statut,CreeLe\n");
        for (User u : users) {
            sb.append(csv(u.getId())).append(',')
                    .append(csv(u.getNom())).append(',')
                    .append(csv(u.getPrenom())).append(',')
                    .append(csv(u.getEmail())).append(',')
                    .append(csv(u.getNumTel())).append(',')
                    .append(csv(u.getRole() == null ? "" : u.getRole().name())).append(',')
                    .append(csv(u.getStatus() == null ? "" : u.getStatus().name())).append(',')
                    .append(csv(u.getCreatedAt() == null ? "" : u.getCreatedAt().format(DATE_FORMAT)))
                    .append('\n');
        }
        writeUtf8(path, sb.toString());
    }

    public void exportUsersPdf(Path path, List<User> users) {
        List<String> subtitle = List.of(
                "Date: " + LocalDateTime.now().format(DATE_FORMAT),
                "Total utilisateurs: " + users.size()
        );
        List<String> headers = List.of("ID", "Nom", "Prenom", "Email", "Telephone", "Role", "Statut", "Cree le");
        int[] widths = {35, 68, 68, 126, 70, 52, 58, 58};
        List<List<String>> rows = new ArrayList<>();
        for (User u : users) {
            rows.add(List.of(
                    String.valueOf(u.getId()),
                    safe(u.getNom()),
                    safe(u.getPrenom()),
                    safe(u.getEmail()),
                    safe(u.getNumTel()),
                    u.getRole() == null ? "" : u.getRole().name(),
                    u.getStatus() == null ? "" : u.getStatus().name(),
                    u.getCreatedAt() == null ? "" : u.getCreatedAt().format(DATE_FORMAT)
            ));
        }
        writePdfTable(path, "FinTrust - Export Utilisateurs", subtitle, headers, widths, rows);
    }

    public void exportAnalyticsCsv(Path path) {
        Map<UserSegmentType, Integer> segments = analyticsService.getSegmentCounters();
        OtpAnalyticsSnapshot otp = analyticsService.getOtpAnalytics();
        List<FailedLoginUser> failed = analyticsService.getTopFailedLogins(10);

        StringBuilder sb = new StringBuilder();
        sb.append("Section,Cle,Valeur\n");
        for (Map.Entry<UserSegmentType, Integer> e : orderedSegments(segments).entrySet()) {
            sb.append("UserSegments,").append(e.getKey().name()).append(',').append(e.getValue()).append('\n');
        }
        sb.append("OTP,RequestsTotal,").append(otp.totalRequests()).append('\n');
        sb.append("OTP,RequestsSuccess,").append(otp.successfulRequests()).append('\n');
        sb.append("OTP,RequestsFailed,").append(otp.failedRequests()).append('\n');
        sb.append("OTP,ValidationsTotal,").append(otp.totalValidations()).append('\n');
        sb.append("OTP,ValidationsSuccess,").append(otp.successfulValidations()).append('\n');
        sb.append("OTP,ValidationsFailed,").append(otp.failedValidations()).append('\n');
        sb.append("OTP,ValidationSuccessRate,").append(String.format(java.util.Locale.US, "%.2f", otp.validationSuccessRate())).append('\n');
        sb.append("OTP,AverageValidationSeconds,").append(String.format(java.util.Locale.US, "%.2f", otp.averageValidationSeconds())).append('\n');

        int rank = 1;
        for (FailedLoginUser row : failed) {
            sb.append("TopFailedLogins,#").append(rank).append('-').append(csv(row.email())).append(',')
                    .append(row.failedLogins30Days()).append('\n');
            rank++;
        }

        writeUtf8(path, sb.toString());
    }

    public void exportAnalyticsPdf(Path path) {
        Map<UserSegmentType, Integer> segments = analyticsService.getSegmentCounters();
        OtpAnalyticsSnapshot otp = analyticsService.getOtpAnalytics();
        List<FailedLoginUser> failed = analyticsService.getTopFailedLogins(10);
        DashboardStats stats = fetchDashboardStats();
        List<DailyActivity> activity10Days = fetchUserActivity10Days();

        List<String> subtitle = List.of(
                "Date: " + LocalDateTime.now().format(DATE_FORMAT),
                "Dashboard analytics admin - snapshot complet"
        );
        List<String> headers = List.of("Section", "Indicateur", "Valeur");
        int[] widths = {120, 245, 150};
        List<List<String>> rows = new ArrayList<>();

        rows.add(List.of("UTILISATEURS", "Total comptes", String.valueOf(stats.totalUsers())));
        rows.add(List.of("UTILISATEURS", "Clients", String.valueOf(stats.totalClients())));
        rows.add(List.of("UTILISATEURS", "Admins", String.valueOf(stats.totalAdmins())));
        rows.add(List.of("STATUTS", "EN_ATTENTE", String.valueOf(stats.statusCount().getOrDefault(UserStatus.EN_ATTENTE, 0))));
        rows.add(List.of("STATUTS", "ACCEPTE", String.valueOf(stats.statusCount().getOrDefault(UserStatus.ACCEPTE, 0))));
        rows.add(List.of("STATUTS", "REFUSE", String.valueOf(stats.statusCount().getOrDefault(UserStatus.REFUSE, 0))));
        rows.add(List.of("STATUT DES COMPTES", "Resume", "Accepte: "
                + stats.statusCount().getOrDefault(UserStatus.ACCEPTE, 0)
                + " | En attente: " + stats.statusCount().getOrDefault(UserStatus.EN_ATTENTE, 0)
                + " | Refuse: " + stats.statusCount().getOrDefault(UserStatus.REFUSE, 0)));

        rows.add(List.of("ACTIVITE UTILISATEURS (10 JOURS)", "Date", "Inscriptions | Valides"));
        for (DailyActivity d : activity10Days) {
            rows.add(List.of(
                    "ACTIVITE_10J",
                    d.day(),
                    d.registrations() + " | " + d.validated()
            ));
        }

        for (Map.Entry<UserSegmentType, Integer> e : orderedSegments(segments).entrySet()) {
            rows.add(List.of("SEGMENTS", e.getKey().name(), String.valueOf(e.getValue())));
        }

        rows.add(List.of("OTP", "Requests total", String.valueOf(otp.totalRequests())));
        rows.add(List.of("OTP", "Requests success", String.valueOf(otp.successfulRequests())));
        rows.add(List.of("OTP", "Requests failed", String.valueOf(otp.failedRequests())));
        rows.add(List.of("OTP", "Validations total", String.valueOf(otp.totalValidations())));
        rows.add(List.of("OTP", "Validations success", String.valueOf(otp.successfulValidations())));
        rows.add(List.of("OTP", "Validations failed", String.valueOf(otp.failedValidations())));
        rows.add(List.of("OTP", "Validation success rate", String.format(java.util.Locale.US, "%.2f%%", otp.validationSuccessRate())));
        rows.add(List.of("OTP", "Average validation seconds", String.format(java.util.Locale.US, "%.2f", otp.averageValidationSeconds())));

        if (failed.isEmpty()) {
            rows.add(List.of("SECURITE", "Top failed login", "Aucun echec sur 30 jours"));
        } else {
            int i = 1;
            for (FailedLoginUser row : failed) {
                rows.add(List.of("SECURITE", "Top failed #" + i, safe(row.email()) + " (" + row.failedLogins30Days() + ")"));
                i++;
            }
        }

        writePdfTable(path, "FinTrust - Export Analytics Dashboard", subtitle, headers, widths, rows);
    }

    public void exportAuditCsv(Path path) {
        List<AuditLine> lines = fetchAuditLines(1000);
        StringBuilder sb = new StringBuilder();
        sb.append("Date,Type,UserId,Email,Canal,Succes,Raison\n");
        for (AuditLine l : lines) {
            sb.append(csv(l.createdAt())).append(',')
                    .append(csv(l.type())).append(',')
                    .append(csv(l.userId())).append(',')
                    .append(csv(l.email())).append(',')
                    .append(csv(l.channel())).append(',')
                    .append(csv(l.success())).append(',')
                    .append(csv(l.reason()))
                    .append('\n');
        }
        writeUtf8(path, sb.toString());
    }

    public void exportAuditPdf(Path path) {
        List<AuditLine> logs = fetchAuditLines(300);
        List<String> subtitle = List.of(
                "Date: " + LocalDateTime.now().format(DATE_FORMAT),
                "Lignes exportees: " + logs.size()
        );
        List<String> headers = List.of("Date", "Type", "UserId", "Email", "Canal", "Success", "Raison");
        int[] widths = {90, 70, 45, 145, 55, 50, 60};
        List<List<String>> rows = new ArrayList<>();
        for (AuditLine log : logs) {
            rows.add(List.of(
                    safe(log.createdAt()),
                    safe(log.type()),
                    safe(log.userId()),
                    safe(log.email()),
                    safe(log.channel()),
                    safe(log.success()),
                    safe(log.reason())
            ));
        }
        writePdfTable(path, "FinTrust - Export Audit Logs", subtitle, headers, widths, rows);
    }

    private List<AuditLine> fetchAuditLines(int limit) {
        String sql = """
                SELECT *
                FROM (
                    SELECT created_at,
                           'LOGIN' AS row_type,
                           user_id,
                           email,
                           '' AS channel,
                           CASE WHEN success = 1 THEN 'YES' ELSE 'NO' END AS success_text,
                           COALESCE(reason, '') AS reason
                    FROM user_login_audit
                    UNION ALL
                    SELECT created_at,
                           CONCAT('OTP_', event_type) AS row_type,
                           user_id,
                           email,
                           channel,
                           CASE WHEN success = 1 THEN 'YES' ELSE 'NO' END AS success_text,
                           COALESCE(reason, '') AS reason
                    FROM otp_audit
                ) all_rows
                ORDER BY created_at DESC
                LIMIT ?
                """;

        List<AuditLine> rows = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, Math.max(1, Math.min(limit, 5000)));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp ts = rs.getTimestamp("created_at");
                    rows.add(new AuditLine(
                            ts == null ? "" : ts.toLocalDateTime().format(DATE_FORMAT),
                            rs.getString("row_type"),
                            rs.getObject("user_id") == null ? "" : String.valueOf(rs.getInt("user_id")),
                            safe(rs.getString("email")),
                            safe(rs.getString("channel")),
                            safe(rs.getString("success_text")),
                            safe(rs.getString("reason"))
                    ));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur export audit logs: " + e.getMessage(), e);
        }
        return rows;
    }

    private Map<UserSegmentType, Integer> orderedSegments(Map<UserSegmentType, Integer> source) {
        Map<UserSegmentType, Integer> ordered = new EnumMap<>(UserSegmentType.class);
        for (UserSegmentType t : UserSegmentType.values()) {
            ordered.put(t, source.getOrDefault(t, 0));
        }
        return ordered;
    }

    private void writeUtf8(Path path, String content) {
        try {
            Files.writeString(path, content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Impossible d'ecrire le fichier: " + path, e);
        }
    }

    private void writePdfTable(Path path, String title, List<String> subtitle, List<String> headers, int[] widths, List<List<String>> rows) {
        try {
            byte[] data = TinyPdfWriter.writeStyledTableReport(title, subtitle, headers, widths, rows);
            Files.write(path, data);
        } catch (IOException e) {
            throw new RuntimeException("Impossible d'ecrire le PDF: " + path, e);
        }
    }

    private String csv(Object v) {
        String value = v == null ? "" : String.valueOf(v);
        String escaped = value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }

    private String safe(String v) {
        if (v == null) return "";
        return v.replace('\n', ' ').replace('\r', ' ').trim();
    }

    private DashboardStats fetchDashboardStats() {
        String sql = "SELECT role, status, COUNT(*) AS c FROM users GROUP BY role, status";
        int totalUsers = 0;
        int totalClients = 0;
        int totalAdmins = 0;
        Map<UserStatus, Integer> statuses = new EnumMap<>(UserStatus.class);
        statuses.put(UserStatus.EN_ATTENTE, 0);
        statuses.put(UserStatus.ACCEPTE, 0);
        statuses.put(UserStatus.REFUSE, 0);

        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String roleRaw = safe(rs.getString("role"));
                String statusRaw = safe(rs.getString("status"));
                int count = rs.getInt("c");
                totalUsers += count;
                if ("CLIENT".equalsIgnoreCase(roleRaw)) totalClients += count;
                if ("ADMIN".equalsIgnoreCase(roleRaw)) totalAdmins += count;

                try {
                    UserStatus status = UserStatus.valueOf(statusRaw);
                    statuses.put(status, statuses.getOrDefault(status, 0) + count);
                } catch (Exception ignored) {
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur lecture stats dashboard: " + e.getMessage(), e);
        }
        return new DashboardStats(totalUsers, totalClients, totalAdmins, statuses);
    }

    private List<DailyActivity> fetchUserActivity10Days() {
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(9);
        Map<LocalDate, int[]> buckets = new LinkedHashMap<>();
        for (int i = 0; i < 10; i++) {
            LocalDate day = start.plusDays(i);
            buckets.put(day, new int[]{0, 0});
        }

        String sql = """
                SELECT DATE(createdAt) AS d,
                       COUNT(*) AS registrations,
                       SUM(CASE WHEN status = 'ACCEPTE' THEN 1 ELSE 0 END) AS validated
                FROM users
                WHERE createdAt >= ?
                GROUP BY DATE(createdAt)
                ORDER BY DATE(createdAt)
                """;

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(start.atStartOfDay()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    java.sql.Date sqlDate = rs.getDate("d");
                    if (sqlDate == null) continue;
                    LocalDate day = sqlDate.toLocalDate();
                    int[] values = buckets.get(day);
                    if (values == null) continue;
                    values[0] = rs.getInt("registrations");
                    values[1] = rs.getInt("validated");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur lecture activite 10 jours: " + e.getMessage(), e);
        }

        List<DailyActivity> rows = new ArrayList<>();
        DateTimeFormatter dayFmt = DateTimeFormatter.ofPattern("dd/MM");
        for (Map.Entry<LocalDate, int[]> e : buckets.entrySet()) {
            rows.add(new DailyActivity(e.getKey().format(dayFmt), e.getValue()[0], e.getValue()[1]));
        }
        return rows;
    }

    private record AuditLine(String createdAt, String type, String userId, String email, String channel, String success, String reason) { }
    private record DashboardStats(int totalUsers, int totalClients, int totalAdmins, Map<UserStatus, Integer> statusCount) { }
    private record DailyActivity(String day, int registrations, int validated) { }

    private static final class TinyPdfWriter {

        private TinyPdfWriter() {
        }

        private static byte[] writeStyledTableReport(
                String title,
                List<String> subtitleLines,
                List<String> headers,
                int[] colWidths,
                List<List<String>> rows
        ) throws IOException {
            int rowsPerPage = 24;
            List<List<List<String>>> pages = paginateRows(rows, rowsPerPage);
            int pageCount = pages.size();
            int firstPageId = 3;
            int fontObjectId = firstPageId + pageCount * 2;
            int objectCount = fontObjectId;

            List<byte[]> objects = new ArrayList<>();
            objects.add(ascii("<< /Type /Catalog /Pages 2 0 R >>"));

            StringBuilder kids = new StringBuilder();
            for (int i = 0; i < pageCount; i++) {
                int pageId = firstPageId + i * 2;
                kids.append(pageId).append(" 0 R ");
            }
            objects.add(ascii("<< /Type /Pages /Count " + pageCount + " /Kids [" + kids + "] >>"));

            for (int i = 0; i < pageCount; i++) {
                int contentId = firstPageId + i * 2 + 1;
                String pageObj = "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] " +
                        "/Resources << /Font << /F1 " + fontObjectId + " 0 R >> >> " +
                        "/Contents " + contentId + " 0 R >>";
                objects.add(ascii(pageObj));

                String contentStream = buildTablePageContent(
                        title,
                        subtitleLines,
                        headers,
                        colWidths,
                        pages.get(i),
                        i + 1,
                        pageCount
                );
                byte[] contentBytes = contentStream.getBytes(StandardCharsets.US_ASCII);
                String prefix = "<< /Length " + contentBytes.length + " >>\nstream\n";
                String suffix = "\nendstream";
                ByteArrayOutputStream streamObj = new ByteArrayOutputStream();
                streamObj.write(prefix.getBytes(StandardCharsets.US_ASCII));
                streamObj.write(contentBytes);
                streamObj.write(suffix.getBytes(StandardCharsets.US_ASCII));
                objects.add(streamObj.toByteArray());
            }

            objects.add(ascii("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>"));

            if (objects.size() != objectCount) {
                throw new IOException("Invalid PDF object count.");
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            out.write(ascii("%PDF-1.4\n"));

            List<Integer> offsets = new ArrayList<>();
            for (int i = 0; i < objects.size(); i++) {
                int objectId = i + 1;
                offsets.add(out.size());
                out.write(ascii(objectId + " 0 obj\n"));
                out.write(objects.get(i));
                out.write(ascii("\nendobj\n"));
            }

            int xrefStart = out.size();
            out.write(ascii("xref\n0 " + (objectCount + 1) + "\n"));
            out.write(ascii("0000000000 65535 f \n"));
            for (Integer offset : offsets) {
                out.write(ascii(String.format("%010d 00000 n \n", offset)));
            }
            out.write(ascii("trailer\n<< /Size " + (objectCount + 1) + " /Root 1 0 R >>\n"));
            out.write(ascii("startxref\n" + xrefStart + "\n%%EOF"));

            return out.toByteArray();
        }

        private static List<List<List<String>>> paginateRows(List<List<String>> rows, int rowsPerPage) {
            List<List<List<String>>> pages = new ArrayList<>();
            List<List<String>> current = new ArrayList<>();
            for (List<String> row : rows) {
                if (current.size() >= rowsPerPage) {
                    pages.add(current);
                    current = new ArrayList<>();
                }
                current.add(row);
            }
            if (current.isEmpty()) {
                current.add(List.of(""));
            }
            pages.add(current);
            return pages;
        }

        private static String buildTablePageContent(
                String title,
                List<String> subtitleLines,
                List<String> headers,
                int[] colWidths,
                List<List<String>> rows,
                int pageNo,
                int totalPages
        ) {
            StringBuilder sb = new StringBuilder();
            sb.append("0.11 0.23 0.54 rg\n");
            sb.append("0 790 595 52 re f\n");

            sb.append("1 1 1 rg\n");
            sb.append("BT\n/F1 18 Tf\n40 808 Td\n");
            sb.append("(").append(escapePdfText(title)).append(") Tj\nET\n");

            sb.append("0.85 0.90 1 rg\n");
            sb.append("BT\n/F1 10 Tf\n500 808 Td\n");
            sb.append("(Page ").append(pageNo).append(" / ").append(totalPages).append(") Tj\nET\n");

            sb.append("0.78 0.85 0.95 RG\n");
            sb.append("1 w\n");
            sb.append("30 782 m 565 782 l S\n");

            int topY = 760;
            if (pageNo == 1 && subtitleLines != null) {
                int shift = 0;
                for (String meta : subtitleLines) {
                    drawText(sb, 40, topY - shift, 11, 0.13, 0.20, 0.33, meta);
                    shift += 16;
                }
                topY -= (shift + 8);
            }

            int tableX = 40;
            int tableW = 0;
            for (int w : colWidths) tableW += w;
            int headerH = 22;
            int rowH = 21;
            int rowCount = Math.max(1, rows.size());
            int tableH = headerH + rowCount * rowH;
            int tableTopY = topY;
            int tableBottomY = tableTopY - tableH;

            sb.append("0.88 0.92 0.98 rg\n");
            sb.append(tableX).append(" ").append(tableTopY - headerH).append(" ")
                    .append(tableW).append(" ").append(headerH).append(" re f\n");

            sb.append("0.74 0.82 0.93 RG\n");
            sb.append("0.8 w\n");
            sb.append(tableX).append(" ").append(tableTopY).append(" m ")
                    .append(tableX + tableW).append(" ").append(tableTopY).append(" l S\n");
            sb.append(tableX).append(" ").append(tableBottomY).append(" m ")
                    .append(tableX + tableW).append(" ").append(tableBottomY).append(" l S\n");
            sb.append(tableX).append(" ").append(tableTopY).append(" m ")
                    .append(tableX).append(" ").append(tableBottomY).append(" l S\n");
            sb.append(tableX + tableW).append(" ").append(tableTopY).append(" m ")
                    .append(tableX + tableW).append(" ").append(tableBottomY).append(" l S\n");

            int x = tableX;
            for (int i = 0; i < colWidths.length; i++) {
                if (i > 0) {
                    sb.append(x).append(" ").append(tableTopY).append(" m ")
                            .append(x).append(" ").append(tableBottomY).append(" l S\n");
                }
                drawText(sb, x + 4, tableTopY - 15, 9, 0.14, 0.23, 0.37, fit(headers.get(i), colWidths[i], 9));
                x += colWidths[i];
            }

            for (int r = 0; r <= rowCount; r++) {
                int y = tableTopY - headerH - (r * rowH);
                sb.append(tableX).append(" ").append(y).append(" m ")
                        .append(tableX + tableW).append(" ").append(y).append(" l S\n");
            }

            for (int r = 0; r < rowCount; r++) {
                List<String> row = rows.get(r);
                int cellX = tableX;
                for (int c = 0; c < colWidths.length; c++) {
                    String cell = c < row.size() ? row.get(c) : "";
                    int textY = tableTopY - headerH - (r * rowH) - 14;
                    drawText(sb, cellX + 4, textY, 8.8, 0.08, 0.14, 0.24, fit(cell, colWidths[c], 8));
                    cellX += colWidths[c];
                }
            }

            return sb.toString();
        }

        private static void drawText(StringBuilder sb, int x, int y, double size, double r, double g, double b, String text) {
            sb.append(r).append(" ").append(g).append(" ").append(b).append(" rg\n");
            sb.append("BT\n/F1 ").append(size).append(" Tf\n");
            sb.append(x).append(" ").append(y).append(" Td\n");
            sb.append("(").append(escapePdfText(text)).append(") Tj\n");
            sb.append("ET\n");
        }

        private static String fit(String value, int width, int fontSize) {
            String text = value == null ? "" : value;
            int max = Math.max(3, (width - 8) / Math.max(4, fontSize - 2));
            if (text.length() <= max) return text;
            return text.substring(0, Math.max(0, max - 1)) + "~";
        }

        private static String escapePdfText(String value) {
            String text = value == null ? "" : value;
            return text
                    .replace("\\", "\\\\")
                    .replace("(", "\\(")
                    .replace(")", "\\)")
                    .replaceAll("[^\\x20-\\x7E]", "?");
        }

        private static byte[] ascii(String text) {
            return text.getBytes(StandardCharsets.US_ASCII);
        }
    }
}