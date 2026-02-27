package org.example.Model.Wallet.ClassWallet;

import java.time.LocalDateTime;

public class Cheque {
    private int id_cheque;
    private String numero_cheque;
    private double montant;
    private LocalDateTime date_emission;
    private LocalDateTime date_presentation;
    private String statut; // EMIS, RESERVE, PAYE, REJETE
    private int id_wallet;
    private String beneficiaire;
    private String motif_rejet;
    private String nomProprietaire;  // ← NOUVEAU

    // Constructeurs
    public Cheque() {
        this.date_emission = LocalDateTime.now();
        this.statut = "EMIS";
    }

    public Cheque(String numero_cheque, double montant, int id_wallet, String beneficiaire) {
        if (numero_cheque == null || numero_cheque.trim().isEmpty()) {
            this.numero_cheque = "CHQ" + System.currentTimeMillis();
        } else {
            this.numero_cheque = numero_cheque;
        }
        this.montant = montant;
        this.id_wallet = id_wallet;
        this.beneficiaire = beneficiaire;
        this.date_emission = LocalDateTime.now();
        this.statut = "EMIS";
    }

    // ✅ Getter/Setter pour nomProprietaire
    public String getNomProprietaire() {
        return nomProprietaire;
    }

    public void setNomProprietaire(String nomProprietaire) {
        this.nomProprietaire = nomProprietaire;
    }

    // Getters et Setters existants
    public int getId_cheque() { return id_cheque; }
    public void setId_cheque(int id_cheque) { this.id_cheque = id_cheque; }

    public String getNumero_cheque() { return numero_cheque; }

    public void setNumero_cheque(String numero_cheque) {
        if (numero_cheque == null || numero_cheque.trim().isEmpty()) {
            this.numero_cheque = "CHQ" + System.currentTimeMillis();
        } else {
            this.numero_cheque = numero_cheque;
        }
    }

    public double getMontant() { return montant; }
    public void setMontant(double montant) { this.montant = montant; }

    public LocalDateTime getDate_emission() { return date_emission; }
    public void setDate_emission(LocalDateTime date_emission) { this.date_emission = date_emission; }

    public LocalDateTime getDate_presentation() { return date_presentation; }
    public void setDate_presentation(LocalDateTime date_presentation) { this.date_presentation = date_presentation; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public int getId_wallet() { return id_wallet; }
    public void setId_wallet(int id_wallet) { this.id_wallet = id_wallet; }

    public String getBeneficiaire() { return beneficiaire; }
    public void setBeneficiaire(String beneficiaire) { this.beneficiaire = beneficiaire; }

    public String getMotif_rejet() { return motif_rejet; }
    public void setMotif_rejet(String motif_rejet) { this.motif_rejet = motif_rejet; }

    @Override
    public String toString() {
        return String.format("Chèque #%s - %.2f %s - %s",
                numero_cheque, montant, beneficiaire, statut);
    }
}