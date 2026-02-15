package org.example.Controlleurs.WalletControlleur;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.Model.Wallet.ClassWallet.Wallet;
import org.example.Model.Wallet.EnumWallet.WalletDevise;
import org.example.Model.Wallet.EnumWallet.WalletStatut;
import org.example.Service.WalletService.WalletService;

public class WalletManagerGUI {

    @FXML
    private TextField nomProprietaireField;

    @FXML
    private TextField soldeField;

    @FXML
    private ComboBox<WalletDevise> deviseComboBox;

    @FXML
    private ComboBox<WalletStatut> statutComboBox;

    // ✅ IMPORTANT : remplir les ComboBox ici
    @FXML
    public void initialize() {
        deviseComboBox.getItems().setAll(WalletDevise.values());
        statutComboBox.getItems().setAll(WalletStatut.values());
    }

    // ===== CREATE WALLET =====
    public void CreateWallet(ActionEvent actionEvent) {

        if (validateInput()) {
            try {
                WalletService ws = new WalletService();

                Wallet wallet = new Wallet();
                wallet.setNomProprietaire(nomProprietaireField.getText());
                wallet.setSolde(Double.parseDouble(soldeField.getText()));

                // ✅ Maintenant on utilise les ENUM
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
