package org.example.Model.Wallet.ClassWallet;

import org.example.Model.Wallet.EnumWallet.WalletDevise;
import org.example.Model.Wallet.EnumWallet.WalletStatut;

import java.time.LocalDateTime;

public class Wallet {

    // ===== Attributes =====
    private int idWallet;
    private String nomProprietaire;
    private Double solde;
    private WalletDevise devise;
    private WalletStatut statut;
    private LocalDateTime dateCreation;

    // ===== Constructors =====

    // Constructeur vide
    public Wallet() {
        this.dateCreation = LocalDateTime.now();
    }

    // Constructeur pour INSERT (sans id)
    public Wallet(String nomProprietaire, Double solde,
                  WalletDevise devise, WalletStatut statut) {
        this.nomProprietaire = nomProprietaire;
        this.solde = solde;
        this.devise = devise;
        this.statut = statut;
        this.dateCreation = LocalDateTime.now();
    }

    // Constructeur pour UPDATE / SELECT (avec id)
    public Wallet(int idWallet, String nomProprietaire, Double solde,
                  WalletDevise devise, WalletStatut statut,
                  LocalDateTime dateCreation) {
        this.idWallet = idWallet;
        this.nomProprietaire = nomProprietaire;
        this.solde = solde;
        this.devise = devise;
        this.statut = statut;
        this.dateCreation = dateCreation;
    }

    // ===== Getters & Setters =====

    public int getIdWallet() {
        return idWallet;
    }

    public void setIdWallet(int idWallet) {
        this.idWallet = idWallet;
    }

    public String getNomProprietaire() {
        return nomProprietaire;
    }

    public void setNomProprietaire(String nomProprietaire) {
        this.nomProprietaire = nomProprietaire;
    }

    public Double getSolde() {
        return solde;
    }

    public void setSolde(Double solde) {
        this.solde = solde;
    }

    public WalletDevise getDevise() {
        return devise;
    }

    public void setDevise(WalletDevise devise) {
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

    // ===== toString =====

    @Override
    public String toString() {
        return "Wallet{" +
                "idWallet=" + idWallet +
                ", nomProprietaire='" + nomProprietaire + '\'' +
                ", solde=" + solde +
                ", devise=" + devise +
                ", statut=" + statut +
                ", dateCreation=" + dateCreation +
                '}';
    }

    // ===== equals (basé sur id) =====

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Wallet wallet = (Wallet) o;

        return idWallet == wallet.idWallet;
    }


    public static String SQLTable(){
        return """
                CREATE TABLE wallet (
                    id_wallet INT PRIMARY KEY AUTO_INCREMENT,
                    nom_proprietaire VARCHAR(100) NOT NULL,
                    solde DOUBLE NOT NULL,
                    devise VARCHAR(10) NOT NULL,
                    statut VARCHAR(20) NOT NULL,
                    date_creation DATETIME NOT NULL
                );
                """;
    }
}
