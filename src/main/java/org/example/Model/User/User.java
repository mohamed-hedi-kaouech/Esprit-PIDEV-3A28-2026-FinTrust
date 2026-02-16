package org.example.Model.User;

import java.time.LocalDateTime;

public class User {

    private int id;
    private int currentKycId;
    private String nom;
    private String prenom;
    private String email;
    private String numTel;
    private String role;
    private String password;
    private String kycStatus;
    private LocalDateTime createdAt;

    // ================== Constructeurs ==================

    /**
     * Constructeur complet avec tous les attributs sauf l'id
     * Id sera généré par la base de données
     */
    public User(int currentKycId, String nom, String prenom, String email, String numTel,
                String role, String password, String kycStatus, LocalDateTime createdAt) {
        this.currentKycId = currentKycId;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.numTel = numTel;
        this.role = role;
        this.password = password;
        this.kycStatus = kycStatus;
        this.createdAt = createdAt;
    }

    /**
     * Constructeur vide (utile pour JavaFX et les frameworks)
     */
    public User(String text, String txtPrenomText, String txtEmailText, String txtNumTelText, String txtRoleText, String txtPasswordText, String enAttente, LocalDateTime now) {}

    // ================== Getters & Setters ==================

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCurrentKycId() {
        return currentKycId;
    }

    public void setCurrentKycId(int currentKycId) {
        this.currentKycId = currentKycId;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNumTel() {
        return numTel;
    }

    public void setNumTel(String numTel) {
        this.numTel = numTel;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getKycStatus() {
        return kycStatus;
    }

    public void setKycStatus(String kycStatus) {
        this.kycStatus = kycStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // ================== toString ==================

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", currentKycId=" + currentKycId +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", email='" + email + '\'' +
                ", numTel='" + numTel + '\'' +
                ", role='" + role + '\'' +
                ", password='" + password + '\'' +
                ", kycStatus='" + kycStatus + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
