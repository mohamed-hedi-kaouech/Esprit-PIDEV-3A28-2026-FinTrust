package org.example.Controlleurs.ClientControlleur;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.Model.User.User;
import org.example.Service.UserService.UserService;
import org.example.Utils.SessionContext;

public class ClientProfileController {
    @FXML
    private TextField nomField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField numTelField;
    @FXML
    private Label infoLabel;

    private final UserService userService = new UserService();

    @FXML
    private void initialize() {
        User user = SessionContext.getInstance().getCurrentUser();
        if (user != null) {
            nomField.setText(user.getNom());
            emailField.setText(user.getEmail());
            numTelField.setText(user.getNumTel());
        }
    }

    @FXML
    private void handleEdit() {
        User user = SessionContext.getInstance().getCurrentUser();
        if (user == null) {
            infoLabel.setText("Utilisateur non connecté.");
            return;
        }
        user.setNom(nomField.getText());
        user.setEmail(emailField.getText());
        user.setNumTel(numTelField.getText());
        userService.updateUserProfile(user);
        infoLabel.setText("Profil modifié avec succès.");
    }

    @FXML
    private void goToDashboard() {
        Stage stage = (Stage) nomField.getScene().getWindow();
        org.example.Utils.FxUtils.navigate(stage, "/Client/ClientDashboard.fxml", "Dashboard Client", "/Styles/StyleWallet.css");
    }

    @FXML
    private void handleLogout() {
        SessionContext.getInstance().logout();
        Stage stage = (Stage) nomField.getScene().getWindow();
        org.example.Utils.FxUtils.navigate(stage, "/Auth/Login.fxml", "Connexion", "/Styles/StyleWallet.css");
    }
}
