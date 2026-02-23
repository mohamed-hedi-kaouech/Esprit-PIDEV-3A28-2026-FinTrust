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
            Parent root = FXMLLoader.load(getClass().getResource("/Auth/Login.fxml"));

            Scene scene = new Scene(root);

            String css = getClass().getResource("/Styles/StyleWallet.css").toExternalForm();
            scene.getStylesheets().add(css);

            primaryStage.setTitle("FinTrust - Authentification");
            primaryStage.setMinWidth(900);
            primaryStage.setMinHeight(640);
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch(Exception e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement de l'application : " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
