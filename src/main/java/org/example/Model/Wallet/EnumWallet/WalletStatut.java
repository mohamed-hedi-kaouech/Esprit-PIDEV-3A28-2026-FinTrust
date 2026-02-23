package org.example.Model.Wallet.EnumWallet;

public enum WalletStatut {
    DRAFT("DRAFT", "Brouillon"),
    ACTIVE("ACTIVE", "Actif"),
    SUSPENDED("SUSPENDED", "Suspendu"),
    CLOSED("CLOSED", "Fermé");

    private final String code;
    private final String libelle;

    WalletStatut(String code, String libelle) {
        this.code = code;
        this.libelle = libelle;
    }

    public String getCode() { return code; }
    public String getLibelle() { return libelle; }

    @Override
    public String toString() { return libelle; }
}