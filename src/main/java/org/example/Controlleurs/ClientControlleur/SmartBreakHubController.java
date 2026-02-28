package org.example.Controlleurs.ClientControlleur;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.example.Utils.SessionContext;

import java.net.URL;

public class SmartBreakHubController {

    @FXML private Label contextLabel;
    @FXML private Label infoLabel;

    private final SessionContext session = SessionContext.getInstance();

    @FXML
    private void initialize() {
        contextLabel.setText("Contexte: " + session.getSmartBreakContext());
    }

    @FXML
    private void handleOpenMemory() {
        navigateTo("/Client/MemoryGame.fxml", "Pause Intelligente - Memory");
    }

    @FXML
    private void handleOpenDino() {
        navigateTo("/Client/DinoGame.fxml", "Pause Intelligente - Dino Run");
    }

    @FXML
    private void handleBack() {
        String context = session.getSmartBreakContext();
        if ("KYC".equalsIgnoreCase(context)) {
            navigateTo("/Kyc/KycForm.fxml", "Formulaire KYC");
            return;
        }
        if ("CHATBOT".equalsIgnoreCase(context)) {
            navigateTo("/Client/ClientChatbot.fxml", "Assistant Client");
            return;
        }
        navigateTo("/Client/ClientDashboard.fxml", "Dashboard Client");
    }

    private void navigateTo(String fxmlPath, String title) {
        try {
            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                setInfo("FXML introuvable: " + fxmlPath, true);
                return;
            }
            Parent root = FXMLLoader.load(fxmlUrl);
            Scene scene = new Scene(root);
            URL cssUrl = getClass().getResource("/Styles/StyleWallet.css");
            if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());
            Stage stage = (Stage) contextLabel.getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            setInfo("Navigation impossible: " + e.getMessage(), true);
        }
    }

    private void setInfo(String message, boolean isError) {
        if (infoLabel == null) return;
        infoLabel.setText(message == null ? "" : message);
        infoLabel.setStyle(isError ? "-fx-text-fill: #b91c1c;" : "-fx-text-fill: #166534;");
    }
}