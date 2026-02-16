package org.example.Service.UserService;

import org.example.Interfaces.InterfaceGlobal;
import org.example.Model.User.Kyc;
import org.example.Utils.MaConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class KycService implements InterfaceGlobal<Kyc> {

    private final Connection cnx;

    public KycService() {
        cnx = MaConnexion.getInstance().getCnx();
    }

    // ================= ADD =================
    @Override
    public boolean Add(Kyc kyc) {
        String sql = """
                INSERT INTO kyc(userId, documentType, documentNumberHash, lastFourDigits,
                                documentFront, documentBack, signature, selfie, status, submittedAt)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement pst = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setInt(1, kyc.getUserId());
            pst.setString(2, kyc.getDocumentType());
            pst.setString(3, kyc.getDocumentNumberHash());
            pst.setString(4, kyc.getLastFourDigits());
            pst.setBytes(5, kyc.getDocumentFront());
            pst.setBytes(6, kyc.getDocumentBack());
            pst.setBytes(7, kyc.getSignature());
            pst.setBytes(8, kyc.getSelfie());
            pst.setString(9, kyc.getStatus());
            pst.setTimestamp(10, Timestamp.valueOf(kyc.getSubmittedAt()));

            int rows = pst.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = pst.getGeneratedKeys()) {
                    if (keys.next()) {
                        kyc.setId(keys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ================= DELETE =================
    @Override
    public boolean Delete(Integer id) {
        String sql = "DELETE FROM kyc WHERE id=?";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setInt(1, id);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ================= UPDATE =================
    @Override
    public void Update(Kyc kyc) {
        String sql = """
                UPDATE kyc
                SET documentType=?, documentNumberHash=?, lastFourDigits=?, 
                    documentFront=?, documentBack=?, signature=?, selfie=?, 
                    status=?, reviewedAt=?, comment=?
                WHERE id=?
                """;
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setString(1, kyc.getDocumentType());
            pst.setString(2, kyc.getDocumentNumberHash());
            pst.setString(3, kyc.getLastFourDigits());
            pst.setBytes(4, kyc.getDocumentFront());
            pst.setBytes(5, kyc.getDocumentBack());
            pst.setBytes(6, kyc.getSignature());
            pst.setBytes(7, kyc.getSelfie());
            pst.setString(8, kyc.getStatus());
            pst.setTimestamp(9, kyc.getReviewedAt() != null ? Timestamp.valueOf(kyc.getReviewedAt()) : null);
            pst.setString(10, kyc.getComment());
            pst.setInt(11, kyc.getId());

            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ================= READ ALL =================
    @Override
    public List<Kyc> ReadAll() {
        List<Kyc> list = new ArrayList<>();
        String sql = "SELECT * FROM kyc";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(map(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ================= READ BY ID =================
    @Override
    public Kyc ReadId(Integer id) {
        String sql = "SELECT * FROM kyc WHERE id=?";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ================= READ BY USER =================
    public List<Kyc> findByUserId(int userId) {
        List<Kyc> list = new ArrayList<>();
        String sql = "SELECT * FROM kyc WHERE userId=?";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setInt(1, userId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ================= UTILS =================
    private Kyc map(ResultSet rs) throws SQLException {
        Kyc kyc = new Kyc();
        kyc.setId(rs.getInt("id"));
        kyc.setUserId(rs.getInt("userId"));
        kyc.setDocumentType(rs.getString("documentType"));
        kyc.setDocumentNumberHash(rs.getString("documentNumberHash"));
        kyc.setLastFourDigits(rs.getString("lastFourDigits"));
        kyc.setDocumentFront(rs.getBytes("documentFront"));
        kyc.setDocumentBack(rs.getBytes("documentBack"));
        kyc.setSignature(rs.getBytes("signature"));
        kyc.setSelfie(rs.getBytes("selfie"));
        kyc.setStatus(rs.getString("status"));
        kyc.setSubmittedAt(rs.getTimestamp("submittedAt").toLocalDateTime());
        Timestamp reviewedAt = rs.getTimestamp("reviewedAt");
        kyc.setReviewedAt(reviewedAt != null ? reviewedAt.toLocalDateTime() : null);
        kyc.setComment(rs.getString("comment"));
        return kyc;
    }
}
