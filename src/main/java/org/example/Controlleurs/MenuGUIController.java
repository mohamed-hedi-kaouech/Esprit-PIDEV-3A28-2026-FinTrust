package org.example.Controlleurs;

import org.example.Controlleurs.PublicationControlleur.ListPubController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MenuGUIController implements Initializable {

    @FXML
    private Label userNameLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (userNameLabel != null) {
            userNameLabel.setText("Administrateur");
        }
    }

    @FXML
    private void goToProduct() {
        try {
            navigateToScene("/Product/ListeProductGUI.fxml", "Gestion des Produits");
        } catch (IOException e) {
            showErrorAlert("Erreur de Navigation",
                    "Impossible d'acceder au module Gestion Produits.\n" + e.getMessage());
        }
    }

    @FXML
    private void goToBudget() {
        try {
            navigateToScene("/Budget/CategorieListeGUI.fxml", "Gestion des Budgets");
        } catch (IOException e) {
            showErrorAlert("Erreur de Navigation",
                    "Impossible d'acceder au module Gestion Budgets.\n" + e.getMessage());
        }
    }

    @FXML
    private void goToLoan() {
    }

    @FXML
    private void goToWallet() {
        try {
            navigateToScene("/Wallet/CreateWalletGUI.fxml", "Gestion des Wallets");
        } catch (IOException e) {
            showErrorAlert("Erreur de Navigation",
                    "Impossible d'acceder au module Gestion Wallets.\n" + e.getMessage());
        }
    }

    @FXML
    private void goToPublication() {
        try {
            URL url = getClass().getResource("/Publication/ListePub.fxml");
            if (url == null) {
                throw new IOException("FXML introuvable: /Publication/ListePub.fxml");
            }

            Parent root = FXMLLoader.load(url);

            Stage stage = (Stage) userNameLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Gestion des Publications");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Erreur de Navigation",
                    "Impossible d'accéder au module Gestion Publications.\n" + e.getMessage());
        }
    }


    private void navigateToScene(String fxmlPath, String title) throws IOException {
        URL url = getClass().getResource(fxmlPath);
        if (url == null) {
            throw new IOException("FXML resource not found: " + fxmlPath);
        }
        Parent root = new FXMLLoader(url).load();

        javafx.stage.Window window = null;
        if (userNameLabel != null && userNameLabel.getScene() != null) {
            window = userNameLabel.getScene().getWindow();
        }
        if (window == null) {
            for (javafx.stage.Window w : javafx.stage.Window.getWindows()) {
                if (w.isShowing()) {
                    window = w;
                    break;
                }
            }
        }
        if (window == null) {
            throw new IOException("No active window available to show scene");
        }

        Stage stage = (Stage) window;
        stage.setScene(new Scene(root));
        stage.setTitle(title);
        stage.show();
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
