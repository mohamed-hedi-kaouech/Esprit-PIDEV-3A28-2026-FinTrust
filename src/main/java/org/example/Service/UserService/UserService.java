package org.example.Service.UserService;

import org.example.Interfaces.InterfaceGlobal;
import org.example.Model.User.User;
import org.example.Utils.MaConnexion;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserService implements InterfaceGlobal<User> {

    private final Connection cnx;

    public UserService() {
        cnx = MaConnexion.getInstance().getCnx();
    }

    @Override
    public boolean Add(User u) {
        // Vérifie si l'utilisateur existe déjà
        if (existsByEmail(u.getEmail())) {
            System.out.println("Utilisateur déjà existant : " + u.getNom() + " " + u.getPrenom());
            return false;
        }

        // Assure que createdAt n'est pas null
        if (u.getCreatedAt() == null) {
            u.setCreatedAt(LocalDateTime.now());
        }

        String sql = """
            INSERT INTO users(currentKycId, nom, prenom, email, numTel, role, password, kycStatus, createdAt)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement pst = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setInt(1, u.getCurrentKycId());
            pst.setString(2, u.getNom());
            pst.setString(3, u.getPrenom());
            pst.setString(4, u.getEmail());
            pst.setString(5, u.getNumTel());
            pst.setString(6, u.getRole());
            pst.setString(7, u.getPassword());
            pst.setString(8, u.getKycStatus());
            pst.setTimestamp(9, Timestamp.valueOf(u.getCreatedAt())); // jamais null maintenant

            int rows = pst.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = pst.getGeneratedKeys()) {
                    if (keys.next()) {
                        u.setId(keys.getInt(1));
                    }
                }
                return true; // succès
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false; // échec
    }


    @Override
    public boolean Delete(Integer id) {
        String sql = "DELETE FROM users WHERE id=?";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setInt(1, id);
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void Update(User u) {
        String sql = """
                UPDATE users SET
                currentKycId=?, nom=?, prenom=?, email=?, numTel=?,
                role=?, password=?, kycStatus=?
                WHERE id=?
                """;

        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setInt(1, u.getCurrentKycId());
            pst.setString(2, u.getNom());
            pst.setString(3, u.getPrenom());
            pst.setString(4, u.getEmail());
            pst.setString(5, u.getNumTel());
            pst.setString(6, u.getRole());
            pst.setString(7, u.getPassword());
            pst.setString(8, u.getKycStatus());
            pst.setInt(9, u.getId());

            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<User> ReadAll() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users";

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

    @Override
    public User ReadId(Integer id) {
        String sql = "SELECT * FROM users WHERE id=?";
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

    // Méthode utilitaire pour vérifier si l'email existe
    public boolean existsByEmail(String email) {
        String sql = "SELECT 1 FROM users WHERE email=?";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setString(1, email);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Mapping ResultSet → User
    private User map(ResultSet rs) throws SQLException {
        User u = new User(
                rs.getInt("currentKycId"),
                rs.getString("nom"),
                rs.getString("prenom"),
                rs.getString("email"),
                rs.getString("numTel"),
                rs.getString("role"),
                rs.getString("password"),
                rs.getString("kycStatus"),
                rs.getTimestamp("createdAt").toLocalDateTime()
        );
        u.setId(rs.getInt("id"));
        return u;
    }
}
