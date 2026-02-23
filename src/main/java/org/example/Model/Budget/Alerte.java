package org.example.Model.Budget;

import java.sql.Timestamp;

public class Alerte {

    private int idAlerte;
    private int idCategorie;
    private String message;
    private double seuil;
    private boolean active;
    private Timestamp createdAt;

    public Alerte() {}

    public Alerte(int idCategorie, String message, double seuil) {
        this.idCategorie = idCategorie;
        this.message = message;
        this.seuil = seuil;
        this.active = true;
    }

    public int getIdAlerte() {
        return idAlerte;
    }

    public void setIdAlerte(int idAlerte) {
        this.idAlerte = idAlerte;
    }

    public int getIdCategorie() {
        return idCategorie;
    }

    public void setIdCategorie(int idCategorie) {
        this.idCategorie = idCategorie;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public double getSeuil() {
        return seuil;
    }

    public void setSeuil(double seuil) {
        this.seuil = seuil;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public static String SQLTable() {
        return """
                CREATE TABLE IF NOT EXISTS alerte (
                      idAlerte INT(11) NOT NULL AUTO_INCREMENT,
                      idCategorie INT(11) NOT NULL,
                      message VARCHAR(512) NOT NULL,
                      seuil DOUBLE NOT NULL,
                      active TINYINT(1) DEFAULT 1,
                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                      PRIMARY KEY (idAlerte),
                      KEY idCategorie (idCategorie),
                      CONSTRAINT fk_alerte_categorie FOREIGN KEY (idCategorie) REFERENCES categorie(idCategorie) ON DELETE CASCADE
                ) ENGINE=InnoDB
                DEFAULT CHARSET=utf8mb4
                COLLATE=utf8mb4_general_ci;
                """;
    }
}
