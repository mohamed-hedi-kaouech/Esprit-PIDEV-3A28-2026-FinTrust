package org.example.Service.PublicationService;

import org.example.Model.Publication.Feedback;
import org.example.Model.Publication.FeedbackStats;
import org.example.Model.Publication.FeedbackTrendPoint;
import org.example.Model.Publication.GlobalFeedbackStats;
import org.example.Model.Publication.MonthlyFeedbackStats;
import org.example.Model.Publication.Publication;
import org.example.Utils.MaConnexion;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.sql.*;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Service de gestion des Feedbacks
 */
public class FeedbackService {
    public static final String ADMIN_REPLY_TYPE = "ADMIN_REPLY";
    private static final String ADMIN_REPLY_PREFIX = "REPLY_TO:";

    private Connection cnx;
    private boolean isConnected = false;

    // offline store when DB unavailable
    private static final List<Feedback> offline = new ArrayList<>();

    public FeedbackService() {
        cnx = MaConnexion.getInstance().getCnx();
        isConnected = cnx != null;
    }

    private void ensureConnection() {
        if (cnx == null) {
            cnx = MaConnexion.getInstance().getCnx();
        }
        isConnected = cnx != null;
    }

    public boolean createFeedback(Feedback f) {
        ensureConnection();

        if (!isConnected) {
            System.out.println("❌ Pas de connexion DB !");
            return false;
        }

        String sql = "INSERT INTO feedback (id_publication, id_user, commentaire, type_reaction, date_feedback) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setInt(1, f.getIdPublication());
            pst.setInt(2, f.getIdUser());
            pst.setString(3, f.getCommentaire());
            pst.setString(4, f.getTypeReaction());
            pst.setTimestamp(5, Timestamp.valueOf(f.getDateFeedback()));

            int rows = pst.executeUpdate();
            System.out.println("✅ Feedback inséré rows=" + rows);
            return rows > 0;

        } catch (SQLException ex) {
            System.out.println("❌ ERREUR SQL INSERT FEEDBACK");
            ex.printStackTrace();
            return false;
        }
    }

    public boolean add(Feedback f) {
        return createFeedback(f);
    }

    public List<Feedback> getOffline() {
        return new ArrayList<>(offline);
    }

    // ===============================
    // Récupérer feedbacks d'une publication
    // ===============================

    public List<Feedback> getByPublication(int idPublication) {

        ensureConnection();

        List<Feedback> list = new ArrayList<>();

        // Offline fallback
        if (!isConnected) {
            for (Feedback f : offline) {
                if (f.getIdPublication() == idPublication) {
                    list.add(f);
                }
            }
            return list;
        }

        String sql = "SELECT * FROM feedback WHERE id_publication = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setInt(1, idPublication);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                Feedback f = new Feedback(
                        rs.getInt("id_feedback"),
                        rs.getInt("id_publication"),
                        rs.getInt("id_user"),
                        rs.getString("commentaire"),
                        rs.getString("type_reaction"),
                        rs.getTimestamp("date_feedback").toLocalDateTime()
                );

                list.add(f);
            }

        } catch (SQLException e) {
            System.out.println("Erreur récupération feedback : " + e.getMessage());
        }

        return list;
    }

    // ===============================
    // Récupérer le feedback d'un user pour une publication par type
    // (utile pour commenter/modifier/supprimer + noter)
    // ===============================

    public Feedback getUserFeedbackByType(int idPublication, int idUser, String typeReaction) {
        ensureConnection();

        // Offline fallback
        if (!isConnected) {
            for (int i = offline.size() - 1; i >= 0; i--) {
                Feedback f = offline.get(i);
                if (f.getIdPublication() == idPublication && f.getIdUser() == idUser) {
                    if (typeReaction.equalsIgnoreCase("RATE")) {
                        if (f.getTypeReaction() != null && f.getTypeReaction().toUpperCase().startsWith("RATE")) {
                            return f;
                        }
                    } else if (typeReaction.equalsIgnoreCase(f.getTypeReaction())) {
                        return f;
                    }
                }
            }
            return null;
        }

        String sql;
        boolean isRate = "RATE".equalsIgnoreCase(typeReaction);
        if (isRate) {
            // compat: accepte RATE et RATE:4 ...
            sql = "SELECT * FROM feedback WHERE id_publication=? AND id_user=? AND type_reaction LIKE 'RATE%' ORDER BY date_feedback DESC LIMIT 1";
        } else {
            sql = "SELECT * FROM feedback WHERE id_publication=? AND id_user=? AND type_reaction=? ORDER BY date_feedback DESC LIMIT 1";
        }

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idPublication);
            ps.setInt(2, idUser);
            if (!isRate) {
                ps.setString(3, typeReaction);
            }
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Feedback(
                        rs.getInt("id_feedback"),
                        rs.getInt("id_publication"),
                        rs.getInt("id_user"),
                        rs.getString("commentaire"),
                        rs.getString("type_reaction"),
                        rs.getTimestamp("date_feedback").toLocalDateTime()
                );
            }
        } catch (SQLException e) {
            System.out.println("Erreur getUserFeedbackByType : " + e.getMessage());
        }
        return null;
    }

    public Feedback getUserComment(int idPublication, int idUser) {
        return getUserFeedbackByType(idPublication, idUser, "COMMENT");
    }

    public Feedback getUserRating(int idPublication, int idUser) {
        return getUserFeedbackByType(idPublication, idUser, "RATE");
    }

    // ===============================
    // Supprimer un feedback user (COMMENT / RATE)
    // ===============================

    public boolean deleteUserFeedback(int idPublication, int idUser, String typeReaction) {
        ensureConnection();

        if (!isConnected) {
            if ("RATE".equalsIgnoreCase(typeReaction)) {
                return offline.removeIf(f -> f.getIdPublication() == idPublication && f.getIdUser() == idUser && f.getTypeReaction() != null && f.getTypeReaction().toUpperCase().startsWith("RATE"));
            }
            return offline.removeIf(f -> f.getIdPublication() == idPublication && f.getIdUser() == idUser && typeReaction.equalsIgnoreCase(f.getTypeReaction()));
        }

        String sql;
        boolean isRate = "RATE".equalsIgnoreCase(typeReaction);
        if (isRate) {
            sql = "DELETE FROM feedback WHERE id_publication=? AND id_user=? AND type_reaction LIKE 'RATE%'";
        } else {
            sql = "DELETE FROM feedback WHERE id_publication=? AND id_user=? AND type_reaction=?";
        }

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idPublication);
            ps.setInt(2, idUser);
            if (!isRate) {
                ps.setString(3, typeReaction);
            }
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Erreur deleteUserFeedback : " + e.getMessage());
            return false;
        }
    }

    // ===============================
    // Commentaire : ajouter OU modifier (1 commentaire par user/publication)
    // ===============================

    public boolean upsertComment(int idPublication, int idUser, String commentaire) {
        // stratégie simple : supprimer l'ancien et insérer le nouveau
        deleteUserFeedback(idPublication, idUser, "COMMENT");
        Feedback f = new Feedback(idPublication, idUser, commentaire, "COMMENT");
        return createFeedback(f);
    }

    // ===============================
    // Note : ajouter OU modifier (1 note par user/publication)
    // Stockage : type_reaction = RATE (compat avec RATE:4 existant)
    // et commentaire = "1..5"
    // ===============================

    public boolean upsertRating(int idPublication, int idUser, int note) {
        if (note < 1 || note > 5) {
            throw new IllegalArgumentException("La note doit être entre 1 et 5");
        }
        deleteUserFeedback(idPublication, idUser, "RATE");
        Feedback f = new Feedback(idPublication, idUser, String.valueOf(note), "RATE");
        return createFeedback(f);
    }

    // ===============================
    // Like / Dislike : 1 seule réaction LIKE/DISLIKE par user/publication
    // Click sur la même réaction => toggle OFF
    // ===============================

    public String getUserReaction(int idPublication, int idUser) {
        ensureConnection();

        if (!isConnected) {
            for (int i = offline.size() - 1; i >= 0; i--) {
                Feedback f = offline.get(i);
                if (f.getIdPublication() == idPublication && f.getIdUser() == idUser) {
                    if ("LIKE".equalsIgnoreCase(f.getTypeReaction()) || "DISLIKE".equalsIgnoreCase(f.getTypeReaction())) {
                        return f.getTypeReaction().toUpperCase();
                    }
                }
            }
            return null;
        }

        String sql = "SELECT type_reaction FROM feedback WHERE id_publication=? AND id_user=? AND type_reaction IN ('LIKE','DISLIKE') ORDER BY date_feedback DESC LIMIT 1";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idPublication);
            ps.setInt(2, idUser);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString(1);
            }
        } catch (SQLException e) {
            System.out.println("Erreur getUserReaction : " + e.getMessage());
        }
        return null;
    }

    private boolean deleteUserReactions(int idPublication, int idUser) {
        ensureConnection();
        if (!isConnected) {
            return offline.removeIf(f -> f.getIdPublication() == idPublication && f.getIdUser() == idUser && ("LIKE".equalsIgnoreCase(f.getTypeReaction()) || "DISLIKE".equalsIgnoreCase(f.getTypeReaction())));
        }
        String sql = "DELETE FROM feedback WHERE id_publication=? AND id_user=? AND type_reaction IN ('LIKE','DISLIKE')";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idPublication);
            ps.setInt(2, idUser);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Erreur deleteUserReactions : " + e.getMessage());
            return false;
        }
    }

    public boolean toggleReaction(int idPublication, int idUser, String reaction) {
        if (!"LIKE".equalsIgnoreCase(reaction) && !"DISLIKE".equalsIgnoreCase(reaction)) {
            throw new IllegalArgumentException("Reaction doit être LIKE ou DISLIKE");
        }

        String existing = getUserReaction(idPublication, idUser);
        // nettoyer les anciennes réactions
        deleteUserReactions(idPublication, idUser);

        // Si l'utilisateur reclique sur la même réaction => on laisse vide (toggle off)
        if (existing != null && existing.equalsIgnoreCase(reaction)) {
            return true;
        }
        return createFeedback(new Feedback(idPublication, idUser, "", reaction.toUpperCase()));
    }

    // ===============================
    // Compter les LIKE
    // ===============================

    public int countLikes(int idPublication) {

        ensureConnection();

        if (!isConnected) {
            int c = 0;
            for (Feedback f : offline) {
                if (f.getIdPublication() == idPublication && "LIKE".equalsIgnoreCase(f.getTypeReaction())) c++;
            }
            return c;
        }

        String sql = "SELECT COUNT(*) FROM feedback WHERE id_publication = ? AND type_reaction = 'LIKE'";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setInt(1, idPublication);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.out.println("Erreur count likes : " + e.getMessage());
        }

        return 0;
    }

    // ===============================
    // Compter les DISLIKE
    // ===============================

    public int countDislikes(int idPublication) {

        ensureConnection();

        if (!isConnected) {
            int c = 0;
            for (Feedback f : offline) {
                if (f.getIdPublication() == idPublication && "DISLIKE".equalsIgnoreCase(f.getTypeReaction())) c++;
            }
            return c;
        }

        String sql = "SELECT COUNT(*) FROM feedback WHERE id_publication = ? AND type_reaction = 'DISLIKE'";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setInt(1, idPublication);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.out.println("Erreur count dislikes : " + e.getMessage());
        }

        return 0;
    }

    public int countComments(int idPublication) {
        ensureConnection();

        if (!isConnected) {
            int c = 0;
            for (Feedback f : offline) {
                if (f.getIdPublication() == idPublication && "COMMENT".equalsIgnoreCase(f.getTypeReaction())) c++;
            }
            return c;
        }

        String sql = "SELECT COUNT(*) FROM feedback WHERE id_publication = ? AND type_reaction = 'COMMENT'";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idPublication);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("Erreur count comments : " + e.getMessage());
        }

        return 0;
    }

    public int countRatings(int idPublication) {
        ensureConnection();

        if (!isConnected) {
            int c = 0;
            for (Feedback f : offline) {
                String type = f.getTypeReaction() == null ? "" : f.getTypeReaction().toUpperCase();
                if (f.getIdPublication() == idPublication && type.startsWith("RATE")) c++;
            }
            return c;
        }

        String sql = "SELECT COUNT(*) FROM feedback WHERE id_publication = ? AND type_reaction LIKE 'RATE%'";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idPublication);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.out.println("Erreur count ratings : " + e.getMessage());
        }
        return 0;
    }

    public List<String> getRecentCommentTexts(int idPublication, int limit) {
        ensureConnection();
        List<String> comments = new ArrayList<>();
        int cappedLimit = Math.max(1, Math.min(limit, 50));

        if (!isConnected) {
            for (int i = offline.size() - 1; i >= 0 && comments.size() < cappedLimit; i--) {
                Feedback f = offline.get(i);
                if (f.getIdPublication() == idPublication && "COMMENT".equalsIgnoreCase(f.getTypeReaction())) {
                    String c = f.getCommentaire();
                    if (c != null && !c.isBlank()) comments.add(c.trim());
                }
            }
            return comments;
        }

        String sql = "SELECT commentaire FROM feedback WHERE id_publication=? AND type_reaction='COMMENT' " +
                "AND commentaire IS NOT NULL AND TRIM(commentaire) <> '' ORDER BY date_feedback DESC LIMIT ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idPublication);
            ps.setInt(2, cappedLimit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String c = rs.getString("commentaire");
                if (c != null && !c.isBlank()) comments.add(c.trim());
            }
        } catch (SQLException e) {
            System.out.println("Erreur getRecentCommentTexts : " + e.getMessage());
        }

        return comments;
    }

    // ===============================
    // Notes (RATE) : moyenne (optionnel mais utile pour l'UI)
    // ===============================

    public double averageRating(int idPublication) {
        ensureConnection();
        if (!isConnected) return 0.0;

        String sql = "SELECT AVG(CAST(commentaire AS DECIMAL(10,2))) FROM feedback WHERE id_publication=? AND type_reaction LIKE 'RATE%'";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idPublication);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            // si commentaire n'est pas numérique, MySQL renverra 0 sur CAST
            System.out.println("Erreur averageRating : " + e.getMessage());
        }
        return 0.0;
    }

    public boolean exportFeedbacksToCSV(int publicationId, File file) {
        ensureConnection();
        if (!isConnected || file == null) return false;

        String sql = "SELECT id_feedback, id_user, type_reaction, commentaire, date_feedback " +
                "FROM feedback WHERE id_publication=? ORDER BY date_feedback DESC";

        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), java.nio.charset.StandardCharsets.UTF_8));
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, publicationId);
            ResultSet rs = ps.executeQuery();

            // UTF-8 BOM for better Excel compatibility on Windows.
            bw.write('\uFEFF');
            // Force Excel delimiter regardless of OS locale.
            bw.write("sep=;");
            bw.newLine();
            bw.write("id_feedback;id_user;type_reaction;commentaire;date_feedback");
            bw.newLine();

            while (rs.next()) {
                String commentaire = rs.getString("commentaire");
                if (commentaire == null) commentaire = "";
                commentaire = commentaire.replace("\"", "\"\"");

                String line = rs.getInt("id_feedback") + ";" +
                        rs.getInt("id_user") + ";" +
                        safeCsv(rs.getString("type_reaction")) + ";" +
                        "\"" + commentaire + "\";" +
                        rs.getTimestamp("date_feedback");
                bw.write(line);
                bw.newLine();
            }
            return true;
        } catch (Exception e) {
            System.out.println("Erreur exportFeedbacksToCSV : " + e.getMessage());
            return false;
        }
    }

    public boolean exportPublicationReportToPDF(int publicationId, String pubTitle, File file) {
        ensureConnection();
        if (!isConnected || file == null) return false;

        int likes = 0;
        int dislikes = 0;
        int comments = 0;
        int ratingCount = 0;
        double avgRating = 0.0;
        Map<Integer, Integer> dist = getRatingDistribution(publicationId);

        String kpiSql = "SELECT " +
                "SUM(CASE WHEN type_reaction='LIKE' THEN 1 ELSE 0 END) AS likes, " +
                "SUM(CASE WHEN type_reaction='DISLIKE' THEN 1 ELSE 0 END) AS dislikes, " +
                "SUM(CASE WHEN type_reaction='COMMENT' THEN 1 ELSE 0 END) AS comments, " +
                "AVG(CASE WHEN type_reaction LIKE 'RATE%' THEN CAST(commentaire AS UNSIGNED) END) AS avg_rating, " +
                "SUM(CASE WHEN type_reaction LIKE 'RATE%' THEN 1 ELSE 0 END) AS rating_count " +
                "FROM feedback WHERE id_publication=?";

        try (PreparedStatement ps = cnx.prepareStatement(kpiSql)) {
            ps.setInt(1, publicationId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                likes = rs.getInt("likes");
                dislikes = rs.getInt("dislikes");
                comments = rs.getInt("comments");
                avgRating = rs.getDouble("avg_rating");
                ratingCount = rs.getInt("rating_count");
            }
        } catch (SQLException e) {
            System.out.println("Erreur KPI report PDF : " + e.getMessage());
        }

        double likeRatio = (likes + dislikes) == 0 ? 0.0 : (likes * 100.0 / (likes + dislikes));

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                float y = 760;
                y = writePdfLine(cs, PDType1Font.HELVETICA_BOLD, 16, 50, y,
                        sanitizePdfText("Rapport de satisfaction - Publication"));
                y = writePdfLine(cs, PDType1Font.HELVETICA_BOLD, 12, 50, y - 10,
                        sanitizePdfText("Publication #" + publicationId + " - " + (pubTitle == null ? "" : pubTitle)));
                y = writePdfLine(cs, PDType1Font.HELVETICA, 10, 50, y - 6,
                        sanitizePdfText("Genere le: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));

                y = writePdfLine(cs, PDType1Font.HELVETICA_BOLD, 12, 50, y - 20, sanitizePdfText("KPI"));
                y = writePdfLine(cs, PDType1Font.HELVETICA, 11, 50, y - 8,
                        sanitizePdfText("Likes=" + likes + " | Dislikes=" + dislikes + " | Commentaires=" + comments));
                y = writePdfLine(cs, PDType1Font.HELVETICA, 11, 50, y - 6,
                        sanitizePdfText(String.format(Locale.US,
                                "Note moyenne=%.2f/5 | Nb notes=%d | Ratio like=%.1f%%", avgRating, ratingCount, likeRatio)));

                BufferedImage statsImage = createStatsImage(
                        publicationId, pubTitle, likes, dislikes, comments, avgRating, ratingCount, likeRatio, dist
                );
                PDImageXObject statsImagePdf = LosslessFactory.createFromImage(doc, statsImage);
                float imageWidth = 500;
                float imageHeight = 240;
                float imageY = y - imageHeight - 20;
                cs.drawImage(statsImagePdf, 50, imageY, imageWidth, imageHeight);
                y = imageY - 10;

                y = writePdfLine(cs, PDType1Font.HELVETICA_BOLD, 12, 50, y - 18, sanitizePdfText("Derniers commentaires"));

                String comSql = "SELECT id_user, commentaire, date_feedback " +
                        "FROM feedback WHERE id_publication=? AND type_reaction='COMMENT' " +
                        "ORDER BY date_feedback DESC LIMIT 8";

                try (PreparedStatement ps = cnx.prepareStatement(comSql)) {
                    ps.setInt(1, publicationId);
                    ResultSet rs = ps.executeQuery();
                    while (rs.next() && y > 80) {
                        String comment = safeOneLine(rs.getString("commentaire"), 90);
                        String line = "User#" + rs.getInt("id_user") + " - " + rs.getTimestamp("date_feedback") + " : " + comment;
                        y = writePdfLine(cs, PDType1Font.HELVETICA, 9, 50, y - 6, sanitizePdfText(line));
                    }
                }
            }

            doc.save(file);
            return true;
        } catch (Exception e) {
            System.out.println("Erreur exportPublicationReportToPDF : " + e.getMessage());
            return false;
        }
    }

    public List<MonthlyFeedbackStats> getMonthlyStats(LocalDate startInclusive, LocalDate endExclusive) {
        ensureConnection();
        List<MonthlyFeedbackStats> rows = new ArrayList<>();
        if (!isConnected) return rows;
        if (startInclusive == null || endExclusive == null) return rows;

        String sql = "SELECT DATE_FORMAT(date_feedback, '%Y-%m') AS month, " +
                "SUM(CASE WHEN type_reaction='LIKE' THEN 1 ELSE 0 END) AS likes, " +
                "SUM(CASE WHEN type_reaction='DISLIKE' THEN 1 ELSE 0 END) AS dislikes, " +
                "SUM(CASE WHEN type_reaction='COMMENT' THEN 1 ELSE 0 END) AS comments, " +
                "AVG(CASE WHEN type_reaction LIKE 'RATE%' THEN CAST(commentaire AS UNSIGNED) END) AS avg_rating " +
                "FROM feedback " +
                "WHERE date_feedback >= ? AND date_feedback < ? " +
                "GROUP BY month ORDER BY month";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(startInclusive.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(endExclusive.atStartOfDay()));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                rows.add(new MonthlyFeedbackStats(
                        rs.getString("month"),
                        rs.getInt("likes"),
                        rs.getInt("dislikes"),
                        rs.getInt("comments"),
                        rs.getDouble("avg_rating")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Erreur getMonthlyStats : " + e.getMessage());
        }
        return rows;
    }

    public boolean exportMonthlyStatsToCSV(LocalDate startInclusive, LocalDate endExclusive, File file) {
        List<MonthlyFeedbackStats> rows = getMonthlyStats(startInclusive, endExclusive);
        if (file == null) return false;

        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), java.nio.charset.StandardCharsets.UTF_8))) {
            bw.write('\uFEFF');
            // Force Excel delimiter regardless of OS locale.
            bw.write("sep=;");
            bw.newLine();
            bw.write("month;likes;dislikes;comments;avg_rating");
            bw.newLine();
            for (MonthlyFeedbackStats row : rows) {
                String avg = String.format(Locale.FRANCE, "%.2f", row.getAvgRating());
                bw.write(row.getMonth() + ";" +
                        row.getLikes() + ";" +
                        row.getDislikes() + ";" +
                        row.getComments() + ";" +
                        avg);
                bw.newLine();
            }
            return true;
        } catch (IOException e) {
            System.out.println("Erreur exportMonthlyStatsToCSV : " + e.getMessage());
            return false;
        }
    }

    public GlobalFeedbackStats getGlobalStats() {
        ensureConnection();
        if (!isConnected) {
            int likes = 0;
            int dislikes = 0;
            int comments = 0;
            int ratingCount = 0;
            int ratingTotal = 0;

            for (Feedback f : offline) {
                String type = f.getTypeReaction() == null ? "" : f.getTypeReaction().toUpperCase();
                if ("LIKE".equals(type)) likes++;
                if ("DISLIKE".equals(type)) dislikes++;
                if ("COMMENT".equals(type)) comments++;
                if (type.startsWith("RATE")) {
                    Integer parsed = parseRatingValue(f);
                    if (parsed != null) {
                        ratingCount++;
                        ratingTotal += parsed;
                    }
                }
            }
            double avg = ratingCount == 0 ? 0.0 : ratingTotal / (double) ratingCount;
            return new GlobalFeedbackStats(likes, dislikes, comments, avg);
        }

        String sql = "SELECT " +
                "SUM(CASE WHEN type_reaction='LIKE' THEN 1 ELSE 0 END) AS total_likes, " +
                "SUM(CASE WHEN type_reaction='DISLIKE' THEN 1 ELSE 0 END) AS total_dislikes, " +
                "SUM(CASE WHEN type_reaction='COMMENT' THEN 1 ELSE 0 END) AS total_comments, " +
                "AVG(CASE WHEN type_reaction LIKE 'RATE%' THEN CAST(commentaire AS UNSIGNED) END) AS avg_rating " +
                "FROM feedback";

        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return new GlobalFeedbackStats(
                        rs.getInt("total_likes"),
                        rs.getInt("total_dislikes"),
                        rs.getInt("total_comments"),
                        rs.getDouble("avg_rating")
                );
            }
        } catch (SQLException e) {
            System.out.println("Erreur getGlobalStats : " + e.getMessage());
        }
        return new GlobalFeedbackStats(0, 0, 0, 0.0);
    }

    public List<FeedbackStats> getStatsByPublication() {
        ensureConnection();
        List<FeedbackStats> list = new ArrayList<>();

        if (!isConnected) {
            PublicationService publicationService = new PublicationService();
            List<Publication> publications = publicationService.findAll();

            for (Publication p : publications) {
                int likes = 0;
                int dislikes = 0;
                int comments = 0;
                int ratingCount = 0;
                int ratingTotal = 0;

                for (Feedback f : offline) {
                    if (f.getIdPublication() != p.getIdPublication()) continue;
                    String type = f.getTypeReaction() == null ? "" : f.getTypeReaction().toUpperCase();
                    if ("LIKE".equals(type)) likes++;
                    if ("DISLIKE".equals(type)) dislikes++;
                    if ("COMMENT".equals(type)) comments++;
                    if (type.startsWith("RATE")) {
                        Integer parsed = parseRatingValue(f);
                        if (parsed != null) {
                            ratingCount++;
                            ratingTotal += parsed;
                        }
                    }
                }

                double avg = ratingCount == 0 ? 0.0 : ratingTotal / (double) ratingCount;
                list.add(new FeedbackStats(p.getIdPublication(), p.getTitre(), likes, dislikes, comments, avg, ratingCount));
            }
            return list;
        }

        String sql = "SELECT " +
                "p.id_publication, " +
                "p.titre, " +
                "SUM(CASE WHEN f.type_reaction='LIKE' THEN 1 ELSE 0 END) AS likes, " +
                "SUM(CASE WHEN f.type_reaction='DISLIKE' THEN 1 ELSE 0 END) AS dislikes, " +
                "SUM(CASE WHEN f.type_reaction='COMMENT' THEN 1 ELSE 0 END) AS comments, " +
                "AVG(CASE WHEN f.type_reaction LIKE 'RATE%' THEN CAST(f.commentaire AS UNSIGNED) END) AS avg_rating, " +
                "SUM(CASE WHEN f.type_reaction LIKE 'RATE%' THEN 1 ELSE 0 END) AS rating_count " +
                "FROM publication p " +
                "LEFT JOIN feedback f ON f.id_publication = p.id_publication " +
                "GROUP BY p.id_publication, p.titre " +
                "ORDER BY p.id_publication DESC";

        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new FeedbackStats(
                        rs.getInt("id_publication"),
                        rs.getString("titre"),
                        rs.getInt("likes"),
                        rs.getInt("dislikes"),
                        rs.getInt("comments"),
                        rs.getDouble("avg_rating"),
                        rs.getInt("rating_count")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Erreur getStatsByPublication : " + e.getMessage());
        }
        return list;
    }

    public boolean createAdminReply(int idPublication, int adminUserId, int parentFeedbackId, String replyText) {
        if (replyText == null || replyText.isBlank()) {
            return false;
        }
        ensureConnection();
        int validAdminId = resolveValidUserId(adminUserId);
        if (validAdminId <= 0) {
            System.out.println("Erreur createAdminReply : aucun user valide disponible pour id_user.");
            return false;
        }
        String payload = ADMIN_REPLY_PREFIX + parentFeedbackId + "|" + replyText.trim();
        Feedback reply = new Feedback(idPublication, validAdminId, payload, ADMIN_REPLY_TYPE);
        return createFeedback(reply);
    }

    public Map<Integer, List<Feedback>> getAdminRepliesGrouped(int idPublication) {
        List<Feedback> all = getByPublication(idPublication);
        Map<Integer, List<Feedback>> grouped = new LinkedHashMap<>();
        for (Feedback f : all) {
            String type = f.getTypeReaction() == null ? "" : f.getTypeReaction().toUpperCase();
            if (!ADMIN_REPLY_TYPE.equals(type)) {
                continue;
            }
            Integer parentId = extractReplyParentId(f.getCommentaire());
            if (parentId == null) {
                continue;
            }
            grouped.computeIfAbsent(parentId, k -> new ArrayList<>()).add(f);
        }
        return grouped;
    }

    public Integer extractReplyParentId(String commentaire) {
        if (commentaire == null || !commentaire.startsWith(ADMIN_REPLY_PREFIX)) {
            return null;
        }
        int sep = commentaire.indexOf('|');
        if (sep < 0) {
            return null;
        }
        String idPart = commentaire.substring(ADMIN_REPLY_PREFIX.length(), sep).trim();
        try {
            return Integer.parseInt(idPart);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public String extractReplyBody(String commentaire) {
        if (commentaire == null) {
            return "";
        }
        if (!commentaire.startsWith(ADMIN_REPLY_PREFIX)) {
            return commentaire;
        }
        int sep = commentaire.indexOf('|');
        if (sep < 0 || sep == commentaire.length() - 1) {
            return "";
        }
        return commentaire.substring(sep + 1).trim();
    }

    private int resolveValidUserId(int preferredUserId) {
        if (!isConnected) {
            // Offline mode fallback: keep preferred ID.
            return preferredUserId;
        }
        if (userExists(preferredUserId)) {
            return preferredUserId;
        }
        return getAnyExistingUserId();
    }

    private boolean userExists(int userId) {
        if (!isConnected || userId <= 0) return false;
        String sql = "SELECT 1 FROM users WHERE id = ? LIMIT 1";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.out.println("Erreur userExists : " + e.getMessage());
            return false;
        }
    }

    private int getAnyExistingUserId() {
        if (!isConnected) return -1;
        String sql = "SELECT id FROM users ORDER BY id ASC LIMIT 1";
        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            System.out.println("Erreur getAnyExistingUserId : " + e.getMessage());
        }
        return -1;
    }

    public Map<Integer, Integer> getRatingDistribution(Integer publicationId) {
        ensureConnection();
        Map<Integer, Integer> distribution = new LinkedHashMap<>();
        for (int i = 1; i <= 5; i++) {
            distribution.put(i, 0);
        }

        if (!isConnected) {
            for (Feedback f : offline) {
                if (publicationId != null && f.getIdPublication() != publicationId) continue;
                String type = f.getTypeReaction() == null ? "" : f.getTypeReaction().toUpperCase();
                if (!type.startsWith("RATE")) continue;
                Integer parsed = parseRatingValue(f);
                if (parsed != null && parsed >= 1 && parsed <= 5) {
                    distribution.put(parsed, distribution.get(parsed) + 1);
                }
            }
            return distribution;
        }

        StringBuilder sql = new StringBuilder(
                "SELECT CAST(commentaire AS UNSIGNED) AS stars, COUNT(*) AS total " +
                "FROM feedback WHERE type_reaction LIKE 'RATE%'"
        );
        if (publicationId != null) {
            sql.append(" AND id_publication=?");
        }
        sql.append(" GROUP BY stars ORDER BY stars");

        try (PreparedStatement ps = cnx.prepareStatement(sql.toString())) {
            if (publicationId != null) {
                ps.setInt(1, publicationId);
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int stars = rs.getInt("stars");
                int total = rs.getInt("total");
                if (stars >= 1 && stars <= 5) {
                    distribution.put(stars, total);
                }
            }
        } catch (SQLException e) {
            System.out.println("Erreur getRatingDistribution : " + e.getMessage());
        }

        return distribution;
    }

    public List<FeedbackTrendPoint> getFeedbackTrendLast7Days() {
        ensureConnection();
        List<FeedbackTrendPoint> points = new ArrayList<>();

        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(6);

        Map<LocalDate, int[]> byDay = new LinkedHashMap<>();
        for (int i = 0; i < 7; i++) {
            byDay.put(start.plusDays(i), new int[]{0, 0, 0});
        }

        if (!isConnected) {
            for (Feedback f : offline) {
                LocalDate d = f.getDateFeedback().toLocalDate();
                if (d.isBefore(start) || d.isAfter(today)) continue;

                int[] arr = byDay.get(d);
                if (arr == null) continue;

                String type = f.getTypeReaction() == null ? "" : f.getTypeReaction().toUpperCase();
                if ("LIKE".equals(type)) arr[0]++;
                if ("DISLIKE".equals(type)) arr[1]++;
                if ("COMMENT".equals(type)) arr[2]++;
            }
        } else {
            String sql = "SELECT " +
                    "DATE(date_feedback) AS day, " +
                    "SUM(CASE WHEN type_reaction='LIKE' THEN 1 ELSE 0 END) AS likes, " +
                    "SUM(CASE WHEN type_reaction='DISLIKE' THEN 1 ELSE 0 END) AS dislikes, " +
                    "SUM(CASE WHEN type_reaction='COMMENT' THEN 1 ELSE 0 END) AS comments " +
                    "FROM feedback " +
                    "WHERE date_feedback >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
                    "GROUP BY day " +
                    "ORDER BY day";

            try (PreparedStatement ps = cnx.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LocalDate day = rs.getDate("day").toLocalDate();
                    int[] arr = byDay.get(day);
                    if (arr != null) {
                        arr[0] = rs.getInt("likes");
                        arr[1] = rs.getInt("dislikes");
                        arr[2] = rs.getInt("comments");
                    }
                }
            } catch (SQLException e) {
                System.out.println("Erreur getFeedbackTrendLast7Days : " + e.getMessage());
            }
        }

        for (Map.Entry<LocalDate, int[]> entry : byDay.entrySet()) {
            int[] arr = entry.getValue();
            points.add(new FeedbackTrendPoint(entry.getKey(), arr[0], arr[1], arr[2]));
        }
        return points;
    }

    private Integer parseRatingValue(Feedback f) {
        if (f == null) return null;

        if (f.getCommentaire() != null && !f.getCommentaire().isBlank()) {
            try {
                return Integer.parseInt(f.getCommentaire().trim());
            } catch (NumberFormatException ignored) {
                // pass
            }
        }

        if (f.getTypeReaction() != null && f.getTypeReaction().toUpperCase().contains(":")) {
            String[] split = f.getTypeReaction().split(":", 2);
            try {
                return Integer.parseInt(split[1].trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }

        return null;
    }

    private String safeCsv(String value) {
        if (value == null) return "";
        String clean = value.replace("\"", "\"\"");
        return "\"" + clean + "\"";
    }

    private String safeOneLine(String s, int max) {
        if (s == null) return "";
        String oneLine = s.replace("\n", " ").replace("\r", " ");
        if (oneLine.length() > max) {
            return oneLine.substring(0, max) + "...";
        }
        return oneLine;
    }

    private float writePdfLine(PDPageContentStream cs, PDType1Font font, int size, float x, float y, String text) throws IOException {
        cs.beginText();
        cs.setFont(font, size);
        cs.newLineAtOffset(x, y);
        cs.showText(text == null ? "" : text);
        cs.endText();
        return y - (size + 4);
    }

    private String sanitizePdfText(String text) {
        if (text == null) return "";
        String normalized = java.text.Normalizer.normalize(text, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return normalized.replaceAll("[^\\x20-\\x7E]", "?");
    }

    private BufferedImage createStatsImage(
            int publicationId,
            String pubTitle,
            int likes,
            int dislikes,
            int comments,
            double avgRating,
            int ratingCount,
            double likeRatio,
            Map<Integer, Integer> dist
    ) {
        int width = 1200;
        int height = 560;
        int padding = 60;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(new Color(245, 250, 255));
        g.fillRect(0, 0, width, height);

        g.setColor(new Color(20, 63, 120));
        g.setFont(new Font("SansSerif", Font.BOLD, 30));
        g.drawString("Rapport Statistique - Publication #" + publicationId, padding, 50);

        g.setColor(new Color(55, 80, 110));
        g.setFont(new Font("SansSerif", Font.PLAIN, 20));
        String safeTitle = pubTitle == null ? "" : safeOneLine(pubTitle, 70);
        g.drawString(safeTitle, padding, 82);

        g.setFont(new Font("SansSerif", Font.BOLD, 20));
        g.setColor(new Color(30, 100, 190));
        g.drawString("Likes: " + likes, padding, 130);
        g.drawString("Dislikes: " + dislikes, padding + 190, 130);
        g.drawString("Commentaires: " + comments, padding + 420, 130);

        g.setColor(new Color(70, 90, 120));
        g.setFont(new Font("SansSerif", Font.PLAIN, 18));
        g.drawString(String.format(Locale.US, "Moyenne: %.2f/5 | Notes: %d | Ratio Like: %.1f%%", avgRating, ratingCount, likeRatio), padding, 160);

        int chartX = padding;
        int chartY = 220;
        int chartW = width - 2 * padding;
        int chartH = 260;

        g.setColor(Color.WHITE);
        g.fillRoundRect(chartX, chartY, chartW, chartH, 16, 16);
        g.setColor(new Color(200, 215, 235));
        g.drawRoundRect(chartX, chartY, chartW, chartH, 16, 16);

        g.setColor(new Color(90, 110, 140));
        g.setFont(new Font("SansSerif", Font.BOLD, 18));
        g.drawString("Distribution des notes (image)", chartX + 20, chartY + 30);

        int maxValue = 1;
        for (int i = 1; i <= 5; i++) {
            maxValue = Math.max(maxValue, dist.getOrDefault(i, 0));
        }

        int graphLeft = chartX + 70;
        int graphRight = chartX + chartW - 30;
        int graphTop = chartY + 55;
        int graphBottom = chartY + chartH - 45;
        int graphHeight = graphBottom - graphTop;
        int graphWidth = graphRight - graphLeft;
        int barWidth = 90;
        int gap = (graphWidth - (barWidth * 5)) / 6;
        if (gap < 10) gap = 10;

        g.setColor(new Color(210, 220, 240));
        g.setStroke(new BasicStroke(2f));
        g.drawLine(graphLeft, graphBottom, graphRight, graphBottom);
        g.drawLine(graphLeft, graphTop, graphLeft, graphBottom);

        for (int i = 1; i <= 5; i++) {
            int value = dist.getOrDefault(i, 0);
            int x = graphLeft + gap * i + barWidth * (i - 1);
            int barHeight = (int) ((value / (double) maxValue) * (graphHeight - 10));
            int y = graphBottom - barHeight;

            g.setColor(new Color(52, 152, 219));
            g.fillRoundRect(x, y, barWidth, barHeight, 12, 12);
            g.setColor(new Color(32, 94, 170));
            g.drawRoundRect(x, y, barWidth, barHeight, 12, 12);

            g.setColor(new Color(50, 65, 85));
            g.setFont(new Font("SansSerif", Font.BOLD, 16));
            g.drawString(String.valueOf(i), x + barWidth / 2 - 4, graphBottom + 22);
            g.setFont(new Font("SansSerif", Font.PLAIN, 15));
            g.drawString(String.valueOf(value), x + barWidth / 2 - 8, y - 8);
        }

        g.dispose();
        return image;
    }
}

