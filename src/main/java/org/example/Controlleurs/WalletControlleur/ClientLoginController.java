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
import javafx.scene.Node;

public class ClientLoginController implements Initializable {

    @FXML private TextField txtIdentifiant;
    @FXML private PasswordField txtCode;

    private WalletService walletService = new WalletService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialisation si nécessaire
    }

    // ✅ NOUVELLE MÉTHODE POUR PRÉ-REMPLIR L'EMAIL
    public void setEmail(String email) {
        if (txtIdentifiant != null) {
            txtIdentifiant.setText(email);
            // Mettre le focus sur le champ code
            txtCode.requestFocus();
        }
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

            org.example.Controlleurs.WalletControlleur.ClientDashboardController controller =
                    loader.getController();
            controller.setClientWallet(wallet);

            // Remplacer la scène actuelle au lieu d'ouvrir une nouvelle fenêtre
            Stage stage = (Stage) txtIdentifiant.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("FinTrust - Espace Client");
            stage.setMaximized(true);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir votre espace client");
        }
    }

    // Méthodes de style (inchangées)
    @FXML
    private void handleButtonHover(MouseEvent event) {
        Button button = (Button) event.getSource();
        button.setStyle("-fx-background-color: #1e90ff; -fx-text-fill: white; -fx-padding: 12; -fx-background-radius: 30; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand;");
    }

    @FXML
    private void handleButtonExit(MouseEvent event) {
        Button button = (Button) event.getSource();
        button.setStyle("-fx-background-color: #0047ab; -fx-text-fill: white; -fx-padding: 12; -fx-background-radius: 30; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand;");
    }

    @FXML
    private void handleSouscrire(MouseEvent event) {
        showAlert("Souscription", "Pour souscrire à nos services, veuillez contacter votre conseiller au +216 71 123 456 ou par email: conseiller@fintrust.tn");
    }
    @FXML
    private void handleRetour(MouseEvent event) {
        try {
            // Retourner au dashboard client
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Client/ClientDashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Dashboard Client");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de retourner au dashboard client");
        }
    }

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