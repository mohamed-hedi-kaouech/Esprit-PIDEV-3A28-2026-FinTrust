package org.example.Controlleurs.WalletControlleur;

import org.example.Model.Wallet.ClassWallet.Wallet;
import org.example.Service.WalletService.WalletService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Optional;
import javafx.scene.Node;

public class ClientLoginController implements Initializable {

    @FXML private TextField txtIdentifiant;
    @FXML private PasswordField txtCode;

    private WalletService walletService = new WalletService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialisation si nécessaire
    }

    @FXML
    private void handleLogin() {
        String identifiant = txtIdentifiant.getText().trim();
        String code = txtCode.getText().trim();

        if (identifiant.isEmpty() || code.isEmpty()) {
            showAlert("Erreur", "Veuillez saisir votre identifiant et votre code");
            return;
        }

        // Vérifier les identifiants
        boolean valide = walletService.verifierCode(identifiant, code);

        if (valide) {
            // Récupérer le wallet
            Wallet wallet = walletService.getWalletByIdentifiant(identifiant);

            // Activer le compte si ce n'est pas déjà fait
            if (!wallet.isEstActif()) {
                walletService.activerCompte(wallet.getIdWallet());
            }

            // Ouvrir le dashboard client
            ouvrirDashboardClient(wallet);

        } else {
            showAlert("Erreur", "Identifiant ou code incorrect");
        }
    }

    private void ouvrirDashboardClient(Wallet wallet) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Wallet/client_dashboard.fxml"));
            Parent root = loader.load();

            ClientDashboardController controller = loader.getController();
            controller.setClientWallet(wallet);

            Stage stage = new Stage();
            stage.setTitle("FinTrust - Espace Client");
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.show();

            // Fermer la fenêtre de login
            Stage loginStage = (Stage) txtIdentifiant.getScene().getWindow();
            loginStage.close();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir votre espace client");
        }
    }

    // ✅ Nouvelle méthode pour le survol du bouton
    @FXML
    private void handleButtonHover(MouseEvent event) {
        Button button = (Button) event.getSource();
        button.setStyle("-fx-background-color: #1e90ff; -fx-text-fill: white; -fx-padding: 12; -fx-background-radius: 30; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand;");
    }

    // ✅ Nouvelle méthode pour la sortie du survol
    @FXML
    private void handleButtonExit(MouseEvent event) {
        Button button = (Button) event.getSource();
        button.setStyle("-fx-background-color: #0047ab; -fx-text-fill: white; -fx-padding: 12; -fx-background-radius: 30; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand;");
    }

    // ✅ Nouvelle méthode pour la souscription
    @FXML
    private void handleSouscrire(MouseEvent event) {
        showAlert("Souscription", "Pour souscrire à nos services, veuillez contacter votre conseiller au +216 71 123 456 ou par email: conseiller@fintrust.tn");
    }

    // ✅ Nouvelle méthode pour le retour à la page de choix
    @FXML
    private void handleRetour(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Wallet/Choice/ChoiceView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de retourner à la page d'accueil");
        }
    }

    // ✅ Nouvelle méthode pour la navigation (aide)
    @FXML
    private void handleNavigation(MouseEvent event) {
        Label source = (Label) event.getSource();
        String text = source.getText();

        if (text.equals("Aide")) {
            showAlert("Aide", "Besoin d'aide ?\n\n• Centre d'aide: help.fintrust.tn\n• Support: support@fintrust.tn\n• Téléphone: +216 71 123 456\n• Horaires: 8h-18h (Lun-Ven)");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}