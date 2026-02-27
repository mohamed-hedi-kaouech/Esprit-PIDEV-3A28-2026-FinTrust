package org.example.Controlleurs.WalletControlleur;

import org.example.Model.Wallet.ClassWallet.Transaction;
import org.example.Model.Wallet.ClassWallet.Wallet;
import org.example.Service.WalletService.TransactionService;
import org.example.Service.WalletService.WalletService;
import org.example.Service.WalletService.SmsService;
import org.example.Service.WalletService.CurrencyService;
import org.example.Service.WalletService.FraisBancaireService;
import org.example.Service.WalletService.SecuriteService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleStringProperty;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.io.IOException;

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

    @FXML private TextField txtDestinataire;
    @FXML private Label lblDestinataireInfo;

    private TransactionService transactionService;
    private WalletService walletService;
    private SmsService smsService;
    private CurrencyService currencyService;
    private FraisBancaireService fraisService;
    private SecuriteService securiteService;
    private ObservableList<Transaction> transactionList;
    private Wallet walletConnecte;
    private Wallet destinataire;
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private Transaction transactionEnAttente;
    private String codeValidation;
    private LocalDateTime codeExpiration;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        transactionService = new TransactionService();
        walletService = new WalletService();
        smsService = new SmsService();
        currencyService = new CurrencyService();
        fraisService = new FraisBancaireService();
        securiteService = new SecuriteService();
        transactionList = FXCollections.observableArrayList();

        setupTypeGroup();
        setupTableColumns();
        setupListeners();
        setupComboBoxes();
    }

    public void setWalletConnecte(Wallet wallet) {
        this.walletConnecte = wallet;

        if (comboWallet != null) {
            comboWallet.setVisible(false);
        }

        lblWalletInfo.setText(wallet.getNom_proprietaire() + " - " + wallet.getDevise());
        double solde = transactionService.getSoldeWallet(wallet.getId_wallet());
        lblSoldeActuel.setText(String.format("%,.2f %s", solde, wallet.getDevise()));

        loadTransactionsByWallet(wallet.getId_wallet());
    }

    private void setupTypeGroup() {
        typeGroup = new ToggleGroup();
        radioDepot.setToggleGroup(typeGroup);
        radioRetrait.setToggleGroup(typeGroup);
        radioTransfert.setToggleGroup(typeGroup);

        radioDepot.setUserData("DEPOT");
        radioRetrait.setUserData("RETRAIT");
        radioTransfert.setUserData("TRANSFERT");

        radioDepot.setSelected(true);

        typeGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.getUserData().equals("TRANSFERT")) {
                txtDestinataire.setVisible(true);
                txtDestinataire.setManaged(true);
                lblDestinataireInfo.setVisible(true);
                lblDestinataireInfo.setManaged(true);
            } else {
                txtDestinataire.setVisible(false);
                txtDestinataire.setManaged(false);
                lblDestinataireInfo.setVisible(false);
                lblDestinataireInfo.setManaged(false);
                destinataire = null;
            }
        });
    }

    private void setupComboBoxes() {
        if (walletConnecte == null) {
            List<Wallet> wallets = walletService.getAllWallets();
            comboWallet.setItems(FXCollections.observableArrayList(wallets));

            if (!wallets.isEmpty()) {
                comboWallet.setValue(wallets.get(0));
                updateWalletInfo(wallets.get(0));
                loadTransactionsByWallet(wallets.get(0).getId_wallet());
            }
        } else {
            comboWallet.setVisible(false);
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
                new SimpleStringProperty(
                        cellData.getValue().getDate_transaction().format(dateTimeFormatter))
        );

        colWalletId.setCellValueFactory(new PropertyValueFactory<>("id_wallet"));

        tableViewTransaction.setItems(transactionList);
    }

    private void loadTransactionsByWallet(int walletId) {
        transactionList.clear();
        transactionList.addAll(transactionService.getTransactionsByWallet(walletId));
        updateTransactionCount();
    }

    private void updateTransactionCount() {
        if (lblTotalTransactions != null) {
            int count = transactionList.size();
            lblTotalTransactions.setText("Total: " + count + " transaction" + (count > 1 ? "s" : ""));
        }
    }

    private void setupListeners() {
        if (comboWallet != null) {
            comboWallet.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldSelection, newSelection) -> {
                        if (newSelection != null) {
                            updateWalletInfo(newSelection);
                            loadTransactionsByWallet(newSelection.getId_wallet());
                        }
                    });
        }

        txtDestinataire.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                chercherDestinataire(newVal);
            } else {
                lblDestinataireInfo.setText("");
                destinataire = null;
            }
        });
    }

    private void chercherDestinataire(String telephone) {
        Wallet found = walletService.getWalletByIdentifiant(telephone);
        if (found != null) {
            destinataire = found;
            lblDestinataireInfo.setText("✓ " + found.getNom_proprietaire());
            lblDestinataireInfo.setStyle("-fx-text-fill: #27ae60;");
        } else {
            destinataire = null;
            lblDestinataireInfo.setText("✗ Compte non trouvé");
            lblDestinataireInfo.setStyle("-fx-text-fill: #e74c3c;");
        }
    }

    private void updateWalletInfo(Wallet wallet) {
        lblWalletInfo.setText(wallet.getNom_proprietaire() + " - " + wallet.getDevise());
        double solde = transactionService.getSoldeWallet(wallet.getId_wallet());
        lblSoldeActuel.setText(String.format("%,.2f %s", solde, wallet.getDevise()));
    }

    private String getSelectedType() {
        if (typeGroup.getSelectedToggle() == null) return null;
        return typeGroup.getSelectedToggle().getUserData().toString();
    }

    private boolean verifierEtConvertir(Wallet source, Wallet dest, double montantSaisi) {
        if (source.getDevise().equals(dest.getDevise())) {
            return true;
        }

        try {
            double taux = currencyService.getTaux(
                    dest.getDevise().name(),
                    source.getDevise().name()
            );

            double montantSource = montantSaisi * taux;

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("💱 Conversion de devises");
            confirm.setHeaderText("Les devises sont différentes");
            confirm.setContentText(String.format(
                    "Vous voulez transférer : %.2f %s\n" +
                            "Votre wallet est en : %s\n" +
                            "Montant à débiter : %.2f %s\n" +
                            "Taux appliqué : 1 %s = %.4f %s\n\n" +
                            "Confirmer le transfert ?",
                    montantSaisi, dest.getDevise(),
                    source.getDevise(),
                    montantSource, source.getDevise(),
                    dest.getDevise(), taux, source.getDevise()
            ));

            Optional<ButtonType> result = confirm.showAndWait();
            return result.isPresent() && result.get() == ButtonType.OK;

        } catch (IOException e) {
            showError("Erreur", "Impossible de récupérer le taux de change");
            e.printStackTrace();
            return false;
        }
    }

    @FXML
    private void handleAjouter() {
        if (!validateInputs()) return;

        try {
            double montant = Double.parseDouble(txtMontant.getText().trim().replace(",", "."));
            String type = getSelectedType();

            Wallet wallet = (walletConnecte != null) ? walletConnecte : comboWallet.getValue();

            // ✅ VÉRIFICATION DE SÉCURITÉ - Compte bloqué ?
            if (!securiteService.verifierSecurite(wallet)) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("🔒 Compte bloqué");
                alert.setHeaderText("Opération impossible");
                alert.setContentText(
                        "Votre wallet est temporairement bloqué pour des raisons de sécurité.\n\n" +
                                "🔐 Raison : Transactions suspectes détectées\n" +
                                "📞 Contactez le service client FinTrust :\n" +
                                "   • Email: support@fintrust.tn\n" +
                                "   • Tél: +216 71 123 456\n\n" +
                                "L'administrateur peut réactiver votre compte."
                );
                alert.showAndWait();
                return;
            }

            if (type.equals("TRANSFERT")) {
                if (destinataire == null) {
                    showError("Erreur", "Destinataire introuvable");
                    return;
                }

                if (destinataire.getId_wallet() == wallet.getId_wallet()) {
                    showError("Erreur", "Vous ne pouvez pas vous transférer à vous-même");
                    return;
                }

                boolean confirmation = verifierEtConvertir(wallet, destinataire, montant);
                if (!confirmation) {
                    return;
                }
            }

            if (type.equals("RETRAIT") || type.equals("TRANSFERT")) {
                double soldeActuel = transactionService.getSoldeWallet(wallet.getId_wallet());

                if (montant > soldeActuel) {
                    // ✅ Enregistrer l'échec pour la sécurité
                    securiteService.enregistrerTentative(wallet, false);

                    showError("Solde insuffisant",
                            String.format("Montant: %,.2f %s\nSolde disponible: %,.2f %s",
                                    montant, wallet.getDevise(),
                                    soldeActuel, wallet.getDevise()));
                    return;
                }
            }

            if (montant > 1000 && wallet.getTelephone() != null && !wallet.getTelephone().isEmpty()) {
                String code = String.format("%06d", new Random().nextInt(999999));
                this.codeValidation = code;
                this.codeExpiration = LocalDateTime.now().plusMinutes(5);
                this.transactionEnAttente = new Transaction(montant, type, wallet.getId_wallet());

                boolean smsEnvoye = smsService.envoyerCodeSms(wallet.getTelephone(), code);

                if (smsEnvoye) {
                    demanderCodeValidation();
                } else {
                    showError("Erreur", "Impossible d'envoyer le code de validation");
                }
                return;
            }

            // ✅ Vérification des transactions rapides AVANT d'exécuter
            if (!securiteService.verifierTransactionsRapides(wallet)) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("🔒 Sécurité");
                alert.setHeaderText("Transactions trop rapides");
                alert.setContentText(
                        "Vous avez effectué trop de transactions en peu de temps.\n" +
                                "Votre compte est temporairement bloqué pour sécurité.\n\n" +
                                "Réessayez dans 15 minutes ou contactez le support."
                );
                alert.showAndWait();
                return;
            }

            executerTransaction(wallet, montant, type);

            // ✅ Transaction réussie - enregistrer le succès
            securiteService.enregistrerTentative(wallet, true);

            // ✅ Appliquer les frais APRÈS la transaction
            if (type.equals("RETRAIT") || type.equals("TRANSFERT")) {
                fraisService.appliquerFrais(type, montant, wallet);
            }

        } catch (Exception e) {
            showError("Erreur", e.getMessage());
            e.printStackTrace();
        }
    }

    private void demanderCodeValidation() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("🔐 Validation 2FA");
        dialog.setHeaderText("Un code de validation a été envoyé par SMS");
        dialog.setContentText("Saisissez le code reçu :");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(codeSaisi -> {
            if (codeSaisi.equals(this.codeValidation) &&
                    LocalDateTime.now().isBefore(this.codeExpiration)) {

                Wallet wallet = (walletConnecte != null) ? walletConnecte : comboWallet.getValue();
                executerTransaction(wallet,
                        transactionEnAttente.getMontant(),
                        transactionEnAttente.getType());

                showSuccess("Transaction validée et effectuée !");
            } else {
                showError("Erreur", "Code invalide ou expiré");
            }
            transactionEnAttente = null;
            codeValidation = null;
        });
    }

    private void executerTransaction(Wallet wallet, double montant, String type) {
        try {
            if (type.equals("TRANSFERT")) {
                Transaction retrait = new Transaction(montant, "RETRAIT", wallet.getId_wallet());
                Transaction depot = new Transaction(montant, "DEPOT", destinataire.getId_wallet());

                boolean success = transactionService.ajouterTransaction(retrait) &&
                        transactionService.ajouterTransaction(depot);

                if (success) {
                    showSuccess("Transfert effectué avec succès !");
                    loadTransactionsByWallet(wallet.getId_wallet());
                    updateWalletInfo(wallet);
                    clearForm();
                } else {
                    showError("Échec", "Le transfert n'a pas pu être effectué");
                }
            } else {
                Transaction transaction = new Transaction(montant, type, wallet.getId_wallet());

                if (transactionService.ajouterTransaction(transaction)) {
                    showSuccess("Transaction effectuée avec succès !");
                    loadTransactionsByWallet(wallet.getId_wallet());
                    updateWalletInfo(wallet);
                    clearForm();
                } else {
                    showError("Échec", "La transaction n'a pas pu être effectuée");
                }
            }
        } catch (Exception e) {
            showError("Erreur", e.getMessage());
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

            selected.setMontant(montant);
            selected.setType(type);

            if (transactionService.modifierTransaction(selected)) {
                showSuccess("Transaction modifiée");
                Wallet wallet = (walletConnecte != null) ? walletConnecte : comboWallet.getValue();
                loadTransactionsByWallet(wallet.getId_wallet());
                clearForm();
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
                Wallet wallet = (walletConnecte != null) ? walletConnecte : comboWallet.getValue();
                loadTransactionsByWallet(wallet.getId_wallet());
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
        Wallet wallet = (walletConnecte != null) ? walletConnecte : comboWallet.getValue();
        if (wallet != null) {
            loadTransactionsByWallet(wallet.getId_wallet());
            updateWalletInfo(wallet);
            showSuccess("Données actualisées");
        } else {
            showWarning("Aucun wallet sélectionné");
        }
    }

    private boolean validateInputs() {
        boolean isValid = true;

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

        if (getSelectedType() == null) {
            lblErrorType.setText("Type obligatoire");
            isValid = false;
        } else {
            lblErrorType.setText("");
        }

        if (getSelectedType() != null && getSelectedType().equals("TRANSFERT") && destinataire == null) {
            lblErrorType.setText("Destinataire invalide");
            isValid = false;
        }

        if (walletConnecte == null && comboWallet.getValue() == null) {
            lblErrorWallet.setText("Wallet obligatoire");
            isValid = false;
        } else {
            lblErrorWallet.setText("");
        }

        return isValid;
    }

    private void clearForm() {
        txtMontant.clear();
        txtDestinataire.clear();
        radioDepot.setSelected(true);
        lblErrorMontant.setText("");
        lblErrorType.setText("");
        lblErrorWallet.setText("");
        lblDestinataireInfo.setText("");
        destinataire = null;
        transactionEnAttente = null;
        codeValidation = null;
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

    public void preRemplirTransfert(Wallet walletDestinataire, double montant, String devise) {
        radioTransfert.setSelected(true);

        txtDestinataire.setVisible(true);
        txtDestinataire.setManaged(true);
        lblDestinataireInfo.setVisible(true);
        lblDestinataireInfo.setManaged(true);

        txtDestinataire.setText(walletDestinataire.getTelephone());
        chercherDestinataire(walletDestinataire.getTelephone());

        txtMontant.setText(String.valueOf(montant));

        System.out.println("Transfert pré-rempli : " + montant + " " + devise + " vers " + walletDestinataire.getNom_proprietaire());
    }
}