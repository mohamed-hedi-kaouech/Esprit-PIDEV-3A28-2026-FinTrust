package org.example.Controlleurs.ProductControlleur;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.Model.Product.ClassProduct.Product;
import org.example.Model.Product.EnumProduct.ProductCategory;
import org.example.Service.ProductService.ProductService;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ProductCreationControlleur implements Initializable {

    // Form Fields
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private TextField priceField;
    @FXML private TextArea descriptionArea;
    @FXML private Label charCountLabel;




    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupPriceFieldValidation();
        setupDescriptionCounter();
    }


    @FXML
    private void goBackToList(ActionEvent event) {

        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/Product/ListeProductGUI.fxml")
            );

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
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
    public void CreateProduct(ActionEvent actionEvent) {
        if (validateInput()) {
            try {
                ProductService ps=new ProductService();
                Product product = new Product();
                product.setCategory(ProductCategory.valueOf(categoryComboBox.getValue()));
                product.setPrice(Double.parseDouble(priceField.getText()));
                product.setDescription(descriptionArea.getText());

                if (ps.add(product)){
                    showAlert(Alert.AlertType.INFORMATION, "Succès",
                            "Le produit a été ajouté avec succès!");
                    handleClear();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur",
                            "Erreur lors de l'ajout du produit.");
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur de saisie",
                        "Le prix doit être un nombre valide.");
            }
        }

    }

    private boolean validateInput() {
        StringBuilder errors = new StringBuilder();
        if (categoryComboBox.getValue() == null || categoryComboBox.getValue().isEmpty()) {
            errors.append("- Veuillez sélectionner une catégorie.\n");
        }
        if (priceField.getText().isEmpty()) {
            errors.append("- Le prix est obligatoire.\n");
        } else {
            try {
                double price = Double.parseDouble(priceField.getText());
                if (price < 0) {
                    errors.append("- Le prix doit être positif.\n");
                }
            } catch (NumberFormatException e) {
                errors.append("- Le prix doit être un nombre valide.\n");
            }
        }
        if (descriptionArea.getText().isEmpty()) {
            errors.append("- La description est obligatoire.\n");
        } else if (descriptionArea.getText().length() > 500) {
            errors.append("- La description ne doit pas dépasser 500 caractères.\n");
        }
        if (errors.length() > 0) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation", errors.toString());
            return false;
        }
        return true;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    public void handleClear(ActionEvent actionEvent) {
        categoryComboBox.setValue(null);
        priceField.clear();
        descriptionArea.clear();
    }
    public void handleClear() {
        categoryComboBox.setValue(null);
        priceField.clear();
        descriptionArea.clear();
    }

}
