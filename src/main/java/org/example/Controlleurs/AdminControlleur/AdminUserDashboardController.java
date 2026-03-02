package org.example.Controlleurs.AdminControlleur;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.Model.User.User;
import org.example.Model.User.UserRole;
import org.example.Model.User.UserStatus;
import org.example.Service.ExportService.AdminExportService;
import org.example.Service.WeatherService;
import org.example.Service.UserService.UserService;
import org.example.Utils.SessionContext;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
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
    @FXML private BarChart<String, Number> activityBarChart;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> sortByCombo;
    @FXML private ComboBox<String> sortOrderCombo;
    @FXML private Label dashboardTitleLabel;
    @FXML private Label usersSectionTitleLabel;
    @FXML private Label usersSectionSubtitleLabel;
    @FXML private Label statusChartTitleLabel;
    @FXML private Label activityTitleLabel;
    @FXML private Label quickStatsLabel;
    @FXML private VBox notificationBar;
    @FXML private ComboBox<String> notificationTargetCombo;
    @FXML private TextField notificationMessageField;

    private final UserService userService = new UserService();
    private final AdminExportService exportService = new AdminExportService();
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final ObservableList<User> masterUsers = FXCollections.observableArrayList();
    private FilteredList<User> filteredUsers;
    private SortedList<User> sortedUsers;
    private boolean english = false;
    private final WeatherService weatherService = new WeatherService();

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
        usersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        statusComboBox.getItems().setAll(UserStatus.values());
        if (sortByCombo != null) {
            sortByCombo.getItems().setAll("Date creation", "ID", "Nom", "Email", "Role", "Statut");
            sortByCombo.setValue("Date creation");
        }
        if (sortOrderCombo != null) {
            sortOrderCombo.getItems().setAll("DESC", "ASC");
            sortOrderCombo.setValue("DESC");
        }
        if (notificationTargetCombo != null) {
            notificationTargetCombo.getItems().setAll(
                    "Client selectionne",
                    "Tous les clients acceptes",
                    "Tous les clients"
            );
            notificationTargetCombo.setValue("Client selectionne");
        }
        if (notificationMessageField != null && notificationMessageField.getText().isBlank()) {
            notificationMessageField.setText("Votre compte a ete mis a jour par l'administration.");
        }
        if (notificationBar != null) {
            notificationBar.setVisible(false);
            notificationBar.setManaged(false);
        }

        filteredUsers = new FilteredList<>(masterUsers, u -> true);
        sortedUsers = new SortedList<>(filteredUsers);
        usersTable.setItems(sortedUsers);

        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldV, newV) -> applyFilterAndSort());
        }
        if (sortByCombo != null) {
            sortByCombo.valueProperty().addListener((obs, oldV, newV) -> applyFilterAndSort());
        }
        if (sortOrderCombo != null) {
            sortOrderCombo.valueProperty().addListener((obs, oldV, newV) -> applyFilterAndSort());
        }
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
    private void handleResetFilter() {
        if (searchField != null) searchField.clear();
        if (sortByCombo != null) sortByCombo.setValue("Date creation");
        if (sortOrderCombo != null) sortOrderCombo.setValue("DESC");
        applyFilterAndSort();
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
    private void goToAdminTasks() {
        navigateTo("/Admin/AdminTasks.fxml", "Admin Productivity / Ops", "/Styles/StyleWallet.css");
    }

    @FXML
    private void goToKycValidation() {
        navigateTo("/Admin/KycValidation.fxml", "Validation KYC", "/Styles/StyleWallet.css");
    }

    @FXML
    private void goToWalletDashboard() {
        navigateTo("/Wallet/dashboard.fxml", "Wallet", "/Styles/StyleWallet.css");
    }

    @FXML
    private void goToProducts() {
        loadScene("/Product/Admin/ListeProductGUI.fxml", "Produits");
    }
    @FXML
    private void goToSubProducts() {
        loadScene("/Product/Admin/ListeSubProductGUI.fxml", "Abonnements");
    }
    @FXML
    private void goToDashboardProduit() {
        loadScene("/Product/Admin/AdminDashboardGUI.fxml", "Dashboard Produit");
    }
    @FXML
    private void goToPublications() {
        setInfo("Module Publications: ouvrez depuis le menu principal.", false);
        goToMenu();
    }

    @FXML
    private void goToBudget() {
        loadScene("/Budget/AdminCategorieListeGUI.fxml", "Gestion Budget");
    }

    @FXML
    private void goToLoan() {
        navigateTo("/Loan/AdminDashboard.fxml", "Gestion des Loans",null);

    }

    @FXML
    private void handleToggleLanguage() {
        english = !english;
        if (dashboardTitleLabel != null) dashboardTitleLabel.setText(english ? "User Administration" : "Administration Utilisateurs");
        if (usersSectionTitleLabel != null) usersSectionTitleLabel.setText(english ? "Users List" : "Liste des utilisateurs");
        if (usersSectionSubtitleLabel != null) usersSectionSubtitleLabel.setText(english ? "Status monitoring and validation" : "Validation et suivi des statuts");
        if (statusChartTitleLabel != null) statusChartTitleLabel.setText(english ? "Account Status" : "Statut des comptes");
        if (activityTitleLabel != null) activityTitleLabel.setText(english ? "User Activity (10 days)" : "Activite utilisateurs (10 jours)");
        if (quickStatsLabel != null) quickStatsLabel.setText(english ? "Quick stats updated" : "Stats rapides mises a jour");
        setInfo(english ? "Language switched to English." : "Langue basculee en Francais.", false);
    }

    @FXML
    private void handleThemeBoost() {
        setInfo("Chargement meteo du jour...", false);
        CompletableFuture
                .supplyAsync(weatherService::getTodayWeatherSummary)
                .thenAccept(msg -> Platform.runLater(() -> {
                    setInfo(msg, false);
                    showWeatherCardDialog(msg);
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        String err = "Meteo indisponible: " + safe(ex.getMessage());
                        setInfo(err, true);
                        showWeatherCardDialog("Meteo indisponible pour le moment. " + err);
                    });
                    return null;
                });
    }

    @FXML
    private void handleFavoriteAction() {
        User best = masterUsers.stream()
                .filter(u -> u.getRole() == UserRole.CLIENT)
                .filter(u -> u.getStatus() == UserStatus.ACCEPTE)
                .max(Comparator.comparing(User::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .orElse(null);

        if (best == null) {
            setInfo("Aucun client accepte pour le meilleur utilisateur.", true);
            showAlert(Alert.AlertType.WARNING, "Meilleur utilisateur", "Aucun client accepte disponible.");
            return;
        }

        String msg = "Meilleur utilisateur: " + safe(best.getNom()) + " (" + safe(best.getEmail()) + ")";
        setInfo(msg, false);
        showBestUserCardDialog(best);
    }

    @FXML
    private void handleAlertsAction() {
        openNotificationDialog();
    }

    @FXML
    private void handleProfileAction() {
        User current = SessionContext.getInstance().getCurrentUser();
        String name = current == null ? "Admin" : safe(current.getNom());
        setInfo((english ? "Connected admin: " : "Admin connecte: ") + name, false);
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
    private void handleSendPanelNotification() {
        String target = notificationTargetCombo == null ? null : notificationTargetCombo.getValue();
        String msg = notificationMessageField == null ? "" : notificationMessageField.getText().trim();

        if (msg.isBlank()) {
            setInfo("Saisissez un message de notification.", true);
            return;
        }

        try {
            int sent = 0;
            if ("Client selectionne".equals(target)) {
                User selected = usersTable.getSelectionModel().getSelectedItem();
                if (selected == null || selected.getRole() != UserRole.CLIENT) {
                    setInfo("Selectionnez un client dans la liste.", true);
                    return;
                }
                userService.sendNotificationToUser(SessionContext.getInstance().getCurrentUser(), selected.getId(), msg);
                sent = 1;
            } else {
                for (User user : masterUsers) {
                    if (user.getRole() != UserRole.CLIENT) continue;
                    if ("Tous les clients acceptes".equals(target) && user.getStatus() != UserStatus.ACCEPTE) continue;
                    userService.sendNotificationToUser(SessionContext.getInstance().getCurrentUser(), user.getId(), msg);
                    sent++;
                }
            }
            setInfo("Notification envoyee a " + sent + " client(s).", false);
        } catch (Exception e) {
            setInfo("Erreur notification: " + e.getMessage(), true);
        }
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
            masterUsers.setAll(users);
            applyFilterAndSort();
            updateStats(users);
        } catch (Exception e) {
            setInfo("Impossible de charger les utilisateurs: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }

    private void applyFilterAndSort() {
        if (filteredUsers == null || sortedUsers == null) return;

        final String q = (searchField == null || searchField.getText() == null)
                ? ""
                : searchField.getText().trim().toLowerCase(Locale.ROOT);

        filteredUsers.setPredicate(user -> {
            if (q.isBlank()) return true;
            String id = String.valueOf(user.getId());
            String nom = safe(user.getNom()).toLowerCase(Locale.ROOT);
            String email = safe(user.getEmail()).toLowerCase(Locale.ROOT);
            String role = user.getRole() == null ? "" : user.getRole().name().toLowerCase(Locale.ROOT);
            String status = user.getStatus() == null ? "" : user.getStatus().name().toLowerCase(Locale.ROOT);
            return id.contains(q) || nom.contains(q) || email.contains(q) || role.contains(q) || status.contains(q);
        });

        String sortBy = (sortByCombo == null || sortByCombo.getValue() == null) ? "Date creation" : sortByCombo.getValue();
        Comparator<User> comparator = switch (sortBy) {
            case "ID" -> Comparator.comparingInt(User::getId);
            case "Nom" -> Comparator.comparing(u -> safe(u.getNom()).toLowerCase(Locale.ROOT));
            case "Email" -> Comparator.comparing(u -> safe(u.getEmail()).toLowerCase(Locale.ROOT));
            case "Role" -> Comparator.comparing(u -> u.getRole() == null ? "" : u.getRole().name());
            case "Statut" -> Comparator.comparing(u -> u.getStatus() == null ? "" : u.getStatus().name());
            default -> Comparator.comparing(User::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()));
        };

        String order = (sortOrderCombo == null || sortOrderCombo.getValue() == null) ? "DESC" : sortOrderCombo.getValue();
        if ("DESC".equalsIgnoreCase(order)) {
            comparator = comparator.reversed();
        }
        sortedUsers.setComparator(comparator);
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
        updateActivityChart(users);
        if (quickStatsLabel != null) {
            quickStatsLabel.setText("Accepte: " + statusCounts.get(UserStatus.ACCEPTE)
                    + " | En attente: " + statusCounts.get(UserStatus.EN_ATTENTE)
                    + " | Refuse: " + statusCounts.get(UserStatus.REFUSE));
        }
    }

    private void stylePieSlice(PieChart.Data data, String color) {
        if (data.getNode() != null) {
            data.getNode().setStyle("-fx-pie-color: " + color + ";");
        }
    }

    private void updateActivityChart(List<User> users) {
        if (activityBarChart == null) return;

        LocalDate today = LocalDate.now();
        Map<LocalDate, Integer> created = new TreeMap<>();
        Map<LocalDate, Integer> accepted = new TreeMap<>();
        for (int i = 9; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            created.put(d, 0);
            accepted.put(d, 0);
        }

        for (User user : users) {
            if (user.getCreatedAt() == null) continue;
            LocalDate d = user.getCreatedAt().toLocalDate();
            if (!created.containsKey(d)) continue;
            created.put(d, created.get(d) + 1);
            if (user.getStatus() == UserStatus.ACCEPTE) {
                accepted.put(d, accepted.get(d) + 1);
            }
        }

        XYChart.Series<String, Number> sCreated = new XYChart.Series<>();
        sCreated.setName(english ? "Registrations" : "Inscriptions");
        XYChart.Series<String, Number> sAccepted = new XYChart.Series<>();
        sAccepted.setName(english ? "Approved" : "Valides");

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM");
        for (LocalDate d : created.keySet()) {
            String key = d.format(fmt);
            sCreated.getData().add(new XYChart.Data<>(key, created.get(d)));
            sAccepted.getData().add(new XYChart.Data<>(key, accepted.get(d)));
        }

        activityBarChart.getData().setAll(sCreated, sAccepted);
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
    private void loadScene(String fxmlPath, String title) {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource(fxmlPath)
            );
            Stage stage = (Stage) Stage.getWindows()
                    .filtered(window -> window.isShowing())
                    .get(0);
            stage.setScene(new Scene(root));
            stage.setTitle(title);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void navigateTo(String fxmlPath, String title, String stylesheet) {
        try {
            URL fxmlUrl = getClass().getResource(fxmlPath);
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

    private void openNotificationDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Notifier un client");
        dialog.setHeaderText(null);

        ComboBox<User> targetCombo = new ComboBox<>();
        targetCombo.getItems().setAll(masterUsers.filtered(u -> u.getRole() == UserRole.CLIENT));
        targetCombo.setPrefWidth(420);
        targetCombo.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getId() + " - " + safe(item.getNom()) + " (" + safe(item.getEmail()) + ")");
            }
        });
        targetCombo.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getId() + " - " + safe(item.getNom()));
            }
        });

        TextField messageField = new TextField("Votre compte a ete mis a jour par l'administration.");
        messageField.setPrefWidth(420);
        messageField.setStyle("-fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #bfdbfe; -fx-padding: 10;");

        Label title = new Label("Envoyer une notification ciblee");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: 800; -fx-text-fill: #1d4f91;");

        Label clientLabel = new Label("Client");
        clientLabel.setStyle("-fx-font-weight: 700; -fx-text-fill: #1e3a5f;");
        Label messageLabel = new Label("Message");
        messageLabel.setStyle("-fx-font-weight: 700; -fx-text-fill: #1e3a5f;");

        VBox content = new VBox(10, title, clientLabel, targetCombo, messageLabel, messageField);
        content.setPadding(new Insets(14));
        content.setStyle("-fx-background-color: linear-gradient(to bottom right, #f5fbff, #e9f2ff); -fx-background-radius: 14; -fx-border-color: #c8dcf7; -fx-border-radius: 14;");
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setStyle("-fx-background-color: #eef6ff;");

        ButtonType sendType = new ButtonType("Envoyer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().setAll(sendType, ButtonType.CANCEL);
        Button sendButton = (Button) dialog.getDialogPane().lookupButton(sendType);
        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        if (sendButton != null) {
            sendButton.setStyle("-fx-background-color: linear-gradient(to right, #2563eb, #3b82f6); -fx-text-fill: white; -fx-font-weight: 700; -fx-background-radius: 10; -fx-padding: 9 18;");
        }
        if (cancelButton != null) {
            cancelButton.setStyle("-fx-background-color: white; -fx-text-fill: #1e3a5f; -fx-font-weight: 700; -fx-border-color: #9fbce4; -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 9 18;");
        }

        dialog.showAndWait().ifPresent(type -> {
            if (type != sendType) return;
            User target = targetCombo.getValue();
            String msg = messageField.getText() == null ? "" : messageField.getText().trim();
            if (target == null) {
                setInfo("Selectionnez un client.", true);
                return;
            }
            if (msg.isBlank()) {
                setInfo("Message vide.", true);
                return;
            }
            try {
                userService.sendNotificationToUser(SessionContext.getInstance().getCurrentUser(), target.getId(), msg);
                setInfo("Notification envoyee a " + safe(target.getNom()) + ".", false);
            } catch (Exception e) {
                setInfo("Erreur notification: " + e.getMessage(), true);
            }
        });
    }

    private void showInfoCardDialog(String title, String message, String iconGlyph, String iconBg, String iconColor) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(null);

        Label icon = new Label(iconGlyph);
        icon.setStyle("-fx-font-size: 22px; -fx-font-weight: 800; -fx-text-fill: " + iconColor + "; -fx-background-color: " + iconBg + "; -fx-background-radius: 999; -fx-padding: 12 14;");

        Label text = new Label(message);
        text.setWrapText(true);
        text.setStyle("-fx-text-fill: #13345c; -fx-font-size: 18px; -fx-font-weight: 700;");

        HBox row = new HBox(12, icon, text);
        row.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(row);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: linear-gradient(to bottom right, #f5fbff, #e7f2ff); -fx-background-radius: 14; -fx-border-color: #bfd8f7; -fx-border-radius: 14;");

        dialog.getDialogPane().setContent(card);
        dialog.getDialogPane().setStyle("-fx-background-color: #eef6ff;");
        dialog.getDialogPane().getButtonTypes().setAll(ButtonType.OK);
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        if (okButton != null) {
            okButton.setStyle("-fx-background-color: linear-gradient(to right, #2563eb, #3b82f6); -fx-text-fill: white; -fx-font-weight: 700; -fx-background-radius: 10; -fx-padding: 9 24;");
        }
        dialog.showAndWait();
    }

    private void showWeatherCardDialog(String message) {
        try {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Meteo du jour");
            dialog.setHeaderText(null);

            Label title = new Label("Etat meteo actuel");
            title.setStyle("-fx-text-fill: #1b4a7a; -fx-font-size: 18px; -fx-font-weight: 800;");

            Label icon = new Label("\u2600");
            icon.setStyle("-fx-font-size: 24px; -fx-font-weight: 900; -fx-text-fill: #9a6700; -fx-background-color: #fde68a; -fx-background-radius: 999; -fx-padding: 10 14;");

            Label text = new Label(message == null ? "Meteo indisponible." : message);
            text.setWrapText(true);
            text.setStyle("-fx-text-fill: #163d67; -fx-font-size: 15px; -fx-font-weight: 700;");

            HBox row = new HBox();
            row.setSpacing(12);
            row.setAlignment(Pos.CENTER_LEFT);
            row.getChildren().addAll(icon, text);

            VBox card = new VBox();
            card.setSpacing(12);
            card.getChildren().addAll(title, row);
            card.setPadding(new Insets(16));
            card.setStyle("-fx-background-color: linear-gradient(to bottom right, #fff9e8, #edf6ff); -fx-background-radius: 14; -fx-border-color: #f1d38a; -fx-border-radius: 14;");

            dialog.getDialogPane().setContent(card);
            dialog.getDialogPane().setStyle("-fx-background-color: #eef6ff;");
            dialog.getDialogPane().getButtonTypes().setAll(ButtonType.OK);
            Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            if (okButton != null) {
                okButton.setStyle("-fx-background-color: linear-gradient(to right, #2563eb, #3b82f6); -fx-text-fill: white; -fx-font-weight: 700; -fx-background-radius: 10; -fx-padding: 9 24;");
            }
            dialog.showAndWait();
        } catch (Exception e) {
            showAlert(Alert.AlertType.INFORMATION, "Meteo du jour", message == null ? "Meteo indisponible." : message);
        }
    }

    private void showBestUserCardDialog(User best) {
        try {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Meilleur utilisateur");
            dialog.setHeaderText(null);

            Label icon = new Label("\u2605");
            icon.setStyle("-fx-font-size: 24px; -fx-font-weight: 900; -fx-text-fill: #6d28d9; -fx-background-color: #ede9fe; -fx-background-radius: 999; -fx-padding: 10 14;");

            Label title = new Label("Top utilisateur du moment");
            title.setStyle("-fx-text-fill: #4c1d95; -fx-font-size: 18px; -fx-font-weight: 800;");

            Label name = new Label("Nom: " + safe(best == null ? "" : best.getNom()));
            Label email = new Label("Email: " + safe(best == null ? "" : best.getEmail()));
            Label status = new Label("Statut: " + (best == null || best.getStatus() == null ? "-" : best.getStatus().name()));
            name.setStyle("-fx-text-fill: #1f2a44; -fx-font-size: 14px; -fx-font-weight: 700;");
            email.setStyle("-fx-text-fill: #1f2a44; -fx-font-size: 14px; -fx-font-weight: 700;");
            status.setStyle("-fx-text-fill: #1f2a44; -fx-font-size: 14px; -fx-font-weight: 700;");

            VBox details = new VBox();
            details.setSpacing(8);
            details.getChildren().addAll(title, name, email, status);

            HBox row = new HBox();
            row.setSpacing(12);
            row.setAlignment(Pos.CENTER_LEFT);
            row.getChildren().addAll(icon, details);

            VBox card = new VBox();
            card.getChildren().add(row);
            card.setPadding(new Insets(16));
            card.setStyle("-fx-background-color: linear-gradient(to bottom right, #f7f1ff, #eaf2ff); -fx-background-radius: 14; -fx-border-color: #d8c8fb; -fx-border-radius: 14;");

            dialog.getDialogPane().setContent(card);
            dialog.getDialogPane().setStyle("-fx-background-color: #eef6ff;");
            dialog.getDialogPane().getButtonTypes().setAll(ButtonType.OK);
            Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            if (okButton != null) {
                okButton.setStyle("-fx-background-color: linear-gradient(to right, #6d28d9, #8b5cf6); -fx-text-fill: white; -fx-font-weight: 700; -fx-background-radius: 10; -fx-padding: 9 24;");
            }
            dialog.showAndWait();
        } catch (Exception e) {
            String msg = best == null
                    ? "Aucun utilisateur disponible."
                    : "Meilleur utilisateur: " + safe(best.getNom()) + " (" + safe(best.getEmail()) + ")";
            showAlert(Alert.AlertType.INFORMATION, "Meilleur utilisateur", msg);
        }
    }

}
