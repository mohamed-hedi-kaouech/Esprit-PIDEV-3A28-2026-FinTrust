package org.example.Controlleurs.WalletControlleur;

import org.example.Model.Wallet.ClassWallet.Wallet;
import org.example.Model.Wallet.EnumWallet.WalletDevise;
import org.example.Model.Wallet.EnumWallet.WalletStatut;
import org.example.Service.WalletService.WalletService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.net.URL;
import java.util.*;

public class WalletController implements Initializable {

    @FXML private TextField txtNomProprietaire;
    @FXML private TextField txtSolde;
    @FXML private ComboBox<WalletDevise> comboDevise;
    @FXML private ComboBox<WalletStatut> comboStatut;
    @FXML private TableView<org.example.Model.Wallet.ClassWallet.Wallet> tableViewWallet;  // Chemin complet
    @FXML private TableColumn<org.example.Model.Wallet.ClassWallet.Wallet, Integer> colId;
    @FXML private TableColumn<org.example.Model.Wallet.ClassWallet.Wallet, String> colNom;
    @FXML private TableColumn<org.example.Model.Wallet.ClassWallet.Wallet, Double> colSolde;
    @FXML private TableColumn<org.example.Model.Wallet.ClassWallet.Wallet, WalletDevise> colDevise;
    @FXML private TableColumn<org.example.Model.Wallet.ClassWallet.Wallet, WalletStatut> colStatut;
    @FXML private TableColumn<org.example.Model.Wallet.ClassWallet.Wallet, Date> colDate;
    @FXML private Label lblErrorNom;
    @FXML private Label lblErrorSolde;
    @FXML private Label lblErrorDevise;

    private WalletService walletService;
    private ObservableList<org.example.Model.Wallet.ClassWallet.Wallet> walletList;  // Chemin complet
    private org.example.Model.Wallet.ClassWallet.Wallet walletConnecte;  // Chemin complet

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        walletService = new WalletService();
        walletList = FXCollections.observableArrayList();

        setupComboBoxes();
        setupTableColumns();
        loadWallets();
        setupListeners();
    }

    private void setupComboBoxes() {
        comboDevise.setItems(FXCollections.observableArrayList(WalletDevise.values()));
        comboStatut.setItems(FXCollections.observableArrayList(WalletStatut.values()));
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id_wallet"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom_proprietaire"));
        colSolde.setCellValueFactory(new PropertyValueFactory<>("solde"));
        colDevise.setCellValueFactory(new PropertyValueFactory<>("devise"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date_creation"));

        tableViewWallet.setItems(walletList);
    }

    private void loadWallets() {
        walletList.clear();
        List<org.example.Model.Wallet.ClassWallet.Wallet> wallets = walletService.getAllWallets();
        walletList.addAll(wallets);
    }

    private void setupListeners() {
        tableViewWallet.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        showWalletDetails(newSelection);
                    }
                });
    }

    private void showWalletDetails(org.example.Model.Wallet.ClassWallet.Wallet wallet) {
        txtNomProprietaire.setText(wallet.getNom_proprietaire());
        txtSolde.setText(String.valueOf(wallet.getSolde()));
        comboDevise.setValue(wallet.getDevise());
        comboStatut.setValue(wallet.getStatut());
    }

    @FXML
    private void handleAjouter() {
        if (!validateInputs()) return;

        try {
            org.example.Model.Wallet.ClassWallet.Wallet wallet = new org.example.Model.Wallet.ClassWallet.Wallet();
            wallet.setNom_proprietaire(txtNomProprietaire.getText().trim());
            wallet.setSolde(Double.parseDouble(txtSolde.getText().trim()));
            wallet.setDevise(comboDevise.getValue());
            wallet.setStatut(comboStatut.getValue() != null ? comboStatut.getValue() : WalletStatut.DRAFT);

            if (walletService.ajouterWallet(wallet)) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Wallet ajouté");
                loadWallets();
                clearForm();
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    @FXML
    private void handleModifier() {
        org.example.Model.Wallet.ClassWallet.Wallet selected = tableViewWallet.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Sélectionnez un wallet");
            return;
        }

        if (!validateInputs()) return;

        try {
            selected.setNom_proprietaire(txtNomProprietaire.getText().trim());
            selected.setSolde(Double.parseDouble(txtSolde.getText().trim()));
            selected.setDevise(comboDevise.getValue());
            selected.setStatut(comboStatut.getValue());

            if (walletService.modifierWallet(selected)) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Wallet modifié");
                loadWallets();
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    @FXML
    private void handleSupprimer() {
        org.example.Model.Wallet.ClassWallet.Wallet selected = tableViewWallet.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Sélectionnez un wallet");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Supprimer ce wallet ?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (walletService.supprimerWallet(selected.getId_wallet())) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Wallet supprimé");
                loadWallets();
                clearForm();
            }
        }
    }

    @FXML
    private void handleVider() {
        clearForm();
    }

    private boolean validateInputs() {
        boolean isValid = true;

        if (txtNomProprietaire.getText() == null || txtNomProprietaire.getText().trim().isEmpty()) {
            lblErrorNom.setText("Nom obligatoire");
            isValid = false;
        } else {
            lblErrorNom.setText("");
        }

        try {
            double solde = Double.parseDouble(txtSolde.getText().trim());
            if (solde < 0) {
                lblErrorSolde.setText("Solde >= 0");
                isValid = false;
            } else {
                lblErrorSolde.setText("");
            }
        } catch (NumberFormatException e) {
            lblErrorSolde.setText("Nombre valide");
            isValid = false;
        }

        if (comboDevise.getValue() == null) {
            lblErrorDevise.setText("Devise obligatoire");
            isValid = false;
        } else {
            lblErrorDevise.setText("");
        }

        return isValid;
    }

    private void clearForm() {
        txtNomProprietaire.clear();
        txtSolde.clear();
        comboDevise.setValue(null);
        comboStatut.setValue(null);
        tableViewWallet.getSelectionModel().clearSelection();
        lblErrorNom.setText("");
        lblErrorSolde.setText("");
        lblErrorDevise.setText("");
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void setWalletConnecte(org.example.Model.Wallet.ClassWallet.Wallet wallet) {
        this.walletConnecte = wallet;
    }
}