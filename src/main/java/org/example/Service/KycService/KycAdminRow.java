package org.example.Service.KycService;

import org.example.Model.Kyc.KycStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class KycAdminRow {
    private final int kycId;
    private final int userId;
    private final String nomComplet;
    private final String email;
    private final String cin;
    private final LocalDate dateNaissance;
    private final KycStatus statut;
    private final String commentaireAdmin;
    private final LocalDateTime dateSubmission;
    private final int filesCount;

    public KycAdminRow(int kycId, int userId, String nomComplet, String email, String cin, LocalDate dateNaissance, KycStatus statut, String commentaireAdmin, LocalDateTime dateSubmission, int filesCount) {
        this.kycId = kycId;
        this.userId = userId;
        this.nomComplet = nomComplet;
        this.email = email;
        this.cin = cin;
        this.dateNaissance = dateNaissance;
        this.statut = statut;
        this.commentaireAdmin = commentaireAdmin;
        this.dateSubmission = dateSubmission;
        this.filesCount = filesCount;
    }

    public int getKycId() {
        return kycId;
    }

    public int getUserId() {
        return userId;
    }

    public String getNomComplet() {
        return nomComplet;
    }

    public String getEmail() {
        return email;
    }

    public String getCin() {
        return cin;
    }

    public LocalDate getDateNaissance() {
        return dateNaissance;
    }

    public KycStatus getStatut() {
        return statut;
    }

    public String getCommentaireAdmin() {
        return commentaireAdmin;
    }

    public LocalDateTime getDateSubmission() {
        return dateSubmission;
    }

    public int getFilesCount() {
        return filesCount;
    }
}
