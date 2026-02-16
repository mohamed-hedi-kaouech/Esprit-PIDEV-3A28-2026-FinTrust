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
import javafx.geometry.Insets;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class WalletController implements Initializable {

    @FXML private TextField txtNomProprietaire;
    @FXML private TextField txtSolde;
    @FXML private ComboBox<WalletDevise> comboDevise;
    @FXML private ComboBox<WalletStatut> comboStatut;
    @FXML private TableView<Wallet> tableViewWallet;
    @FXML private TableColumn<Wallet, Integer> colId;
    @FXML private TableColumn<Wallet, String> colNom;
    @FXML private TableColumn<Wallet, Double> colSolde;
    @FXML private TableColumn<Wallet, WalletDevise> colDevise;
    @FXML private TableColumn<Wallet, WalletStatut> colStatut;
    @FXML private TableColumn<Wallet, String> colDate;
    @FXML private Label lblErrorNom;
    @FXML private Label lblErrorSolde;
    @FXML private Label lblErrorDevise;
    @FXML private Label lblErrorStatut;

    private WalletService walletService;
    private ObservableList<Wallet> walletList;
    private Wallet walletConnecte;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        walletService = new WalletService();
        walletList = FXCollections.observableArrayList();

        setupComboBoxes();
        setupTableColumns();
        loadWallets();
        setupListeners();

        // Ajouter des listeners pour la validation en temps réel
        setupValidationListeners();
    }

    private void setupComboBoxes() {
        comboDevise.setItems(FXCollections.observableArrayList(WalletDevise.values()));
        comboStatut.setItems(FXCollections.observableArrayList(WalletStatut.values()));

        // Style des combobox
        comboDevise.setPromptText("Sélectionner une devise");
        comboStatut.setPromptText("Sélectionner un statut");
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id_wallet"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom_proprietaire"));
        colSolde.setCellValueFactory(new PropertyValueFactory<>("solde"));
        colSolde.setCellFactory(column -> new TableCell<Wallet, Double>() {
            @Override
            protected void updateItem(Double solde, boolean empty) {
                super.updateItem(solde, empty);
                if (empty || solde == null) {
                    setText(null);
                } else {
                    setText(String.format("%,.2f", solde));
                }
            }
        });
        colDevise.setCellValueFactory(new PropertyValueFactory<>("devise"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colStatut.setCellFactory(column -> new TableCell<Wallet, WalletStatut>() {
            @Override
            protected void updateItem(WalletStatut statut, boolean empty) {
                super.updateItem(statut, empty);
                if (empty || statut == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(statut.toString());
                    badge.setPadding(new Insets(4, 12, 4, 12));
                    badge.setStyle("-fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: 600;");

                    switch (statut) {
                        case ACTIVE:
                            badge.setStyle(badge.getStyle() + "-fx-background-color: #d1fae5; -fx-text-fill: #059669;");
                            break;
                        case DRAFT:
                            badge.setStyle(badge.getStyle() + "-fx-background-color: #fff3cd; -fx-text-fill: #b45309;");
                            break;
                        case SUSPENDED:
                            badge.setStyle(badge.getStyle() + "-fx-background-color: #fee2e2; -fx-text-fill: #b91c1c;");
                            break;
                        case CLOSED:
                            badge.setStyle(badge.getStyle() + "-fx-background-color: #e2e8f0; -fx-text-fill: #475569;");
                            break;
                    }

                    setGraphic(badge);
                    setText(null);
                }
            }
        });
        colDate.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getDate_creation().format(dateFormatter))
        );

        tableViewWallet.setItems(walletList);
    }

    private void setupValidationListeners() {
        // Validation du nom (pas de chiffres ni symboles)
        txtNomProprietaire.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                if (!newValue.matches("[a-zA-Z\\s]+")) {
                    lblErrorNom.setText("Le nom ne doit contenir que des lettres");
                    txtNomProprietaire.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2;");
                } else {
                    lblErrorNom.setText("");
                    txtNomProprietaire.setStyle("");
                }
            }
        });

        // Validation du solde (nombre positif)
        txtSolde.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                try {
                    double solde = Double.parseDouble(newValue.replace(",", "."));
                    if (solde < 0) {
                        lblErrorSolde.setText("Le solde doit être positif");
                        txtSolde.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2;");
                    } else {
                        lblErrorSolde.setText("");
                        txtSolde.setStyle("");
                    }
                } catch (NumberFormatException e) {
                    lblErrorSolde.setText("Format de nombre invalide");
                    txtSolde.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2;");
                }
            }
        });
    }

    private void loadWallets() {
        walletList.clear();
        walletList.addAll(walletService.getAllWallets());
    }

    private void setupListeners() {
        tableViewWallet.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        showWalletDetails(newSelection);
                    }
                });
    }

    private void showWalletDetails(Wallet wallet) {
        txtNomProprietaire.setText(wallet.getNom_proprietaire());
        txtSolde.setText(String.valueOf(wallet.getSolde()));
        comboDevise.setValue(wallet.getDevise());
        comboStatut.setValue(wallet.getStatut());
    }

    @FXML
    private void handleAjouter() {
        if (!validateInputs()) return;

        try {
            Wallet wallet = new Wallet();
            wallet.setNom_proprietaire(txtNomProprietaire.getText().trim());
            wallet.setSolde(Double.parseDouble(txtSolde.getText().trim().replace(",", ".")));
            wallet.setDevise(comboDevise.getValue());
            wallet.setStatut(comboStatut.getValue() != null ? comboStatut.getValue() : WalletStatut.DRAFT);

            if (walletService.ajouterWallet(wallet)) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Wallet ajouté avec succès");
                loadWallets();
                clearForm();
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    @FXML
    private void handleModifier() {
        Wallet selected = tableViewWallet.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un wallet");
            return;
        }

        if (!validateInputs()) return;

        try {
            selected.setNom_proprietaire(txtNomProprietaire.getText().trim());
            selected.setSolde(Double.parseDouble(txtSolde.getText().trim().replace(",", ".")));
            selected.setDevise(comboDevise.getValue());
            selected.setStatut(comboStatut.getValue());

            if (walletService.modifierWallet(selected)) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Wallet modifié avec succès");
                loadWallets();
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    @FXML
    private void handleSupprimer() {
        Wallet selected = tableViewWallet.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un wallet");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Voulez-vous vraiment supprimer ce wallet ?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (walletService.supprimerWallet(selected.getId_wallet())) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Wallet supprimé avec succès");
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

        // Validation du nom (obligatoire et lettres uniquement)
        String nom = txtNomProprietaire.getText();
        if (nom == null || nom.trim().isEmpty()) {
            lblErrorNom.setText("Le nom est obligatoire");
            txtNomProprietaire.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2;");
            isValid = false;
        } else if (!nom.matches("[a-zA-Z\\s]+")) {
            lblErrorNom.setText("Le nom ne doit contenir que des lettres");
            txtNomProprietaire.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2;");
            isValid = false;
        } else {
            lblErrorNom.setText("");
            txtNomProprietaire.setStyle("");
        }

        // Validation du solde (obligatoire et positif)
        String soldeText = txtSolde.getText();
        if (soldeText == null || soldeText.trim().isEmpty()) {
            lblErrorSolde.setText("Le solde est obligatoire");
            txtSolde.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2;");
            isValid = false;
        } else {
            try {
                double solde = Double.parseDouble(soldeText.trim().replace(",", "."));
                if (solde < 0) {
                    lblErrorSolde.setText("Le solde doit être positif");
                    txtSolde.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2;");
                    isValid = false;
                } else {
                    lblErrorSolde.setText("");
                    txtSolde.setStyle("");
                }
            } catch (NumberFormatException e) {
                lblErrorSolde.setText("Format de nombre invalide");
                txtSolde.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2;");
                isValid = false;
            }
        }

        // Validation de la devise
        if (comboDevise.getValue() == null) {
            lblErrorDevise.setText("La devise est obligatoire");
            isValid = false;
        } else {
            lblErrorDevise.setText("");
        }

        // Validation du statut
        if (comboStatut.getValue() == null) {
            lblErrorStatut.setText("Le statut est obligatoire");
            isValid = false;
        } else {
            lblErrorStatut.setText("");
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
        lblErrorStatut.setText("");
        txtNomProprietaire.setStyle("");
        txtSolde.setStyle("");
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void setWalletConnecte(Wallet wallet) {
        this.walletConnecte = wallet;
    }
}