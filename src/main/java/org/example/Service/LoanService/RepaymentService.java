package org.example.Service.LoanService;

import org.example.Interfaces.InterfaceGlobal;
import org.example.Model.Loan.LoanClass.Repayment;
import org.example.Model.Loan.LoanEnum.RepaymentStatus;
import org.example.Utils.MaConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RepaymentService implements InterfaceGlobal<Repayment> {

    private final Connection cnx = MaConnexion.getInstance().getCnx();

    // ======================
    // CREATE
    // ======================
    @Override
    public void Add(Repayment r) {

        String sql = """
        INSERT INTO repayment
        (loanId, month, startingBalance, monthlyPayment,
         capitalPart, interestPart, remainingBalance, status)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setInt(1, r.getLoanId());
            ps.setInt(2, r.getMonth());
            ps.setDouble(3, r.getStartingBalance());
            ps.setDouble(4, r.getMonthlyPayment());
            ps.setDouble(5, r.getCapitalPart());
            ps.setDouble(6, r.getInterestPart());
            ps.setDouble(7, r.getRemainingBalance());
            ps.setString(8, r.getStatus().name());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error inserting repayment", e);
        }
    }

    // ======================
    // DELETE
    // ======================
    @Override
    public void Delete(Integer id) {

        String sql = "DELETE FROM repayment WHERE repayId = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error deleting repayment", e);
        }
    }

    // ======================
    // UPDATE
    // ======================
    @Override
    public void Update(Repayment r) {

        String sql = """
            UPDATE repayment SET
            month = ?,
            startingBalance = ?,
            monthlyPayment = ?,
            capitalPart = ?,
            interestPart = ?,
            remainingBalance = ?,
            status = ?
            WHERE repayId = ?
            """;

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setInt(1, r.getMonth());
            ps.setDouble(2, r.getStartingBalance());
            ps.setDouble(3, r.getMonthlyPayment());
            ps.setDouble(4, r.getCapitalPart());
            ps.setDouble(5, r.getInterestPart());
            ps.setDouble(6, r.getRemainingBalance());
            ps.setString(7, r.getStatus().name());
            ps.setInt(8, r.getRepayId());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error updating repayment", e);
        }
    }

    // ======================
    // READ ALL
    // ======================
    @Override
    public List<Repayment> ReadAll() {

        List<Repayment> list = new ArrayList<>();
        String sql = "SELECT * FROM repayment ORDER BY loanId, month";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error reading repayments", e);
        }

        return list;
    }

    // ======================
    // READ BY ID
    // ======================
    @Override
    public Repayment ReadId(Integer id) {

        String sql = "SELECT * FROM repayment WHERE repayId = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapRow(rs);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error reading repayment by id", e);
        }

        return null;
    }

    // ======================
    // READ BY LOAN
    // ======================
    public List<Repayment> getByLoan(int loanId) {

        List<Repayment> list = new ArrayList<>();
        String sql = "SELECT * FROM repayment WHERE loanId=? ORDER BY month";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setInt(1, loanId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(mapRow(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error reading repayments by loan", e);
        }

        return list;
    }

    // ======================
    // MARK AS PAID (SIMPLE VERSION)
    // ======================
    public void markAsPaid(int repayId) {

        String sql = "UPDATE repayment SET status = ? WHERE repayId = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setString(1, RepaymentStatus.PAID.name());
            ps.setInt(2, repayId);

            int rows = ps.executeUpdate();

            if (rows == 0) {
                throw new RuntimeException("Échéance introuvable.");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error marking repayment as paid", e);
        }
    }

    public void updateRemainingPrincipal(int loanId, double capitalPart) {

        String sql = """
        UPDATE loan
        SET remaining_principal = remaining_principal - ?
        WHERE loanId = ?
    """;

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setDouble(1, capitalPart);
            ps.setInt(2, loanId);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erreur mise à jour capital restant", e);
        }
    }


    public void updateLoanStatusIfCompleted(int loanId) {

        try {

            String checkSql = """
            SELECT COUNT(*) FROM repayment
            WHERE loanId = ? AND status = 'UNPAID'
        """;

            PreparedStatement psCheck = cnx.prepareStatement(checkSql);
            psCheck.setInt(1, loanId);

            ResultSet rs = psCheck.executeQuery();

            if (rs.next() && rs.getInt(1) == 0) {

                String updateSql = """
                UPDATE loan
                SET status = 'COMPLETED'
                WHERE loanId = ?
            """;

                PreparedStatement psUpdate = cnx.prepareStatement(updateSql);
                psUpdate.setInt(1, loanId);
                psUpdate.executeUpdate();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur mise à jour statut prêt", e);
        }
    }

    // ======================
    // MAPPER
    // ======================
    private Repayment mapRow(ResultSet rs) throws SQLException {

        Repayment r = new Repayment();

        r.setRepayId(rs.getInt("repayId"));
        r.setLoanId(rs.getInt("loanId"));
        r.setMonth(rs.getInt("month"));
        r.setStartingBalance(rs.getDouble("startingBalance"));
        r.setMonthlyPayment(rs.getDouble("monthlyPayment"));
        r.setCapitalPart(rs.getDouble("capitalPart"));
        r.setInterestPart(rs.getDouble("interestPart"));
        r.setRemainingBalance(rs.getDouble("remainingBalance"));
        r.setStatus(RepaymentStatus.valueOf(rs.getString("status")));

        return r;
    }


    public boolean canPayRepayment(int loanId, int month) {

        String sql = """
        SELECT COUNT(*) FROM repayment
        WHERE loanId = ?
        AND month < ?
        AND status = 'UNPAID'
    """;

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setInt(1, loanId);
            ps.setInt(2, month);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) == 0;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur vérification paiement", e);
        }

        return false;
    }
}
