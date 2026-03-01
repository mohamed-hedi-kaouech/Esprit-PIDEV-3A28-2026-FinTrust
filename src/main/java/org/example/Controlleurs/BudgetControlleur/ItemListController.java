package org.example.Controlleurs.BudgetControlleur;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;
import org.example.Controlleurs.BudgetControlleur.ItemUpdateController;
import org.example.Model.Budget.Categorie;
import org.example.Model.Budget.Item;
import org.example.Service.BudgetService.ItemService;
import org.example.Service.BudgetService.AlerteService;
import org.example.Model.Budget.Alerte;
import org.example.Utils.NotificationCenter;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ItemListController implements Initializable {

    @FXML private ListView<Item> itemListView;
    @FXML private Label lblTotalItems;
    @FXML private Label lblMontantTotal;
    @FXML private Label lblSommeMontants;
    @FXML private Label lblSommeItems;
    @FXML private TextField tfSearch;

    private ItemService itemService;
    private ObservableList<Item> items;
    private Categorie currentCategory;
    // Track whether we've already shown the alert for the current category
    private boolean seuilAlertShown = false;
    private AlerteService alerteService = new AlerteService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        itemService = new ItemService();
        loadItems();

        // Styled placeholder like categories list
        Label placeholder = new Label("Aucun item disponible");
        placeholder.getStyleClass().add("placeholder");
        itemListView.setPlaceholder(placeholder);

        // Custom cell with update/delete actions
        itemListView.setCellFactory(lv -> new ListCell<Item>() {
            private final VBox container = new VBox(8);
            private final HBox topRow = new HBox(20);
            private final VBox itemSection = new VBox(4);
            private final Label lblItemLabel = new Label("Item");
            private final Label lblLibelle = new Label();
            private final VBox montantSection = new VBox(4);
            private final Label lblMontantLabel = new Label("Montant");
            private final Label lblMontant = new Label();
            private final HBox footer = new HBox(8);
            private final Button btnUpdate = new Button("✏️ Modifier");
            private final Button btnDelete = new Button("🗑️ Supprimer");

            {
                // Item section with label and value
                lblItemLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666666; -fx-font-weight: bold;");
                lblLibelle.getStyleClass().add("categorie-nom");
                itemSection.getChildren().addAll(lblItemLabel, lblLibelle);

                // Montant section with label and value
                lblMontantLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666666; -fx-font-weight: bold;");
                lblMontant.getStyleClass().add("categorie-budget");
                montantSection.getChildren().addAll(lblMontantLabel, lblMontant);

                // Top row: both sections displayed nicely
                HBox.setHgrow(itemSection, Priority.ALWAYS);
                topRow.getChildren().addAll(itemSection, montantSection);

                // Footer: action buttons
                footer.getChildren().addAll(new Region(), btnUpdate, btnDelete);

                container.getChildren().addAll(topRow, footer);

                // Card styling and sizing for full-width appearance
                container.getStyleClass().add("categorie-card");
                container.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(container, Priority.ALWAYS);
                btnUpdate.getStyleClass().add("btn-update-card");
                btnDelete.getStyleClass().add("btn-delete-card");
            }

            @Override
            protected void updateItem(Item item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    lblLibelle.setText(item.getLibelle());
                    lblMontant.setText(String.format("%.2f DT", item.getMontant()));

                    btnUpdate.setOnAction(e -> openUpdate(item));
                    // Make the whole card clickable like categorie list
                    container.setOnMouseClicked(evt -> {
                        if (evt.getClickCount() == 1) {
                            openUpdate(item);
                        }
                    });
                    container.setOnMouseEntered(evt -> container.setStyle("-fx-cursor: hand;"));
                    container.setOnMouseExited(evt -> container.setStyle("-fx-cursor: default;"));
                    btnDelete.setOnAction(e -> {
                        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                        confirm.setTitle("Confirmer la suppression");
                        confirm.setHeaderText(null);
                        confirm.setContentText("Supprimer l'item '" + item.getLibelle() + "' ?");
                        confirm.showAndWait().ifPresent(bt -> {
                            if (bt == ButtonType.OK) {
                                try {
                                    itemService.Delete(item.getIdItem());
                                    showSuccessAlert("Succès", "Item supprimé.");
                                    if (currentCategory != null) loadItemsForCategory(currentCategory); else loadItems();
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                    showErrorAlert("Erreur", "Impossible de supprimer l'item.");
                                }
                            }
                        });
                    });

                    setGraphic(container);
                }
            }

            private void openUpdate(Item item) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/Budget/ItemUpdate.fxml"));
                    Parent root = loader.load();
                    ItemUpdateController ctrl = loader.getController();
                    ctrl.loadItem(item);

                    Stage stage = (Stage) itemListView.getScene().getWindow();
                    stage.setScene(new Scene(root));
                    stage.setTitle("Modifier Item");
                    stage.show();
                } catch (IOException ex) {
                    ex.printStackTrace();
                    showErrorAlert("Erreur", "Impossible d'ouvrir le formulaire de modification.");
                }
            }
        });

        // Recherche en temps réel
        tfSearch.textProperty().addListener((obs, oldVal, newVal) -> filterItems(newVal));
    }

    // Charge tous les items depuis le service
    public void loadItems() {
        try {
            List<Item> itemList = itemService.ReadAll();
            items = FXCollections.observableArrayList(itemList);
            itemListView.setItems(items);
            // Reset category context when showing all items
            currentCategory = null;
            seuilAlertShown = false;
            updateStats(items);
        } catch (Exception e) {
            e.printStackTrace();
            items = FXCollections.observableArrayList();
            itemListView.setItems(items);
            currentCategory = null;
            seuilAlertShown = false;
            updateStats(items);
            showErrorAlert("Erreur de base de données", "Impossible de charger les items: " + e.getMessage());
        }
    }

    // Charge les items d'une catégorie spécifique
    public void loadItemsForCategory(org.example.Model.Budget.Categorie categorie) {
        try {
            List<Item> itemList = itemService.ReadByCategory(categorie.getIdCategorie());
            items = FXCollections.observableArrayList(itemList);
            itemListView.setItems(items);
            // Set current category first so updateStats can evaluate seuil
            currentCategory = categorie;
            seuilAlertShown = false; // reset alert state when switching categories
            updateStats(items);
        } catch (Exception e) {
            e.printStackTrace();
            items = FXCollections.observableArrayList();
            itemListView.setItems(items);
            currentCategory = categorie;
            seuilAlertShown = false;
            updateStats(items);
            showErrorAlert("Erreur de base de données", "Impossible de charger les items pour la catégorie: " + e.getMessage());
        }
    }

    // Met à jour le nombre d’items et le total
    private void updateStats(List<Item> itemList) {
        int totalItems = itemList.size();
        double totalMontant = itemList.stream().mapToDouble(Item::getMontant).sum();

        lblTotalItems.setText(String.valueOf(totalItems));
        lblMontantTotal.setText(String.format("%.2f DT", totalMontant));
        // Mirror values into the footer labels if they're present in the FXML
        if (lblSommeMontants != null) lblSommeMontants.setText(String.format("%.2f DT", totalMontant));
        if (lblSommeItems != null) lblSommeItems.setText(String.valueOf(totalItems));

        // Check against the category threshold (seuilAlerte) when a category is selected
        checkSeuil(totalMontant);
    }

    // Show a warning when the total montant for the current category reaches or exceeds its seuilAlerte
    private void checkSeuil(double totalMontant) {
        if (currentCategory == null) return;
        double seuil = currentCategory.getSeuilAlerte();
        if (seuil <= 0) return;

        if (totalMontant >= seuil && !seuilAlertShown) {
            String title = "Seuil d'alerte atteint";
            String message = String.format("La catégorie '%s' a atteint le seuil (%.2f DT). Montant actuel: %.2f DT.",
                    currentCategory.getNomCategorie(), seuil, totalMontant);
            showWarningAlert(title, message);
            // Persist an alert record if one doesn't already exist for this category+seuil
            try {
                java.util.List<Alerte> existing = alerteService.ReadByCategory(currentCategory.getIdCategorie());
                boolean duplicate = existing.stream().anyMatch(a -> a.isActive() && Double.compare(a.getSeuil(), seuil) == 0 && a.getMessage().equals(message));
                if (!duplicate) {
                    Alerte a = new Alerte(currentCategory.getIdCategorie(), message, seuil);
                    System.out.println(a);
                    alerteService.Add(a);
                    // notify other controllers (UI) about new alert so they can refresh
                    try { NotificationCenter.getInstance().postAlerte(a); } catch (Exception ignored) {}
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            seuilAlertShown = true;
        } else if (totalMontant < seuil) {
            // If total drops below threshold, allow alert to be shown again on next crossing
            seuilAlertShown = false;
        }
    }

    private void showWarningAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Filtrage par recherche
    private void filterItems(String query) {
        if (query == null || query.isEmpty()) {
            itemListView.setItems(items);
            updateStats(items);
            return;
        }

        List<Item> filtered = items.stream()
                .filter(item -> item.getLibelle().toLowerCase().contains(query.toLowerCase()) ||
                        item.getCategorie().getNomCategorie().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());

        itemListView.setItems(FXCollections.observableArrayList(filtered));
        updateStats(filtered);
    }

    @FXML
    private void resetSearch() {
        tfSearch.clear();
        itemListView.setItems(items);
        updateStats(items);
    }

    @FXML
    private void goToCreate() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Budget/ItemCreateGUI.fxml"));
            Parent root = loader.load();

            // Passer la référence de ce controller pour rafraîchir après création
            ItemCreateController createController = loader.getController();
            createController.setListController(this);
            if (currentCategory != null) {
                createController.setDefaultCategorie(currentCategory);
            }

            Stage stage = (Stage) itemListView.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Créer un nouvel Item");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Erreur", "Impossible d'ouvrir le formulaire de création.\n" + e.getMessage());
        }
    }

    @FXML
    private void goBackToMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Budget/CategorieListeGUI.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) itemListView.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Menu Principal");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Erreur", "Impossible de revenir au menu.");
        }
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}