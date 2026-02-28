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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import okhttp3.OkHttpClient;
import org.example.Model.Product.ClassProduct.Product;
import org.example.Model.Product.ClassProduct.ProductSubscription;
import org.example.Model.Product.EnumProduct.SubscriptionType;
import org.example.Model.User.User;
import org.example.Model.User.UserRole;
import org.example.Model.Wallet.ClassWallet.Wallet;
import org.example.Service.ProductService.ProductService;
import org.example.Service.ProductService.ProductSubscriptionService;
import org.example.Service.WalletService.WalletService;
import org.example.Utils.SessionContext;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;


public class ClientMarketControlleur implements Initializable {


    // TableView
    @FXML private ListView<Product> productListView;
    private ProductSubscriptionService PSS;

    @FXML private TextField searchField;
    @FXML private Label totalProductsLabel;

    private ObservableList<Product> productList = FXCollections.observableArrayList();
    private ObservableList<Product> filteredList = FXCollections.observableArrayList();
    private ProductService PS;
    private WalletService WS;
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
        WS = new WalletService();
        setupListView();
        loadProductData();
        setupSearchListener();
    }
    

    private void setupListView() {
        // Set custom cell factory for ListView
        productListView.setCellFactory(listView -> new ProductListCell());

        // Set placeholder when list is empty
        Label placeholder = new Label("Aucun produit disponible");
        placeholder.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 14px; -fx-font-style: italic;");
        productListView.setPlaceholder(placeholder);
    }

    private void loadProductData() {
        productList.clear();
        productList.addAll(PS.ReadAll());
        filteredList.setAll(productList);
        productListView.setItems(filteredList);
        updateTotalLabel();
    }


    private void setupSearchListener() {
        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filterProducts(newValue);
            });
        }
    }

    private void filterProducts(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            filteredList.setAll(productList);
        } else {
            filteredList.clear();
            String lowerCaseFilter = searchText.toLowerCase();

            for (Product product : productList) {
                if (product.getCategory().name().toLowerCase().contains(lowerCaseFilter) ||
                        product.getDescription().toLowerCase().contains(lowerCaseFilter) ||
                        String.valueOf(product.getProductId()).contains(lowerCaseFilter)) {
                    filteredList.add(product);
                }
            }
        }
        updateTotalLabel();
    }

    private void updateTotalLabel() {
        if (totalProductsLabel != null) {
            int total = filteredList.size();
            totalProductsLabel.setText(String.format("Total: %d produit%s", total, total > 1 ? "s" : ""));
        }
    }

    @FXML
    private void handleSearch() {
        String searchText = searchField.getText();
        filterProducts(searchText);
    }

    @FXML
    private void handleReset() {
        searchField.clear();
        filteredList.setAll(productList);
        updateTotalLabel();
    }


    private void handleSub(Product product) {
        // Create a custom dialog
        Dialog<SubscriptionType> dialog = new Dialog<>();
        dialog.setTitle("Abonnement");
        dialog.setHeaderText("Choisissez votre type d'abonnement pour :\n" + formatCategoryName(product.getCategory().name()));

        // Set the button types
        ButtonType confirmButtonType = new ButtonType("Confirmer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

        // Create the content
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        Label choiceLabel = new Label("Type d'abonnement :");
        choiceLabel.setFont(Font.font("System Bold", 13));

        ToggleGroup toggleGroup = new ToggleGroup();

        RadioButton monthlyBtn = new RadioButton("Mensuel (MONTHLY)");
        RadioButton annualBtn  = new RadioButton("Annuel (ANNUAL)");
        RadioButton transactionBtn = new RadioButton("Par transaction (TRANSACTION)");
        RadioButton oneTimeBtn = new RadioButton("Unique (ONE_TIME)");

        monthlyBtn.setToggleGroup(toggleGroup);
        annualBtn.setToggleGroup(toggleGroup);
        transactionBtn.setToggleGroup(toggleGroup);
        oneTimeBtn.setToggleGroup(toggleGroup);

        // Map each button to its SubscriptionType
        monthlyBtn.setUserData(SubscriptionType.MONTHLY);
        annualBtn.setUserData(SubscriptionType.ANNUAL);
        transactionBtn.setUserData(SubscriptionType.TRANSACTION);
        oneTimeBtn.setUserData(SubscriptionType.ONE_TIME);

        monthlyBtn.setSelected(true); // default selection

        content.getChildren().addAll(choiceLabel, monthlyBtn, annualBtn, transactionBtn, oneTimeBtn);
        dialog.getDialogPane().setContent(content);

        // Disable confirm button if nothing selected (safety)
        Node confirmButton = dialog.getDialogPane().lookupButton(confirmButtonType);
        confirmButton.setDisable(false);

        // Convert result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                Toggle selected = toggleGroup.getSelectedToggle();
                if (selected != null) {
                    return (SubscriptionType) selected.getUserData();
                }
            }
            return null;
        });

        Optional<SubscriptionType> result = dialog.showAndWait();

        result.ifPresent(subscriptionType -> {
            try {
                PSS = new ProductSubscriptionService();
                Wallet w = WS.getWalletByMail(SessionContext.getInstance().getCurrentUser().getEmail());
                if(w.getSolde()> product.getPrice()){
                    ProductSubscription productSubscription = new ProductSubscription(SessionContext.getInstance().getCurrentUser().getId(), product.getProductId(), subscriptionType);
                    WS.modifiersolde(w.getId_wallet(), w.getSolde()-product.getPrice());
                    boolean success = PSS.add(productSubscription);
                    if (success) {
                        showAlert(Alert.AlertType.INFORMATION, "Succès",
                                "Abonnement " + subscriptionType.name() + " ajouté avec succès !");

                        // Fire webhook asynchronously — never block the FX thread
                        String productCategorie = product.getCategory().name();
                        String productType      = subscriptionType.name();
                        String price            = product.getPrice().toString();

                        String jsonBody = String.format("""
                        {
                            "ProductCategorie": "%s",
                            "ProductType": "%s",
                            "Price": "%s"
                        }
                        """, productCategorie, productType, price);
                        CompletableFuture.runAsync(() -> {
                            String webhookUrl = "http://localhost:5680/webhook/775c96dd-935c-455d-a9d4-5cb84ff1ea8a";
                            // Step 1: Test basic connectivity first
                            try {
                                java.net.Socket socket = new java.net.Socket();
                                socket.connect(new java.net.InetSocketAddress("localhost", 5680), 2000);
                                socket.close();
                                OkHttpClient client = new OkHttpClient.Builder()
                                        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                                        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                                        .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                                        .build();

                                okhttp3.RequestBody body = okhttp3.RequestBody.create(
                                        jsonBody,
                                        okhttp3.MediaType.parse("application/json")
                                );

                                okhttp3.Request request = new okhttp3.Request.Builder()
                                        .url(webhookUrl)
                                        .post(body)
                                        .build();

                                try (okhttp3.Response response = client.newCall(request).execute()) {
                                    System.out.println("✅ Status: " + response.code());
                                    System.out.println("✅ Body: " + response.body().string());
                                }


                            } catch (Exception e) {
                                System.out.println("❌ Error: " + e.getClass().getSimpleName() + ": " + e.getMessage());
                                e.printStackTrace();
                            }
                        });
                        String invoiceNumber = "INV-" +
                                java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE)
                                + "-" +
                                System.currentTimeMillis() % 1000;
                        String jsonBody1 = String.format("""
                            {
                                "invoiceNumber": "%s",
                                "subscriptionId": "%s",
                                "customerName": "%s",
                                "customerEmail": "%s",
                                "productDescription": "%s",
                                "productCategory": "%s",
                                "price": %s,
                                "TVA": %s
                            }
                            """, invoiceNumber, productSubscription.getSubscriptionId(), SessionContext.getInstance().getCurrentUser().getNom(),
                                SessionContext.getInstance().getCurrentUser().getEmail(), product.getDescription(), productCategorie, price, 19);
                        CompletableFuture.runAsync(() -> {
                            String webhookUrl = "http://localhost:5680/webhook/generate-bankfintrust-invoice";
                            // Step 1: Test basic connectivity first
                            try {
                                java.net.Socket socket = new java.net.Socket();
                                socket.connect(new java.net.InetSocketAddress("localhost", 5680), 2000);
                                socket.close();
                                OkHttpClient client = new OkHttpClient.Builder()
                                        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                                        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                                        .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                                        .build();

                                okhttp3.RequestBody body = okhttp3.RequestBody.create(
                                        jsonBody1,
                                        okhttp3.MediaType.parse("application/json")
                                );

                                okhttp3.Request request = new okhttp3.Request.Builder()
                                        .url(webhookUrl)
                                        .post(body)
                                        .build();

                                try (okhttp3.Response response = client.newCall(request).execute()) {
                                    System.out.println("✅ Status: " + response.code());
                                    System.out.println("✅ Body: " + response.body().string());
                                }


                            } catch (Exception e) {
                                System.out.println("❌ Error: " + e.getClass().getSimpleName() + ": " + e.getMessage());
                                e.printStackTrace();
                            }
                        });
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Erreur",
                                "Échec de l'abonnement. Veuillez réessayer.");
                    }
                }else {
                    showAlert(Alert.AlertType.ERROR, "Erreur",
                            "Montant insuffisant.");
                }
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue : " + e.getMessage());
            }
        });
    }

    private void handleDelete(Product product) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation de suppression");
        confirmAlert.setHeaderText("Supprimer le produit ");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer ce produit?\n\n" +
                "Catégorie: " + formatCategoryName(product.getCategory().name()) + "\n" +
                "Prix: " + String.format("%.2f DT", product.getPrice()) + "\n\n" +
                "Cette action est irréversible!");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (PS.delete(product.getProductId())) {
                showSuccessAlert("Succès", "Le produit a été supprimé avec succès!");
                loadProductData();
            } else {
                showErrorAlert("Erreur", "Erreur lors de la suppression du produit.");
            }
        }
    }

    private String formatCategoryName(String category) {
        if (category == null) return "";
        return category.replace("_", " ");
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
    private class ProductListCell extends ListCell<Product> {
        private final VBox container;
        private final HBox headerBox;
        private final HBox bodyBox;
        private final HBox footerBox;

        private final Label idLabel;
        private final Label categoryLabel;
        private final Label priceLabel;
        private final Label descriptionLabel;
        private final Label dateLabel;
        private final Button SubButton;

        public ProductListCell() {
            super();

            // Container
            container = new VBox(10);
            container.setPadding(new Insets(15));
            container.getStyleClass().add("product-card");

            // Header with ID, Category, and Price
            headerBox = new HBox(15);
            headerBox.setAlignment(Pos.CENTER_LEFT);

            idLabel = new Label();
            idLabel.getStyleClass().add("product-id");
            idLabel.setFont(Font.font("System Bold", 14));

            categoryLabel = new Label();
            categoryLabel.getStyleClass().add("product-category-badge");

            Region spacer1 = new Region();
            HBox.setHgrow(spacer1, Priority.ALWAYS);

            priceLabel = new Label();
            priceLabel.getStyleClass().add("product-price");
            priceLabel.setFont(Font.font("System Bold", 16));

            headerBox.getChildren().addAll(idLabel, categoryLabel, spacer1, priceLabel);

            // Body with Description
            bodyBox = new HBox();
            bodyBox.setAlignment(Pos.CENTER_LEFT);

            descriptionLabel = new Label();
            descriptionLabel.getStyleClass().add("product-description");
            descriptionLabel.setWrapText(true);
            descriptionLabel.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(descriptionLabel, Priority.ALWAYS);

            bodyBox.getChildren().add(descriptionLabel);

            // Footer with Date and Action Buttons
            footerBox = new HBox(10);
            footerBox.setAlignment(Pos.CENTER_LEFT);

            dateLabel = new Label();
            dateLabel.getStyleClass().add("product-date");
            dateLabel.setFont(Font.font("System", 11));

            Region spacer2 = new Region();
            HBox.setHgrow(spacer2, Priority.ALWAYS);

            SubButton = new Button("Abonner");
            SubButton.getStyleClass().add("btn-update");

            footerBox.getChildren().addAll(dateLabel, spacer2, SubButton);

            // Add all sections to container
            container.getChildren().addAll(headerBox, new Separator(), bodyBox, footerBox);
        }

        @Override
        protected void updateItem(Product product, boolean empty) {
            super.updateItem(product, empty);

            if (empty || product == null) {
                setGraphic(null);
            } else {
                // Set ID
//                idLabel.setText("#" + product.getProductId());

                // Set Category with styling
                String category = formatCategoryName(product.getCategory().name());
                categoryLabel.setText(category);
                categoryLabel.setStyle(getCategoryStyle(product.getCategory().name()));

                // Set Price
                priceLabel.setText(String.format("%.2f DT", product.getPrice()));

                // Set Description
                descriptionLabel.setText(product.getDescription());

                // Set Date
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                dateLabel.setText("📅 Créé le: " + product.getCreatedAt().format(formatter));

                // Set button actions
                SubButton.setOnAction(e -> handleSub(product));
                setGraphic(container);
            }
        }

        private String getCategoryStyle(String category) {
            if (category == null) return "";

            if (category.startsWith("COMPTE")) {
                return "-fx-background-color: #e3f2fd; -fx-text-fill: #1976D2; " +
                        "-fx-background-radius: 12px; -fx-padding: 4px 12px; -fx-font-weight: bold; -fx-font-size: 11px;";
            } else if (category.startsWith("CARTE")) {
                return "-fx-background-color: #f3e5f5; -fx-text-fill: #7B1FA2; " +
                        "-fx-background-radius: 12px; -fx-padding: 4px 12px; -fx-font-weight: bold; -fx-font-size: 11px;";
            } else if (category.startsWith("EPARGNE") || category.contains("DEPOT") || category.contains("PLACEMENT")) {
                return "-fx-background-color: #e8f5e9; -fx-text-fill: #2e7d32; " +
                        "-fx-background-radius: 12px; -fx-padding: 4px 12px; -fx-font-weight: bold; -fx-font-size: 11px;";
            } else if (category.startsWith("ASSURANCE")) {
                return "-fx-background-color: #fff3e0; -fx-text-fill: #e65100; " +
                        "-fx-background-radius: 12px; -fx-padding: 4px 12px; -fx-font-weight: bold; -fx-font-size: 11px;";
            }

            return "-fx-background-color: #f5f5f5; -fx-text-fill: #666666; " +
                    "-fx-background-radius: 12px; -fx-padding: 4px 12px; -fx-font-weight: bold; -fx-font-size: 11px;";
        }
    }
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
