package org.example.Controlleurs.AuthControlleur;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.Service.UserService.PasswordResetResult;
import org.example.Service.UserService.UserService;

import java.net.URL;

public class PasswordResetController {
    @FXML
    private TextField emailField;
    @FXML
    private TextField codeField;
    @FXML
    private PasswordField newPasswordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Label codeLabel;
    @FXML
    private Label messageLabel;

    private final UserService userService = new UserService();

    @FXML
    private void initialize() {
        if (messageLabel != null) {
            messageLabel.setText("Reinitialisation disponible par EMAIL uniquement.");
            messageLabel.setStyle("-fx-text-fill: #1d6b34;");
        }
    }

    @FXML
    private void handleSendCode() {
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        PasswordResetResult result = userService.requestPasswordResetCode(email, UserService.RESET_BY_EMAIL);
        if (!result.isSuccess()) {
            messageLabel.setText(result.getMessage());
            messageLabel.setStyle("-fx-text-fill: #cc2e2e;");
            codeLabel.setText("Code non envoye.");
            return;
        }

        codeLabel.setText("Code envoye avec succes. Verifiez votre email.");
        messageLabel.setText(result.getMessage());
        messageLabel.setStyle("-fx-text-fill: #1d6b34;");
    }

    @FXML
    private void handleResetPassword() {
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        String code = codeField.getText() == null ? "" : codeField.getText().trim();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        PasswordResetResult result = userService.resetPassword(email, code, newPassword, confirmPassword);
        if (!result.isSuccess()) {
            messageLabel.setText(result.getMessage());
            messageLabel.setStyle("-fx-text-fill: #cc2e2e;");
            return;
        }

        messageLabel.setText("Mot de passe reinitialise avec succes. Redirection vers connexion...");
        messageLabel.setStyle("-fx-text-fill: #1d6b34;");
        navigateTo("/Auth/Login.fxml", "Connexion", "/Styles/StyleWallet.css");
    }

    private void navigateTo(String fxmlPath, String title, String stylesheetPath) {
        try {
            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                messageLabel.setText("FXML introuvable: " + fxmlPath);
                return;
            }

            Parent root = FXMLLoader.load(fxmlUrl);
            Scene scene = new Scene(root);

            if (stylesheetPath != null && !stylesheetPath.isBlank()) {
                URL cssUrl = getClass().getResource(stylesheetPath);
                if (cssUrl != null) {
                    scene.getStylesheets().add(cssUrl.toExternalForm());
                }
            }

            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Erreur navigation: " + e.getMessage());
        }
    }
}
