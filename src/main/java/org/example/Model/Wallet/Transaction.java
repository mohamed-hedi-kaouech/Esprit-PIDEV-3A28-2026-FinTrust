package org.example.Model.Wallet;


import java.time.LocalDateTime;

public class Transaction {

    // ===== Attributes =====
    private int idTransaction;
    private int walletId; // clé étrangère
    private Double montant;
    private String type; // DEPOT / RETRAIT
    private String description;
    private LocalDateTime dateTransaction;

    // ===== Constructors =====

    // Constructeur vide
    public Transaction() {
        this.dateTransaction = LocalDateTime.now();
    }

    // Constructeur pour INSERT (sans id)
    public Transaction(int walletId, Double montant, String type, String description) {
        this.walletId = walletId;
        this.montant = montant;
        this.type = type;
        this.description = description;
        this.dateTransaction = LocalDateTime.now();
    }

    // Constructeur pour SELECT / UPDATE
    public Transaction(int idTransaction, int walletId, Double montant, String type, String description, LocalDateTime dateTransaction) {
        this.idTransaction = idTransaction;
        this.walletId = walletId;
        this.montant = montant;
        this.type = type;
        this.description = description;
        this.dateTransaction = dateTransaction;
    }

    // ===== Getters & Setters =====

    public int getIdTransaction() {
        return idTransaction;
    }

    public void setIdTransaction(int idTransaction) {
        this.idTransaction = idTransaction;
    }

    public int getWalletId() {
        return walletId;
    }

    public void setWalletId(int walletId) {
        this.walletId = walletId;
    }

    public Double getMontant() {
        return montant;
    }

    public void setMontant(Double montant) {
        this.montant = montant;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getDateTransaction() {
        return dateTransaction;
    }

    public void setDateTransaction(LocalDateTime dateTransaction) {
        this.dateTransaction = dateTransaction;
    }

    // ===== toString =====

    @Override
    public String toString() {
        return "Transaction{" +
                "idTransaction=" + idTransaction +
                ", walletId=" + walletId +
                ", montant=" + montant +
                ", type='" + type + '\'' +
                ", description='" + description + '\'' +
                ", dateTransaction=" + dateTransaction +
                '}';
    }

    // ===== equals basé sur id =====

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Transaction that = (Transaction) o;

        return idTransaction == that.idTransaction;
    }

    public static String SQLTable(){
        return """
                CREATE TABLE IF NOT EXISTS transaction (
                    id_transaction INT PRIMARY KEY AUTO_INCREMENT,
                    montant DECIMAL(15,2) NOT NULL,
                    type ENUM('DEBIT','CREDIT') NOT NULL,
                    date_transaction DATETIME NOT NULL,
                    id_wallet INT NOT NULL,
                    CONSTRAINT fk_wallet_transaction
                        FOREIGN KEY (id_wallet)
                        REFERENCES wallet(id_wallet)
                        ON DELETE CASCADE
                        ON UPDATE CASCADE
                ) ENGINE=InnoDB;
                """;
    }
}
