package org.example.Controlleurs.WalletControlleur;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.Model.Wallet.Wallet;
import org.example.Service.WalletService.WalletService;

import java.io.IOException;

public class WalletManagerGUI {

    @FXML
    private TextField nomProprietaireField;

    @FXML
    private TextField soldeField;

    @FXML
    private ComboBox<String> deviseComboBox;

    @FXML
    private ComboBox<String> statutComboBox;

    // ===== CREATE WALLET =====
    public void CreateWallet(ActionEvent actionEvent) {

        if (validateInput()) {
            try {
                WalletService ws = new WalletService();

                Wallet wallet = new Wallet();
                wallet.setNomProprietaire(nomProprietaireField.getText());
                wallet.setSolde(Double.parseDouble(soldeField.getText()));
                wallet.setDevise(deviseComboBox.getValue());
                wallet.setStatut(statutComboBox.getValue());

                ws.Add(wallet);

                showAlert(Alert.AlertType.INFORMATION,
                        "Succès",
                        "Wallet ajouté avec succès !");

                handleClear();

            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR,
                        "Erreur de saisie",
                        "Le solde doit être un nombre valide.");
            }
        }
    }

    @FXML
    private void goBackToMenu(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/MenuGUI.fxml")
            );
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Manager");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // ===== VALIDATION =====
    private boolean validateInput() {

        StringBuilder errors = new StringBuilder();

        if (nomProprietaireField.getText().isEmpty()) {
            errors.append("- Le nom du propriétaire est obligatoire.\n");
        }

        if (soldeField.getText().isEmpty()) {
            errors.append("- Le solde est obligatoire.\n");
        } else {
            try {
                double solde = Double.parseDouble(soldeField.getText());
                if (solde < 0) {
                    errors.append("- Le solde doit être positif.\n");
                }
            } catch (NumberFormatException e) {
                errors.append("- Le solde doit être un nombre valide.\n");
            }
        }

        if (deviseComboBox.getValue() == null) {
            errors.append("- Veuillez sélectionner une devise.\n");
        }

        if (statutComboBox.getValue() == null) {
            errors.append("- Veuillez sélectionner un statut.\n");
        }

        if (errors.length() > 0) {
            showAlert(Alert.AlertType.ERROR,
                    "Erreur de validation",
                    errors.toString());
            return false;
        }

        return true;
    }

    // ===== ALERT =====
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ===== CLEAR =====
    public void handleClear(ActionEvent actionEvent) {
        handleClear();
    }

    public void handleClear() {
        nomProprietaireField.clear();
        soldeField.clear();
        deviseComboBox.setValue(null);
        statutComboBox.setValue(null);
    }
}
