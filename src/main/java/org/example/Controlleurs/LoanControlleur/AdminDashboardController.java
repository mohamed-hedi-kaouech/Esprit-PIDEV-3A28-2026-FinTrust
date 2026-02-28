package org.example.Controlleurs.LoanControlleur;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.Model.Loan.LoanClass.Loan;
import org.example.Model.Loan.LoanEnum.LoanStatus;
import org.example.Service.LoanService.LoanService;
import org.example.Service.LoanService.RiskService;
import org.example.Service.LoanService.AiInsightService;

import java.util.List;

public class AdminDashboardController {

    @FXML private Label totalLoansLabel;
    @FXML private Label activeLoansLabel;
    @FXML private Label pendingLoansLabel;
    @FXML private Label completedLoansLabel;

    @FXML private PieChart statusPieChart;
    @FXML private BarChart<String, Number> riskBarChart;
    @FXML private Label aiMessageLabel;

    private final LoanService loanService = new LoanService();
    private final RiskService riskService = new RiskService();
    private final AiInsightService aiService = new AiInsightService();

    @FXML
    public void initialize() {
        loadStatistics();
        loadCharts();
    }

    private void loadStatistics() {

        List<Loan> loans = loanService.ReadAll();

        int total = loans.size();
        int active = 0;
        int pending = 0;
        int completed = 0;

        for (Loan loan : loans) {
            if (loan.getStatus() == LoanStatus.ACTIVE) active++;
            if (loan.getStatus() == LoanStatus.PENDING) pending++;
            if (loan.getStatus() == LoanStatus.COMPLETED) completed++;
        }

        totalLoansLabel.setText(String.valueOf(total));
        activeLoansLabel.setText(String.valueOf(active));
        pendingLoansLabel.setText(String.valueOf(pending));
        completedLoansLabel.setText(String.valueOf(completed));
    }

    private void loadCharts() {

        List<Loan> loans = loanService.ReadAll();

        int active = 0, pending = 0, completed = 0;
        int lowRisk = 0, mediumRisk = 0, highRisk = 0;

        for (Loan loan : loans) {

            switch (loan.getStatus()) {
                case ACTIVE -> active++;
                case PENDING -> pending++;
                case COMPLETED -> completed++;
            }

            int score = riskService.calculateRiskScore(loan);
            String level = riskService.classifyRisk(score);

            switch (level) {
                case "LOW" -> lowRisk++;
                case "MEDIUM" -> mediumRisk++;
                case "HIGH" -> highRisk++;
            }
        }

        // PIE CHART
        statusPieChart.getData().clear();
        statusPieChart.setLegendVisible(false);
        statusPieChart.getData().addAll(
                new PieChart.Data("Active", active),
                new PieChart.Data("Pending", pending),
                new PieChart.Data("Completed", completed)
        );

        // BAR CHART
        riskBarChart.getData().clear();
        riskBarChart.setLegendVisible(false);
        riskBarChart.setCategoryGap(40);
        riskBarChart.setBarGap(5);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("Low", lowRisk));
        series.getData().add(new XYChart.Data<>("Medium", mediumRisk));
        series.getData().add(new XYChart.Data<>("High", highRisk));
        riskBarChart.getData().add(series);

        int max = Math.max(lowRisk, Math.max(mediumRisk, highRisk));
        NumberAxis yAxis = (NumberAxis) riskBarChart.getYAxis();
        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(max + 1);
        yAxis.setTickUnit(1);

        generateAiInsight(active, pending, completed,
                lowRisk, mediumRisk, highRisk);
    }

    private void generateAiInsight(int active,
                                   int pending,
                                   int completed,
                                   int lowRisk,
                                   int mediumRisk,
                                   int highRisk) {

        try {

            String prompt = """
                You are an internal risk monitoring assistant for a loan management system.
                
                Here is the current ACTIVE loan portfolio data:
                
                Active Loans: %d
                Low Risk: %d
                Medium Risk: %d
                High Risk: %d
                
                Your task:
                
                1) Clearly state if the current active portfolio is LOW, MODERATE, or HIGH overall risk.
                2) Explain in one sentence why.
                3) If HIGH risk loans are more than 50%% of active loans, clearly flag this as CRITICAL.
                4) Provide ONE practical administrative action (not generic advice).
                
                Keep response under 6 lines.
                No paragraphs.
                No theory.
                Be direct and executive-style.
                """.formatted(active, lowRisk, mediumRisk, highRisk);

            aiMessageLabel.setOpacity(0);
            aiMessageLabel.setText("Generating AI insight...");

            String insight = aiService.generateInsight(prompt);

            showAnimatedMessage(cleanInsight(insight));

        } catch (Exception e) {
            aiMessageLabel.setText("AI insight unavailable.");
        }
    }

    private void showAnimatedMessage(String message) {

        aiMessageLabel.setText(message);

        FadeTransition fade = new FadeTransition(Duration.seconds(0.8), aiMessageLabel);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();

        TranslateTransition slide = new TranslateTransition(Duration.seconds(0.5), aiMessageLabel);
        slide.setFromY(15);
        slide.setToY(0);
        slide.play();
    }
    private String cleanInsight(String s) {
        if (s == null) return "";
        return s.replace("###", "")
                .replace("**", "")
                .replace("- ", "• ")
                .trim();
    }

    @FXML
    private void goToLoanList() throws Exception {
        Parent root = FXMLLoader.load(
                getClass().getResource("/Loan/AdminLoanList.fxml"));
        Stage stage = (Stage) totalLoansLabel.getScene().getWindow();
        stage.setScene(new Scene(root));
    }

    @FXML
    private void handleLogout() throws Exception {
        Parent root = FXMLLoader.load(
                getClass().getResource("/MenuGUI.fxml"));
        Stage stage = (Stage) totalLoansLabel.getScene().getWindow();
        stage.setScene(new Scene(root));
    }
}