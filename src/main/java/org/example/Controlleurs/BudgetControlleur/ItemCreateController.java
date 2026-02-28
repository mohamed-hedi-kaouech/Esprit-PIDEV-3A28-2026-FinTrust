package org.example.Controlleurs.BudgetControlleur;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;
import org.example.Model.Budget.Categorie;
import org.example.Model.Budget.Item;
import org.example.Service.BudgetService.BudgetService;
import org.example.Service.BudgetService.ItemService;

import java.util.List;

public class ItemCreateController {

    @FXML private TextField tfLibelle;
    @FXML private TextField tfMontant;
    @FXML private ComboBox<Categorie> cbCategorie;

    private final ItemService itemService = new ItemService();

    // Référence vers le controller de la liste pour rafraîchir après ajout
    private ItemListController listController;

    // If set, the create form will default to this category and disable selection
    private Categorie defaultCategorie;

    public void setListController(ItemListController controller) {
        this.listController = controller;
    }

    public void setDefaultCategorie(Categorie categorie) {
        this.defaultCategorie = categorie;
        if (cbCategorie != null && categorie != null) {
            cbCategorie.getSelectionModel().select(categorie);
            cbCategorie.setDisable(true);
        }
    }

    @FXML
    public void initialize() {
        // Populate categories in combobox
        List<Categorie> categories = new BudgetService().ReadAll();
        cbCategorie.setItems(FXCollections.observableArrayList(categories));
        if (defaultCategorie != null) {
            cbCategorie.getSelectionModel().select(defaultCategorie);
            cbCategorie.setDisable(true);
        }
    }

    @FXML
    private void enregistrer() {
        String libelle = tfLibelle.getText().trim();
        Categorie categorie = (defaultCategorie != null) ? defaultCategorie : cbCategorie.getSelectionModel().getSelectedItem();
        double montant;

        if (libelle.isEmpty() || categorie == null || tfMontant.getText().trim().isEmpty()) {
            showErrorAlert("Erreur", "Tous les champs doivent être remplis !");
            return;
        }

        try {
            montant = Double.parseDouble(tfMontant.getText().trim());
        } catch (NumberFormatException e) {
            showErrorAlert("Erreur", "Montant invalide !");
            return;
        }

        Item item = new Item();
        item.setLibelle(libelle);
        item.setCategorie(categorie);
        item.setMontant(montant);

        itemService.Add(item);

        showSuccessAlert("Succès", "Item créé avec succès !");
        reinitialiser();

        if (listController != null) {
            if (defaultCategorie != null) {
                listController.loadItemsForCategory(defaultCategorie);
            } else {
                listController.loadItems();
            }
        }
    }

    @FXML
    private void reinitialiser() {
        tfLibelle.clear();
        tfMontant.clear();
        cbCategorie.getSelectionModel().clearSelection();
    }

    // Méthode pour bouton retour
    @FXML
    private void goBackToListe() {
        Stage stage = (Stage) tfLibelle.getScene().getWindow();
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
            if (defaultCategorie != null) {
                controller.loadItemsForCategory(defaultCategorie);
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