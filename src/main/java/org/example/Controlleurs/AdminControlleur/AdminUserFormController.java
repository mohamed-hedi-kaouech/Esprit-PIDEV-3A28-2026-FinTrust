package org.example.Controlleurs.AdminControlleur;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.Model.User.UserRole;
import org.example.Model.User.UserStatus;
import org.example.Service.UserService.SignupResult;
import org.example.Service.UserService.UserService;
import org.example.Utils.SessionContext;

import java.io.IOException;

public class AdminUserFormController {
    @FXML
    private TextField nomField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private ComboBox<UserRole> roleCombo;

    @FXML
    private ComboBox<UserStatus> statusCombo;

    @FXML
    private Label infoLabel;

    private final UserService userService = new UserService();

    @FXML
    private void initialize() {
        if (!SessionContext.getInstance().isAdmin()) {
            Platform.runLater(() -> navigateTo("/Auth/Login.fxml", "Connexion", "/Styles/StyleWallet.css"));
            return;
        }

        roleCombo.getItems().setAll(UserRole.values());
        statusCombo.getItems().setAll(UserStatus.values());
        roleCombo.setValue(UserRole.CLIENT);
        statusCombo.setValue(UserStatus.EN_ATTENTE);
    }

    @FXML
    private void handleCreateUser() {
        SignupResult result = userService.createUserByAdmin(
                SessionContext.getInstance().getCurrentUser(),
                nomField.getText(),
                emailField.getText(),
                passwordField.getText(),
                confirmPasswordField.getText(),
                roleCombo.getValue(),
                statusCombo.getValue()
        );

        if (!result.isSuccess()) {
            setInfo(result.getMessage(), true);
            return;
        }

        setInfo(result.getMessage(), false);
        clearForm();
    }

    @FXML
    private void goToUserList() {
        navigateTo("/Admin/UserDashboard.fxml", "Liste Utilisateurs", "/Styles/StyleWallet.css");
    }

    @FXML
    private void goToMenu() {
        navigateTo("/MenuGUI.fxml", "Menu Principal", "/Styles/MenuStyle.css");
    }

    @FXML
    private void handleLogout() {
        SessionContext.getInstance().logout();
        navigateTo("/Auth/Login.fxml", "Connexion", "/Styles/StyleWallet.css");
    }

    private void clearForm() {
        nomField.clear();
        emailField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        roleCombo.setValue(UserRole.CLIENT);
        statusCombo.setValue(UserStatus.EN_ATTENTE);
    }

    private void setInfo(String message, boolean isError) {
        infoLabel.setText(message);
        infoLabel.setStyle(isError ? "-fx-text-fill: #b91c1c;" : "-fx-text-fill: #166534;");
    }

    private void navigateTo(String fxmlPath, String title, String stylesheetPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Scene scene = new Scene(root);
            if (stylesheetPath != null && !stylesheetPath.isBlank()) {
                scene.getStylesheets().add(getClass().getResource(stylesheetPath).toExternalForm());
            }
            Stage stage = (Stage) nomField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            setInfo("Erreur navigation: " + e.getMessage(), true);
        }
    }
}
