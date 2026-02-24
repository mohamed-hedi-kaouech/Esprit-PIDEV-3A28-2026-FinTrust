package org.example.Controlleurs.ClientControlleur;

import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.Model.User.User;
import org.example.Service.AnalyticsService.ClientGamificationSnapshot;
import org.example.Service.AnalyticsService.GamificationService;
import org.example.Service.GameService.GameService;
import org.example.Service.GameService.GameSessionResult;
import org.example.Service.GameService.GameSessionStart;
import org.example.Utils.SessionContext;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MemoryGameController {

    @FXML private Label contextLabel;
    @FXML private Label timerLabel;
    @FXML private Label scoreLabel;
    @FXML private Label infoLabel;
    @FXML private GridPane boardGrid;

    private final SessionContext session = SessionContext.getInstance();
    private final GameService gameService = new GameService();
    private final GamificationService gamificationService = new GamificationService();
    private final List<String> symbols = new ArrayList<>();
    private final List<Button> cardButtons = new ArrayList<>();

    private int remainingSeconds = 60;
    private int score = 0;
    private int moves = 0;
    private int matchedPairs = 0;
    private int firstIndex = -1;
    private int secondIndex = -1;
    private long startedAtMillis;
    private String sessionId;
    private String initialMedalLabel = "Niveau Starter";
    private Timeline timeline;
    private boolean locked = false;

    @FXML
    private void initialize() {
        User user = session.getCurrentUser();
        if (user == null) {
            navigateTo("/Auth/Login.fxml", "Connexion");
            return;
        }

        String context = session.getSmartBreakContext();
        contextLabel.setText("Contexte: " + context);
        GameSessionStart start = gameService.startSession(user.getId(), context);
        sessionId = start.sessionId();
        initialMedalLabel = readCurrentMedal(user.getId());

        initBoard();
        startTimer();
        startedAtMillis = System.currentTimeMillis();
        updateScoreLabel();
    }

    private void initBoard() {
        symbols.clear();
        symbols.add("A"); symbols.add("A");
        symbols.add("B"); symbols.add("B");
        symbols.add("C"); symbols.add("C");
        symbols.add("D"); symbols.add("D");
        Collections.shuffle(symbols);

        boardGrid.getChildren().clear();
        cardButtons.clear();
        for (int i = 0; i < symbols.size(); i++) {
            Button card = new Button("?");
            card.setPrefSize(90, 74);
            card.getStyleClass().add("memory-card");
            final int idx = i;
            card.setOnAction(e -> onCardClick(idx));
            boardGrid.add(card, i % 4, i / 4);
            cardButtons.add(card);
        }
    }

    private void startTimer() {
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            remainingSeconds--;
            timerLabel.setText(remainingSeconds + "s");
            if (remainingSeconds <= 0) {
                finishGame(false);
            }
        }));
        timeline.setCycleCount(60);
        timeline.play();
    }

    private void onCardClick(int index) {
        if (locked || index == firstIndex || index == secondIndex) return;
        Button card = cardButtons.get(index);
        if (card.isDisabled()) return;

        card.setText(symbols.get(index));
        if (firstIndex == -1) {
            firstIndex = index;
            return;
        }
        secondIndex = index;
        moves++;
        locked = true;

        String a = symbols.get(firstIndex);
        String b = symbols.get(secondIndex);
        if (a.equals(b)) {
            cardButtons.get(firstIndex).setDisable(true);
            cardButtons.get(secondIndex).setDisable(true);
            cardButtons.get(firstIndex).setStyle("-fx-background-color: #dbf7e6; -fx-text-fill: #0f5132;");
            cardButtons.get(secondIndex).setStyle("-fx-background-color: #dbf7e6; -fx-text-fill: #0f5132;");
            score += 10;
            matchedPairs++;
            resetTurn();
            if (matchedPairs == symbols.size() / 2) {
                finishGame(true);
            }
        } else {
            score = Math.max(0, score - 2);
            PauseTransition pause = new PauseTransition(Duration.millis(420));
            pause.setOnFinished(e -> {
                cardButtons.get(firstIndex).setText("?");
                cardButtons.get(secondIndex).setText("?");
                resetTurn();
            });
            pause.play();
        }
        updateScoreLabel();
    }

    private void resetTurn() {
        firstIndex = -1;
        secondIndex = -1;
        locked = false;
    }

    private void updateScoreLabel() {
        scoreLabel.setText(String.valueOf(score));
    }

    private void finishGame(boolean completed) {
        if (timeline != null) timeline.stop();
        for (Button b : cardButtons) b.setDisable(true);
        long duration = System.currentTimeMillis() - startedAtMillis;
        if (completed && remainingSeconds > 0) {
            score += 10;
            updateScoreLabel();
        }
        User user = session.getCurrentUser();
        GameSessionResult result = gameService.endSession(
                user.getId(),
                sessionId,
                score,
                duration,
                moves
        );
        String status = completed ? "Partie terminee" : "Temps ecoule";
        StringBuilder message = new StringBuilder(status + " | Score: " + score + " | " + result.message());
        String newMedal = readCurrentMedal(user.getId());
        if (newMedal != null && !newMedal.equalsIgnoreCase(initialMedalLabel)) {
            message.append(" | Nouveau badge: ").append(newMedal);
            initialMedalLabel = newMedal;
        }
        infoLabel.setText(message.toString());
        infoLabel.setStyle("-fx-text-fill: #0f4e96; -fx-font-weight: 700;");
    }

    private String readCurrentMedal(int userId) {
        try {
            ClientGamificationSnapshot snapshot = gamificationService.getClientSnapshot(userId);
            return snapshot == null ? "Niveau Starter" : snapshot.medalLabel();
        } catch (Exception ignored) {
            return "Niveau Starter";
        }
    }

    @FXML
    private void handleBack() {
        User user = session.getCurrentUser();
        if (user != null && sessionId != null) {
            gameService.cancelSession(user.getId(), sessionId);
        }
        navigateTo("/Client/SmartBreakHub.fxml", "Pause Intelligente");
    }

    private void navigateTo(String fxmlPath, String title) {
        try {
            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) return;
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            URL cssUrl = getClass().getResource("/Styles/StyleWallet.css");
            if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());
            Stage stage = (Stage) boardGrid.getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            infoLabel.setText("Navigation impossible: " + e.getMessage());
            infoLabel.setStyle("-fx-text-fill: #b91c1c;");
        }
    }
}

