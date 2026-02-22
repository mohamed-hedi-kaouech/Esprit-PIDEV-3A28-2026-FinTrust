package org.example.Controlleurs.ProductControlleur;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MenuProductControlleur implements Initializable {

    @FXML private VBox marketCard;
    @FXML private VBox mySubsCard;
    @FXML private VBox adminCard;
    @FXML private VBox dashboardCard;
    @FXML private VBox adminSub;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        addHoverEffect(marketCard,     "#1a2d42", "#111d2b", "#1e3a5f");
        addHoverEffect(mySubsCard,     "#1a2d42", "#111d2b", "#1e3a5f");
        addHoverEffect(adminCard,      "#1a2d42", "#111d2b", "#1e3a5f");
        addHoverEffect(dashboardCard,  "#0d2d4a", "#0d2137", "#1e5a9e");
        addHoverEffect(adminSub,  "#0d2d4a", "#0d2137", "#1e5a9e");
    }
    @FXML
    private void goBackToMenu(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/MenuGUI.fxml")
            );
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Manager");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void addHoverEffect(VBox card, String hoverBg, String normalBg, String borderColor) {
        card.setOnMouseEntered(e -> card.setStyle(card.getStyle()
                .replace(normalBg, hoverBg)
                .replace("-fx-cursor: hand;", "-fx-cursor: hand; -fx-scale-x: 1.03; -fx-scale-y: 1.03;")));
        card.setOnMouseExited(e -> card.setStyle(card.getStyle()
                .replace(hoverBg, normalBg)
                .replace("-fx-scale-x: 1.03; -fx-scale-y: 1.03;", "")));
    }

    @FXML
    private void navigateToMarket() {
        loadScene("/Product/Client/ClientMarketGUI.fxml", "Marché des Produits");
    }

    @FXML
    private void navigateToClientList() {
        loadScene("/Product/Client/ClientListeProductGUI.fxml", "Mes Abonnements");
    }

    @FXML
    private void navigateToAdminList() {
        loadScene("/Product/Admin/ListeProductGUI.fxml", "Administration des Produits");
    }

    @FXML
    private void navigateToDashboard() {
        loadScene("/Product/Admin/AdminDashboardGUI.fxml", "Dashboard Analytique");
    }
    @FXML
    public void navigateToAdminListSub() {
        loadScene("/Product/Admin/ListeSubProductGUI.fxml", "Administration des Sub Produits");
    }
    private void loadScene(String fxmlPath, String title) {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource(fxmlPath)
            );
            Stage stage = (Stage) Stage.getWindows()
                    .filtered(window -> window.isShowing())
                    .get(0);
            stage.setScene(new Scene(root));
            stage.setTitle(title);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
