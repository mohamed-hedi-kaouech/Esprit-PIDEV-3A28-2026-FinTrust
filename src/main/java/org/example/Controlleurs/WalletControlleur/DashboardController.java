package org.example.Controlleurs.WalletControlleur;

import org.example.Model.Wallet.ClassWallet.Wallet;
import org.example.Model.Wallet.ClassWallet.Transaction;
import org.example.Model.Wallet.EnumWallet.WalletDevise;
import org.example.Model.Wallet.EnumWallet.WalletStatut;
import org.example.Service.WalletService.WalletService;
import org.example.Service.WalletService.TransactionService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.chart.PieChart;
import javafx.collections.FXCollections;
import javafx.stage.Stage;
import java.net.URL;
import java.util.*;

public class DashboardController implements Initializable {

    @FXML private Label lblTotalWallets;
    @FXML private Label lblTotalTransactions;
    @FXML private Label lblSoldeTotal;
    @FXML private TableView<Wallet> tableViewWallets;
    @FXML private TableColumn<Wallet, Integer> colId;
    @FXML private TableColumn<Wallet, String> colProprietaire;
    @FXML private TableColumn<Wallet, Double> colSolde;
    @FXML private TableColumn<Wallet, String> colDevise;
    @FXML private TableColumn<Wallet, String> colStatut;
    @FXML private ListView<String> recentTransactionsList;
    @FXML private PieChart pieChartWallets;

    private WalletService walletService;
    private TransactionService transactionService;
    private Wallet walletConnecte;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        walletService = new WalletService();
        transactionService = new TransactionService();

        List<Wallet> wallets = walletService.getAllWallets();
        if (!wallets.isEmpty()) walletConnecte = wallets.get(0);

        setupTableColumns();
        loadDashboardData();
        setupCharts();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getId_wallet()).asObject());
        colProprietaire.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNom_proprietaire()));
        colSolde.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getSolde()).asObject());

        // Utilisation de toString() sur l'enum
        colDevise.setCellValueFactory(cellData -> {
            WalletDevise devise = cellData.getValue().getDevise();
            return new javafx.beans.property.SimpleStringProperty(devise != null ? devise.toString() : "");
        });

        colStatut.setCellValueFactory(cellData -> {
            WalletStatut statut = cellData.getValue().getStatut();
            return new javafx.beans.property.SimpleStringProperty(statut != null ? statut.toString() : "");
        });
    }

    private void loadDashboardData() {
        List<Wallet> wallets = walletService.getAllWallets();
        List<Transaction> transactions = transactionService.getAllTransactions();

        lblTotalWallets.setText(String.valueOf(wallets.size()));
        lblTotalTransactions.setText(String.valueOf(transactions.size()));

        double soldeTotal = wallets.stream().mapToDouble(Wallet::getSolde).sum();
        lblSoldeTotal.setText(String.format("%.2f TND", soldeTotal));

        tableViewWallets.setItems(FXCollections.observableArrayList(wallets));

        recentTransactionsList.getItems().clear();
        transactions.stream().limit(5).forEach(t -> {
            Wallet w = walletService.getWalletById(t.getId_wallet());
            recentTransactionsList.getItems().add(
                    String.format("%s - %s: %.2f %s",
                            t.getDate_transaction().toLocalDate(),
                            t.getType(),
                            t.getMontant(),
                            w != null ? w.getDevise().toString() : "TND")
            );
        });
    }

    private void setupCharts() {
        Map<String, Integer> deviseCount = new HashMap<>();
        walletService.getAllWallets().forEach(w -> {
            String devise = w.getDevise().toString();
            deviseCount.put(devise, deviseCount.getOrDefault(devise, 0) + 1);
        });

        pieChartWallets.getData().clear();
        deviseCount.forEach((devise, count) ->
                pieChartWallets.getData().add(new PieChart.Data(devise, count)));
    }

    @FXML
    private void handleGestionWallet() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Wallet/wallet.fxml"));
            Parent root = loader.load();

            WalletController controller = loader.getController();
            controller.setWalletConnecte(walletConnecte);

            Stage stage = new Stage();
            stage.setTitle("Gestion des Wallets");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir");
        }
    }

    @FXML
    private void handleGestionTransaction() {
        if (walletConnecte == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Créez d'abord un wallet");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Wallet/transaction.fxml"));
            Parent root = loader.load();

            TransactionController controller = loader.getController();
            controller.setWalletConnecte(walletConnecte);

            Stage stage = new Stage();
            stage.setTitle("Gestion des Transactions");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir");
        }
    }

    @FXML private void handleRefresh() { loadDashboardData(); }
    @FXML private void handleQuitter() { System.exit(0); }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}