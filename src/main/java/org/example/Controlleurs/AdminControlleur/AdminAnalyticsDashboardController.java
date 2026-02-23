package org.example.Controlleurs.AdminControlleur;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import org.example.Service.AnalyticsService.AnalyticsService;
import org.example.Service.AnalyticsService.ChurnRiskItem;
import org.example.Service.AnalyticsService.FailedLoginUser;
import org.example.Service.AnalyticsService.HeatmapPoint;
import org.example.Service.AnalyticsService.OtpAnalyticsSnapshot;
import org.example.Service.AnalyticsService.UserScoreService;
import org.example.Service.AnalyticsService.UserSegmentType;

import java.net.URL;
import java.util.List;
import java.util.Map;

public class AdminAnalyticsDashboardController {

    @FXML private Label activeUsersLabel;
    @FXML private Label dormantUsersLabel;
    @FXML private Label atRiskUsersLabel;
    @FXML private Label veryActiveUsersLabel;
    @FXML private Label churnCountLabel;
    @FXML private Label otpRequestRateLabel;
    @FXML private Label otpValidationRateLabel;
    @FXML private Label avgOtpTimeLabel;

    @FXML private PieChart segmentPieChart;
    @FXML private BarChart<String, Number> loginHourBarChart;

    @FXML private TableView<ChurnRiskItem> churnRiskTable;
    @FXML private TableColumn<ChurnRiskItem, Integer> churnUserIdCol;
    @FXML private TableColumn<ChurnRiskItem, String> churnEmailCol;
    @FXML private TableColumn<ChurnRiskItem, Integer> churnDaysCol;

    @FXML private TableView<FailedLoginUser> failedLoginTable;
    @FXML private TableColumn<FailedLoginUser, Integer> failedUserIdCol;
    @FXML private TableColumn<FailedLoginUser, String> failedEmailCol;
    @FXML private TableColumn<FailedLoginUser, Integer> failedCountCol;

    @FXML private Label infoLabel;

    private final AnalyticsService analyticsService = new AnalyticsService();
    private final UserScoreService userScoreService = new UserScoreService();

    @FXML
    private void initialize() {
        bindTables();
        refreshAnalytics();
    }

    @FXML
    private void handleRefresh() {
        refreshAnalytics();
    }

    @FXML
    private void handleBack() {
        navigateTo("/Admin/UserDashboard.fxml", "Dashboard Admin", "/Styles/StyleWallet.css");
    }

    private void bindTables() {
        churnUserIdCol.setCellValueFactory(d -> new javafx.beans.property.SimpleObjectProperty<>(d.getValue().userId()));
        churnEmailCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().email()));
        churnDaysCol.setCellValueFactory(d -> new javafx.beans.property.SimpleObjectProperty<>(d.getValue().inactiveDays()));

        failedUserIdCol.setCellValueFactory(d -> new javafx.beans.property.SimpleObjectProperty<>(d.getValue().userId()));
        failedEmailCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().email()));
        failedCountCol.setCellValueFactory(d -> new javafx.beans.property.SimpleObjectProperty<>(d.getValue().failedLogins30Days()));
    }

    private void refreshAnalytics() {
        try {
            Map<UserSegmentType, Integer> segments = analyticsService.getSegmentCounters();
            int active = segments.getOrDefault(UserSegmentType.ACTIVE, 0);
            int dormant = segments.getOrDefault(UserSegmentType.DORMANT, 0);
            int atRisk = segments.getOrDefault(UserSegmentType.AT_RISK, 0);
            int veryActive = segments.getOrDefault(UserSegmentType.VERY_ACTIVE, 0);

            activeUsersLabel.setText(String.valueOf(active));
            dormantUsersLabel.setText(String.valueOf(dormant));
            atRiskUsersLabel.setText(String.valueOf(atRisk));
            veryActiveUsersLabel.setText(String.valueOf(veryActive));

            segmentPieChart.setData(FXCollections.observableArrayList(
                    new PieChart.Data("Actifs", active),
                    new PieChart.Data("Dormants", dormant),
                    new PieChart.Data("A risque", atRisk),
                    new PieChart.Data("Tres actifs", veryActive)
            ));

            OtpAnalyticsSnapshot otp = analyticsService.getOtpAnalytics();
            otpRequestRateLabel.setText(String.format("%.1f %%", otp.requestSuccessRate()));
            otpValidationRateLabel.setText(String.format("%.1f %%", otp.validationSuccessRate()));
            avgOtpTimeLabel.setText(String.format("%.1f s", otp.averageValidationSeconds()));

            List<ChurnRiskItem> churnRisk = analyticsService.getChurnRisk();
            churnCountLabel.setText(String.valueOf(churnRisk.size()));
            churnRiskTable.getItems().setAll(churnRisk);

            failedLoginTable.getItems().setAll(analyticsService.getTopFailedLogins(10));

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Logins / heure");
            for (HeatmapPoint p : analyticsService.getLoginHeatmapByHour()) {
                series.getData().add(new XYChart.Data<>(p.bucket(), p.count()));
            }
            loginHourBarChart.getData().setAll(series);

            int highScore = userScoreService.computeHealthScores().size();
            infoLabel.setText("Analytics mis a jour. Scores utilisateur calcules: " + highScore);
            infoLabel.setStyle("-fx-text-fill: #1d6b34;");
        } catch (Exception e) {
            infoLabel.setText("Erreur analytics: " + e.getMessage());
            infoLabel.setStyle("-fx-text-fill: #cc2e2e;");
        }
    }

    private void navigateTo(String fxmlPath, String title, String stylesheetPath) {
        try {
            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                if (infoLabel != null) {
                    infoLabel.setText("FXML introuvable: " + fxmlPath);
                    infoLabel.setStyle("-fx-text-fill: #cc2e2e;");
                }
                return;
            }

            Parent root = FXMLLoader.load(fxmlUrl);
            Scene scene = new Scene(root);
            if (stylesheetPath != null && !stylesheetPath.isBlank()) {
                URL cssUrl = getClass().getResource(stylesheetPath);
                if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            Stage stage = (Stage) infoLabel.getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            if (infoLabel != null) {
                infoLabel.setText("Erreur navigation: " + e.getMessage());
                infoLabel.setStyle("-fx-text-fill: #cc2e2e;");
            }
        }
    }
}
