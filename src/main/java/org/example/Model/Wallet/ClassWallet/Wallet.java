package org.example.Model.Wallet.ClassWallet;

import org.example.Model.Wallet.EnumWallet.WalletDevise;
import org.example.Model.Wallet.EnumWallet.WalletStatut;

import java.time.LocalDateTime;

public class Wallet {
    private int id_wallet;
    private String nom_proprietaire;
    private double solde;
    private WalletDevise devise;
    private WalletStatut statut;
    private LocalDateTime date_creation;

    // Constructeurs
    public Wallet() {
        this.date_creation = LocalDateTime.now();
        this.statut = WalletStatut.DRAFT;
    }

    public Wallet(String nom_proprietaire, double solde, WalletDevise devise) {
        this.nom_proprietaire = nom_proprietaire;
        this.solde = solde;
        this.devise = devise;
        this.statut = WalletStatut.DRAFT;
        this.date_creation = LocalDateTime.now();
    }

    // Getters et Setters
    public int getId_wallet() { return id_wallet; }
    public void setId_wallet(int id_wallet) { this.id_wallet = id_wallet; }

    public String getNom_proprietaire() { return nom_proprietaire; }
    public void setNom_proprietaire(String nom_proprietaire) {
        if (nom_proprietaire == null || nom_proprietaire.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom du proprietaire est obligatoire");
        }
        this.nom_proprietaire = nom_proprietaire;
    }

    public double getSolde() { return solde; }
    public void setSolde(double solde) {
        if (solde < 0) throw new IllegalArgumentException("Le solde ne peut pas etre negatif");
        this.solde = solde;
    }

    public WalletDevise getDevise() { return devise; }
    public void setDevise(WalletDevise devise) {
        if (devise == null) throw new IllegalArgumentException("La devise est obligatoire");
        this.devise = devise;
    }

    public WalletStatut getStatut() { return statut; }
    public void setStatut(WalletStatut statut) { this.statut = statut; }

    public LocalDateTime getDate_creation() { return date_creation; }
    public void setDate_creation(LocalDateTime date_creation) { this.date_creation = date_creation; }

    @Override
    public String toString() {
        return nom_proprietaire + " - " + devise + " - " + solde;
    }

    public static String SQLTable() {
        return """
                CREATE TABLE IF NOT EXISTS wallet (
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
