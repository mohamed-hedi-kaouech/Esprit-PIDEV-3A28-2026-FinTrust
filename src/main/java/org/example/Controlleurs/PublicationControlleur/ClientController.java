package org.example.Controlleurs.PublicationControlleur;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

import org.example.Model.Publication.Feedback;
import org.example.Model.Publication.Publication;
import org.example.Service.PublicationService.FeedbackService;
import org.example.Service.PublicationService.PublicationService;
import org.example.Utils.BadWordsApiClient;

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
    @FXML private Button backBtn;

    private PublicationService publicationService;
    private FeedbackService feedbackService;
    private final ObservableList<Publication> publications = FXCollections.observableArrayList();
    private static final int CURRENT_USER_ID = 1;
    // ✅ Placeholder tant que l'authentification n'est pas intégrée
    // Plus tard : remplace par l'id de l'utilisateur connecté (Session / Singleton / etc.)

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        publicationService = new PublicationService();
        feedbackService = new FeedbackService();

        loadPublications();

        // Fermeture de la fenêtre (vu que l'admin ouvre l'espace client en popup)
        backBtn.setOnAction(e -> ((Stage) backBtn.getScene().getWindow()).close());

        searchField.textProperty().addListener((obs, o, n) -> filter(n));
    }

    private void loadPublications() {
        clientListContainer.getChildren().clear();
        publications.clear();
        // Pour le moment : afficher toutes les publications.
        // Option (quand tu veux) : remplacer par publicationService.findVisiblePublications()
        publications.addAll(publicationService.findAll());
        for (Publication p : publications) {
            clientListContainer.getChildren().add(buildClientCard(p));
        }
    }

    private void filter(String q) {
        clientListContainer.getChildren().clear();
        String lower = q == null ? "" : q.toLowerCase();
        for (Publication p : publications) {
            if (lower.isEmpty() || p.getTitre().toLowerCase().contains(lower) || p.getContenu().toLowerCase().contains(lower)) {
                clientListContainer.getChildren().add(buildClientCard(p));
            }
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

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(id, title, spacer);

        // Content
        Label content = new Label(p.getContenu());
        content.getStyleClass().add("publication-content");
        content.setWrapText(true);

        // Actions
        HBox actions = new HBox(10);
        Button likeBtn = new Button("👍");
        Button dislikeBtn = new Button("👎");
        Button commentBtn = new Button("Commenter");

        Label likeCount = new Label();
        Label dislikeCount = new Label();
        Label avgRating = new Label();

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

            refreshFeedbackList(feedbackBox, p);

            // refresh stars selected for current user
            Feedback existingRating = feedbackService.getUserRating(p.getIdPublication(), CURRENT_USER_ID);
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
                feedbackService.upsertRating(p.getIdPublication(), CURRENT_USER_ID, ratingValue);
                refreshUI.run();
            });

            stars[i] = star;
            starsBox.getChildren().add(star);
        }

        // init
        refreshUI.run();

        // Like / Dislike
        likeBtn.setOnAction(e -> {
            feedbackService.toggleReaction(p.getIdPublication(), CURRENT_USER_ID, "LIKE");
            refreshUI.run();
        });

        dislikeBtn.setOnAction(e -> {
            feedbackService.toggleReaction(p.getIdPublication(), CURRENT_USER_ID, "DISLIKE");
            refreshUI.run();
        });

        // Comment
        commentBtn.setOnAction(e -> {
            openCommentDialog(p);
            refreshUI.run();
        });

        // UI order
        actions.getChildren().addAll(
                likeBtn, likeCount,
                dislikeBtn, dislikeCount,
                starsBox, avgRating,
                commentBtn
        );

        container.getChildren().addAll(header, content, actions, feedbackBox);
        return container;
    }

    private void refreshFeedbackList(VBox feedbackBox, Publication p) {
        feedbackBox.getChildren().clear();
        List<Feedback> list = feedbackService.getByPublication(p.getIdPublication());
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM HH:mm");

        for (Feedback f : list) {
            // Dans l'espace client : on affiche surtout les COMMENT + RATE
            // (les LIKE/DISLIKE sont visibles via les compteurs)
            if (f.getTypeReaction() == null) continue;
            String type = f.getTypeReaction().toUpperCase();
            if (type.equals("LIKE") || type.equals("DISLIKE")) continue;

            VBox commentCard = new VBox(4);
            commentCard.setStyle("""
    -fx-background-color: #f0f7ff;
    -fx-background-radius: 8;
    -fx-padding: 8;
    -fx-border-color: #dbeeff;
    -fx-border-radius: 8;
""");

            HBox row = new HBox(8);
            Label user = new Label("User#" + f.getIdUser());
            user.setStyle("-fx-text-fill: #0b5ed7; -fx-font-weight:600;");

            Label content = new Label(formatFeedbackText(f));
            content.setWrapText(true);

            Label date = new Label(f.getDateFeedback().format(fmt));
            date.setStyle("-fx-text-fill:#6c7b89; -fx-font-size:11px;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            // ✅ Le client peut modifier / supprimer SON commentaire et SA note
            if (f.getIdUser() == CURRENT_USER_ID && (type.equals("COMMENT") || type.startsWith("RATE"))) {
                Button editBtn = new Button("✏");
                Button delBtn = new Button("🗑");

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
                        feedbackService.deleteUserFeedback(p.getIdPublication(), CURRENT_USER_ID, "COMMENT");
                    } else {
                        feedbackService.deleteUserFeedback(p.getIdPublication(), CURRENT_USER_ID, "RATE");
                    }
                    refreshFeedbackList(feedbackBox, p);
                });
                row.getChildren().addAll(user, content, spacer, date, editBtn, delBtn);
            } else {
                row.getChildren().addAll(user, content, spacer, date);
            }

            commentCard.getChildren().add(row);
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
        Feedback existing = feedbackService.getUserComment(p.getIdPublication(), CURRENT_USER_ID);

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
                if (!BadWordsApiClient.isAllowed(cleaned)) {
                    new Alert(Alert.AlertType.ERROR, "Commentaire refusé : langage inapproprié").showAndWait();
                    return;
                }
                feedbackService.upsertComment(p.getIdPublication(), CURRENT_USER_ID, cleaned);
            } else if (result == deleteBtn) {
                feedbackService.deleteUserFeedback(p.getIdPublication(), CURRENT_USER_ID, "COMMENT");
            }
        });
    }

    private void openRatingDialog(Publication p) {
        Feedback existing = feedbackService.getUserRating(p.getIdPublication(), CURRENT_USER_ID);
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
                feedbackService.upsertRating(p.getIdPublication(), CURRENT_USER_ID, note);
            } else if (result == deleteBtn) {
                feedbackService.deleteUserFeedback(p.getIdPublication(), CURRENT_USER_ID, "RATE");
            }
        });
    }

    @FXML
    private void handleSearch() {
        filter(searchField.getText());
    }
}
