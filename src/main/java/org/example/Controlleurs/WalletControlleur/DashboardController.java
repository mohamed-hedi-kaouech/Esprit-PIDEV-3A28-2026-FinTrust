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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.PieChart;
import javafx.collections.FXCollections;
import javafx.stage.Stage;
import java.net.URL;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DashboardController implements Initializable {

    @FXML private HBox logoContainer;
    @FXML private Label lblDate;
    @FXML private Label lblWelcome;
    @FXML private Label lblTotalBalance;
    @FXML private Label lblTotalWallets;
    @FXML private Label lblTotalTransactions;
    @FXML private Label lblMonthlyChange;
    @FXML private FlowPane walletsContainer;
    @FXML private VBox transactionsContainer;
    @FXML private PieChart pieChartWallets;
    @FXML private Label lblLastUpdate;

    private WalletService walletService;
    private TransactionService transactionService;
    private Wallet walletConnecte;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        walletService = new WalletService();
        transactionService = new TransactionService();

        // Ajouter le logo
        addLogo();

        // Afficher la date actuelle
        lblDate.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy")));
        lblWelcome.setText("Bonjour, Feryel"); // À remplacer par le nom de l'utilisateur connecté

        loadDashboardData();
        setupCharts();
    }

    private void addLogo() {
        try {
            // Chemin vers votre logo
            String logoPath = "C:/Users/Feryel Hajji/Downloads/Capture_d_écran_2026-02-16_154906-removebg-preview.png";
            File file = new File(logoPath);
            if (file.exists()) {
                Image logoImage = new Image(file.toURI().toString());
                ImageView logoView = new ImageView(logoImage);
                logoView.setFitHeight(50);
                logoView.setFitWidth(50);
                logoView.setPreserveRatio(true);

                // Créer un conteneur pour le logo et le texte
                HBox logoBox = new HBox(10);
                logoBox.setAlignment(Pos.CENTER_LEFT);

                VBox textBox = new VBox(-5);
                Label bankName = new Label("FinTrust");
                bankName.getStyleClass().add("logo-text");
                Label bankSub = new Label("BANKING SOLUTIONS");
                bankSub.getStyleClass().add("logo-subtext");
                textBox.getChildren().addAll(bankName, bankSub);

                logoBox.getChildren().addAll(logoView, textBox);

                // Remplacer le contenu du logoContainer
                logoContainer.getChildren().clear();
                logoContainer.getChildren().add(logoBox);
            } else {
                System.out.println("Logo non trouvé au chemin: " + logoPath);
                // Fallback: texte seulement
                Label bankName = new Label("FinTrust");
                bankName.getStyleClass().add("logo-text");
                logoContainer.getChildren().add(bankName);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback: texte seulement
            Label bankName = new Label("FinTrust");
            bankName.getStyleClass().add("logo-text");
            logoContainer.getChildren().add(bankName);
        }
    }

    private void loadDashboardData() {
        List<Wallet> wallets = walletService.getAllWallets();
        List<Transaction> transactions = transactionService.getAllTransactions();

        // Mettre à jour les statistiques
        updateStatistics(wallets, transactions);

        // Afficher les wallets
        displayWallets(wallets);

        // Afficher les transactions récentes
        displayRecentTransactions(transactions);

        // Dernière mise à jour
        lblLastUpdate.setText("Dernière mise à jour: " + LocalDate.now().format(dateFormatter));
    }

    private void updateStatistics(List<Wallet> wallets, List<Transaction> transactions) {
        lblTotalWallets.setText(String.valueOf(wallets.size()));
        lblTotalTransactions.setText(String.valueOf(transactions.size()));

        double soldeTotal = wallets.stream().mapToDouble(Wallet::getSolde).sum();
        lblTotalBalance.setText(String.format("%,.2f", soldeTotal));

        double evolution = 12.5;
        String evolutionText = (evolution >= 0 ? "+" : "") + String.format("%.1f", evolution) + "%";
        lblMonthlyChange.setText(evolutionText);
        lblMonthlyChange.setStyle(evolution >= 0 ?
                "-fx-text-fill: #10b981; -fx-font-weight: 600;" :
                "-fx-text-fill: #ef4444; -fx-font-weight: 600;");
    }

    private void displayWallets(List<Wallet> wallets) {
        walletsContainer.getChildren().clear();

        int colorIndex = 0;
        for (Wallet wallet : wallets) {
            VBox walletCard = createWalletCard(wallet, colorIndex % 5 + 1);
            walletsContainer.getChildren().add(walletCard);
            colorIndex++;
        }
    }

    private VBox createWalletCard(Wallet wallet, int colorIndex) {
        VBox card = new VBox(10);
        card.getStyleClass().addAll("wallet-card", "wallet-card-" + colorIndex);
        card.setPadding(new Insets(20));
        card.setAlignment(Pos.TOP_LEFT);

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(10);

        Label typeLabel = new Label(wallet.getDevise().toString());
        typeLabel.getStyleClass().add("wallet-type");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label statusLabel = new Label(wallet.getStatut().toString());
        statusLabel.getStyleClass().addAll("status-badge",
                wallet.getStatut() == WalletStatut.ACTIVE ? "status-badge-active" :
                        wallet.getStatut() == WalletStatut.DRAFT ? "status-badge-draft" :
                                wallet.getStatut() == WalletStatut.SUSPENDED ? "status-badge-suspended" :
                                        "status-badge-closed");

        header.getChildren().addAll(typeLabel, spacer, statusLabel);

        Label titleLabel = new Label(wallet.getNom_proprietaire());
        titleLabel.getStyleClass().add("wallet-title");

        HBox balanceBox = new HBox(5);
        balanceBox.setAlignment(Pos.CENTER_LEFT);

        Label balanceLabel = new Label(String.format("%,.2f", wallet.getSolde()));
        balanceLabel.getStyleClass().add("wallet-balance");

        Label currencyLabel = new Label(wallet.getDevise().toString());
        currencyLabel.getStyleClass().add("wallet-currency");

        balanceBox.getChildren().addAll(balanceLabel, currencyLabel);

        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.getStyleClass().add("wallet-footer");
        footer.setPadding(new Insets(10, 0, 0, 0));

        Label dateLabel = new Label("Créé le " + wallet.getDate_creation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        dateLabel.getStyleClass().add("wallet-footer-label");

        footer.getChildren().add(dateLabel);

        card.getChildren().addAll(header, titleLabel, balanceBox, footer);

        card.setOnMouseClicked(event -> {
            openWalletDetails(wallet);
        });

        return card;
    }

    private void displayRecentTransactions(List<Transaction> transactions) {
        transactionsContainer.getChildren().clear();

        int count = 0;
        for (Transaction transaction : transactions) {
            if (count >= 5) break;

            HBox transactionItem = createTransactionItem(transaction);
            transactionsContainer.getChildren().add(transactionItem);
            count++;
        }
    }

    private HBox createTransactionItem(Transaction transaction) {
        HBox item = new HBox(15);
        item.getStyleClass().addAll("transaction-item",
                transaction.getType().equals("DEPOT") ? "transaction-item-depot" :
                        transaction.getType().equals("RETRAIT") ? "transaction-item-retrait" :
                                "transaction-item-transfert");
        item.setPadding(new Insets(12, 15, 12, 15));
        item.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label(transaction.getType().equals("DEPOT") ? "↓" :
                transaction.getType().equals("RETRAIT") ? "↑" : "↔");
        iconLabel.getStyleClass().addAll("transaction-icon",
                transaction.getType().equals("DEPOT") ? "transaction-icon-depot" :
                        transaction.getType().equals("RETRAIT") ? "transaction-icon-retrait" :
                                "transaction-icon-transfert");

        VBox detailsBox = new VBox(3);
        detailsBox.setAlignment(Pos.CENTER_LEFT);

        Label descLabel = new Label(getTransactionDescription(transaction));
        descLabel.getStyleClass().add("transaction-description");

        Label dateLabel = new Label(transaction.getDate_transaction().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")));
        dateLabel.getStyleClass().add("transaction-date");

        detailsBox.getChildren().addAll(descLabel, dateLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox amountBox = new VBox(3);
        amountBox.setAlignment(Pos.CENTER_RIGHT);

        Label amountLabel = new Label((transaction.getType().equals("DEPOT") ? "+" : "-") +
                String.format("%,.2f", transaction.getMontant()));
        amountLabel.getStyleClass().addAll("transaction-amount",
                transaction.getType().equals("DEPOT") ? "transaction-amount-positive" : "transaction-amount-negative");

        amountBox.getChildren().add(amountLabel);

        item.getChildren().addAll(iconLabel, detailsBox, spacer, amountBox);

        return item;
    }

    private String getTransactionDescription(Transaction transaction) {
        Wallet wallet = walletService.getWalletById(transaction.getId_wallet());
        String walletName = wallet != null ? wallet.getNom_proprietaire() : "Wallet";

        switch (transaction.getType()) {
            case "DEPOT": return "Dépôt - " + walletName;
            case "RETRAIT": return "Retrait - " + walletName;
            case "TRANSFERT": return "Transfert - " + walletName;
            default: return "Transaction - " + walletName;
        }
    }

    private void setupCharts() {
        Map<String, Double> deviseTotal = new HashMap<>();
        walletService.getAllWallets().forEach(w -> {
            String devise = w.getDevise().toString();
            deviseTotal.put(devise, deviseTotal.getOrDefault(devise, 0.0) + w.getSolde());
        });

        pieChartWallets.getData().clear();
        deviseTotal.forEach((devise, montant) -> {
            PieChart.Data slice = new PieChart.Data(devise + " " + String.format("%,.0f", montant), montant);
            pieChartWallets.getData().add(slice);
        });
    }

    @FXML
    private void handleAddWallet() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Wallet/wallet.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Nouveau Wallet");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            loadDashboardData();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le formulaire");
        }
    }

    @FXML
    private void handleNewTransaction() {
        if (walletConnecte == null && !walletService.getAllWallets().isEmpty()) {
            walletConnecte = walletService.getAllWallets().get(0);
        }

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
            stage.setTitle("Nouvelle Transaction");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            loadDashboardData();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le formulaire");
        }
    }

    private void openWalletDetails(Wallet wallet) {
        this.walletConnecte = wallet;
        handleNewTransaction();
    }

    @FXML
    private void handleRefresh() {
        loadDashboardData();
        showAlert(Alert.AlertType.INFORMATION, "Succès", "Données actualisées");
    }

    @FXML
    private void handleQuitter() {
        System.exit(0);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}