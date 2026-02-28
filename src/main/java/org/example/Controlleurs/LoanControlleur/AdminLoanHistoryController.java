package org.example.Controlleurs.LoanControlleur;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.example.Model.Loan.LoanClass.Loan;
import org.example.Model.Loan.LoanClass.Repayment;
import org.example.Model.Loan.LoanEnum.LoanStatus;
import org.example.Model.Loan.LoanEnum.RepaymentStatus;
import org.example.Service.LoanService.LoanService;
import org.example.Service.LoanService.RepaymentService;
import java.util.List;

public class AdminLoanHistoryController {

    @FXML private Label loanInfoLabel;
    @FXML private Label totalPaidLabel;
    @FXML private Label remainingLabel;
    @FXML private ProgressBar progressBar;

    @FXML private TableView<Repayment> repaymentTable;
    @FXML private TableColumn<Repayment, Integer> colMonth;
    @FXML private TableColumn<Repayment, Double> colCapital;
    @FXML private TableColumn<Repayment, Double> colInterest;
    @FXML private TableColumn<Repayment, Double> colTotal;
    @FXML private TableColumn<Repayment, RepaymentStatus> colStatus;

    private final RepaymentService repaymentService = new RepaymentService();
    private Loan loan;

    @FXML
    public void initialize() {

        colMonth.setCellValueFactory(new PropertyValueFactory<>("month"));
        colCapital.setCellValueFactory(new PropertyValueFactory<>("capitalPart"));
        colInterest.setCellValueFactory(new PropertyValueFactory<>("interestPart"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("monthlyPayment"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    @FXML
    public void setLoan(Loan loan) {

        this.loan = loan;

        loanInfoLabel.setText("Loan ID: " + loan.getLoanId()
                + " | Amount: " + loan.getAmount() + " DT");

        List<Repayment> repayments =
                repaymentService.getByLoan(loan.getLoanId());

        repaymentTable.getItems().setAll(repayments);

        double totalPaid = 0;

        for (Repayment r : repayments) {
            if (r.getStatus() == RepaymentStatus.PAID) {
                totalPaid += r.getMonthlyPayment();
            }
        }

        double totalLoan = loan.getAmount();
        double remaining = totalLoan - totalPaid;

        totalPaidLabel.setText("Total Paid: " + totalPaid + " DT");
        remainingLabel.setText("Remaining: " + remaining + " DT");

        progressBar.setProgress(totalPaid / totalLoan);
    }

    @FXML
    private void handleBack() throws Exception {

        Parent root = FXMLLoader.load(
                getClass().getResource("/Loan/AdminDashboard.fxml"));

        Stage stage = (Stage) repaymentTable.getScene().getWindow();
        stage.setScene(new Scene(root));
    }
}
