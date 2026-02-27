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
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class ChequierController implements Initializable {

    @FXML private TextField txtNumeroCheque;
    @FXML private TextField txtMontant;
    @FXML private TextField txtBeneficiaire;
    @FXML private TableView<Cheque> tableViewCheques;
    @FXML private TableColumn<Cheque, String> colNumero;
    @FXML private TableColumn<Cheque, Double> colMontant;
    @FXML private TableColumn<Cheque, String> colBeneficiaire;
    @FXML private TableColumn<Cheque, String> colDateEmission;
    @FXML private TableColumn<Cheque, String> colStatut;
    @FXML private TableColumn<Cheque, Void> colAction;

    private ChequeService chequeService;
    private ObservableList<Cheque> chequeList;
    private Wallet walletConnecte;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        chequeService = new ChequeService();
        chequeList = FXCollections.observableArrayList();
        setupTableColumns();
    }

    public void setWalletConnecte(Wallet wallet) {
        this.walletConnecte = wallet;
        loadCheques();
    }

    private void setupTableColumns() {
        colNumero.setCellValueFactory(new PropertyValueFactory<>("numero_cheque"));
        colMontant.setCellValueFactory(new PropertyValueFactory<>("montant"));
        colBeneficiaire.setCellValueFactory(new PropertyValueFactory<>("beneficiaire"));
        colDateEmission.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getDate_emission().format(dateFormatter))
        );
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        // Ajouter un bouton "Présenter" pour les chèques émis
        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button btnPresenter = new Button("Présenter");
            {
                btnPresenter.setOnAction(event -> {
                    Cheque cheque = getTableView().getItems().get(getIndex());
                    presenterCheque(cheque);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Cheque cheque = getTableView().getItems().get(getIndex());
                    if (cheque.getStatut().equals("EMIS") || cheque.getStatut().equals("RESERVE")) {
                        setGraphic(btnPresenter);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });

        tableViewCheques.setItems(chequeList);
    }

    private void loadCheques() {
        chequeList.clear();
        chequeList.addAll(chequeService.getChequesByWallet(walletConnecte.getId_wallet()));
    }

    @FXML
    private void handleEmettre() {
        if (!validateInputs()) return;

        try {
            String numero = txtNumeroCheque.getText().trim();
            double montant = Double.parseDouble(txtMontant.getText().trim().replace(",", "."));
            String beneficiaire = txtBeneficiaire.getText().trim();

            Cheque cheque = new Cheque(numero, montant, walletConnecte.getId_wallet(), beneficiaire);

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

    private void presenterCheque(Cheque cheque) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Présentation du chèque");
        confirm.setHeaderText("Présenter le chèque n° " + cheque.getNumero_cheque());
        confirm.setContentText("Confirmer la présentation de ce chèque pour encaissement ?");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            if (chequeService.presenterCheque(cheque.getId_cheque())) {
                showAlert("Succès", "Chèque présenté avec succès");
                loadCheques();
            } else {
                showAlert("Erreur", "La présentation du chèque a échoué");
            }
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