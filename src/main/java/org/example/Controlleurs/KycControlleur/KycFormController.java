package org.example.Controlleurs.KycControlleur;

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
import org.example.Model.Kyc.KycStatus;
import org.example.Model.User.User;
import org.example.Model.User.UserRole;
import org.example.Service.KycService.KycFileDownload;
import org.example.Service.KycService.KycService;
import org.example.Service.KycService.KycStateResult;
import org.example.Service.KycService.KycSubmitResult;
import org.example.Service.KycService.UploadDoc;
import org.example.Utils.SessionContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class KycFormController {
    @FXML
    private Label statusLabel;

    @FXML
    private Label commentLabel;

    @FXML
    private Label infoLabel;

    @FXML
    private TextField cinField;

    @FXML
    private TextArea adresseField;

    @FXML
    private DatePicker dateNaissancePicker;

    @FXML
    private ListView<String> selectedFilesList;

    @FXML
    private TableView<KycFile> existingFilesTable;

    @FXML
    private TableColumn<KycFile, String> fileNameCol;

    @FXML
    private TableColumn<KycFile, String> fileTypeCol;

    @FXML
    private TableColumn<KycFile, String> fileSizeCol;

    @FXML
    private TableColumn<KycFile, String> updatedAtCol;

    private final KycService kycService = new KycService();
    private final SessionContext session = SessionContext.getInstance();
    private final List<File> selectedFiles = new ArrayList<>();
    private final List<KycFile> existingFiles = new ArrayList<>();
    private KycStatus currentStatus;

    @FXML
    private void initialize() {
        User user = session.getCurrentUser();
        if (user == null || user.getRole() != UserRole.CLIENT) {
            navigateTo("/Auth/Login.fxml", "Connexion", "/Styles/StyleWallet.css");
            return;
        }

        fileNameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFileName()));
        fileTypeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFileType()));
        fileSizeCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getFileSize())));
        updatedAtCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getUpdatedAt() == null ? "-" : data.getValue().getUpdatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        ));

        loadState();
        loadExistingFiles();
    }

    @FXML
    private void chooseFiles() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Selectionner documents KYC");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Documents KYC", "*.pdf", "*.jpg", "*.jpeg", "*.png")
        );

        List<File> files = chooser.showOpenMultipleDialog(getStage());
        if (files == null || files.isEmpty()) {
            return;
        }

        selectedFiles.clear();
        selectedFiles.addAll(files);
        refreshSelectedFilesList();
        setInfo(files.size() + " fichier(s) selectionne(s).", false);
    }

    @FXML
    private void submitKyc() {
            if (selectedFiles.isEmpty()) {
                setInfo("Selectionnez des fichiers avant soumission.", true);
                return;
            }
            if (currentStatus == KycStatus.APPROUVE) {
                setInfo("KYC approuve: modification desactivee.", true);
                return;
            }

            String cin = cinField.getText();
            if (!cin.matches("\\d{8}")) {
                setInfo("Le CIN doit contenir exactement 8 chiffres.", true);
                return;
            }

            List<UploadDoc> docs = new ArrayList<>();
            try {
                for (File file : selectedFiles) {
                    byte[] data = Files.readAllBytes(file.toPath());
                    String mime = Files.probeContentType(file.toPath());
                    docs.add(new UploadDoc(file.getName(), mime, file.length(), data));
                }

                String adresse = adresseField.getText();
                LocalDate dateNaissance = dateNaissancePicker.getValue();
                KycSubmitResult result = kycService.submitOrUpdateClientKyc(session.getCurrentUser(), cin, adresse, dateNaissance, docs);
                if (!result.isSuccess()) {
                    setInfo(result.getMessage(), true);
                    return;
                }

                session.setCurrentKycStatus(KycStatus.EN_ATTENTE);
                session.setCurrentKycComment(null);
                setInfo(result.getMessage(), false);
                selectedFiles.clear();
                loadState();
                loadExistingFiles();
            } catch (IOException e) {
                setInfo("Erreur lecture fichiers: " + e.getMessage(), true);
            }
    }

    @FXML
    private void downloadSelected() {
        KycFile selected = existingFilesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setInfo("Selectionnez un fichier dans la liste.", true);
            return;
        }

        try {
            KycFileDownload download = kycService.downloadKycFile(session.getCurrentUser(), selected.getId());
            FileChooser chooser = new FileChooser();
            chooser.setInitialFileName(download.getFileName());
            File dest = chooser.showSaveDialog(getStage());
            if (dest == null) {
                return;
            }
            Files.write(dest.toPath(), download.getFileData());
            setInfo("Fichier telecharge avec succes.", false);
        } catch (Exception e) {
            setInfo("Telechargement impossible: " + e.getMessage(), true);
        }
    }

    @FXML
    private void goToClientDashboard() {
        navigateTo("/Client/ClientDashboard.fxml", "Dashboard Client", "/Styles/StyleWallet.css");
    }

    @FXML
    private void handleLogout() {
        session.logout();
        navigateTo("/Auth/Login.fxml", "Connexion", "/Styles/StyleWallet.css");
    }

    private void loadState() {
        try {
            KycStateResult state = kycService.getClientKycState(session.getCurrentUser());
            currentStatus = state.getStatus();
            session.setCurrentKycStatus(state.getStatus());
            session.setCurrentKycComment(state.getCommentaireAdmin());
            statusLabel.setText("Statut KYC: " + state.getStatus().name());
            commentLabel.setText(state.getCommentaireAdmin() == null || state.getCommentaireAdmin().isBlank()
                    ? "Commentaire admin: -"
                    : "Commentaire admin: " + state.getCommentaireAdmin());
            cinField.setText(state.getCin() == null ? "" : state.getCin());
            adresseField.setText(state.getAdresse() == null ? "" : state.getAdresse());
            dateNaissancePicker.setValue(state.getDateNaissance());
            boolean readOnly = state.getStatus() == KycStatus.APPROUVE;
            cinField.setDisable(readOnly);
            adresseField.setDisable(readOnly);
            dateNaissancePicker.setDisable(readOnly);
        } catch (Exception e) {
            setInfo("Erreur chargement KYC: " + e.getMessage(), true);
        }
    }

    private void loadExistingFiles() {
        try {
            List<KycFile> files = kycService.getClientKycFiles(session.getCurrentUser());
            existingFiles.clear();
            existingFiles.addAll(files);
            existingFilesTable.getItems().setAll(files);
            refreshSelectedFilesList();
        } catch (Exception e) {
            setInfo("Erreur chargement fichiers: " + e.getMessage(), true);
        }
    }

    private void refreshSelectedFilesList() {
        List<String> lines = new ArrayList<>();
        for (KycFile file : existingFiles) {
            lines.add("[EXISTANT] " + file.getFileName());
        }
        for (File file : selectedFiles) {
            lines.add("[NOUVEAU] " + file.getName());
        }
        selectedFilesList.getItems().setAll(lines);
    }

    private void navigateTo(String fxmlPath, String title, String stylesheetPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Scene scene = new Scene(root);
            if (stylesheetPath != null && !stylesheetPath.isBlank()) {
                scene.getStylesheets().add(getClass().getResource(stylesheetPath).toExternalForm());
            }
            Stage stage = getStage();
            stage.setScene(scene);
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            setInfo("Navigation impossible: " + e.getMessage(), true);
        }
    }

    private void setInfo(String message, boolean isError) {
        infoLabel.setText(message);
        infoLabel.setStyle(isError ? "-fx-text-fill: #b91c1c;" : "-fx-text-fill: #166534;");
    }

    private Stage getStage() {
        return (Stage) statusLabel.getScene().getWindow();
    }
}
