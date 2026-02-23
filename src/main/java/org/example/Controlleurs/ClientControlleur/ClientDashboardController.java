    @FXML
    private void goToProfile() {
        if (session.getCurrentKycStatus() != KycStatus.APPROUVE) {
            showError("Acces refuse", "Votre KYC doit etre approuve pour consulter/modifier votre profil.");
            return;
        }
        navigateTo("/Client/ClientProfile.fxml", "Profil Client", "/Styles/StyleWallet.css");
    }
package org.example.Controlleurs.ClientControlleur;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.example.Model.Kyc.KycStatus;
import org.example.Model.User.User;
import org.example.Model.User.UserRole;
import org.example.Utils.SessionContext;

import java.io.IOException;

public class ClientDashboardController {
    @FXML
    private Label welcomeLabel;

    @FXML
    private Label kycStatusLabel;

    @FXML
    private Label kycCommentLabel;

    @FXML
    private Button walletButton;

    private final SessionContext session = SessionContext.getInstance();

    @FXML
    private void initialize() {
        User user = session.getCurrentUser();
        if (user == null || user.getRole() != UserRole.CLIENT) {
            navigateTo("/Auth/Login.fxml", "Connexion", "/Styles/StyleWallet.css");
            return;
        }

        welcomeLabel.setText("Bienvenue " + user.getNom() + " " + user.getPrenom());
        KycStatus status = session.getCurrentKycStatus();
        if (status == null) {
            status = KycStatus.EN_ATTENTE;
            session.setCurrentKycStatus(status);
        }

        kycStatusLabel.setText("Statut KYC: " + status.name());
        if (status == KycStatus.REFUSE) {
            String comment = session.getCurrentKycComment();
            kycCommentLabel.setText(comment == null || comment.isBlank() ? "KYC refuse. Veuillez corriger les documents." : "Commentaire admin: " + comment);
        } else {
            kycCommentLabel.setText(status == KycStatus.APPROUVE
                    ? "Votre KYC est approuve. Acces complet disponible."
                    : "Votre KYC est en attente. Acces limite a ce dashboard et au formulaire KYC.");
        }

        walletButton.setDisable(status != KycStatus.APPROUVE);
    }

    @FXML
    private void goToKycForm() {
        navigateTo("/Kyc/KycForm.fxml", "Formulaire KYC", "/Styles/StyleWallet.css");
    }

    @FXML
    private void goToWalletDashboard() {
        if (session.getCurrentKycStatus() != KycStatus.APPROUVE) {
            showError("Acces refuse", "Votre KYC doit etre approuve pour acceder au dashboard wallet.");
            return;
        }
        navigateTo("/Wallet/dashboard.fxml", "Dashboard Client", "/Styles/StyleWallet.css");
    }

        @FXML
        private void checkKycStatus() {
            String status = SessionContext.getInstance().getCurrentKycStatus() != null ? SessionContext.getInstance().getCurrentKycStatus().name() : "-";
            String comment = SessionContext.getInstance().getCurrentKycComment();
            String message = "Statut KYC: " + status + "\n";
            if (comment != null && !comment.isBlank()) {
                message += "Commentaire admin: " + comment;
            } else {
                message += "Aucun commentaire admin.";
            }
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Statut KYC");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        }
    @FXML
    private void handleLogout() {
        session.logout();
        navigateTo("/Auth/Login.fxml", "Connexion", "/Styles/StyleWallet.css");
    }

    private void navigateTo(String fxmlPath, String title, String stylesheetPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Scene scene = new Scene(root);
            if (stylesheetPath != null && !stylesheetPath.isBlank()) {
                scene.getStylesheets().add(getClass().getResource(stylesheetPath).toExternalForm());
            }
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            showError("Erreur", "Navigation impossible: " + e.getMessage());
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
