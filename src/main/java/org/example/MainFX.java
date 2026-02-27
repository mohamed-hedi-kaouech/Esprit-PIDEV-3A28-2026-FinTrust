package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.Controlleurs.WalletControlleur.ChoiceController;
import java.util.Objects;

public class MainFX extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                    getClass().getResource("/Wallet/Choice/ChoiceView.fxml")));

            Parent root = loader.load();

            // ✅ Récupérer le contrôleur et lui passer la fenêtre
            ChoiceController controller = loader.getController();
            controller.setChoiceStage(primaryStage);

            Scene scene = new Scene(root);
            primaryStage.setTitle("FinTrust - Connexion");
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}