package org.example.Controlleurs.PublicationControlleur;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.Model.Publication.Publication;
import org.example.Service.PublicationService.PublicationService;
import java.time.LocalDateTime;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

public class EditPubController {
    @FXML private TextField publicationIdField;

    // Fields matching UpdatePub.fxml
    @FXML private TextField titleField;
    @FXML private TextArea contentArea;
    @FXML private Label charCountLabel;
    @FXML private DatePicker createdAtPicker;
    @FXML private Label currentTitleLabel;
    @FXML private Label currentDateLabel;

    private Publication currentPublication;
    private PublicationService publicationService;

    public void initialize() {
        publicationService = new PublicationService();
        setupDescriptionCounter();
    }

    // ================= LOAD =================
    public void loadPublication(Publication publication) {

        if (publication == null) {
            showError("Erreur", "Aucune publication à modifier.");
            return;
        }

        this.currentPublication = publication;

        // populate fields that exist in the FXML
        publicationIdField.setText(String.valueOf(publication.getIdPublication()));
        titleField.setText(publication.getTitre());
        contentArea.setText(publication.getContenu());

        currentTitleLabel.setText(publication.getTitre());
        if (publication.getDatePublication() != null) {
            currentDateLabel.setText(publication.getDatePublication().toLocalDate().toString());
            createdAtPicker.setValue(publication.getDatePublication().toLocalDate());
        }

        charCountLabel.setText(contentArea.getText().length() + "/500 caractères");
    }

    // ================= UPDATE =================
    @FXML
    private void handleUpdate() {

        if (!validateInput()) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Confirmer la modification ?");
        confirm.setContentText("Titre : " + titleField.getText());

        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {

            // Update from fields present in this edit form
            currentPublication.setTitre(titleField.getText());
            currentPublication.setContenu(contentArea.getText());

            // Conversion LocalDate -> LocalDateTime
            if (createdAtPicker.getValue() != null) {
                LocalDateTime dateTime = LocalDateTime.of(createdAtPicker.getValue(), LocalTime.now());
                currentPublication.setDatePublication(dateTime);
            }

            if (publicationService.update(currentPublication)) {
                showSuccess("Succès", "Publication modifiée avec succès !");
                goBackToList();
            } else {
                showError("Erreur", "Erreur lors de la modification.");
            }
        }
    }

    // ================= DELETE =================
    @FXML
    private void handleDelete() {

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Suppression");
        confirm.setHeaderText("Supprimer cette publication ?");
        confirm.setContentText("Cette action est irréversible.");

        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {

            if (publicationService.delete(currentPublication.getIdPublication())) {
                showSuccess("Succès", "Publication supprimée !");
                goBackToList();
            } else {
                showError("Erreur", "Impossible de supprimer.");
            }
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        goBackToList();
    }

    // ================= VALIDATION =================
    private boolean validateInput() {

        StringBuilder errors = new StringBuilder();

        if (titleField.getText().isEmpty())
            errors.append("- Le titre est obligatoire\n");

        if (contentArea.getText().isEmpty())
            errors.append("- Le contenu est obligatoire\n");

        if (contentArea.getText().length() > 500)
            errors.append("- Maximum 500 caractères\n");

        if (errors.length() > 0) {
            showError("Erreur de validation", errors.toString());
            return false;
        }

        return true;
    }

    private void setupDescriptionCounter() {
        contentArea.textProperty().addListener((obs, oldText, newText) -> {
            if (newText.length() > 500) {
                contentArea.setText(oldText);
            }
            charCountLabel.setText(contentArea.getText().length() + "/500 caractères");
        });
    }

    // ================= NAVIGATION =================
    @FXML
    private void goBackToList() {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/Publication/ListePub.fxml")
            );

            Stage stage = (Stage) titleField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Liste Publications");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showSuccess(String title, String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg).showAndWait();
    }

    private void showError(String title, String msg) {
        new Alert(Alert.AlertType.ERROR, msg).showAndWait();
    }
}
