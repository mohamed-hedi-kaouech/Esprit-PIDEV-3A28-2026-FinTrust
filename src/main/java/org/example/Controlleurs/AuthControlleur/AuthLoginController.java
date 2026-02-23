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
import org.example.Service.AuditService.AuditService;
import org.example.Service.UserService.LoginResult;
import org.example.Service.UserService.UserService;
import org.example.Utils.SessionContext;

import java.net.URL;

public class AuthLoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    private UserService userService;
    private AuditService auditService;

    @FXML
    private void initialize() {
        try {
            userService = new UserService();
        } catch (Exception e) {
            e.printStackTrace();
            showMessage("Base de donnees indisponible. Verifiez MySQL (localhost:3306 / PIDEV).", true);
        }
        try {
            auditService = new AuditService();
        } catch (Exception ignored) {
            auditService = null;
        }
    }

    @FXML
    private void handleLogin() {
        String email = emailField != null ? emailField.getText() : "";

        if (userService == null) {
            auditLogin(null, email, false, "db_unavailable");
            showMessage("Connexion impossible: base de donnees indisponible.", true);
            return;
        }

        String password = passwordField != null ? passwordField.getText() : "";

        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            auditLogin(null, email, false, "empty_credentials");
            showMessage("Veuillez saisir votre email et votre mot de passe.", true);
            return;
        }

        LoginResult result;
        try {
            result = userService.login(email, password);
        } catch (Exception e) {
            e.printStackTrace();
            auditLogin(null, email, false, "login_exception");
            showMessage("Erreur lors de la connexion: " + e.getMessage(), true);
            return;
        }

        if (result == null || !result.isSuccess() || result.getUser() == null) {
            auditLogin(null, email, false, result != null ? result.getMessage() : "login_failed");
            showMessage(result != null ? result.getMessage() : "Connexion impossible.", true);
            return;
        }

        User loggedUser = result.getUser();
        auditLogin(loggedUser.getId(), loggedUser.getEmail(), true, "ok");

        SessionContext session = SessionContext.getInstance();
        session.setCurrentUser(loggedUser);

        if (loggedUser.getRole() == UserRole.CLIENT) {
            KycStatus st = result.getKycStatus() != null ? result.getKycStatus() : KycStatus.EN_ATTENTE;
            session.setCurrentKycStatus(st);
            session.setCurrentKycComment(result.getKycComment());
        } else {
            session.setCurrentKycStatus(null);
            session.setCurrentKycComment(null);
        }

        if (loggedUser.getRole() == UserRole.ADMIN) {
            navigateTo("/Admin/UserDashboard.fxml", "Dashboard Admin", "/Styles/StyleWallet.css");
            return;
        }

        navigateTo("/Client/ClientDashboard.fxml", "Dashboard Client", "/Styles/StyleWallet.css");
    }

    private void auditLogin(Integer userId, String email, boolean success, String reason) {
        if (auditService == null) return;
        try {
            auditService.logLoginAttempt(userId, email, success, reason);
        } catch (Exception ignored) {
        }
    }

    @FXML
    private void goToSignup() {
        navigateTo("/Auth/Signup.fxml", "Inscription", "/Styles/StyleWallet.css");
    }

    @FXML
    private void goToPasswordReset() {
        navigateTo("/Auth/PasswordReset.fxml", "Reinitialisation mot de passe", "/Styles/StyleWallet.css");
    }

    private void showMessage(String text, boolean isError) {
        if (messageLabel == null) return;
        messageLabel.setText(text == null ? "" : text);
        messageLabel.setStyle(isError ? "-fx-text-fill: #cc2e2e;" : "-fx-text-fill: #1d6b34;");
    }

    private void navigateTo(String fxmlPath, String title, String stylesheetPath) {
        try {
            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                showMessage("FXML introuvable: " + fxmlPath, true);
                System.err.println("FXML introuvable: " + fxmlPath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            Scene scene = new Scene(root);

            if (stylesheetPath != null && !stylesheetPath.isBlank()) {
                URL cssUrl = getClass().getResource(stylesheetPath);
                if (cssUrl != null) {
                    scene.getStylesheets().add(cssUrl.toExternalForm());
                } else {
                    System.err.println("CSS introuvable: " + stylesheetPath);
                }
            }

            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            Throwable root = e;
            while (root.getCause() != null) root = root.getCause();
            String details = root.getClass().getSimpleName() + (root.getMessage() != null ? (": " + root.getMessage()) : "");
            showMessage("Erreur navigation: " + details, true);
        }
    }
}
