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

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class FeedbackUserController implements Initializable {

    @FXML private VBox feedbackList;
    @FXML private Label titleLabel;
    @FXML private Label likeCount;
    @FXML private Label dislikeCount;
    @FXML private Label avgRatingLabel;
    @FXML private Label ratingCountLabel;
    @FXML private Button backButton;

    private FeedbackService feedbackService;
    private Publication publication;
    private static final int ADMIN_USER_ID = 9999;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        feedbackService = new FeedbackService();
        backButton.setOnAction(e -> back());
    }

    public void setPublication(Publication pub) {
        this.publication = pub;
        titleLabel.setText("Feedbacks — " + pub.getTitre());
        refreshAll();
    }

    private void refreshAll() {
        updateStats();
        loadFeedbacks();
    }

    private void updateStats() {
        int likes = feedbackService.countLikes(publication.getIdPublication());
        int dislikes = feedbackService.countDislikes(publication.getIdPublication());
        double avg = feedbackService.averageRating(publication.getIdPublication());
        int ratingCount = feedbackService.countRatings(publication.getIdPublication());

        likeCount.setText(String.valueOf(likes));
        dislikeCount.setText(String.valueOf(dislikes));
        avgRatingLabel.setText(String.format("%.1f/5", avg));
        ratingCountLabel.setText(String.valueOf(ratingCount));
    }

    private void loadFeedbacks() {
        feedbackList.getChildren().clear();
        List<Feedback> list = feedbackService.getByPublication(publication.getIdPublication());
        Map<Integer, List<Feedback>> replies = feedbackService.getAdminRepliesGrouped(publication.getIdPublication());
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        for (Feedback f : list) {
            String type = f.getTypeReaction() == null ? "" : f.getTypeReaction().toUpperCase();
            if (!"COMMENT".equals(type)) {
                continue;
            }

            VBox commentCard = new VBox(6);
            commentCard.setStyle("-fx-background-color: white; -fx-padding:10; -fx-border-color: #dbe9ff; -fx-border-radius:8; -fx-background-radius:8;");

            HBox row = new HBox(10);
            Label user = new Label("Client#" + f.getIdUser());
            user.setStyle("-fx-font-weight: bold; -fx-text-fill: #0b5ed7;");

            Label content = new Label(f.getCommentaire() == null ? "" : f.getCommentaire());
            content.setWrapText(true);
            content.setStyle("-fx-text-fill: #163a6b; -fx-font-size: 13px;");

            Label date = new Label(f.getDateFeedback().format(fmt));
            date.setStyle("-fx-text-fill: #7a8a9a; -fx-font-size:11px;");

            Button replyBtn = new Button("Répondre");
            replyBtn.getStyleClass().add("btn-outline");
            replyBtn.setOnAction(e -> replyToComment(f));

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            row.getChildren().addAll(user, content, spacer, date, replyBtn);
            commentCard.getChildren().add(row);

            List<Feedback> commentReplies = replies.get(f.getIdFeedback());
            if (commentReplies != null) {
                for (Feedback reply : commentReplies) {
                    HBox repRow = new HBox(8);
                    repRow.setStyle("-fx-background-color: #eef5ff; -fx-padding:8; -fx-background-radius:6; -fx-border-color:#d4e5ff; -fx-border-radius:6;");
                    Label admin = new Label("Admin");
                    admin.setStyle("-fx-font-weight: bold; -fx-text-fill: #1450a3;");
                    Label repText = new Label(feedbackService.extractReplyBody(reply.getCommentaire()));
                    repText.setWrapText(true);
                    repText.setStyle("-fx-text-fill: #1f4b83;");
                    repRow.getChildren().addAll(admin, repText);
                    commentCard.getChildren().add(repRow);
                }
            }

            feedbackList.getChildren().add(commentCard);
        }
    }

    private void replyToComment(Feedback comment) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Réponse admin");
        dialog.setHeaderText("Répondre au commentaire de Client#" + comment.getIdUser());
        dialog.setContentText("Votre réponse:");
        dialog.showAndWait().ifPresent(text -> {
            if (text == null || text.trim().isEmpty()) {
                return;
            }
            boolean ok = feedbackService.createAdminReply(
                    publication.getIdPublication(),
                    ADMIN_USER_ID,
                    comment.getIdFeedback(),
                    text.trim()
            );
            if (!ok) {
                new Alert(Alert.AlertType.ERROR, "Impossible d'ajouter la réponse admin (vérifier users.id en base).").showAndWait();
                return;
            }
            refreshAll();
        });
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
