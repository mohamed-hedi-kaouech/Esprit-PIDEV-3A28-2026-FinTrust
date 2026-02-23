package org.example.Controlleurs.AuthControlleur;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.Model.Kyc.KycStatus;
import org.example.Model.User.User;
import org.example.Model.User.UserRole;
import org.example.Service.UserService.LoginResult;
import org.example.Service.UserService.UserService;
import org.example.Utils.SessionContext;

import java.io.IOException;
import java.net.URL;

public class AuthLoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    private final UserService userService = new UserService();

    @FXML
    private void handleLogin() {
        String email = (emailField != null) ? emailField.getText() : "";
        String password = (passwordField != null) ? passwordField.getText() : "";

        if (email.isBlank() || password.isBlank()) {
            showMessage("Veuillez saisir votre email et votre mot de passe.", true);
            return;
        }

        LoginResult result = userService.login(email, password);

        if (result == null || !result.isSuccess()) {
            String msg = (result != null) ? result.getMessage() : "Erreur inconnue lors de la connexion.";
            showMessage(msg, true);
            return;
        }

        User loggedUser = result.getUser();
        if (loggedUser == null) {
            showMessage("Erreur: utilisateur introuvable.", true);
            return;
        }

        // Session
        SessionContext.getInstance().setCurrentUser(loggedUser);
        SessionContext.getInstance().setCurrentKycStatus(result.getKycStatus());
        SessionContext.getInstance().setCurrentKycComment(result.getKycComment());

        // Navigation selon rôle/KYC
        if (loggedUser.getRole() == UserRole.ADMIN) {
            navigateTo("/Admin/UserDashboard.fxml", "Dashboard Admin", "/Styles/StyleWallet.css");
            return;
        }

        if (result.getKycStatus() == KycStatus.APPROUVE) {
            navigateTo("/Wallet/dashboard.fxml", "Dashboard Client", "/Styles/StyleWallet.css");
        } else {
            navigateTo("/Client/ClientDashboard.fxml", "Dashboard Client", "/Styles/StyleWallet.css");
        }
    }

    @FXML
    private void goToSignup() {
        navigateTo("/Auth/Signup.fxml", "Inscription", "/Styles/StyleWallet.css");
    }

    @FXML
    private void goToPasswordReset() {
        navigateTo("/Auth/PasswordReset.fxml", "Réinitialisation mot de passe", "/Styles/StyleWallet.css");
    }

    private void showMessage(String text, boolean isError) {
        if (messageLabel == null) return;

        messageLabel.setText(text == null ? "" : text);
        messageLabel.setStyle(isError
                ? "-fx-text-fill: #cc2e2e;"
                : "-fx-text-fill: #1d6b34;");
    }

    private void navigateTo(String fxml, String title, String stylesheet) {
        try {
            URL fxmlUrl = getClass().getResource(fxml);
            if (fxmlUrl == null) {
                showMessage("FXML introuvable: " + fxml, true);
                return;
            }

            Parent root = FXMLLoader.load(fxmlUrl);
            Scene scene = new Scene(root);

            if (stylesheet != null && !stylesheet.isBlank()) {
                URL cssUrl = getClass().getResource(stylesheet);
                if (cssUrl != null) {
                    scene.getStylesheets().add(cssUrl.toExternalForm());
                } else {
                    // pas bloquant
                    System.err.println("CSS introuvable: " + stylesheet);
                }
            }

            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            showMessage("Erreur lors de la navigation: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }
}
