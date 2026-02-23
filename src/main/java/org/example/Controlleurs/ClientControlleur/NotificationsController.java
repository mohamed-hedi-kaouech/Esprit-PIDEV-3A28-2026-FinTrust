package org.example.Controlleurs.ClientControlleur;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import org.example.Model.Notification.Notification;
import org.example.Service.NotificationService.NotificationService;
import org.example.Utils.SessionContext;

import java.net.URL;
import java.util.List;

public class NotificationsController {

    @FXML private ListView<String> notifList;
    @FXML private Label infoLabel;

    private final NotificationService notificationService = new NotificationService();

    @FXML
    private void initialize() {
        notifList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: #102a43; -fx-font-size: 13px; -fx-padding: 8 10;");
                }
            }
        });
        load();
    }

    private void load() {
        try {
            if (SessionContext.getInstance().getCurrentUser() == null) {
                infoLabel.setText("Erreur: utilisateur non connecte.");
                return;
            }

            int userId = SessionContext.getInstance().getCurrentUser().getId();
            List<Notification> notifs = notificationService.listForUser(userId);

            notifList.getItems().clear();
            for (Notification n : notifs) {
                String prefix = n.isRead() ? "[LU] " : "[NOUVEAU] ";
                notifList.getItems().add(prefix + "[" + n.getType() + "] " + n.getMessage() + " (" + n.getCreatedAt() + ")");
            }

            if (notifs.isEmpty()) {
                infoLabel.setText("Aucune notification pour le moment.");
            } else {
                infoLabel.setText("Total: " + notifs.size());
            }
        } catch (Exception e) {
            e.printStackTrace();
            infoLabel.setText("Erreur: " + e.getMessage());
        }
    }

    @FXML
    private void markAllRead() {
        try {
            if (SessionContext.getInstance().getCurrentUser() == null) {
                infoLabel.setText("Erreur: utilisateur non connecte.");
                return;
            }
            int userId = SessionContext.getInstance().getCurrentUser().getId();
            notificationService.markAllRead(userId);
            load();
        } catch (Exception e) {
            e.printStackTrace();
            infoLabel.setText("Erreur: " + e.getMessage());
        }
    }

    @FXML
    private void goBack() {
        Stage stage = (Stage) notifList.getScene().getWindow();
        navigate(stage, "/Client/ClientDashboard.fxml", "Dashboard Client", "/Styles/StyleWallet.css");
    }

    private void navigate(Stage stage, String fxml, String title, String css) {
        try {
            URL url = getClass().getResource(fxml);
            if (url == null) {
                infoLabel.setText("Erreur: FXML introuvable: " + fxml);
                return;
            }
            Parent root = FXMLLoader.load(url);
            Scene scene = new Scene(root);
            if (css != null && !css.isBlank()) {
                URL cssUrl = getClass().getResource(css);
                if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());
            }
            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            infoLabel.setText("Erreur navigation: " + e.getMessage());
        }
    }
}
