package org.example.Model.Wallet.ClassWallet;

import java.time.LocalDate;

public class ClientRisk {
    private int id;
    private int userId;
    private int walletId;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private int score;
    private String niveau;
    private int nbChequesRefuses;
    private double solde;
    private LocalDate dateInscription;
    private int nbRetraitsEleves;
    private int nbJoursNegatifs;
    private String privilege;
    private double plafondRetrait;
    private boolean validationAuto;
    private boolean fraisReduits;

    // Constructeur complet
    public ClientRisk(int userId, String nom, String prenom, String email, String telephone,
                      int score, int nbChequesRefuses, double solde,
                      LocalDate dateInscription, int nbRetraitsEleves,
                      int nbJoursNegatifs, String privilege) {
        this.userId = userId;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.telephone = telephone;
        this.score = score;
        this.niveau = ScoreConfiance.getNiveauFromScore(score);
        this.nbChequesRefuses = nbChequesRefuses;
        this.solde = solde;
        this.dateInscription = dateInscription;
        this.nbRetraitsEleves = nbRetraitsEleves;
        this.nbJoursNegatifs = nbJoursNegatifs;
        this.privilege = privilege;

        // Définir les privilèges selon le score
        miseAJourPrivileges();
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getWalletId() { return walletId; }
    public void setWalletId(int walletId) { this.walletId = walletId; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public int getScore() { return score; }
    public void setScore(int score) {
        this.score = score;
        this.niveau = ScoreConfiance.getNiveauFromScore(score);
        miseAJourPrivileges();
    }

    public String getNiveau() { return niveau; }

    public int getNbChequesRefuses() { return nbChequesRefuses; }
    public void setNbChequesRefuses(int nbChequesRefuses) { this.nbChequesRefuses = nbChequesRefuses; }

    public double getSolde() { return solde; }
    public void setSolde(double solde) { this.solde = solde; }

    public LocalDate getDateInscription() { return dateInscription; }
    public void setDateInscription(LocalDate dateInscription) { this.dateInscription = dateInscription; }

    public int getNbRetraitsEleves() { return nbRetraitsEleves; }
    public void setNbRetraitsEleves(int nbRetraitsEleves) { this.nbRetraitsEleves = nbRetraitsEleves; }

    public int getNbJoursNegatifs() { return nbJoursNegatifs; }
    public void setNbJoursNegatifs(int nbJoursNegatifs) { this.nbJoursNegatifs = nbJoursNegatifs; }

    public String getPrivilege() { return privilege; }
    public void setPrivilege(String privilege) {
        this.privilege = privilege;
        miseAJourPrivileges();
    }

    public double getPlafondRetrait() { return plafondRetrait; }
    public boolean isValidationAuto() { return validationAuto; }
    public boolean isFraisReduits() { return fraisReduits; }

    public String getNomComplet() {
        return prenom + " " + nom;
    }

    // Mise à jour automatique des privilèges selon le score
    private void miseAJourPrivileges() {
        if (score >= 80) {
            // VIP
            this.plafondRetrait = 5000.0;
            this.validationAuto = true;
            this.fraisReduits = true;
        } else if (score >= 50) {
            // STANDARD
            this.plafondRetrait = 1000.0;
            this.validationAuto = false;
            this.fraisReduits = false;
        } else {
            // SURVEILLE
            this.plafondRetrait = 200.0;
            this.validationAuto = false;
            this.fraisReduits = false;
        }
    }

    // Vérifier si le client est à surveiller
    public boolean estASurveiller() {
        return (score < 50) ||
                (nbChequesRefuses > 2) ||
                (solde < 0 && nbJoursNegatifs > 5) ||
                (nbRetraitsEleves > 3);
    }

    // Obtenir les raisons de surveillance
    public String getRaisonsSurveillance() {
        StringBuilder raisons = new StringBuilder();
        if (score < 50) raisons.append("Score faible (").append(score).append("/100), ");
        if (nbChequesRefuses > 2) raisons.append(nbChequesRefuses).append(" chèques refusés, ");
        if (solde < 0 && nbJoursNegatifs > 5) raisons.append("Solde négatif fréquent, ");
        if (nbRetraitsEleves > 3) raisons.append("Retraits élevés fréquents, ");

        String result = raisons.toString();
        return result.isEmpty() ? "Aucune" : result.substring(0, result.length() - 2);
    }
}