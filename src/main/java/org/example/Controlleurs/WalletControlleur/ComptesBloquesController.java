package org.example.Controlleurs.WalletControlleur;

import org.example.Model.Wallet.ClassWallet.Wallet;
import org.example.Service.WalletService.WalletService;
import org.example.Service.WalletService.EmailService;
import org.example.Service.WalletService.SmsService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.layout.HBox;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class ComptesBloquesController implements Initializable {

    @FXML private Label lblTotalBloques;
    @FXML private Label lblDebloquesAujourdhui;
    @FXML private TableView<Wallet> tableViewBloques;
    @FXML private TableColumn<Wallet, Integer> colId;
    @FXML private TableColumn<Wallet, String> colNom;
    @FXML private TableColumn<Wallet, String> colTelephone;
    @FXML private TableColumn<Wallet, String> colEmail;
    @FXML private TableColumn<Wallet, String> colDateBlocage;
    @FXML private TableColumn<Wallet, Void> colAction;

    private WalletService walletService;
    private EmailService emailService;
    private SmsService smsService;
    private ObservableList<Wallet> walletList;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        walletService = new WalletService();
        emailService = new EmailService();
        smsService = new SmsService();
        walletList = FXCollections.observableArrayList();

        setupTableColumns();
        loadDonnees();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idWallet"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nomProprietaire"));
        colTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        colDateBlocage.setCellValueFactory(cellData -> {
            LocalDateTime date = cellData.getValue().getDateDerniereTentative();
            if (date != null) {
                return new SimpleStringProperty(date.format(dateFormatter));
            }
            return new SimpleStringProperty("-");
        });

        // Bouton Réactiver
        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button btnReactiver = new Button("🔓 Réactiver");
            {
                btnReactiver.setOnAction(event -> {
                    Wallet wallet = getTableView().getItems().get(getIndex());
                    reactiverCompte(wallet);
                });
                btnReactiver.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 5;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btnReactiver);
                }
            }
        });

        tableViewBloques.setItems(walletList);
    }

    private void loadDonnees() {
        walletList.clear();
        List<Wallet> tousLesWallets = walletService.getAllWallets();

        int compteurBloques = 0;
        int compteurDebloquesAujourdhui = 0;

        System.out.println("🔍 RECHERCHE DES COMPTES BLOQUÉS");
        System.out.println("Total wallets: " + tousLesWallets.size());

        for (Wallet w : tousLesWallets) {
            System.out.println("Wallet: " + w.getNomProprietaire() + " - Bloqué: " + w.isEstBloque());

            if (w.isEstBloque()) {
                walletList.add(w);
                compteurBloques++;
                System.out.println("   ✅ AJOUTÉ à la liste");

                // Vérifier si débloqué aujourd'hui
                if (w.getDateDerniereTentative() != null &&
                        w.getDateDerniereTentative().toLocalDate().equals(LocalDateTime.now().toLocalDate())) {
                    compteurDebloquesAujourdhui++;
                }
            }
        }

        lblTotalBloques.setText(String.valueOf(compteurBloques));
        lblDebloquesAujourdhui.setText(String.valueOf(compteurDebloquesAujourdhui));

        System.out.println("📊 Total bloqués trouvés: " + compteurBloques);
    }

    private void reactiverCompte(Wallet wallet) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Réactiver le compte");
        confirm.setContentText("Voulez-vous vraiment réactiver le compte de " +
                wallet.getNomProprietaire() + " ?");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            // Débloquer le compte
            wallet.setEstBloque(false);
            wallet.setTentativesEchouees(0);
            walletService.mettreAJourWallet(wallet);

            // ✅ Incrémenter le compteur de débloqués aujourd'hui
            incrementerDebloquesAujourdhui();

            // Envoyer une notification au client
            String message = "✅ Votre compte a été réactivé par l'administrateur. Vous pouvez à nouveau effectuer des transactions.";

            if (wallet.getEmail() != null && !wallet.getEmail().isEmpty()) {
                emailService.envoyerEmailSimple(
                        wallet.getEmail(),
                        "✅ Compte réactivé - FinTrust",
                        message
                );
            }

            if (wallet.getTelephone() != null && !wallet.getTelephone().isEmpty()) {
                smsService.envoyerCodeSms(
                        wallet.getTelephone(),
                        "Compte réactivé. Vous pouvez à nouveau utiliser votre wallet."
                );
            }

            showAlert("Succès", "Compte réactivé avec succès");
            loadDonnees(); // Rafraîchir la liste
        }
    }

    private void incrementerDebloquesAujourdhui() {
        int current = Integer.parseInt(lblDebloquesAujourdhui.getText());
        lblDebloquesAujourdhui.setText(String.valueOf(current + 1));
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}