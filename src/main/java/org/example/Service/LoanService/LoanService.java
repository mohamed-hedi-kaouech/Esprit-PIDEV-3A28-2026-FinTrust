package org.example.Service.LoanService;

import org.example.Interfaces.InterfaceGlobal;
import org.example.Model.Loan.LoanClass.Loan;
import org.example.Model.Loan.LoanClass.Repayment;
import org.example.Model.Loan.LoanEnum.LoanStatus;
import org.example.Model.Loan.LoanEnum.LoanType;
import org.example.Model.Loan.LoanEnum.RepaymentStatus;
import org.example.Utils.MaConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LoanService implements InterfaceGlobal<Loan> {

    private final Connection cnx = MaConnexion.getInstance().getCnx();

    // ======================
    // CREATE
    // ======================
    @Override
    public void Add(Loan loan) {

        String sql = """
            INSERT INTO loan
            (loanType, amount, duration, status,
             interest_rate, remaining_principal, createdAt)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement ps =
                     cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, loan.getLoanType().name());
            ps.setDouble(2, loan.getAmount());
            ps.setInt(3, loan.getDuration());
            ps.setString(4, loan.getStatus().name());
            ps.setDouble(5, loan.getInterestRate());
            ps.setDouble(6, loan.getRemainingPrincipal());
            ps.setTimestamp(7, Timestamp.valueOf(loan.getCreationDate()));

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                loan.setLoanId(rs.getInt(1));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error inserting loan", e);
        }
    }

    // ======================
    // DELETE
    // ======================
    @Override
    public void Delete(Integer id) {

        String sql = "DELETE FROM loan WHERE loanId = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error deleting loan", e);
        }
    }

    // ======================
    // UPDATE
    // ======================
    @Override
    public void Update(Loan loan) {

        String sql = """
            UPDATE loan SET
            loanType = ?,
            amount = ?,
            duration = ?,
            status = ?,
            interest_rate = ?,
            remaining_principal = ?,
            createdAt = ?
            WHERE loanId = ?
            """;

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setString(1, loan.getLoanType().name());
            ps.setDouble(2, loan.getAmount());
            ps.setInt(3, loan.getDuration());
            ps.setString(4, loan.getStatus().name());
            ps.setDouble(5, loan.getInterestRate());
            ps.setDouble(6, loan.getRemainingPrincipal());
            ps.setTimestamp(7, Timestamp.valueOf(loan.getCreationDate()));
            ps.setInt(8, loan.getLoanId());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error updating loan", e);
        }
    }

    // ======================
    // READ ALL
    // ======================
    @Override
    public List<Loan> ReadAll() {

        List<Loan> loans = new ArrayList<>();
        String sql = "SELECT * FROM loan";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                loans.add(mapRow(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error reading loans", e);
        }

        return loans;
    }

    // ======================
    // READ BY ID
    // ======================
    @Override
    public Loan ReadId(Integer id) {

        String sql = "SELECT * FROM loan WHERE loanId = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapRow(rs);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error reading loan by id", e);
        }

        return null;
    }

    // ======================
    // ACTIVATE LOAN
    // ======================
    public void activateLoan(int loanId) {

        String sql = "UPDATE loan SET status=? WHERE loanId=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setString(1, LoanStatus.ACTIVE.name());
            ps.setInt(2, loanId);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error activating loan", e);
        }
    }

    // ======================
    // MAPPER
    // ======================
    private Loan mapRow(ResultSet rs) throws SQLException {

        Loan loan = new Loan();

        loan.setLoanId(rs.getInt("loanId"));
        loan.setLoanType(
                LoanType.valueOf(rs.getString("loanType"))
        );
        loan.setAmount(rs.getDouble("amount"));
        loan.setDuration(rs.getInt("duration"));
        loan.setStatus(
                LoanStatus.valueOf(rs.getString("status"))
        );
        loan.setInterestRate(rs.getDouble("interest_rate"));
        loan.setRemainingPrincipal(rs.getDouble("remaining_principal"));
        loan.setCreationDate(
                rs.getTimestamp("createdAt").toLocalDateTime()
        );

        return loan;
    }



    // ======================
// CALCUL MENSUALITÉ
// ======================

    public double calculateMonthlyPayment(Loan loan) {

        double monthlyRate = loan.getInterestRate() / 100 / 12;

        return loan.getAmount() *
                (monthlyRate * Math.pow(1 + monthlyRate, loan.getDuration())) /
                (Math.pow(1 + monthlyRate, loan.getDuration()) - 1);
    }


// ======================
// GÉNÉRATION PLAN
// ======================

    public List<Repayment> generateRepaymentPlan(Loan loan) {

        List<Repayment> list = new ArrayList<>();

        double monthlyRate = loan.getInterestRate() / 100 / 12;
        double monthlyPayment = calculateMonthlyPayment(loan);
        double balance = loan.getAmount();

        for (int i = 1; i <= loan.getDuration(); i++) {

            double interest = balance * monthlyRate;
            double capital = monthlyPayment - interest;
            double remaining = balance - capital;

            if (remaining < 0) remaining = 0;

            Repayment repayment = new Repayment(
                    loan.getLoanId(),     // loanId
                    i,                    // month
                    balance,              // startingBalance
                    monthlyPayment,       // monthlyPayment
                    capital,              // capitalPart
                    interest,             // interestPart
                    remaining,            // remainingBalance
                    RepaymentStatus.UNPAID
            );

            list.add(repayment);

            balance = remaining;
        }

        return list;
    }


}
