package org.example.Controlleurs.PublicationControlleur;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.example.Model.Publication.FeedbackStats;
import org.example.Model.Publication.FeedbackTrendPoint;
import org.example.Model.Publication.GlobalFeedbackStats;
import org.example.Service.PublicationService.FeedbackService;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class FeedbackStatsController implements Initializable {

    @FXML private Label totalLikesLabel;
    @FXML private Label totalDislikesLabel;
    @FXML private Label likeRatioLabel;
    @FXML private Label avgRatingLabel;
    @FXML private Label totalCommentsLabel;
    @FXML private Label topPublicationLabel;

    @FXML private TextField searchField;
    @FXML private TableView<FeedbackStats> statsTable;
    @FXML private TableColumn<FeedbackStats, Number> idCol;
    @FXML private TableColumn<FeedbackStats, String> titleCol;
    @FXML private TableColumn<FeedbackStats, Number> likesCol;
    @FXML private TableColumn<FeedbackStats, Number> dislikesCol;
    @FXML private TableColumn<FeedbackStats, Number> ratioCol;
    @FXML private TableColumn<FeedbackStats, Number> avgRatingCol;
    @FXML private TableColumn<FeedbackStats, Number> ratingCountCol;
    @FXML private TableColumn<FeedbackStats, Number> commentsCol;
    @FXML private TableColumn<FeedbackStats, Number> scoreCol;

    @FXML private BarChart<String, Number> ratingDistributionChart;
    @FXML private LineChart<String, Number> trendChart;

    private final FeedbackService feedbackService = new FeedbackService();
    private final ObservableList<FeedbackStats> statsData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initTable();
        loadGlobalStats();
        loadPublicationStats();
        loadTrend();
        loadRatingDistribution(null);
    }

    private void initTable() {
        idCol.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getPublicationId()));
        titleCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getTitre()));
        likesCol.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getLikes()));
        dislikesCol.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getDislikes()));
        ratioCol.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(round(cell.getValue().getRatioLike() * 100.0)));
        avgRatingCol.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(round(cell.getValue().getAvgRating())));
        ratingCountCol.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getRatingCount()));
        commentsCol.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getComments()));
        scoreCol.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(round(cell.getValue().getScoreGlobal())));

        FilteredList<FeedbackStats> filtered = new FilteredList<>(statsData, p -> true);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String query = newVal == null ? "" : newVal.trim().toLowerCase(Locale.ROOT);
            filtered.setPredicate(item -> query.isEmpty() || item.getTitre().toLowerCase(Locale.ROOT).contains(query));
        });

        SortedList<FeedbackStats> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(statsTable.comparatorProperty());
        statsTable.setItems(sorted);

        statsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selected) -> {
            if (selected == null) {
                loadRatingDistribution(null);
                return;
            }
            loadRatingDistribution(selected.getPublicationId());
        });
    }

    private void loadGlobalStats() {
        try {
            GlobalFeedbackStats global = feedbackService.getGlobalStats();
            totalLikesLabel.setText(String.valueOf(global.getTotalLikes()));
            totalDislikesLabel.setText(String.valueOf(global.getTotalDislikes()));
            likeRatioLabel.setText(String.format(Locale.US, "%.1f%%", global.getLikeRatioPercent()));
            avgRatingLabel.setText(String.format(Locale.US, "%.2f/5", global.getAvgRating()));
            totalCommentsLabel.setText(String.valueOf(global.getTotalComments()));
        } catch (Exception ex) {
            showError("Erreur chargement stats globales", ex);
        }
    }

    private void loadPublicationStats() {
        try {
            List<FeedbackStats> rows = feedbackService.getStatsByPublication();
            statsData.setAll(rows);

            FeedbackStats topLiked = rows.stream().max(java.util.Comparator.comparingInt(FeedbackStats::getLikes)).orElse(null);
            FeedbackStats topRated = rows.stream().max(java.util.Comparator.comparingDouble(FeedbackStats::getAvgRating)).orElse(null);
            FeedbackStats topCommented = rows.stream().max(java.util.Comparator.comparingInt(FeedbackStats::getComments)).orElse(null);

            String likedText = topLiked == null ? "-" : topLiked.getTitre() + " (" + topLiked.getLikes() + " likes)";
            String ratedText = topRated == null ? "-" : topRated.getTitre() + " (" + round(topRated.getAvgRating()) + "/5)";
            String commentText = topCommented == null ? "-" : topCommented.getTitre() + " (" + topCommented.getComments() + " commentaires)";

            topPublicationLabel.setText("Like: " + likedText + " | Note: " + ratedText + " | Commentaires: " + commentText);
        } catch (Exception ex) {
            showError("Erreur chargement stats par publication", ex);
        }
    }

    private void loadRatingDistribution(Integer publicationId) {
        try {
            Map<Integer, Integer> distribution = feedbackService.getRatingDistribution(publicationId);

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(publicationId == null ? "Toutes publications" : "Publication #" + publicationId);
            for (int i = 1; i <= 5; i++) {
                series.getData().add(new XYChart.Data<>(i + "★", distribution.getOrDefault(i, 0)));
            }

            ratingDistributionChart.getData().setAll(series);
        } catch (Exception ex) {
            showError("Erreur chargement distribution des notes", ex);
        }
    }

    private void loadTrend() {
        try {
            List<FeedbackTrendPoint> points = feedbackService.getFeedbackTrendLast7Days();
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM");

            XYChart.Series<String, Number> likesSeries = new XYChart.Series<>();
            likesSeries.setName("Likes");

            XYChart.Series<String, Number> dislikesSeries = new XYChart.Series<>();
            dislikesSeries.setName("Dislikes");

            XYChart.Series<String, Number> commentsSeries = new XYChart.Series<>();
            commentsSeries.setName("Commentaires");

            for (FeedbackTrendPoint p : points) {
                String day = p.getDay().format(fmt);
                likesSeries.getData().add(new XYChart.Data<>(day, p.getLikes()));
                dislikesSeries.getData().add(new XYChart.Data<>(day, p.getDislikes()));
                commentsSeries.getData().add(new XYChart.Data<>(day, p.getComments()));
            }

            trendChart.getData().setAll(likesSeries, dislikesSeries, commentsSeries);
        } catch (Exception ex) {
            showError("Erreur chargement tendance feedback", ex);
        }
    }

    @FXML
    private void refresh() {
        loadGlobalStats();
        loadPublicationStats();
        loadTrend();
        FeedbackStats selected = statsTable.getSelectionModel().getSelectedItem();
        loadRatingDistribution(selected == null ? null : selected.getPublicationId());
    }

    @FXML
    private void goBack() {
        try {
            javafx.stage.Stage stage = (javafx.stage.Stage) statsTable.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            showError("Erreur fermeture", e);
        }
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private void showError(String title, Exception ex) {
        ex.printStackTrace();
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(ex.getMessage() == null ? "Erreur inconnue" : ex.getMessage());
        alert.showAndWait();
    }
}
