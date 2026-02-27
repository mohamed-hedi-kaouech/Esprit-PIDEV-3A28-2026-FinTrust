package org.example.Controlleurs.WalletControlleur;

import org.example.Model.Wallet.ClassWallet.Transaction;
import org.example.Model.Wallet.ClassWallet.Wallet;
import org.example.Service.WalletService.TransactionService;
import org.example.Service.WalletService.WalletService;
import org.example.Service.WalletService.FraisBancaireService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleStringProperty;
import javafx.stage.Stage;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class RevenusController implements Initializable {

    @FXML private Label lblSoldeCompte;
    @FXML private Label lblTotalFrais;
    @FXML private Label lblNbOperations;
    @FXML private Label lblDateSolde;
    @FXML private Label lblTotalRetraits;
    @FXML private Label lblTotalTransferts;
    @FXML private Label lblTotalRejets;
    @FXML private Label lblTotalAgios;
    @FXML private Label lblTotalAutres;
    @FXML private Label lblMoyenne;

    @FXML private TableView<Transaction> tableViewRevenus;
    @FXML private TableColumn<Transaction, String> colDate;
    @FXML private TableColumn<Transaction, String> colDescription;
    @FXML private TableColumn<Transaction, Double> colMontant;

    private WalletService walletService;
    private TransactionService transactionService;
    private FraisBancaireService fraisService;
    private ObservableList<Transaction> transactionList;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        walletService = new WalletService();
        transactionService = new TransactionService();
        fraisService = new FraisBancaireService();
        transactionList = FXCollections.observableArrayList();

        setupTableColumns();
        loadData();
    }

    private void setupTableColumns() {
        colDate.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDate_transaction()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
        colDescription.setCellValueFactory(cellData -> {
            String desc = cellData.getValue().getDescription();
            return new SimpleStringProperty(desc != null ? desc : "Opération");
        });
        colMontant.setCellValueFactory(new PropertyValueFactory<>("montant"));

        // Formatage des montants
        colMontant.setCellFactory(column -> new TableCell<Transaction, Double>() {
            @Override
            protected void updateItem(Double montant, boolean empty) {
                super.updateItem(montant, empty);
                if (empty || montant == null) {
                    setText(null);
                } else {
                    setText(String.format("+%.2f TND", montant));
                    setStyle("-fx-text-fill: #27ae60; -fx-font-weight: 600;");
                }
            }
        });

        tableViewRevenus.setItems(transactionList);
    }

    private void loadData() {
        int idCompteBanque = fraisService.getIdCompteBanque();
        Wallet compteBanque = walletService.getWalletById(idCompteBanque);
        List<Transaction> transactions = transactionService.getTransactionsByWallet(idCompteBanque);

        System.out.println("\n💰 **CHARGEMENT DES REVENUS** 💰");
        System.out.println("Compte banque ID: " + idCompteBanque);
        System.out.println("Nombre de transactions: " + transactions.size());

        // Solde du compte
        lblSoldeCompte.setText(String.format("%,.2f", compteBanque.getSolde()));
        lblDateSolde.setText("Dernière mise à jour: " +
                LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        // Statistiques
        double totalRetraits = 0, totalTransferts = 0, totalRejets = 0, totalAgios = 0;

        for (Transaction t : transactions) {
            String desc = t.getDescription() != null ? t.getDescription().toLowerCase() : "sans description";
            double montant = t.getMontant();

            System.out.println("📝 Transaction: " + montant + " TND - Description: '" + desc + "'");

            // ✅ CORRIGÉ : Meilleure détection des types
            if (desc.contains("retrait")) {
                totalRetraits += montant;
                System.out.println("   ➡️ Classé comme RETRAIT");
            } else if (desc.contains("transfert") || desc.contains("transfer")) {
                totalTransferts += montant;
                System.out.println("   ➡️ Classé comme TRANSFERT");
            } else if (desc.contains("rejet") || desc.contains("chèque") || desc.contains("cheque")) {
                totalRejets += montant;
                System.out.println("   ➡️ Classé comme REJET");
            } else if (desc.contains("agios") || desc.contains("découvert") || desc.contains("decouvert")) {
                totalAgios += montant;
                System.out.println("   ➡️ Classé comme AGIOS");
            } else {
                System.out.println("   ➡️ Classé comme AUTRES");
            }
        }

        double totalGeneral = transactions.stream().mapToDouble(Transaction::getMontant).sum();
        double totalAutres = totalGeneral - (totalRetraits + totalTransferts + totalRejets + totalAgios);
        double moyenne = transactions.isEmpty() ? 0 : totalGeneral / transactions.size();

        lblTotalFrais.setText(String.format("%,.2f TND", totalGeneral));
        lblNbOperations.setText(transactions.size() + " opérations");

        lblTotalRetraits.setText(String.format("%,.2f TND", totalRetraits));
        lblTotalTransferts.setText(String.format("%,.2f TND", totalTransferts));
        lblTotalRejets.setText(String.format("%,.2f TND", totalRejets));
        lblTotalAgios.setText(String.format("%,.2f TND", totalAgios));
        lblTotalAutres.setText(String.format("%,.2f TND", totalAutres));
        lblMoyenne.setText(String.format("%,.2f TND", moyenne));

        System.out.println("\n📊 RÉSULTATS:");
        System.out.println("   Retraits: " + totalRetraits + " TND");
        System.out.println("   Transferts: " + totalTransferts + " TND");
        System.out.println("   Rejets: " + totalRejets + " TND");
        System.out.println("   Agios: " + totalAgios + " TND");
        System.out.println("   Autres: " + totalAutres + " TND");
        System.out.println("   TOTAL: " + totalGeneral + " TND\n");

        // Ajouter les transactions au tableau
        transactionList.clear();
        transactionList.addAll(transactions);
    }

    @FXML
    private void handleFermer() {
        Stage stage = (Stage) lblSoldeCompte.getScene().getWindow();
        stage.close();
    }
}