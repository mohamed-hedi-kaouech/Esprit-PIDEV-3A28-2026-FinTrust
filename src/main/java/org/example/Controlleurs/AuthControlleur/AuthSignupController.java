package org.example.Controlleurs.AuthControlleur;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.Service.UserService.SignupRequest;
import org.example.Service.UserService.SignupResult;
import org.example.Service.UserService.UserService;
import org.example.Utils.SessionContext;

import java.io.IOException;

public class AuthSignupController {
    @FXML
    private TextField nomField;

    @FXML
    private TextField prenomField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField numTelField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label messageLabel;

    private final UserService userService = new UserService();

    @FXML
    private void handleSignup() {
        SignupRequest request = new SignupRequest(
                nomField.getText(),
                prenomField.getText(),
                emailField.getText(),
                numTelField.getText(),
                passwordField.getText(),
                confirmPasswordField.getText()
        );

        SignupResult result = userService.signup(request);
        if (!result.isSuccess()) {
            showMessage(result.getMessage(), true);
            return;
        }

        showMessage(result.getMessage(), false);

        SessionContext session = SessionContext.getInstance();
        session.setForceCaptchaOnNextLogin(true);
        session.setCaptchaTargetEmail(emailField == null ? null : emailField.getText());
        navigateTo("/Auth/Login.fxml", "Connexion", "/Styles/StyleWallet.css");
    }

    @FXML
    private void goToLogin() {
        navigateTo("/Auth/Login.fxml", "Connexion", "/Styles/StyleWallet.css");
    }

    private void showMessage(String text, boolean isError) {
        messageLabel.setText(text);
        messageLabel.setStyle(isError ? "-fx-text-fill: #cc2e2e;" : "-fx-text-fill: #1d6b34;");
    }

    private void navigateTo(String fxml, String title, String stylesheet) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Scene scene = new Scene(root);
            if (stylesheet != null && !stylesheet.isBlank()) {
                scene.getStylesheets().add(getClass().getResource(stylesheet).toExternalForm());
            }
            Stage stage = (Stage) nomField.getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showMessage("Erreur lors de la navigation: " + e.getMessage(), true);
        }
    }
}
