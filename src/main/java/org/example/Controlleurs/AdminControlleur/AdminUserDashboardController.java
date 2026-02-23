package org.example.Controlleurs.AdminControlleur;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.Model.User.User;
import org.example.Model.User.UserRole;
import org.example.Model.User.UserStatus;
import org.example.Service.ExportService.AdminExportService;
import org.example.Service.UserService.UserService;
import org.example.Utils.SessionContext;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    @FXML private Label totalUsersLabel;
    @FXML private Label totalClientsLabel;
    @FXML private Label totalAdminsLabel;
    @FXML private PieChart statusPieChart;

    private final UserService userService = new UserService();
    private final AdminExportService exportService = new AdminExportService();
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    private void initialize() {
        // ✅ Sécurité : admin obligatoire
        if (!SessionContext.getInstance().isAdmin()) {
            showAlert(Alert.AlertType.ERROR, "Accès refusé",
                    "Vous devez être connecté en tant qu'administrateur.");
            Platform.runLater(() -> navigateTo("/Auth/Login.fxml", "Connexion", "/Styles/StyleWallet.css"));
            return;
        }

        // ✅ Colonnes table
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

        // ✅ Status combo
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

        if (selectedUser == null) { setInfo("Sélectionnez un utilisateur.", true); return; }
        if (selectedStatus == null) { setInfo("Sélectionnez un statut.", true); return; }

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

    @FXML
    private void goToAnalyticsDashboard() {
        navigateTo("/Admin/AnalyticsDashboard.fxml", "Data Analytics Dashboard", "/Styles/StyleWallet.css");
    }

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

            loadUsers(); // ✅ refresh après fermeture

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
            // ✅ Appel direct (plus de reflection)
            userService.deleteUser(SessionContext.getInstance().getCurrentUser(), selectedUser.getId());
            loadUsers();
            setInfo("Utilisateur supprimé avec succès.", false);

        } catch (Exception e) {
            setInfo("Erreur suppression: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSendNotification() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            setInfo("Selectionnez un utilisateur pour envoyer une notification.", true);
            return;
        }

        String msg = "Votre compte a ete mis a jour par l'administration.";

        try {
            userService.sendNotificationToUser(
                    SessionContext.getInstance().getCurrentUser(),
                    selectedUser.getId(),
                    msg
            );
            setInfo("Notification envoyee.", false);
        } catch (Exception e) {
            e.printStackTrace();
            setInfo("Erreur notification: " + e.getMessage(), true);
        }
    }

    @FXML
    private void handleBack() {
        navigateTo("/MenuGUI.fxml", "Menu Principal", "/Styles/MenuStyle.css");
    }

    @FXML
    private void handleExportUsersExcel() {
        exportUsersTo("csv");
    }

    @FXML
    private void handleExportUsersPdf() {
        exportUsersTo("pdf");
    }

    @FXML
    private void handleExportAnalyticsExcel() {
        exportAnalyticsTo("csv");
    }

    @FXML
    private void handleExportAnalyticsPdf() {
        exportAnalyticsTo("pdf");
    }

    @FXML
    private void handleExportAuditExcel() {
        exportAuditTo("csv");
    }

    @FXML
    private void handleExportAuditPdf() {
        exportAuditTo("pdf");
    }

    private void loadUsers() {
        try {
            List<User> users = userService.listUsersForAdmin(SessionContext.getInstance().getCurrentUser());
            usersTable.getItems().setAll(users);
            updateStats(users);
        } catch (Exception e) {
            setInfo("Impossible de charger les utilisateurs: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }

    private void updateStats(List<User> users) {
        int total = users.size();
        int admins = 0;
        int clients = 0;

        Map<UserStatus, Integer> statusCounts = new LinkedHashMap<>();
        statusCounts.put(UserStatus.EN_ATTENTE, 0);
        statusCounts.put(UserStatus.ACCEPTE, 0);
        statusCounts.put(UserStatus.REFUSE, 0);

        for (User user : users) {
            if (user.getRole() == UserRole.ADMIN) admins++;
            if (user.getRole() == UserRole.CLIENT) clients++;

            UserStatus status = user.getStatus();
            if (status != null && statusCounts.containsKey(status)) {
                statusCounts.put(status, statusCounts.get(status) + 1);
            }
        }

        if (totalUsersLabel != null) totalUsersLabel.setText(String.valueOf(total));
        if (totalClientsLabel != null) totalClientsLabel.setText(String.valueOf(clients));
        if (totalAdminsLabel != null) totalAdminsLabel.setText(String.valueOf(admins));

        if (statusPieChart == null) return;

        PieChart.Data attente = new PieChart.Data("En attente", statusCounts.get(UserStatus.EN_ATTENTE));
        PieChart.Data approuve = new PieChart.Data("Accepte", statusCounts.get(UserStatus.ACCEPTE));
        PieChart.Data refuse = new PieChart.Data("Refuse", statusCounts.get(UserStatus.REFUSE));
        statusPieChart.setData(FXCollections.observableArrayList(attente, approuve, refuse));

        Platform.runLater(() -> {
            stylePieSlice(attente, "#1e3a8a");
            stylePieSlice(approuve, "#3b82f6");
            stylePieSlice(refuse, "#93c5fd");
        });
    }

    private void stylePieSlice(PieChart.Data data, String color) {
        if (data.getNode() != null) {
            data.getNode().setStyle("-fx-pie-color: " + color + ";");
        }
    }

    private void exportUsersTo(String extension) {
        try {
            List<User> users = usersTable.getItems();
            if (users == null || users.isEmpty()) {
                setInfo("Aucun utilisateur a exporter.", true);
                return;
            }

            Path target = chooseExportPath(
                    "Exporter la liste des utilisateurs",
                    exportService.buildDefaultName("users", extension),
                    extension
            );
            if (target == null) return;

            if ("pdf".equalsIgnoreCase(extension)) {
                exportService.exportUsersPdf(target, users);
            } else {
                exportService.exportUsersCsv(target, users);
            }
            setInfo("Export utilisateurs termine: " + target.getFileName(), false);
        } catch (Exception e) {
            setInfo("Erreur export utilisateurs: " + e.getMessage(), true);
        }
    }

    private void exportAnalyticsTo(String extension) {
        try {
            Path target = chooseExportPath(
                    "Exporter les statistiques admin",
                    exportService.buildDefaultName("analytics", extension),
                    extension
            );
            if (target == null) return;

            if ("pdf".equalsIgnoreCase(extension)) {
                exportService.exportAnalyticsPdf(target);
            } else {
                exportService.exportAnalyticsCsv(target);
            }
            setInfo("Export statistiques termine: " + target.getFileName(), false);
        } catch (Exception e) {
            setInfo("Erreur export statistiques: " + e.getMessage(), true);
        }
    }

    private void exportAuditTo(String extension) {
        try {
            Path target = chooseExportPath(
                    "Exporter les journaux d'audit",
                    exportService.buildDefaultName("audit_logs", extension),
                    extension
            );
            if (target == null) return;

            if ("pdf".equalsIgnoreCase(extension)) {
                exportService.exportAuditPdf(target);
            } else {
                exportService.exportAuditCsv(target);
            }
            setInfo("Export audit termine: " + target.getFileName(), false);
        } catch (Exception e) {
            setInfo("Erreur export audit: " + e.getMessage(), true);
        }
    }

    private Path chooseExportPath(String title, String initialFileName, String extension) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        chooser.setInitialFileName(initialFileName);

        String ext = extension == null ? "" : extension.toLowerCase();
        if ("pdf".equals(ext)) {
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF (*.pdf)", "*.pdf"));
        } else {
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel compatible CSV (*.csv)", "*.csv"));
        }

        Stage stage = (Stage) usersTable.getScene().getWindow();
        File file = chooser.showSaveDialog(stage);
        return file == null ? null : file.toPath();
    }

    private void setInfo(String text, boolean isError) {
        if (infoLabel == null) return;
        infoLabel.setText(text == null ? "" : text);
        infoLabel.setStyle(isError ? "-fx-text-fill: #cc2e2e;" : "-fx-text-fill: #1d6b34;");
    }

    private void navigateTo(String fxmlPath, String title, String stylesheet) {
        try {
            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "FXML introuvable: " + fxmlPath);
                return;
            }

            Parent root = FXMLLoader.load(fxmlUrl);
            Scene scene = new Scene(root);

            if (stylesheet != null && !stylesheet.isBlank()) {
                URL cssUrl = getClass().getResource(stylesheet);
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
            if (fxmlUrl == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "FXML introuvable: " + fxmlPath);
                return;
            }

            Parent root = FXMLLoader.load(fxmlUrl);
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
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir: " + e.getMessage());
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
}
