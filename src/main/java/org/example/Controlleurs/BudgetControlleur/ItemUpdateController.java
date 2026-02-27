package org.example.Controlleurs.BudgetControlleur;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.Model.Budget.Item;
import org.example.Model.Budget.Categorie;
import org.example.Service.BudgetService.ItemService;
import org.example.Service.BudgetService.BudgetService;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ItemUpdateController implements Initializable {

    @FXML private TextField tfNomItem;
    @FXML private TextField tfPrix;
    @FXML private ComboBox<Categorie> cbCategorie;
    @FXML private Label lblCurrentLibelle;
    @FXML private Label lblCurrentMontant;
    @FXML private Label lblCurrentCategorie;

    private ItemService itemService;
    private BudgetService budgetService;

    private Item currentItem; // FIXED: should be Item, not Integer
    private String originalNom;
    private Categorie originalCategorie;
    private double originalPrix;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        itemService = new ItemService();
        budgetService = new BudgetService();
        loadCategories();
    }

    private void loadCategories() {
        List<Categorie> categories = budgetService.ReadAll();
        cbCategorie.setItems(FXCollections.observableArrayList(categories));
    }

    public void loadItem(Item item) {
        this.currentItem = item;

        originalNom = item.getLibelle();
        originalCategorie = item.getCategorie();
        originalPrix = item.getMontant();

        tfNomItem.setText(originalNom);
        tfPrix.setText(String.valueOf(originalPrix));
        cbCategorie.setValue(originalCategorie);

        lblCurrentLibelle.setText(originalNom);
        lblCurrentMontant.setText(originalPrix + " DT");
        lblCurrentCategorie.setText(originalCategorie != null ? originalCategorie.getNomCategorie() : "-");
    }

    @FXML
    private void enregistrer() {
        if (!validateInput()) return;

        currentItem.setLibelle(tfNomItem.getText().trim());
        currentItem.setMontant(Double.parseDouble(tfPrix.getText().trim()));
        currentItem.setCategorie(cbCategorie.getValue());

        try {
            itemService.Update(currentItem);
            showSuccessAlert("Succès", "Item modifié avec succès !");
            closeWindow();
        } catch (Exception e) {
            showErrorAlert("Erreur", "Impossible de modifier l'item : " + e.getMessage());
        }
    }

    @FXML
    private void reinitialiser() {
        tfNomItem.setText(originalNom);
        tfPrix.setText(String.valueOf(originalPrix));
        cbCategorie.setValue(originalCategorie);
        clearStyles();
    }

    private void clearStyles() {
        tfNomItem.getStyleClass().removeAll("error", "success");
        tfPrix.getStyleClass().removeAll("error", "success");
        cbCategorie.getStyleClass().removeAll("error", "success");
    }

    private boolean validateInput() {
        boolean isValid = true;
        StringBuilder errors = new StringBuilder();

        if (tfNomItem.getText().trim().isEmpty()) {
            errors.append("- Le nom de l'item est obligatoire.\n");
            tfNomItem.getStyleClass().add("error");
            isValid = false;
        } else {
            tfNomItem.getStyleClass().removeAll("error");
            tfNomItem.getStyleClass().add("success");
        }

        if (cbCategorie.getValue() == null) {
            errors.append("- Veuillez sélectionner une catégorie.\n");
            cbCategorie.getStyleClass().add("error");
            isValid = false;
        } else {
            cbCategorie.getStyleClass().removeAll("error");
            cbCategorie.getStyleClass().add("success");
        }

        String prixText = tfPrix.getText().trim();
        if (prixText.isEmpty()) {
            errors.append("- Le prix est obligatoire.\n");
            tfPrix.getStyleClass().add("error");
            isValid = false;
        } else {
            try {
                double prix = Double.parseDouble(prixText);
                if (prix <= 0) {
                    errors.append("- Le prix doit être supérieur à 0.\n");
                    tfPrix.getStyleClass().add("error");
                    isValid = false;
                } else {
                    tfPrix.getStyleClass().removeAll("error");
                    tfPrix.getStyleClass().add("success");
                }
            } catch (NumberFormatException e) {
                errors.append("- Le prix doit être un nombre valide.\n");
                tfPrix.getStyleClass().add("error");
                isValid = false;
            }
        }

        if (!isValid) showErrorAlert("Erreur de validation", errors.toString());
        return isValid;
    }


    @FXML
    private void closeWindow() {
        Stage stage = (Stage) tfNomItem.getScene().getWindow();
        // If this was opened as a dialog (has an owner) just close it
        if (stage.getOwner() != null) {
            stage.close();
            return;
        }

        // Otherwise navigate back to the Item list scene
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Budget/ItemListGUI.fxml"));
            Parent root = loader.load();

            ItemListController controller = loader.getController();
            if (currentItem != null && currentItem.getCategorie() != null) {
                controller.loadItemsForCategory(currentItem.getCategorie());
            } else {
                controller.loadItems();
            }

            stage.setScene(new Scene(root));
            stage.setTitle("Liste des Items");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Erreur", "Impossible de retourner à la liste des items.");
        }
    }


    @FXML
    private void DeleteItem() {
        try {
            itemService.Delete(currentItem.getIdItem()); // FIXED: use currentItem.getIdItem()
            showSuccessAlert("Succès", "Item supprimé !");
            closeWindow();
        } catch (Exception e) {
            showErrorAlert("Erreur", "Impossible de supprimer l'item : " + e.getMessage());
        }
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
}