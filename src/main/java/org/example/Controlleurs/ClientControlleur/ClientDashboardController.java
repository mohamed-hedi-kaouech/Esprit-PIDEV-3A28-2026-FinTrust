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
import org.example.Service.NotificationService.NotificationService;
import org.example.Utils.SessionContext;

import java.io.IOException;
import java.net.URL;

public class ClientDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label kycStatusLabel;
    @FXML private Label kycCommentLabel;

    @FXML private Label notifCountLabel;

    @FXML private Button walletButton;
    @FXML private Button profileButton;
    @FXML private Button loanButton;
    @FXML private Button budgetButton;
    @FXML private Button publicationButton;

    private final SessionContext session = SessionContext.getInstance();
    private final NotificationService notificationService = new NotificationService();

    @FXML
    private void initialize() {
        User user = session.getCurrentUser();
        if (user == null || user.getRole() != UserRole.CLIENT) {
            navigateTo("/Auth/Login.fxml", "Connexion", "/Styles/StyleWallet.css");
            return;
        }

        welcomeLabel.setText("Bienvenue " + safe(user.getNom()) + " " + safe(user.getPrenom()));

        KycStatus status = session.getCurrentKycStatus();
        if (status == null) status = KycStatus.EN_ATTENTE;

        kycStatusLabel.setText("Statut KYC: " + status.name());

        if (status == KycStatus.REFUSE) {
            String comment = session.getCurrentKycComment();
            kycCommentLabel.setText(
                    (comment == null || comment.isBlank())
                            ? "KYC refuse. Veuillez corriger les documents."
                            : "Commentaire admin: " + comment
            );
        } else if (status == KycStatus.APPROUVE) {
            kycCommentLabel.setText("Votre KYC est approuve. Acces complet disponible.");
        } else {
            kycCommentLabel.setText("Votre KYC est en attente. Acces limite.");
        }

        boolean allowed = (status == KycStatus.APPROUVE);
        walletButton.setDisable(!allowed);
        profileButton.setDisable(!allowed);
        loanButton.setDisable(!allowed);
        budgetButton.setDisable(!allowed);
        publicationButton.setDisable(!allowed);

        refreshNotifBadge();
    }

    private void refreshNotifBadge() {
        try {
            User user = session.getCurrentUser();
            int count = notificationService.countUnread(user.getId());
            notifCountLabel.setText(String.valueOf(count));
        } catch (Exception e) {
            notifCountLabel.setText("0");
        }
    }

    @FXML
    private void goToKycForm() {
        navigateTo("/Kyc/KycForm.fxml", "Formulaire KYC", "/Styles/StyleWallet.css");
    }

    @FXML
    private void goToProfile() {
        if (!ensureKycApprovedOrShow()) return;
        navigateTo("/Client/ClientProfile.fxml", "Profil Client", "/Styles/StyleWallet.css");
    }

    @FXML
    private void goToWalletDashboard() {
        if (!ensureKycApprovedOrShow()) return;
        navigateTo("/Wallet/dashboard.fxml", "Wallet", "/Styles/StyleWallet.css");
    }

    @FXML
    private void goToLoan() {
        if (!ensureKycApprovedOrShow()) return;
        navigateTo("/Loan/LoanList.fxml", "Loans", "/Styles/StyleWallet.css");
    }

    @FXML
    private void goToBudget() {
        if (!ensureKycApprovedOrShow()) return;
        navigateTo("/Budget/CategorieListeGUI.fxml", "Budget", "/Styles/StyleWallet.css");
    }

    @FXML
    private void goToPublications() {
        if (!ensureKycApprovedOrShow()) return;
        navigateTo("/Publication/PublicationManagerGUI.fxml", "Publications", "/Styles/StyleWallet.css");
    }

    @FXML
    private void goToNotifications() {
        navigateTo("/Client/Notifications.fxml", "Notifications", "/Styles/StyleWallet.css");
    }

    @FXML
    private void goToChatbot() {
        navigateTo("/Client/ClientChatbot.fxml", "Assistant Client", "/Styles/StyleWallet.css");
    }

    private boolean ensureKycApprovedOrShow() {
        if (session.getCurrentKycStatus() != KycStatus.APPROUVE) {
            showError("Acces refuse", "Votre KYC doit etre approuve pour acceder a cette section.");
            return false;
        }
        return true;
    }

    @FXML
    private void handleLogout() {
        session.logout();
        navigateTo("/Auth/Login.fxml", "Connexion", "/Styles/StyleWallet.css");
    }

    private void navigateTo(String fxmlPath, String title, String stylesheetPath) {
        try {
            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                showError("Erreur", "FXML introuvable: " + fxmlPath);
                return;
            }

            Parent root = FXMLLoader.load(fxmlUrl);
            Scene scene = new Scene(root);

            if (stylesheetPath != null && !stylesheetPath.isBlank()) {
                URL cssUrl = getClass().getResource(stylesheetPath);
                if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            showError("Erreur", "Navigation impossible: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}
