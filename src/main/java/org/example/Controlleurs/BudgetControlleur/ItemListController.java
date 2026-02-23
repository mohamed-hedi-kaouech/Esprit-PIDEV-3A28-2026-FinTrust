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

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ItemListController implements Initializable {

    @FXML private ListView<Item> itemListView;
    @FXML private Label lblTotalItems;
    @FXML private Label lblMontantTotal;
    @FXML private TextField tfSearch;

    private ItemService itemService;
    private ObservableList<Item> items;
    private Categorie currentCategory;

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
            private final HBox topRow = new HBox(10);
            private final Label lblId = new Label();
            private final Label lblLibelle = new Label();
            private final Region spacer = new Region();
            private final Label lblMontant = new Label();
            private final Label lblCategorie = new Label();
            private final HBox footer = new HBox(8);
            private final Button btnUpdate = new Button("✏️ Modifier");
            private final Button btnDelete = new Button("🗑️ Supprimer");

            {
                // Top row: id badge, name, spacer, montant
                lblId.getStyleClass().add("categorie-id");
                lblLibelle.getStyleClass().add("categorie-nom");
                lblMontant.getStyleClass().add("categorie-budget");

                // Column sizing to align like a table
                lblId.setMinWidth(60);
                lblMontant.setMinWidth(120);
                lblCategorie.setMinWidth(160);
                HBox.setHgrow(lblLibelle, Priority.ALWAYS);

                topRow.getChildren().addAll(lblId, lblLibelle, spacer, lblMontant);
                HBox.setHgrow(spacer, Priority.ALWAYS);

                // Middle row: category name
                lblCategorie.getStyleClass().add("categorie-seuil");

                // Footer: action buttons
                footer.getChildren().addAll(new Region(), btnUpdate, btnDelete);

                container.getChildren().addAll(topRow, lblCategorie, footer);

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
                    lblId.setText("#" + item.getIdItem());
                    lblLibelle.setText(item.getLibelle());
                    lblMontant.setText(String.format("%.2f DT", item.getMontant()));
                    lblCategorie.setText(item.getCategorie() != null ? "Catégorie: " + item.getCategorie().getNomCategorie() : "Catégorie: -" );

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
        List<Item> itemList = itemService.ReadAll();
        items = FXCollections.observableArrayList(itemList);
        itemListView.setItems(items);
        updateStats(items);
        currentCategory = null;
    }

    // Charge les items d'une catégorie spécifique
    public void loadItemsForCategory(org.example.Model.Budget.Categorie categorie) {
        List<Item> itemList = itemService.ReadByCategory(categorie.getIdCategorie());
        items = FXCollections.observableArrayList(itemList);
        itemListView.setItems(items);
        updateStats(items);
        currentCategory = categorie;
    }

    // Met à jour le nombre d’items et le total
    private void updateStats(List<Item> itemList) {
        int totalItems = itemList.size();
        double totalMontant = itemList.stream().mapToDouble(Item::getMontant).sum();

        lblTotalItems.setText(String.valueOf(totalItems));
        lblMontantTotal.setText(String.format("%.2f DT", totalMontant));
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
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Erreur", "Impossible d'ouvrir le formulaire de création.");
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