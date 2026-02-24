package org.example.Controlleurs.WalletControlleur;

import org.example.Model.Wallet.ClassWallet.Transaction;
import org.example.Model.Wallet.ClassWallet.Wallet;
import org.example.Model.Wallet.EnumWallet.WalletDevise;
import org.example.Service.WalletService.TransactionService;
import org.example.Service.WalletService.WalletService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TransactionController implements Initializable {

    @FXML private TextField txtMontant;
    @FXML private ComboBox<String> comboType;
    @FXML private ComboBox<org.example.Model.Wallet.ClassWallet.Wallet> comboWallet;
    @FXML private Label lblWalletInfo;
    @FXML private Label lblSoldeActuel;
    @FXML private TableView<Transaction> tableViewTransaction;
    @FXML private TableColumn<Transaction, Integer> colId;
    @FXML private TableColumn<Transaction, Double> colMontant;
    @FXML private TableColumn<Transaction, String> colType;
    @FXML private TableColumn<Transaction, String> colDate;
    @FXML private TableColumn<Transaction, Integer> colWalletId;
    @FXML private Label lblErrorMontant;
    @FXML private Label lblErrorType;
    @FXML private Label lblErrorWallet;

    private TransactionService transactionService;
    private WalletService walletService;
    private ObservableList<Transaction> transactionList;
    private org.example.Model.Wallet.ClassWallet.Wallet walletConnecte;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        transactionService = new TransactionService();
        walletService = new WalletService();
        transactionList = FXCollections.observableArrayList();

        setupComboBoxes();
        setupTableColumns();
        loadAllTransactions();
        setupListeners();
    }

    private void setupComboBoxes() {
        comboType.setItems(FXCollections.observableArrayList("DEPOT", "RETRAIT", "TRANSFERT"));

        List<org.example.Model.Wallet.ClassWallet.Wallet> wallets = walletService.getAllWallets();
        comboWallet.setItems(FXCollections.observableArrayList(wallets));

        comboWallet.setCellFactory(lv -> new ListCell<org.example.Model.Wallet.ClassWallet.Wallet>() {
            @Override
            protected void updateItem(org.example.Model.Wallet.ClassWallet.Wallet wallet, boolean empty) {
                super.updateItem(wallet, empty);
                if (empty || wallet == null) {
                    setText(null);
                } else {
                    setText(wallet.getNom_proprietaire() + " - " + wallet.getDevise().toString());
                }
            }
        });

        comboWallet.setButtonCell(new ListCell<org.example.Model.Wallet.ClassWallet.Wallet>() {
            @Override
            protected void updateItem(org.example.Model.Wallet.ClassWallet.Wallet wallet, boolean empty) {
                super.updateItem(wallet, empty);
                if (empty || wallet == null) {
                    setText(null);
                } else {
                    setText(wallet.getNom_proprietaire() + " - " + wallet.getDevise().toString());
                }
            }
        });
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id_transaction"));
        colMontant.setCellValueFactory(new PropertyValueFactory<>("montant"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colDate.setCellValueFactory(cellData -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            return new javafx.beans.property.SimpleStringProperty(
                    cellData.getValue().getDate_transaction().format(formatter));
        });
        colWalletId.setCellValueFactory(new PropertyValueFactory<>("id_wallet"));

        tableViewTransaction.setItems(transactionList);
    }

    private void loadAllTransactions() {
        transactionList.clear();
        transactionList.addAll(transactionService.getAllTransactions());
    }

    private void loadTransactionsByWallet(org.example.Model.Wallet.ClassWallet.Wallet wallet) {
        transactionList.clear();
        transactionList.addAll(transactionService.getTransactionsByWallet(wallet.getId_wallet()));
    }

    private void setupListeners() {
        comboWallet.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        updateWalletInfo(newSelection);
                        loadTransactionsByWallet(newSelection);
                    }
                });
    }

    private void updateWalletInfo(org.example.Model.Wallet.ClassWallet.Wallet wallet) {
        lblWalletInfo.setText(wallet.getNom_proprietaire() + " - " + wallet.getDevise().toString());
        double solde = transactionService.getSoldeWallet(wallet.getId_wallet());
        lblSoldeActuel.setText(String.format("%.2f %s", solde, wallet.getDevise().toString()));
    }

    @FXML
    private void handleAjouter() {
        if (!validateInputs()) return;

        try {
            double montant = Double.parseDouble(txtMontant.getText().trim());
            String type = comboType.getValue();
            org.example.Model.Wallet.ClassWallet.Wallet wallet = comboWallet.getValue();

            if (type.equals("RETRAIT") || type.equals("TRANSFERT")) {
                double soldeActuel = transactionService.getSoldeWallet(wallet.getId_wallet());

                if (montant > soldeActuel) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erreur de transaction");
                    alert.setHeaderText("Solde insuffisant");
                    alert.setContentText(String.format(
                            "Montant demandé: %.2f %s\nSolde disponible: %.2f %s",
                            montant, wallet.getDevise().toString(),
                            soldeActuel, wallet.getDevise().toString()));
                    alert.showAndWait();
                    return;
                }
            }

            Transaction transaction = new Transaction();
            transaction.setMontant(montant);
            transaction.setType(type);
            transaction.setId_wallet(wallet.getId_wallet());

            if (transactionService.ajouterTransaction(transaction)) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Transaction effectuée");
                loadTransactionsByWallet(comboWallet.getValue());
                updateWalletInfo(comboWallet.getValue());
                clearForm();
            }
        } catch (RuntimeException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la transaction");
        }
    }

    @FXML
    private void handleModifier() {
        Transaction selected = tableViewTransaction.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Sélectionnez une transaction");
            return;
        }

        if (!validateInputs()) return;

        try {
            double montant = Double.parseDouble(txtMontant.getText().trim());
            String type = comboType.getValue();
            org.example.Model.Wallet.ClassWallet.Wallet wallet = comboWallet.getValue();

            if (type.equals("RETRAIT") || type.equals("TRANSFERT")) {
                double soldeActuel = transactionService.getSoldeWallet(wallet.getId_wallet());

                if (montant > soldeActuel) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erreur de modification");
                    alert.setHeaderText("Solde insuffisant");
                    alert.setContentText(String.format(
                            "Montant demandé: %.2f %s\nSolde disponible: %.2f %s",
                            montant, wallet.getDevise().toString(),
                            soldeActuel, wallet.getDevise().toString()));  // CORRIGÉ: wallet au lieu de Wallet
                    alert.showAndWait();
                    return;
                }
            }

            selected.setMontant(montant);
            selected.setType(type);

            if (transactionService.modifierTransaction(selected)) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Transaction modifiée");
                loadTransactionsByWallet(comboWallet.getValue());
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    @FXML
    private void handleSupprimer() {
        Transaction selected = tableViewTransaction.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Sélectionnez une transaction");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Supprimer cette transaction ?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (transactionService.supprimerTransaction(selected.getId_transaction())) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Transaction supprimée");
                if (comboWallet.getValue() != null) {
                    loadTransactionsByWallet(comboWallet.getValue());
                }
                clearForm();
            }
        }
    }

    @FXML
    private void handleVider() {
        clearForm();
    }

    @FXML
    private void handleRefresh() {
        if (comboWallet.getValue() != null) {
            loadTransactionsByWallet(comboWallet.getValue());
            updateWalletInfo(comboWallet.getValue());
        }
    }

    private boolean validateInputs() {
        boolean isValid = true;

        try {
            String montantText = txtMontant.getText().trim();
            if (montantText.isEmpty()) {
                lblErrorMontant.setText("Montant obligatoire");
                isValid = false;
            } else {
                double montant = Double.parseDouble(montantText);
                if (montant <= 0) {
                    lblErrorMontant.setText("Montant > 0");
                    isValid = false;
                } else {
                    lblErrorMontant.setText("");
                }
            }
        } catch (NumberFormatException e) {
            lblErrorMontant.setText("Nombre valide");
            isValid = false;
        }

        if (comboType.getValue() == null) {
            lblErrorType.setText("Type obligatoire");
            isValid = false;
        } else {
            lblErrorType.setText("");
        }

        if (comboWallet.getValue() == null) {
            lblErrorWallet.setText("Wallet obligatoire");
            isValid = false;
        } else {
            lblErrorWallet.setText("");
        }

        return isValid;
    }

    private void clearForm() {
        txtMontant.clear();
        comboType.setValue(null);
        lblErrorMontant.setText("");
        lblErrorType.setText("");
        lblErrorWallet.setText("");
        tableViewTransaction.getSelectionModel().clearSelection();
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
        if (wallet != null) {
            comboWallet.setValue(wallet);
            updateWalletInfo(wallet);
            loadTransactionsByWallet(wallet);
        }
    }
}