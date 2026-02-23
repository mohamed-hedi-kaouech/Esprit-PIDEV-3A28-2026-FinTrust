package org.example.Controlleurs.LoanControlleur;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.Model.Loan.LoanClass.Repayment;
import org.example.Model.Loan.LoanEnum.RepaymentStatus;
import org.example.Service.LoanService.RepaymentService;

public class CreateRepaymentController {

    @FXML private TextField numberField;
    @FXML private TextField amountField;
    @FXML private TextField capitalField;
    @FXML private TextField interestField;

    private int loanId;

    private RepaymentService service = new RepaymentService();

    // Receive loanId from previous page
    public void setLoanId(int loanId) {
        this.loanId = loanId;
    }

    @FXML
    private void handleSave() {

        if (numberField.getText().isEmpty() ||
                amountField.getText().isEmpty() ||
                capitalField.getText().isEmpty() ||
                interestField.getText().isEmpty()) {

            showError("Tous les champs sont obligatoires.");
            return;
        }

        try {

            int number = Integer.parseInt(numberField.getText());
            double amount = Double.parseDouble(amountField.getText());
            double capital = Double.parseDouble(capitalField.getText());
            double interest = Double.parseDouble(interestField.getText());

            Repayment repayment = new Repayment(
                    loanId,
                    number,
                    amount,
                    capital,
                    interest,
                    RepaymentStatus.UNPAID
            );

            service.Add(repayment);

            showSuccess("Remboursement ajouté !");
            goBackToList();

        } catch (NumberFormatException e) {
            showError("Valeurs numériques invalides.");
        }
    }

    private void goBackToList() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/Loan/RepaymentList.fxml")
            );

            Parent root = loader.load();

            RepaymentListController controller = loader.getController();
            controller.setLoanId(loanId);

            Stage stage = (Stage) numberField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Remboursements");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBack() {
        goBackToList();
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