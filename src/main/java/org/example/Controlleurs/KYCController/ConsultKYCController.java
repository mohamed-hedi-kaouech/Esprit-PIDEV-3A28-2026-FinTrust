package org.example.Controlleurs.KYCController;
import org.example.Service.UserService.UserService;
import org.example.Service.UserService.KycService;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.example.Model.User.Kyc;
import org.example.Model.User.User;
import org.example.Service.UserService.KycService;

import java.io.ByteArrayInputStream;

public class ConsultKYCController {

    private User user;
    private KycService kycService;

    // ================= FXML Components =================
    @FXML
    private Label userNameLabel;

    @FXML
    private Label documentTypeLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private Label submittedAtLabel;

    @FXML
    private Label reviewedAtLabel;

    @FXML
    private Label commentLabel;

    @FXML
    private ImageView documentFrontImage;

    @FXML
    private ImageView documentBackImage;

    @FXML
    private ImageView signatureImage;

    @FXML
    private ImageView selfieImage;

    // ================= INIT =================
    @FXML
    public void initialize() {
        kycService = new KycService();
    }

    // Méthode appelée depuis ListUsersController pour passer l'utilisateur
    public void setUser(User user) {
        this.user = user;
        loadKycDetails();
    }

    // ================= LOAD KYC DETAILS =================
    private void loadKycDetails() {
        try {
            if (user == null) return;

            // Charger KYC depuis le service
            Kyc kyc = kycService.ReadId(user.getId());

            if (kyc == null) {
                showAlert("Aucun KYC trouvé pour cet utilisateur.");
                return;
            }

            // Remplir les labels
            userNameLabel.setText(user.getNom() + " " + user.getPrenom());
            documentTypeLabel.setText(kyc.getDocumentType());
            statusLabel.setText(kyc.getStatus());
            submittedAtLabel.setText(kyc.getSubmittedAt() != null ? kyc.getSubmittedAt().toString() : "-");
            reviewedAtLabel.setText(kyc.getReviewedAt() != null ? kyc.getReviewedAt().toString() : "-");
            commentLabel.setText(kyc.getComment() != null ? kyc.getComment() : "-");

            // Afficher les images si elles existent
            if (kyc.getDocumentFront() != null)
                documentFrontImage.setImage(new Image(new ByteArrayInputStream(kyc.getDocumentFront())));

            if (kyc.getDocumentBack() != null)
                documentBackImage.setImage(new Image(new ByteArrayInputStream(kyc.getDocumentBack())));

            if (kyc.getSignature() != null)
                signatureImage.setImage(new Image(new ByteArrayInputStream(kyc.getSignature())));

            if (kyc.getSelfie() != null)
                selfieImage.setImage(new Image(new ByteArrayInputStream(kyc.getSelfie())));

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur lors du chargement du KYC : " + e.getMessage());
        }
    }

    // ================= UTILS =================
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Consultation KYC");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
