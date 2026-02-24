package org.example.Controlleurs.PublicationControlleur;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import org.example.Model.Publication.Publication;
import org.example.Model.Publication.Feedback;
import org.example.Service.PublicationService.FeedbackService;
import org.example.Service.PublicationService.PublicationService;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;

public class FeedbackController implements Initializable {

    @FXML private VBox listContainer;

    private PublicationService publicationService;
    private FeedbackService feedbackService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        publicationService = new PublicationService();
        feedbackService = new FeedbackService();
        loadPublications();
    }

    private void loadPublications() {
        listContainer.getChildren().clear();
        List<Publication> pubs = publicationService.findAll();
        for (Publication p : pubs) {
            listContainer.getChildren().add(buildPublicationCard(p));
        }
    }

    private VBox buildPublicationCard(Publication p) {
        VBox container = new VBox(6);
        container.setPadding(new Insets(12));
        container.getStyleClass().add("publication-card");

        HBox header = new HBox(10);
        Label title = new Label(p.getTitre());
        title.setFont(Font.font("System Bold", 14));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(new Label("#"+p.getIdPublication()), title, spacer);

        Label content = new Label(p.getContenu());
        content.setWrapText(true);

        HBox actions = new HBox(8);
        Button likeBtn = new Button("Like");
        Button dislikeBtn = new Button("Dislike");
        Button commentBtn = new Button("Commenter");
        Button rateBtn = new Button("Noter");

        likeBtn.setOnAction(e -> sendReaction(p, "LIKE", ""));
        dislikeBtn.setOnAction(e -> sendReaction(p, "DISLIKE", ""));

        commentBtn.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Commenter");
            dialog.setHeaderText("Ajouter un commentaire");
            dialog.setContentText("Votre commentaire:");
            dialog.showAndWait().ifPresent(text -> sendReaction(p, "COMMENT", text));
        });

        rateBtn.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Noter");
            dialog.setHeaderText("Donnez une note (1-5)");
            dialog.setContentText("Note:");
            dialog.showAndWait().ifPresent(text -> sendReaction(p, "RATE:"+text, text));
        });

        actions.getChildren().addAll(likeBtn, dislikeBtn, commentBtn, rateBtn);

        container.getChildren().addAll(header, content, actions);
        return container;
    }

    private void sendReaction(Publication p, String type, String commentaire) {
        try {
            // placeholder user id 1
            Feedback f = new Feedback(p.getIdPublication(), 1, commentaire, type);
            boolean ok = feedbackService.createFeedback(f);
            if (ok) {
                new Alert(Alert.AlertType.INFORMATION, "Feedback envoyé").showAndWait();
            } else {
                new Alert(Alert.AlertType.ERROR, "Erreur lors de l'envoi du feedback").showAndWait();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur interne").showAndWait();
        }
    }

    @FXML
    private void goBack() {
        try {
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("/MenuGUI.fxml"));
            javafx.stage.Stage stage = (javafx.stage.Stage) listContainer.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
