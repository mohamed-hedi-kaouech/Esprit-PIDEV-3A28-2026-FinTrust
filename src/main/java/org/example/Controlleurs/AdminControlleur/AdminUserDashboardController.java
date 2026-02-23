package org.example.Controlleurs.AdminControlleur;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.Model.User.User;
import org.example.Model.User.UserStatus;
import org.example.Service.UserService.UserService;
import org.example.Utils.SessionContext;

import java.lang.reflect.Method;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AdminUserDashboardController {

    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, Number> idColumn;
    @FXML private TableColumn<User, String> nomColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> roleColumn;
    @FXML private TableColumn<User, String> statusColumn;
    @FXML private TableColumn<User, String> createdAtColumn;
    @FXML private ComboBox<UserStatus> statusComboBox;
    @FXML private Label infoLabel;

    private final UserService userService = new UserService();
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    private void initialize() {
        if (!SessionContext.getInstance().isAdmin()) {
            showAlert(Alert.AlertType.ERROR, "Accès refusé",
                    "Vous devez être connecté en tant qu'administrateur.");
            Platform.runLater(() -> navigateTo("/Auth/Login.fxml", "Connexion", "/Styles/StyleWallet.css"));
            return;
        }

        idColumn.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getId()));
        nomColumn.setCellValueFactory(d -> new SimpleStringProperty(safe(d.getValue().getNom())));
        emailColumn.setCellValueFactory(d -> new SimpleStringProperty(safe(d.getValue().getEmail())));
        roleColumn.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getRole() != null ? d.getValue().getRole().name() : ""));
        statusColumn.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getStatus() != null ? d.getValue().getStatus().name() : ""));
        createdAtColumn.setCellValueFactory(d -> {
            if (d.getValue().getCreatedAt() == null) return new SimpleStringProperty("");
            return new SimpleStringProperty(d.getValue().getCreatedAt().format(DATE_FORMAT));
        });

        statusComboBox.getItems().setAll(UserStatus.values());

        usersTable.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) statusComboBox.setValue(n.getStatus());
        });

        loadUsers();
    }

    @FXML
    private void handleRefresh() {
        loadUsers();
        setInfo("Liste actualisée.", false);
    }

    @FXML
    private void handleApplyStatus() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        UserStatus selectedStatus = statusComboBox.getValue();

        if (selectedUser == null) {
            setInfo("Sélectionnez un utilisateur.", true);
            return;
        }
        if (selectedStatus == null) {
            setInfo("Sélectionnez un statut.", true);
            return;
        }

        try {
            userService.updateUserStatus(SessionContext.getInstance().getCurrentUser(),
                    selectedUser.getId(), selectedStatus);
            loadUsers();
            setInfo("Statut mis à jour avec succès.", false);
        } catch (Exception e) {
            setInfo("Erreur: " + e.getMessage(), true);
        }
    }

    @FXML
    private void goToMenu() {
        navigateTo("/MenuGUI.fxml", "Menu Principal", "/Styles/MenuStyle.css");
    }

    @FXML
    private void goToCreateUserForm() {
        navigateTo("/Admin/UserCreate.fxml", "Création Utilisateur", "/Styles/StyleWallet.css");
    }

    /**
     * ✅ Ouvre KYC dans une NOUVELLE fenêtre (plus stable)
     * Si ça échoue, la console affichera l'erreur exacte.
     */
    @FXML
    private void goToKycValidation() {
        openInNewWindow("/Admin/KycValidation.fxml", "Validation KYC", "/Styles/StyleWallet.css");
    }

    @FXML
    private void handleLogout() {
        SessionContext.getInstance().logout();
        navigateTo("/Auth/Login.fxml", "Connexion", "/Styles/StyleWallet.css");
    }

    @FXML
    private void handleEditUser() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            setInfo("Sélectionnez un utilisateur à modifier.", true);
            return;
        }

        try {
            URL fxmlUrl = getClass().getResource("/Admin/UserEdit.fxml");
            System.out.println("EDIT FXML => " + fxmlUrl);

            if (fxmlUrl == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "FXML introuvable: /Admin/UserEdit.fxml");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            AdminUserEditController controller = loader.getController();
            controller.setUserToEdit(selectedUser);

            Stage stage = new Stage();
            stage.setTitle("Modifier utilisateur");
            Scene scene = new Scene(root);

            URL cssUrl = getClass().getResource("/Styles/StyleWallet.css");
            if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());

            stage.setScene(scene);
            stage.showAndWait();

            loadUsers(); // refresh après fermeture

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir l'édition: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteUser() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            setInfo("Sélectionnez un utilisateur à supprimer.", true);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Voulez-vous supprimer l'utilisateur ID=" + selectedUser.getId() + " ?");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        try {
            boolean called = tryInvokeUserServiceLooser("deleteUser",
                    new Object[]{SessionContext.getInstance().getCurrentUser(), selectedUser.getId()});

            if (!called) {
                setInfo("Suppression non implémentée dans UserService (deleteUser introuvable).", true);
                return;
            }

            loadUsers();
            setInfo("Utilisateur supprimé avec succès.", false);
        } catch (Exception e) {
            setInfo("Erreur suppression: " + e.getMessage(), true);
        }
    }

    @FXML
    private void handleSendNotification() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            setInfo("Sélectionnez un utilisateur pour envoyer une notification.", true);
            return;
        }

        try {
            String msg = "Votre compte a été mis à jour par l'administration.";
            boolean called = tryInvokeUserServiceLooser("sendNotificationToUser",
                    new Object[]{SessionContext.getInstance().getCurrentUser(), selectedUser.getId(), msg});

            if (!called) {
                setInfo("Notification non implémentée (sendNotificationToUser introuvable).", true);
                return;
            }

            setInfo("Notification envoyée.", false);
        } catch (Exception e) {
            setInfo("Erreur notification: " + e.getMessage(), true);
        }
    }

    @FXML
    private void handleBack() {
        navigateTo("/MenuGUI.fxml", "Menu Principal", "/Styles/MenuStyle.css");
    }

    private void loadUsers() {
        try {
            List<User> users = userService.listUsersForAdmin(SessionContext.getInstance().getCurrentUser());
            usersTable.getItems().setAll(users);
        } catch (Exception e) {
            setInfo("Impossible de charger les utilisateurs: " + e.getMessage(), true);
        }
    }

    private void setInfo(String text, boolean isError) {
        if (infoLabel == null) return;
        infoLabel.setText(text == null ? "" : text);
        infoLabel.setStyle(isError ? "-fx-text-fill: #cc2e2e;" : "-fx-text-fill: #1d6b34;");
    }

    private void navigateTo(String fxmlPath, String title, String stylesheet) {
        try {
            URL fxmlUrl = getClass().getResource(fxmlPath);
            System.out.println("NAVIGATE -> " + fxmlPath + " => " + fxmlUrl);

            if (fxmlUrl == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "FXML introuvable: " + fxmlPath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            Scene scene = new Scene(root);

            if (stylesheet != null && !stylesheet.isBlank()) {
                URL cssUrl = getClass().getResource(stylesheet);
                System.out.println("CSS -> " + stylesheet + " => " + cssUrl);
                if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            Stage stage = (Stage) usersTable.getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de naviguer: " + e.getMessage());
        }
    }

    private void openInNewWindow(String fxmlPath, String title, String stylesheet) {
        try {
            URL fxmlUrl = getClass().getResource(fxmlPath);
            System.out.println("OPEN WINDOW -> " + fxmlPath + " => " + fxmlUrl);

            if (fxmlUrl == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "FXML introuvable: " + fxmlPath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            Scene scene = new Scene(root);
            if (stylesheet != null && !stylesheet.isBlank()) {
                URL cssUrl = getClass().getResource(stylesheet);
                if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de naviguer: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private boolean tryInvokeUserServiceLooser(String methodName, Object[] args) {
        try {
            Method[] methods = userService.getClass().getMethods();
            for (Method m : methods) {
                if (!m.getName().equals(methodName)) continue;
                if (m.getParameterCount() != args.length) continue;
                m.invoke(userService, args);
                return true;
            }
            return false;
        } catch (Exception ignored) {
            return false;
        }
    }
}
