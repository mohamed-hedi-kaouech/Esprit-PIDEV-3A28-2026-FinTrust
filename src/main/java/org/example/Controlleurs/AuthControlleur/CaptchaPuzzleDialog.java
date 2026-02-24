package org.example.Controlleurs.AuthControlleur;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.example.Service.Security.CaptchaChallenge;
import org.example.Service.Security.CaptchaService;
import org.example.Service.Security.CaptchaTrailPoint;
import org.example.Service.Security.CaptchaVerificationResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class CaptchaPuzzleDialog {

    private CaptchaPuzzleDialog() {}

    public static String show(Window owner, String fingerprint, String intent) {
        CaptchaService service = CaptchaService.getInstance();
        CaptchaChallenge challenge = service.createChallenge(fingerprint, intent);
        AtomicReference<String> tokenOut = new AtomicReference<>(null);

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        if (owner != null) stage.initOwner(owner);
        stage.setTitle("Verification puzzle");

        VBox root = new VBox(12);
        root.setPadding(new Insets(16));
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #f5f9ff, #eaf2ff);");

        Label title = new Label("Glissez la piece pour completer le puzzle");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: 800; -fx-text-fill: #103a6d;");

        Pane puzzlePane = new Pane();
        puzzlePane.setMinSize(challenge.sliderMax() + 60, 110);
        puzzlePane.setPrefSize(challenge.sliderMax() + 60, 110);
        puzzlePane.setStyle("-fx-background-color: linear-gradient(to right, #dceaff, #cde1ff); -fx-background-radius: 12;");

        Region hole = new Region();
        hole.setPrefSize(challenge.pieceWidth(), challenge.pieceHeight());
        hole.setLayoutX(challenge.targetX());
        hole.setLayoutY(25);
        hole.setStyle("-fx-background-color: rgba(255,255,255,0.35); -fx-border-color: #5c86c4; -fx-border-style: segments(4,4); -fx-border-width: 2;");

        Region piece = new Region();
        piece.setPrefSize(challenge.pieceWidth(), challenge.pieceHeight());
        piece.setLayoutX(0);
        piece.setLayoutY(25);
        piece.setStyle("-fx-background-color: linear-gradient(to bottom right, #1f4f9d, #3d77d8); -fx-background-radius: 8;");

        puzzlePane.getChildren().addAll(hole, piece);

        Slider slider = new Slider(0, challenge.sliderMax(), 0);
        slider.setPrefWidth(challenge.sliderMax() + 40);

        Label info = new Label("Relachez puis cliquez Verifier");
        info.setStyle("-fx-text-fill: #294d7b; -fx-font-size: 12px;");

        List<CaptchaTrailPoint> trail = new ArrayList<>();
        long t0 = System.nanoTime();
        trail.add(new CaptchaTrailPoint(0, 0));
        slider.valueProperty().addListener((obs, oldV, newV) -> {
            int x = newV.intValue();
            piece.setLayoutX(x);
            long elapsedMs = (System.nanoTime() - t0) / 1_000_000L;
            trail.add(new CaptchaTrailPoint(elapsedMs, x));
        });

        Button verifyBtn = new Button("Verifier");
        verifyBtn.setStyle("-fx-background-color: #0f3f7a; -fx-text-fill: white; -fx-font-weight: 700; -fx-background-radius: 20;");
        verifyBtn.setOnAction(e -> {
            int finalX = (int) slider.getValue();
            long elapsedMs = (System.nanoTime() - t0) / 1_000_000L;
            trail.add(new CaptchaTrailPoint(elapsedMs, finalX));

            CaptchaVerificationResult result = service.verify(
                    challenge.challengeId(),
                    finalX,
                    trail,
                    fingerprint,
                    intent
            );
            if (result.verified()) {
                tokenOut.set(result.captchaToken());
                stage.close();
            } else {
                info.setText("Echec: " + toFriendlyReason(result.reason()) + ". Reessayez.");
                info.setStyle("-fx-text-fill: #b91c1c; -fx-font-size: 12px; -fx-font-weight: 700;");
            }
        });

        Button cancelBtn = new Button("Annuler");
        cancelBtn.setStyle("-fx-background-color: #ffffff; -fx-border-color: #0f3f7a; -fx-text-fill: #0f3f7a; -fx-background-radius: 20; -fx-border-radius: 20;");
        cancelBtn.setOnAction(e -> stage.close());

        HBox actions = new HBox(8, verifyBtn, cancelBtn);
        actions.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(title, puzzlePane, slider, info, actions);
        stage.setScene(new Scene(root, challenge.sliderMax() + 110, 260));
        stage.showAndWait();
        return tokenOut.get();
    }

    private static String toFriendlyReason(String reason) {
        if (reason == null) return "verification invalide";
        return switch (reason) {
            case "TRAIL_TOO_SHORT" -> "mouvement trop court";
            case "TRAIL_TOO_FAST" -> "mouvement trop rapide";
            case "TRAIL_INVALID_TIME" -> "mouvement invalide";
            case "TRAIL_IMPOSSIBLE_JUMP" -> "deplacement non naturel";
            case "TRAIL_NON_HUMAN" -> "mouvement non humain detecte";
            case "MISALIGN" -> "piece mal alignee";
            case "EXPIRED" -> "challenge expire";
            default -> reason;
        };
    }
}
