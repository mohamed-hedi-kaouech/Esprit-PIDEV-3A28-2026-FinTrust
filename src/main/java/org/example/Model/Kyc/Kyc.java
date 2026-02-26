package org.example.Model.Kyc;

import java.time.LocalDateTime;
import java.time.LocalDate;

public class Kyc {
    private int id;
    private int userId;
    private String cin;
    private String adresse;
    private LocalDate dateNaissance;
    private KycStatus statut;
    private String commentaireAdmin;
    private LocalDateTime dateSubmission;
    private String signaturePath;
    private LocalDateTime signatureUploadedAt;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getCin() {
        return cin;
    }

    public void setCin(String cin) {
        this.cin = cin;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public LocalDate getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(LocalDate dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    public KycStatus getStatut() {
        return statut;
    }

    public void setStatut(KycStatus statut) {
        this.statut = statut;
    }

    public String getCommentaireAdmin() {
        return commentaireAdmin;
    }

    public void setCommentaireAdmin(String commentaireAdmin) {
        this.commentaireAdmin = commentaireAdmin;
    }

    public LocalDateTime getDateSubmission() {
        return dateSubmission;
    }

    public void setDateSubmission(LocalDateTime dateSubmission) {
        this.dateSubmission = dateSubmission;
    }

    public String getSignaturePath() {
        return signaturePath;
    }

    public void setSignaturePath(String signaturePath) {
        this.signaturePath = signaturePath;
    }

    public LocalDateTime getSignatureUploadedAt() {
        return signatureUploadedAt;
    }

    public void setSignatureUploadedAt(LocalDateTime signatureUploadedAt) {
        this.signatureUploadedAt = signatureUploadedAt;
    }

    public static String SQLTable() {
        return """
                CREATE TABLE IF NOT EXISTS kyc (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    user_id INT NOT NULL UNIQUE,
                    cin VARCHAR(20) NOT NULL UNIQUE,
                    adresse VARCHAR(255) NOT NULL,
                    date_naissance DATE NOT NULL,
                    signature_path VARCHAR(255) NULL,
                    signature_uploaded_at DATETIME NULL,
                    statut ENUM('EN_ATTENTE','APPROUVE','REFUSE') NOT NULL DEFAULT 'EN_ATTENTE',
                    commentaire_admin TEXT NULL,
                    date_submission DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                );
                """;
    }
}
