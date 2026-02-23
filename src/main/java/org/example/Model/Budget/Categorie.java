package org.example.Model.Budget;

import java.util.List;

public class Categorie {

    // Attributs
    private int idCategorie;
    private String nomCategorie;
    private double budgetPrevu;
    private double seuilAlerte;

    // Relation inverse
    private List<Item> items;

    // Constructeurs
    public Categorie() {}

    public Categorie(String nomCategorie, double budgetPrevu, double seuilAlerte) {
        this.nomCategorie = nomCategorie;
        this.budgetPrevu = budgetPrevu;
        this.seuilAlerte = seuilAlerte;
    }

    public Categorie(int idCategorie, String nomCategorie, double budgetPrevu, double seuilAlerte) {
        this.idCategorie = idCategorie;
        this.nomCategorie = nomCategorie;
        this.budgetPrevu = budgetPrevu;
        this.seuilAlerte = seuilAlerte;
    }

    // Getters & Setters
    public int getIdCategorie() {
        return idCategorie;
    }

    public void setIdCategorie(int idCategorie) {
        this.idCategorie = idCategorie;
    }

    public String getNomCategorie() {
        return nomCategorie;
    }

    public void setNomCategorie(String nomCategorie) {
        this.nomCategorie = nomCategorie;
    }

    public double getBudgetPrevu() {
        return budgetPrevu;
    }

    public void setBudgetPrevu(double budgetPrevu) {
        this.budgetPrevu = budgetPrevu;
    }

    public double getSeuilAlerte() {
        return seuilAlerte;
    }

    public void setSeuilAlerte(double seuilAlerte) {
        this.seuilAlerte = seuilAlerte;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    // affichage dans ComboBox
    @Override
    public String toString() {
        return nomCategorie;
    }

    // SQL Table
    public static String SQLTable() {
        return """
                CREATE TABLE IF NOT EXISTS categorie (
                      idCategorie INT(11) NOT NULL AUTO_INCREMENT,
                      nomCategorie VARCHAR(255) NOT NULL,
                      budgetPrevu DOUBLE NOT NULL,
                      seuilAlerte DOUBLE NOT NULL,
                      PRIMARY KEY (idCategorie)
                ) ENGINE=InnoDB
                DEFAULT CHARSET=utf8mb4
                COLLATE=utf8mb4_general_ci;
                """;
    }
}