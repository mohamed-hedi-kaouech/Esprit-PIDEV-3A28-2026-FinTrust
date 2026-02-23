package org.example.Controlleurs.LoanControlleur;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.Model.Loan.LoanClass.Loan;
import org.example.Service.LoanService.LoanService;

public class UpdateLoanControlleur {

    @FXML private Label loanInfoLabel;
    @FXML private TextField paymentField;

    private Loan currentLoan;
    private LoanService service = new LoanService();

    public void setLoanData(Loan loan) {

        this.currentLoan = loan;

        loanInfoLabel.setText(
                "Prêt #" + loan.getLoanId() +
                        " | Restant: " +
                        loan.getRemainingPrincipal() + " DT"
        );
    }

    @FXML
    private void handlePayment() {

        try {
            double payment =
                    Double.parseDouble(paymentField.getText());

            service.makePayment(
                    currentLoan.getLoanId(),
                    payment
            );

            showSuccess("Paiement effectué !");
            handleBack();

        } catch (NumberFormatException e) {
            showError("Veuillez entrer un montant valide.");
        } catch (RuntimeException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void handleBack() {

        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/Loan/LoanList.fxml")
            );

            Stage stage =
                    (Stage) paymentField.getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.setTitle("Liste des prêts");

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