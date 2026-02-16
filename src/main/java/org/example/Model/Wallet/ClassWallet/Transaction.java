package org.example.Model.Wallet;

import java.time.LocalDateTime;

public class Transaction {
    private int id_transaction;
    private double montant;
    private String type; // DEPOT, RETRAIT, TRANSFERT
    private LocalDateTime date_transaction;
    private int id_wallet;

    // Constructeurs
    public Transaction() {
        this.date_transaction = LocalDateTime.now();
    }

    public Transaction(double montant, String type, int id_wallet) {
        this.montant = montant;
        this.type = type;
        this.id_wallet = id_wallet;
        this.date_transaction = LocalDateTime.now();
    }

    // Getters et Setters
    public int getId_transaction() { return id_transaction; }
    public void setId_transaction(int id_transaction) { this.id_transaction = id_transaction; }

    public double getMontant() { return montant; }
    public void setMontant(double montant) {
        if (montant <= 0) throw new IllegalArgumentException("Le montant doit être > 0");
        this.montant = montant;
    }

    public String getType() { return type; }
    public void setType(String type) {
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Le type est obligatoire");
        }
        this.type = type;
    }

    public LocalDateTime getDate_transaction() { return date_transaction; }
    public void setDate_transaction(LocalDateTime date_transaction) { this.date_transaction = date_transaction; }

    public int getId_wallet() { return id_wallet; }
    public void setId_wallet(int id_wallet) {
        if (id_wallet <= 0) throw new IllegalArgumentException("L'ID du wallet est obligatoire");
        this.id_wallet = id_wallet;
    }
<<<<<<< HEAD
}
=======

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
                CREATE TABLE transaction (
                    id_transaction INT PRIMARY KEY AUTO_INCREMENT,
                    montant DOUBLE NOT NULL,
                    type VARCHAR(20) NOT NULL,
                    date_transaction DATETIME NOT NULL,
                    id_wallet INT NOT NULL,
                    FOREIGN KEY (id_wallet) REFERENCES wallet(id_wallet)
                        ON DELETE CASCADE
                );
                """;
    }
}
>>>>>>> 73e547e27955c8dd234e9be4bc09f7eef35e3643
