package org.example.Model.User;

import java.time.LocalDateTime;

public class Kyc {

    private int id;
    private int userId;
    private String documentType;
    private String documentNumberHash; // hash du numéro sensible
    private String lastFourDigits;     // 4 derniers chiffres si carte bancaire
    private byte[] documentFront;      // fichier binaire
    private byte[] documentBack;       // fichier binaire
    private byte[] signature;          // signature photo ou dessinée
    private byte[] selfie;             // selfie pour vérification
    private String status;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private String comment;

    // Constructeur principal
    public Kyc(int userId, String documentType, String documentNumberHash, String lastFourDigits,
               byte[] documentFront, byte[] documentBack, byte[] signature, byte[] selfie,
               String status, LocalDateTime submittedAt) {
        this.userId = userId;
        this.documentType = documentType;
        this.documentNumberHash = documentNumberHash;
        this.lastFourDigits = lastFourDigits;
        this.documentFront = documentFront;
        this.documentBack = documentBack;
        this.signature = signature;
        this.selfie = selfie;
        this.status = status;
        this.submittedAt = submittedAt;
    }

    // Constructeur vide
    public Kyc() {}

    // ================= GETTERS =================
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getDocumentType() { return documentType; }
    public String getDocumentNumberHash() { return documentNumberHash; }
    public String getLastFourDigits() { return lastFourDigits; }
    public byte[] getDocumentFront() { return documentFront; }
    public byte[] getDocumentBack() { return documentBack; }
    public byte[] getSignature() { return signature; }
    public byte[] getSelfie() { return selfie; }
    public String getStatus() { return status; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public String getComment() { return comment; }

    // ================= SETTERS =================
    public void setId(int id) { this.id = id; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }
    public void setDocumentNumberHash(String documentNumberHash) { this.documentNumberHash = documentNumberHash; }
    public void setLastFourDigits(String lastFourDigits) { this.lastFourDigits = lastFourDigits; }
    public void setDocumentFront(byte[] documentFront) { this.documentFront = documentFront; }
    public void setDocumentBack(byte[] documentBack) { this.documentBack = documentBack; }
    public void setSignature(byte[] signature) { this.signature = signature; }
    public void setSelfie(byte[] selfie) { this.selfie = selfie; }
    public void setStatus(String status) { this.status = status; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
    public void setComment(String comment) { this.comment = comment; }

    // ================= SQL CREATION TABLE =================
    public static String SQLTable() {
        return """
            CREATE TABLE IF NOT EXISTS kyc (
                id INT AUTO_INCREMENT PRIMARY KEY,
                userId INT NOT NULL,
                documentType VARCHAR(50),
                documentNumberHash CHAR(64),
                lastFourDigits CHAR(4),
                documentFront MEDIUMBLOB,
                documentBack MEDIUMBLOB,
                signature MEDIUMBLOB,
                selfie MEDIUMBLOB,
                status VARCHAR(20) DEFAULT 'PENDING',
                submittedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                reviewedAt TIMESTAMP NULL,
                comment TEXT,
                FOREIGN KEY (userId) REFERENCES users(id) ON DELETE CASCADE
            )
            """;
    }

    @Override
    public String toString() {
        return "Kyc{" +
                "id=" + id +
                ", userId=" + userId +
                ", documentType='" + documentType + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
