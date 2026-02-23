package org.example.Controlleurs.ClientControlleur;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.example.Model.Kyc.KycStatus;
import org.example.Model.User.User;
import org.example.Model.User.UserRole;
import org.example.Service.AnalyticsService.ClientGamificationSnapshot;
import org.example.Service.AnalyticsService.GamificationService;
import org.example.Service.NotificationService.NotificationService;
import org.example.Service.QrService.ClientQrService;
import org.example.Utils.SessionContext;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class ClientDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label kycStatusLabel;
    @FXML private Label kycCommentLabel;

    @FXML private Label notifCountLabel;
    @FXML private ImageView qrImageView;
    @FXML private Label qrInfoLabel;
    @FXML private Label rewardLevelLabel;
    @FXML private Label rewardPointsLabel;
    @FXML private Label rewardMedalLabel;
    @FXML private Label rewardBadgesLabel;

    @FXML private Button walletButton;
    @FXML private Button profileButton;
    @FXML private Button loanButton;
    @FXML private Button budgetButton;
    @FXML private Button publicationButton;

    private final SessionContext session = SessionContext.getInstance();
    private final NotificationService notificationService = new NotificationService();
    private final ClientQrService clientQrService = new ClientQrService();
    private final GamificationService gamificationService = new GamificationService();

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
        refreshRewards(user);
        refreshClientQr(user);
    }

    private void refreshRewards(User user) {
        if (user == null) return;
        try {
            ClientGamificationSnapshot snapshot = gamificationService.getClientSnapshot(user.getId());
            if (rewardLevelLabel != null) rewardLevelLabel.setText(snapshot.level());
            if (rewardPointsLabel != null) rewardPointsLabel.setText(snapshot.points() + " pts");
            if (rewardMedalLabel != null) rewardMedalLabel.setText(snapshot.medalLabel());
            if (rewardBadgesLabel != null) {
                if (snapshot.badges() == null || snapshot.badges().isEmpty()) {
                    rewardBadgesLabel.setText("Aucun badge pour le moment");
                } else {
                    rewardBadgesLabel.setText(String.join("  |  ", snapshot.badges()));
                }
            }
        } catch (Exception e) {
            if (rewardLevelLabel != null) rewardLevelLabel.setText("STARTER");
            if (rewardPointsLabel != null) rewardPointsLabel.setText("0 pts");
            if (rewardMedalLabel != null) rewardMedalLabel.setText("Niveau Starter");
            if (rewardBadgesLabel != null) rewardBadgesLabel.setText("Badges indisponibles");
        }
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

    @FXML
    private void handleRefreshQr() {
        User user = session.getCurrentUser();
        if (user == null) {
            setQrInfo("Utilisateur non connecte.", true);
            return;
        }
        refreshClientQr(user);
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

    private void refreshClientQr(User user) {
        try {
            File qr = clientQrService.generateClientQrImage(user, 260).toFile();
            Image image = new Image(qr.toURI().toString(), true);
            if (qrImageView != null) {
                qrImageView.setImage(image);
                qrImageView.setPreserveRatio(true);
                qrImageView.setFitWidth(220);
                qrImageView.setFitHeight(220);
            }
            setQrInfo("QR client genere automatiquement avec infos completes.", false);
        } catch (Exception e) {
            e.printStackTrace();
            setQrInfo("Generation QR impossible: " + e.getMessage(), true);
        }
    }

    private void setQrInfo(String message, boolean isError) {
        if (qrInfoLabel == null) return;
        qrInfoLabel.setText(message == null ? "" : message);
        qrInfoLabel.setStyle(isError ? "-fx-text-fill: #b91c1c;" : "-fx-text-fill: #166534; -fx-font-weight: 600;");
    }
}
