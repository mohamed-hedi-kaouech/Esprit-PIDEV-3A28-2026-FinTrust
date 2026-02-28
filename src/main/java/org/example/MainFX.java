package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainFX extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(MainFX.class.getResource("/Auth/Login.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);

            var cssUrl = MainFX.class.getResource("/Styles/StyleWallet.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            } else {
                System.err.println("CSS introuvable: /Styles/StyleWallet.css");
            }

            primaryStage.setTitle("FinTrust - Authentification");
            primaryStage.setMinWidth(900);
            primaryStage.setMinHeight(640);
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur chargement app: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}