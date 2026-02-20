package org.example.Model.Wallet.ClassWallet; // Correction: tout en minuscules

import org.example.Model.Wallet.EnumWallet.WalletDevise; // Correction du chemin
import org.example.Model.Wallet.EnumWallet.WalletStatut;
import java.time.LocalDateTime;
import java.util.Objects;

public class Wallet {
    private int idWallet;           // camelCase pour Java
    private String nomProprietaire;  // camelCase
    private double solde;
    private WalletDevise devise;
    private WalletStatut statut;
    private LocalDateTime dateCreation; // camelCase

    // Constructeurs
    public Wallet() {
        this.dateCreation = LocalDateTime.now();
        this.statut = WalletStatut.DRAFT;
    }

    public Wallet(String nomProprietaire, double solde, WalletDevise devise) {
        setNomProprietaire(nomProprietaire); // Utilisation du setter avec validation
        this.solde = solde;
        setDevise(devise); // Utilisation du setter avec validation
        this.statut = WalletStatut.DRAFT;
        this.dateCreation = LocalDateTime.now();
    }

    // Getters et Setters
    public int getIdWallet() {
        return idWallet;
    }

    public void setIdWallet(int idWallet) {
        this.idWallet = idWallet;
    }

    // Pour compatibilité avec l'ancien code
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
        if (solde < 0) {
            throw new IllegalArgumentException("Le solde ne peut pas être négatif");
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

    @Override
    public String toString() {
        return String.format("Wallet{id=%d, propriétaire='%s', solde=%.2f %s, statut=%s}",
                idWallet, nomProprietaire, solde, devise, statut);
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
            nom_proprietaire VARCHAR(100) NOT NULL,
            solde DOUBLE NOT NULL,
            devise VARCHAR(10) NOT NULL,
            statut VARCHAR(20) NOT NULL,
            date_creation DATETIME NOT NULL
        )
    """;
    }
}
