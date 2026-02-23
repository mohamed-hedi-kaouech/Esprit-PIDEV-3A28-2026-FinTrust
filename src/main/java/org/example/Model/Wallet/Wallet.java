package org.example.Model.Wallet;

import java.time.LocalDateTime;

public class Wallet {

    // ===== Attributes =====
    private int idWallet;
    private String nomProprietaire;
    private Double solde;
    private String devise;
    private String statut;
    private LocalDateTime dateCreation;

    // ===== Constructors =====

    // Constructeur vide
    public Wallet() {
        this.dateCreation = LocalDateTime.now();
    }

    // Constructeur pour INSERT (sans id)
    public Wallet(String nomProprietaire, Double solde, String devise, String statut) {
        this.nomProprietaire = nomProprietaire;
        this.solde = solde;
        this.devise = devise;
        this.statut = statut;
        this.dateCreation = LocalDateTime.now();
    }

    // Constructeur pour UPDATE / SELECT (avec id)
    public Wallet(int idWallet, String nomProprietaire, Double solde, String devise, String statut, LocalDateTime dateCreation) {
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

    public String getDevise() {
        return devise;
    }

    public void setDevise(String devise) {
        this.devise = devise;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
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
                ", devise='" + devise + '\'' +
                ", statut='" + statut + '\'' +
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
                CREATE TABLE IF NOT EXISTS wallet (
                    id_wallet INT PRIMARY KEY AUTO_INCREMENT,
                    nom_proprietaire VARCHAR(100) NOT NULL,
                    solde DECIMAL(15,2) NOT NULL DEFAULT 0.00,
                    devise VARCHAR(10) NOT NULL,
                    statut VARCHAR(20) NOT NULL,
                    date_creation DATETIME NOT NULL
                ) ENGINE=InnoDB;
                """;
    }
}
