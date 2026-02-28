package org.example.Controlleurs.AdminControlleur;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.Model.AdminOps.AdminTask;
import org.example.Model.AdminOps.AdminTaskPriority;
import org.example.Model.AdminOps.AdminTaskStatus;
import org.example.Service.AdminOps.AdminOpsService;
import org.example.Service.AdminOps.AdminOpsSnapshot;
import org.example.Service.AdminOps.AdminTaskAuditEntry;
import org.example.Utils.SessionContext;

import java.net.URL;
import java.time.format.DateTimeFormatter;

public class AdminTasksController {

    @FXML private ComboBox<String> filterCombo;
    @FXML private TextField titleField;
    @FXML private TextArea descriptionArea;
    @FXML private ComboBox<AdminTaskPriority> priorityCombo;
    @FXML private TextField tagsField;
    @FXML private DatePicker dueDatePicker;

    @FXML private ListView<AdminTask> todoList;
    @FXML private ListView<AdminTask> doingList;
    @FXML private ListView<AdminTask> doneList;
    @FXML private ListView<String> auditList;

    @FXML private Label urgentCountLabel;
    @FXML private Label overdueCountLabel;
    @FXML private Label todayCountLabel;
    @FXML private Label slaRateLabel;
    @FXML private Label starsLabel;
    @FXML private Label pointsLabel;
    @FXML private Label streakLabel;
    @FXML private Label badgeLabel;
    @FXML private Label starToastLabel;
    @FXML private Label infoLabel;

    private final AdminOpsService adminOpsService = new AdminOpsService();
    private static final DateTimeFormatter AUDIT_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    private void initialize() {
        if (!SessionContext.getInstance().isAdmin()) {
            navigateTo("/Auth/Login.fxml", "Connexion", "/Styles/StyleWallet.css");
            return;
        }

        filterCombo.getItems().setAll("MY", "ALL", "URGENT", "OVERDUE");
        filterCombo.setValue("MY");
        priorityCombo.getItems().setAll(AdminTaskPriority.values());
        priorityCombo.setValue(AdminTaskPriority.MEDIUM);

        configureTaskList(todoList);
        configureTaskList(doingList);
        configureTaskList(doneList);
        wireMutualSelection();

        refreshBoard();
    }

    @FXML
    private void handleCreateTask() {
        String title = titleField == null ? "" : titleField.getText();
        String description = descriptionArea == null ? "" : descriptionArea.getText();
        String tags = tagsField == null ? "" : tagsField.getText();

        try {
            adminOpsService.createTask(
                    SessionContext.getInstance().getCurrentUser().getId(),
                    title,
                    description,
                    priorityCombo.getValue(),
                    tags,
                    dueDatePicker == null ? null : dueDatePicker.getValue()
            );
            clearCreateForm();
            refreshBoard();
            setInfo("Tache creee avec succes.", false);
        } catch (Exception e) {
            setInfo("Erreur creation tache: " + e.getMessage(), true);
        }
    }

    @FXML
    private void handleRefresh() {
        refreshBoard();
    }

    @FXML
    private void handleFilterChanged() {
        refreshBoard();
    }

    @FXML
    private void handleMoveToTodo() {
        moveSelected(AdminTaskStatus.TODO);
    }

    @FXML
    private void handleMoveToDoing() {
        moveSelected(AdminTaskStatus.DOING);
    }

    @FXML
    private void handleMoveToDone() {
        moveSelected(AdminTaskStatus.DONE);
    }

    @FXML
    private void handleComplete() {
        AdminTask selected = selectedTask();
        if (selected == null) {
            setInfo("Selectionnez une tache.", true);
            return;
        }
        try {
            int stars = adminOpsService.completeTask(selected.getId(), SessionContext.getInstance().getCurrentUser().getId());
            refreshBoard();
            showStarToast(stars);
            setInfo("Tache terminee avec succes.", false);
        } catch (Exception e) {
            setInfo("Erreur completion: " + e.getMessage(), true);
        }
    }

    @FXML
    private void handleBack() {
        goToDashboard();
    }

    @FXML
    private void goToDashboard() {
        navigateTo("/Admin/UserDashboard.fxml", "Dashboard Admin", "/Styles/StyleWallet.css");
    }

    @FXML
    private void goToAnalyticsDashboard() {
        navigateTo("/Admin/AnalyticsDashboard.fxml", "Data Analytics Dashboard", "/Styles/StyleWallet.css");
    }

    @FXML
    private void goToAdminTasks() {
        handleRefresh();
    }

    @FXML
    private void goToKycValidation() {
        navigateTo("/Admin/KycValidation.fxml", "Validation KYC", "/Styles/StyleWallet.css");
    }

    @FXML
    private void goToCreateUserForm() {
        navigateTo("/Admin/UserCreate.fxml", "Creation Utilisateur", "/Styles/StyleWallet.css");
    }

    @FXML
    private void goToMenu() {
        navigateTo("/MenuGUI.fxml", "Menu Principal", "/Styles/MenuStyle.css");
    }

    @FXML
    private void handleLogout() {
        SessionContext.getInstance().logout();
        navigateTo("/Auth/Login.fxml", "Connexion", "/Styles/StyleWallet.css");
    }

    @FXML
    private void goToWalletDashboard() {
        navigateTo("/Wallet/dashboard.fxml", "Wallet", "/Styles/StyleWallet.css");
    }

    @FXML
    private void goToProducts() {
        navigateTo("/Product/ListeProductGUI.fxml", "Produits", "/Styles/StyleWallet.css");
    }

    @FXML
    private void goToPublications() {
        setInfo("Module Publications: ouvrez depuis le menu principal.", false);
        goToMenu();
    }

    @FXML
    private void goToBudget() {
        setInfo("Module Budget: ouvrez depuis le menu principal.", false);
        goToMenu();
    }

    @FXML
    private void goToLoans() {
        setInfo("Module Loans: ouvrez depuis le menu principal.", false);
        goToMenu();
    }

    private void moveSelected(AdminTaskStatus target) {
        AdminTask selected = selectedTask();
        if (selected == null) {
            setInfo("Selectionnez une tache.", true);
            return;
        }
        try {
            adminOpsService.moveTask(selected.getId(), SessionContext.getInstance().getCurrentUser().getId(), target);
            refreshBoard();
            setInfo("Tache deplacee vers " + target.name() + ".", false);
        } catch (Exception e) {
            setInfo("Erreur deplacement: " + e.getMessage(), true);
        }
    }

    private void refreshBoard() {
        try {
            int adminId = SessionContext.getInstance().getCurrentUser().getId();
            String filter = filterCombo == null ? "MY" : filterCombo.getValue();
            AdminOpsSnapshot snapshot = adminOpsService.getSnapshot(adminId, filter);

            todoList.getItems().setAll(snapshot.todo());
            doingList.getItems().setAll(snapshot.doing());
            doneList.getItems().setAll(snapshot.done());

            urgentCountLabel.setText(String.valueOf(snapshot.urgentCount()));
            overdueCountLabel.setText(String.valueOf(snapshot.overdueCount()));
            todayCountLabel.setText(String.valueOf(snapshot.todayCreatedCount()));
            slaRateLabel.setText(String.format("%.1f %%", snapshot.beforeDeadlineRate()));

            starsLabel.setText(String.valueOf(snapshot.reward().totalStars()));
            pointsLabel.setText(String.valueOf(snapshot.reward().totalPoints()));
            streakLabel.setText(snapshot.reward().streakDays() + " jours");
            badgeLabel.setText(snapshot.reward().taskFinisherBadge() ? "Task Finisher" : "Aucun badge");

            auditList.getItems().setAll(snapshot.auditEntries().stream().map(this::toAuditLine).toList());
            starToastLabel.setText("");
        } catch (Exception e) {
            setInfo("Erreur chargement Kanban: " + e.getMessage(), true);
        }
    }

    private String toAuditLine(AdminTaskAuditEntry entry) {
        String at = entry.createdAt() == null ? "" : entry.createdAt().format(AUDIT_DATE_FORMAT);
        String actor = entry.actorEmail() == null ? "admin" : entry.actorEmail();
        String stars = entry.starsEarned() > 0 ? " | +" + entry.starsEarned() + " star(s)" : "";
        return at + " | " + actor + " | #" + entry.taskId() + " | " + entry.action() + stars;
    }

    private void configureTaskList(ListView<AdminTask> list) {
        list.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(AdminTask item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                Label title = new Label(item.getTitle());
                title.getStyleClass().add("task-card-title");
                title.setWrapText(true);

                String metaText = "Priorite: " + item.getPriority().name()
                        + " | Deadline: " + (item.getDueDate() == null ? "-" : item.getDueDate().toString());
                Label meta = new Label(metaText);
                meta.getStyleClass().add("task-card-meta");

                Label tags = new Label("Tags: " + (item.getTags() == null || item.getTags().isBlank() ? "-" : item.getTags()));
                tags.getStyleClass().add("task-card-tags");

                Label auto = new Label(item.isAutoGenerated() ? "Auto" : "Manuel");
                auto.getStyleClass().add(item.isAutoGenerated() ? "task-chip-auto" : "task-chip-manual");

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                HBox top = new HBox(8, title, spacer, auto);

                VBox box = new VBox(4, top, meta, tags);
                box.getStyleClass().add("task-card");
                if (item.isOverdue()) {
                    box.getStyleClass().add("task-priority-overdue");
                } else {
                    switch (item.getPriority()) {
                        case URGENT -> box.getStyleClass().add("task-priority-urgent");
                        case HIGH -> box.getStyleClass().add("task-priority-high");
                        case MEDIUM -> box.getStyleClass().add("task-priority-medium");
                        case LOW -> box.getStyleClass().add("task-priority-low");
                    }
                }

                setGraphic(box);
                setText(null);
            }
        });
    }

    private void wireMutualSelection() {
        todoList.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                doingList.getSelectionModel().clearSelection();
                doneList.getSelectionModel().clearSelection();
            }
        });
        doingList.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                todoList.getSelectionModel().clearSelection();
                doneList.getSelectionModel().clearSelection();
            }
        });
        doneList.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                todoList.getSelectionModel().clearSelection();
                doingList.getSelectionModel().clearSelection();
            }
        });
    }

    private AdminTask selectedTask() {
        AdminTask fromTodo = todoList.getSelectionModel().getSelectedItem();
        if (fromTodo != null) return fromTodo;
        AdminTask fromDoing = doingList.getSelectionModel().getSelectedItem();
        if (fromDoing != null) return fromDoing;
        return doneList.getSelectionModel().getSelectedItem();
    }

    private void clearCreateForm() {
        titleField.clear();
        descriptionArea.clear();
        tagsField.clear();
        dueDatePicker.setValue(null);
        priorityCombo.setValue(AdminTaskPriority.MEDIUM);
    }

    private void showStarToast(int stars) {
        if (starToastLabel == null) return;
        starToastLabel.setText("Tache terminee: +" + stars + " ★");
        starToastLabel.setStyle("-fx-text-fill: #0f4e96; -fx-font-weight: 800;");
    }

    private void setInfo(String message, boolean isError) {
        if (infoLabel == null) return;
        infoLabel.setText(message == null ? "" : message);
        infoLabel.setStyle(isError ? "-fx-text-fill: #b91c1c;" : "-fx-text-fill: #166534;");
    }

    private void navigateTo(String fxmlPath, String title, String stylesheetPath) {
        try {
            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                setInfo("FXML introuvable: " + fxmlPath, true);
                return;
            }
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            if (stylesheetPath != null && !stylesheetPath.isBlank()) {
                URL cssUrl = getClass().getResource(stylesheetPath);
                if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());
            }
            Stage stage = (Stage) infoLabel.getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            setInfo("Navigation impossible: " + e.getMessage(), true);
        }
    }
}
