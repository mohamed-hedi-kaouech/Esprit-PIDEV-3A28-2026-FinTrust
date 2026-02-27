package org.example.Controlleurs.AdminControlleur;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.Model.Kyc.KycFile;
import org.example.Model.User.User;
import org.example.Model.User.UserRole;
import org.example.Service.KycService.KycAdminRow;
import org.example.Service.KycService.KycFileDownload;
import org.example.Service.KycService.KycService;
import org.example.Service.QrService.AdminQrScanService;
import org.example.Utils.SessionContext;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdminKycValidationController {
    private static final Pattern JSON_PAIR = Pattern.compile("\"([^\"]+)\"\\s*:\\s*\"([^\"]*)\"");

    @FXML private TableView<KycAdminRow> kycTable;
    @FXML private TableColumn<KycAdminRow, Number> kycIdCol;
    @FXML private TableColumn<KycAdminRow, String> clientCol;
    @FXML private TableColumn<KycAdminRow, String> emailCol;
    @FXML private TableColumn<KycAdminRow, String> cinCol;
    @FXML private TableColumn<KycAdminRow, String> dateNaissanceCol;
    @FXML private TableColumn<KycAdminRow, String> statusCol;
    @FXML private TableColumn<KycAdminRow, Number> filesCountCol;
    @FXML private TableColumn<KycAdminRow, String> submittedCol;

    @FXML private TableView<KycFile> filesTable;
    @FXML private TableColumn<KycFile, Number> fileIdCol;
    @FXML private TableColumn<KycFile, String> fileNameCol;
    @FXML private TableColumn<KycFile, String> fileTypeCol;

    @FXML private TextArea commentaireField;
    @FXML private Label infoLabel;

    private final KycService kycService = new KycService();
    private final AdminQrScanService qrScanService = new AdminQrScanService();
    private final SessionContext session = SessionContext.getInstance();
    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    private void initialize() {
        User user = session.getCurrentUser();
        if (user == null || user.getRole() != UserRole.ADMIN) {
            navigateTo("/Auth/Login.fxml", "Connexion", "/Styles/StyleWallet.css");
            return;
        }

        kycIdCol.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getKycId()));
        clientCol.setCellValueFactory(d -> new SimpleStringProperty(safe(d.getValue().getNomComplet())));
        emailCol.setCellValueFactory(d -> new SimpleStringProperty(safe(d.getValue().getEmail())));
        cinCol.setCellValueFactory(d -> new SimpleStringProperty(safe(d.getValue().getCin())));
        dateNaissanceCol.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getDateNaissance() == null ? "-" : d.getValue().getDateNaissance().toString()
        ));
        statusCol.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getStatut() == null ? "-" : d.getValue().getStatut().name()
        ));
        filesCountCol.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getFilesCount()));
        submittedCol.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getDateSubmission() == null ? "-" : d.getValue().getDateSubmission().format(DT)
        ));

        fileIdCol.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getId()));
        fileNameCol.setCellValueFactory(d -> new SimpleStringProperty(safe(d.getValue().getFileName())));
        fileTypeCol.setCellValueFactory(d -> new SimpleStringProperty(safe(d.getValue().getFileType())));

        kycTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                loadFiles(newV.getKycId());
                commentaireField.setText(newV.getCommentaireAdmin() == null ? "" : newV.getCommentaireAdmin());
            }
        });

        refreshList();
    }

    @FXML
    private void handleApprove() {
        KycAdminRow row = selectedRow();
        if (row == null) return;

        try {
            kycService.adminValidate(session.getCurrentUser(), row.getKycId());
            refreshList();
            setInfo("KYC approuve.", false);
        } catch (Exception e) {
            e.printStackTrace();
            setInfo("Erreur: " + e.getMessage(), true);
        }
    }

    @FXML
    private void handlePending() {
        KycAdminRow row = selectedRow();
        if (row == null) return;

        try {
            kycService.adminSetPending(session.getCurrentUser(), row.getKycId());
            refreshList();
            setInfo("KYC mis en attente.", false);
        } catch (Exception e) {
            e.printStackTrace();
            setInfo("Erreur: " + e.getMessage(), true);
        }
    }

    @FXML
    private void handleRefuse() {
        KycAdminRow row = selectedRow();
        if (row == null) return;

        String comment = commentaireField.getText() == null ? "" : commentaireField.getText().trim();
        if (comment.isBlank()) {
            setInfo("Commentaire obligatoire pour Refuser.", true);
            return;
        }

        try {
            kycService.adminRefuse(session.getCurrentUser(), row.getKycId(), comment);
            refreshList();
            setInfo("KYC refuse.", false);
        } catch (Exception e) {
            e.printStackTrace();
            setInfo("Erreur: " + e.getMessage(), true);
        }
    }

    @FXML
    private void handleDownloadFile() {
        KycFile file = filesTable.getSelectionModel().getSelectedItem();
        if (file == null) {
            setInfo("Selectionnez un fichier.", true);
            return;
        }

        try {
            KycFileDownload dl = kycService.downloadKycFile(session.getCurrentUser(), file.getId());

            FileChooser chooser = new FileChooser();
            chooser.setInitialFileName(dl.getFileName());
            File dest = chooser.showSaveDialog(getStage());
            if (dest == null) return;

            Files.write(dest.toPath(), dl.getFileData());
            setInfo("Fichier telecharge.", false);
        } catch (Exception e) {
            e.printStackTrace();
            setInfo("Telechargement impossible: " + e.getMessage(), true);
        }
    }

    @FXML
    private void refreshList() {
        try {
            List<KycAdminRow> rows = kycService.listKycForAdmin(session.getCurrentUser());
            kycTable.getItems().setAll(rows);
            filesTable.getItems().clear();
        } catch (Exception e) {
            e.printStackTrace();
            setInfo("Erreur chargement: " + e.getMessage(), true);
        }
    }

    @FXML
    private void handleScanQr() {
        try {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Scanner un QR KYC (image)");
            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Images QR", "*.png", "*.jpg", "*.jpeg")
            );
            File image = chooser.showOpenDialog(getStage());
            if (image == null) return;

            String rawPayload = qrScanService.decodeTokenFromImage(image);
            int userId = qrScanService.resolveUserIdAndConsume(rawPayload);

            refreshList();
            boolean selected = selectKycRowByUserId(userId);
            if (!selected) {
                setInfo("Token valide, mais aucun dossier KYC trouve pour userId=" + userId, true);
                return;
            }
            KycAdminRow row = kycTable.getSelectionModel().getSelectedItem();
            if (row != null) {
                showQrScanCard(rawPayload, row);
            }
            setInfo("QR scanne avec succes. Dossier KYC charge.", false);
        } catch (Exception e) {
            e.printStackTrace();
            setInfo("Scan QR impossible: " + e.getMessage(), true);
        }
    }

    @FXML
    private void goToMenu() {
        navigateTo("/MenuGUI.fxml", "Menu Principal", "/Styles/MenuStyle.css");
    }

    @FXML
    private void goToDashboard() {
        navigateTo("/Admin/UserDashboard.fxml", "Dashboard Admin", "/Styles/StyleWallet.css");
    }

    @FXML
    private void goToAnalyticsDashboard() {
        navigateTo("/Admin/AnalyticsDashboard.fxml", "Data Analytics Dashboard", "/Styles/StyleWallet.css");
    }

    @FXML
    private void goToAdminTasks() {
        navigateTo("/Admin/AdminTasks.fxml", "Admin Productivity / Ops", "/Styles/StyleWallet.css");
    }

    @FXML
    private void goToKycValidation() {
        refreshList();
    }

    @FXML
    private void goToCreateUserForm() {
        navigateTo("/Admin/UserCreate.fxml", "Creation Utilisateur", "/Styles/StyleWallet.css");
    }

    @FXML
    private void goToWalletDashboard() {
        navigateTo("/Wallet/dashboard.fxml", "Wallet", "/Styles/StyleWallet.css");
    }

    @FXML
    private void goToProducts() {
        navigateTo("/Product/ListeProductGUI.fxml", "Produits", "/Styles/StyleWallet.css");
    }

    @FXML
    private void goToPublications() {
        setInfo("Module Publications: ouvrez depuis le menu principal.", false);
        goToMenu();
    }

    @FXML
    private void goToBudget() {
        setInfo("Module Budget: ouvrez depuis le menu principal.", false);
        goToMenu();
    }

    @FXML
    private void goToLoans() {
        setInfo("Module Loans: ouvrez depuis le menu principal.", false);
        goToMenu();
    }

    @FXML
    private void handleLogout() {
        session.logout();
        navigateTo("/Auth/Login.fxml", "Connexion", "/Styles/StyleWallet.css");
    }

    @FXML
    private void handleBack() {
        goToDashboard();
    }

    private void loadFiles(int kycId) {
        try {
            filesTable.getItems().setAll(
                    kycService.listKycFilesForAdmin(session.getCurrentUser(), kycId)
            );
        } catch (Exception e) {
            e.printStackTrace();
            setInfo("Erreur lecture fichiers: " + e.getMessage(), true);
        }
    }

    private KycAdminRow selectedRow() {
        KycAdminRow row = kycTable.getSelectionModel().getSelectedItem();
        if (row == null) setInfo("Selectionnez une ligne KYC.", true);
        return row;
    }

    private boolean selectKycRowByUserId(int userId) {
        List<KycAdminRow> rows = kycTable.getItems();
        if (rows == null || rows.isEmpty()) return false;

        for (KycAdminRow row : rows) {
            if (row.getUserId() == userId) {
                kycTable.getSelectionModel().select(row);
                kycTable.scrollTo(row);
                loadFiles(row.getKycId());
                commentaireField.setText(row.getCommentaireAdmin() == null ? "" : row.getCommentaireAdmin());
                return true;
            }
        }
        return false;
    }

    private void navigateTo(String fxmlPath, String title, String stylesheetPath) {
        try {
            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                showAlert("Erreur", "FXML introuvable: " + fxmlPath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            Scene scene = new Scene(root);
            if (stylesheetPath != null && !stylesheetPath.isBlank()) {
                URL cssUrl = getClass().getResource(stylesheetPath);
                if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            Stage stage = getStage();
            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            setInfo("Navigation impossible: " + e.getMessage(), true);
        }
    }

    private void setInfo(String message, boolean isError) {
        if (infoLabel == null) return;
        infoLabel.setText(message == null ? "" : message);
        infoLabel.setStyle(isError ? "-fx-text-fill: #b91c1c;" : "-fx-text-fill: #166534;");
    }

    private void showAlert(String title, String message) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        a.showAndWait();
    }

    private Stage getStage() {
        return (Stage) kycTable.getScene().getWindow();
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private void showQrScanCard(String rawPayload, KycAdminRow row) {
        Map<String, String> data = parseJsonPairs(rawPayload);

        VBox root = new VBox(12);
        root.setPadding(new Insets(16));
        root.getStyleClass().add("qr-scan-card-root");

        StackPane badge = new StackPane();
        badge.getStyleClass().add("qr-scan-badge-circle");
        Label badgeText = new Label("QR");
        badgeText.getStyleClass().add("qr-scan-badge-text");
        badge.getChildren().add(badgeText);

        Label title = new Label("Donnees scannees");
        title.getStyleClass().add("qr-scan-title");

        Label subtitle = new Label("Lecture QR client + KYC reussie");
        subtitle.getStyleClass().add("qr-scan-subtitle");

        HBox header = new HBox(10, badge, new VBox(2, title, subtitle));
        header.setAlignment(Pos.CENTER_LEFT);

        VBox details = new VBox(6,
                line("Utilisateur", row.getNomComplet()),
                line("Email", row.getEmail()),
                line("User ID", String.valueOf(row.getUserId())),
                line("Statut compte", valueFromPayloadOr(data, "statusCompte", "-")),
                line("Statut KYC", row.getStatut() == null ? "-" : row.getStatut().name()),
                line("CIN", safe(row.getCin())),
                line("Date naissance", row.getDateNaissance() == null ? "-" : row.getDateNaissance().toString()),
                line("Emis le", valueFromPayloadOr(data, "issuedAt", "-"))
        );
        details.getStyleClass().add("qr-scan-details-box");

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Button closeBtn = new Button("Fermer");
        closeBtn.getStyleClass().add("button-secondary");

        HBox footer = new HBox(spacer, closeBtn);
        footer.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(header, details, footer);

        Scene scene = new Scene(root, 430, 360);
        URL cssUrl = getClass().getResource("/Styles/StyleWallet.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        Stage modal = new Stage();
        modal.initOwner(getStage());
        modal.initModality(Modality.WINDOW_MODAL);
        modal.setTitle("Resultat scan QR");
        modal.setScene(scene);
        closeBtn.setOnAction(e -> modal.close());
        modal.showAndWait();
    }

    private HBox line(String key, String value) {
        Label k = new Label(key + " :");
        k.getStyleClass().add("qr-scan-key");
        Label v = new Label(value == null || value.isBlank() ? "-" : value);
        v.getStyleClass().add("qr-scan-value");
        v.setWrapText(true);

        HBox row = new HBox(8, k, v);
        row.setAlignment(Pos.TOP_LEFT);
        return row;
    }

    private Map<String, String> parseJsonPairs(String payload) {
        Map<String, String> map = new HashMap<>();
        if (payload == null || !payload.trim().startsWith("{")) {
            return map;
        }
        Matcher m = JSON_PAIR.matcher(payload);
        while (m.find()) {
            map.put(m.group(1), m.group(2));
        }
        return map;
    }

    private String valueFromPayloadOr(Map<String, String> map, String key, String fallback) {
        String value = map.get(key);
        if (value == null || value.isBlank()) return fallback;
        return value;
    }
}
