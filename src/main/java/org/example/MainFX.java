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
            // Charge le fichier FXML du dashboard
            Parent root = FXMLLoader.load(getClass().getResource("/Wallet/dashboard.fxml"));

            Scene scene = new Scene(root);

            // Ajoute le CSS
            String css = getClass().getResource("/Styles/StyleWallet.css").toExternalForm();
            scene.getStylesheets().add(css);

            primaryStage.setTitle("FinTrust - Gestion Bancaire");
            primaryStage.setMaximized(true);
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
<<<<<<< HEAD
}
=======

    @Override
    public void start(Stage primaryStage) {

        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/MenuGUI.fxml")));
            Scene scene = new Scene(root);
            primaryStage.setTitle("Manager");
            primaryStage.setMinWidth(900);
            primaryStage.setMinHeight(600);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
>>>>>>> 73e547e27955c8dd234e9be4bc09f7eef35e3643
