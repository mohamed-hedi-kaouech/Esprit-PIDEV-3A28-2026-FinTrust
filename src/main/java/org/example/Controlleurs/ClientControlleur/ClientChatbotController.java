package org.example.Controlleurs.ClientControlleur;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.Model.Chatbot.ChatMessage;
import org.example.Model.User.User;
import org.example.Service.ChatbotService.ChatbotService;
import org.example.Utils.SessionContext;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ClientChatbotController {

    @FXML private ListView<String> historyList;
    @FXML private TextField messageField;
    @FXML private Label infoLabel;

    private final ChatbotService chatbotService = new ChatbotService();
    private final SessionContext session = SessionContext.getInstance();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    private void initialize() {
        User user = session.getCurrentUser();
        if (user == null) {
            setInfo("Utilisateur non connecte.", true);
            return;
        }
        refreshHistory(user.getId());
    }

    @FXML
    private void handleSend() {
        User user = session.getCurrentUser();
        if (user == null) {
            setInfo("Utilisateur non connecte.", true);
            return;
        }

        String msg = messageField == null ? "" : messageField.getText().trim();
        if (msg.isBlank()) {
            setInfo("Saisissez un message.", true);
            return;
        }

        try {
            chatbotService.sendMessage(user.getId(), msg);
            if (messageField != null) messageField.clear();
            refreshHistory(user.getId());
            setInfo("Message envoye.", false);
        } catch (Exception e) {
            setInfo("Erreur chatbot: " + e.getMessage(), true);
        }
    }

    @FXML
    private void handleQuickReset() {
        if (messageField != null) {
            messageField.setText("Comment reinitialiser mon mot de passe ?");
        }
        handleSend();
    }

    @FXML
    private void handleQuickEmail() {
        if (messageField != null) {
            messageField.setText("Je veux changer mon email");
        }
        handleSend();
    }

    @FXML
    private void handleQuickBlocked() {
        if (messageField != null) {
            messageField.setText("Mon compte est bloque");
        }
        handleSend();
    }

    @FXML
    private void handleBack() {
        navigateTo("/Client/ClientDashboard.fxml", "Dashboard Client", "/Styles/StyleWallet.css");
    }

    @FXML
    private void goToSmartBreakFromChatbot() {
        session.setSmartBreakContext("CHATBOT");
        navigateTo("/Client/SmartBreakHub.fxml", "Pause Intelligente", "/Styles/StyleWallet.css");
    }

    @FXML
    private void handleLogout() {
        session.logout();
        navigateTo("/Auth/Login.fxml", "Connexion", "/Styles/StyleWallet.css");
    }

    private void refreshHistory(int userId) {
        List<ChatMessage> history = chatbotService.getHistory(userId);
        historyList.getItems().clear();
        for (ChatMessage m : history) {
            String prefix = "USER".equals(m.getSender()) ? "Vous" : "Bot";
            historyList.getItems().add("[" + m.getCreatedAt().format(FMT) + "] " + prefix + ": " + m.getMessage());
        }
        if (!historyList.getItems().isEmpty()) {
            historyList.scrollTo(historyList.getItems().size() - 1);
        }
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

            Stage stage = (Stage) historyList.getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            setInfo("Navigation impossible: " + e.getMessage(), true);
        }
    }

    private void setInfo(String msg, boolean isError) {
        if (infoLabel == null) return;
        infoLabel.setText(msg == null ? "" : msg);
        infoLabel.setStyle(isError ? "-fx-text-fill: #b91c1c;" : "-fx-text-fill: #166534;");
    }
}

