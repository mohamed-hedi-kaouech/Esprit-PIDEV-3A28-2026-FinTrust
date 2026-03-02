package org.example.Controlleurs.PublicationControlleur;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;

import org.example.Model.Publication.Feedback;
import org.example.Model.Publication.Publication;
import org.example.Service.PublicationService.FeedbackService;
import org.example.Service.PublicationService.PublicationService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.scene.control.TextField;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;


public class ListPubController implements Initializable {

    @FXML private ListView<Publication> publicationListView;
    @FXML private TextField searchField;
    @FXML private Label totalPublicationsLabel;

    private ObservableList<Publication> publicationList = FXCollections.observableArrayList();
    private ObservableList<Publication> filteredList = FXCollections.observableArrayList();
    private PublicationService PS;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            PS = new PublicationService();
        } catch (Exception ex) {
            PS = null;
            ex.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Avertissement");
            alert.setHeaderText("Impossible de se connecter à la base de données");
            alert.setContentText("Les publications ne pourront pas être chargées pour le moment.");
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            javafx.scene.control.TextArea textArea = new javafx.scene.control.TextArea(sw.toString());
            textArea.setEditable(false);
            textArea.setWrapText(true);
            alert.getDialogPane().setExpandableContent(textArea);
            alert.showAndWait();
        }

        setupListView();
        loadPublicationData();
        setupSearchListener();
    }

    @FXML
    private void goToCreatePage(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/Publication/CreatePub.fxml")
            );
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Créer Publication");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 🔹 Méthode appelée quand on clique sur le bouton "Client"
    // Ouvre une fenêtre séparée (pratique tant que le projet n'est pas encore intégré).
    @FXML
    public void goToClientView(ActionEvent event) {

        try {
            // ✅ Le FXML est dans /resources/Publication/ClientView.fxml
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/Publication/ClientView.fxml")
            );

            Parent root = loader.load();

            // Ouvrir dans une nouvelle fenêtre pour ne pas casser l'interface admin
            Stage popup = new Stage();
            popup.setTitle("Espace Client - Publications");
            popup.setScene(new Scene(root));
            popup.initOwner(((Node) event.getSource()).getScene().getWindow());
            popup.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("❌ Erreur lors du chargement de /Publication/ClientView.fxml");
        }
    }

    @FXML
    public void goToFeedbackStats(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/Publication/FeedbackStats.fxml")
            );
            Parent root = loader.load();

            Stage popup = new Stage();
            popup.setTitle("Statistiques Feedback - Publications");
            popup.setScene(new Scene(root));
            popup.initOwner(((Node) event.getSource()).getScene().getWindow());
            popup.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Erreur", "Impossible d'ouvrir le dashboard des statistiques feedback.");
        }
    }

    @FXML
    private void goBackToMenu(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/MenuGUI.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Manager");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    private void setupListView() {
        publicationListView.setCellFactory(listView -> new PublicationListCell());
        Label placeholder = new Label("Aucune publication disponible");
        placeholder.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 14px; -fx-font-style: italic;");
        publicationListView.setPlaceholder(placeholder);
    }

    private void loadPublicationData() {
        publicationList.clear();
        if (PS != null) {
            publicationList.addAll(PS.findAll()); // correct method name
        }
        filteredList.setAll(publicationList);
        publicationListView.setItems(filteredList);
        updateTotalLabel();
    }

    private void setupSearchListener() {
        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> filterPublications(newValue));
        }
    }

    private void filterPublications(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            filteredList.setAll(publicationList);
        } else {
            filteredList.clear();
            String lowerCaseFilter = searchText.toLowerCase();

            for (Publication pub : publicationList) {
                if (pub.getTitre().toLowerCase().contains(lowerCaseFilter) ||
                        pub.getContenu().toLowerCase().contains(lowerCaseFilter) ||
                        String.valueOf(pub.getIdPublication()).contains(lowerCaseFilter)) {
                    filteredList.add(pub);
                }
            }
        }
        updateTotalLabel();
    }

    private void updateTotalLabel() {
        if (totalPublicationsLabel != null) {
            int total = filteredList.size();
            totalPublicationsLabel.setText(String.format("Total: %d publication%s", total, total > 1 ? "s" : ""));
        }
    }

    @FXML
    private void handleSearch() {
        filterPublications(searchField.getText());
    }

    @FXML
    private void handleReset() {
        searchField.clear();
        filteredList.setAll(publicationList);
        updateTotalLabel();
    }

    @FXML
    private void exportMonthlyStats(ActionEvent event) {
        TextInputDialog monthDialog = new TextInputDialog(YearMonth.now().toString());
        monthDialog.setTitle("Export stats mensuelles");
        monthDialog.setHeaderText("Saisir le mois au format YYYY-MM");
        monthDialog.setContentText("Mois:");

        Optional<String> monthResult = monthDialog.showAndWait();
        if (monthResult.isEmpty()) {
            return;
        }

        YearMonth ym;
        try {
            ym = YearMonth.parse(monthResult.get().trim());
        } catch (Exception ex) {
            showErrorAlert("Format invalide", "Utilise le format YYYY-MM (ex: 2026-02).");
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Exporter stats mensuelles en CSV");
        chooser.setInitialFileName("stats_feedback_" + ym + ".csv");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        java.io.File file = chooser.showSaveDialog(((Node) event.getSource()).getScene().getWindow());
        if (file == null) {
            return;
        }

        FeedbackService service = new FeedbackService();
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.plusMonths(1).atDay(1);
        boolean ok = service.exportMonthlyStatsToCSV(start, end, file);
        if (ok) {
            showSuccessAlert("Export réussi", "Stats mensuelles exportées: " + file.getName());
        } else {
            showErrorAlert("Export échoué", "Impossible d'exporter les stats mensuelles.");
        }
    }

    private void handleUpdate(Publication pub) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Publication/UpdatePub.fxml"));
            Parent root = loader.load();

            EditPubController controller = loader.getController();
            controller.loadPublication(pub);

            Stage stage = (Stage) publicationListView.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleDelete(Publication pub) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation de suppression");
        confirmAlert.setHeaderText("Supprimer la publication");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer cette publication?\n\nTitre: " + pub.getTitre() + "\n\nCette action est irréversible!");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (PS.delete(pub.getIdPublication())) { // use correct getter
                showSuccessAlert("Succès", "La publication a été supprimée avec succès!");
                loadPublicationData();
            } else {
                showErrorAlert("Erreur", "Erreur lors de la suppression de la publication.");
            }
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

    // ==================== Custom ListView Cell ====================
    private class PublicationListCell extends ListCell<Publication> {
        private final VBox container;
        private final HBox headerBox;
        private final HBox bodyBox;
        private final HBox footerBox;

        private final Label idLabel;
        private final Label titreLabel;
        private final Label contenuLabel;
        private final Label dateLabel;
        private final Button updateButton;
        private final Button deleteButton;
        private final Button feedbackButton;
        private final Button likeButton;
        private final Button dislikeButton;
        private final Button exportCsvButton;
        private final Button exportPdfButton;
        private final Label likeCountLabel;
        private final Label dislikeCountLabel;
        private final FeedbackService feedbackService;

        public PublicationListCell() {
            super();

            container = new VBox(10);
            container.setPadding(new Insets(15));
            container.getStyleClass().add("publication-card");

            headerBox = new HBox(15);
            headerBox.setAlignment(Pos.CENTER_LEFT);

            idLabel = new Label();
            idLabel.getStyleClass().add("publication-id");
            idLabel.setFont(Font.font("System Bold", 14));

            titreLabel = new Label();
            titreLabel.getStyleClass().add("publication-title");
            titreLabel.setFont(Font.font("System Bold", 14));

            Region spacer1 = new Region();
            HBox.setHgrow(spacer1, Priority.ALWAYS);

            headerBox.getChildren().addAll(idLabel, titreLabel, spacer1);

            bodyBox = new HBox();
            bodyBox.setAlignment(Pos.CENTER_LEFT);

            contenuLabel = new Label();
            contenuLabel.getStyleClass().add("publication-content");
            contenuLabel.setWrapText(true);
            contenuLabel.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(contenuLabel, Priority.ALWAYS);

            bodyBox.getChildren().add(contenuLabel);

            footerBox = new HBox(10);
            footerBox.setAlignment(Pos.CENTER_LEFT);

            dateLabel = new Label();
            dateLabel.getStyleClass().add("publication-date");
            dateLabel.getStyleClass().add("date-chip");
            dateLabel.setFont(Font.font("System", 11));

            Region spacer2 = new Region();
            HBox.setHgrow(spacer2, Priority.ALWAYS);

            updateButton = new Button("Modifier");
            updateButton.getStyleClass().add("btn-update");

            deleteButton = new Button("Supprimer");
            deleteButton.getStyleClass().add("btn-delete");

            feedbackButton = new Button("Feedback");
            feedbackButton.getStyleClass().add("btn-feedback");

            likeButton = new Button("👍");
            dislikeButton = new Button("👎");
            likeButton.getStyleClass().add("btn-like");
            dislikeButton.getStyleClass().add("btn-dislike");
            likeButton.setDisable(true);
            dislikeButton.setDisable(true);
            likeButton.setFocusTraversable(false);
            dislikeButton.setFocusTraversable(false);

            exportCsvButton = new Button("Donnees CSV");
            exportPdfButton = new Button("Rapport PDF (stats image)");
            exportCsvButton.getStyleClass().add("btn-outline");
            exportPdfButton.getStyleClass().add("btn-outline");
            exportCsvButton.getStyleClass().add("btn-export-csv");
            exportPdfButton.getStyleClass().add("btn-export-pdf");

            likeCountLabel = new Label("0");
            dislikeCountLabel = new Label("0");
            likeCountLabel.getStyleClass().addAll("count-badge", "count-like");
            dislikeCountLabel.getStyleClass().addAll("count-badge", "count-dislike");

            feedbackService = new FeedbackService();
            setupInteractiveEffects();

            footerBox.getChildren().addAll(
                    dateLabel, spacer2,
                    feedbackButton, exportPdfButton, exportCsvButton,
                    likeButton, likeCountLabel, dislikeButton, dislikeCountLabel,
                    updateButton, deleteButton
            );

            container.getChildren().addAll(headerBox, new Separator(), bodyBox, footerBox);
        }

        @Override
        protected void updateItem(Publication pub, boolean empty) {
            super.updateItem(pub, empty);

            if (empty || pub == null) {
                setGraphic(null);
            } else {
                idLabel.setText("#" + pub.getIdPublication());
                titreLabel.setText(pub.getTitre());
                contenuLabel.setText(pub.getContenu());

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                dateLabel.setText("📅 Créé le: " + pub.getDatePublication().format(formatter));

                updateButton.setOnAction(e -> handleUpdate(pub));
                deleteButton.setOnAction(e -> handleDelete(pub));

                // Initialize counts
                updateReactionCounts(pub);

                // Feedback dialog
                feedbackButton.setOnAction(e -> {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Publication/FeedbackUser.fxml"));
                        javafx.scene.Parent root = loader.load();
                        FeedbackUserController controller = loader.getController();
                        controller.setPublication(pub);

                        Stage stage = new Stage();
                        stage.setTitle("Feedbacks — " + pub.getTitre());
                        stage.setScene(new Scene(root));
                        stage.initOwner(publicationListView.getScene().getWindow());
                        stage.show();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                });

                exportCsvButton.setOnAction(e -> exportFeedbackCsv(pub));
                exportPdfButton.setOnAction(e -> exportFeedbackPdf(pub));
                animateCardEntry();

                setGraphic(container);
            }
        }

        private void setupInteractiveEffects() {
            container.setOnMouseEntered(e -> animateHover(1.012, -3));
            container.setOnMouseExited(e -> animateHover(1.0, 0));
        }

        private void animateHover(double scale, double y) {
            ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(140), container);
            scaleTransition.setToX(scale);
            scaleTransition.setToY(scale);
            scaleTransition.play();

            TranslateTransition translateTransition = new TranslateTransition(Duration.millis(140), container);
            translateTransition.setToY(y);
            translateTransition.play();
        }

        private void animateCardEntry() {
            container.setOpacity(0);
            FadeTransition fadeTransition = new FadeTransition(Duration.millis(240), container);
            fadeTransition.setFromValue(0);
            fadeTransition.setToValue(1);
            fadeTransition.play();
        }

        private void exportFeedbackCsv(Publication pub) {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Exporter feedbacks CSV");
            chooser.setInitialFileName("feedback_pub_" + pub.getIdPublication() + ".csv");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));

            java.io.File selectedFile = chooser.showSaveDialog(publicationListView.getScene().getWindow());
            if (selectedFile == null) return;
            java.io.File file = ensureExtension(selectedFile, ".csv");

            boolean ok = feedbackService.exportFeedbacksToCSV(pub.getIdPublication(), file);
            if (ok) {
                showSuccessAlert("Export CSV réussi", "Fichier généré: " + file.getName());
            } else {
                showErrorAlert("Export échoué", "Impossible d'exporter le CSV.");
            }
        }

        private void exportFeedbackPdf(Publication pub) {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Exporter rapport PDF");
            chooser.setInitialFileName("rapport_satisfaction_pub_" + pub.getIdPublication() + ".pdf");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));

            java.io.File selectedFile = chooser.showSaveDialog(publicationListView.getScene().getWindow());
            if (selectedFile == null) return;
            java.io.File file = ensureExtension(selectedFile, ".pdf");

            boolean ok = feedbackService.exportPublicationReportToPDF(pub.getIdPublication(), pub.getTitre(), file);
            if (ok) {
                showSuccessAlert("Export PDF réussi", "Rapport statistique image généré: " + file.getName());
            } else {
                showErrorAlert("Export échoué", "Impossible d'exporter le PDF.");
            }
        }

        private java.io.File ensureExtension(java.io.File file, String extension) {
            String name = file.getName().toLowerCase();
            if (name.endsWith(extension)) {
                return file;
            }
            return new java.io.File(file.getParentFile(), file.getName() + extension);
        }

        private void updateReactionCounts(Publication pub) {
            try {
                int likes = feedbackService.countLikes(pub.getIdPublication());
                int dislikes = feedbackService.countDislikes(pub.getIdPublication());
                likeCountLabel.setText(String.valueOf(likes));
                dislikeCountLabel.setText(String.valueOf(dislikes));
            } catch (Exception ex) {
                likeCountLabel.setText("0");
                dislikeCountLabel.setText("0");
            }
        }
    }


}
