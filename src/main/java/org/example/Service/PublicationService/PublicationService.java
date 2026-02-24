package org.example.Service.PublicationService;

import org.example.Utils.MaConnexion;
import org.example.Model.Publication.Publication;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Iterator;

public class PublicationService {

    private Connection cnx;
    private boolean isConnected = false;

    // In-memory offline store for UI when DB is unavailable
    private static final List<Publication> offlineStore = new ArrayList<>();
    private static final AtomicInteger offlineId = new AtomicInteger(0);

    // Constructeur
    public PublicationService() {
        cnx = MaConnexion.getInstance().getCnx();
        isConnected = cnx != null;
        // Try to sync any offline entries on startup if possible
        if (!isConnected) {
            // attempt to re-obtain connection via MaConnexion.getInstance()
            cnx = MaConnexion.getInstance().getCnx();
            isConnected = cnx != null;
        }
        if (isConnected) {
            syncOfflineToDb();
        }
    }

    // ================= CREATE =================
    public boolean create(Publication p) {
        // Try to refresh connection before deciding offline
        if (!isConnected) {
            cnx = MaConnexion.getInstance().getCnx();
            isConnected = cnx != null;
        }

        if (!isConnected) {
            // Offline fallback: assign a temporary id and store in-memory so UI can function
            int id = offlineId.incrementAndGet();
            p.setIdPublication(id);
            offlineStore.add(p);
            System.out.println("Publication stored offline with id=" + id);
            return true;
        }
        String sql = "INSERT INTO publication (titre, contenu, categorie, statut, est_visible, date_publication) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pst = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pst.setString(1, p.getTitre());
            pst.setString(2, p.getContenu());
            pst.setString(3, p.getCategorie());
            pst.setString(4, p.getStatut());
            pst.setBoolean(5, p.isEstVisible());
            // Ensure datePublication is set
            if (p.getDatePublication() == null) {
                p.setDatePublication(java.time.LocalDateTime.now());
            }
            pst.setTimestamp(6, Timestamp.valueOf(p.getDatePublication()));

            int affectedRows = pst.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = pst.getGeneratedKeys()) {
                    if (rs.next()) {
                        p.setIdPublication(rs.getInt(1));
                    }
                }
                System.out.println("Publication ajoutée : " + p.getTitre());
                // After a successful DB insert, attempt to flush any offline entries
                syncOfflineToDb();
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // On SQL exception, fallback to offline store so UI remains responsive
            int id = offlineId.incrementAndGet();
            p.setIdPublication(id);
            offlineStore.add(p);
            System.err.println("Failed to insert to DB; stored offline id=" + id);
            return true;
        }

        return false;
    }

    /**
     * Attempt to persist offline stored publications to the database.
     * This will try to obtain a connection and insert each offline item.
     */
    public void syncOfflineToDb() {
        if (offlineStore.isEmpty()) return;
        // Ensure connection is available
        if (cnx == null) {
            cnx = MaConnexion.getInstance().getCnx();
            isConnected = cnx != null;
        }
        if (!isConnected) return;

        Iterator<Publication> it = offlineStore.iterator();
        while (it.hasNext()) {
            Publication p = it.next();
            String sql = "INSERT INTO publication (titre, contenu, categorie, statut, est_visible, date_publication) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pst = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pst.setString(1, p.getTitre());
                pst.setString(2, p.getContenu());
                pst.setString(3, p.getCategorie());
                pst.setString(4, p.getStatut());
                pst.setBoolean(5, p.isEstVisible());
                if (p.getDatePublication() != null) {
                    pst.setTimestamp(6, Timestamp.valueOf(p.getDatePublication()));
                } else {
                    pst.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
                }

                int affectedRows = pst.executeUpdate();
                if (affectedRows > 0) {
                    try (ResultSet rs = pst.getGeneratedKeys()) {
                        if (rs.next()) {
                            p.setIdPublication(rs.getInt(1));
                        }
                    }
                    it.remove();
                    System.out.println("Flushed offline publication to DB: " + p.getTitre());
                }
            } catch (SQLException ex) {
                // stop on first failure to avoid tight loop; will retry later
                ex.printStackTrace();
                break;
            }
        }
    }

    // ================= READ BY ID =================
    public Publication find(int id) {
        if (!isConnected) {
            for (Publication p : offlineStore) {
                if (p.getIdPublication() == id) return p;
            }
            System.err.println("DB not available: find() returning null.");
            return null;
        }
        String sql = "SELECT * FROM publication WHERE id_publication = ?";

        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setInt(1, id);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPublication(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // ================= READ ALL =================
    public List<Publication> findAll() {
        List<Publication> list = new ArrayList<>();
        if (!isConnected) {
            // Return a copy of offline store for UI
            return new ArrayList<>(offlineStore);
        }
        String sql = "SELECT * FROM publication";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapResultSetToPublication(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    // ================= UPDATE =================
    public boolean update(Publication p) {
        if (!isConnected) {
            for (int i = 0; i < offlineStore.size(); i++) {
                if (offlineStore.get(i).getIdPublication() == p.getIdPublication()) {
                    offlineStore.set(i, p);
                    return true;
                }
            }
            return false;
        }
        String sql = "UPDATE publication SET titre=?, contenu=?, categorie=?, statut=?, est_visible=? " +
                "WHERE id_publication=?";

        try (PreparedStatement pst = cnx.prepareStatement(sql)) {

            pst.setString(1, p.getTitre());
            pst.setString(2, p.getContenu());
            pst.setString(3, p.getCategorie());
            pst.setString(4, p.getStatut());
            pst.setBoolean(5, p.isEstVisible());
            pst.setInt(6, p.getIdPublication());

            return pst.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // ================= DELETE =================
    public boolean delete(int id) {
        if (!isConnected) {
            return offlineStore.removeIf(p -> p.getIdPublication() == id);
        }
        String sql = "DELETE FROM publication WHERE id_publication = ?";

        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setInt(1, id);
            return pst.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // ================= MÉTHODES SPÉCIFIQUES =================

    public boolean changeVisibility(int idPublication, boolean visible) {
        if (!isConnected) {
            for (Publication p : offlineStore) {
                if (p.getIdPublication() == idPublication) {
                    p.setEstVisible(visible);
                    return true;
                }
            }
            return false;
        }
        String sql = "UPDATE publication SET est_visible = ? WHERE id_publication = ?";

        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setBoolean(1, visible);
            pst.setInt(2, idPublication);
            return pst.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public List<Publication> findByCategorie(String categorie) {
        List<Publication> list = new ArrayList<>();
        if (!isConnected) {
            for (Publication p : offlineStore) {
                if (categorie != null && categorie.equals(p.getCategorie())) list.add(p);
            }
            return list;
        }
        String sql = "SELECT * FROM publication WHERE categorie = ?";

        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setString(1, categorie);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToPublication(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<Publication> findVisiblePublications() {
        List<Publication> list = new ArrayList<>();
        if (!isConnected) {
            for (Publication p : offlineStore) {
                if (p.isEstVisible()) list.add(p);
            }
            return list;
        }
        String sql = "SELECT * FROM publication WHERE est_visible = true";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapResultSetToPublication(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    // ================= MAPPING =================
    private Publication mapResultSetToPublication(ResultSet rs) throws SQLException {
        Publication p = new Publication(
                rs.getString("titre"),
                rs.getString("contenu"),
                rs.getString("categorie"),
                rs.getString("statut"),
                rs.getBoolean("est_visible"),
                rs.getTimestamp("date_publication").toLocalDateTime()
        );

        p.setIdPublication(rs.getInt("id_publication"));
        return p;
    }
}
