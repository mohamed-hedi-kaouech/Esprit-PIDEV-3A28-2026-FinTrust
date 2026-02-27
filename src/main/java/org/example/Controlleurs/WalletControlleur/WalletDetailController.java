package org.example.Controlleurs.WalletControlleur;

import org.example.Model.Wallet.ClassWallet.Wallet;
import org.example.Model.Wallet.ClassWallet.Transaction;
import org.example.Service.WalletService.TransactionService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class WalletDetailController implements Initializable {

    @FXML private Label lblNomProprietaire;
    @FXML private Label lblSolde;
    @FXML private Label lblDevise;
    @FXML private Label lblEmail;
    @FXML private Label lblTelephone;
    @FXML private Label lblDateCreation;
    @FXML private Label lblStatut;
    @FXML private Label lblPlafond;
    @FXML private Label lblDecouvert;
    @FXML private Label lblTotalDepots;
    @FXML private Label lblTotalRetraits;
    @FXML private Label lblTotalTransferts;
    @FXML private Label lblSoldeMoyen;

    @FXML private ComboBox<Integer> comboMois;
    @FXML private ComboBox<Integer> comboAnnee;
    @FXML private TableView<Transaction> tableViewTransactions;
    @FXML private TableColumn<Transaction, LocalDateTime> colDate;  // ← LocalDateTime
    @FXML private TableColumn<Transaction, String> colType;
    @FXML private TableColumn<Transaction, Double> colMontant;
    @FXML private TableColumn<Transaction, String> colDescription;
    @FXML private TableColumn<Transaction, String> colStatut;

    private Wallet wallet;
    private TransactionService transactionService;
    private ObservableList<Transaction> allTransactions;
    private ObservableList<Transaction> filteredTransactions;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        transactionService = new TransactionService();
        allTransactions = FXCollections.observableArrayList();
        filteredTransactions = FXCollections.observableArrayList();

        setupTableColumns();
        setupMonthYearCombos();
    }

    private void setupTableColumns() {
        // ✅ CORRIGÉ: Utiliser PropertyValueFactory pour les champs simples
        colDate.setCellValueFactory(new PropertyValueFactory<>("date_transaction"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colMontant.setCellValueFactory(new PropertyValueFactory<>("montant"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));

        // ✅ Formatage de la date
        colDate.setCellFactory(column -> new TableCell<Transaction, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.format(dateFormatter));
                }
            }
        });

        // ✅ Formatage du montant avec couleur selon le type
        colMontant.setCellFactory(column -> new TableCell<Transaction, Double>() {
            @Override
            protected void updateItem(Double montant, boolean empty) {
                super.updateItem(montant, empty);
                if (empty || montant == null) {
                    setText(null);
                } else {
                    Transaction t = getTableView().getItems().get(getIndex());
                    String signe = t.getType().equals("DEPOT") ? "+" : "-";
                    setText(signe + " " + String.format("%,.2f", Math.abs(montant)) + " €");

                    if (t.getType().equals("DEPOT")) {
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    }
                }
            }
        });

        // ✅ Statut par défaut
        colStatut.setCellValueFactory(cellData -> {
            Transaction t = cellData.getValue();
            String statut = "Complété";
            return new javafx.beans.property.SimpleStringProperty(statut);
        });
    }

    private void setupMonthYearCombos() {
        for (int i = 1; i <= 12; i++) {
            comboMois.getItems().add(i);
        }

        int currentYear = LocalDate.now().getYear();
        for (int i = currentYear - 5; i <= currentYear + 1; i++) {
            comboAnnee.getItems().add(i);
        }

        comboMois.setValue(LocalDate.now().getMonthValue());
        comboAnnee.setValue(currentYear);
    }

    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
        afficherInfosWallet();
        chargerTransactions();
    }

    private void afficherInfosWallet() {
        lblNomProprietaire.setText(wallet.getNom_proprietaire());
        lblSolde.setText(String.format("%,.2f", wallet.getSolde()));
        lblDevise.setText(wallet.getDevise().toString());
        lblEmail.setText(wallet.getEmail() != null ? wallet.getEmail() : "Non renseigné");
        lblTelephone.setText(wallet.getTelephone() != null ? wallet.getTelephone() : "Non renseigné");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        lblDateCreation.setText(wallet.getDate_creation().format(formatter));

        lblStatut.setText(wallet.getStatut().toString());
        lblPlafond.setText(String.format("%,.2f", wallet.getPlafondDecouvert()));

        if (wallet.isEnDecouvert()) {
            lblDecouvert.setText("⚠️ OUI (" + String.format("%,.2f", Math.abs(wallet.getSolde())) + "€)");
            lblDecouvert.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        } else {
            lblDecouvert.setText("✅ NON");
            lblDecouvert.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
        }
    }

    private void chargerTransactions() {
        allTransactions.clear();
        List<Transaction> transactions = transactionService.getTransactionsByWallet(wallet.getIdWallet());
        allTransactions.addAll(transactions);

        calculerStatistiques(transactions);
        filtrerTransactions();
    }

    private void calculerStatistiques(List<Transaction> transactions) {
        double totalDepots = transactions.stream()
                .filter(t -> "DEPOT".equals(t.getType()))
                .mapToDouble(Transaction::getMontant)
                .sum();

        double totalRetraits = transactions.stream()
                .filter(t -> "RETRAIT".equals(t.getType()))
                .mapToDouble(Transaction::getMontant)
                .sum();

        double totalTransferts = transactions.stream()
                .filter(t -> "TRANSFERT".equals(t.getType()))
                .mapToDouble(Transaction::getMontant)
                .sum();

        double soldeMoyen = transactions.stream()
                .mapToDouble(Transaction::getMontant)
                .average().orElse(0);

        lblTotalDepots.setText(String.format("%,.2f €", totalDepots));
        lblTotalRetraits.setText(String.format("%,.2f €", Math.abs(totalRetraits)));
        lblTotalTransferts.setText(String.format("%,.2f €", Math.abs(totalTransferts)));
        lblSoldeMoyen.setText(String.format("%,.2f €", soldeMoyen));
    }

    @FXML
    private void filtrerTransactions() {
        Integer mois = comboMois.getValue();
        Integer annee = comboAnnee.getValue();

        if (mois == null || annee == null) {
            tableViewTransactions.setItems(allTransactions);
            return;
        }

        filteredTransactions.clear();

        List<Transaction> filtered = allTransactions.stream()
                .filter(t -> {
                    LocalDateTime date = t.getDate_transaction();
                    return date.getMonthValue() == mois && date.getYear() == annee;
                })
                .collect(Collectors.toList());

        filteredTransactions.addAll(filtered);
        tableViewTransactions.setItems(filteredTransactions);

        calculerStatistiques(filtered);
    }

    @FXML
    private void exporterPDF() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter le rapport PDF");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf")
        );
        fileChooser.setInitialFileName("rapport_wallet_" + wallet.getIdWallet() + ".pdf");

        File file = fileChooser.showSaveDialog(lblNomProprietaire.getScene().getWindow());

        if (file != null) {
            try {
                // ✅ DÉCOMMENTEZ CETTE LIGNE
                ReportGenerator.exportWalletReport(wallet, filteredTransactions, file.getAbsolutePath());

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setHeaderText(null);
                alert.setContentText("Rapport PDF généré avec succès !\n" + file.getAbsolutePath());
                alert.showAndWait();

                // ✅ Optionnel : ouvrir le fichier automatiquement
                ouvrirFichier(file);

            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Erreur", "Erreur lors de l'export : " + e.getMessage());
            }
        }
    }

    // ✅ Méthode pour ouvrir le fichier automatiquement
    private void ouvrirFichier(File file) {
        try {
            java.awt.Desktop.getDesktop().open(file);
        } catch (Exception e) {
            System.out.println("Impossible d'ouvrir le fichier automatiquement");
        }
    }

    @FXML
    private void exporterCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter en CSV");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers CSV", "*.csv")
        );
        fileChooser.setInitialFileName("transactions_wallet_" + wallet.getIdWallet() + ".csv");

        File file = fileChooser.showSaveDialog(lblNomProprietaire.getScene().getWindow());

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                writer.println("Date,Type,Montant,Description");

                for (Transaction t : filteredTransactions) {
                    writer.printf("%s,%s,%.2f,%s%n",
                            t.getDate_transaction().format(dateFormatter),
                            t.getType(),
                            t.getMontant(),
                            t.getDescription() != null ? t.getDescription() : ""
                    );
                }

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setHeaderText(null);
                alert.setContentText("Export CSV réussi !");
                alert.showAndWait();

            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Erreur", "Erreur lors de l'export CSV");
            }
        }
    }

    @FXML
    private void fermer() {
        Stage stage = (Stage) lblNomProprietaire.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}