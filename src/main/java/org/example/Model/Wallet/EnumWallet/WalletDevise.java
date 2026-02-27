package org.example.Model.Wallet.EnumWallet;

public enum WalletDevise {
    TND("TND", "Dinar Tunisien"),
    USD("USD", "Dollar US"),
    EUR("EUR", "Euro");

    private final String code;
    private final String libelle;

    WalletDevise(String code, String libelle) {
        this.code = code;
        this.libelle = libelle;
    }

    public String getCode() { return code; }
    public String getLibelle() { return libelle; }

    @Override
    public String toString() { return code + " - " + libelle; }
}