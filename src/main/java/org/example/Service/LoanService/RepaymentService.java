package org.example.Service.LoanService;

import org.example.Interfaces.InterfaceGlobal;
import org.example.Model.Loan.LoanClass.Repayment;
import org.example.Model.Loan.LoanEnum.RepaymentStatus;
import org.example.Utils.MaConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RepaymentService implements InterfaceGlobal<Repayment> {

    Connection cnx = MaConnexion.getInstance().getCnx();

    // ======================
    // CREATE
    // ======================
    @Override
    public void Add(Repayment r) {

        String req = "INSERT INTO `repayment` " +
                "(loanId, number, amount, capitalPart, interestPart, status) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement ps = cnx.prepareStatement(req);

            ps.setInt(1, r.getLoanId());
            ps.setInt(2, r.getNumber());
            ps.setDouble(3, r.getAmount());
            ps.setDouble(4, r.getCapitalPart());
            ps.setDouble(5, r.getInterestPart());
            ps.setString(6, r.getStatus().name());

            ps.executeUpdate();

            System.out.println("Repayment added successfully!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ======================
    // DELETE
    // ======================
    @Override
    public void Delete(Integer id) {

        String req = "DELETE FROM `repayment` WHERE repayId = ?";

        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("Repayment deleted successfully!");
            } else {
                System.out.println("Repayment not found.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ======================
    // UPDATE
    // ======================
    @Override
    public void Update(Repayment r) {

        String req = "UPDATE `repayment` SET " +
                "loanId = ?, number = ?, amount = ?, " +
                "capitalPart = ?, interestPart = ?, status = ? " +
                "WHERE repayId = ?";

        try {
            PreparedStatement ps = cnx.prepareStatement(req);

            ps.setInt(1, r.getLoanId());
            ps.setInt(2, r.getNumber());
            ps.setDouble(3, r.getAmount());
            ps.setDouble(4, r.getCapitalPart());
            ps.setDouble(5, r.getInterestPart());
            ps.setString(6, r.getStatus().name());
            ps.setInt(7, r.getRepayId());

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("Repayment updated successfully!");
            } else {
                System.out.println("Repayment not found.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ======================
    // READ ALL
    // ======================
    @Override
    public List<Repayment> ReadAll() {

        List<Repayment> list = new ArrayList<>();

        String req = "SELECT * FROM `repayment`";

        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(req);

            while (rs.next()) {

                Repayment r = new Repayment(
                        rs.getInt("repayId"),
                        rs.getInt("loanId"),
                        rs.getInt("number"),
                        rs.getDouble("amount"),
                        rs.getDouble("capitalPart"),
                        rs.getDouble("interestPart"),
                        RepaymentStatus.valueOf(rs.getString("status"))
                );

                list.add(r);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    // ======================
    // READ BY ID
    // ======================
    @Override
    public Repayment ReadId(Integer id) {

        Repayment r = null;
        String req = "SELECT * FROM `repayment` WHERE repayId = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                r = new Repayment(
                        rs.getInt("repayId"),
                        rs.getInt("loanId"),
                        rs.getInt("number"),
                        rs.getDouble("amount"),
                        rs.getDouble("capitalPart"),
                        rs.getDouble("interestPart"),
                        RepaymentStatus.valueOf(rs.getString("status"))
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return r;
    }

    // ======================
    // READ BY LOAN (Important Relation)
    // ======================
    public List<Repayment> getByLoan(int loanId) {

        List<Repayment> list = new ArrayList<>();

        String req = "SELECT * FROM repayment WHERE loanId = ? ORDER BY number";

        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, loanId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Repayment r = new Repayment(
                        rs.getInt("repayId"),
                        rs.getInt("loanId"),
                        rs.getInt("number"),
                        rs.getDouble("amount"),
                        rs.getDouble("capitalPart"),
                        rs.getDouble("interestPart"),
                        RepaymentStatus.valueOf(rs.getString("status"))
                );

                list.add(r);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }
}