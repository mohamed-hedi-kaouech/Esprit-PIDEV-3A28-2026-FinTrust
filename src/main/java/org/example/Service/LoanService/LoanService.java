package org.example.Service.LoanService;

import org.example.Interfaces.InterfaceGlobal;
import org.example.Model.Loan.LoanClass.Loan;
import org.example.Model.Loan.LoanEnum.LoanStatus;
import org.example.Utils.MaConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LoanService implements InterfaceGlobal<Loan> {

    Connection cnx = MaConnexion.getInstance().getCnx();

    // ======================
    // CREATE (ADD)
    // ======================
    @Override
    public void Add(Loan loan) {

        String req = "INSERT INTO loan " +
                "(amount, duration,status, interest_rate, remaining_principal, createdAt) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement ps = cnx.prepareStatement(req);

            ps.setDouble(1, loan.getAmount());
            ps.setInt(2, loan.getDuration());
            ps.setString(3, loan.getStatus().name());
            ps.setDouble(4, loan.getInterestRate());
            ps.setDouble(5, loan.getRemainingPrincipal());

            ps.setTimestamp(6, Timestamp.valueOf(loan.getCreationDate()));

            ps.executeUpdate();

            System.out.println("Loan added successfully!");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    // ======================
    // DELETE
    // ======================
    @Override
    public void Delete(Integer id) {
        String req = "DELETE FROM `loan` WHERE loanId = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Loan Supprimer avec succes");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // ======================
    // UPDATE
    // ======================
    @Override
    public void Update(Loan loan) {
        String req = "UPDATE `loan` SET amount = ?, duration = ?,status = ?, interest_rate = ?,remaining_principal = ?, createdAt = ? WHERE loanId = ?";

        try {
            PreparedStatement ps = cnx.prepareStatement(req);


            ps.setDouble(1, loan.getAmount());
            ps.setInt(2, loan.getDuration());
            ps.setString(3, loan.getStatus().name());
            ps.setDouble(4, loan.getInterestRate());
            ps.setDouble(5, loan.getRemainingPrincipal());
            ps.setTimestamp(6, Timestamp.valueOf(loan.getCreationDate()));
            ps.setInt(7, loan.getLoanId());
            ps.executeUpdate();
            System.out.println("Loan updated successfully!");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // ======================
    // READ (GET ALL)
    // ======================
    @Override
    public List<Loan> ReadAll() {
        List<Loan> loans = new ArrayList<>();
        String req = "SELECT * FROM `loan`";
        try {
            Statement st = cnx.createStatement();
            ResultSet res = st.executeQuery(req);
            while (res.next()){
                Loan l =new Loan();
                l.setLoanId(res.getInt(1));
                l.setAmount(res.getDouble(2));
                l.setDuration(res.getInt(3));
                l.setStatus(LoanStatus.valueOf(res.getString(4)));
                l.setInterestRate(res.getDouble(5));
                l.setRemainingPrincipal(res.getDouble(6));
                l.setCreationDate(res.getTimestamp(7).toLocalDateTime());
                loans.add(l);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return loans;
    }
    // READ (ONE BY ID)
    @Override
    public Loan ReadId(Integer id) {
        Loan l = new Loan();
        String req = "SELECT * FROM `loan`  WHERE loanId = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ResultSet res = ps.executeQuery();
            while (res.next()){
                l.setLoanId(res.getInt(1));
                l.setAmount(res.getDouble(2));
                l.setDuration(res.getInt(3));
                l.setStatus(LoanStatus.valueOf(res.getString(4)));
                l.setInterestRate(res.getDouble(5));
                l.setRemainingPrincipal(res.getDouble(6));
                l.setCreationDate(res.getTimestamp(7).toLocalDateTime());
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return l;
    }

    // FUNCTION AVANCE
    public void makePayment(int loanId, double paymentAmount) {

        Loan loan = ReadId(loanId);

        if (loan == null)
            throw new RuntimeException("Prêt introuvable.");

        if (loan.getStatus() == LoanStatus.COMPLETED)
            throw new RuntimeException("Prêt déjà clôturé.");

        if (paymentAmount <= 0)
            throw new RuntimeException("Montant invalide.");

        double remaining = loan.getRemainingPrincipal();

        if (paymentAmount > remaining)
            throw new RuntimeException("Montant dépasse le capital restant.");

        double newRemaining = remaining - paymentAmount;

        String req =
                "UPDATE loan SET remaining_principal=?, status=? WHERE loanId=?";

        try {
            PreparedStatement ps = cnx.prepareStatement(req);

            ps.setDouble(1, newRemaining);

            if (newRemaining == 0) {
                ps.setString(2, LoanStatus.COMPLETED.name());
            } else {
                ps.setString(2, LoanStatus.ACTIVE.name());
            }

            ps.setInt(3, loanId);

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
