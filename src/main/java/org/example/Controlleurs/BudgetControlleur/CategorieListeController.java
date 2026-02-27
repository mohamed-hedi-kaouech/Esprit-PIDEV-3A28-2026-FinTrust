package org.example.Controlleurs.BudgetControlleur;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.example.Model.Budget.Categorie;
import org.example.Service.BudgetService.BudgetService;
import org.example.Service.BudgetService.AlerteService;
import javafx.scene.layout.GridPane;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class CategorieListeController implements Initializable {

    // Stats Labels
    @FXML private Label lblTotalCategories;
    @FXML private Label lblBudgetTotal;
    @FXML private Label lblFilteredCount;
    @FXML private Label lblSommeBudgets;
    @FXML private Label lblSommeSeuils;

    // Search
    @FXML private TextField tfSearch;
    // Notifications
    @FXML private MenuButton notificationMenu;

    // ListView
    @FXML private ListView<Categorie> categorieListView;

    // Data
    private ObservableList<Categorie> categorieList = FXCollections.observableArrayList();
    private FilteredList<Categorie> filteredList;
    private BudgetService BS;
    private AlerteService alerteService = new AlerteService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        BS = new BudgetService();

        // load alerts into menu
        loadAlertsMenu();

        setupListView();
        setupSearchFilter();
        loadCategories();
        updateStatistics();
    }

    private void setupListView() {
        // Set custom cell factory for ListView
        categorieListView.setCellFactory(listView -> new CategorieListCell());

        // Set placeholder
        Label placeholder = new Label("Aucune catégorie disponible\n\nCliquez sur 'Ajouter Catégorie' pour commencer");
        placeholder.setStyle("-fx-text-fill: #64748b; -fx-font-size: 14px; -fx-font-style: italic;");
        categorieListView.setPlaceholder(placeholder);
    }

    private void setupSearchFilter() {
        filteredList = new FilteredList<>(categorieList, p -> true);
        categorieListView.setItems(filteredList);

        if (tfSearch != null) {
            tfSearch.textProperty().addListener((observable, oldValue, newValue) -> {
                filterCategories(newValue);
            });
        }
    }

    private void loadCategories() {
        categorieList.clear();
        categorieList.addAll(BS.ReadAll());

        // Calculate statut for each category
        for (Categorie cat : categorieList) {
//            cat.setStatut(calculateStatut(cat.getBudgetPrevu(), cat.getSeuilAlerte()));
        }

        updateStatistics();
        // refresh alerts list when categories change
        loadAlertsMenu();
    }

    private void loadAlertsMenu() {
        if (notificationMenu == null) return;
        notificationMenu.getItems().clear();
        // Fetch latest alerts
        try {
            java.util.List<org.example.Model.Budget.Alerte> alerts = alerteService.ReadAll();
            if (alerts.isEmpty()) {
                MenuItem empty = new MenuItem("Aucune alerte");
                empty.setDisable(true);
                notificationMenu.getItems().add(empty);
                return;
            }

            for (org.example.Model.Budget.Alerte a : alerts) {
                String text = String.format("[%s] %s (%.2f DT)",
                        a.getCreatedAt() != null ? a.getCreatedAt().toString() : "--",
                        a.getMessage(), a.getSeuil());
                MenuItem mi = new MenuItem(text);
                mi.setOnAction(ev -> showAlerteDetails(a));
                notificationMenu.getItems().add(mi);
            }
        } catch (Exception e) {
            e.printStackTrace();
            MenuItem err = new MenuItem("Erreur lors du chargement");
            err.setDisable(true);
            notificationMenu.getItems().add(err);
        }
    }

    private void showAlerteDetails(org.example.Model.Budget.Alerte a) {
        StringBuilder sb = new StringBuilder();
        sb.append("Alerte pour Catégorie ID: ").append(a.getIdCategorie()).append("\n\n");
        sb.append("Message: ").append(a.getMessage()).append("\n");
        sb.append("Seuil: ").append(String.format("%.2f DT", a.getSeuil())).append("\n");
        sb.append("Active: ").append(a.isActive() ? "Oui" : "Non");

        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Détails Alerte");
        info.setHeaderText(null);
        info.setContentText(sb.toString());
        info.showAndWait();
    }

    @FXML
    private void openCreateAlerte() {
        try {
            // Build dialog with category choice, message and seuil
            Dialog<org.example.Model.Budget.Alerte> dialog = new Dialog<>();
            dialog.setTitle("Créer une Alerte");

            ButtonType createBtn = new ButtonType("Créer", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(createBtn, ButtonType.CANCEL);

            // Content
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            ComboBox<Categorie> cbCategories = new ComboBox<>();
            cbCategories.getItems().addAll(categorieList);
            cbCategories.setPrefWidth(300);

            TextField tfMessage = new TextField();
            tfMessage.setPromptText("Message de l'alerte");

            TextField tfSeuil = new TextField();
            tfSeuil.setPromptText("Seuil (ex: 100.0)");

            grid.add(new Label("Catégorie:"), 0, 0);
            grid.add(cbCategories, 1, 0);
            grid.add(new Label("Message:"), 0, 1);
            grid.add(tfMessage, 1, 1);
            grid.add(new Label("Seuil:"), 0, 2);
            grid.add(tfSeuil, 1, 2);

            dialog.getDialogPane().setContent(grid);

            // Convert result
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == createBtn) {
                    Categorie chosen = cbCategories.getValue();
                    if (chosen == null) return null;
                    String msg = tfMessage.getText();
                    double s = 0;
                    try { s = Double.parseDouble(tfSeuil.getText()); } catch (NumberFormatException ex) { s = chosen.getSeuilAlerte(); }
                    org.example.Model.Budget.Alerte a = new org.example.Model.Budget.Alerte(chosen.getIdCategorie(), msg, s);
                    return a;
                }
                return null;
            });

            Optional<org.example.Model.Budget.Alerte> result = dialog.showAndWait();
            result.ifPresent(a -> {
                alerteService.Add(a);
                showSuccessAlert("Succès", "Alerte créée.");
                loadAlertsMenu();
            });

        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Erreur", "Impossible d'ouvrir le dialogue de création d'alerte.");
        }
    }

    private String calculateStatut(double budget, double seuil) {
        if (budget == 0) return "🔴 Invalide";

        double ratio = (seuil / budget) * 100;

        if (ratio < 30) {
            return "🟢 OK";
        } else if (ratio < 70) {
            return "🟡 Attention";
        } else {
            return "🔴 Critique";
        }
    }

    private void updateStatistics() {
        int total = categorieList.size();
        double totalBudget = categorieList.stream()
                .mapToDouble(Categorie::getBudgetPrevu)
                .sum();
        double totalSeuils = categorieList.stream()
                .mapToDouble(Categorie::getSeuilAlerte)
                .sum();

        lblTotalCategories.setText(String.valueOf(total));
        lblBudgetTotal.setText(String.format("%.2f DT", totalBudget));
        lblSommeBudgets.setText(String.format("%.2f DT", totalBudget));
        lblSommeSeuils.setText(String.format("%.2f DT", totalSeuils));

        updateFilteredCount();
    }

    private void updateFilteredCount() {
        int filtered = filteredList.size();
        lblFilteredCount.setText("Affichage: " + filtered + " catégorie" + (filtered > 1 ? "s" : ""));
    }

    private void filterCategories(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            filteredList.setPredicate(p -> true);
        } else {
            String lowerCaseFilter = searchText.toLowerCase();

            filteredList.setPredicate(categorie -> {
                if (categorie.getNomCategorie().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                if (String.valueOf(categorie.getIdCategorie()).contains(lowerCaseFilter)) {
                    return true;
                }
                return false;
            });
        }

        updateFilteredCount();
    }

    @FXML
    private void resetSearch() {
        if (tfSearch != null) {
            tfSearch.clear();
        }
        filteredList.setPredicate(p -> true);
        updateFilteredCount();
    }

    @FXML
    private void goToCreate() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Budget/CategorieCreateGUI.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) categorieListView.getScene().getWindow();
            Scene scene = new Scene(root);

            stage.setScene(scene);
            stage.setTitle("Créer une Catégorie");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Erreur", "Impossible de charger la page de création.");
        }
    }

    private void goToUpdate(Categorie categorie) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Budget/CategorieUpdate.fxml"));
            Parent root = loader.load();

            // Get controller and load category data
            CategorieUpdateController controller = loader.getController();
            controller.loadCategorie(categorie);

            Stage stage = (Stage) categorieListView.getScene().getWindow();
            Scene scene = new Scene(root);

            stage.setScene(scene);
            stage.setTitle("Modifier la Catégorie");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Erreur", "Impossible de charger la page de modification.");
        }
    }

    private void deleteCategorie(Categorie categorie) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation de suppression");
        confirmAlert.setHeaderText("Supprimer la catégorie");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer la catégorie \"" +
                categorie.getIdCategorie() + "\"?\n\n" +
                "Budget: " + String.format("%.2f DT", categorie.getBudgetPrevu()) + "\n" +
                "Seuil: " + String.format("%.2f DT", categorie.getSeuilAlerte()) + "\n\n" +
                "Cette action est irréversible!");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (BS.delete(categorie.getIdCategorie())) {
                showSuccessAlert("Succès", "La catégorie a été supprimée avec succès!");
                loadCategories();
            } else {
                showErrorAlert("Erreur", "Erreur lors de la suppression de la catégorie.");
            }
        }
    }

    @FXML
    private void goBackToMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MenuGUI.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) categorieListView.getScene().getWindow();
            Scene scene = new Scene(root);

            stage.setScene(scene);
            stage.setTitle("Système de Gestion Bancaire");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Erreur", "Impossible de retourner au menu.");
        }
    }

    // Open Item list filtered by category
    private void openItemsForCategory(Categorie categorie) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Budget/ItemListGUI.fxml"));
            Parent root = loader.load();

            ItemListController controller = loader.getController();
            controller.loadItemsForCategory(categorie);

            Stage stage = (Stage) categorieListView.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Items de la catégorie: " + categorie.getNomCategorie());
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Erreur", "Impossible d'ouvrir la liste des items pour cette catégorie.");
        }
    }

    // Alert methods
    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ==================== Custom ListView Cell ====================
    private class CategorieListCell extends ListCell<Categorie> {
        private final VBox container;
        private final HBox headerBox;
        private final HBox bodyBox;
        private final HBox footerBox;

        private final Label idLabel;
        private final Label nomLabel;
        private final Label budgetLabel;
        private final Label seuilLabel;
        private final Label statutLabel;
        private final Label percentageLabel;
        private final Button updateButton;
        private final Button deleteButton;

        public CategorieListCell() {
            super();

            // Container
            container = new VBox(12);
            container.setPadding(new Insets(15));
            container.getStyleClass().add("categorie-card");

            // Header with ID, Nom, and Statut
            headerBox = new HBox(15);
            headerBox.setAlignment(Pos.CENTER_LEFT);

            idLabel = new Label();
            idLabel.getStyleClass().add("categorie-id");
            idLabel.setFont(Font.font("System Bold", 14));

            nomLabel = new Label();
            nomLabel.getStyleClass().add("categorie-nom");
            nomLabel.setFont(Font.font("System Bold", 16));
            HBox.setHgrow(nomLabel, Priority.ALWAYS);

            statutLabel = new Label();
            statutLabel.getStyleClass().add("categorie-statut-badge");

            headerBox.getChildren().addAll(idLabel, nomLabel, statutLabel);

            // Body with Budget and Seuil
            bodyBox = new HBox(30);
            bodyBox.setAlignment(Pos.CENTER_LEFT);

            VBox budgetBox = new VBox(3);
            Label budgetTitle = new Label("Budget Prévu");
            budgetTitle.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");
            budgetLabel = new Label();
            budgetLabel.getStyleClass().add("categorie-budget");
            budgetLabel.setFont(Font.font("System Bold", 16));
            budgetBox.getChildren().addAll(budgetTitle, budgetLabel);

            VBox seuilBox = new VBox(3);
            Label seuilTitle = new Label("Seuil d'Alerte");
            seuilTitle.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");
            seuilLabel = new Label();
            seuilLabel.getStyleClass().add("categorie-seuil");
            seuilLabel.setFont(Font.font("System Bold", 16));
            seuilBox.getChildren().addAll(seuilTitle, seuilLabel);

            VBox percentageBox = new VBox(3);
            Label percentageTitle = new Label("Pourcentage");
            percentageTitle.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");
            percentageLabel = new Label();
            percentageLabel.getStyleClass().add("categorie-percentage");
            percentageLabel.setFont(Font.font("System Bold", 14));
            percentageBox.getChildren().addAll(percentageTitle, percentageLabel);

            bodyBox.getChildren().addAll(budgetBox, seuilBox, percentageBox);

            // Footer with Action Buttons
            footerBox = new HBox(10);
            footerBox.setAlignment(Pos.CENTER_RIGHT);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            updateButton = new Button("✏️ Modifier");
            updateButton.getStyleClass().add("btn-update-card");

            deleteButton = new Button("🗑️ Supprimer");
            deleteButton.getStyleClass().add("btn-delete-card");

            footerBox.getChildren().addAll(spacer, updateButton, deleteButton);

            // Add separator
            Separator separator = new Separator();
            separator.getStyleClass().add("card-separator");

            // Add all sections to container
            container.getChildren().addAll(headerBox, separator, bodyBox, footerBox);
        }

        @Override
        protected void updateItem(Categorie categorie, boolean empty) {
            super.updateItem(categorie, empty);

            if (empty || categorie == null) {
                setGraphic(null);
            } else {
                // Set ID
                idLabel.setText("#" + categorie.getIdCategorie());

                // Set Nom
                nomLabel.setText(categorie.getNomCategorie());

                // Set Budget
                budgetLabel.setText(String.format("%.2f DT", categorie.getBudgetPrevu()));

                // Set Seuil
                seuilLabel.setText(String.format("%.2f DT", categorie.getSeuilAlerte()));

                // Calculate and set percentage
                double percentage = categorie.getBudgetPrevu() > 0 ?
                        (categorie.getSeuilAlerte() / categorie.getBudgetPrevu()) * 100 : 0;
                percentageLabel.setText(String.format("%.1f%%", percentage));

                // Set percentage color
                if (percentage < 30) {
                    percentageLabel.setStyle("-fx-text-fill: #22c55e;");
                } else if (percentage < 70) {
                    percentageLabel.setStyle("-fx-text-fill: #f59e0b;");
                } else {
                    percentageLabel.setStyle("-fx-text-fill: #ef4444;");
                }

                // Set Statut
//                String statut = categorie.getStatut() != null ? categorie.getStatut() :
//                        calculateStatut(categorie.getBudgetPrevu(), categorie.getSeuilAlerte());
//                statutLabel.setText(statut);

                // Style statut badge
//                if (statut.contains("OK") || statut.contains("🟢")) {
//                    statutLabel.setStyle("-fx-background-color: rgba(34, 197, 94, 0.2); " +
//                            "-fx-text-fill: #22c55e; -fx-background-radius: 12px; " +
//                            "-fx-padding: 4px 12px; -fx-font-weight: bold; -fx-font-size: 11px;");
//                } else if (statut.contains("Attention") || statut.contains("🟡")) {
//                    statutLabel.setStyle("-fx-background-color: rgba(245, 158, 11, 0.2); " +
//                            "-fx-text-fill: #f59e0b; -fx-background-radius: 12px; " +
//                            "-fx-padding: 4px 12px; -fx-font-weight: bold; -fx-font-size: 11px;");
//                } else {
//                    statutLabel.setStyle("-fx-background-color: rgba(239, 68, 68, 0.2); " +
//                            "-fx-text-fill: #ef4444; -fx-background-radius: 12px; " +
//                            "-fx-padding: 4px 12px; -fx-font-weight: bold; -fx-font-size: 11px;");
//                }

                // Set button actions
                    updateButton.setOnAction(e -> goToUpdate(categorie));
                    deleteButton.setOnAction(e -> deleteCategorie(categorie));

                    // Click on the card (except buttons) opens the items list for this category
                    container.setOnMouseClicked(e -> {
                        if (e.getTarget() instanceof Button) return;
                        openItemsForCategory(categorie);
                    });

                setGraphic(container);
            }
        }
    }
}