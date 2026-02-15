package org.example.Controlleurs.ProductControlleur;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.example.Model.Product.ClassProduct.Product;
import org.example.Service.ProductService.ProductService;
import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;


public class ListeProductControlleur implements Initializable {


    // TableView
    @FXML private ListView<Product> productListView;


    @FXML private TextField searchField;
    @FXML private Label totalProductsLabel;

    private ObservableList<Product> productList = FXCollections.observableArrayList();
    private ObservableList<Product> filteredList = FXCollections.observableArrayList();
    private ProductService PS;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        PS = new ProductService();

        setupListView();
        loadProductData();
        setupSearchListener();
    }

    @FXML
    private void goToCreatePage(ActionEvent event) {

        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/Product/CreateProductGUI.fxml")
            );

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Créer Produit");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goBackToMenu(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/MenuGUI.fxml")
            );
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Manager");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupListView() {
        // Set custom cell factory for ListView
        productListView.setCellFactory(listView -> new ProductListCell());

        // Set placeholder when list is empty
        Label placeholder = new Label("Aucun produit disponible");
        placeholder.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 14px; -fx-font-style: italic;");
        productListView.setPlaceholder(placeholder);
    }

    private void loadProductData() {
        productList.clear();
        productList.addAll(PS.ReadAll());
        filteredList.setAll(productList);
        productListView.setItems(filteredList);
        updateTotalLabel();
    }


    private void setupSearchListener() {
        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filterProducts(newValue);
            });
        }
    }

    private void filterProducts(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            filteredList.setAll(productList);
        } else {
            filteredList.clear();
            String lowerCaseFilter = searchText.toLowerCase();

            for (Product product : productList) {
                if (product.getCategory().name().toLowerCase().contains(lowerCaseFilter) ||
                        product.getDescription().toLowerCase().contains(lowerCaseFilter) ||
                        String.valueOf(product.getProductId()).contains(lowerCaseFilter)) {
                    filteredList.add(product);
                }
            }
        }
        updateTotalLabel();
    }

    private void updateTotalLabel() {
        if (totalProductsLabel != null) {
            int total = filteredList.size();
            totalProductsLabel.setText(String.format("Total: %d produit%s", total, total > 1 ? "s" : ""));
        }
    }

    @FXML
    private void handleSearch() {
        String searchText = searchField.getText();
        filterProducts(searchText);
    }

    @FXML
    private void handleReset() {
        searchField.clear();
        filteredList.setAll(productList);
        updateTotalLabel();
    }


    // Dans votre ListView, lors du clic sur "Modifier"
    private void handleUpdate(Product product) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/Product/ProductUpdateGUI.fxml")
            );
            Parent root = loader.load();

            ProductUpdateControlleur controller = loader.getController();
            controller.loadProduct(product); // Charge les données

            Stage stage = (Stage) productListView.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleDelete(Product product) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation de suppression");
        confirmAlert.setHeaderText("Supprimer le produit ");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer ce produit?\n\n" +
                "Catégorie: " + formatCategoryName(product.getCategory().name()) + "\n" +
                "Prix: " + String.format("%.2f DT", product.getPrice()) + "\n\n" +
                "Cette action est irréversible!");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (PS.delete(product.getProductId())) {
                showSuccessAlert("Succès", "Le produit a été supprimé avec succès!");
                loadProductData();
            } else {
                showErrorAlert("Erreur", "Erreur lors de la suppression du produit.");
            }
        }
    }

    private String formatCategoryName(String category) {
        if (category == null) return "";
        return category.replace("_", " ");
    }

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
    private class ProductListCell extends ListCell<Product> {
        private final VBox container;
        private final HBox headerBox;
        private final HBox bodyBox;
        private final HBox footerBox;

        private final Label idLabel;
        private final Label categoryLabel;
        private final Label priceLabel;
        private final Label descriptionLabel;
        private final Label dateLabel;
        private final Button updateButton;
        private final Button deleteButton;

        public ProductListCell() {
            super();

            // Container
            container = new VBox(10);
            container.setPadding(new Insets(15));
            container.getStyleClass().add("product-card");

            // Header with ID, Category, and Price
            headerBox = new HBox(15);
            headerBox.setAlignment(Pos.CENTER_LEFT);

            idLabel = new Label();
            idLabel.getStyleClass().add("product-id");
            idLabel.setFont(Font.font("System Bold", 14));

            categoryLabel = new Label();
            categoryLabel.getStyleClass().add("product-category-badge");

            Region spacer1 = new Region();
            HBox.setHgrow(spacer1, Priority.ALWAYS);

            priceLabel = new Label();
            priceLabel.getStyleClass().add("product-price");
            priceLabel.setFont(Font.font("System Bold", 16));

            headerBox.getChildren().addAll(idLabel, categoryLabel, spacer1, priceLabel);

            // Body with Description
            bodyBox = new HBox();
            bodyBox.setAlignment(Pos.CENTER_LEFT);

            descriptionLabel = new Label();
            descriptionLabel.getStyleClass().add("product-description");
            descriptionLabel.setWrapText(true);
            descriptionLabel.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(descriptionLabel, Priority.ALWAYS);

            bodyBox.getChildren().add(descriptionLabel);

            // Footer with Date and Action Buttons
            footerBox = new HBox(10);
            footerBox.setAlignment(Pos.CENTER_LEFT);

            dateLabel = new Label();
            dateLabel.getStyleClass().add("product-date");
            dateLabel.setFont(Font.font("System", 11));

            Region spacer2 = new Region();
            HBox.setHgrow(spacer2, Priority.ALWAYS);

            updateButton = new Button("Modifier");
            updateButton.getStyleClass().add("btn-update");

            deleteButton = new Button("Supprimer");
            deleteButton.getStyleClass().add("btn-delete");

            footerBox.getChildren().addAll(dateLabel, spacer2, updateButton, deleteButton);

            // Add all sections to container
            container.getChildren().addAll(headerBox, new Separator(), bodyBox, footerBox);
        }

        @Override
        protected void updateItem(Product product, boolean empty) {
            super.updateItem(product, empty);

            if (empty || product == null) {
                setGraphic(null);
            } else {
                // Set ID
//                idLabel.setText("#" + product.getProductId());

                // Set Category with styling
                String category = formatCategoryName(product.getCategory().name());
                categoryLabel.setText(category);
                categoryLabel.setStyle(getCategoryStyle(product.getCategory().name()));

                // Set Price
                priceLabel.setText(String.format("%.2f DT", product.getPrice()));

                // Set Description
                descriptionLabel.setText(product.getDescription());

                // Set Date
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                dateLabel.setText("📅 Créé le: " + product.getCreatedAt().format(formatter));

                // Set button actions
                updateButton.setOnAction(e -> handleUpdate(product));
                deleteButton.setOnAction(e -> handleDelete(product));

                setGraphic(container);
            }
        }

        private String getCategoryStyle(String category) {
            if (category == null) return "";

            if (category.startsWith("COMPTE")) {
                return "-fx-background-color: #e3f2fd; -fx-text-fill: #1976D2; " +
                        "-fx-background-radius: 12px; -fx-padding: 4px 12px; -fx-font-weight: bold; -fx-font-size: 11px;";
            } else if (category.startsWith("CARTE")) {
                return "-fx-background-color: #f3e5f5; -fx-text-fill: #7B1FA2; " +
                        "-fx-background-radius: 12px; -fx-padding: 4px 12px; -fx-font-weight: bold; -fx-font-size: 11px;";
            } else if (category.startsWith("EPARGNE") || category.contains("DEPOT") || category.contains("PLACEMENT")) {
                return "-fx-background-color: #e8f5e9; -fx-text-fill: #2e7d32; " +
                        "-fx-background-radius: 12px; -fx-padding: 4px 12px; -fx-font-weight: bold; -fx-font-size: 11px;";
            } else if (category.startsWith("ASSURANCE")) {
                return "-fx-background-color: #fff3e0; -fx-text-fill: #e65100; " +
                        "-fx-background-radius: 12px; -fx-padding: 4px 12px; -fx-font-weight: bold; -fx-font-size: 11px;";
            }

            return "-fx-background-color: #f5f5f5; -fx-text-fill: #666666; " +
                    "-fx-background-radius: 12px; -fx-padding: 4px 12px; -fx-font-weight: bold; -fx-font-size: 11px;";
        }
    }
}
