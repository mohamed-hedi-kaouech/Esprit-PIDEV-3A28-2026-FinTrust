package org.example.Controlleurs.AdminControlleur;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.Model.Kyc.KycFile;
import org.example.Model.User.User;
import org.example.Model.User.UserRole;
import org.example.Service.KycService.KycAdminRow;
import org.example.Service.KycService.KycFileDownload;
import org.example.Service.KycService.KycService;
import org.example.Utils.SessionContext;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AdminKycValidationController {

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
    private final SessionContext session = SessionContext.getInstance();
    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    private void initialize() {
        User user = session.getCurrentUser();
        if (user == null || user.getRole() != UserRole.ADMIN) {
            navigateTo("/Auth/Login.fxml", "Connexion", "/Styles/StyleWallet.css");
            return;
        }

        // Table KYC
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

        // Table fichiers
        fileIdCol.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getId()));
        fileNameCol.setCellValueFactory(d -> new SimpleStringProperty(safe(d.getValue().getFileName())));
        fileTypeCol.setCellValueFactory(d -> new SimpleStringProperty(safe(d.getValue().getFileType())));

        // Selection KYC -> load files + commentaire
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
            setInfo("KYC approuvé.", false);
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
            setInfo("KYC refusé.", false);
        } catch (Exception e) {
            e.printStackTrace();
            setInfo("Erreur: " + e.getMessage(), true);
        }
    }

    @FXML
    private void handleDownloadFile() {
        KycFile file = filesTable.getSelectionModel().getSelectedItem();
        if (file == null) {
            setInfo("Sélectionnez un fichier.", true);
            return;
        }

        try {
            KycFileDownload dl = kycService.downloadKycFile(session.getCurrentUser(), file.getId());

            FileChooser chooser = new FileChooser();
            chooser.setInitialFileName(dl.getFileName());
            File dest = chooser.showSaveDialog(getStage());
            if (dest == null) return;

            Files.write(dest.toPath(), dl.getFileData());
            setInfo("Fichier téléchargé.", false);
        } catch (Exception e) {
            e.printStackTrace();
            setInfo("Téléchargement impossible: " + e.getMessage(), true);
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
    private void handleSendNotification() {
        KycAdminRow row = selectedRow();
        if (row == null) return;

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Envoyer notification");
        dialog.setHeaderText("Notification pour: " + row.getNomComplet() + " (" + row.getEmail() + ")");
        dialog.setContentText("Message:");

        dialog.showAndWait().ifPresent(message -> {
            String msg = message == null ? "" : message.trim();
            if (msg.isBlank()) {
                setInfo("Message vide.", true);
                return;
            }

            try {
                // ✅ Version qui marche tout de suite: envoi EMAIL
                // (assure-toi que EmailService existe)
                org.example.Service.EmailService emailService = new org.example.Service.EmailService();
                emailService.sendNotification(row.getEmail(), msg);

                // ✅ PLUS TARD: enregistrer en DB pour affichage dans le compte client
                // Exemple :
                // NotificationService notif = new NotificationService();
                // notif.createNotification(row.getUserId(), "KYC", msg);

                setInfo("Notification envoyée (email).", false);

            } catch (Exception e) {
                e.printStackTrace();
                setInfo("Erreur notification: " + e.getMessage(), true);
            }
        });
    }

    @FXML
    private void goToMenu() {
        navigateTo("/MenuGUI.fxml", "Menu Principal", "/Styles/MenuStyle.css");
    }

    @FXML
    private void handleLogout() {
        session.logout();
        navigateTo("/Auth/Login.fxml", "Connexion", "/Styles/StyleWallet.css");
    }

    @FXML
    private void handleBack() {
        navigateTo("/Admin/UserDashboard.fxml", "Dashboard Admin", "/Styles/StyleWallet.css");
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
        if (row == null) setInfo("Sélectionnez une ligne KYC.", true);
        return row;
    }

    private void navigateTo(String fxmlPath, String title, String stylesheetPath) {
        try {
            URL fxmlUrl = getClass().getResource(fxmlPath);
            System.out.println("NAVIGATE(KYC) -> " + fxmlPath + " => " + fxmlUrl);

            if (fxmlUrl == null) {
                showAlert("Erreur", "FXML introuvable: " + fxmlPath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            Scene scene = new Scene(root);
            if (stylesheetPath != null && !stylesheetPath.isBlank()) {
                URL cssUrl = getClass().getResource(stylesheetPath);
                System.out.println("CSS(KYC) -> " + stylesheetPath + " => " + cssUrl);
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
}
