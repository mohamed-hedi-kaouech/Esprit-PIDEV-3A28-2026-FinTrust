package org.example.Controlleurs.LoanControlleur;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.Model.Loan.LoanClass.Loan;
import org.example.Model.Loan.LoanClass.Repayment;
import org.example.Model.Loan.LoanEnum.LoanStatus;
import org.example.Model.Loan.LoanEnum.LoanType;
import org.example.Service.LoanService.LoanService;
import org.example.Service.LoanService.RepaymentService;

import java.io.IOException;
import java.util.List;

public class SimulatorController {

    @FXML private TextField amountField;
    @FXML private TextField ageField;
    @FXML private TextField incomeField;
    @FXML private Slider durationSlider;

    @FXML private Label durationValueLabel;
    @FXML private Label maxAmountLabel;
    @FXML private Label rateLabel;
    @FXML private Label maxDurationLabel;

    private final LoanService loanService = new LoanService();

    private LoanType loanType;

    public void setLoanType(LoanType type) {

        this.loanType = type;

        maxAmountLabel.setText(type.getMaxAmount() + " DT");
        rateLabel.setText(type.getInterestRate() + " %");
        maxDurationLabel.setText(type.getMaxYears() + " ans");

        durationSlider.setMax(type.getMaxMonths());
    }

    @FXML
    public void initialize() {

        durationValueLabel.textProperty().bind(
                durationSlider.valueProperty().asString("Durée sélectionnée : %.0f mois")
        );
    }

    @FXML
    private void handleSimulation() {

        try {

            double amount = Double.parseDouble(
                    amountField.getText().replace(" ", "")
            );

            double income = Double.parseDouble(
                    incomeField.getText().replace(" ", "")
            );

            int age = Integer.parseInt(ageField.getText());
            int duration = (int) durationSlider.getValue();

            if (loanType == null) {
                showError("Type de prêt non sélectionné.");
                return;
            }

            if (!loanType.isValidAmount(amount)) {
                showError("Montant dépasse le plafond autorisé.");
                return;
            }

            if (!loanType.isValidDuration(duration)) {
                showError("Durée dépasse la limite autorisée.");
                return;
            }

            if (age < 18 || age + (duration / 12.0) > 65) {
                showError("Condition d'âge non respectée.");
                return;
            }

            Loan loan = new Loan(loanType, amount, duration);

            double monthly = loanService.calculateMonthlyPayment(loan);

            if (monthly > income * 0.4) {
                showError("Mensualité dépasse 40% du revenu.");
                return;
            }

            // REDIRECTION PROPRE
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/Loan/LoanPreview.fxml")
            );

            Parent root = loader.load();

            LoanPreviewController controller = loader.getController();
            controller.setLoan(loan);

            Stage stage = (Stage) amountField.getScene().getWindow();
            stage.setScene(new Scene(root));

        } catch (NumberFormatException e) {
            showError("Veuillez entrer des valeurs numériques valides.");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur lors de la simulation.");
        }
    }
    @FXML
    private void handleBack() {

        try {

            Parent root = FXMLLoader.load(
                    getClass().getResource("/Loan/LoanListUser.fxml")
            );

            Stage stage = (Stage) amountField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Mes prêts");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void selectHousing() {
        setLoanType(LoanType.HOUSING);
    }

    @FXML
    private void selectCar() {
        setLoanType(LoanType.CAR);
    }

    @FXML
    private void selectPersonal() {
        setLoanType(LoanType.PERSONAL);
    }

    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg).showAndWait();
    }
}

