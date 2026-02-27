package org.example.Controlleurs.WalletControlleur;

import org.example.Model.Wallet.ClassWallet.Wallet;
import org.example.Model.Wallet.EnumWallet.WalletDevise;
import org.example.Model.Wallet.EnumWallet.WalletStatut;
import org.example.Service.WalletService.WalletService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleStringProperty;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class WalletController implements Initializable {

    @FXML private TextField txtNomProprietaire;
    @FXML private TextField txtTelephone;
    @FXML private TextField txtEmail;
    @FXML private TextField txtSolde;
    @FXML private TextField txtPlafondDecouvert;
    @FXML private ComboBox<WalletDevise> comboDevise;
    @FXML private ComboBox<WalletStatut> comboStatut;
    @FXML private Label lblErrorNom;
    @FXML private Label lblErrorTelephone;
    @FXML private Label lblErrorEmail;
    @FXML private Label lblErrorSolde;
    @FXML private Label lblErrorPlafond;
    @FXML private HBox codeInfoBox;
    @FXML private Label lblCodeInfo;

    @FXML private TableView<Wallet> tableViewWallet;
    @FXML private TableColumn<Wallet, Integer> colId;
    @FXML private TableColumn<Wallet, String> colNom;
    @FXML private TableColumn<Wallet, String> colTelephone;
    @FXML private TableColumn<Wallet, String> colEmail;
    @FXML private TableColumn<Wallet, Double> colSolde;
    @FXML private TableColumn<Wallet, Double> colPlafond;
    @FXML private TableColumn<Wallet, String> colDevise;
    @FXML private TableColumn<Wallet, String> colStatut;
    @FXML private TableColumn<Wallet, String> colDate;

    private WalletService walletService;
    private ObservableList<Wallet> walletList;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        walletService = new WalletService();
        walletList = FXCollections.observableArrayList();

        setupComboBoxes();
        setupTableColumns();
        loadWallets();
        setupListeners();
        setupTooltips();

        if (codeInfoBox != null) {
            codeInfoBox.setVisible(false);
        }
    }

    private void setupComboBoxes() {
        comboDevise.setItems(FXCollections.observableArrayList(WalletDevise.values()));
        comboStatut.setItems(FXCollections.observableArrayList(WalletStatut.values()));
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idWallet"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nomProprietaire"));
        colTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colSolde.setCellValueFactory(new PropertyValueFactory<>("solde"));
        colPlafond.setCellValueFactory(new PropertyValueFactory<>("plafondDecouvert"));
        colDevise.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDevise().toString()));
        colStatut.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStatut().toString()));
        colDate.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getDateCreation().format(dateFormatter)));

        tableViewWallet.setItems(walletList);
    }

    private void loadWallets() {
        walletList.clear();
        walletList.addAll(walletService.getAllWallets());
    }

    private void setupListeners() {
        tableViewWallet.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        afficherWallet(newSelection);
                    }
                });
    }

    private void setupTooltips() {
        // ✅ Tooltip pour expliquer le plafond de découvert
        Tooltip plafondTooltip = new Tooltip(
                "Le plafond de découvert est le montant maximum autorisé en négatif.\n" +
                        "Exemple : Si le solde est de 1000 TND et le plafond de 500 TND,\n" +
                        "vous pouvez descendre jusqu'à -500 TND (découvert total de 1500 TND).\n" +
                        "Au-delà, les transactions seront bloquées."
        );
        txtPlafondDecouvert.setTooltip(plafondTooltip);

        // ✅ Label d'information visible
        Label infoLabel = new Label("ⓘ");
        infoLabel.setTooltip(plafondTooltip);
        infoLabel.setStyle("-fx-text-fill: #003366; -fx-font-size: 16px; -fx-cursor: hand;");
        // Tu peux ajouter ce label dans le GridPane à côté du champ plafond
    }

    private void afficherWallet(Wallet wallet) {
        txtNomProprietaire.setText(wallet.getNomProprietaire());
        txtTelephone.setText(wallet.getTelephone());
        txtEmail.setText(wallet.getEmail());
        txtSolde.setText(String.valueOf(wallet.getSolde()));
        txtPlafondDecouvert.setText(String.valueOf(wallet.getPlafondDecouvert()));
        comboDevise.setValue(wallet.getDevise());
        comboStatut.setValue(wallet.getStatut());
        if (codeInfoBox != null) {
            codeInfoBox.setVisible(false);
        }
    }

    @FXML
    private void handleAjouter() {
        if (!validateInputs()) return;

        try {
            double solde = Double.parseDouble(txtSolde.getText().trim().replace(",", "."));
            double plafond = Double.parseDouble(txtPlafondDecouvert.getText().trim().replace(",", "."));

            Wallet wallet = new Wallet(
                    txtNomProprietaire.getText().trim(),
                    txtTelephone.getText().trim(),
                    txtEmail.getText().trim(),
                    solde,
                    comboDevise.getValue()
            );
            wallet.setStatut(comboStatut.getValue());
            wallet.setPlafondDecouvert(plafond);

            if (walletService.ajouterWallet(wallet)) {
                showSuccess("Wallet ajouté avec succès !");

                String code = wallet.getCodeAcces();
                lblCodeInfo.setText("✅ Code d'accès généré : " + code +
                        " (envoyé au client) - Plafond découvert: " + plafond);
                if (codeInfoBox != null) {
                    codeInfoBox.setVisible(true);
                }

                loadWallets();
                clearForm();
            } else {
                showError("Échec", "L'ajout du wallet a échoué");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", e.getMessage());
        }
    }

    @FXML
    private void handleModifier() {
        Wallet selected = tableViewWallet.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Sélectionnez un wallet");
            return;
        }

        if (!validateInputs()) return;

        try {
            selected.setNomProprietaire(txtNomProprietaire.getText().trim());
            selected.setTelephone(txtTelephone.getText().trim());
            selected.setEmail(txtEmail.getText().trim());
            selected.setSolde(Double.parseDouble(txtSolde.getText().trim().replace(",", ".")));
            selected.setPlafondDecouvert(Double.parseDouble(txtPlafondDecouvert.getText().trim().replace(",", ".")));
            selected.setDevise(comboDevise.getValue());
            selected.setStatut(comboStatut.getValue());

            if (walletService.modifierWallet(selected)) {
                showSuccess("Wallet modifié");
                loadWallets();
                clearForm();
                if (codeInfoBox != null) {
                    codeInfoBox.setVisible(false);
                }
            }

        } catch (Exception e) {
            showError("Erreur", e.getMessage());
        }
    }

    @FXML
    private void handleSupprimer() {
        Wallet selected = tableViewWallet.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Sélectionnez un wallet");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setContentText("Supprimer ce wallet ?");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            if (walletService.supprimerWallet(selected.getIdWallet())) {
                showSuccess("Wallet supprimé");
                loadWallets();
                clearForm();
                if (codeInfoBox != null) {
                    codeInfoBox.setVisible(false);
                }
            }
        }
    }

    @FXML
    private void handleVider() {
        clearForm();
        if (codeInfoBox != null) {
            codeInfoBox.setVisible(false);
        }
    }

    private boolean validateInputs() {
        boolean isValid = true;

        if (txtNomProprietaire.getText().trim().isEmpty()) {
            lblErrorNom.setText("Nom obligatoire");
            isValid = false;
        } else {
            lblErrorNom.setText("");
        }

        String tel = txtTelephone.getText().trim();
        if (!tel.isEmpty() && !tel.matches("\\+?[0-9]{8,15}")) {
            lblErrorTelephone.setText("Format invalide");
            isValid = false;
        } else {
            lblErrorTelephone.setText("");
        }

        String email = txtEmail.getText().trim();
        if (!email.isEmpty() && !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            lblErrorEmail.setText("Email invalide");
            isValid = false;
        } else {
            lblErrorEmail.setText("");
        }

        try {
            Double.parseDouble(txtSolde.getText().trim().replace(",", "."));
            lblErrorSolde.setText("");
        } catch (NumberFormatException e) {
            lblErrorSolde.setText("Montant invalide");
            isValid = false;
        }

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

        if (comboDevise.getValue() == null) {
            isValid = false;
        }

        return isValid;
    }

    private void clearForm() {
        txtNomProprietaire.clear();
        txtTelephone.clear();
        txtEmail.clear();
        txtSolde.clear();
        txtPlafondDecouvert.clear();
        comboDevise.setValue(null);
        comboStatut.setValue(null);
        lblErrorNom.setText("");
        lblErrorTelephone.setText("");
        lblErrorEmail.setText("");
        lblErrorSolde.setText("");
        lblErrorPlafond.setText("");
        tableViewWallet.getSelectionModel().clearSelection();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText("✅ Opération réussie");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText("❌ " + title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Attention");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void preRemplirCreation(String nom, String devise, double soldeInitial) {
        txtNomProprietaire.setText(nom);

        for (WalletDevise d : comboDevise.getItems()) {
            if (d.toString().equals(devise)) {
                comboDevise.setValue(d);
                break;
            }
        }

        txtSolde.setText(String.valueOf(soldeInitial));
        txtPlafondDecouvert.setText("0");

        System.out.println("Formulaire pré-rempli pour " + nom + " - " + devise);
    }
}