package org.example.Controlleurs.WalletControlleur;

import org.example.Model.Wallet.ClassWallet.Wallet;
import org.example.Service.WalletService.WalletService;
import org.example.Service.WalletService.CurrencyService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.stage.Stage;
import java.net.URL;
import java.util.ResourceBundle;
import java.io.IOException;
import java.util.Optional;

public class ConversionController implements Initializable {

    @FXML private TextField txtMontant;
    @FXML private ComboBox<String> comboDeviseSource;
    @FXML private ComboBox<String> comboDeviseCible;
    @FXML private Label lblWalletSource;
    @FXML private Label lblWalletCible;
    @FXML private Label lblTaux;
    @FXML private Label lblMontantSource;
    @FXML private Label lblDeviseSource;
    @FXML private Label lblMontantCible;
    @FXML private Label lblDeviseCible;
    @FXML private Label lblMessage;

    private WalletService walletService;
    private CurrencyService currencyService;
    private Wallet walletConnecte;
    private double tauxActuel;
    private double montantConverti;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        walletService = new WalletService();
        currencyService = new CurrencyService();

        comboDeviseSource.setItems(FXCollections.observableArrayList(
                "TND", "EUR", "USD", "GBP", "CHF"
        ));
        comboDeviseCible.setItems(FXCollections.observableArrayList(
                "TND", "EUR", "USD", "GBP", "CHF"
        ));

        comboDeviseSource.setValue("TND");
        comboDeviseCible.setValue("EUR");

        comboDeviseSource.valueProperty().addListener((obs, old, nv) -> mettreAJourTaux());
        comboDeviseCible.valueProperty().addListener((obs, old, nv) -> mettreAJourTaux());
        txtMontant.textProperty().addListener((obs, old, nv) -> calculerConversion());

        mettreAJourTaux();
    }

    public void setWalletConnecte(Wallet wallet) {
        this.walletConnecte = wallet;
        lblWalletSource.setText("Wallet: " + wallet.getNom_proprietaire() + " (" + wallet.getDevise() + ")");
    }

    private void mettreAJourTaux() {
        try {
            String source = comboDeviseSource.getValue();
            String cible = comboDeviseCible.getValue();

            if (source != null && cible != null) {
                tauxActuel = currencyService.getTaux(source, cible);
                lblTaux.setText(String.format("1 %s = %.4f %s", source, tauxActuel, cible));
                calculerConversion();
            }
        } catch (IOException e) {
            lblTaux.setText("Erreur de taux");
            e.printStackTrace();
        }
    }

    private void calculerConversion() {
        try {
            String montantText = txtMontant.getText().trim();
            if (montantText.isEmpty()) {
                lblMontantSource.setText("0.00");
                lblMontantCible.setText("0.00");
                return;
            }

            double montant = Double.parseDouble(montantText.replace(",", "."));
            montantConverti = montant * tauxActuel;

            String source = comboDeviseSource.getValue();
            String cible = comboDeviseCible.getValue();

            lblMontantSource.setText(String.format("%.2f", montant));
            lblDeviseSource.setText(source != null ? source : "TND");
            lblMontantCible.setText(String.format("%.2f", montantConverti));
            lblDeviseCible.setText(cible != null ? cible : "EUR");

        } catch (NumberFormatException e) {
            lblMontantSource.setText("0.00");
            lblMontantCible.setText("0.00");
        }
    }

    @FXML
    private void handleConvertir() {
        calculerConversion();
    }

    @FXML
    private void handleTransfert() {
        if (walletConnecte == null) {
            showAlert("Erreur", "Aucun wallet connecté");
            return;
        }

        try {
            double montantSource = Double.parseDouble(txtMontant.getText().trim().replace(",", "."));
            String sourceDevise = comboDeviseSource.getValue();
            String cibleDevise = comboDeviseCible.getValue();

            // Demander le numéro du destinataire
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("📞 Destinataire");
            dialog.setHeaderText("Entrez le numéro de téléphone du destinataire");
            dialog.setContentText("Téléphone :");

            Optional<String> result = dialog.showAndWait();
            if (!result.isPresent() || result.get().trim().isEmpty()) {
                return; // Annulé
            }

            String telephoneDestinataire = result.get().trim();

            // Chercher le wallet du destinataire dans la devise cible
            Wallet destinataire = walletService.getWalletByTelephoneAndDevise(telephoneDestinataire, cibleDevise);

            if (destinataire == null) {
                // Le destinataire n'a pas de wallet dans cette devise
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("❌ Erreur");
                alert.setHeaderText("Compte introuvable");
                alert.setContentText("Le destinataire n'a pas de wallet en " + cibleDevise);
                alert.showAndWait();
                return;
            }

            // Vérifier que ce n'est pas le même que l'expéditeur
            if (destinataire.getId_wallet() == walletConnecte.getId_wallet()) {
                showAlert("Erreur", "Vous ne pouvez pas vous transférer à vous-même");
                return;
            }

            // Ouvrir l'interface de transfert avec les données pré-remplies
            ouvrirTransfert(destinataire, montantConverti, cibleDevise);

        } catch (NumberFormatException e) {
            showAlert("Erreur", "Montant invalide");
        }
    }

    // ✅ NOUVELLE MÉTHODE AJOUTÉE
    private void ouvrirTransfert(Wallet destinataire, double montant, String devise) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Wallet/transaction.fxml"));
            Parent root = loader.load();

            TransactionController controller = loader.getController();
            controller.setWalletConnecte(walletConnecte);

            // Pré-remplir les champs de transfert
            controller.preRemplirTransfert(destinataire, montant, devise);

            Stage stage = new Stage();
            stage.setTitle("Transfert avec conversion");
            stage.setScene(new Scene(root));
            stage.show();

            // Fermer la fenêtre de conversion
            Stage conversionStage = (Stage) txtMontant.getScene().getWindow();
            conversionStage.close();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir l'interface de transfert");
        }
    }

    @FXML
    private void handleAnnuler() {
        txtMontant.clear();
        lblMessage.setVisible(false);
        lblMontantSource.setText("0.00");
        lblMontantCible.setText("0.00");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}