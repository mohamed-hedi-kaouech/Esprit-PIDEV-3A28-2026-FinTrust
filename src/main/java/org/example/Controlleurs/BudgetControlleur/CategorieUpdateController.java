package org.example.Controlleurs.BudgetControlleur;


import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;

import javafx.stage.Stage;
import org.example.Model.Budget.Categorie;
import org.example.Service.BudgetService.BudgetService;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class CategorieUpdateController implements Initializable {

    // Current Info Labels
    @FXML private Label lblCategorieId;
    @FXML private Label lblCurrentNom;
    @FXML private Label lblCurrentBudget;
    @FXML private Label lblCurrentSeuil;
    @FXML private Label lblCurrentStatut;
    @FXML private Label lblLastModified;

    // Form Fields
    @FXML private TextField tfIdCategorie;
    @FXML private TextField tfNomCategorie;
    @FXML private TextField tfBudgetPrevu;
    @FXML private TextField tfSeuilAlerte;

    // Preview Labels
    @FXML private Label lblPreviewPercentage;
    @FXML private Label lblPreviewStatut;

    private BudgetService BS;
    private Categorie currentCategorie;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        BS = new BudgetService();

        setupNumericValidation();
        setupPreviewListeners();
    }

    /**
     * Load category data into the form
     */
    public void loadCategorie(Categorie categorie) {
        if (categorie == null) {
            showErrorAlert("Erreur", "Aucune catégorie à modifier.");
            return;
        }

        this.currentCategorie = categorie;

        // Set ID badge
        lblCategorieId.setText("#" + categorie.getIdCategorie());

        // Set current values display
        lblCurrentNom.setText(categorie.getNomCategorie());
        lblCurrentBudget.setText(String.format("%.2f DT", categorie.getBudgetPrevu()));
        lblCurrentSeuil.setText(String.format("%.2f DT", categorie.getSeuilAlerte()));

//        String statut = categorie.getStatut() != null ? categorie.getStatut() :
//                calculateStatut(categorie.getBudgetPrevu(), categorie.getSeuilAlerte());
//        lblCurrentStatut.setText(statut);
//
//        // Style current statut
//        if (statut.contains("OK") || statut.contains("🟢")) {
//            lblCurrentStatut.setStyle("-fx-text-fill: #22c55e; -fx-font-weight: bold;");
//        } else if (statut.contains("Attention") || statut.contains("🟡")) {
//            lblCurrentStatut.setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
//        } else {
//            lblCurrentStatut.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
//        }

        // Populate form fields
        tfIdCategorie.setText(String.valueOf(categorie.getIdCategorie()));
        tfNomCategorie.setText(categorie.getNomCategorie());
        tfBudgetPrevu.setText(String.valueOf(categorie.getBudgetPrevu()));
        tfSeuilAlerte.setText(String.valueOf(categorie.getSeuilAlerte()));

        // Update preview
        updatePreview();
    }

    private void setupNumericValidation() {
        // Budget field
        tfBudgetPrevu.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d*")) {
                tfBudgetPrevu.setText(oldValue);
            }
        });

        // Seuil field
        tfSeuilAlerte.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d*")) {
                tfSeuilAlerte.setText(oldValue);
            }
        });
    }

    private void setupPreviewListeners() {
        tfBudgetPrevu.textProperty().addListener((obs, old, newVal) -> updatePreview());
        tfSeuilAlerte.textProperty().addListener((obs, old, newVal) -> updatePreview());
    }

    private void updatePreview() {
        try {
            String budgetText = tfBudgetPrevu.getText().trim();
            String seuilText = tfSeuilAlerte.getText().trim();

            if (budgetText.isEmpty() || seuilText.isEmpty()) {
                lblPreviewPercentage.setText("0%");
                lblPreviewStatut.setText("-");
                lblPreviewStatut.setStyle("-fx-text-fill: #64748b;");
                return;
            }

            double budget = Double.parseDouble(budgetText);
            double seuil = Double.parseDouble(seuilText);

            if (budget == 0) {
                lblPreviewPercentage.setText("N/A");
                lblPreviewStatut.setText("Budget invalide");
                lblPreviewStatut.setStyle("-fx-text-fill: #ef4444;");
                return;
            }

            double percentage = (seuil / budget) * 100;
            lblPreviewPercentage.setText(String.format("%.1f%%", percentage));

            // Set statut and color
            if (percentage < 30) {
                lblPreviewStatut.setText("🟢 OK");
                lblPreviewStatut.setStyle("-fx-text-fill: #22c55e;");
                lblPreviewPercentage.setStyle("-fx-text-fill: #22c55e;");
            } else if (percentage < 70) {
                lblPreviewStatut.setText("🟡 Attention");
                lblPreviewStatut.setStyle("-fx-text-fill: #f59e0b;");
                lblPreviewPercentage.setStyle("-fx-text-fill: #f59e0b;");
            } else {
                lblPreviewStatut.setText("🔴 Critique");
                lblPreviewStatut.setStyle("-fx-text-fill: #ef4444;");
                lblPreviewPercentage.setStyle("-fx-text-fill: #ef4444;");
            }

        } catch (NumberFormatException e) {
            lblPreviewPercentage.setText("0%");
            lblPreviewStatut.setText("-");
        }
    }

    @FXML
    private void updateCategorie() {
        if (!validateInput()) {
            return;
        }

        // Show confirmation
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText("Confirmer les modifications");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir enregistrer ces modifications?\n\n" +
                "Catégorie: " + tfNomCategorie.getText() + "\n" +
                "Nouveau budget: " + tfBudgetPrevu.getText() + " DT\n" +
                "Nouveau seuil: " + tfSeuilAlerte.getText() + " DT");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                int id = Integer.parseInt(tfIdCategorie.getText());
                String nom = tfNomCategorie.getText().trim();
                double budget = Double.parseDouble(tfBudgetPrevu.getText().trim());
                double seuil = Double.parseDouble(tfSeuilAlerte.getText().trim());

                Categorie categorie = new Categorie(id, nom, budget, seuil);

                if (BS.update(categorie)) {
                    showSuccessAlert("Succès",
                            "La catégorie a été modifiée avec succès!");
                    goBackToListe();
                } else {
                    showErrorAlert("Erreur",
                            "Erreur lors de la modification de la catégorie.");
                }

            } catch (NumberFormatException e) {
                showErrorAlert("Erreur de saisie",
                        "Veuillez entrer des nombres valides.");
            }
        }
    }

    @FXML
    private void deleteCategorie() {
        if (currentCategorie == null) {
            showErrorAlert("Erreur", "Aucune catégorie sélectionnée.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.WARNING);
        confirmAlert.setTitle("⚠️ Supprimer la catégorie");
        confirmAlert.setHeaderText("ATTENTION: Suppression définitive!");
        confirmAlert.setContentText("Êtes-vous ABSOLUMENT SÛR de vouloir supprimer cette catégorie?\n\n" +
                "ID: #" + currentCategorie.getIdCategorie() + "\n" +
                "Nom: " + currentCategorie.getNomCategorie() + "\n" +
                "Budget: " + String.format("%.2f DT", currentCategorie.getBudgetPrevu()) + "\n\n" +
                "⚠️ Cette action est IRRÉVERSIBLE!");

        ButtonType deleteButtonType = new ButtonType("Supprimer définitivement", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmAlert.getButtonTypes().setAll(deleteButtonType, cancelButtonType);

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == deleteButtonType) {
            if (BS.delete(currentCategorie.getIdCategorie())) {
                showSuccessAlert("Succès",
                        "La catégorie a été supprimée avec succès!");
                goBackToListe();
            } else {
                showErrorAlert("Erreur",
                        "Erreur lors de la suppression de la catégorie.");
            }
        }
    }

    @FXML
    private void resetForm() {
        if (currentCategorie != null) {
            loadCategorie(currentCategorie);
        }
    }

    @FXML
    private void goBackToListe() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Budget/CategorieListeGUI.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) tfNomCategorie.getScene().getWindow();
            Scene scene = new Scene(root);

            stage.setScene(scene);
            stage.setTitle("Liste des Catégories");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Erreur", "Impossible de retourner à la liste.");
        }
    }

    private boolean validateInput() {
        StringBuilder errors = new StringBuilder();
        boolean isValid = true;

        // Validate nom
        if (tfNomCategorie.getText().trim().isEmpty()) {
            errors.append("- Le nom de la catégorie est obligatoire.\n");
            tfNomCategorie.getStyleClass().add("error");
            isValid = false;
        } else {
            tfNomCategorie.getStyleClass().remove("error");
            tfNomCategorie.getStyleClass().add("success");
        }

        // Validate budget
        if (tfBudgetPrevu.getText().trim().isEmpty()) {
            errors.append("- Le budget prévu est obligatoire.\n");
            tfBudgetPrevu.getStyleClass().add("error");
            isValid = false;
        } else {
            try {
                double budget = Double.parseDouble(tfBudgetPrevu.getText().trim());
                if (budget <= 0) {
                    errors.append("- Le budget doit être supérieur à 0.\n");
                    tfBudgetPrevu.getStyleClass().add("error");
                    isValid = false;
                } else {
                    tfBudgetPrevu.getStyleClass().remove("error");
                    tfBudgetPrevu.getStyleClass().add("success");
                }
            } catch (NumberFormatException e) {
                errors.append("- Le budget doit être un nombre valide.\n");
                tfBudgetPrevu.getStyleClass().add("error");
                isValid = false;
            }
        }

        // Validate seuil
        if (tfSeuilAlerte.getText().trim().isEmpty()) {
            errors.append("- Le seuil d'alerte est obligatoire.\n");
            tfSeuilAlerte.getStyleClass().add("error");
            isValid = false;
        } else {
            try {
                double seuil = Double.parseDouble(tfSeuilAlerte.getText().trim());
                double budget = tfBudgetPrevu.getText().isEmpty() ? 0 :
                        Double.parseDouble(tfBudgetPrevu.getText().trim());

                if (seuil <= 0) {
                    errors.append("- Le seuil doit être supérieur à 0.\n");
                    tfSeuilAlerte.getStyleClass().add("error");
                    isValid = false;
                } else if (seuil > budget) {
                    errors.append("- Le seuil doit être inférieur ou égal au budget.\n");
                    tfSeuilAlerte.getStyleClass().add("error");
                    isValid = false;
                } else {
                    tfSeuilAlerte.getStyleClass().remove("error");
                    tfSeuilAlerte.getStyleClass().add("success");
                }
            } catch (NumberFormatException e) {
                errors.append("- Le seuil doit être un nombre valide.\n");
                tfSeuilAlerte.getStyleClass().add("error");
                isValid = false;
            }
        }

        if (!isValid) {
            showErrorAlert("Erreur de validation", errors.toString());
        }

        return isValid;
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