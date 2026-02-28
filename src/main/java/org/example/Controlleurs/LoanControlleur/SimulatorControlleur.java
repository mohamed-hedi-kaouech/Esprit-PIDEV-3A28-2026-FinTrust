package org.example.Controlleurs.LoanControlleur;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class SimulatorControlleur {

    @FXML private TextField ageField;
    @FXML private TextField incomeField;
    @FXML private TextField amountField;
    @FXML private TextField durationField;

    private final double interestRate = 5.0; // Fixed system rate

    @FXML
    private void handleSimulation() {

        // ===== EMPTY CHECK =====
        if (ageField.getText().isEmpty() ||
                incomeField.getText().isEmpty() ||
                amountField.getText().isEmpty() ||
                durationField.getText().isEmpty()) {

            showError("Tous les champs sont obligatoires.");
            return;
        }

        try {
            int age = Integer.parseInt(ageField.getText());
            double income = Double.parseDouble(incomeField.getText());
            double amount = Double.parseDouble(amountField.getText());
            int duration = Integer.parseInt(durationField.getText());

            // ===== BASIC VALIDATION =====
            if (age <= 0 || income <= 0 || amount <= 0 || duration <= 0) {
                showError("Valeurs invalides.");
                return;
            }

            // ===== AGE CHECK =====
            if (age < 18) {
                showError("Âge minimum requis : 18 ans.");
                return;
            }

            if (age + (duration / 12) > 65) {
                showError("L'âge dépasse 65 ans à la fin du prêt.");
                return;
            }

            // ===== CALCULATION =====
            double total = amount + (amount * interestRate / 100);
            double monthly = total / duration;

            // ===== INCOME CAPACITY CHECK =====
            if(monthly > income * 0.6) {
                showError("Mensualité dépasse 60% du revenu.");
                return;
            }

            // ===== CONFIRMATION =====
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Simulation validée");
            confirm.setHeaderText("Éligibilité confirmée !");
            confirm.setContentText(
                    String.format("Mensualité estimée : %.2f DT\n\nContinuer ?", monthly)
            );

            if (confirm.showAndWait().get() == ButtonType.OK) {
                redirectToCreateLoan(amount, duration);
            }

        } catch (NumberFormatException e) {
            showError("Veuillez entrer des valeurs numériques valides.");
        }
    }

    @FXML
    private void handleBack() {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/Loan/LoanList.fxml")
            );

            Stage stage = (Stage) ageField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Liste des prêts");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void redirectToCreateLoan(double amount, int duration) {

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/Loan/CreateLoan.fxml")
            );

            Parent root = loader.load();

            CreateLoanControlleur controller = loader.getController();
            controller.prefillData(amount, duration);

            Stage stage = (Stage) ageField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Création du prêt");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}