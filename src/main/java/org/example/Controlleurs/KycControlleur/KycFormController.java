package org.example.Controlleurs.KycControlleur;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.TextFormatter;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.UnaryOperator;

public class KycFormController {
    private static final long MAX_FILE_SIZE = 5L * 1024L * 1024L;
    private static final Set<String> ALLOWED_EXT = Set.of("pdf", "jpg", "jpeg", "png");
    private static final Set<String> ALLOWED_MIME = Set.of("application/pdf", "image/jpeg", "image/png");

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
    private Canvas signatureCanvas;

    @FXML
    private Label signatureInfoLabel;
    @FXML
    private ProgressBar submitProgressBar;
    @FXML
    private Label submitProgressLabel;
    @FXML
    private Button submitKycBtn;

    @FXML
    private Button saveSignatureBtn;

    @FXML
    private Button clearSignatureBtn;

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
    private GraphicsContext signatureGc;
    private boolean signatureDirty;
    private Timeline submitProgressTimeline;
    private boolean submitInProgress;

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

        // CIN est String, mais uniquement chiffres (max 8) des la saisie.
        UnaryOperator<TextFormatter.Change> cinFilter = change -> {
            String next = change.getControlNewText();
            if (next == null) return null;
            if (!next.matches("\\d*")) return null;
            if (next.length() > 8) return null;
            return change;
        };
        cinField.setTextFormatter(new TextFormatter<>(cinFilter));
        cinField.setPromptText("CIN (8 chiffres)");

        initSignatureCanvas();
        loadState();
        loadExistingFiles();
        if (submitProgressBar != null) {
            submitProgressBar.setProgress(0);
        }
    }

    @FXML
    private void clearSignature() {
        resetSignatureCanvas();
        signatureDirty = false;
        signatureInfoLabel.setText("Signature effacee.");
    }

    @FXML
    private void saveSignature() {
        if (currentStatus == KycStatus.APPROUVE) {
            setInfo("KYC approuve: signature verrouillee.", true);
            return;
        }
        if (isSignatureBlank()) {
            setInfo("Veuillez dessiner votre signature avant enregistrement.", true);
            return;
        }

        try {
            byte[] png = signatureToPng();
            String path = kycService.saveClientSignature(session.getCurrentUser(), png);
            signatureInfoLabel.setText("Signature enregistree: " + path);
            setInfo("Signature KYC enregistree avec succes.", false);
            signatureDirty = false;
            loadState();
        } catch (Exception e) {
            setInfo("Erreur sauvegarde signature: " + e.getMessage(), true);
        }
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
        for (File file : files) {
            String fileError = validateFile(file);
            if (fileError != null) {
                setInfo(fileError, true);
                return;
            }
        }

        selectedFiles.clear();
        selectedFiles.addAll(files);
        refreshSelectedFilesList();
        setInfo(files.size() + " fichier(s) selectionne(s).", false);
    }

    @FXML
    private void submitKyc() {
            if (submitInProgress) {
                setInfo("Soumission deja en cours...", false);
                return;
            }
            if (selectedFiles.isEmpty()) {
                setInfo("Selectionnez des fichiers avant soumission.", true);
                return;
            }
            if (currentStatus == KycStatus.APPROUVE) {
                setInfo("KYC approuve: modification desactivee.", true);
                return;
            }

            String cin = cinField.getText() == null ? "" : cinField.getText().trim();
            if (!cin.matches("\\d{8}")) {
                setInfo("Le CIN doit contenir exactement 8 chiffres.", true);
                return;
            }

            String adresse = adresseField.getText();
            LocalDate dateNaissance = dateNaissancePicker.getValue();
            startSubmitProgress();

            Task<KycSubmitResult> submitTask = new Task<>() {
                @Override
                protected KycSubmitResult call() throws Exception {
                    List<UploadDoc> docs = new ArrayList<>();
                    for (File file : selectedFiles) {
                        byte[] data = Files.readAllBytes(file.toPath());
                        String mime = Files.probeContentType(file.toPath());
                        docs.add(new UploadDoc(file.getName(), mime, file.length(), data));
                    }
                    return kycService.submitOrUpdateClientKyc(session.getCurrentUser(), cin, adresse, dateNaissance, docs);
                }
            };

            submitTask.setOnSucceeded(evt -> {
                KycSubmitResult result = submitTask.getValue();
                if (result == null || !result.isSuccess()) {
                    String msg = result == null ? "Soumission KYC echouee." : result.getMessage();
                    finishSubmitProgress(false, msg);
                    setInfo(msg, true);
                } else {
                    session.setCurrentKycStatus(KycStatus.EN_ATTENTE);
                    session.setCurrentKycComment(null);
                    selectedFiles.clear();
                    loadState();
                    loadExistingFiles();
                    finishSubmitProgress(true, "Soumission terminee.");
                    setInfo(result.getMessage(), false);
                }
                submitInProgress = false;
                if (submitKycBtn != null) submitKycBtn.setDisable(false);
            });

            submitTask.setOnFailed(evt -> {
                Throwable ex = submitTask.getException();
                String msg = ex == null ? "Erreur soumission KYC." : ex.getMessage();
                finishSubmitProgress(false, msg);
                setInfo("Erreur soumission: " + msg, true);
                submitInProgress = false;
                if (submitKycBtn != null) submitKycBtn.setDisable(false);
            });

            submitInProgress = true;
            if (submitKycBtn != null) submitKycBtn.setDisable(true);
            Thread t = new Thread(submitTask, "kyc-submit-thread");
            t.setDaemon(true);
            t.start();
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
    private void goToSmartBreakFromKyc() {
        session.setSmartBreakContext("KYC");
        navigateTo("/Client/SmartBreakHub.fxml", "Pause Intelligente", "/Styles/StyleWallet.css");
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
            String signaturePath = state.getSignaturePath();
            if (signaturePath == null || signaturePath.isBlank()) {
                signatureInfoLabel.setText("Aucune signature enregistree.");
            } else {
                signatureInfoLabel.setText("Signature: " + signaturePath);
            }
            boolean readOnly = state.getStatus() == KycStatus.APPROUVE;
            cinField.setDisable(readOnly);
            adresseField.setDisable(readOnly);
            dateNaissancePicker.setDisable(readOnly);
            signatureCanvas.setDisable(readOnly);
            saveSignatureBtn.setDisable(readOnly);
            clearSignatureBtn.setDisable(readOnly);
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

    private String validateFile(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            return "Un fichier selectionne est invalide.";
        }
        long size = file.length();
        if (size <= 0) {
            return "Le fichier '" + file.getName() + "' est vide.";
        }
        if (size > MAX_FILE_SIZE) {
            return "Le fichier '" + file.getName() + "' depasse 5MB.";
        }

        String name = file.getName() == null ? "" : file.getName().trim().toLowerCase(Locale.ROOT);
        int dot = name.lastIndexOf('.');
        String ext = dot >= 0 ? name.substring(dot + 1) : "";
        if (!ALLOWED_EXT.contains(ext)) {
            return "Type non autorise pour '" + file.getName() + "'. Formats: PDF/JPG/JPEG/PNG.";
        }

        try {
            String mime = Files.probeContentType(file.toPath());
            if (mime != null && !mime.isBlank()) {
                String normalized = mime.toLowerCase(Locale.ROOT);
                if (!ALLOWED_MIME.contains(normalized)) {
                    return "MIME non autorise pour '" + file.getName() + "' : " + mime;
                }
            }
        } catch (IOException ignored) {
            // Si le type MIME est indisponible, on s'appuie sur extension + taille.
        }
        return null;
    }

    private void initSignatureCanvas() {
        signatureGc = signatureCanvas.getGraphicsContext2D();
        signatureGc.setStroke(Color.web("#0f3b7a"));
        signatureGc.setLineWidth(2.5);
        resetSignatureCanvas();

        signatureCanvas.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            if (signatureCanvas.isDisabled()) {
                return;
            }
            signatureGc.beginPath();
            signatureGc.moveTo(e.getX(), e.getY());
            signatureGc.stroke();
            signatureDirty = true;
        });

        signatureCanvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> {
            if (signatureCanvas.isDisabled()) {
                return;
            }
            signatureGc.lineTo(e.getX(), e.getY());
            signatureGc.stroke();
            signatureDirty = true;
        });
    }

    private void resetSignatureCanvas() {
        signatureGc.setFill(Color.WHITE);
        signatureGc.fillRect(0, 0, signatureCanvas.getWidth(), signatureCanvas.getHeight());
        signatureGc.setStroke(Color.web("#c7d8f8"));
        signatureGc.setLineWidth(1);
        signatureGc.strokeRect(0.5, 0.5, signatureCanvas.getWidth() - 1, signatureCanvas.getHeight() - 1);
        signatureGc.setStroke(Color.web("#0f3b7a"));
        signatureGc.setLineWidth(2.5);
    }

    private boolean isSignatureBlank() {
        if (!signatureDirty) {
            return true;
        }
        WritableImage img = new WritableImage((int) signatureCanvas.getWidth(), (int) signatureCanvas.getHeight());
        signatureCanvas.snapshot(null, img);
        PixelReader reader = img.getPixelReader();
        int w = (int) img.getWidth();
        int h = (int) img.getHeight();
        for (int y = 0; y < h; y += 6) {
            for (int x = 0; x < w; x += 6) {
                int rgb = reader.getArgb(x, y) & 0xFFFFFF;
                if (rgb != 0xFFFFFF) {
                    return false;
                }
            }
        }
        return true;
    }

    private byte[] signatureToPng() throws IOException {
        WritableImage img = new WritableImage((int) signatureCanvas.getWidth(), (int) signatureCanvas.getHeight());
        signatureCanvas.snapshot(null, img);
        PixelReader reader = img.getPixelReader();
        int w = (int) img.getWidth();
        int h = (int) img.getHeight();
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                bi.setRGB(x, y, reader.getArgb(x, y));
            }
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(bi, "png", out);
        return out.toByteArray();
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

    private void startSubmitProgress() {
        if (submitProgressBar == null || submitProgressLabel == null) return;
        submitProgressBar.setProgress(0.02);
        submitProgressLabel.setText("Envoi des documents... 2%");
        submitProgressLabel.setStyle("-fx-text-fill: #0f4e96; -fx-font-weight: 700;");

        if (submitProgressTimeline != null) {
            submitProgressTimeline.stop();
        }
        submitProgressTimeline = new Timeline(new KeyFrame(Duration.millis(120), e -> {
            double p = submitProgressBar.getProgress();
            if (p < 0.92) {
                p = Math.min(0.92, p + 0.025);
                submitProgressBar.setProgress(p);
                submitProgressLabel.setText("Envoi des documents... " + (int) Math.round(p * 100) + "%");
            }
        }));
        submitProgressTimeline.setCycleCount(Timeline.INDEFINITE);
        submitProgressTimeline.play();
    }

    private void finishSubmitProgress(boolean success, String message) {
        if (submitProgressTimeline != null) {
            submitProgressTimeline.stop();
        }
        if (submitProgressBar == null || submitProgressLabel == null) return;

        if (success) {
            submitProgressBar.setProgress(1.0);
            submitProgressLabel.setText("Soumission terminee ✔");
            submitProgressLabel.setStyle("-fx-text-fill: #0f7a3b; -fx-font-weight: 800;");
        } else {
            submitProgressBar.setProgress(0);
            submitProgressLabel.setText("Soumission echouee: " + (message == null ? "Erreur." : message));
            submitProgressLabel.setStyle("-fx-text-fill: #b91c1c; -fx-font-weight: 700;");
        }
    }
}

