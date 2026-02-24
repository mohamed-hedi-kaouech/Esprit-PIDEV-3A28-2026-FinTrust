package org.example.Service.PublicationService;

import org.example.Model.Publication.Feedback;
import org.example.Model.Publication.FeedbackStats;
import org.example.Model.Publication.FeedbackTrendPoint;
import org.example.Model.Publication.GlobalFeedbackStats;
import org.example.Model.Publication.Publication;
import org.example.Utils.MaConnexion;

import java.sql.*;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Service de gestion des Feedbacks
 */
public class FeedbackService {

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
}

