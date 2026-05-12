package org.example.Controlleurs.AdminControlleur;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.Model.User.User;
import org.example.Model.User.UserStatus;
import org.example.Service.UserService.UserService;
import org.example.Utils.SessionContext;

import java.io.IOException;

public class AdminUserEditController {

    @FXML private TextField nomField;
    @FXML private TextField emailField;
    @FXML private TextField numTelField;
    @FXML private ComboBox<UserStatus> statusComboBox;
    @FXML private Label infoLabel;

    private User userToEdit;
    private final UserService userService = new UserService();

    @FXML
    private void initialize() {
        statusComboBox.getItems().setAll(UserStatus.values());

        if (!SessionContext.getInstance().isAdmin()) {
            navigateTo("/Auth/Login.fxml", "Connexion", "/Styles/StyleWallet.css");
            return;
        }

        User pendingUser = SessionContext.getInstance().getAdminUserBeingEdited();
        if (pendingUser != null) {
            setUserToEdit(pendingUser);
        }
    }

    public void setUserToEdit(User user) {
        this.userToEdit = user;

        if (user == null) {
            setInfo("Erreur: utilisateur null.", true);
            return;
        }

        if (nomField == null) {
            return;
        }

        nomField.setText(safe(user.getNom()));
        emailField.setText(safe(user.getEmail()));
        numTelField.setText(safe(user.getNumTel()));
        statusComboBox.setValue(user.getStatus());
    }

    @FXML
    private void handleSave() {
        if (userToEdit == null) {
            setInfo("Aucun utilisateur a modifier.", true);
            return;
        }

        String nom = nomField.getText() == null ? "" : nomField.getText().trim();
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        String tel = numTelField.getText() == null ? "" : numTelField.getText().trim();
        UserStatus status = statusComboBox.getValue();

        if (nom.isBlank() || email.isBlank()) {
            setInfo("Nom et Email sont obligatoires.", true);
            return;
        }
        if (status == null) {
            setInfo("Veuillez selectionner un statut.", true);
            return;
        }

        userToEdit.setNom(nom);
        userToEdit.setEmail(email);
        userToEdit.setNumTel(tel);
        userToEdit.setStatus(status);

        try {
            User admin = SessionContext.getInstance().getCurrentUser();
            userService.updateUserByAdmin(
                    admin,
                    userToEdit.getId(),
                    userToEdit.getNom(),
                    userToEdit.getEmail(),
                    userToEdit.getNumTel(),
                    userToEdit.getStatus()
            );

            SessionContext.getInstance().setAdminUserBeingEdited(null);
            setInfo("Utilisateur modifie avec succes.", false);
            closeWindow();

        } catch (Exception e) {
            e.printStackTrace();
            setInfo("Erreur lors de l'enregistrement: " + e.getMessage(), true);
        }
    }

    @FXML
    private void handleCancel() {
        SessionContext.getInstance().setAdminUserBeingEdited(null);
        closeWindow();
    }

    @FXML
    private void goToDashboard() {
        SessionContext.getInstance().setAdminUserBeingEdited(null);
        navigateTo("/Admin/UserDashboard.fxml", "Dashboard Admin", "/Styles/StyleWallet.css");
    }

    @FXML
    private void goToAnalyticsDashboard() {
        SessionContext.getInstance().setAdminUserBeingEdited(null);
        navigateTo("/Admin/AnalyticsDashboard.fxml", "Data Analytics Dashboard", "/Styles/StyleWallet.css");
    }

    @FXML
    private void goToAdminTasks() {
        SessionContext.getInstance().setAdminUserBeingEdited(null);
        navigateTo("/Admin/AdminTasks.fxml", "Admin Productivity / Ops", "/Styles/StyleWallet.css");
    }

    @FXML
    private void goToKycValidation() {
        SessionContext.getInstance().setAdminUserBeingEdited(null);
        navigateTo("/Admin/KycValidation.fxml", "Validation KYC", "/Styles/StyleWallet.css");
    }

    @FXML
    private void goToCreateUserForm() {
        SessionContext.getInstance().setAdminUserBeingEdited(null);
        navigateTo("/Admin/UserCreate.fxml", "Creation Utilisateur", "/Styles/StyleWallet.css");
    }

    @FXML
    private void goToWalletDashboard() {
        SessionContext.getInstance().setAdminUserBeingEdited(null);
        navigateTo("/Wallet/dashboard.fxml", "Wallet", "/Styles/StyleWallet.css");
    }

    @FXML
    private void goToProducts() {
        SessionContext.getInstance().setAdminUserBeingEdited(null);
        navigateTo("/Product/Admin/ListeProductGUI.fxml", "Produits", "/Styles/StyleWallet.css");
    }

    @FXML
    private void goToPublications() {
        SessionContext.getInstance().setAdminUserBeingEdited(null);
        navigateTo("/Publication/ListePub.fxml", "Dashboard Publication", "/Styles/StyleWallet.css");
    }

    @FXML
    private void goToBudget() {
        SessionContext.getInstance().setAdminUserBeingEdited(null);
        navigateTo("/Budget/AdminCategorieListeGUI.fxml", "Gestion Budget", "/Styles/StyleWallet.css");
    }

    @FXML
    private void goToLoans() {
        SessionContext.getInstance().setAdminUserBeingEdited(null);
        navigateTo("/Loan/AdminDashboard.fxml", "Gestion des Loans", null);
    }

    @FXML
    private void goToMenu() {
        SessionContext.getInstance().setAdminUserBeingEdited(null);
        navigateTo("/MenuGUI.fxml", "Menu Principal", "/Styles/MenuStyle.css");
    }

    @FXML
    private void handleLogout() {
        SessionContext.getInstance().setAdminUserBeingEdited(null);
        SessionContext.getInstance().logout();
        navigateTo("/Auth/Login.fxml", "Connexion", "/Styles/StyleWallet.css");
    }

    private void closeWindow() {
        Stage stage = (Stage) nomField.getScene().getWindow();
        stage.close();
    }

    private void setInfo(String text, boolean isError) {
        if (infoLabel == null) return;
        infoLabel.setText(text == null ? "" : text);
        infoLabel.setStyle(isError ? "-fx-text-fill: #cc2e2e;" : "-fx-text-fill: #1d6b34;");
    }

    private String safe(String s) {
        return s == null ? "" : s;
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
