package org.example.Model.Publication;

import java.time.LocalDateTime;


public class Publication {private int idPublication;
    private String titre;
    private String contenu;
    private String categorie;
    private String statut;
    private boolean estVisible;
    private LocalDateTime datePublication;

    public Publication(){}
    // Constructeur (sans id car AUTO_INCREMENT)
    public Publication(String titre, String contenu, String categorie,
                       String statut, boolean estVisible, LocalDateTime datePublication) {
        this.titre = titre;
        this.contenu = contenu;
        this.categorie = categorie;
        this.statut = statut;
        this.estVisible = estVisible;
        this.datePublication = datePublication;
    }

    // Getters & Setters
    public int getIdPublication() {
        return idPublication;
    }

    public void setIdPublication(int idPublication) {
        this.idPublication = idPublication;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public boolean isEstVisible() {
        return estVisible;
    }

    public void setEstVisible(boolean estVisible) {
        this.estVisible = estVisible;
    }

    public LocalDateTime getDatePublication() {
        return datePublication;
    }

    public void setDatePublication(LocalDateTime datePublication) {
        this.datePublication = datePublication;
    }

    // toString
    @Override
    public String toString() {
        return "Publication{" +
                "idPublication=" + idPublication +
                ", titre='" + titre + '\'' +
                ", contenu='" + contenu + '\'' +
                ", categorie='" + categorie + '\'' +
                ", statut='" + statut + '\'' +
                ", estVisible=" + estVisible +
                ", datePublication=" + datePublication +
                '}';
    }

    public static String SQLTable() {
        return "CREATE TABLE IF NOT EXISTS publication (" +
                "id_publication INT AUTO_INCREMENT PRIMARY KEY, " +
                "titre VARCHAR(255) NOT NULL, " +
                "contenu TEXT, " +
                "categorie VARCHAR(100), " +
                "statut VARCHAR(50), " +
                "est_visible BOOLEAN, " +
                "date_publication DATETIME" +
                ");";
    }
}
