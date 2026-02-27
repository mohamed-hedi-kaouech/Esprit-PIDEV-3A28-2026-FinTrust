package org.example.Model.Budget;

public class Item {

    // Attributs
    private int idItem;
    private String libelle;
    private double montant;
    private Categorie categorie;// Relation objet
    private int idCategorie;

    // Constructeurs
    public Item() {}

    public Item(int idItem, String libelle, double montant, Categorie categorie) {
        this.idItem = idItem;
        this.libelle = libelle;
        this.montant = montant;
        this.categorie = categorie;
    }

    // Getters & Setters
    public int getIdItem() {
        return idItem;
    }

    public void setIdItem(int idItem) {
        this.idItem = idItem;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public double getMontant() {
        return montant;
    }

    public void setMontant(double montant) {
        this.montant = montant;
    }

    public Categorie getCategorie() {
        return categorie;
    }

    public void setCategorie(Categorie categorie) {
        this.categorie = categorie;
    }

    public int getIdCategorie() {
        return idCategorie;
    }

    public void setIdCategorie(int idCategorie) {
        this.idCategorie = idCategorie;
    }

    @Override
    public String toString() {
        return "Item{" +
                "idItem=" + idItem +
                ", libelle='" + libelle + '\'' +
                ", montant=" + montant +
                ", categorie=" + categorie +
                '}';
    }

    public static String SQLTable() {
        return """
            CREATE TABLE IF NOT EXISTS item (
                  idItem INT(11) NOT NULL AUTO_INCREMENT,
                  libelle VARCHAR(255) NOT NULL,
                  montant DOUBLE NOT NULL,
                  categorie VARCHAR(255) NULL,
                  idCategorie INT(11) NOT NULL,
                  PRIMARY KEY (idItem),
                  KEY idCategorie (idCategorie),
                  CONSTRAINT fk_item_categorie FOREIGN KEY (idCategorie) REFERENCES categorie(idCategorie) ON UPDATE CASCADE
                ) ENGINE=InnoDB
                DEFAULT CHARSET=utf8mb4
                COLLATE=utf8mb4_general_ci;
            """;
    }
}
