package org.example.Controlleurs.WalletControlleur;

import org.example.Model.Wallet.ClassWallet.Wallet;
import org.example.Model.Wallet.ClassWallet.Transaction;
import org.example.Model.Wallet.EnumWallet.WalletDevise;
import org.example.Model.Wallet.EnumWallet.WalletStatut;
import org.example.Service.WalletService.WalletService;
import org.example.Service.WalletService.TransactionService;
import org.example.Service.WalletService.FraisBancaireService;
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
import javafx.scene.chart.*;
import javafx.collections.FXCollections;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import java.net.URL;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

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

    // ✅ Composants pour les stats
    @FXML private BarChart<String, Number> barChartInscriptions;
    @FXML private Label lblComptes2024;
    @FXML private Label lblComptes2025;
    @FXML private Label lblComptes2026;
    @FXML private ProgressBar progressObjectif;
    @FXML private Label lblObjectif;

    private WalletService walletService;
    private TransactionService transactionService;
    private Wallet walletConnecte;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        walletService = new WalletService();
        transactionService = new TransactionService();

        addLogo();
        lblDate.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy")));
        lblWelcome.setText("Bonjour, Administrateur");

        loadDashboardData();
        setupCharts();
        setupStatsInscriptions();
    }

    private void addLogo() {
        try {
            String logoPath = "C:/Users/Feryel Hajji/Downloads/Capture_d_écran_2026-02-16_154906-removebg-preview.png";
            File file = new File(logoPath);
            if (file.exists()) {
                Image logoImage = new Image(file.toURI().toString());
                ImageView logoView = new ImageView(logoImage);
                logoView.setFitHeight(50);
                logoView.setFitWidth(50);
                logoView.setPreserveRatio(true);

                HBox logoBox = new HBox(10);
                logoBox.setAlignment(Pos.CENTER_LEFT);

                VBox textBox = new VBox(-5);
                Label bankName = new Label("FinTrust");
                bankName.getStyleClass().add("logo-text");
                Label bankSub = new Label("BANKING SOLUTIONS");
                bankSub.getStyleClass().add("logo-subtext");
                textBox.getChildren().addAll(bankName, bankSub);

                logoBox.getChildren().addAll(logoView, textBox);
                logoContainer.getChildren().clear();
                logoContainer.getChildren().add(logoBox);
            } else {
                System.out.println("Logo non trouvé au chemin: " + logoPath);
                Label bankName = new Label("FinTrust");
                bankName.getStyleClass().add("logo-text");
                logoContainer.getChildren().add(bankName);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Label bankName = new Label("FinTrust");
            bankName.getStyleClass().add("logo-text");
            logoContainer.getChildren().add(bankName);
        }
    }

    private void loadDashboardData() {
        List<Wallet> wallets = walletService.getAllWallets();
        List<Transaction> transactions = transactionService.getAllTransactions();

        updateStatistics(wallets, transactions);
        displayWallets(wallets);
        displayRecentTransactions(transactions);
        lblLastUpdate.setText("Dernière mise à jour: " + LocalDate.now().format(dateFormatter) + " " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
    }

    private void updateStatistics(List<Wallet> wallets, List<Transaction> transactions) {
        lblTotalWallets.setText(String.valueOf(wallets.size()));
        lblTotalTransactions.setText(String.valueOf(transactions.size()));

        double soldeTotal = wallets.stream().mapToDouble(Wallet::getSolde).sum();
        lblTotalBalance.setText(String.format("%,.2f", soldeTotal));

        double evolution = calculerEvolutionMensuelle(transactions);
        String evolutionText = (evolution >= 0 ? "+" : "") + String.format("%.1f", evolution) + "%";
        lblMonthlyChange.setText(evolutionText);
        lblMonthlyChange.setStyle(evolution >= 0 ?
                "-fx-text-fill: #10b981; -fx-font-weight: 600;" :
                "-fx-text-fill: #ef4444; -fx-font-weight: 600;");
    }

    private double calculerEvolutionMensuelle(List<Transaction> transactions) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime debutMois = now.withDayOfMonth(1).withHour(0).withMinute(0);
        LocalDateTime debutMoisPrecedent = debutMois.minusMonths(1);

        double totalMoisActuel = transactions.stream()
                .filter(t -> t.getDate_transaction().isAfter(debutMois))
                .mapToDouble(Transaction::getMontant)
                .sum();

        double totalMoisPrecedent = transactions.stream()
                .filter(t -> t.getDate_transaction().isAfter(debutMoisPrecedent) &&
                        t.getDate_transaction().isBefore(debutMois))
                .mapToDouble(Transaction::getMontant)
                .sum();

        if (totalMoisPrecedent == 0) return 0;
        return ((totalMoisActuel - totalMoisPrecedent) / totalMoisPrecedent) * 100;
    }

    private void setupStatsInscriptions() {
        List<Wallet> wallets = walletService.getAllWallets();

        Map<Integer, Long> comptesParAnnee = wallets.stream()
                .collect(Collectors.groupingBy(
                        w -> w.getDateCreation().getYear(),
                        Collectors.counting()
                ));

        lblComptes2024.setText(String.valueOf(comptesParAnnee.getOrDefault(2024, 0L)));
        lblComptes2025.setText(String.valueOf(comptesParAnnee.getOrDefault(2025, 0L)));
        lblComptes2026.setText(String.valueOf(comptesParAnnee.getOrDefault(2026, 0L)));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Nouveaux comptes");

        comptesParAnnee.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    series.getData().add(new XYChart.Data<>(
                            String.valueOf(entry.getKey()),
                            entry.getValue()
                    ));
                });

        barChartInscriptions.getData().clear();
        barChartInscriptions.getData().add(series);
        barChartInscriptions.setAnimated(true);
        barChartInscriptions.setLegendVisible(false);

        long comptes2026 = comptesParAnnee.getOrDefault(2026, 0L);
        int objectif = 1000;
        double progression = Math.min((double) comptes2026 / objectif, 1.0);
        progressObjectif.setProgress(progression);
        lblObjectif.setText(comptes2026 + "/" + objectif + " comptes");
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

        Label statusLabel;
        if (wallet.isEstBloque()) {
            statusLabel = new Label("🔒 BLOQUÉ");
            statusLabel.getStyleClass().addAll("status-badge", "status-badge-blocked");
        } else {
            statusLabel = new Label(wallet.getStatut().toString());
            statusLabel.getStyleClass().addAll("status-badge",
                    wallet.getStatut() == WalletStatut.ACTIVE ? "status-badge-active" :
                            wallet.getStatut() == WalletStatut.DRAFT ? "status-badge-draft" :
                                    wallet.getStatut() == WalletStatut.SUSPENDED ? "status-badge-suspended" :
                                            "status-badge-closed");
        }

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

        // ✅ CORRIGÉ : Ouvre le détail du wallet, pas la transaction
        card.setOnMouseClicked(event -> openWalletDetails(wallet));

        return card;
    }

    // ✅ NOUVELLE MÉTHODE CORRIGÉE
    private void openWalletDetails(Wallet wallet) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Wallet/wallet_detail.fxml"));
            Parent root = loader.load();

            WalletDetailController controller = loader.getController();
            controller.setWallet(wallet);

            Stage stage = new Stage();
            stage.setTitle("Détails du wallet - " + wallet.getNom_proprietaire());
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'ouvrir les détails du wallet : " + e.getMessage());
        }
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

    // ✅ Méthodes pour la sidebar
    @FXML private void handleGestionClients() { ouvrirModule("Gestion des clients", "/Wallet/gestion_clients.fxml"); }
    @FXML private void handleGestionPublications() { ouvrirModule("Gestion des publications", "/Wallet/publications.fxml"); }
    @FXML private void handleGestionProduits() { ouvrirModule("Gestion des produits", "/Product/Admin/ListeProductGUI.fxml"); }
    @FXML public void handleGestionSubs() {ouvrirModule("Gestion des Abonners", "/Product/Admin/ListeSubProductGUI.fxml");}
    @FXML public void handleDashboardProduits() {ouvrirModule("Dashboard produits & Abonners", "/Product/Admin/AdminDashboardGUI.fxml");}
    @FXML private void handleGestionBudgets() { ouvrirModule("Gestion des budgets", "/Wallet/budgets.fxml"); }
    @FXML private void handleGestionLoans() { ouvrirModule("Gestion des prêts", "/Wallet/loans.fxml"); }
    @FXML private void handleGestionUsers() { ouvrirModule("Gestion des utilisateurs", "/Wallet/users.fxml"); }
    private void ouvrirModule(String titre, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle(titre);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.INFORMATION, "Module en développement",
                    "Le module " + titre + " sera bientôt disponible");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir " + titre);
        }
    }

    @FXML
    private void handleOpenRiskDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Wallet/RiskDashboard.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("🧠 Risk Intelligence Dashboard");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.INFORMATION, "Module en développement",
                    "Le Risk Dashboard sera bientôt disponible");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le Risk Dashboard");
        }
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
            setupCharts();
            setupStatsInscriptions();
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
            setupCharts();
            setupStatsInscriptions();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le formulaire");
        }
    }

    @FXML
    private void handleRefresh() {
        loadDashboardData();
        setupCharts();
        setupStatsInscriptions();
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

    @FXML
    private void handleGestionCheques() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Wallet/chequier_admin.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("📒 Administration des chèques");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la gestion des chèques");
        }
    }

    @FXML
    private void handleNavigation(ActionEvent event) {
        Button source = (Button) event.getSource();
        String text = source.getText();

        switch(text) {
            case "Accueil":
                showAlert(Alert.AlertType.INFORMATION, "Navigation", "Vous êtes déjà sur l'accueil");
                break;
            case "Wallets":
                walletsContainer.requestFocus();
                break;
            case "Transactions":
                transactionsContainer.requestFocus();
                break;
            case "Chéquiers":
                handleGestionCheques();
                break;
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Déconnexion");
        confirm.setHeaderText("Quitter votre session ?");
        confirm.setContentText("Voulez-vous vraiment vous déconnecter ?");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Wallet/Choice/ChoiceView.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de se déconnecter");
            }
        }
    }

    @FXML
    private void handleAbout() {
        showAlert(Alert.AlertType.INFORMATION, "À propos",
                "FinTrust - Haute Finance Numérique\nVersion 2.0\n© 2026 Tous droits réservés");
    }

    @FXML
    private void handleSouscrire() {
        showAlert(Alert.AlertType.INFORMATION, "Souscription",
                "Pour souscrire à nos services, veuillez contacter votre conseiller.");
    }

    @FXML
    private void handleContact() {
        showAlert(Alert.AlertType.INFORMATION, "Contact",
                "Support : support@fintrust.tn\nTél: +216 71 123 456");
    }

    @FXML
    private void handleSecurity() {
        showAlert(Alert.AlertType.INFORMATION, "Sécurité",
                "Connexion sécurisée avec authentification à deux facteurs\nDonnées chiffrées de bout en bout");
    }

    @FXML
    private void handleOpenWallet() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Wallet/wallet.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Gestion des Wallets");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la gestion des wallets");
        }
    }

    @FXML
    private void handleVoirRevenus() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Wallet/revenus.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("💰 Revenus FinTrust");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir l'interface des revenus");
        }
    }

    @FXML
    private void handleStatsRevenus() {
        try {
            FraisBancaireService fraisService = new FraisBancaireService();
            int idCompteBanque = fraisService.getIdCompteBanque();
            List<Transaction> transactions = transactionService.getTransactionsByWallet(idCompteBanque);

            double totalRetraits = 0, totalTransferts = 0, totalRejets = 0, totalAgios = 0;

            for (Transaction t : transactions) {
                String desc = t.getDescription() != null ? t.getDescription().toLowerCase() : "";
                if (desc.contains("retrait")) totalRetraits += t.getMontant();
                else if (desc.contains("transfert")) totalTransferts += t.getMontant();
                else if (desc.contains("rejet")) totalRejets += t.getMontant();
                else if (desc.contains("agios") || desc.contains("découvert")) totalAgios += t.getMontant();
            }

            double totalGeneral = transactions.stream().mapToDouble(Transaction::getMontant).sum();

            String stats = String.format(
                    "📊 STATISTIQUES DES REVENUS\n━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                            "💳 Frais de retrait:     %10.2f TND\n↗️ Frais de transfert:   %10.2f TND\n" +
                            "❌ Frais de rejet:       %10.2f TND\n💰 Agios sur découvert:  %10.2f TND\n" +
                            "📦 Autres frais:         %10.2f TND\n━━━━━━━━━━━━━━━━━━━━━━━━━━\n💎 TOTAL:                %10.2f TND",
                    totalRetraits, totalTransferts, totalRejets, totalAgios,
                    totalGeneral - (totalRetraits + totalTransferts + totalRejets + totalAgios),
                    totalGeneral
            );

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("📈 Statistiques FinTrust");
            alert.setHeaderText("Analyse des revenus bancaires");
            alert.setContentText(stats);
            alert.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de calculer les statistiques");
        }
    }
    @FXML
    private void handleStats() {
        try {
            // Ouvrir les statistiques générales
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Wallet/statistiques.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("📈 Statistiques FinTrust");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.INFORMATION, "Module en développement",
                    "Les statistiques détaillées seront bientôt disponibles");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir les statistiques");
        }
    }
    @FXML
    private void handleVoirComptesBloques() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Wallet/comptes_bloques.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("🔒 Comptes bloqués");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la liste des comptes bloqués");
        }
    }



}