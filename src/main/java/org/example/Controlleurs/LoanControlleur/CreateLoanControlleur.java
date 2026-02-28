package org.example.Controlleurs.LoanControlleur;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.Model.Loan.LoanClass.Loan;
import org.example.Model.Loan.LoanEnum.LoanStatus;
import org.example.Service.LoanService.LoanService;

public class CreateLoanControlleur {

    @FXML private TextField amountField;
    @FXML private TextField durationField;
    @FXML private Label monthlyLabel;

    private LoanService service = new LoanService();

    private double interestRate = 5.0; // Fixed system rate
    private double calculatedMonthly = 0;

    @FXML
    private void handleCalculate() {

        try {
            double amount = Double.parseDouble(amountField.getText());
            int duration = Integer.parseInt(durationField.getText());

            if (amount <= 0 || duration <= 0) {
                showError("Valeurs invalides.");
                return;
            }

            // Simple academic formula
            double total = amount + (amount * interestRate / 100);
            calculatedMonthly = total / duration;

            monthlyLabel.setText(
                    String.format("Mensualité : %.2f DT", calculatedMonthly)
            );

        } catch (NumberFormatException e) {
            showError("Veuillez entrer des valeurs numériques valides.");
        }
    }
    public void prefillData(double amount, int duration) {
        amountField.setText(String.valueOf(amount));
        durationField.setText(String.valueOf(duration));
    }

    @FXML
    private void handleCreate() {

        try {
            double amount = Double.parseDouble(amountField.getText());
            int duration = Integer.parseInt(durationField.getText());

            if (calculatedMonthly == 0) {
                showError("Veuillez d'abord calculer la mensualité.");
                return;
            }

            Loan loan = new Loan(amount, duration, interestRate, amount);
            loan.setStatus(LoanStatus.ACTIVE);

            service.Add(loan);

            showSuccess("Prêt créé avec succès !");
            goBackToList();

        } catch (NumberFormatException e) {
            showError("Erreur de saisie.");
        }
    }

    private void goBackToList() {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/Loan/LoanList.fxml")
            );

            Stage stage = (Stage) amountField.getScene().getWindow();
            stage.setScene(new Scene(root));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void showSuccess(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}