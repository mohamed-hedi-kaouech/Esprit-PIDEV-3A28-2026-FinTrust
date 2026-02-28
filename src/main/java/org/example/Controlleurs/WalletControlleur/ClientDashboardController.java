package org.example.Controlleurs.WalletControlleur;

import org.example.Model.User.User;
import org.example.Model.Wallet.ClassWallet.Wallet;
import org.example.Model.Wallet.ClassWallet.Transaction;
import org.example.Model.Wallet.ClassWallet.ScoreConfiance;
import org.example.Service.WalletService.WalletService;
import org.example.Service.WalletService.TransactionService;
import org.example.Service.WalletService.ScoreConfianceService;
import java.io.IOException;
import org.example.Controlleurs.WalletControlleur.ConversionController;
import org.example.Controlleurs.WalletControlleur.ChequierClientController;
import org.example.Controlleurs.WalletControlleur.TransactionController;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleStringProperty;
import javafx.stage.Stage;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class ClientDashboardController implements Initializable {

    @FXML private Label lblWelcome;
    @FXML private Label lblSolde;
    @FXML private Label lblDevise;
    @FXML private Label lblWalletName;
    @FXML private Label lblPlafond;
    @FXML private Label lblDisponible;
    @FXML private Label lblStatutDecouvert;
    @FXML private Label lblDevisePlafond;
    @FXML private Label lblDeviseDispo;

    // Score de confiance
    @FXML private ProgressIndicator progressScore;
    @FXML private Label lblScore;
    @FXML private Label lblNiveau;
    @FXML private Label lblAnciennete;
    @FXML private Label lblTransactionsScore;
    @FXML private Label lblStabilite;
    @FXML private Label lblRecommandation;

    // TableView des transactions
    @FXML private TableView<Transaction> tableViewTransactions;
    @FXML private TableColumn<Transaction, String> colDate;
    @FXML private TableColumn<Transaction, String> colType;
    @FXML private TableColumn<Transaction, Double> colMontant;
    @FXML private TableColumn<Transaction, String> colDevise;
    @FXML private TableColumn<Transaction, String> colBeneficiaire;
    @FXML private TableColumn<Transaction, String> colDescription;
    @FXML private TableColumn<Transaction, String> colStatut;

    private WalletService walletService;
    private TransactionService transactionService;
    private ScoreConfianceService scoreService;
    private Wallet clientWallet;
    private ObservableList<Transaction> transactionList;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private User currentUser;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        walletService = new WalletService();
        transactionService = new TransactionService();
        scoreService = new ScoreConfianceService();
        transactionList = FXCollections.observableArrayList();

        setupTableColumns();

        // Récupérer le wallet de l'utilisateur connecté
        if (currentUser != null) {
            chargerWalletUtilisateur();
        } else {
            lblWelcome.setText("Bienvenue dans votre espace client");
        }
    }

    private void chargerWalletUtilisateur() {
        List<Wallet> wallets = walletService.getAllWallets();
        clientWallet = wallets.stream()
                .filter(w -> w.getIdUser() == currentUser.getId())
                .findFirst()
                .orElse(null);

        if (clientWallet != null) {
            afficherInfosClient();
            chargerTransactions();
            afficherScoreConfiance();
            lblWelcome.setText("Bonjour " + clientWallet.getNom_proprietaire());
        } else {
            lblWelcome.setText("Aucun wallet trouvé");
        }
    }

    // ✅ Méthode appelée depuis l'extérieur pour passer l'utilisateur
    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (lblWelcome != null) {
            chargerWalletUtilisateur();
        }
    }

    private void setupTableColumns() {
        colDate.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDate_transaction().format(dateFormatter)));

        colType.setCellValueFactory(new PropertyValueFactory<>("type"));

        colMontant.setCellValueFactory(new PropertyValueFactory<>("montant"));
        colMontant.setCellFactory(column -> new TableCell<Transaction, Double>() {
            @Override
            protected void updateItem(Double montant, boolean empty) {
                super.updateItem(montant, empty);
                if (empty || montant == null) {
                    setText(null);
                } else {
                    Transaction t = getTableView().getItems().get(getIndex());
                    String signe = t.getType().equals("DEPOT") ? "+" : "-";
                    setText(signe + " " + String.format("%.2f", montant));

                    if (t.getType().equals("DEPOT")) {
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    }
                }
            }
        });

        colDevise.setCellValueFactory(cellData ->
                new SimpleStringProperty(clientWallet != null ? clientWallet.getDevise().toString() : "TND"));

        colBeneficiaire.setCellValueFactory(cellData -> {
            Transaction t = cellData.getValue();
            String info = "-";

            if (t.getType().equals("TRANSFERT")) {
                if (clientWallet != null && t.getId_wallet() == clientWallet.getId_wallet()) {
                    info = "Vers: " + trouverDestinataire(t);
                } else {
                    info = "De: " + trouverExpediteur(t);
                }
            } else if (t.getType().equals("DEPOT")) {
                info = "Dépôt";
            } else if (t.getType().equals("RETRAIT")) {
                info = "Retrait";
            }
            return new SimpleStringProperty(info);
        });

        colDescription.setCellValueFactory(cellData -> {
            String desc = cellData.getValue().getDescription();
            return new SimpleStringProperty(desc != null ? desc : "-");
        });

        colStatut.setCellValueFactory(cellData ->
                new SimpleStringProperty("✅ Complété"));

        colStatut.setCellFactory(column -> new TableCell<Transaction, String>() {
            @Override
            protected void updateItem(String statut, boolean empty) {
                super.updateItem(statut, empty);
                if (empty || statut == null) {
                    setText(null);
                } else {
                    setText(statut);
                    setStyle("-fx-text-fill: #27ae60;");
                }
            }
        });

        tableViewTransactions.setItems(transactionList);
    }

    private String trouverDestinataire(Transaction t) {
        String desc = t.getDescription();
        if (desc != null && desc.contains("vers")) {
            String[] parts = desc.split("vers");
            return parts.length > 1 ? parts[1].trim() : "Compte externe";
        }
        return "Compte externe";
    }

    private String trouverExpediteur(Transaction t) {
        String desc = t.getDescription();
        if (desc != null && desc.contains("de")) {
            String[] parts = desc.split("de");
            return parts.length > 1 ? parts[1].trim() : "Compte externe";
        }
        return "Compte externe";
    }

    private void chargerTransactions() {
        if (clientWallet == null) return;
        transactionList.clear();
        List<Transaction> transactions = transactionService
                .getTransactionsByWallet(clientWallet.getId_wallet());
        transactionList.addAll(transactions);
    }

    private void afficherInfosClient() {
        if (clientWallet == null) return;

        lblWalletName.setText(clientWallet.getNom_proprietaire());
        lblSolde.setText(String.format("%.2f", clientWallet.getSolde()));
        lblDevise.setText(clientWallet.getDevise().toString());

        if (lblPlafond != null) {
            lblPlafond.setText(String.format("%.2f", clientWallet.getPlafondDecouvert()));
            lblDevisePlafond.setText(clientWallet.getDevise().toString());
        }

        if (lblDisponible != null) {
            double disponible = clientWallet.getSoldeDisponible();
            lblDisponible.setText(String.format("%.2f", disponible));
            lblDeviseDispo.setText(clientWallet.getDevise().toString());
        }

        if (lblStatutDecouvert != null) {
            if (clientWallet.isEnDecouvert()) {
                lblStatutDecouvert.setText("⚠️ Compte à découvert");
                lblStatutDecouvert.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            } else {
                lblStatutDecouvert.setText("✅ Compte en positif");
                lblStatutDecouvert.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            }
        }
    }

    private void afficherScoreConfiance() {
        if (clientWallet == null) return;

        List<Transaction> transactions = transactionService
                .getTransactionsByWallet(clientWallet.getId_wallet());

        ScoreConfiance score = scoreService.calculerScore(clientWallet, transactions);

        double progression = score.getScoreGlobal() / 100.0;
        progressScore.setProgress(progression);

        lblScore.setText(score.getScoreGlobal() + "/100");
        lblNiveau.setText(score.getNiveau());
        lblAnciennete.setText(score.getAnciennete() + "/15");
        lblTransactionsScore.setText(score.getTransactions() + "/20");
        lblStabilite.setText(score.getStabilite() + "/25");
        lblRecommandation.setText("💡 " + score.getRecommandation());

        String couleur = determinerCouleurScore(score.getScoreGlobal());
        lblScore.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: " + couleur + ";");
    }

    private String determinerCouleurScore(int score) {
        if (score >= 80) return "#4CAF50";
        if (score >= 60) return "#8BC34A";
        if (score >= 40) return "#FFC107";
        if (score >= 20) return "#FF9800";
        return "#F44336";
    }

    @FXML
    private void handleNewTransaction() {
        if (clientWallet == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Wallet/transaction.fxml"));
            Parent root = loader.load();

            TransactionController controller = loader.getController();
            controller.setWalletConnecte(clientWallet);

            Stage stage = new Stage();
            stage.setTitle("Nouvelle Transaction");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Recharger les données
            clientWallet = walletService.getWalletById(clientWallet.getId_wallet());
            afficherInfosClient();
            chargerTransactions();
            afficherScoreConfiance();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le formulaire de transaction");
        }
    }

    @FXML
    private void handleOuvrirConvertisseur() {
        if (clientWallet == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Wallet/conversion.fxml"));
            Parent root = loader.load();

            ConversionController controller = loader.getController();
            controller.setWalletConnecte(clientWallet);

            Stage stage = new Stage();
            stage.setTitle("Convertisseur de devises");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le convertisseur");
        }
    }

    @FXML
    private void handleOuvrirChequier() {
        if (clientWallet == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Wallet/chequier_client.fxml"));
            Parent root = loader.load();

            ChequierClientController controller = loader.getController();
            controller.setWalletConnecte(clientWallet);

            Stage stage = new Stage();
            stage.setTitle("📒 Mon Chéquier");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le chéquier");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setClientWallet(Wallet wallet) {
        this.clientWallet = wallet;
        afficherInfosClient();
        chargerTransactions();
        afficherScoreConfiance();
    }
    @FXML
    private void handleLogout() {
        try {
            // Retourner à la page de login client
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Wallet/client_login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) lblWelcome.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Connexion Wallet Client");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de se déconnecter: " + e.getMessage());
        }
    }
}