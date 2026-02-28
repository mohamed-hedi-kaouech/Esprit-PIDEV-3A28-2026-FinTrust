package org.example.Controlleurs;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.Model.User.User;
import org.example.Utils.SessionContext;
import org.example.Utils.AccessGuard;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class MenuGUIController implements Initializable {

    @FXML
    private Label userNameLabel;

    @FXML
    private VBox adminUserListCard;

    @FXML
    private VBox adminUserFormCard;

    @FXML
    private VBox adminKycCard;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        User currentUser = SessionContext.getInstance().getCurrentUser();
        if (currentUser == null) {
            Platform.runLater(() -> {
                try {
                    navigateToScene("/Auth/Login.fxml", "Connexion", "/Styles/StyleWallet.css");
                } catch (IOException e) {
                    showErrorAlert("Erreur", "Session invalide: " + e.getMessage());
                }
            });
            return;
        }

        if (SessionContext.getInstance().hasLimitedAccess()) {
            Platform.runLater(() -> {
                try {
                    navigateToScene("/Client/ClientDashboard.fxml", "Dashboard Client", "/Styles/StyleWallet.css");
                } catch (IOException e) {
                    showErrorAlert("Erreur", "Redirection KYC impossible: " + e.getMessage());
                }
            });
            return;
        }

        userNameLabel.setText(currentUser.getNom() + " (" + currentUser.getRole().name() + ")");

        boolean isAdmin = AccessGuard.canAccessAdminPages();
        if (adminUserListCard != null) {
            adminUserListCard.setManaged(isAdmin);
            adminUserListCard.setVisible(isAdmin);
        }
        if (adminUserFormCard != null) {
            adminUserFormCard.setManaged(isAdmin);
            adminUserFormCard.setVisible(isAdmin);
        }
        if (adminKycCard != null) {
            adminKycCard.setManaged(isAdmin);
            adminKycCard.setVisible(isAdmin);
        }
    }

    @FXML
    private void goToProduct() {
        try {
            navigateToScene("/Product/Admin/ListeProductGUI.fxml", "Gestion des Produits", null);
        } catch (IOException e) {
            showErrorAlert("Erreur de Navigation",
                    "Impossible d'accéder au module Gestion Produits.\n" + e.getMessage());
        }
    }

    @FXML
    private void goToSubProduct() {
        try {
            navigateToScene("/Product/Admin/ListeSubProductGUI.fxml", "Gestion des Abonnements", null);
        } catch (IOException e) {
            showErrorAlert("Erreur de Navigation",
                    "Impossible d'accéder au module Gestion Abonnements.\n" + e.getMessage());
        }
    }
    public void goToDashboardProduit(MouseEvent mouseEvent) {
        try {
            navigateToScene("/Product/Admin/AdminDashboardGUI.fxml", "Dashboard Analytique",null);
        } catch (IOException e) {
            showErrorAlert("Erreur de Navigation",
                    "Impossible d'accéder au dashboard produit & Abonnements.\n" + e.getMessage());
        }

    }

    @FXML
    private void goToBudget() {
        try {
            navigateToScene("/Budget/CategorieListeGUI.fxml", "Gestion des Budgets",null);
        } catch (IOException e) {
            showErrorAlert("Erreur de Navigation",
                    "Impossible d'accéder au module Gestion Budgets.\n" + e.getMessage());
        }
    }

    /**
     * Navigate to Loan Management
     */
    @FXML
    private void goToLoan() {
        try {
            navigateToScene("/Loan/LoanList.fxml", "Gestion des Loans",null);
        } catch (IOException e) {
            showErrorAlert("Erreur de Navigation",
                    "Impossible d'accéder au module Gestion Loans.\n" + e.getMessage());
        }
    }

    /**
     * Navigate to Wallet Management
     */
    @FXML
    private void goToWallet() {
        if (!AccessGuard.canAccessFullModules()) {
            showErrorAlert("Acces limite", "KYC non approuve. Acces limite au dashboard client et formulaire KYC.");
            return;
        }
        try {
            navigateToScene("/Wallet/CreateWalletGUI.fxml", "Gestion des Wallets", "/Styles/StyleWallet.css");
        } catch (IOException e) {
            showErrorAlert("Erreur de Navigation",
                    "Impossible d'accéder au module Gestion Wallets.\n" + e.getMessage());
        }
    }

    @FXML
    private void goToPublication() {
        showInfoAlert("Information", "Module Publication non branche dans ce flux.");
    }

    @FXML
    private void goToAdminUsers() {
        if (!AccessGuard.canAccessAdminPages()) {
            showErrorAlert("Acces refuse", "Seul un administrateur peut acceder a cet ecran.");
            return;
        }

        try {
            navigateToScene("/Admin/UserDashboard.fxml", "Dashboard Admin", "/Styles/StyleWallet.css");
        } catch (IOException e) {
            showErrorAlert("Erreur de Navigation", "Impossible d'acceder au dashboard admin.\n" + e.getMessage());
        }
    }

    @FXML
    private void goToAdminUserForm() {
        if (!AccessGuard.canAccessAdminPages()) {
            showErrorAlert("Acces refuse", "Seul un administrateur peut creer des utilisateurs.");
            return;
        }

        try {
            navigateToScene("/Admin/UserCreate.fxml", "Creation Utilisateur", "/Styles/StyleWallet.css");
        } catch (IOException e) {
            showErrorAlert("Erreur de Navigation", "Impossible d'acceder au formulaire utilisateur.\n" + e.getMessage());
        }
    }

    @FXML
    private void goToAdminKycValidation() {
        if (!AccessGuard.canAccessAdminPages()) {
            showErrorAlert("Acces refuse", "Seul un administrateur peut valider les KYC.");
            return;
        }
        try {
            navigateToScene("/Admin/KycValidation.fxml", "Validation KYC", "/Styles/StyleWallet.css");
        } catch (IOException e) {
            showErrorAlert("Erreur de Navigation", "Impossible d'acceder a la validation KYC.\n" + e.getMessage());
        }
    }

    @FXML
    private void handleLogout() {
        SessionContext.getInstance().logout();
        try {
            navigateToScene("/Auth/Login.fxml", "Connexion", "/Styles/StyleWallet.css");
        } catch (IOException e) {
            showErrorAlert("Erreur", "Deconnexion impossible: " + e.getMessage());
        }
    }

    private void navigateToScene(String fxmlPath, String title, String stylesheetPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();

        Stage stage = (Stage) userNameLabel.getScene().getWindow();
        Scene scene = new Scene(root);
        if (stylesheetPath != null && !stylesheetPath.isBlank()) {
            scene.getStylesheets().add(getClass().getResource(stylesheetPath).toExternalForm());
        }

        stage.setScene(scene);
        stage.setTitle(title);
        stage.show();
    }

    //Show error alert
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


}
