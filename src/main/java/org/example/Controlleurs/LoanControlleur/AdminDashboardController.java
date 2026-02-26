package org.example.Controlleurs.LoanControlleur;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.example.Model.Loan.LoanClass.Loan;
import org.example.Model.Loan.LoanEnum.LoanStatus;
import org.example.Service.LoanService.LoanService;

import java.util.List;

public class AdminDashboardController {

    @FXML private Label totalLoansLabel;
    @FXML private Label activeLoansLabel;
    @FXML private Label pendingLoansLabel;
    @FXML private Label completedLoansLabel;

    private final LoanService loanService = new LoanService();

    @FXML
    public void initialize() {
        loadStatistics();
    }

    @FXML
    // =============================
    // LOAD DASHBOARD STATISTICS
    // =============================
    private void loadStatistics() {

        List<Loan> loans = loanService.ReadAll();

        int total = loans.size();
        int active = 0;
        int pending = 0;
        int completed = 0;

        for (Loan loan : loans) {

            if (loan.getStatus() == LoanStatus.ACTIVE)
                active++;

            if (loan.getStatus() == LoanStatus.PENDING)
                pending++;

            if (loan.getStatus() == LoanStatus.COMPLETED)
                completed++;
        }

        totalLoansLabel.setText(String.valueOf(total));
        activeLoansLabel.setText(String.valueOf(active));
        pendingLoansLabel.setText(String.valueOf(pending));
        completedLoansLabel.setText(String.valueOf(completed));
    }

    // =============================
    // NAVIGATION
    // =============================

    @FXML
    private void goToLoanList() throws Exception {

        Parent root = FXMLLoader.load(
                getClass().getResource("/Loan/AdminLoanList.fxml"));

        Stage stage = (Stage) totalLoansLabel.getScene().getWindow();
        stage.setScene(new Scene(root));
    }

    @FXML
    private void goToPaymentHistory() throws Exception {

        Parent root = FXMLLoader.load(
                getClass().getResource("/Loan/LoanHistoryPayment.fxml"));

        Stage stage = (Stage) totalLoansLabel.getScene().getWindow();
        stage.setScene(new Scene(root));
    }

    @FXML
    private void handleLogout() throws Exception {

        Parent root = FXMLLoader.load(
                getClass().getResource("/MenuGUI.fxml"));

        Stage stage = (Stage) totalLoansLabel.getScene().getWindow();
        stage.setScene(new Scene(root));
    }
}