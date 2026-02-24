package org.example.Controlleurs.PublicationControlleur;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.example.Model.Publication.Feedback;
import org.example.Model.Publication.Publication;
import org.example.Service.PublicationService.FeedbackService;
import org.example.Utils.BadWordsApiClient;
import org.example.Utils.PiiApiClient;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class FeedbackUserController implements Initializable {

    @FXML private VBox feedbackList;
    @FXML private Label titleLabel;
    @FXML private Label likeCount;
    @FXML private Label dislikeCount;
    @FXML private Button likeBtn;
    @FXML private Button dislikeBtn;
    @FXML private TextArea commentArea;
    @FXML private Button submitBtn;
    @FXML private Button backButton;

    private FeedbackService feedbackService;
    private Publication publication;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        feedbackService = new FeedbackService();

        submitBtn.setOnAction(e -> submitComment());
        likeBtn.setOnAction(e -> sendReaction("LIKE"));
        dislikeBtn.setOnAction(e -> sendReaction("DISLIKE"));
        backButton.setOnAction(e -> back());
    }

    public void setPublication(Publication pub) {
        this.publication = pub;
        titleLabel.setText("Feedbacks — " + pub.getTitre());
        refreshAll();
    }

    private void refreshAll() {
        loadFeedbacks();
        updateCounts();
    }

    private void loadFeedbacks() {
        feedbackList.getChildren().clear();
        List<Feedback> list = feedbackService.getByPublication(publication.getIdPublication());
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        for (Feedback f : list) {
            HBox row = new HBox(10);
            row.setStyle("-fx-background-color: white; -fx-padding:8; -fx-border-color: #e6f0ff; -fx-border-radius:4; -fx-background-radius:4;");
            Label user = new Label("User#" + f.getIdUser());
            user.setStyle("-fx-font-weight: bold; -fx-text-fill: #0b5ed7;");
            Label date = new Label(f.getDateFeedback().format(fmt));
            date.setStyle("-fx-text-fill: #7a8a9a; -fx-font-size:11px;");
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label content = new Label(formatFeedbackText(f));
            content.setWrapText(true);
            content.setMaxWidth(520);

            VBox left = new VBox(4, user, date);
            row.getChildren().addAll(left, content, spacer);
            feedbackList.getChildren().add(row);
        }
    }

    private String formatFeedbackText(Feedback f) {
        if (f.getTypeReaction() == null) return "";
        String type = f.getTypeReaction().toUpperCase();

        switch (type) {
            case "LIKE":
                return "👍 Like";
            case "DISLIKE":
                return "👎 Dislike";
            case "COMMENT":
                return f.getCommentaire() == null ? "" : f.getCommentaire();
            default:
                // compat: RATE ou RATE:4...
                if (type.startsWith("RATE")) {
                    String note = (f.getCommentaire() != null && !f.getCommentaire().isBlank())
                            ? f.getCommentaire().trim()
                            : type.contains(":") ? type.substring(type.indexOf(':') + 1) : "";
                    return "⭐ Note: " + note + "/5";
                }
                return (f.getCommentaire() == null || f.getCommentaire().isBlank()) ? f.getTypeReaction() : f.getCommentaire();
        }
    }

    private void updateCounts() {
        try {
            int likes = feedbackService.countLikes(publication.getIdPublication());
            int dislikes = feedbackService.countDislikes(publication.getIdPublication());
            likeCount.setText(String.valueOf(likes));
            dislikeCount.setText(String.valueOf(dislikes));
        } catch (Exception ex) {
            likeCount.setText("0");
            dislikeCount.setText("0");
        }
    }

    private void submitComment() {
        String text = commentArea.getText();
        if (text == null || text.trim().isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Le commentaire est vide").showAndWait();
            return;
        }
        String cleaned = text.trim();
        if (!PiiApiClient.isAllowed(cleaned)) {
            new Alert(Alert.AlertType.ERROR, "Interdit d'ajouter des données sensibles").showAndWait();
            return;
        }
        if (!BadWordsApiClient.isAllowed(cleaned)) {
            new Alert(Alert.AlertType.ERROR, "language innaproprié").showAndWait();
            return;
        }
        Feedback f = new Feedback(publication.getIdPublication(), 1, cleaned, "COMMENT");
        feedbackService.createFeedback(f);
        commentArea.clear();
        refreshAll();
    }

    private static final int CURRENT_USER_ID = 1;

    private void sendReaction(String type) {
        feedbackService.toggleReaction(
                publication.getIdPublication(),
                CURRENT_USER_ID,
                type
        );
        refreshAll();
    }

    private void back() {
        try {
            javafx.stage.Stage stage = (javafx.stage.Stage) feedbackList.getScene().getWindow();
            stage.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
