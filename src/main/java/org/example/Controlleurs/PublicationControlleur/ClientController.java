package org.example.Controlleurs.PublicationControlleur;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.example.Model.Publication.Feedback;
import org.example.Model.Publication.Publication;
import org.example.Service.PublicationService.FeedbackService;
import org.example.Service.PublicationService.PublicationService;
import org.example.Utils.BadWordsApiClient;
import org.example.Utils.PiiApiClient;
import org.example.Utils.SummaryApiClient;
import org.example.Utils.SummaryResult;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ClientController implements Initializable {

    @FXML private VBox clientListContainer;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> sortModeCombo;
    @FXML private Button backBtn;

    private PublicationService publicationService;
    private FeedbackService feedbackService;
    private final ObservableList<Publication> publications = FXCollections.observableArrayList();
    private final Map<Integer, SummaryResult> summaryCache = new HashMap<>();
    private int currentUserId = 1;
    // ✅ Placeholder tant que l'authentification n'est pas intégrée
    // Plus tard : remplace par l'id de l'utilisateur connecté (Session / Singleton / etc.)

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        publicationService = new PublicationService();
        feedbackService = new FeedbackService();
        currentUserId = feedbackService.resolveClientUserId(currentUserId);

        sortModeCombo.getItems().addAll("Tendance", "Mieux notees", "Recentes");
        sortModeCombo.setValue("Tendance");
        sortModeCombo.valueProperty().addListener((obs, oldValue, newValue) -> refreshFeed());

        loadPublications();

        // Fermeture de la fenêtre (vu que l'admin ouvre l'espace client en popup)
        backBtn.setOnAction(e -> ((Stage) backBtn.getScene().getWindow()).close());

        searchField.textProperty().addListener((obs, o, n) -> refreshFeed());
    }

    private void loadPublications() {
        publications.clear();
        // Pour le moment : afficher toutes les publications.
        // Option (quand tu veux) : remplacer par publicationService.findVisiblePublications()
        publications.addAll(publicationService.findAll());
        refreshFeed();
    }

    private void refreshFeed() {
        clientListContainer.getChildren().clear();
        String q = searchField == null ? "" : searchField.getText();
        String lower = q == null ? "" : q.toLowerCase();

        List<Publication> visible = new ArrayList<>();
        for (Publication p : publications) {
            if (lower.isEmpty()
                    || p.getTitre().toLowerCase().contains(lower)
                    || p.getContenu().toLowerCase().contains(lower)) {
                visible.add(p);
            }
        }

        String mode = sortModeCombo == null ? "Tendance" : sortModeCombo.getValue();
        if ("Recentes".equals(mode)) {
            visible.sort(Comparator.comparing(Publication::getDatePublication, Comparator.nullsLast(Comparator.reverseOrder())));
        } else if ("Mieux notees".equals(mode)) {
            visible.sort((a, b) -> Double.compare(
                    feedbackService.averageRating(b.getIdPublication()),
                    feedbackService.averageRating(a.getIdPublication())
            ));
        } else {
            visible.sort((a, b) -> Double.compare(
                    calculateTrendingScore(b),
                    calculateTrendingScore(a)
            ));
        }

        for (Publication p : visible) {
            clientListContainer.getChildren().add(buildClientCard(p));
        }
    }
    private VBox buildClientCard(Publication p) {
        VBox container = new VBox(10);
        container.setPadding(new Insets(12));
        container.getStyleClass().add("publication-card");

        // Header
        HBox header = new HBox(8);
        Label id = new Label("#" + p.getIdPublication());
        id.getStyleClass().add("publication-id");

        Label title = new Label(p.getTitre());
        title.getStyleClass().add("publication-title");

        Label trendScoreLabel = new Label();
        trendScoreLabel.setStyle("-fx-text-fill: #0b5ed7; -fx-font-weight: 700;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(id, title, spacer, trendScoreLabel);

        // Content
        Label content = new Label(p.getContenu());
        content.getStyleClass().add("publication-content");
        content.setWrapText(true);

        // Actions
        HBox actions = new HBox(10);
        actions.getStyleClass().add("publication-action-row");
        Button likeBtn = new Button("👍");
        Button dislikeBtn = new Button("👎");
        Button commentBtn = new Button("Commenter");
        Button summaryBtn = new Button("Voir resume IA");
        likeBtn.getStyleClass().add("btn-like");
        dislikeBtn.getStyleClass().add("btn-dislike");
        commentBtn.getStyleClass().add("btn-comment");
        summaryBtn.getStyleClass().add("btn-outline");

        Label likeCount = new Label();
        Label dislikeCount = new Label();
        Label avgRating = new Label();
        likeCount.getStyleClass().addAll("count-badge", "count-like");
        dislikeCount.getStyleClass().addAll("count-badge", "count-dislike");
        avgRating.getStyleClass().addAll("count-badge", "count-rate");
        Label globalRatingLabel = new Label();
        globalRatingLabel.setStyle("-fx-text-fill: #0f4ea5; -fx-font-weight: 700;");
        Label globalSentimentLabel = new Label();
        globalSentimentLabel.setStyle("-fx-text-fill: #184f90; -fx-font-weight: 800;");
        Label engagementLabel = new Label();
        engagementLabel.setStyle("-fx-text-fill: #2b5d99; -fx-font-size: 12px;");
        Label summaryLabel = new Label("Resume IA non charge.");
        summaryLabel.setWrapText(true);
        summaryLabel.setStyle("-fx-text-fill: #2b4f7a; -fx-font-size: 12px;");
        VBox insightsBox = new VBox(4, globalRatingLabel, globalSentimentLabel, engagementLabel, summaryLabel);
        insightsBox.setPadding(new Insets(4, 0, 2, 0));

        // Feedback list under publication
        VBox feedbackBox = new VBox(6);
        feedbackBox.setPadding(new Insets(6));

        // Stars box ⭐⭐⭐⭐⭐
        HBox starsBox = new HBox(4);
        Label[] stars = new Label[5];

        // ✅ Refresh UI (déclaré AVANT utilisation)
        Runnable refreshUI = () -> {
            likeCount.setText(String.valueOf(feedbackService.countLikes(p.getIdPublication())));
            dislikeCount.setText(String.valueOf(feedbackService.countDislikes(p.getIdPublication())));

            double avg = feedbackService.averageRating(p.getIdPublication());
            avgRating.setText(avg > 0 ? String.format("⭐ %.1f", avg) : "⭐ -");

            int ratingsCount = feedbackService.countRatings(p.getIdPublication());
            int commentsCount = feedbackService.countComments(p.getIdPublication());
            double avgForLabel = feedbackService.averageRating(p.getIdPublication());
            globalRatingLabel.setText(String.format("Rating general: %.1f/5 (%d notes)", avgForLabel, ratingsCount));
            engagementLabel.setText("Engagement: 👍 " + likeCount.getText() + " | 👎 " + dislikeCount.getText() + " | 💬 " + commentsCount);

            SummaryResult cached = summaryCache.get(p.getIdPublication());
            if (cached != null) {
                globalSentimentLabel.setText("Avis global: " + cached.getRatingLabel());
                summaryLabel.setText("Resume IA: " + cached.getSummary());
            } else {
                List<String> comments = feedbackService.getRecentCommentTexts(p.getIdPublication(), 20);
                SummaryResult generated = SummaryApiClient.summarize(p.getIdPublication(), p.getTitre(), comments);
                summaryCache.put(p.getIdPublication(), generated);
                globalSentimentLabel.setText("Avis global: " + generated.getRatingLabel());
                summaryLabel.setText("Resume IA: " + generated.getSummary());
            }

            refreshFeedbackList(feedbackBox, p);
            trendScoreLabel.setText(String.format("Score: %.2f", calculateTrendingScore(p)));

            // refresh stars selected for current user
            Feedback existingRating = feedbackService.getUserRating(p.getIdPublication(), currentUserId);
            int userRating = 0;
            if (existingRating != null && existingRating.getCommentaire() != null) {
                try { userRating = Integer.parseInt(existingRating.getCommentaire().trim()); } catch (Exception ignored) {}
            }
            for (int i = 0; i < 5; i++) {
                stars[i].setText((i + 1) <= userRating ? "★" : "☆");
            }
        };

        // init stars (après refreshUI)
        for (int i = 0; i < 5; i++) {
            final int ratingValue = i + 1;
            Label star = new Label("☆");
            star.setStyle("-fx-font-size: 18px; -fx-text-fill: #f5c518;");

            star.setOnMouseClicked(e -> {
                feedbackService.upsertRating(p.getIdPublication(), currentUserId, ratingValue);
                refreshUI.run();
            });

            stars[i] = star;
            starsBox.getChildren().add(star);
        }

        // init
        refreshUI.run();

        // Like / Dislike
        likeBtn.setOnAction(e -> {
            feedbackService.toggleReaction(p.getIdPublication(), currentUserId, "LIKE");
            refreshUI.run();
        });

        dislikeBtn.setOnAction(e -> {
            feedbackService.toggleReaction(p.getIdPublication(), currentUserId, "DISLIKE");
            refreshUI.run();
        });

        // Comment
        commentBtn.setOnAction(e -> {
            openCommentDialog(p);
            refreshUI.run();
        });
        summaryBtn.setOnAction(e -> {
            List<String> comments = feedbackService.getRecentCommentTexts(p.getIdPublication(), 20);
            SummaryResult result = SummaryApiClient.summarize(p.getIdPublication(), p.getTitre(), comments);
            summaryCache.put(p.getIdPublication(), result);
            globalSentimentLabel.setText("Avis global: " + result.getRatingLabel());
            summaryLabel.setText("Resume IA: " + result.getSummary());
        });

        // UI order
        actions.getChildren().addAll(
                likeBtn, likeCount,
                dislikeBtn, dislikeCount,
                starsBox, avgRating,
                commentBtn, summaryBtn
        );

        container.getChildren().addAll(header, content, insightsBox, actions, feedbackBox);
        return container;
    }

    private void refreshFeedbackList(VBox feedbackBox, Publication p) {
        feedbackBox.getChildren().clear();
        List<Feedback> list = feedbackService.getByPublication(p.getIdPublication());
        Map<Integer, List<Feedback>> adminReplies = feedbackService.getAdminRepliesGrouped(p.getIdPublication());
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM HH:mm");

        for (Feedback f : list) {
            // Dans l'espace client : on affiche surtout les COMMENT + RATE
            // (les LIKE/DISLIKE sont visibles via les compteurs)
            if (f.getTypeReaction() == null) continue;
            String type = f.getTypeReaction().toUpperCase();
            if (type.equals("LIKE") || type.equals("DISLIKE") || FeedbackService.ADMIN_REPLY_TYPE.equals(type)) continue;

            VBox commentCard = new VBox(4);
            commentCard.getStyleClass().add("comment-card");

            HBox row = new HBox(8);
            Label user = new Label("User#" + f.getIdUser());
            user.getStyleClass().add("comment-author");

            Label content = new Label(formatFeedbackText(f));
            content.setWrapText(true);
            content.getStyleClass().add("comment-body");

            Label date = new Label(f.getDateFeedback().format(fmt));
            date.getStyleClass().add("comment-date");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            // ✅ Le client peut modifier / supprimer SON commentaire et SA note
            if (f.getIdUser() == currentUserId && (type.equals("COMMENT") || type.startsWith("RATE"))) {
                Button editBtn = new Button("✏");
                Button delBtn = new Button("🗑");
                editBtn.getStyleClass().add("btn-comment");
                delBtn.getStyleClass().add("btn-dislike");

                editBtn.setOnAction(e -> {
                    if (type.equals("COMMENT")) {
                        openCommentDialog(p);
                    } else {
                        openRatingDialog(p);
                    }
                    // refresh after edit
                    refreshFeedbackList(feedbackBox, p);
                });

                delBtn.setOnAction(e -> {
                    if (type.equals("COMMENT")) {
                        feedbackService.deleteUserFeedback(p.getIdPublication(), currentUserId, "COMMENT");
                    } else {
                        feedbackService.deleteUserFeedback(p.getIdPublication(), currentUserId, "RATE");
                    }
                    refreshFeedbackList(feedbackBox, p);
                });
                row.getChildren().addAll(user, content, spacer, date, editBtn, delBtn);
            } else {
                row.getChildren().addAll(user, content, spacer, date);
            }

            commentCard.getChildren().add(row);

            if ("COMMENT".equals(type)) {
                List<Feedback> replies = adminReplies.get(f.getIdFeedback());
                if (replies != null) {
                    for (Feedback reply : replies) {
                        HBox replyRow = new HBox(8);
                        replyRow.getStyleClass().add("reply-row");
                        Label admin = new Label("Admin:");
                        admin.getStyleClass().add("reply-author");
                        Label replyText = new Label(feedbackService.extractReplyBody(reply.getCommentaire()));
                        replyText.setWrapText(true);
                        replyText.getStyleClass().add("reply-body");
                        replyRow.getChildren().addAll(admin, replyText);
                        commentCard.getChildren().add(replyRow);
                    }
                }
            }

            feedbackBox.getChildren().add(commentCard);        }
    }

    private String formatFeedbackText(Feedback f) {
        if (f.getTypeReaction() == null) return "";
        String type = f.getTypeReaction().toUpperCase();
        if (type.equals("COMMENT")) {
            return f.getCommentaire() == null ? "" : f.getCommentaire();
        }
        if (type.startsWith("RATE")) {
            // compat: type peut être RATE ou RATE:4
            String note = (f.getCommentaire() != null && !f.getCommentaire().isBlank())
                    ? f.getCommentaire().trim()
                    : type.contains(":") ? type.substring(type.indexOf(':') + 1) : "";
            return "⭐ Note: " + note + "/5";
        }
        // fallback
        return (f.getCommentaire() == null || f.getCommentaire().isBlank()) ? f.getTypeReaction() : f.getCommentaire();
    }

    private void openCommentDialog(Publication p) {
        Feedback existing = feedbackService.getUserComment(p.getIdPublication(), currentUserId);

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Commentaire");
        dialog.setHeaderText("Votre commentaire sur : " + p.getTitre());

        TextArea area = new TextArea(existing != null ? existing.getCommentaire() : "");
        area.setWrapText(true);
        area.setPrefRowCount(5);

        dialog.getDialogPane().setContent(area);

        ButtonType saveBtn = new ButtonType(existing == null ? "Publier" : "Mettre à jour", ButtonBar.ButtonData.OK_DONE);
        ButtonType deleteBtn = new ButtonType("Supprimer", ButtonBar.ButtonData.OTHER);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, deleteBtn, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(result -> {
            if (result == saveBtn) {
                String txt = area.getText();
                if (txt == null || txt.trim().isEmpty()) {
                    new Alert(Alert.AlertType.WARNING, "Le commentaire est vide").showAndWait();
                    return;
                }
                String cleaned = txt.trim();
                if (!PiiApiClient.isAllowed(cleaned)) {
                    new Alert(Alert.AlertType.ERROR, "Interdit d'ajouter des données sensibles").showAndWait();
                    return;
                }
                if (!BadWordsApiClient.isAllowed(cleaned)) {
                    new Alert(Alert.AlertType.ERROR, "language innaproprié").showAndWait();
                    return;
                }
                feedbackService.upsertComment(p.getIdPublication(), currentUserId, cleaned);
            } else if (result == deleteBtn) {
                feedbackService.deleteUserFeedback(p.getIdPublication(), currentUserId, "COMMENT");
            }
        });
    }

    private void openRatingDialog(Publication p) {
        Feedback existing = feedbackService.getUserRating(p.getIdPublication(), currentUserId);
        int existingNote = 0;
        try {
            if (existing != null && existing.getCommentaire() != null) {
                existingNote = Integer.parseInt(existing.getCommentaire().trim());
            }
        } catch (Exception ignored) {
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Noter");
        dialog.setHeaderText("Donnez une note (1 à 5)");

        javafx.scene.control.ChoiceBox<Integer> cb = new javafx.scene.control.ChoiceBox<>();
        cb.getItems().addAll(1, 2, 3, 4, 5);
        if (existingNote >= 1 && existingNote <= 5) {
            cb.setValue(existingNote);
        } else {
            cb.setValue(5);
        }

        dialog.getDialogPane().setContent(cb);

        ButtonType saveBtn = new ButtonType(existing == null ? "Enregistrer" : "Mettre à jour", ButtonBar.ButtonData.OK_DONE);
        ButtonType deleteBtn = new ButtonType("Supprimer", ButtonBar.ButtonData.OTHER);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, deleteBtn, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(result -> {
            if (result == saveBtn) {
                Integer note = cb.getValue();
                if (note == null) {
                    new Alert(Alert.AlertType.WARNING, "Veuillez choisir une note").showAndWait();
                    return;
                }
                feedbackService.upsertRating(p.getIdPublication(), currentUserId, note);
            } else if (result == deleteBtn) {
                feedbackService.deleteUserFeedback(p.getIdPublication(), currentUserId, "RATE");
            }
        });
    }

    private double calculateTrendingScore(Publication p) {
        int likes = feedbackService.countLikes(p.getIdPublication());
        int dislikes = feedbackService.countDislikes(p.getIdPublication());
        int comments = feedbackService.countComments(p.getIdPublication());
        double avgRating = feedbackService.averageRating(p.getIdPublication());
        double recencyBonus = computeRecencyBonus(p.getDatePublication());

        return (likes - dislikes) + (avgRating * 3.0) + (comments * 0.5) + recencyBonus;
    }

    private double computeRecencyBonus(LocalDateTime publicationDate) {
        if (publicationDate == null) {
            return 0.0;
        }
        long ageDays = Math.max(0, ChronoUnit.DAYS.between(publicationDate, LocalDateTime.now()));
        return 10.0 / (1.0 + ageDays);
    }

    @FXML
    private void handleSearch() {
        refreshFeed();
    }
}
