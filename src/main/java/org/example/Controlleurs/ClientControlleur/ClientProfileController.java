package org.example.Controlleurs.ClientControlleur;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.Model.User.User;
import org.example.Service.AnalyticsService.ClientGamificationSnapshot;
import org.example.Service.AnalyticsService.GamificationService;
import org.example.Service.KycService.KycService;
import org.example.Service.KycService.KycStateResult;
import org.example.Service.UserService.UserService;
import org.example.Utils.SessionContext;

import java.net.URL;
import java.time.LocalDate;
import java.util.regex.Pattern;

public class ClientProfileController {

    @FXML private TextField nomField;
    @FXML private TextField emailField;
    @FXML private TextField numTelField;
    @FXML private DatePicker dateNaissancePicker;
    @FXML private Label rewardLevelLabel;
    @FXML private Label rewardPointsLabel;
    @FXML private Label rewardMedalLabel;
    @FXML private Label infoLabel;

    private final UserService userService = new UserService();
    private final GamificationService gamificationService = new GamificationService();
    private final KycService kycService = new KycService();

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[0-9+\\-\\s]{8,20}$");

    @FXML
    private void initialize() {
        User user = SessionContext.getInstance().getCurrentUser();
        if (user == null) {
            setInfo("Utilisateur non connecte.", true);
            return;
        }

        if (nomField != null) nomField.setText(nullSafe(user.getNom()));
        if (emailField != null) emailField.setText(nullSafe(user.getEmail()));
        if (numTelField != null) numTelField.setText(nullSafe(user.getNumTel()));
        try {
            KycStateResult state = kycService.getClientKycState(user);
            if (dateNaissancePicker != null) {
                dateNaissancePicker.setValue(state.getDateNaissance());
            }
        } catch (Exception ignored) {
        }
        refreshRewards(user);
    }

    private void refreshRewards(User user) {
        try {
            ClientGamificationSnapshot snapshot = gamificationService.getClientSnapshot(user.getId());
            if (rewardLevelLabel != null) rewardLevelLabel.setText(snapshot.level());
            if (rewardPointsLabel != null) rewardPointsLabel.setText(snapshot.points() + " pts");
            if (rewardMedalLabel != null) rewardMedalLabel.setText(snapshot.medalLabel());
        } catch (Exception e) {
            if (rewardLevelLabel != null) rewardLevelLabel.setText("STARTER");
            if (rewardPointsLabel != null) rewardPointsLabel.setText("0 pts");
            if (rewardMedalLabel != null) rewardMedalLabel.setText("Niveau Starter");
        }
    }

    @FXML
    private void handleEdit() {
        User current = SessionContext.getInstance().getCurrentUser();
        if (current == null) {
            setInfo("Utilisateur non connecte.", true);
            return;
        }

        String nom = nomField != null ? nomField.getText().trim() : "";
        String email = emailField != null ? emailField.getText().trim() : "";
        String tel = numTelField != null ? numTelField.getText().trim() : "";
        LocalDate dateNaissance = dateNaissancePicker != null ? dateNaissancePicker.getValue() : null;

        if (nom.length() < 2) {
            setInfo("Nom invalide (min 2 caracteres).", true);
            return;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            setInfo("Email invalide.", true);
            return;
        }
        if (!PHONE_PATTERN.matcher(tel).matches()) {
            setInfo("Telephone invalide.", true);
            return;
        }
        if (dateNaissance == null) {
            setInfo("Date de naissance obligatoire.", true);
            return;
        }

        try {
            current.setNom(nom);
            current.setEmail(email.toLowerCase());
            current.setNumTel(tel);

            userService.updateUserProfile(current);
            KycStateResult state = kycService.updateClientBirthDate(current, dateNaissance);
            SessionContext.getInstance().setCurrentKycStatus(state.getStatus());
            SessionContext.getInstance().setCurrentKycComment(state.getCommentaireAdmin());
            SessionContext.getInstance().setCurrentUser(current);

            setInfo("Profil modifie avec succes.", false);

        } catch (Exception e) {
            e.printStackTrace();
            setInfo("Erreur modification profil: " + e.getMessage(), true);
        }
    }

    @FXML
    private void goToDashboard() {
        navigateTo("/Client/ClientDashboard.fxml", "Dashboard Client", "/Styles/StyleWallet.css");
    }

    @FXML
    private void goToSmartBreakFromProfile() {
        SessionContext.getInstance().setSmartBreakContext("PROFILE");
        navigateTo("/Client/SmartBreakHub.fxml", "Pause Intelligente", "/Styles/StyleWallet.css");
    }

    @FXML
    private void handleLogout() {
        SessionContext.getInstance().logout();
        navigateTo("/Auth/Login.fxml", "Connexion", "/Styles/StyleWallet.css");
    }

    private void setInfo(String msg, boolean isError) {
        if (infoLabel == null) return;
        infoLabel.setText(msg == null ? "" : msg);
        infoLabel.setStyle(isError ? "-fx-text-fill: #b91c1c;" : "-fx-text-fill: #166534;");
    }

    private void navigateTo(String fxmlPath, String title, String stylesheetPath) {
        try {
            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                setInfo("FXML introuvable: " + fxmlPath, true);
                return;
            }

            Parent root = FXMLLoader.load(fxmlUrl);
            Scene scene = new Scene(root);

            if (stylesheetPath != null && !stylesheetPath.isBlank()) {
                URL cssUrl = getClass().getResource(stylesheetPath);
                if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            Stage stage = (Stage) (nomField != null ? nomField.getScene().getWindow() : infoLabel.getScene().getWindow());
            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            setInfo("Navigation impossible: " + e.getMessage(), true);
        }
    }

    private String nullSafe(String s) {
        return s == null ? "" : s;
    }
}