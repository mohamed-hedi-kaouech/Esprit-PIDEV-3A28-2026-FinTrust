package org.example.Service.BudgetService;

import org.example.Model.Budget.Alerte;
import org.example.Utils.MaConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AlerteService {

    private Connection cnx = MaConnexion.getInstance().getCnx();

    public void Add(Alerte a) {
        String req = "INSERT INTO alerte(idCategorie, message, seuil, active) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, a.getIdCategorie());
            ps.setString(2, a.getMessage());
            ps.setDouble(3, a.getSeuil());
            ps.setBoolean(4, a.isActive());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) a.setIdAlerte(rs.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void Delete(int id) {
        String req = "DELETE FROM alerte WHERE idAlerte = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void Update(Alerte a) {
        String req = "UPDATE alerte SET idCategorie=?, message=?, seuil=?, active=? WHERE idAlerte=?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, a.getIdCategorie());
            ps.setString(2, a.getMessage());
            ps.setDouble(3, a.getSeuil());
            ps.setBoolean(4, a.isActive());
            ps.setInt(5, a.getIdAlerte());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public List<Alerte> ReadAll() {
        List<Alerte> result = new ArrayList<>();
        String req = "SELECT * FROM alerte ORDER BY created_at DESC";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                Alerte a = new Alerte();
                a.setIdAlerte(rs.getInt("idAlerte"));
                a.setIdCategorie(rs.getInt("idCategorie"));
                a.setMessage(rs.getString("message"));
                a.setSeuil(rs.getDouble("seuil"));
                a.setActive(rs.getBoolean("active"));
                a.setCreatedAt(rs.getTimestamp("created_at"));
                result.add(a);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return result;
    }

    public List<Alerte> ReadByCategory(int idCategorie) {
        List<Alerte> result = new ArrayList<>();
        String req = "SELECT * FROM alerte WHERE idCategorie = ? ORDER BY created_at DESC";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, idCategorie);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Alerte a = new Alerte();
                    a.setIdAlerte(rs.getInt("idAlerte"));
                    a.setIdCategorie(rs.getInt("idCategorie"));
                    a.setMessage(rs.getString("message"));
                    a.setSeuil(rs.getDouble("seuil"));
                    a.setActive(rs.getBoolean("active"));
                    a.setCreatedAt(rs.getTimestamp("created_at"));
                    result.add(a);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return result;
    }
}
