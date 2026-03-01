package org.example.Controlleurs.WalletControlleur;

import org.example.Model.Wallet.ClassWallet.Wallet;
import org.example.Model.Wallet.EnumWallet.WalletDevise;
import org.example.Model.Wallet.EnumWallet.WalletStatut;
import org.example.Service.WalletService.WalletService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.Node;

import java.io.IOException;

public class ClientWalletCreationController {

    @FXML private TextField txtNomProprietaire;
    @FXML private TextField txtTelephone;
    @FXML private TextField txtEmail;
    @FXML private TextField txtSolde;
    @FXML private TextField txtPlafondDecouvert;
    @FXML private ComboBox<WalletDevise> comboDevise;  // Changé en WalletDevise
    @FXML private ComboBox<WalletStatut> comboStatut;

    @FXML private Label lblErrorNom;
    @FXML private Label lblErrorTelephone;
    @FXML private Label lblErrorEmail;
    @FXML private Label lblErrorSolde;
    @FXML private Label lblErrorPlafond;
    @FXML private Label lblError;

    @FXML private VBox codeInfoBox;
    @FXML private Label lblCodeInfo;

    private int currentUserId;
    private String userNom;
    private String userPrenom;
    private WalletService walletService = new WalletService();

    @FXML
    public void initialize() {
        // Initialiser les ComboBox
        if (comboDevise != null) {
            comboDevise.setItems(javafx.collections.FXCollections.observableArrayList(WalletDevise.values()));
            comboDevise.setValue(WalletDevise.TND); // Valeur par défaut
        }
        if (comboStatut != null) {
            comboStatut.setItems(javafx.collections.FXCollections.observableArrayList(WalletStatut.values()));
            comboStatut.setValue(WalletStatut.ACTIVE); // Valeur par défaut
            comboStatut.setVisible(false);
            comboStatut.setManaged(false);
        }

        // ✅ Le plafond est maintenant modifiable (pas de setEditable(false))
        if (txtPlafondDecouvert != null) {
            txtPlafondDecouvert.setText("0");
            // Pas de setEditable(false) - le champ est modifiable
        }

        if (codeInfoBox != null) {
            codeInfoBox.setVisible(false);
            codeInfoBox.setManaged(false);
        }
    }

    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
    }

    public void setUserInfo(String nom, String prenom, String email, String telephone) {
        this.userNom = nom;
        this.userPrenom = prenom;

        if (txtNomProprietaire != null) {
            txtNomProprietaire.setText(nom + " " + prenom);
        }
        if (txtEmail != null) {
            txtEmail.setText(email);
        }
        if (txtTelephone != null && telephone != null && !telephone.isEmpty()) {
            txtTelephone.setText(telephone);
        }
    }

    @FXML
    private void handleCreerWallet() {
        // Réinitialiser les erreurs
        clearErrors();

        // Validation
        if (!validateInputs()) {
            return;
        }

        try {
            double solde = Double.parseDouble(txtSolde.getText().trim().replace(",", "."));
            double plafond = Double.parseDouble(txtPlafondDecouvert.getText().trim().replace(",", "."));

            // Créer le wallet
            Wallet wallet = new Wallet(
                    txtNomProprietaire.getText().trim(),
                    txtTelephone.getText().trim(),
                    txtEmail.getText().trim(),
                    solde,
                    comboDevise.getValue()
            );
            wallet.setStatut(WalletStatut.ACTIVE);
            wallet.setPlafondDecouvert(plafond);
            wallet.setIdUser(currentUserId);

            if (walletService.ajouterWallet(wallet)) {
                // ✅ Succès - Afficher le code comme dans WalletController
                String code = wallet.getCodeAcces();
                lblCodeInfo.setText("✅ Code d'accès généré : " + code + " (envoyé au client) - Plafond découvert: " + plafond);
                codeInfoBox.setVisible(true);
                codeInfoBox.setManaged(true);

                // ✅ Désactiver les champs après création
                txtNomProprietaire.setDisable(true);
                txtTelephone.setDisable(true);
                txtEmail.setDisable(true);
                txtSolde.setDisable(true);
                txtPlafondDecouvert.setDisable(true);
                comboDevise.setDisable(true);

                // Ne pas rediriger immédiatement, laisser le client voir le code
                // Le bouton "Continuer vers la connexion" fera la redirection

            } else {
                showError("Erreur lors de la création du wallet");
            }

        } catch (NumberFormatException e) {
            showError("Montant invalide");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur: " + e.getMessage());
        }
    }

    @FXML
    private void handleAnnuler(ActionEvent event) {
        // Retourner au dashboard client
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Client/ClientDashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Dashboard Client");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur lors du retour");
        }
    }

    @FXML
    private void handleRetour(ActionEvent event) {
        handleAnnuler(event); // Même action que Annuler
    }

    @FXML
    private void handleContinuerVersLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Wallet/client_login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) txtNomProprietaire.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Connexion à votre wallet");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur de redirection");
        }
    }

    private boolean validateInputs() {
        boolean isValid = true;

        // Nom
        if (txtNomProprietaire.getText().trim().isEmpty()) {
            lblErrorNom.setText("Nom obligatoire");
            isValid = false;
        } else {
            lblErrorNom.setText("");
        }

        // Téléphone (optionnel mais format valide)
        if (!txtTelephone.getText().trim().isEmpty()) {
            String tel = txtTelephone.getText().trim();
            if (!tel.matches("\\+?[0-9]{8,15}")) {
                lblErrorTelephone.setText("Format invalide");
                isValid = false;
            } else {
                lblErrorTelephone.setText("");
            }
        } else {
            lblErrorTelephone.setText("");
        }

        // Email
        String email = txtEmail.getText().trim();
        if (email.isEmpty()) {
            lblErrorEmail.setText("Email obligatoire");
            isValid = false;
        } else if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            lblErrorEmail.setText("Email invalide");
            isValid = false;
        } else {
            lblErrorEmail.setText("");
        }

        // Solde
        try {
            Double.parseDouble(txtSolde.getText().trim().replace(",", "."));
            lblErrorSolde.setText("");
        } catch (NumberFormatException e) {
            lblErrorSolde.setText("Montant invalide");
            isValid = false;
        }

        // Plafond découvert
        try {
            double plafond = Double.parseDouble(txtPlafondDecouvert.getText().trim().replace(",", "."));
            if (plafond < 0) {
                lblErrorPlafond.setText("Plafond doit être ≥ 0");
                isValid = false;
            } else {
                lblErrorPlafond.setText("");
            }
        } catch (NumberFormatException e) {
            lblErrorPlafond.setText("Plafond invalide");
            isValid = false;
        }

        // Devise
        if (comboDevise.getValue() == null) {
            isValid = false;
        }

        return isValid;
    }

    private void clearErrors() {
        lblErrorNom.setText("");
        lblErrorTelephone.setText("");
        lblErrorEmail.setText("");
        lblErrorSolde.setText("");
        lblErrorPlafond.setText("");
        lblError.setText("");
    }

    private void showError(String message) {
        lblError.setText(message);
    }
}