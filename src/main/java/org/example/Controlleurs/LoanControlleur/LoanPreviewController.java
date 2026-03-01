package org.example.Controlleurs.LoanControlleur;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.example.Model.Loan.LoanClass.Loan;
import org.example.Model.Loan.LoanClass.Repayment;
import org.example.Model.Loan.LoanEnum.LoanStatus;
import org.example.Service.LoanService.LoanService;
import org.example.Service.LoanService.RepaymentService;

import java.util.List;

public class LoanPreviewController {

    @FXML private Label typeLabel;
    @FXML private Label amountLabel;
    @FXML private Label durationLabel;
    @FXML private Label rateLabel;
    @FXML private Label monthlyLabel;
    @FXML private Label totalLabel;

    @FXML private TableView<Repayment> repaymentTable;
    @FXML private TableColumn<Repayment, Integer> colMonth;
    @FXML private TableColumn<Repayment, Double> colStarting;
    @FXML private TableColumn<Repayment, Double> colCapital;
    @FXML private TableColumn<Repayment, Double> colInterest;
    @FXML private TableColumn<Repayment, Double> colRemaining;

    private Loan loan;

    private final LoanService loanService = new LoanService();
    private final RepaymentService repaymentService = new RepaymentService();

    // =========================
    // INITIALIZE TABLE
    // =========================
    @FXML
    public void initialize() {

        colMonth.setCellValueFactory(new PropertyValueFactory<>("month"));
        colStarting.setCellValueFactory(new PropertyValueFactory<>("startingBalance"));
        colCapital.setCellValueFactory(new PropertyValueFactory<>("capitalPart"));
        colInterest.setCellValueFactory(new PropertyValueFactory<>("interestPart"));
        colRemaining.setCellValueFactory(new PropertyValueFactory<>("remainingBalance"));

        formatMoneyColumn(colStarting);
        formatMoneyColumn(colCapital);
        formatMoneyColumn(colInterest);
        formatMoneyColumn(colRemaining);
    }

    private void formatMoneyColumn(TableColumn<Repayment, Double> column) {
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null :
                        String.format("%.2f DT", value));
            }
        });
    }

    // =========================
    // RECEIVE LOAN
    // =========================
    public void setLoan(Loan loan) {

        this.loan = loan;

        typeLabel.setText(loan.getLoanType().name());
        amountLabel.setText(String.format("%.2f DT", loan.getAmount()));
        durationLabel.setText(loan.getDuration() + " mois");
        rateLabel.setText(loan.getInterestRate() + " %");

        double monthly = loanService.calculateMonthlyPayment(loan);
        double total = monthly * loan.getDuration();

        monthlyLabel.setText(String.format("%.2f DT", monthly));
        totalLabel.setText(String.format("%.2f DT", total));

        // 🔥 GENERATE PLAN BEFORE CONFIRMATION
        List<Repayment> plan = loanService.generateRepaymentPlan(loan);
        repaymentTable.getItems().setAll(plan);
    }

    // =========================
    // CONFIRM
    // =========================
    @FXML
    private void handleConfirm() {

        if (loan == null) {
            showError("Aucun prêt à confirmer.");
            return;
        }

        try {

            loan.setStatus(LoanStatus.PENDING);

            loanService.Add(loan);

            // Optional but clean
            Loan savedLoan = loanService.ReadId(loan.getLoanId());

            List<Repayment> plan =
                    loanService.generateRepaymentPlan(savedLoan);

            for (Repayment r : plan) {
                repaymentService.Add(r);
            }

            showSuccess("Demande Prêt Ajouter avec succès ! ");
            goToLoanList();

        } catch (Exception e) {
            e.printStackTrace();   // 🔥 show real error in console
            showError("Erreur confirmation.");
        }
    }

    // =========================
    // BACK
    // =========================
    @FXML
    private void handleBack() {

        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/Loan/Simulator.fxml")
            );

            Stage stage = (Stage) typeLabel.getScene().getWindow();
            stage.setScene(new Scene(root));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void goToLoanList() {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/Loan/LoanListUser.fxml")
            );

            Stage stage = (Stage) typeLabel.getScene().getWindow();
            stage.setScene(new Scene(root));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void showSuccess(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}