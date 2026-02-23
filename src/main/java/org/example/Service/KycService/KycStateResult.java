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

    public KycStateResult(KycStatus status, String commentaireAdmin, int filesCount, String cin, String adresse, LocalDate dateNaissance) {
        this.status = status;
        this.commentaireAdmin = commentaireAdmin;
        this.filesCount = filesCount;
        this.cin = cin;
        this.adresse = adresse;
        this.dateNaissance = dateNaissance;
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

    public boolean isApproved() {
        return status == KycStatus.APPROUVE;
    }
}
