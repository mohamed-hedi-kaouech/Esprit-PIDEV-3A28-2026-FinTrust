package org.example.Controlleurs.ProductControlleur.Admin;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import okhttp3.*;
import org.example.Model.Product.ClassProduct.Product;
import org.example.Model.Product.ClassProduct.ProductSubscription;
import org.example.Service.ProductService.ProductService;
import org.example.Service.ProductService.ProductSubscriptionService;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class AdminDashboardControlleur implements Initializable {

    // ── KPI Labels ────────────────────────────────────────────
    @FXML private Label labelTotalProducts;
    @FXML private Label labelProductsGrowth;
    @FXML private Label labelAvgPrice;
    @FXML private Label labelTopCategory;
    @FXML private Label labelTopCategoryCount;
    @FXML private Label labelPriceRange;
    @FXML private Label labelNewestProduct;
    @FXML private Label labelNewestDate;

    @FXML private Label labelTotalSubs;
    @FXML private Label labelActiveSubs;
    @FXML private Label labelActiveRate;
    @FXML private Label labelSuspendedSubs;
    @FXML private Label labelSuspendedRate;
    @FXML private Label labelClosedSubs;
    @FXML private Label labelClosedRate;
    @FXML private Label labelTotalRevenue;
    @FXML private Label labelLastRefresh;

    // ── Charts ────────────────────────────────────────────────
    @FXML private PieChart chartSubsByStatus;
    @FXML private BarChart<String, Number> chartProductsByCategory;
    @FXML private BarChart<String, Number> chartSubsByType;
    @FXML private LineChart<String, Number> chartSubsOverTime;
    @FXML private BarChart<String, Number> chartRevenueByCategory;

    private ProductService productService;
    private ProductSubscriptionService subscriptionService;

    private List<Product> products;
    private List<ProductSubscription> subscriptions;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        productService     = new ProductService();
        subscriptionService = new ProductSubscriptionService();
        applyChartStyles();
        loadData();
    }

    @FXML
    private void refreshData() {
        loadData();
    }


    // ══════════════════════════════════════════════════════════
    //  DATA LOADING
    // ══════════════════════════════════════════════════════════
    private void loadData() {
        products      = productService.ReadAll();
        subscriptions = subscriptionService.ReadAll();

        if (products      == null) products      = new ArrayList<>();
        if (subscriptions == null) subscriptions = new ArrayList<>();

        updateProductKPIs();
        updateSubscriptionKPIs();
        updateCharts();

        labelLastRefresh.setText("Dernière actualisation : "
                + java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
    }

    // ══════════════════════════════════════════════════════════
    //  PRODUCT KPIs
    // ══════════════════════════════════════════════════════════
    private void updateProductKPIs() {
        int total = products.size();
        labelTotalProducts.setText(String.valueOf(total));
        labelProductsGrowth.setText(total + " produit" + (total > 1 ? "s" : "") + " dans le catalogue");

        if (!products.isEmpty()) {
            // Average price
            double avg = products.stream()
                    .mapToDouble(p -> p.getPrice() != null ? p.getPrice() : 0)
                    .average().orElse(0);
            labelAvgPrice.setText(String.format("%.2f", avg));

            // Min / Max price
            double min = products.stream().mapToDouble(p -> p.getPrice() != null ? p.getPrice() : 0).min().orElse(0);
            double max = products.stream().mapToDouble(p -> p.getPrice() != null ? p.getPrice() : 0).max().orElse(0);
            labelPriceRange.setText(String.format("%.0f → %.0f", min, max));

            // Newest product
            products.stream()
                    .filter(p -> p.getCreatedAt() != null)
                    .max(Comparator.comparing(Product::getCreatedAt))
                    .ifPresent(p -> {
                        labelNewestProduct.setText(p.getCategory() != null
                                ? p.getCategory().name().replace("_", " ") : "—");
                        labelNewestDate.setText(p.getCreatedAt()
                                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                    });

            // Top category by subscription count
            Map<String, Long> subsByCategory = subscriptions.stream()
                    .filter(s -> s.getProduct() > 0)
                    .collect(Collectors.groupingBy(s -> {
                        return products.stream()
                                .filter(p -> p.getProductId() == s.getProduct())
                                .findFirst()
                                .map(p -> p.getCategory() != null ? p.getCategory().name() : "AUTRE")
                                .orElse("AUTRE");
                    }, Collectors.counting()));

            subsByCategory.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .ifPresent(e -> {
                        labelTopCategory.setText(e.getKey().replace("_", " "));
                        labelTopCategoryCount.setText(e.getValue() + " abonnements");
                    });
        } else {
            labelAvgPrice.setText("0.00");
            labelPriceRange.setText("— → —");
            labelNewestProduct.setText("—");
            labelNewestDate.setText("—");
            labelTopCategory.setText("—");
            labelTopCategoryCount.setText("—");
        }
    }

    // ══════════════════════════════════════════════════════════
    //  SUBSCRIPTION KPIs
    // ══════════════════════════════════════════════════════════
    private void updateSubscriptionKPIs() {
        int total     = subscriptions.size();
        long active   = subscriptions.stream().filter(s -> "ACTIVE".equals(safeStatus(s))).count();
        long suspended= subscriptions.stream().filter(s -> "SUSPENDED".equals(safeStatus(s))).count();
        long closed   = subscriptions.stream().filter(s -> "CLOSED".equals(safeStatus(s))).count();

        labelTotalSubs.setText(String.valueOf(total));
        labelActiveSubs.setText(String.valueOf(active));
        labelSuspendedSubs.setText(String.valueOf(suspended));
        labelClosedSubs.setText(String.valueOf(closed));

        if (total > 0) {
            labelActiveRate.setText(String.format("%.1f%% du total", (active * 100.0) / total));
            labelSuspendedRate.setText(String.format("%.1f%% du total", (suspended * 100.0) / total));
            labelClosedRate.setText(String.format("%.1f%% du total", (closed * 100.0) / total));
        }

        // Estimated revenue: sum of prices for active subscriptions
        double revenue = subscriptions.stream()
                .filter(s -> "ACTIVE".equals(safeStatus(s)))
                .mapToDouble(s -> products.stream()
                        .filter(p -> p.getProductId() == s.getProduct())
                        .findFirst()
                        .map(p -> p.getPrice() != null ? p.getPrice() : 0.0)
                        .orElse(0.0))
                .sum();
        labelTotalRevenue.setText(String.format("%.2f DT", revenue));
    }

    private String safeStatus(ProductSubscription s) {
        return s.getStatus() != null ? s.getStatus().name() : "";
    }

    // ══════════════════════════════════════════════════════════
    //  CHARTS
    // ══════════════════════════════════════════════════════════
    private void updateCharts() {
        buildSubsByStatusPie();
        buildProductsByCategoryBar();
        buildSubsByTypeBar();
        buildSubsOverTimeLine();
        buildRevenueByCategoryBar();
    }

    // Pie: subscriptions by status
    private void buildSubsByStatusPie() {
        Map<String, Long> counts = subscriptions.stream()
                .collect(Collectors.groupingBy(s -> safeStatus(s).isEmpty() ? "UNKNOWN" : safeStatus(s),
                        Collectors.counting()));

        chartSubsByStatus.setData(FXCollections.observableArrayList(
                counts.entrySet().stream()
                        .map(e -> new PieChart.Data(e.getKey() + " (" + e.getValue() + ")", e.getValue()))
                        .collect(Collectors.toList())
        ));

        // Color slices after layout
        chartSubsByStatus.setLegendVisible(true);
        chartSubsByStatus.setLabelsVisible(true);
    }

    // Bar: products count per category
    private void buildProductsByCategoryBar() {
        Map<String, Long> counts = products.stream()
                .filter(p -> p.getCategory() != null)
                .collect(Collectors.groupingBy(
                        p -> shortenCategory(p.getCategory().name()),
                        Collectors.counting()
                ));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Produits");
        counts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .forEach(e -> series.getData().add(new XYChart.Data<>(e.getKey(), e.getValue())));

        chartProductsByCategory.getData().clear();
        chartProductsByCategory.getData().add(series);
        chartProductsByCategory.setLegendVisible(false);
    }

    // Bar: subscriptions by type
    private void buildSubsByTypeBar() {
        Map<String, Long> counts = subscriptions.stream()
                .filter(s -> s.getType() != null)
                .collect(Collectors.groupingBy(s -> s.getType().name(), Collectors.counting()));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Abonnements");
        List.of("MONTHLY", "ANNUAL", "TRANSACTION", "ONE_TIME")
                .forEach(t -> series.getData().add(new XYChart.Data<>(t, counts.getOrDefault(t, 0L))));

        chartSubsByType.getData().clear();
        chartSubsByType.getData().add(series);
        chartSubsByType.setLegendVisible(false);
    }

    // Line: subscriptions per month (subscription date)
    private void buildSubsOverTimeLine() {
        Map<String, Long> perDay = new TreeMap<>(subscriptions.stream()
                .filter(s -> s.getSubscriptionDate() != null)
                .collect(Collectors.groupingBy(
                        s -> s.getSubscriptionDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                        Collectors.counting()
                )));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Nouvelles souscriptions");

        perDay.forEach((day, count) ->
                series.getData().add(new XYChart.Data<>(day, count)));

        chartSubsOverTime.getData().clear();
        chartSubsOverTime.getData().add(series);
    }

    // Joint Bar: revenue (price × active subs) AND sub count per category
    // Revenue per category (count shown in label)
    private void buildRevenueByCategoryBar() {

        // Map productId → product
        Map<Integer, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getProductId, p -> p, (a, b) -> a));

        // Group active subscriptions by category → revenue & count
        Map<String, double[]> dataByCategory = new LinkedHashMap<>();

        subscriptions.stream()
                .filter(s -> "ACTIVE".equals(safeStatus(s)))
                .forEach(s -> {
                    Product p = productMap.get(s.getProduct());
                    if (p == null || p.getCategory() == null) return;

                    String cat = shortenCategory(p.getCategory().name());
                    double price = p.getPrice() != null ? p.getPrice() : 0;

                    dataByCategory.computeIfAbsent(cat, k -> new double[]{0, 0});
                    dataByCategory.get(cat)[0] += price;  // revenue
                    dataByCategory.get(cat)[1] += 1;      // count
                });

        XYChart.Series<String, Number> revenueSeries = new XYChart.Series<>();
        revenueSeries.setName("Revenu estimé (DT)");

        dataByCategory.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue()[0], a.getValue()[0]))
                .forEach(e -> {

                    double revenue = e.getValue()[0];
                    int count = (int) e.getValue()[1];

                    // Add count inside category label
                    String label = e.getKey() + " (" + count + ")";

                    revenueSeries.getData().add(
                            new XYChart.Data<>(label, revenue)
                    );
                });

        chartRevenueByCategory.getData().clear();
        chartRevenueByCategory.getData().add(revenueSeries);
    }

    // ══════════════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════════════
    private String shortenCategory(String cat) {
        if (cat == null) return "?";
        // e.g. COMPTE_COURANT → C. COURANT
        String[] parts = cat.split("_");
        if (parts.length >= 2) {
            return parts[0].charAt(0) + ". " + String.join(" ", Arrays.copyOfRange(parts, 1, parts.length));
        }
        return cat;
    }

    private void applyChartStyles() {
        // Make chart backgrounds transparent to match dark theme
        for (Chart chart : List.of(chartSubsByStatus, chartProductsByCategory,
                chartSubsByType, chartSubsOverTime, chartRevenueByCategory)) {
            if (chart != null) {
                chart.setStyle("-fx-background-color: transparent;");
                chart.lookup(".chart-plot-background") /*.setStyle(...)*/; // handled via CSS
            }
        }
    }

    @FXML
    private void sendEmailReport() {
        // Collect stats from already-loaded data
        int    totalProducts  = products.size();
        int    totalSubs      = subscriptions.size();
        long   activeSubs     = subscriptions.stream().filter(s -> "ACTIVE".equals(safeStatus(s))).count();
        long   suspendedSubs  = subscriptions.stream().filter(s -> "SUSPENDED".equals(safeStatus(s))).count();
        long   closedSubs     = subscriptions.stream().filter(s -> "CLOSED".equals(safeStatus(s))).count();
        long   draftSubs      = subscriptions.stream().filter(s -> "DRAFT".equals(safeStatus(s))).count();

        double avgPrice    = products.stream().mapToDouble(p -> p.getPrice() != null ? p.getPrice() : 0).average().orElse(0);
        double minPrice    = products.stream().mapToDouble(p -> p.getPrice() != null ? p.getPrice() : 0).min().orElse(0);
        double maxPrice    = products.stream().mapToDouble(p -> p.getPrice() != null ? p.getPrice() : 0).max().orElse(0);

        Map<Integer, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getProductId, p -> p, (a, b) -> a));

        double totalRevenue = subscriptions.stream()
                .filter(s -> "ACTIVE".equals(safeStatus(s)))
                .mapToDouble(s -> {
                    Product p = productMap.get(s.getProduct());
                    return p != null && p.getPrice() != null ? p.getPrice() : 0;
                }).sum();

        Map<String, Long> byType = subscriptions.stream()
                .filter(s -> s.getType() != null)
                .collect(Collectors.groupingBy(s -> s.getType().name(), Collectors.counting()));

        String generatedAt = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        // Build JSON — flat structure, same style as your working webhook calls
        String jsonBody = String.format("""
            {
                "generatedAt": "%s",
                "totalProducts": %d,
                "avgPrice": "%.2f",
                "minPrice": "%.2f",
                "maxPrice": "%.2f",
                "totalSubs": %d,
                "activeSubs": %d,
                "suspendedSubs": %d,
                "closedSubs": %d,
                "draftSubs": %d,
                "activeRate": "%.1f",
                "totalRevenue": "%.2f",
                "monthlyCount": %d,
                "annualCount": %d,
                "transactionCount": %d,
                "oneTimeCount": %d
            }
            """,
                generatedAt,
                totalProducts,
                avgPrice, minPrice, maxPrice,
                totalSubs,
                activeSubs, suspendedSubs, closedSubs, draftSubs,
                totalSubs > 0 ? (activeSubs * 100.0 / totalSubs) : 0,
                totalRevenue,
                byType.getOrDefault("MONTHLY",     0L),
                byType.getOrDefault("ANNUAL",      0L),
                byType.getOrDefault("TRANSACTION", 0L),
                byType.getOrDefault("ONE_TIME",    0L)
        );

        System.out.println("📤 Payload:\n" + jsonBody);

        showAlert(Alert.AlertType.INFORMATION, "Rapport en cours...",
                "Génération du rapport PDF en cours.\nVous recevrez l'email dans quelques instants.");

        CompletableFuture.runAsync(() -> {
            String webhookUrl = "http://localhost:5680/webhook/Rapport_Admin";

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
                Platform.runLater(() ->
                        showAlert(Alert.AlertType.ERROR, "Erreur",
                                e.getClass().getSimpleName() + ": " + e.getMessage())
                );
            }
        });
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
                    getClass().getResource("/Product/MenuProductGUI.fxml")
            );
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Manager");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}