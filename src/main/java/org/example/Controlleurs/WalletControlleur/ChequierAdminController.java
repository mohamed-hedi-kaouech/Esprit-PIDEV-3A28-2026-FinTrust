package org.example.Controlleurs.WalletControlleur;

import org.example.Model.Wallet.ClassWallet.Cheque;
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
import java.util.List;
import java.util.ResourceBundle;
import javafx.scene.layout.HBox;
import java.util.Optional;

public class ChequierAdminController implements Initializable {

    @FXML private TableView<Cheque> tableViewCheques;
    @FXML private TableColumn<Cheque, String> colNumero;
    @FXML private TableColumn<Cheque, Double> colMontant;
    @FXML private TableColumn<Cheque, String> colBeneficiaire;
    @FXML private TableColumn<Cheque, String> colProprietaire;
    @FXML private TableColumn<Cheque, String> colDateEmission;
    @FXML private TableColumn<Cheque, String> colStatut;
    @FXML private TableColumn<Cheque, Void> colAction;

    private ChequeService chequeService;
    private ObservableList<Cheque> chequeList;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        System.out.println("\n=== INITIALISATION ADMIN CHÈQUES ===");
        chequeService = new ChequeService();
        chequeList = FXCollections.observableArrayList();

        System.out.println("1️⃣ Configuration des colonnes...");
        setupTableColumns();

        System.out.println("2️⃣ Chargement des chèques...");
        loadAllCheques();

        System.out.println("3️⃣ Liaison de la liste à la TableView...");
        tableViewCheques.setItems(chequeList);

        System.out.println("4️⃣ Rafraîchissement de la TableView...");
        tableViewCheques.refresh();

        System.out.println("=== FIN INITIALISATION ===\n");
    }

    private void setupTableColumns() {
        colNumero.setCellValueFactory(new PropertyValueFactory<>("numero_cheque"));
        colMontant.setCellValueFactory(new PropertyValueFactory<>("montant"));
        colBeneficiaire.setCellValueFactory(new PropertyValueFactory<>("beneficiaire"));

        colProprietaire.setCellValueFactory(cellData -> {
            String nom = cellData.getValue().getNomProprietaire();
            if (nom != null && !nom.isEmpty()) {
                return new SimpleStringProperty(nom);
            }
            return new SimpleStringProperty("Wallet " + cellData.getValue().getId_wallet());
        });

        colDateEmission.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getDate_emission().format(dateFormatter))
        );
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button btnPresenter = new Button("✅ Présenter");
            private final Button btnRejeter = new Button("❌ Rejeter");
            {
                btnPresenter.setOnAction(event -> {
                    Cheque cheque = getTableView().getItems().get(getIndex());
                    presenterCheque(cheque);
                });
                btnPresenter.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");

                btnRejeter.setOnAction(event -> {
                    Cheque cheque = getTableView().getItems().get(getIndex());
                    rejeterCheque(cheque);
                });
                btnRejeter.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Cheque cheque = getTableView().getItems().get(getIndex());
                    HBox box = new HBox(5);
                    if (cheque.getStatut().equals("EMIS") || cheque.getStatut().equals("RESERVE")) {
                        box.getChildren().addAll(btnPresenter, btnRejeter);
                        setGraphic(box);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });
    }

    private void loadAllCheques() {
        System.out.println("📥 Appel à chequeService.getAllCheques()...");
        List<Cheque> cheques = chequeService.getAllCheques();

        System.out.println("📊 Résultat reçu: " + cheques.size() + " chèques");

        if (cheques.isEmpty()) {
            System.out.println("⚠️ Aucun chèque trouvé dans la base !");
        } else {
            System.out.println("📋 Détail des chèques reçus:");
            for (Cheque c : cheques) {
                System.out.println("   - ID: " + c.getId_cheque() +
                        " | N°: " + c.getNumero_cheque() +
                        " | Montant: " + c.getMontant() +
                        " | Bénéficiaire: " + c.getBeneficiaire() +
                        " | Propriétaire: " + c.getNomProprietaire());
            }
        }

        chequeList.clear();
        chequeList.addAll(cheques);
        System.out.println("✅ Liste mise à jour avec " + chequeList.size() + " éléments");
    }

    private void presenterCheque(Cheque cheque) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Présentation");
        confirm.setHeaderText("Présenter le chèque n° " + cheque.getNumero_cheque());
        confirm.setContentText("Confirmer l'encaissement de ce chèque ?");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            if (chequeService.presenterCheque(cheque.getId_cheque())) {
                showAlert("Succès", "Chèque présenté avec succès");
                loadAllCheques();
                tableViewCheques.refresh();
            } else {
                showAlert("Erreur", "Échec de la présentation");
            }
        }
    }

    private void rejeterCheque(Cheque cheque) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Rejet");
        dialog.setHeaderText("Motif du rejet");
        dialog.setContentText("Raison :");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(motif -> {
            if (chequeService.rejeterCheque(cheque.getId_cheque(), motif)) {
                showAlert("Succès", "Chèque rejeté");
                loadAllCheques();
                tableViewCheques.refresh();
            } else {
                showAlert("Erreur", "Échec du rejet");
            }
        });
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}