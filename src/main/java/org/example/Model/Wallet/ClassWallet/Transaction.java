package org.example.Model.Wallet.ClassWallet;

import java.time.LocalDateTime;
import java.util.Objects;

public class Transaction {
    private int id_transaction;      // snake_case pour correspondre à la DB
    private double montant;
    private String type;
    private String description;
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

    // Getters et Setters en snake_case
    public int getId_transaction() { return id_transaction; }
    public void setId_transaction(int id_transaction) { this.id_transaction = id_transaction; }

    public double getMontant() { return montant; }
    public void setMontant(double montant) { this.montant = montant; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getDate_transaction() { return date_transaction; }
    public void setDate_transaction(LocalDateTime date_transaction) { this.date_transaction = date_transaction; }

    public int getId_wallet() { return id_wallet; }
    public void setId_wallet(int id_wallet) { this.id_wallet = id_wallet; }

    @Override
    public String toString() {
        return "Transaction{" +
                "id_transaction=" + id_transaction +
                ", montant=" + montant +
                ", type='" + type + '\'' +
                ", description='" + description + '\'' +
                ", date_transaction=" + date_transaction +
                ", id_wallet=" + id_wallet +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return id_transaction == that.id_transaction;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id_transaction);
    }

    public static String getSQLCreateTable() {
        return """
                CREATE TABLE IF NOT EXISTS transaction (
                    id_transaction INT PRIMARY KEY AUTO_INCREMENT,
                    montant DOUBLE NOT NULL,
                    type VARCHAR(20) NOT NULL,
                    description TEXT,
                    date_transaction DATETIME NOT NULL,
                    id_wallet INT NOT NULL,
                    FOREIGN KEY (id_wallet) REFERENCES wallet(id_wallet) ON DELETE CASCADE
                );
                """;
    }
}