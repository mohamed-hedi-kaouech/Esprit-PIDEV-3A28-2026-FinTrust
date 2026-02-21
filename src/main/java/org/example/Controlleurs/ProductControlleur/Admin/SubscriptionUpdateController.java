package org.example.Controlleurs.ProductControlleur.Admin;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.Model.Product.ClassProduct.ProductSubscription;
import org.example.Model.Product.EnumProduct.SubscriptionStatus;
import org.example.Service.ProductService.ProductService;
import org.example.Service.ProductService.ProductSubscriptionService;
import org.example.Model.Product.EnumProduct.SubscriptionType;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class SubscriptionUpdateController implements Initializable {

    // Current Info Labels
    @FXML private Label lblSubscriptionId;
    @FXML private Label lblCurrentClient;
    @FXML private Label lblCurrentProduct;
    @FXML private Label lblCurrentType;
    @FXML private Label lblCurrentStatus;
    @FXML private Label lblLastModified;
    @FXML private Label lblProductDetails;

    // Form Fields
    @FXML private TextField tfSubscriptionId;
    @FXML private ComboBox<String> cbClient;
    @FXML private ComboBox<String> cbProduct;
    @FXML private ComboBox<String> cbType;
    @FXML private DatePicker dpSubscriptionDate;
    @FXML private DatePicker dpExpirationDate;
    @FXML private ComboBox<String> cbStatus;

    // Preview Labels
    @FXML private Label lblDuration;
    @FXML private Label lblDaysRemaining;

    private ProductSubscriptionService Ps;
    private ProductSubscription currentSubscription;

    private ProductService P;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Ps = new ProductSubscriptionService();

        setupDateListeners();
        loadComboBoxData();
    }

    private void setupDateListeners() {
        // Update preview when dates change
        if (dpSubscriptionDate != null) {
            dpSubscriptionDate.valueProperty().addListener((obs, old, newVal) -> updatePreview());
        }
        if (dpExpirationDate != null) {
            dpExpirationDate.valueProperty().addListener((obs, old, newVal) -> updatePreview());
        }
    }

    private void loadComboBoxData() {
        // Load products from database
        List<String> products = Ps.getAllProductsForDisplay();
        if (cbProduct != null && !products.isEmpty()) {
            cbProduct.getItems().clear();
            cbProduct.getItems().addAll(products);
        }

        // Load clients (placeholder - implement when client table exists)
        if (cbClient != null) {
            cbClient.getItems().clear();
            // Add placeholder clients
            for (int i = 1; i <= 10; i++) {
                cbClient.getItems().add("Client #" + i);
            }
        }

        // Add listener to product selection to show details
        if (cbProduct != null) {
            cbProduct.setOnAction(e -> {
                String selected = cbProduct.getValue();
                if (selected != null && lblProductDetails != null) {
                    lblProductDetails.setText("Produit sélectionné");
                    lblProductDetails.setStyle("-fx-text-fill: #22c55e;");
                }
            });
        }
    }

    /**
     * Load subscription data into the form
     */
    public void loadSubscription(ProductSubscription subscription) {
        if (subscription == null) {
            showErrorAlert("Erreur", "Aucun abonnement à modifier.");
            return;
        }

        this.currentSubscription = subscription;

        // Set ID badge
        lblSubscriptionId.setText("#" + subscription.getSubscriptionId());

        // Set current values display
//        lblCurrentClient.setText(subscription.getClientName() != null ?
//                subscription.getClientName() : "Client #" + subscription.getClient());
        lblCurrentProduct.setText(Ps.getProductName(subscription.getSubscriptionId()) != null ?
                Ps.getProductName(subscription.getSubscriptionId()).name() : "Produit #" + subscription.getProduct());
        lblCurrentType.setText(subscription.getType().name());
        lblCurrentStatus.setText(subscription.getStatus().name());

        // Style current status
        styleStatusLabel(lblCurrentStatus, subscription.getStatus().name());

        // Populate form fields
        tfSubscriptionId.setText(String.valueOf(subscription.getSubscriptionId()));

        // Set client and product (by matching display format)
//        if (subscription.getClientName() != null) {
//            cbClient.setValue(subscription.getClientName());
//        } else {
//            cbClient.setValue("Client #" + subscription.getClient());
//        }

        // Find and select the product in the combo box
        if (cbProduct != null) {
            int productId = subscription.getProduct();
            for (String item : cbProduct.getItems()) {
                if (item.startsWith("#" + productId + " |")) {
                    cbProduct.setValue(item);
                    break;
                }
            }
        }

        cbType.setValue(subscription.getType().name());
        dpSubscriptionDate.setValue(subscription.getSubscriptionDate().toLocalDate());
        dpExpirationDate.setValue(subscription.getExpirationDate().toLocalDate());
        cbStatus.setValue(subscription.getStatus().name());

        // Update preview
        updatePreview();
    }

    private void styleStatusLabel(Label label, String status) {
        switch (status) {
            case "ACTIVE":
                label.setStyle("-fx-text-fill: #22c55e; -fx-font-weight: bold;");
                break;
            case "DRAFT":
                label.setStyle("-fx-text-fill: #94a3b8; -fx-font-weight: bold;");
                break;
            case "SUSPENDED":
                label.setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
                break;
            case "CLOSED":
                label.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                break;
        }
    }

    private void updatePreview() {
        LocalDate subDate = dpSubscriptionDate.getValue();
        LocalDate expDate = dpExpirationDate.getValue();

        if (subDate == null || expDate == null) {
            lblDuration.setText("-");
            lblDaysRemaining.setText("-");
            lblDaysRemaining.setStyle("-fx-text-fill: #64748b;");
            return;
        }

        // Calculate duration
        long months = ChronoUnit.MONTHS.between(subDate, expDate);
        long days = ChronoUnit.DAYS.between(subDate, expDate);

        if (months > 0) {
            lblDuration.setText(months + " mois (" + days + " jours)");
        } else {
            lblDuration.setText(days + " jours");
        }

        // Calculate days remaining
        long daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), expDate);

        if (daysRemaining < 0) {
            lblDaysRemaining.setText("Expiré (" + Math.abs(daysRemaining) + " jours)");
            lblDaysRemaining.setStyle("-fx-text-fill: #ef4444;");
        } else if (daysRemaining <= 30) {
            lblDaysRemaining.setText(daysRemaining + " jours (⚠️ Bientôt)");
            lblDaysRemaining.setStyle("-fx-text-fill: #f59e0b;");
        } else {
            lblDaysRemaining.setText(daysRemaining + " jours");
            lblDaysRemaining.setStyle("-fx-text-fill: #22c55e;");
        }
    }

    @FXML
    private void updateSubscription() {
        if (!validateInput()) {
            return;
        }

        // Show confirmation
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText("Confirmer les modifications");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir enregistrer ces modifications?\n\n" +
                "Type: " + cbType.getValue() + "\n" +
                "Date début: " + dpSubscriptionDate.getValue() + "\n" +
                "Date fin: " + dpExpirationDate.getValue() + "\n" +
                "Statut: " + cbStatus.getValue());

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                int id = Integer.parseInt(tfSubscriptionId.getText());

                // Extract client ID from selection
                String clientSelection = cbClient.getValue();
                int clientId = currentSubscription.getClient(); // Use existing for now
                if (clientSelection != null && clientSelection.startsWith("Client #")) {
                    try {
                        clientId = Integer.parseInt(clientSelection.substring(8));
                    } catch (NumberFormatException e) {
                        // Keep current client ID
                    }
                }

                // Extract product ID from selection
                int productId = Ps.extractProductIdFromDisplay(cbProduct.getValue());
                if (productId == -1) {
                    productId = currentSubscription.getProduct(); // Use existing if extraction fails
                }

                LocalDate subDate = dpSubscriptionDate.getValue();
                LocalDate expDate = dpExpirationDate.getValue();
                SubscriptionType typeEnum = SubscriptionType.valueOf(cbType.getValue());
                SubscriptionStatus statusEnum = SubscriptionStatus.valueOf(cbStatus.getValue());

                ProductSubscription subscription = new ProductSubscription(
                        id, clientId, productId, typeEnum, subDate.atStartOfDay(), expDate.atStartOfDay(), statusEnum
                );

                if (Ps.update(subscription)) {
                    showSuccessAlert("Succès",
                            "L'abonnement a été modifié avec succès!");
                    goBackToListe();
                } else {
                    showErrorAlert("Erreur",
                            "Erreur lors de la modification de l'abonnement.");
                }

            } catch (Exception e) {
                showErrorAlert("Erreur", "Erreur: " + e.getMessage());
            }
        }
    }

    @FXML
    private void deleteSubscription() {
        if (currentSubscription == null) {
            showErrorAlert("Erreur", "Aucun abonnement sélectionné.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.WARNING);
        confirmAlert.setTitle("⚠️ Supprimer l'abonnement");
        confirmAlert.setHeaderText("ATTENTION: Suppression définitive!");
        confirmAlert.setContentText("Êtes-vous ABSOLUMENT SÛR de vouloir supprimer cet abonnement?\n\n" +
                "ID: #" + currentSubscription.getSubscriptionId() + "\n" +
                "Client: " + lblCurrentClient.getText() + "\n" +
                "Produit: " + lblCurrentProduct.getText() + "\n\n" +
                "⚠️ Cette action est IRRÉVERSIBLE!");

        ButtonType deleteButtonType = new ButtonType("Supprimer définitivement", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmAlert.getButtonTypes().setAll(deleteButtonType, cancelButtonType);

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == deleteButtonType) {
            if (Ps.delete(currentSubscription.getSubscriptionId())) {
                showSuccessAlert("Succès",
                        "L'abonnement a été supprimé avec succès!");
                goBackToListe();
            } else {
                showErrorAlert("Erreur",
                        "Erreur lors de la suppression de l'abonnement.");
            }
        }
    }

    @FXML
    private void resetForm() {
        if (currentSubscription != null) {
            loadSubscription(currentSubscription);
        }
    }

    @FXML
    private void goBackToListe() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Product/Admin/ListeSubProductGUI.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) tfSubscriptionId.getScene().getWindow();
            Scene scene = new Scene(root);

            stage.setScene(scene);
            stage.setTitle("Liste des Abonnements");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Erreur", "Impossible de retourner à la liste.");
        }
    }

    private boolean validateInput() {
        StringBuilder errors = new StringBuilder();
        boolean isValid = true;

        if (isEmpty(cbClient)) {
            errors.append("- Le client est obligatoire.\n");
            isValid = false;
        }

        if (isEmpty(cbProduct)) {
            errors.append("- Le produit est obligatoire.\n");
            isValid = false;
        }

        if (isEmpty(cbType)) {
            errors.append("- Le type est obligatoire.\n");
            isValid = false;
        }

        LocalDate today = LocalDate.now();
        LocalDate subscriptionDate = dpSubscriptionDate.getValue();
        LocalDate expirationDate = dpExpirationDate.getValue();

        if (subscriptionDate == null) {
            errors.append("- La date de souscription est obligatoire.\n");
            isValid = false;
        } else if (subscriptionDate.isAfter(today)) {
            errors.append("- La date de souscription ne peut pas être dans le futur.\n");
            isValid = false;
        }

        if (expirationDate == null) {
            errors.append("- La date d'expiration est obligatoire.\n");
            isValid = false;
        } else if (subscriptionDate != null && !expirationDate.isAfter(subscriptionDate)) {
            errors.append("- La date d'expiration doit être après la date de souscription.\n");
            isValid = false;
        }

        if (isEmpty(cbStatus)) {
            errors.append("- Le statut est obligatoire.\n");
            isValid = false;
        }

        if (!isValid) {
            showErrorAlert("Erreur de validation", errors.toString());
        }

        return isValid;
    }

    private boolean isEmpty(ComboBox<String> cb) {
        return cb.getValue() == null || cb.getValue().trim().isEmpty();
    }

    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}