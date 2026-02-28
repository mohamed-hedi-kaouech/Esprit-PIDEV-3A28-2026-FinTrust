package org.example.Controlleurs.BudgetControlleur;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.ToggleGroup;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.function.UnaryOperator;
import org.example.Model.Budget.Categorie;
import org.example.Model.Budget.Item;
import org.example.Service.BudgetService.ItemService;

public class ItemCreateController {

    @FXML private TextField tfLibelle;
    @FXML private TextField tfMontant;
    @FXML private DatePicker dpDate;

    // mode toggles
    @FXML private ToggleGroup modeGroup;
    @FXML private RadioButton rbManual;
    @FXML private RadioButton rbInvoice;

    // container for OCR fields
    @FXML private javafx.scene.layout.VBox ocrContainer;

    // fields for OCR file selection
    @FXML private TextField tfFilePath;
    @FXML private javafx.scene.control.TextArea taOcrResult;
    private File selectedFile;

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
    }

    @FXML
    public void initialize() {
        // Add number formatter to montant field (only digits and decimal separators)
        UnaryOperator<TextFormatter.Change> numberFilter = change -> {
            String newText = change.getControlNewText();
            if (newText.isEmpty()) return change; // allow clearing
            // allow only digits and optional single dot/comma with up to 2 decimals
            if (newText.matches("\\d*([.,]\\d{0,2})?")) {
                return change;
            }
            return null;
        };
        tfMontant.setTextFormatter(new TextFormatter<>(numberFilter));

        // mode toggle listener
        modeGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            boolean invoiceMode = rbInvoice.isSelected();
            ocrContainer.setVisible(invoiceMode);
            ocrContainer.setManaged(invoiceMode);
            tfFilePath.setDisable(!invoiceMode);
            taOcrResult.setDisable(!invoiceMode);
            // clear file selection if switching back to manual
            if (!invoiceMode) {
                selectedFile = null;
                tfFilePath.clear();
                taOcrResult.clear();
            }
        });

        // ensure initial state
        rbManual.setSelected(true);
        tfFilePath.setDisable(true);
        taOcrResult.setDisable(true);
    }

    @FXML
    private void enregistrer() {
        String libelle = tfLibelle.getText().trim();
        Categorie categorie = defaultCategorie;
        double montant;

        // ensure mode consistency
        if (rbInvoice.isSelected() && selectedFile == null) {
            showErrorAlert("Erreur", "Veuillez sélectionner une facture pour le mode Facture.");
            return;
        }

        if (libelle.isEmpty() || categorie == null || tfMontant.getText().trim().isEmpty()) {
            showErrorAlert("Erreur", "Tous les champs doivent être remplis !");
            return;
        }

        try {
            String montantStr = tfMontant.getText().trim();
            // Normalize comma to dot for decimal separator (handles OCR extraction)
            montantStr = montantStr.replace(",", ".");
            montant = Double.parseDouble(montantStr);
        } catch (NumberFormatException e) {
            showErrorAlert("Erreur", "Montant invalide !");
            return;
        }

        Item item = new Item();
        item.setLibelle(libelle);
        item.setCategorie(categorie);
        item.setMontant(montant);

        itemService.Add(item);

        // if in manual mode, generate invoice for the item
        if (rbManual.isSelected()) {
            try {
                java.nio.file.Path invoicePath = org.example.Service.BudgetService.InvoiceGeneratorService.generateItemInvoice(item);
                showSuccessAlert("Succès", "Item créé avec succès !\n\nFacture générée: " + invoicePath.getFileName());
            } catch (Exception ex) {
                ex.printStackTrace();
                showSuccessAlert("Succès", "Item créé avec succès !\n(Génération facture échouée: " + ex.getMessage() + ")");
            }
        } else if (selectedFile != null) {
            // if in invoice mode and file was selected, process OCR
            String ocrText = org.example.Utils.OCRService.extractText(selectedFile);
            try {
                java.nio.file.Path out = org.example.Service.BudgetService.OCRInvoiceFormatterService.generateFormattedOCRInvoice(item, ocrText);
                showSuccessAlert("Succès", "Item créé et OCR effectué !\n\nFacture OCR générée: " + out.toAbsolutePath());
            } catch (Exception ex) {
                ex.printStackTrace();
                showErrorAlert("OCR échoué", "Item créé mais impossible d'enregistrer la facture OCR: " + ex.getMessage());
            }
        } else {
            showSuccessAlert("Succès", "Item créé avec succès !");
        }

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
        tfFilePath.clear();
        taOcrResult.clear();
        dpDate.setValue(null);
        selectedFile = null;
        rbManual.setSelected(true); // reset to manual
        tfFilePath.setDisable(true);
        taOcrResult.setDisable(true);
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

    @FXML
    private void chooseFile() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Sélectionner fichier pour OCR");
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images or PDF or Text", "*.png", "*.jpg", "*.jpeg", "*.bmp", "*.pdf", "*.txt"),
                new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
        );
        File f = fc.showOpenDialog(tfLibelle.getScene().getWindow());
        if (f != null) {
            rbInvoice.setSelected(true); // switch to invoice mode when choosing file
            selectedFile = f;
            tfFilePath.setText(f.getAbsolutePath());
            String text = org.example.Utils.OCRService.extractText(f);
            taOcrResult.setText(text == null ? "" : text);
            // Parse OCR text and auto-fill form fields
            if (text != null && !text.isEmpty()) {
                org.example.Utils.InvoiceOCRParser.InvoiceData data =
                        org.example.Utils.InvoiceOCRParser.parseInvoiceText(text);

                if (data.itemName != null && !data.itemName.isEmpty()) {
                    tfLibelle.setText(data.itemName);
                }

                if (data.amount != null && data.amount > 0) {
                    tfMontant.setText(String.format("%.2f", data.amount));
                }

                if (data.date != null) {
                    dpDate.setValue(data.date);
                }

                // Display a message about what was extracted
                StringBuilder extractedInfo = new StringBuilder();
                extractedInfo.append("--- Données extraites ---\n");
                if (data.itemName != null) {
                    extractedInfo.append("Nom: ").append(data.itemName).append("\n");
                }
                if (data.amount != null) {
                    extractedInfo.append("Montant: ").append(String.format("%.2f", data.amount)).append(" DT\n");
                }
                if (data.date != null) {
                    extractedInfo.append("Date: ").append(data.date).append("\n");
                }
                extractedInfo.append("\n--- Texte complet ---\n");
                extractedInfo.append(text);

                taOcrResult.setText(extractedInfo.toString());
            }        }
    }
}