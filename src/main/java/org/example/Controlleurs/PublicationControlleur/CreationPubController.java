package org.example.Controlleurs.PublicationControlleur;
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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.example.Model.Publication.Publication;
import org.example.Service.PublicationService.PublicationService;
import org.example.Utils.MaConnexion;

import java.sql.Connection;
import java.sql.SQLException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;

public class CreationPubController  implements Initializable  {
    // Form Fields
    @FXML private TextField titreField;
    @FXML private TextArea contenuArea;
    @FXML private TextField categorieField;
    @FXML private TextField statutField;
    @FXML private CheckBox estVisibleCheck;
    @FXML private DatePicker datePicker;
    @FXML private Label charCountLabel;



    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupContenuCounter();
    }

    @FXML
    private void goBackToList(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/Publication/ListePub.fxml")
            );

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Liste Publications");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupContenuCounter() {
        contenuArea.textProperty().addListener((observable, oldValue, newValue) -> {
            int length = newValue.length();
            charCountLabel.setText(length + "/1000 caractères"); // exemple : max 1000 caractères
            if (length > 1000) {
                contenuArea.setText(oldValue);
            }
        });
    }

    public void createPublication(ActionEvent actionEvent) {
        if (validateInput()) {
            try {
                PublicationService ps = new PublicationService();
                Publication publication = new Publication();
                publication.setTitre(titreField.getText());
                publication.setContenu(contenuArea.getText());
                // Fill optional fields with form values or sensible defaults
                publication.setCategorie(categorieField.getText() == null ? "" : categorieField.getText());
                publication.setStatut(statutField.getText() == null ? "" : statutField.getText());
                publication.setEstVisible(estVisibleCheck.isSelected());
                if (datePicker.getValue() != null) {
                    publication.setDatePublication(datePicker.getValue().atStartOfDay());
                } else {
                    publication.setDatePublication(java.time.LocalDateTime.now());
                }

                boolean created = ps.create(publication);
                if (created) {
                    showAlert(Alert.AlertType.INFORMATION, "Succès",
                            "La publication a été ajoutée avec succès!");
                    handleClear();
                } else {
                    showDetailedError("Erreur lors de l'ajout de la publication.");
                }
            } catch (Exception e) {
                showDetailedError("Une erreur est survenue lors de l'ajout.", e);
            }
        }
    }

    private boolean validateInput() {
        StringBuilder errors = new StringBuilder();
        if (titreField.getText().isEmpty()) {
            errors.append("- Le titre est obligatoire.\n");
        }
        if (contenuArea.getText().isEmpty()) {
            errors.append("- Le contenu est obligatoire.\n");
        } else if (contenuArea.getText().length() > 1000) {
            errors.append("- Le contenu ne doit pas dépasser 1000 caractères.\n");
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

    private void showDetailedError(String message) {
        showDetailedError(message, null);
    }

    private void showDetailedError(String message, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(message);

        String content = "Les publications ne pourront pas être chargées pour le moment.";
        if (e != null) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionText = sw.toString();

            TextArea textArea = new TextArea(exceptionText);
            textArea.setEditable(false);
            textArea.setWrapText(true);

            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            GridPane.setVgrow(textArea, Priority.ALWAYS);
            GridPane.setHgrow(textArea, Priority.ALWAYS);

            VBox dialogPaneContent = new VBox();
            dialogPaneContent.getChildren().add(new Label(content));
            dialogPaneContent.getChildren().add(textArea);

            alert.getDialogPane().setContent(dialogPaneContent);
        } else {
            alert.setContentText(content);
        }

        alert.showAndWait();
    }

    public void handleClear(ActionEvent actionEvent) {
        titreField.clear();
        contenuArea.clear();
    }

    public void handleClear() {
        titreField.clear();
        contenuArea.clear();
    }

    @FXML
    private void checkDbConnection(ActionEvent event) {
        try {
            Connection cnx = MaConnexion.getInstance().getCnx();
            boolean connected = (cnx != null && !cnx.isClosed());
            if (connected) {
                showAlert(Alert.AlertType.INFORMATION, "État BD", "Connecté à la base de données.");
            } else {
                showAlert(Alert.AlertType.WARNING, "État BD", "Non connecté à la base de données.");
            }
        } catch (SQLException ex) {
            showDetailedError("Erreur lors de la vérification de la connexion.", ex);
        }
    }
}
