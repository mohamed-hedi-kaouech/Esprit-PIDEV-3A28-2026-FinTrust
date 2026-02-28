package org.example.Model.Wallet.ClassWallet;

import org.example.Model.Wallet.EnumWallet.WalletDevise;
import org.example.Model.Wallet.EnumWallet.WalletStatut;
import java.time.LocalDateTime;
import java.util.Objects;

public class Wallet {
    private int idWallet;
    private int idUser;  // ✅ NOUVEAU : ID de l'utilisateur propriétaire
    private String nomProprietaire;
    private String telephone;
    private String email;
    private String codeAcces;
    private boolean estActif;
    private double solde;
    private double plafondDecouvert;
    private WalletDevise devise;
    private WalletStatut statut;
    private LocalDateTime dateCreation;

    // Autres attributs existants
    private int tentativesEchouees;
    private LocalDateTime dateDerniereTentative;
    private boolean estBloque;

    // Constructeurs
    public Wallet() {
        this.dateCreation = LocalDateTime.now();
        this.statut = WalletStatut.DRAFT;
        this.estActif = false;
        this.plafondDecouvert = 0;
    }

    public Wallet(String nomProprietaire, double solde, WalletDevise devise) {
        setNomProprietaire(nomProprietaire);
        this.solde = solde;
        setDevise(devise);
        this.statut = WalletStatut.DRAFT;
        this.estActif = false;
        this.plafondDecouvert = 0;
        this.dateCreation = LocalDateTime.now();
    }

    public Wallet(String nomProprietaire, String telephone, String email,
                  double solde, WalletDevise devise) {
        this(nomProprietaire, solde, devise);
        this.telephone = telephone;
        this.email = email;
    }

    // ✅ GETTERS/SETTERS POUR idUser
    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    // ✅ Getters/Setters pour plafondDecouvert
    public double getPlafondDecouvert() {
        return plafondDecouvert;
    }

    public void setPlafondDecouvert(double plafondDecouvert) {
        if (plafondDecouvert < 0) {
            throw new IllegalArgumentException("Le plafond de découvert ne peut pas être négatif");
        }
        this.plafondDecouvert = plafondDecouvert;
    }

    // ✅ Méthodes utilitaires pour le découvert
    public boolean isDecouvertAutorise() {
        return plafondDecouvert > 0;
    }

    public double getSoldeDisponible() {
        return solde + plafondDecouvert;
    }

    public boolean isEnDecouvert() {
        return solde < 0;
    }

    public double getMontantDecouvert() {
        return Math.max(0, -solde);
    }

    public double getMargeDisponible() {
        return plafondDecouvert - getMontantDecouvert();
    }

    // Getters et Setters existants
    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCodeAcces() {
        return codeAcces;
    }

    public void setCodeAcces(String codeAcces) {
        this.codeAcces = codeAcces;
    }

    public boolean isEstActif() {
        return estActif;
    }

    public void setEstActif(boolean estActif) {
        this.estActif = estActif;
    }

    @Deprecated
    public String getTelephone_sql() {
        return telephone;
    }

    @Deprecated
    public void setTelephone_sql(String telephone) {
        this.telephone = telephone;
    }

    @Deprecated
    public String getEmail_sql() {
        return email;
    }

    @Deprecated
    public void setEmail_sql(String email) {
        this.email = email;
    }

    @Deprecated
    public String getCode_acces() {
        return codeAcces;
    }

    @Deprecated
    public void setCode_acces(String codeAcces) {
        this.codeAcces = codeAcces;
    }

    @Deprecated
    public boolean isEst_actif() {
        return estActif;
    }

    @Deprecated
    public void setEst_actif(boolean estActif) {
        this.estActif = estActif;
    }

    public int getIdWallet() {
        return idWallet;
    }

    public void setIdWallet(int idWallet) {
        this.idWallet = idWallet;
    }

    @Deprecated
    public int getId_wallet() {
        return idWallet;
    }

    @Deprecated
    public void setId_wallet(int id_wallet) {
        this.idWallet = id_wallet;
    }

    public String getNomProprietaire() {
        return nomProprietaire;
    }

    public void setNomProprietaire(String nomProprietaire) {
        if (nomProprietaire == null || nomProprietaire.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom du propriétaire est obligatoire");
        }
        this.nomProprietaire = nomProprietaire;
    }

    @Deprecated
    public String getNom_proprietaire() {
        return nomProprietaire;
    }

    @Deprecated
    public void setNom_proprietaire(String nom_proprietaire) {
        setNomProprietaire(nom_proprietaire);
    }

    public double getSolde() {
        return solde;
    }

    public void setSolde(double solde) {
        if (solde < -plafondDecouvert) {
            throw new IllegalArgumentException(
                    String.format("Découvert maximum dépassé. Plafond autorisé : %.2f", plafondDecouvert)
            );
        }
        this.solde = solde;
    }

    public WalletDevise getDevise() {
        return devise;
    }

    public void setDevise(WalletDevise devise) {
        if (devise == null) {
            throw new IllegalArgumentException("La devise est obligatoire");
        }
        this.devise = devise;
    }

    public WalletStatut getStatut() {
        return statut;
    }

    public void setStatut(WalletStatut statut) {
        this.statut = statut;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    @Deprecated
    public LocalDateTime getDate_creation() {
        return dateCreation;
    }

    @Deprecated
    public void setDate_creation(LocalDateTime date_creation) {
        this.dateCreation = date_creation;
    }

    public int getTentativesEchouees() { return tentativesEchouees; }
    public void setTentativesEchouees(int tentativesEchouees) { this.tentativesEchouees = tentativesEchouees; }

    public LocalDateTime getDateDerniereTentative() { return dateDerniereTentative; }
    public void setDateDerniereTentative(LocalDateTime dateDerniereTentative) { this.dateDerniereTentative = dateDerniereTentative; }

    public boolean isEstBloque() { return estBloque; }
    public void setEstBloque(boolean estBloque) { this.estBloque = estBloque; }

    // Méthodes pour les compteurs
    public int getNbChequesRefuses() {
        return 0;
    }

    public void setNbChequesRefuses(int nbChequesRefuses) {
        // À implémenter
    }

    public int getNbRetraitsEleves() {
        return 0;
    }

    public void setNbRetraitsEleves(int nbRetraitsEleves) {
        // À implémenter
    }

    public int getNbJoursNegatifs() {
        return isEnDecouvert() ? 1 : 0;
    }

    public void setNbJoursNegatifs(int nbJoursNegatifs) {
        // À implémenter
    }

    public String getPrivilege() {
        return "STANDARD";
    }

    public void setPrivilege(String privilege) {
        // À implémenter
    }

    @Override
    public String toString() {
        return String.format("Wallet{id=%d, userId=%d, propriétaire='%s', solde=%.2f, plafond=%.2f, découvert=%b}",
                idWallet, idUser, nomProprietaire, solde, plafondDecouvert, isEnDecouvert());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Wallet wallet = (Wallet) o;
        return idWallet == wallet.idWallet;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idWallet);
    }

    public static String getSQLCreateTable() {
        return """
        CREATE TABLE IF NOT EXISTS wallet (
            id_wallet INT PRIMARY KEY AUTO_INCREMENT,
            id_user INT,
            nom_proprietaire VARCHAR(100) NOT NULL,
            telephone VARCHAR(20),
            email VARCHAR(100),
            code_acces VARCHAR(10),
            est_actif BOOLEAN DEFAULT FALSE,
            solde DOUBLE NOT NULL,
            plafond_decouvert DOUBLE DEFAULT 0,
            devise VARCHAR(10) NOT NULL,
            statut VARCHAR(20) NOT NULL,
            date_creation DATETIME NOT NULL,
            FOREIGN KEY (id_user) REFERENCES users(id)
        )
        """;
    }
}