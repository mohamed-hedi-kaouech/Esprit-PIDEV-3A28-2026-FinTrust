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
import org.example.Service.AnalyticsService.GamificationBadgeStat;
import org.example.Service.AnalyticsService.GamificationChallengeItem;
import org.example.Service.AnalyticsService.GamificationLeaderboardItem;
import org.example.Service.AnalyticsService.GamificationService;
import org.example.Service.AnalyticsService.GamificationSnapshot;
import org.example.Service.AnalyticsService.HeatmapPoint;
import org.example.Service.AnalyticsService.OtpAnalyticsSnapshot;
import org.example.Service.AnalyticsService.UserScoreService;
import org.example.Service.AnalyticsService.UserSegmentType;
import org.example.Utils.SessionContext;

import java.net.URL;
import java.util.HashMap;
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
    @FXML private Label totalPointsLabel;
    @FXML private Label totalBadgesLabel;
    @FXML private Label challengesDoneLabel;
    @FXML private Label challengesPendingLabel;
    @FXML private Label bronzeLevelLabel;
    @FXML private Label silverLevelLabel;
    @FXML private Label goldLevelLabel;
    @FXML private Label platinumLevelLabel;

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
    @FXML private TableView<GamificationLeaderboardItem> leaderboardTable;
    @FXML private TableColumn<GamificationLeaderboardItem, Integer> leaderboardRankCol;
    @FXML private TableColumn<GamificationLeaderboardItem, Integer> leaderboardUserIdCol;
    @FXML private TableColumn<GamificationLeaderboardItem, String> leaderboardEmailCol;
    @FXML private TableColumn<GamificationLeaderboardItem, Integer> leaderboardPointsCol;
    @FXML private TableColumn<GamificationLeaderboardItem, String> leaderboardLevelCol;
    @FXML private TableColumn<GamificationLeaderboardItem, Integer> leaderboardBadgesCol;

    @FXML private TableView<GamificationChallengeItem> challengeTable;
    @FXML private TableColumn<GamificationChallengeItem, Integer> challengeUserIdCol;
    @FXML private TableColumn<GamificationChallengeItem, String> challengeEmailCol;
    @FXML private TableColumn<GamificationChallengeItem, String> challengeTitleCol;
    @FXML private TableColumn<GamificationChallengeItem, String> challengeStatusCol;
    @FXML private TableColumn<GamificationChallengeItem, Integer> challengeProgressCol;

    @FXML private TableView<GamificationBadgeStat> badgeTable;
    @FXML private TableColumn<GamificationBadgeStat, String> badgeCodeCol;
    @FXML private TableColumn<GamificationBadgeStat, String> badgeLabelCol;
    @FXML private TableColumn<GamificationBadgeStat, Integer> badgeHoldersCol;

    @FXML private Label infoLabel;

    private final AnalyticsService analyticsService = new AnalyticsService();
    private final UserScoreService userScoreService = new UserScoreService();
    private final GamificationService gamificationService = new GamificationService();

    @FXML
    private void initialize() {
        bindTables();
        bindGamificationTables();
        refreshAnalytics();
    }

    @FXML
    private void handleRefresh() {
        refreshAnalytics();
    }

    @FXML
    private void handleBack() {
        goToDashboard();
    }

    @FXML
    private void goToDashboard() {
        navigateTo("/Admin/UserDashboard.fxml", "Dashboard Admin", "/Styles/StyleWallet.css");
    }

    @FXML
    private void goToAnalyticsDashboard() {
        handleRefresh();
    }

    @FXML
    private void goToAdminTasks() {
        navigateTo("/Admin/AdminTasks.fxml", "Admin Productivity / Ops", "/Styles/StyleWallet.css");
    }

    @FXML
    private void goToKycValidation() {
        navigateTo("/Admin/KycValidation.fxml", "Validation KYC", "/Styles/StyleWallet.css");
    }

    @FXML
    private void goToCreateUserForm() {
        navigateTo("/Admin/UserCreate.fxml", "Creation Utilisateur", "/Styles/StyleWallet.css");
    }

    @FXML
    private void goToMenu() {
        navigateTo("/MenuGUI.fxml", "Menu Principal", "/Styles/MenuStyle.css");
    }

    @FXML
    private void handleLogout() {
        SessionContext.getInstance().logout();
        navigateTo("/Auth/Login.fxml", "Connexion", "/Styles/StyleWallet.css");
    }

    @FXML
    private void goToWalletDashboard() {
        navigateTo("/Wallet/dashboard.fxml", "Wallet", "/Styles/StyleWallet.css");
    }

    @FXML
    private void goToProducts() {
        navigateTo("/Product/ListeProductGUI.fxml", "Produits", "/Styles/StyleWallet.css");
    }

    @FXML
    private void goToPublications() {
        infoLabel.setText("Module Publications: ouvrez depuis le menu principal.");
        infoLabel.setStyle("-fx-text-fill: #1d6b34;");
        goToMenu();
    }

    @FXML
    private void goToBudget() {
        infoLabel.setText("Module Budget: ouvrez depuis le menu principal.");
        infoLabel.setStyle("-fx-text-fill: #1d6b34;");
        goToMenu();
    }

    @FXML
    private void goToLoans() {
        infoLabel.setText("Module Loans: ouvrez depuis le menu principal.");
        infoLabel.setStyle("-fx-text-fill: #1d6b34;");
        goToMenu();
    }

    private void bindTables() {
        churnUserIdCol.setCellValueFactory(d -> new javafx.beans.property.SimpleObjectProperty<>(d.getValue().userId()));
        churnEmailCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().email()));
        churnDaysCol.setCellValueFactory(d -> new javafx.beans.property.SimpleObjectProperty<>(d.getValue().inactiveDays()));

        failedUserIdCol.setCellValueFactory(d -> new javafx.beans.property.SimpleObjectProperty<>(d.getValue().userId()));
        failedEmailCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().email()));
        failedCountCol.setCellValueFactory(d -> new javafx.beans.property.SimpleObjectProperty<>(d.getValue().failedLogins30Days()));
    }

    private void bindGamificationTables() {
        leaderboardRankCol.setCellValueFactory(d -> new javafx.beans.property.SimpleObjectProperty<>(d.getValue().rank()));
        leaderboardUserIdCol.setCellValueFactory(d -> new javafx.beans.property.SimpleObjectProperty<>(d.getValue().userId()));
        leaderboardEmailCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().email()));
        leaderboardPointsCol.setCellValueFactory(d -> new javafx.beans.property.SimpleObjectProperty<>(d.getValue().points()));
        leaderboardLevelCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().level()));
        leaderboardBadgesCol.setCellValueFactory(d -> new javafx.beans.property.SimpleObjectProperty<>(d.getValue().badgesCount()));

        challengeUserIdCol.setCellValueFactory(d -> new javafx.beans.property.SimpleObjectProperty<>(d.getValue().userId()));
        challengeEmailCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().email()));
        challengeTitleCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().challengeTitle()));
        challengeStatusCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().status()));
        challengeProgressCol.setCellValueFactory(d -> new javafx.beans.property.SimpleObjectProperty<>(d.getValue().progress()));

        badgeCodeCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().badgeCode()));
        badgeLabelCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().badgeLabel()));
        badgeHoldersCol.setCellValueFactory(d -> new javafx.beans.property.SimpleObjectProperty<>(d.getValue().holders()));
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
            Map<String, Integer> byHour = new HashMap<>();
            for (HeatmapPoint p : analyticsService.getLoginHeatmapByHour()) {
                byHour.put(p.bucket(), p.count());
            }
            for (int h = 0; h < 24; h++) {
                String bucket = String.format("%02d:00", h);
                series.getData().add(new XYChart.Data<>(bucket, byHour.getOrDefault(bucket, 0)));
            }
            loginHourBarChart.getData().setAll(series);

            GamificationSnapshot g = gamificationService.refreshAndGetSnapshot();
            totalPointsLabel.setText(String.valueOf(g.totalPointsDistributed()));
            totalBadgesLabel.setText(String.valueOf(g.totalBadges()));
            challengesDoneLabel.setText(String.valueOf(g.challengesCompleted()));
            challengesPendingLabel.setText(String.valueOf(g.challengesPending()));

            bronzeLevelLabel.setText(String.valueOf(g.bronzeCount()));
            silverLevelLabel.setText(String.valueOf(g.silverCount()));
            goldLevelLabel.setText(String.valueOf(g.goldCount()));
            platinumLevelLabel.setText(String.valueOf(g.platinumCount()));

            leaderboardTable.getItems().setAll(g.leaderboard());
            challengeTable.getItems().setAll(g.challenges());
            badgeTable.getItems().setAll(g.badgeStats());

            int highScore = userScoreService.computeHealthScores().size();
            infoLabel.setText("Analytics + Gamification mis a jour. Scores utilisateur calcules: " + highScore);
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

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
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