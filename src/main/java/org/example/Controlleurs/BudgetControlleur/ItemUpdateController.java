package org.example.Controlleurs.BudgetControlleur;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.Model.Budget.Item;
import org.example.Service.BudgetService.ItemService;
import org.example.Service.BudgetService.BudgetService;
import javafx.scene.control.TextFormatter;
import java.util.function.UnaryOperator;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ItemUpdateController implements Initializable {

    @FXML private TextField tfNomItem;
    @FXML private TextField tfPrix;
    @FXML private Label lblCurrentLibelle;
    @FXML private Label lblCurrentMontant;

    private ItemService itemService;
    private BudgetService budgetService;

    private Item currentItem; // FIXED: should be Item, not Integer
    private String originalNom;
    private double originalPrix;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        itemService = new ItemService();
        budgetService = new BudgetService();
        // numeric filter for montant
        UnaryOperator<TextFormatter.Change> numberFilter = change -> {
            String newText = change.getControlNewText();
            if (newText.isEmpty()) return change;
            if (newText.matches("\\d*([.,]\\d{0,2})?")) return change;
            return null;
        };
        tfPrix.setTextFormatter(new TextFormatter<>(numberFilter));
    }


    public void loadItem(Item item) {
        this.currentItem = item;

        originalNom = item.getLibelle();
        originalPrix = item.getMontant();

        tfNomItem.setText(originalNom);
        tfPrix.setText(String.valueOf(originalPrix));

        lblCurrentLibelle.setText(originalNom);
        lblCurrentMontant.setText(originalPrix + " DT");
    }

    @FXML
    private void enregistrer() {
        if (!validateInput()) return;

        currentItem.setLibelle(tfNomItem.getText().trim());
        currentItem.setMontant(Double.parseDouble(tfPrix.getText().trim()));
        // category unchanged

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
        // Clear and reset prix field to work around TextFormatter
        tfPrix.clear();
        tfPrix.setText(String.format("%.2f", originalPrix));
        clearStyles();
    }

    private void clearStyles() {
        tfNomItem.getStyleClass().removeAll("error", "success");
        tfPrix.getStyleClass().removeAll("error", "success");
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

        // category selection removed from update form

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