package org.example.Controlleurs.ClientControlleur;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import org.example.Model.User.User;
import org.example.Service.AnalyticsService.ClientGamificationSnapshot;
import org.example.Service.AnalyticsService.GamificationService;
import org.example.Service.GameService.GameService;
import org.example.Service.GameService.GameSessionResult;
import org.example.Service.GameService.GameSessionStart;
import org.example.Utils.SessionContext;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class DinoGameController {

    @FXML private Label contextLabel;
    @FXML private Label timerLabel;
    @FXML private Label scoreLabel;
    @FXML private Label jumpsLabel;
    @FXML private Label infoLabel;
    @FXML private Pane gamePane;
    @FXML private Region dinoNode;
    @FXML private Button retryButton;

    private static final double GROUND_Y = 190.0;
    private static final int GAME_SECONDS = 60;

    private final SessionContext session = SessionContext.getInstance();
    private final GameService gameService = new GameService();
    private final GamificationService gamificationService = new GamificationService();
    private final Random random = new Random();

    private final List<Region> obstacles = new ArrayList<>();
    private AnimationTimer loop;
    private String sessionId;
    private String initialMedalLabel = "Niveau Starter";
    private long startedAtMillis;
    private long lastTickNanos = 0L;
    private double dinoY = GROUND_Y;
    private double velocityY = 0;
    private boolean running = false;
    private int remainingSeconds = GAME_SECONDS;
    private double elapsedObstacle = 0;
    private int score = 0;
    private int jumps = 0;

    @FXML
    private void initialize() {
        User user = session.getCurrentUser();
        if (user == null) {
            navigateTo("/Auth/Login.fxml", "Connexion");
            return;
        }

        contextLabel.setText("Contexte: " + session.getSmartBreakContext());
        initialMedalLabel = readCurrentMedal(user.getId());
        retryButton.setDisable(true);
        setupKeyboard();
        startGameSession();
    }

    @FXML
    private void handleJump() {
        if (!running) return;
        if (dinoY >= GROUND_Y - 0.5) {
            velocityY = -460.0;
            jumps++;
            jumpsLabel.setText(String.valueOf(jumps));
        }
    }

    @FXML
    private void handleRetry() {
        User user = session.getCurrentUser();
        if (user != null && sessionId != null) {
            gameService.cancelSession(user.getId(), sessionId);
        }
        clearObstacles();
        retryButton.setDisable(true);
        infoLabel.setText("");
        startGameSession();
    }

    @FXML
    private void handleBackToMenu() {
        stopLoop();
        User user = session.getCurrentUser();
        if (user != null && sessionId != null) {
            gameService.cancelSession(user.getId(), sessionId);
        }
        navigateTo("/Client/SmartBreakHub.fxml", "Pause Intelligente");
    }

    private void startGameSession() {
        User user = session.getCurrentUser();
        if (user == null) return;

        GameSessionStart start = gameService.startSession(user.getId(), session.getSmartBreakContext());
        sessionId = start.sessionId();
        startedAtMillis = System.currentTimeMillis();
        lastTickNanos = 0L;
        remainingSeconds = GAME_SECONDS;
        score = 0;
        jumps = 0;
        elapsedObstacle = 0;
        dinoY = GROUND_Y;
        velocityY = 0;
        dinoNode.setLayoutY(dinoY);
        timerLabel.setText(remainingSeconds + "s");
        scoreLabel.setText("0");
        jumpsLabel.setText("0");
        running = true;
        startLoop();
    }

    private void startLoop() {
        stopLoop();
        loop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastTickNanos == 0) {
                    lastTickNanos = now;
                    return;
                }
                double dt = (now - lastTickNanos) / 1_000_000_000.0;
                lastTickNanos = now;
                update(dt);
            }
        };
        loop.start();
    }

    private void update(double dt) {
        if (!running) return;

        // Timer
        int elapsed = (int) ((System.currentTimeMillis() - startedAtMillis) / 1000);
        int left = Math.max(0, GAME_SECONDS - elapsed);
        if (left != remainingSeconds) {
            remainingSeconds = left;
            timerLabel.setText(remainingSeconds + "s");
        }
        if (remainingSeconds <= 0) {
            finishRunSuccess();
            return;
        }

        // Dino physics
        velocityY += 980 * dt;
        dinoY += velocityY * dt;
        if (dinoY > GROUND_Y) {
            dinoY = GROUND_Y;
            velocityY = 0;
        }
        dinoNode.setLayoutY(dinoY);

        // Obstacles
        elapsedObstacle += dt;
        double spawnInterval = 0.9 + random.nextDouble() * 0.9; // 0.9s - 1.8s
        if (elapsedObstacle >= spawnInterval) {
            elapsedObstacle = 0;
            spawnObstacle();
        }

        Iterator<Region> it = obstacles.iterator();
        while (it.hasNext()) {
            Region obstacle = it.next();
            obstacle.setLayoutX(obstacle.getLayoutX() - (260 * dt));

            if (obstacle.getLayoutX() + obstacle.getPrefWidth() < 0) {
                gamePane.getChildren().remove(obstacle);
                it.remove();
                score += 5;
                scoreLabel.setText(String.valueOf(score));
                continue;
            }

            if (collides(dinoNode, obstacle)) {
                failRun();
                return;
            }
        }
    }

    private void spawnObstacle() {
        Region obstacle = new Region();
        double w = 18 + random.nextInt(12);
        double h = 24 + random.nextInt(18);
        obstacle.setPrefSize(w, h);
        obstacle.getStyleClass().add("dino-obstacle");
        obstacle.setLayoutX(Math.max(gamePane.getWidth(), 900));
        obstacle.setLayoutY(GROUND_Y + 34 - h);
        obstacles.add(obstacle);
        gamePane.getChildren().add(obstacle);
    }

    private boolean collides(Region a, Region b) {
        return a.getBoundsInParent().intersects(b.getBoundsInParent());
    }

    private void failRun() {
        running = false;
        stopLoop();
        User user = session.getCurrentUser();
        if (user != null && sessionId != null) {
            gameService.cancelSession(user.getId(), sessionId);
        }
        retryButton.setDisable(false);
        infoLabel.setText("Collision avec un arbre. Echec, cliquez sur Rejouer.");
        infoLabel.setStyle("-fx-text-fill: #b91c1c; -fx-font-weight: 700;");
    }

    private void finishRunSuccess() {
        running = false;
        stopLoop();
        retryButton.setDisable(false);

        User user = session.getCurrentUser();
        long duration = System.currentTimeMillis() - startedAtMillis;
        GameSessionResult result = gameService.endSession(user.getId(), sessionId, score, duration, Math.max(jumps, 1));

        StringBuilder msg = new StringBuilder("Run termine | Score: " + score + " | " + result.message());
        String newMedal = readCurrentMedal(user.getId());
        if (newMedal != null && !newMedal.equalsIgnoreCase(initialMedalLabel)) {
            msg.append(" | Nouveau badge: ").append(newMedal);
            initialMedalLabel = newMedal;
        }
        infoLabel.setText(msg.toString());
        infoLabel.setStyle("-fx-text-fill: #0f4e96; -fx-font-weight: 700;");
    }

    private void clearObstacles() {
        for (Region obstacle : obstacles) {
            gamePane.getChildren().remove(obstacle);
        }
        obstacles.clear();
    }

    private void stopLoop() {
        if (loop != null) {
            loop.stop();
            loop = null;
        }
    }

    private void setupKeyboard() {
        gamePane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.SPACE || event.getCode() == KeyCode.UP) {
                        handleJump();
                    }
                });
                gamePane.requestFocus();
            }
        });
    }

    private String readCurrentMedal(int userId) {
        try {
            ClientGamificationSnapshot snapshot = gamificationService.getClientSnapshot(userId);
            return snapshot == null ? "Niveau Starter" : snapshot.medalLabel();
        } catch (Exception ignored) {
            return "Niveau Starter";
        }
    }

    private void navigateTo(String fxmlPath, String title) {
        try {
            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) return;
            Parent root = FXMLLoader.load(fxmlUrl);
            Scene scene = new Scene(root);
            URL cssUrl = getClass().getResource("/Styles/StyleWallet.css");
            if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());
            Stage stage = (Stage) gamePane.getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            infoLabel.setText("Navigation impossible: " + e.getMessage());
            infoLabel.setStyle("-fx-text-fill: #b91c1c;");
        }
    }
}