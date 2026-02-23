package org.example.Repository;

import org.example.Model.Kyc.Kyc;
import org.example.Model.Kyc.KycFile;
import org.example.Model.Kyc.KycStatus;
import org.example.Service.KycService.KycAdminRow;
import org.example.Utils.MaConnexion;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class KycRepository {
    private static final String TEMP_CIN_PREFIX = "TMP-KYC-";

    private final Connection cnx;

    public KycRepository() {
        this.cnx = MaConnexion.getInstance().getCnx();
    }

    public Optional<Kyc> findByUserId(int userId) {
        String sql = "SELECT id, user_id, cin, adresse, date_naissance, statut, commentaire_admin, date_submission FROM kyc WHERE user_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapKyc(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur recherche KYC user", e);
        }
    }

    public Kyc createIfMissing(int userId) {
        Optional<Kyc> existing = findByUserId(userId);
        if (existing.isPresent()) {
            return existing.get();
        }

        String sql = "INSERT INTO kyc(user_id, cin, adresse, date_naissance, statut, commentaire_admin, date_submission) VALUES(?, ?, ?, ?, 'EN_ATTENTE', NULL, NOW())";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.setString(2, buildTempCin(userId));
            ps.setString(3, "");
            ps.setDate(4, Date.valueOf(LocalDate.of(1970, 1, 1)));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    Kyc kyc = new Kyc();
                    kyc.setId(keys.getInt(1));
                    kyc.setUserId(userId);
                    kyc.setCin(buildTempCin(userId));
                    kyc.setAdresse("");
                    kyc.setDateNaissance(LocalDate.of(1970, 1, 1));
                    kyc.setStatut(KycStatus.EN_ATTENTE);
                    kyc.setCommentaireAdmin(null);
                    kyc.setDateSubmission(LocalDateTime.now());
                    return kyc;
                }
            }
            throw new RuntimeException("Impossible de creer KYC");
        } catch (SQLException e) {
            throw new RuntimeException("Erreur creation KYC", e);
        }
    }

    public boolean existsByCinExceptUser(String cin, int userId) {
        String sql = "SELECT COUNT(*) FROM kyc WHERE cin = ? AND user_id <> ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, cin);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur verification unicite CIN", e);
        }
    }

    public Kyc saveOrUpdateKyc(int userId,
                               String cin,
                               String adresse,
                               LocalDate dateNaissance,
                               KycStatus status,
                               String commentaireAdmin,
                               LocalDateTime submissionAt) {
        createIfMissing(userId);
        String sql = "UPDATE kyc SET cin=?, adresse=?, date_naissance=?, statut=?, commentaire_admin=?, date_submission=? WHERE user_id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, cin);
            ps.setString(2, adresse);
            ps.setDate(3, Date.valueOf(dateNaissance));
            ps.setString(4, status.name());
            ps.setString(5, commentaireAdmin);
            ps.setTimestamp(6, Timestamp.valueOf(submissionAt));
            ps.setInt(7, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur update KYC", e);
        }
        return findByUserId(userId).orElseThrow();
    }

    public List<Kyc> findAllWithUser() {
        String sql = "SELECT id, user_id, cin, adresse, date_naissance, statut, commentaire_admin, date_submission FROM kyc ORDER BY date_submission DESC";
        List<Kyc> list = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapKyc(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur liste KYC", e);
        }
    }

    public List<KycAdminRow> findAllAdminRows() {
        String sql = """
                SELECT k.id as kyc_id, u.id as user_id, CONCAT(u.nom,' ',u.prenom) as nom_complet,
                       u.email, k.cin, k.date_naissance, k.statut, k.commentaire_admin, k.date_submission,
                       COUNT(f.id) as files_count
                FROM kyc k
                JOIN users u ON u.id = k.user_id
                LEFT JOIN kyc_files f ON f.kyc_id = k.id
                GROUP BY k.id, u.id, u.nom, u.prenom, u.email, k.cin, k.date_naissance, k.statut, k.commentaire_admin, k.date_submission
                ORDER BY k.date_submission DESC
                """;
        List<KycAdminRow> rows = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Timestamp dateSubmission = rs.getTimestamp("date_submission");
                Date dateNaissance = rs.getDate("date_naissance");
                rows.add(new KycAdminRow(
                        rs.getInt("kyc_id"),
                        rs.getInt("user_id"),
                        rs.getString("nom_complet"),
                        rs.getString("email"),
                        rs.getString("cin"),
                        dateNaissance == null ? null : dateNaissance.toLocalDate(),
                        KycStatus.valueOf(rs.getString("statut")),
                        rs.getString("commentaire_admin"),
                        dateSubmission == null ? null : dateSubmission.toLocalDateTime(),
                        rs.getInt("files_count")
                ));
            }
            return rows;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur liste admin KYC", e);
        }
    }

    public List<KycFile> findFilesByKycId(int kycId) {
        String sql = "SELECT id, kyc_id, file_name, file_type, file_size, file_data, updated_at FROM kyc_files WHERE kyc_id = ? ORDER BY updated_at DESC";
        List<KycFile> files = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, kycId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    files.add(mapFile(rs));
                }
            }
            return files;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lecture fichiers KYC", e);
        }
    }

    public Optional<KycFile> findFileById(int fileId) {
        String sql = "SELECT id, kyc_id, file_name, file_type, file_size, file_data, updated_at FROM kyc_files WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, fileId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapFile(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lecture fichier KYC", e);
        }
    }

    public void upsertKycFile(int kycId, String fileName, String fileType, long fileSize, byte[] fileData) {
        String sql = """
                INSERT INTO kyc_files(kyc_id, file_name, file_path, file_type, file_size, file_data, updated_at)
                VALUES(?, ?, NULL, ?, ?, ?, NOW())
                ON DUPLICATE KEY UPDATE
                    file_type=VALUES(file_type),
                    file_size=VALUES(file_size),
                    file_data=VALUES(file_data),
                    updated_at=NOW()
                """;
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, kycId);
            ps.setString(2, fileName);
            ps.setString(3, fileType);
            ps.setLong(4, fileSize);
            ps.setBytes(5, fileData);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur upsert fichier KYC", e);
        }
    }

    public void deleteKycFileById(int fileId) {
        String sql = "DELETE FROM kyc_files WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, fileId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur suppression fichier KYC", e);
        }
    }

    public void updateKycStatusByAdmin(int kycId, KycStatus status, String commentaireAdmin) {
        String sql = "UPDATE kyc SET statut=?, commentaire_admin=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setString(2, commentaireAdmin);
            ps.setInt(3, kycId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur update statut KYC", e);
        }
    }

    public Optional<Kyc> findById(int kycId) {
        String sql = "SELECT id, user_id, cin, adresse, date_naissance, statut, commentaire_admin, date_submission FROM kyc WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, kycId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapKyc(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findById KYC", e);
        }
    }

    public static boolean isTempCin(String cin) {
        return cin != null && cin.startsWith(TEMP_CIN_PREFIX);
    }

    private static String buildTempCin(int userId) {
        return TEMP_CIN_PREFIX + userId;
    }

    private Kyc mapKyc(ResultSet rs) throws SQLException {
        Kyc k = new Kyc();
        k.setId(rs.getInt("id"));
        k.setUserId(rs.getInt("user_id"));
        k.setCin(rs.getString("cin"));
        k.setAdresse(rs.getString("adresse"));
        Date dob = rs.getDate("date_naissance");
        k.setDateNaissance(dob == null ? null : dob.toLocalDate());
        k.setStatut(KycStatus.valueOf(rs.getString("statut")));
        k.setCommentaireAdmin(rs.getString("commentaire_admin"));
        Timestamp ts = rs.getTimestamp("date_submission");
        k.setDateSubmission(ts == null ? LocalDateTime.now() : ts.toLocalDateTime());
        return k;
    }

    private KycFile mapFile(ResultSet rs) throws SQLException {
        KycFile f = new KycFile();
        f.setId(rs.getInt("id"));
        f.setKycId(rs.getInt("kyc_id"));
        f.setFileName(rs.getString("file_name"));
        f.setFileType(rs.getString("file_type"));
        f.setFileSize(rs.getLong("file_size"));
        f.setFileData(rs.getBytes("file_data"));
        Timestamp ts = rs.getTimestamp("updated_at");
        f.setUpdatedAt(ts == null ? null : ts.toLocalDateTime());
        return f;
    }
}
