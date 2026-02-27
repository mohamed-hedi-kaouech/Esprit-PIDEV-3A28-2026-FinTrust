package org.example.Controlleurs.ProductControlleur.Admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.example.Model.Product.ClassProduct.ProductSubscription;
import org.example.Service.ProductService.ProductSubscriptionService;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;

public class SubscriptionListeController implements Initializable {

    // Stats Labels
    @FXML private Label lblTotalSubscriptions;
    @FXML private Label lblActiveSubscriptions;
    @FXML private Label lblDraftSubscriptions;
    @FXML private Label lblExpiringSoon;
    @FXML private Label lblFilteredCount;
    @FXML private Label lblTotal;
    @FXML private Label lblActiveCount;
    @FXML private Label lblSuspendedCount;
    @FXML private Label lblClosedCount;

    // Filters and Search
    @FXML private ComboBox<String> cbTypeFilter;
    @FXML private ComboBox<String> cbStatusFilter;
    @FXML private TextField tfSearch;

    // ListView
    @FXML private ListView<ProductSubscription> subscriptionListView;

    // Data
    private ObservableList<ProductSubscription> subscriptionList = FXCollections.observableArrayList();
    private FilteredList<ProductSubscription> filteredList;
    private ProductSubscriptionService PS;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        PS = new ProductSubscriptionService();

        setupListView();
        setupFilters();
        loadSubscriptions();
        updateStatistics();
    }

    private void setupListView() {
        // Set custom cell factory
        subscriptionListView.setCellFactory(listView -> new SubscriptionListCell());

        // Set placeholder
        Label placeholder = new Label("Aucun abonnement disponible\n\nCliquez sur 'Nouvel Abonnement' pour commencer");
        placeholder.setStyle("-fx-text-fill: #64748b; -fx-font-size: 14px; -fx-font-style: italic;");
        subscriptionListView.setPlaceholder(placeholder);
    }

    private void setupFilters() {
        filteredList = new FilteredList<>(subscriptionList, p -> true);
        subscriptionListView.setItems(filteredList);

        // Type filter
        if (cbTypeFilter != null) {
            cbTypeFilter.setValue("Tous");
            cbTypeFilter.setOnAction(e -> applyFilters());
        }

        // Status filter
        if (cbStatusFilter != null) {
            cbStatusFilter.setValue("Tous");
            cbStatusFilter.setOnAction(e -> applyFilters());
        }

        // Search filter
        if (tfSearch != null) {
            tfSearch.textProperty().addListener((observable, oldValue, newValue) -> {
                applyFilters();
            });
        }
    }

    private void loadSubscriptions() {
        subscriptionList.clear();
        subscriptionList.addAll(PS.ReadAll());
        updateStatistics();
    }

    private void applyFilters() {
        String typeFilter = cbTypeFilter != null ? cbTypeFilter.getValue() : "Tous";
        String statusFilter = cbStatusFilter != null ? cbStatusFilter.getValue() : "Tous";
        String searchText = tfSearch != null ? tfSearch.getText().toLowerCase() : "";

        filteredList.setPredicate(subscription -> {
            // Type filter
            boolean typeMatch = true;
            if (typeFilter != null && !typeFilter.equals("Tous")) {
                typeMatch = subscription.getType().name().equals(typeFilter);
            }

            // Status filter
            boolean statusMatch = true;
            if (statusFilter != null && !statusFilter.equals("Tous")) {
                statusMatch = subscription.getStatus().name().equals(statusFilter);
            }

            // Search filter
            boolean searchMatch = true;
            if (searchText != null && !searchText.isEmpty()) {
                searchMatch = String.valueOf(subscription.getSubscriptionId()).contains(searchText) ||
//                        (subscription.getClientName() != null && subscription.getClientName().toLowerCase().contains(searchText)) ||
                        (PS.getProductName(subscription.getSubscriptionId()) != null && PS.getProductName(subscription.getSubscriptionId()).name().toLowerCase().contains(searchText)) ||
                        subscription.getType().name().toLowerCase().contains(searchText) ||
                        subscription.getStatus().name().toLowerCase().contains(searchText);
            }

            return typeMatch && statusMatch && searchMatch;
        });

        updateFilteredCount();
    }

    private void updateStatistics() {
        int total = subscriptionList.size();
        long active = subscriptionList.stream().filter(s -> "ACTIVE".equals(s.getStatus().name())).count();
        long draft = subscriptionList.stream().filter(s -> "DRAFT".equals(s.getStatus().name())).count();
        long suspended = subscriptionList.stream().filter(s -> "SUSPENDED".equals(s.getStatus().name())).count();
        long closed = subscriptionList.stream().filter(s -> "CLOSED".equals(s.getStatus().name())).count();
        long expiringSoon = subscriptionList.stream()
                .filter(s -> "ACTIVE".equals(s.getStatus().name()) && s.isExpiringSoon())
                .count();

        lblTotalSubscriptions.setText(String.valueOf(total));
        lblActiveSubscriptions.setText(String.valueOf(active));
        lblDraftSubscriptions.setText(String.valueOf(draft));
        lblExpiringSoon.setText(String.valueOf(expiringSoon));

        lblTotal.setText(String.valueOf(total));
        lblActiveCount.setText(String.valueOf(active));
        lblSuspendedCount.setText(String.valueOf(suspended));
        lblClosedCount.setText(String.valueOf(closed));

        updateFilteredCount();
    }

    private void updateFilteredCount() {
        int filtered = filteredList.size();
        lblFilteredCount.setText("Affichage: " + filtered + " abonnement" + (filtered > 1 ? "s" : ""));
    }

    @FXML
    private void resetSearch() {
        if (tfSearch != null) {
            tfSearch.clear();
        }
        if (cbTypeFilter != null) {
            cbTypeFilter.setValue("Tous");
        }
        if (cbStatusFilter != null) {
            cbStatusFilter.setValue("Tous");
        }
        applyFilters();
    }

    private void goToUpdate(ProductSubscription subscription) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Product/Admin/SubscriptionUpdate.fxml"));
            Parent root = loader.load();

            SubscriptionUpdateController controller = loader.getController();
            controller.loadSubscription(subscription);

            Stage stage = (Stage) subscriptionListView.getScene().getWindow();
            Scene scene = new Scene(root);

            stage.setScene(scene);
            stage.setTitle("Modifier l'Abonnement");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Erreur", "Impossible de charger la page de modification.");
        }
    }

    private void deleteSubscription(ProductSubscription subscription) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation de suppression");
        confirmAlert.setHeaderText("Supprimer l'abonnement");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer cet abonnement?\n\n" +
                "ID: #" + subscription.getSubscriptionId() + "\n" +
//                "Client: " + subscription.getClientName() + "\n" +
                "Produit: " + PS.getProductName(subscription.getSubscriptionId()) + "\n\n" +
                "Cette action est irréversible!");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (PS.delete(subscription.getSubscriptionId())) {
                showSuccessAlert("Succès", "L'abonnement a été supprimé avec succès!");
                loadSubscriptions();
            } else {
                showErrorAlert("Erreur", "Erreur lors de la suppression de l'abonnement.");
            }
        }
    }



    // Alert methods
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

    // ==================== Custom ListView Cell ====================
    private class SubscriptionListCell extends ListCell<ProductSubscription> {
        private final VBox container;
        private final HBox headerBox;
        private final HBox bodyBox;
        private final HBox footerBox;

        private final Label idLabel;
        private final Label clientLabel;
        private final Label productLabel;
        private final Label typeLabel;
        private final Label statusLabel;
        private final Label subscriptionDateLabel;
        private final Label expirationDateLabel;
        private final Label daysRemainingLabel;
        private final Button updateButton;
        private final Button deleteButton;

        public SubscriptionListCell() {
            super();

            // Container
            container = new VBox(12);
            container.setPadding(new Insets(15));
            container.getStyleClass().add("product-card");

            // Header
            headerBox = new HBox(15);
            headerBox.setAlignment(Pos.CENTER_LEFT);

            idLabel = new Label();
            idLabel.getStyleClass().add("product-id");
            idLabel.setFont(Font.font("System Bold", 14));

            VBox clientProductBox = new VBox(3);
            clientLabel = new Label();
            clientLabel.getStyleClass().add("product-name");
            clientLabel.setFont(Font.font("System Bold", 15));

            productLabel = new Label();
            productLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");

            clientProductBox.getChildren().addAll(clientLabel, productLabel);
            HBox.setHgrow(clientProductBox, Priority.ALWAYS);

            statusLabel = new Label();
            statusLabel.getStyleClass().add("product-status-badge");

            headerBox.getChildren().addAll(idLabel, clientProductBox, statusLabel);

            // Body
            bodyBox = new HBox(30);
            bodyBox.setAlignment(Pos.CENTER_LEFT);

            VBox typeBox = new VBox(3);
            Label typeTitle = new Label("Type");
            typeTitle.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");
            typeLabel = new Label();
            typeLabel.setStyle("-fx-text-fill: #60a5fa; -fx-font-weight: bold; -fx-font-size: 13px;");
            typeBox.getChildren().addAll(typeTitle, typeLabel);

            VBox subDateBox = new VBox(3);
            Label subDateTitle = new Label("Souscription");
            subDateTitle.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");
            subscriptionDateLabel = new Label();
            subscriptionDateLabel.setStyle("-fx-text-fill: #e2e8f0; -fx-font-size: 12px;");
            subDateBox.getChildren().addAll(subDateTitle, subscriptionDateLabel);

            VBox expDateBox = new VBox(3);
            Label expDateTitle = new Label("Expiration");
            expDateTitle.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");
            expirationDateLabel = new Label();
            expirationDateLabel.setStyle("-fx-text-fill: #e2e8f0; -fx-font-size: 12px;");
            expDateBox.getChildren().addAll(expDateTitle, expirationDateLabel);

            VBox daysBox = new VBox(3);
            Label daysTitle = new Label("Jours restants");
            daysTitle.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");
            daysRemainingLabel = new Label();
            daysRemainingLabel.setFont(Font.font("System Bold", 14));
            daysBox.getChildren().addAll(daysTitle, daysRemainingLabel);

            bodyBox.getChildren().addAll(typeBox, subDateBox, expDateBox, daysBox);

            // Footer
            footerBox = new HBox(10);
            footerBox.setAlignment(Pos.CENTER_RIGHT);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            updateButton = new Button("✏️ Modifier");
            updateButton.getStyleClass().add("btn-update-card");

            deleteButton = new Button("🗑️ Supprimer");
            deleteButton.getStyleClass().add("btn-delete-card");

            footerBox.getChildren().addAll(spacer, updateButton, deleteButton);

            Separator separator = new Separator();
            separator.getStyleClass().add("card-separator");

            container.getChildren().addAll(headerBox, separator, bodyBox, footerBox);
        }

        @Override
        protected void updateItem(ProductSubscription sub, boolean empty) {
            super.updateItem(sub, empty);

            if (empty || sub == null) {
                setGraphic(null);
            } else {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

                // ID
                idLabel.setText("#" + sub.getSubscriptionId());

                // Client and Product
//                clientLabel.setText(sub.getClientName() != null ? sub.getClientName() : "Client #" + sub.getClient());
                productLabel.setText("📦 " + (PS.getProductName(sub.getSubscriptionId()) != null ? PS.getProductName(sub.getSubscriptionId()) : "Produit #" + sub.getProduct()));

                // Type
                typeLabel.setText(sub.getType().name());

                // Dates
                subscriptionDateLabel.setText(sub.getSubscriptionDate().format(formatter));
                expirationDateLabel.setText(sub.getExpirationDate().format(formatter));

                // Days remaining
                long daysRemaining = sub.getDaysUntilExpiration();
                if (daysRemaining < 0) {
                    daysRemainingLabel.setText("Expiré");
                    daysRemainingLabel.setStyle("-fx-text-fill: #ef4444;");
                } else if (daysRemaining <= 30) {
                    daysRemainingLabel.setText(daysRemaining + " jours");
                    daysRemainingLabel.setStyle("-fx-text-fill: #f59e0b;");
                } else {
                    daysRemainingLabel.setText(daysRemaining + " jours");
                    daysRemainingLabel.setStyle("-fx-text-fill: #22c55e;");
                }

                // Status
                statusLabel.setText(sub.getStatus().name());
                String statusStyle = getStatusStyle(sub.getStatus().name());
                statusLabel.setStyle(statusStyle);

                // Buttons
                updateButton.setOnAction(e -> goToUpdate(sub));
                deleteButton.setOnAction(e -> deleteSubscription(sub));

                setGraphic(container);
            }
        }

        private String getStatusStyle(String status) {
            switch (status) {
                case "ACTIVE":
                    return "-fx-background-color: rgba(34, 197, 94, 0.2); -fx-text-fill: #22c55e; " +
                            "-fx-background-radius: 12px; -fx-padding: 4px 12px; -fx-font-weight: bold; -fx-font-size: 11px;";
                case "DRAFT":
                    return "-fx-background-color: rgba(148, 163, 184, 0.2); -fx-text-fill: #94a3b8; " +
                            "-fx-background-radius: 12px; -fx-padding: 4px 12px; -fx-font-weight: bold; -fx-font-size: 11px;";
                case "SUSPENDED":
                    return "-fx-background-color: rgba(245, 158, 11, 0.2); -fx-text-fill: #f59e0b; " +
                            "-fx-background-radius: 12px; -fx-padding: 4px 12px; -fx-font-weight: bold; -fx-font-size: 11px;";
                case "CLOSED":
                    return "-fx-background-color: rgba(239, 68, 68, 0.2); -fx-text-fill: #ef4444; " +
                            "-fx-background-radius: 12px; -fx-padding: 4px 12px; -fx-font-weight: bold; -fx-font-size: 11px;";
                default:
                    return "-fx-background-color: rgba(148, 163, 184, 0.2); -fx-text-fill: #94a3b8; " +
                            "-fx-background-radius: 12px; -fx-padding: 4px 12px; -fx-font-weight: bold; -fx-font-size: 11px;";
            }
        }
    }
}