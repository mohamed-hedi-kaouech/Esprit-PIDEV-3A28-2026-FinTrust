package org.example.Service.KycService;

import org.example.Model.Kyc.KycStatus;
import java.time.LocalDate;

public class KycStateResult {
    private final KycStatus status;
    private final String commentaireAdmin;
    private final int filesCount;
    private final String cin;
    private final String adresse;
    private final LocalDate dateNaissance;
    private final String signaturePath;

    public KycStateResult(KycStatus status, String commentaireAdmin, int filesCount, String cin, String adresse, LocalDate dateNaissance, String signaturePath) {
        this.status = status;
        this.commentaireAdmin = commentaireAdmin;
        this.filesCount = filesCount;
        this.cin = cin;
        this.adresse = adresse;
        this.dateNaissance = dateNaissance;
        this.signaturePath = signaturePath;
    }

    public KycStatus getStatus() {
        return status;
    }

    public String getCommentaireAdmin() {
        return commentaireAdmin;
    }

    public int getFilesCount() {
        return filesCount;
    }

    public String getCin() {
        return cin;
    }

    public String getAdresse() {
        return adresse;
    }

    public LocalDate getDateNaissance() {
        return dateNaissance;
    }

    public String getSignaturePath() {
        return signaturePath;
    }

    public boolean isApproved() {
        return status == KycStatus.APPROUVE;
    }
}
