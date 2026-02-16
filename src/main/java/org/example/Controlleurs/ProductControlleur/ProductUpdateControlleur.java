package org.example.Controlleurs.ProductControlleur;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.Model.Product.ClassProduct.Product;
import org.example.Model.Product.EnumProduct.ProductCategory;
import org.example.Service.ProductService.ProductService;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;
public class ProductUpdateControlleur implements Initializable {

    // Hidden field for product ID
    @FXML private TextField productIdField;

    // Display fields
    @FXML private Label productIdDisplay;
    @FXML private Label currentCategoryLabel;
    @FXML private Label currentPriceLabel;
    @FXML private Label currentDateLabel;

    // Form Fields
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private TextField priceField;
    @FXML private TextArea descriptionArea;
    @FXML private DatePicker createdAtPicker;
    @FXML private Label charCountLabel;

    // Buttons
    @FXML private Button updateButton;
    @FXML private Button cancelButton;
    @FXML private Button deleteButton;

    private Product currentProduct;
    private ProductService ProductService;

    @FXML
    private VBox rootPane;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ProductService = new ProductService();

        // Setup character counter for description
        setupDescriptionCounter();

        // Setup price field to accept only numbers
        setupPriceFieldValidation();
    }

    //Load product data into the form
    //This method should be called after navigating to this page
    public void loadProduct(Product product) {
        if (product == null) {
            showErrorAlert("Erreur", "Aucun produit à modifier.");
            return;
        }

        this.currentProduct = product;

        // Set hidden product ID
        productIdField.setText(String.valueOf(product.getProductId()));


        // Display current information
        currentCategoryLabel.setText(formatCategoryName(product.getCategory().name()));
        currentPriceLabel.setText(String.format("%.2f DT", product.getPrice()));
        currentDateLabel.setText(product.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        // Populate form fields with current values
        categoryComboBox.setValue(product.getCategory().name());
        priceField.setText(String.valueOf(product.getPrice()));
        descriptionArea.setText(product.getDescription());
        createdAtPicker.setValue(LocalDate.from(product.getCreatedAt()));

        // Update character count
        int length = product.getDescription().length();
        charCountLabel.setText(length + "/500 caractères");
    }

    /**
     * Load product by ID
     */
//    public void loadProductById(int productId) {
//        Product product = PS.getProductById(productId);
//        if (product != null) {
//            loadProduct(product);
//        } else {
//            showErrorAlert("Erreur", "Produit introuvable avec l'ID: " + productId);
//        }
//    }

    @FXML
    private void goBackToList() {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/Product/ListeProductGUI.fxml")
            );

            Stage stage = (Stage) Stage.getWindows()
                    .filtered(window -> window.isShowing())
                    .get(0);

            stage.setScene(new Scene(root));
            stage.setTitle("Liste Produits");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void setupDescriptionCounter() {
        descriptionArea.textProperty().addListener((observable, oldValue, newValue) -> {
            int length = newValue.length();
            charCountLabel.setText(length + "/500 caractères");

            if (length > 500) {
                descriptionArea.setText(oldValue);
            }
        });
    }

    private void setupPriceFieldValidation() {
        priceField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d*")) {
                priceField.setText(oldValue);
            }
        });
    }

    @FXML
    private void handleUpdate() {
        if (!validateInput()) {
            return;
        }

        // Show confirmation dialog
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText("Confirmer les modifications");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir enregistrer ces modifications?\n\n" +
                "Produit #" + currentProduct.getProductId() + "\n" +
                "Nouvelle catégorie: " + formatCategoryName(categoryComboBox.getValue()) + "\n" +
                "Nouveau prix: " + priceField.getText() + " DT");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Create updated product object
                Product updatedProduct = new Product();
                updatedProduct.setProductId(Integer.parseInt(productIdField.getText()));
                updatedProduct.setCategory(ProductCategory.valueOf(categoryComboBox.getValue()));
                updatedProduct.setPrice(Double.parseDouble(priceField.getText()));
                updatedProduct.setDescription(descriptionArea.getText());
                updatedProduct.setCreatedAt(createdAtPicker.getValue().atStartOfDay());

                // Update in database
                if (ProductService.update(updatedProduct)) {
                    showSuccessAlert("Succès", "Le produit a été modifié avec succès!");

                    // Update current product reference
                    currentProduct = updatedProduct;

                    // Refresh current info display
                    loadProduct(updatedProduct);

                    // Optionally navigate back to list
                     goBackToList();
                } else {
                    showErrorAlert("Erreur", "Erreur lors de la modification du produit.");
                }
            } catch (NumberFormatException e) {
                showErrorAlert("Erreur de saisie", "Le prix doit être un nombre valide.");
            }
        }
    }

    @FXML
    private void handleCancel() {
        // Show confirmation dialog
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Annuler les modifications");
        confirmAlert.setHeaderText("Voulez-vous annuler les modifications?");
        confirmAlert.setContentText("Les modifications non enregistrées seront perdues.");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Reload original product data
            if (currentProduct != null) {
                loadProduct(currentProduct);
            }
        }
    }


    private boolean validateInput() {
        StringBuilder errors = new StringBuilder();

        if (categoryComboBox.getValue() == null || categoryComboBox.getValue().isEmpty()) {
            errors.append("- Veuillez sélectionner une catégorie.\n");
            categoryComboBox.getStyleClass().add("error");
        } else {
            categoryComboBox.getStyleClass().remove("error");
        }

        if (priceField.getText().isEmpty()) {
            errors.append("- Le prix est obligatoire.\n");
            priceField.getStyleClass().add("error");
        } else {
            try {
                double price = Double.parseDouble(priceField.getText());
                if (price < 0) {
                    errors.append("- Le prix doit être positif.\n");
                    priceField.getStyleClass().add("error");
                } else {
                    priceField.getStyleClass().remove("error");
                }
            } catch (NumberFormatException e) {
                errors.append("- Le prix doit être un nombre valide.\n");
                priceField.getStyleClass().add("error");
            }
        }

        if (descriptionArea.getText().isEmpty()) {
            errors.append("- La description est obligatoire.\n");
            descriptionArea.getStyleClass().add("error");
        } else if (descriptionArea.getText().length() > 500) {
            errors.append("- La description ne doit pas dépasser 500 caractères.\n");
            descriptionArea.getStyleClass().add("error");
        } else {
            descriptionArea.getStyleClass().remove("error");
        }

        if (errors.length() > 0) {
            showErrorAlert("Erreur de validation", errors.toString());
            return false;
        }

        return true;
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

    // Getters for accessing from other controllers
    public Product getCurrentProduct() {
        return currentProduct;
    }
}
