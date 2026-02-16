package org.example.Controlleurs;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class MenuGUIController implements Initializable {

    @FXML
    private Label userNameLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize user info
        // You can load this from a session or user context
        if (userNameLabel != null) {
            userNameLabel.setText("Administrateur");
        }
    }

    /**
     * Navigate to Product Management
     */
    @FXML
    private void goToProduct() {
        try {
            navigateToScene("/Product/ListeProductGUI.fxml", "Gestion des Produits");
        } catch (IOException e) {
            showErrorAlert("Erreur de Navigation",
                    "Impossible d'accéder au module Gestion Produits.\n" + e.getMessage());
        }
    }


    /**
     * Navigate to Budget Management
     */
    @FXML
    private void goToBudget() {

    }

    /**
     * Navigate to Loan Management
     */
    @FXML
    private void goToLoan() {

    }

    /**
     * Navigate to Wallet Management
     */
    @FXML
    private void goToWallet() {
        try {
            navigateToScene("/Wallet/CreateWalletGUI.fxml", "Gestion des Wallets");
        } catch (IOException e) {
            showErrorAlert("Erreur de Navigation",
                    "Impossible d'accéder au module Gestion Wallets.\n" + e.getMessage());
        }
    }

    /**
     * Navigate to Publication Management
     */
    @FXML
    private void goToPublication() {

    }

    //Generic method to navigate to a scene
    private void navigateToScene(String fxmlPath, String title) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();

        Stage stage = (Stage) userNameLabel.getScene().getWindow();
        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.setTitle(title);
        stage.show();
    }

    //Show error alert
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}