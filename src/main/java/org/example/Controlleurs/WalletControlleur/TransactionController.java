package org.example.Controlleurs.WalletControlleur;

import org.example.Model.Wallet.ClassWallet.Transaction;
import org.example.Model.Wallet.ClassWallet.Wallet;
import org.example.Service.WalletService.TransactionService;
import org.example.Service.WalletService.WalletService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TransactionController implements Initializable {

    @FXML private TextField txtMontant;
    @FXML private RadioButton radioDepot;
    @FXML private RadioButton radioRetrait;
    @FXML private RadioButton radioTransfert;
    @FXML private ToggleGroup typeGroup;
    @FXML private ComboBox<Wallet> comboWallet;
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
    @FXML private Label lblTotalTransactions;

    private TransactionService transactionService;
    private WalletService walletService;
    private ObservableList<Transaction> transactionList;
    private Wallet walletConnecte;
    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        System.out.println("=== Initialisation du TransactionController ===");

        transactionService = new TransactionService();
        walletService = new WalletService();
        transactionList = FXCollections.observableArrayList();

        setupTypeGroup();
        setupComboBoxes();
        setupTableColumns();
        loadAllTransactions();
        setupListeners();

        System.out.println("=== Initialisation terminée ===");
    }

    private void setupTypeGroup() {
        typeGroup = new ToggleGroup();
        radioDepot.setToggleGroup(typeGroup);
        radioRetrait.setToggleGroup(typeGroup);
        radioTransfert.setToggleGroup(typeGroup);

        radioDepot.setUserData("DEPOT");
        radioRetrait.setUserData("RETRAIT");
        radioTransfert.setUserData("TRANSFERT");

        radioDepot.setSelected(true); // Sélection par défaut
    }

    private void setupComboBoxes() {
        List<Wallet> wallets = walletService.getAllWallets();
        System.out.println("Nombre de wallets chargés: " + wallets.size());
        comboWallet.setItems(FXCollections.observableArrayList(wallets));

        comboWallet.setCellFactory(lv -> new ListCell<Wallet>() {
            @Override
            protected void updateItem(Wallet wallet, boolean empty) {
                super.updateItem(wallet, empty);
                if (empty || wallet == null) {
                    setText(null);
                } else {
                    setText(wallet.getNom_proprietaire() + " - " + wallet.getDevise().toString() +
                            " - " + String.format("%,.2f", wallet.getSolde()));
                }
            }
        });

        if (!wallets.isEmpty()) {
            comboWallet.setValue(wallets.get(0));
            updateWalletInfo(wallets.get(0));
            loadTransactionsByWallet(wallets.get(0));
        }
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id_transaction"));

        colMontant.setCellValueFactory(new PropertyValueFactory<>("montant"));
        colMontant.setCellFactory(column -> new TableCell<Transaction, Double>() {
            @Override
            protected void updateItem(Double montant, boolean empty) {
                super.updateItem(montant, empty);
                if (empty || montant == null) {
                    setText(null);
                } else {
                    Transaction transaction = getTableView().getItems().get(getIndex());
                    String signe = transaction.getType().equals("DEPOT") ? "+" : "-";
                    setText(signe + " " + String.format("%,.2f", montant));

                    if (transaction.getType().equals("DEPOT")) {
                        setStyle("-fx-text-fill: #166534; -fx-font-weight: 600;");
                    } else {
                        setStyle("-fx-text-fill: #b91c1c; -fx-font-weight: 600;");
                    }
                }
            }
        });

        colType.setCellValueFactory(new PropertyValueFactory<>("type"));

        colDate.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getDate_transaction().format(dateTimeFormatter))
        );

        colWalletId.setCellValueFactory(new PropertyValueFactory<>("id_wallet"));

        tableViewTransaction.setItems(transactionList);
    }

    private void loadAllTransactions() {
        transactionList.clear();
        transactionList.addAll(transactionService.getAllTransactions());
        updateTransactionCount();
    }

    private void loadTransactionsByWallet(Wallet wallet) {
        transactionList.clear();
        transactionList.addAll(transactionService.getTransactionsByWallet(wallet.getId_wallet()));
        updateTransactionCount();
    }

    private void updateTransactionCount() {
        if (lblTotalTransactions != null) {
            int count = transactionList.size();
            lblTotalTransactions.setText("Total: " + count + " transaction" + (count > 1 ? "s" : ""));
        }
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

    private void updateWalletInfo(Wallet wallet) {
        lblWalletInfo.setText(wallet.getNom_proprietaire() + " - " + wallet.getDevise().toString());
        double solde = transactionService.getSoldeWallet(wallet.getId_wallet());
        lblSoldeActuel.setText(String.format("%,.2f %s", solde, wallet.getDevise().toString()));
    }

    private String getSelectedType() {
        if (typeGroup.getSelectedToggle() == null) return null;
        return typeGroup.getSelectedToggle().getUserData().toString();
    }

    @FXML
    private void handleAjouter() {
        if (!validateInputs()) return;

        try {
            double montant = Double.parseDouble(txtMontant.getText().trim().replace(",", "."));
            String type = getSelectedType();
            Wallet wallet = comboWallet.getValue();

            if (type.equals("RETRAIT") || type.equals("TRANSFERT")) {
                double soldeActuel = transactionService.getSoldeWallet(wallet.getId_wallet());

                if (montant > soldeActuel) {
                    showError("Solde insuffisant",
                            String.format("Montant: %,.2f %s\nSolde disponible: %,.2f %s",
                                    montant, wallet.getDevise().toString(),
                                    soldeActuel, wallet.getDevise().toString()));
                    return;
                }
            }

            Transaction transaction = new Transaction();
            transaction.setMontant(montant);
            transaction.setType(type);
            transaction.setId_wallet(wallet.getId_wallet());
            transaction.setDate_transaction(LocalDateTime.now());

            if (transactionService.ajouterTransaction(transaction)) {
                showSuccess("Transaction effectuée avec succès !");
                loadTransactionsByWallet(wallet);
                updateWalletInfo(wallet);
                clearForm();
            } else {
                showError("Échec", "La transaction n'a pas pu être effectuée.");
            }

        } catch (Exception e) {
            showError("Erreur", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleModifier() {
        Transaction selected = tableViewTransaction.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Sélectionnez une transaction");
            return;
        }

        if (!validateInputs()) return;

        try {
            double montant = Double.parseDouble(txtMontant.getText().trim().replace(",", "."));
            String type = getSelectedType();
            Wallet wallet = comboWallet.getValue();

            if (type.equals("RETRAIT") || type.equals("TRANSFERT")) {
                double soldeActuel = transactionService.getSoldeWallet(wallet.getId_wallet());
                if (montant > soldeActuel) {
                    showError("Solde insuffisant", "Opération impossible");
                    return;
                }
            }

            selected.setMontant(montant);
            selected.setType(type);

            if (transactionService.modifierTransaction(selected)) {
                showSuccess("Transaction modifiée");
                loadTransactionsByWallet(wallet);
            }
        } catch (Exception e) {
            showError("Erreur", e.getMessage());
        }
    }

    @FXML
    private void handleSupprimer() {
        Transaction selected = tableViewTransaction.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Sélectionnez une transaction");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setContentText("Supprimer cette transaction ?");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            if (transactionService.supprimerTransaction(selected.getId_transaction())) {
                showSuccess("Transaction supprimée");
                if (comboWallet.getValue() != null) {
                    loadTransactionsByWallet(comboWallet.getValue());
                } else {
                    loadAllTransactions();
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
        } else {
            loadAllTransactions();
        }
    }

    private boolean validateInputs() {
        boolean isValid = true;

        // Validation du montant
        String montantText = txtMontant.getText().trim();
        if (montantText.isEmpty()) {
            lblErrorMontant.setText("Montant obligatoire");
            isValid = false;
        } else {
            try {
                double montant = Double.parseDouble(montantText.replace(",", "."));
                if (montant <= 0) {
                    lblErrorMontant.setText("Montant doit être > 0");
                    isValid = false;
                } else {
                    lblErrorMontant.setText("");
                }
            } catch (NumberFormatException e) {
                lblErrorMontant.setText("Nombre valide requis");
                isValid = false;
            }
        }

        // Validation du type
        if (getSelectedType() == null) {
            lblErrorType.setText("Type obligatoire");
            isValid = false;
        } else {
            lblErrorType.setText("");
        }

        // Validation du wallet
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
        radioDepot.setSelected(true);
        lblErrorMontant.setText("");
        lblErrorType.setText("");
        lblErrorWallet.setText("");
        tableViewTransaction.getSelectionModel().clearSelection();
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

    public void setWalletConnecte(Wallet wallet) {
        this.walletConnecte = wallet;
        if (wallet != null) {
            comboWallet.setValue(wallet);
            updateWalletInfo(wallet);
            loadTransactionsByWallet(wallet);
        }
    }
}