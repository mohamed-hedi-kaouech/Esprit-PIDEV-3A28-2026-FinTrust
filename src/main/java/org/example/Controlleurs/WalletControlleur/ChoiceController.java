package org.example.Controlleurs.WalletControlleur;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Objects;
import javafx.scene.Node;

public class ChoiceController {

    private Stage choiceStage;

    public void setChoiceStage(Stage stage) {
        this.choiceStage = stage;
    }

    @FXML
    private void handleAdminChoice() {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(
                    getClass().getResource("/Wallet/dashboard.fxml")));

            Stage adminStage = new Stage();
            adminStage.setTitle("FinTrust - Administration");
            adminStage.setScene(new Scene(root));
            adminStage.setMaximized(true);
            adminStage.show();

            if (choiceStage != null) {
                choiceStage.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir l'interface Admin: " + e.getMessage());
        } catch (NullPointerException e) {
            e.printStackTrace();
            showAlert("Erreur", "Fichier FXML non trouvé");
        }
    }

    @FXML
    private void handleClientChoice() {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(
                    getClass().getResource("/Wallet/client_login.fxml")));

            Stage clientStage = new Stage();
            clientStage.setTitle("FinTrust - Espace Client");
            clientStage.setScene(new Scene(root));
            clientStage.setMaximized(true);
            clientStage.show();

            if (choiceStage != null) {
                choiceStage.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir l'interface Client: " + e.getMessage());
        } catch (NullPointerException e) {
            e.printStackTrace();
            showAlert("Erreur", "Fichier FXML non trouvé");
        }
    }

    @FXML
    private void handleButtonHover(MouseEvent event) {
        Button button = (Button) event.getSource();
        button.setStyle("-fx-background-color: #1e90ff; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 30; -fx-cursor: hand;");
    }

    @FXML
    private void handleButtonExit(MouseEvent event) {
        Button button = (Button) event.getSource();
        String text = button.getText();
        if (text.equals("Se connecter")) {
            button.setStyle("-fx-background-color: #0047ab; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 30; -fx-cursor: hand;");
        } else {
            button.setStyle("-fx-background-color: #003366; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 30; -fx-cursor: hand;");
        }
    }

    @FXML
    private void handleSouscrire(MouseEvent event) {
        showAlert("Souscription", "Pour souscrire à nos services, veuillez contacter votre conseiller au +216 71 123 456 ou par email: conseiller@fintrust.tn");
    }

    @FXML
    private void handleNavigation(MouseEvent event) {
        Label source = (Label) event.getSource();
        String text = source.getText();

        switch(text) {
            case "Accueil":
                // Déjà sur l'accueil
                showAlert("Navigation", "Vous êtes déjà sur la page d'accueil");
                break;
            case "À propos":
                showAlert("À propos", "FinTrust - Haute Finance Numérique\nVersion 2.0\n© 2026 Tous droits réservés\n\nUne solution bancaire innovante et sécurisée.");
                break;
            case "Sécurité":
                showAlert("Sécurité", "🔒 Sécurité de bout en bout\n\n• Authentification à deux facteurs\n• Chiffrement des données\n• Conformité aux normes internationales\n• Détection de fraude en temps réel");
                break;
            case "Aide":
                showAlert("Aide", "Besoin d'aide ?\n\n• Centre d'aide: help.fintrust.tn\n• Support: support@fintrust.tn\n• Téléphone: +216 71 123 456\n• Horaires: 8h-18h (Lun-Ven)");
                break;
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}