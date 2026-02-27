package org.example.Controlleurs.WalletControlleur;

import org.example.Model.Wallet.ClassWallet.Cheque;
import org.example.Model.Wallet.ClassWallet.Wallet;
import org.example.Service.WalletService.ChequeService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleStringProperty;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.List;

public class ChequierClientController implements Initializable {

    @FXML private TextField txtNumeroCheque;
    @FXML private TextField txtMontant;
    @FXML private TextField txtBeneficiaire;
    @FXML private TableView<Cheque> tableViewCheques;
    @FXML private TableColumn<Cheque, String> colNumero;
    @FXML private TableColumn<Cheque, Double> colMontant;
    @FXML private TableColumn<Cheque, String> colBeneficiaire;
    @FXML private TableColumn<Cheque, String> colDateEmission;
    @FXML private TableColumn<Cheque, String> colStatut;
    @FXML private TableColumn<Cheque, String> colMotif;  // ← NOUVELLE COLONNE

    private ChequeService chequeService;
    private ObservableList<Cheque> chequeList;
    private Wallet clientWallet;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        chequeService = new ChequeService();
        chequeList = FXCollections.observableArrayList();
        setupTableColumns();
    }

    public void setWalletConnecte(Wallet wallet) {
        this.clientWallet = wallet;
        loadCheques();
    }

    private void setupTableColumns() {
        colNumero.setCellValueFactory(new PropertyValueFactory<>("numero_cheque"));
        colMontant.setCellValueFactory(new PropertyValueFactory<>("montant"));
        colBeneficiaire.setCellValueFactory(new PropertyValueFactory<>("beneficiaire"));
        colDateEmission.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getDate_emission().format(dateFormatter))
        );
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        // ✅ Nouvelle colonne pour le motif de rejet
        colMotif.setCellValueFactory(cellData -> {
            String motif = cellData.getValue().getMotif_rejet();
            if (motif != null && !motif.isEmpty()) {
                return new SimpleStringProperty(motif);
            }
            return new SimpleStringProperty("-");
        });

        // ✅ Style spécial pour les différents statuts
        colStatut.setCellFactory(column -> new TableCell<Cheque, String>() {
            @Override
            protected void updateItem(String statut, boolean empty) {
                super.updateItem(statut, empty);
                if (empty || statut == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(statut);
                    if (statut.equals("REJETE")) {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    } else if (statut.equals("PAYE")) {
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    } else if (statut.equals("RESERVE")) {
                        setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;");
                    }
                }
            }
        });

        tableViewCheques.setItems(chequeList);
    }

    private void loadCheques() {
        if (clientWallet != null) {
            chequeList.clear();
            List<Cheque> cheques = chequeService.getChequesByWallet(clientWallet.getId_wallet());
            chequeList.addAll(cheques);
            System.out.println("📊 " + cheques.size() + " chèques chargés pour le client");

            // Debug : afficher les motifs de rejet
            for (Cheque c : cheques) {
                if (c.getStatut().equals("REJETE")) {
                    System.out.println("   ❌ Chèque rejeté: " + c.getNumero_cheque() +
                            " - Motif: " + c.getMotif_rejet());
                }
            }
        }
    }

    @FXML
    private void handleEmettre() {
        if (!validateInputs()) return;

        try {
            String numero = txtNumeroCheque.getText().trim();
            double montant = Double.parseDouble(txtMontant.getText().trim().replace(",", "."));
            String beneficiaire = txtBeneficiaire.getText().trim();

            Cheque cheque = new Cheque(numero, montant, clientWallet.getId_wallet(), beneficiaire);

            if (chequeService.emettreCheque(cheque)) {
                showAlert("Succès", "Chèque émis avec succès");
                loadCheques();
                clearForm();
            } else {
                showAlert("Erreur", "L'émission du chèque a échoué");
            }

        } catch (NumberFormatException e) {
            showAlert("Erreur", "Montant invalide");
        }
    }

    private boolean validateInputs() {
        if (txtNumeroCheque.getText().trim().isEmpty()) {
            showAlert("Erreur", "Numéro de chèque obligatoire");
            return false;
        }
        if (txtMontant.getText().trim().isEmpty()) {
            showAlert("Erreur", "Montant obligatoire");
            return false;
        }
        try {
            Double.parseDouble(txtMontant.getText().trim().replace(",", "."));
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Montant invalide");
            return false;
        }
        if (txtBeneficiaire.getText().trim().isEmpty()) {
            showAlert("Erreur", "Bénéficiaire obligatoire");
            return false;
        }
        return true;
    }

    private void clearForm() {
        txtNumeroCheque.clear();
        txtMontant.clear();
        txtBeneficiaire.clear();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}