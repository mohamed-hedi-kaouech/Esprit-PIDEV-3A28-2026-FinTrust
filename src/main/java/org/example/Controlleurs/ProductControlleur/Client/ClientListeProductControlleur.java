package org.example.Controlleurs.ProductControlleur.Client;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.example.Model.Product.ClassProduct.SubProduct;
import org.example.Model.Product.EnumProduct.SubscriptionStatus;
import org.example.Model.User.User;
import org.example.Model.User.UserRole;
import org.example.Service.ProductService.ProductService;
import org.example.Service.ProductService.ProductSubscriptionService;
import org.example.Utils.SessionContext;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.ResourceBundle;

public class ClientListeProductControlleur implements Initializable {

    @FXML private ListView<SubProduct> SubProductsListeView;
    @FXML private TextField searchField;
    @FXML private Label totalProductsLabel;

    private ObservableList<SubProduct> SubProductsListe = FXCollections.observableArrayList();
    private ObservableList<SubProduct> filteredList = FXCollections.observableArrayList();
    private ProductService PS;
    private ProductSubscriptionService PSS;
    private final SessionContext session = SessionContext.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        User user = session.getCurrentUser();
        if (user == null || user.getRole() != UserRole.CLIENT) {
            try {
                Parent root = FXMLLoader.load(
                        getClass().getResource("/Auth/Login.fxml")
                );

                Stage stage = (Stage) Stage.getWindows()
                        .filtered(window -> window.isShowing())
                        .get(0);

                stage.setScene(new Scene(root));
                stage.setTitle("Connexion");

            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        PS = new ProductService();
        PSS = new ProductSubscriptionService();
        setupListView();
        loadProductData();
        setupSearchListener();
    }

    private void setupListView() {
        SubProductsListeView.setCellFactory(listView -> new SubProductListCell());
        SubProductsListeView.setStyle("-fx-background-color: #f4f6f9; -fx-border-color: transparent;");

        Label placeholder = new Label("Aucun abonnement trouvé");
        placeholder.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 14px; -fx-font-style: italic;");
        SubProductsListeView.setPlaceholder(placeholder);
    }

    private void loadProductData() {
        SubProductsListe.clear();
        SubProductsListe.addAll(PSS.getSubProducts(1));
        filteredList.setAll(SubProductsListe);
        SubProductsListeView.setItems(filteredList);
        updateTotalLabel();
    }

    private void setupSearchListener() {
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> filterProducts(newVal));
        }
    }

    private void filterProducts(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            filteredList.setAll(SubProductsListe);
        } else {
            filteredList.clear();
            String lower = searchText.toLowerCase();
            for (SubProduct sp : SubProductsListe) {
                if (sp.getCategory().name().toLowerCase().contains(lower) ||
                        sp.getDescription().toLowerCase().contains(lower) ||
                        sp.getType().name().toLowerCase().contains(lower) ||
                        sp.getStatus().name().toLowerCase().contains(lower) ||
                        String.valueOf(sp.getProductId()).contains(lower)) {
                    filteredList.add(sp);
                }
            }
        }
        updateTotalLabel();
    }

    private void updateTotalLabel() {
        if (totalProductsLabel != null) {
            int total = filteredList.size();
            totalProductsLabel.setText(String.format("Total: %d abonnement%s", total, total > 1 ? "s" : ""));
        }
    }

    @FXML
    private void handleSearch() {
        filterProducts(searchField.getText());
    }

    @FXML
    private void handleReset() {
        if (searchField != null) searchField.clear();
        filteredList.setAll(SubProductsListe);
        updateTotalLabel();
    }

    private void handleUnsub(SubProduct subProduct) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Désabonnement");
        confirmAlert.setHeaderText("Confirmer le désabonnement");
        confirmAlert.setContentText(
                "Êtes-vous sûr de vouloir vous désabonner ?\n\n" +
                        "Produit: " + formatCategoryName(subProduct.getCategory().name()) + "\n" +
                        "Type: " + subProduct.getType().name() + "\n" +
                        "Prix: " + String.format("%.2f DT", subProduct.getPrice()) + "\n\n" +
                        "⚠️ Cette action est irréversible !"
        );

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (PSS.delete(subProduct.getSubscriptionId())) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Désabonnement effectué avec succès !");
                loadProductData();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du désabonnement.");
            }
        }
    }

    private String formatCategoryName(String category) {
        if (category == null) return "";
        return category.replace("_", " ");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void goBackToMenu(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/Client/ClientDashboard.fxml")
            );
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Manager");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ==================== Custom ListView Cell ====================
    private class SubProductListCell extends ListCell<SubProduct> {

        private final VBox card;

        // Header row
        private final Label categoryBadge;
        private final Label typeBadge;
        private final Label statusBadge;
        private final Label priceLabel;

        // Description
        private final Label descriptionLabel;

        // Dates row
        private final Label subDateLabel;
        private final Label expDateLabel;
        private final Label daysLeftLabel;

        // Footer
        private final Label subIdLabel;
        private final Button unsubButton;

        public SubProductListCell() {
            super();

            // ── Category badge ──────────────────────────────────
            categoryBadge = new Label();
            categoryBadge.setFont(Font.font("System", FontWeight.BOLD, 11));
            categoryBadge.setPadding(new Insets(3, 10, 3, 10));

            // ── Type badge ──────────────────────────────────────
            typeBadge = new Label();
            typeBadge.setFont(Font.font("System", FontWeight.NORMAL, 11));
            typeBadge.setPadding(new Insets(3, 10, 3, 10));
            typeBadge.setStyle("-fx-background-color: #e8eaf6; -fx-text-fill: #3949ab; -fx-background-radius: 10px;");

            // ── Status badge ────────────────────────────────────
            statusBadge = new Label();
            statusBadge.setFont(Font.font("System", FontWeight.BOLD, 11));
            statusBadge.setPadding(new Insets(3, 10, 3, 10));

            // ── Price ───────────────────────────────────────────
            priceLabel = new Label();
            priceLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
            priceLabel.setStyle("-fx-text-fill: #1a237e;");

            Region headerSpacer = new Region();
            HBox.setHgrow(headerSpacer, Priority.ALWAYS);

            HBox badgeBox = new HBox(8, categoryBadge, typeBadge, statusBadge);
            badgeBox.setAlignment(Pos.CENTER_LEFT);

            HBox headerRow = new HBox(10, badgeBox, headerSpacer, priceLabel);
            headerRow.setAlignment(Pos.CENTER_LEFT);

            // ── Description ─────────────────────────────────────
            descriptionLabel = new Label();
            descriptionLabel.setWrapText(true);
            descriptionLabel.setFont(Font.font("System", 13));
            descriptionLabel.setStyle("-fx-text-fill: #37474f;");
            descriptionLabel.setMaxWidth(Double.MAX_VALUE);

            // ── Dates ────────────────────────────────────────────
            subDateLabel = new Label();
            subDateLabel.setFont(Font.font("System", 11));
            subDateLabel.setStyle("-fx-text-fill: #607d8b;");

            expDateLabel = new Label();
            expDateLabel.setFont(Font.font("System", 11));
            expDateLabel.setStyle("-fx-text-fill: #607d8b;");

            daysLeftLabel = new Label();
            daysLeftLabel.setFont(Font.font("System", FontWeight.BOLD, 11));
            daysLeftLabel.setPadding(new Insets(2, 8, 2, 8));

            Region dateSpacer = new Region();
            HBox.setHgrow(dateSpacer, Priority.ALWAYS);

            HBox datesRow = new HBox(16, subDateLabel, expDateLabel, dateSpacer, daysLeftLabel);
            datesRow.setAlignment(Pos.CENTER_LEFT);

            // ── Footer ───────────────────────────────────────────
            subIdLabel = new Label();
            subIdLabel.setFont(Font.font("System", 10));
            subIdLabel.setStyle("-fx-text-fill: #b0bec5;");

            unsubButton = new Button("✕  Se désabonner");
            unsubButton.setFont(Font.font("System", FontWeight.BOLD, 11));
            unsubButton.setStyle(
                    "-fx-background-color: #ffebee;" +
                            "-fx-text-fill: #c62828;" +
                            "-fx-background-radius: 8px;" +
                            "-fx-cursor: hand;" +
                            "-fx-padding: 6px 16px;"
            );
            unsubButton.setOnMouseEntered(e -> unsubButton.setStyle(
                    "-fx-background-color: #c62828;" +
                            "-fx-text-fill: white;" +
                            "-fx-background-radius: 8px;" +
                            "-fx-cursor: hand;" +
                            "-fx-padding: 6px 16px;"
            ));
            unsubButton.setOnMouseExited(e -> unsubButton.setStyle(
                    "-fx-background-color: #ffebee;" +
                            "-fx-text-fill: #c62828;" +
                            "-fx-background-radius: 8px;" +
                            "-fx-cursor: hand;" +
                            "-fx-padding: 6px 16px;"
            ));

            Region footerSpacer = new Region();
            HBox.setHgrow(footerSpacer, Priority.ALWAYS);

            HBox footerRow = new HBox(10, subIdLabel, footerSpacer, unsubButton);
            footerRow.setAlignment(Pos.CENTER_LEFT);

            // ── Card container ───────────────────────────────────
            Separator sep1 = new Separator();
            sep1.setStyle("-fx-background-color: #eceff1;");
            Separator sep2 = new Separator();
            sep2.setStyle("-fx-background-color: #eceff1;");

            card = new VBox(10, headerRow, sep1, descriptionLabel, datesRow, sep2, footerRow);
            card.setPadding(new Insets(16, 20, 16, 20));
            card.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-background-radius: 12px;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);"
            );
            VBox.setMargin(card, new Insets(6, 10, 6, 10));
        }

        @Override
        protected void updateItem(SubProduct sp, boolean empty) {
            super.updateItem(sp, empty);
            setStyle("-fx-background-color: transparent; -fx-padding: 0;");

            if (empty || sp == null) {
                setGraphic(null);
                return;
            }

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            // Category badge
            categoryBadge.setText(formatCategoryName(sp.getCategory().name()));
            categoryBadge.setStyle(getCategoryStyle(sp.getCategory().name()));

            // Type badge
            typeBadge.setText("🔄 " + sp.getType().name());

            // Status badge
            statusBadge.setText(getStatusIcon(sp.getStatus()) + " " + sp.getStatus().name());
            statusBadge.setStyle(getStatusStyle(sp.getStatus()));

            // Price
            priceLabel.setText(String.format("%.2f DT", sp.getPrice()));

            // Description
            descriptionLabel.setText(sp.getDescription());

            // Dates
            subDateLabel.setText("📅 Début : " + sp.getSubscriptionDate().format(fmt));
            expDateLabel.setText("⏳ Expiration : " + sp.getExpirationDate().format(fmt));

            // Days left indicator
            long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), sp.getExpirationDate());
            if (daysLeft < 0) {
                daysLeftLabel.setText("Expiré");
                daysLeftLabel.setStyle("-fx-background-color: #fce4ec; -fx-text-fill: #b71c1c; -fx-background-radius: 10px;");
            } else if (daysLeft <= 7) {
                daysLeftLabel.setText("⚠️ " + daysLeft + "j restants");
                daysLeftLabel.setStyle("-fx-background-color: #fff8e1; -fx-text-fill: #e65100; -fx-background-radius: 10px;");
            } else {
                daysLeftLabel.setText("✓ " + daysLeft + "j restants");
                daysLeftLabel.setStyle("-fx-background-color: #e8f5e9; -fx-text-fill: #2e7d32; -fx-background-radius: 10px;");
            }

            // Sub ID
            subIdLabel.setText("#" + sp.getSubscriptionId() + "  ·  Produit #" + sp.getProductId());

            // Button action
            unsubButton.setOnAction(e -> handleUnsub(sp));

            setGraphic(card);
        }

        // ── Helpers ──────────────────────────────────────────────

        private String getCategoryStyle(String category) {
            if (category == null) return defaultBadgeStyle("#f5f5f5", "#666666");
            if (category.startsWith("COMPTE"))   return defaultBadgeStyle("#e3f2fd", "#1565C0");
            if (category.startsWith("CARTE"))    return defaultBadgeStyle("#f3e5f5", "#6A1B9A");
            if (category.startsWith("EPARGNE") || category.contains("DEPOT") || category.contains("PLACEMENT"))
                return defaultBadgeStyle("#e8f5e9", "#1b5e20");
            if (category.startsWith("ASSURANCE"))return defaultBadgeStyle("#fff3e0", "#bf360c");
            return defaultBadgeStyle("#f5f5f5", "#424242");
        }

        private String defaultBadgeStyle(String bg, String fg) {
            return "-fx-background-color: " + bg + ";" +
                    "-fx-text-fill: " + fg + ";" +
                    "-fx-background-radius: 10px;";
        }

        private String getStatusStyle(SubscriptionStatus status) {
            if (status == null) return defaultBadgeStyle("#f5f5f5", "#616161");
            switch (status) {
                case ACTIVE:   return defaultBadgeStyle("#e8f5e9", "#2e7d32");
                case CLOSED: return defaultBadgeStyle("#fce4ec", "#b71c1c");
                case SUSPENDED:  return defaultBadgeStyle("#fff9c4", "#f57f17");
                default:       return defaultBadgeStyle("#f5f5f5", "#616161");
            }
        }

        private String getStatusIcon(SubscriptionStatus status) {
            if (status == null) return "●";
            switch (status) {
                case ACTIVE:   return "✅";
                case CLOSED: return "❌";
                case SUSPENDED:  return "🕐";
                default:       return "●";
            }
        }
    }
}