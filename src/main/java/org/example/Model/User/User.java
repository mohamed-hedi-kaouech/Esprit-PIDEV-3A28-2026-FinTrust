package org.example.Model.User;

import java.time.LocalDateTime;

public class User {
    private int id;
    private String nom;
    private String prenom;
    private String email;
    private String numTel;
    private String passwordHash;
    private UserRole role;
    private UserStatus status;
    private LocalDateTime createdAt;

    public User() {
    }

    public User(String nom, String prenom, String email, String numTel, String passwordHash, UserRole role, UserStatus status, LocalDateTime createdAt) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.numTel = numTel;
        this.passwordHash = passwordHash;
        this.role = role;
        this.status = status;
        this.createdAt = createdAt;
    }

    public User(String nom, String email, String passwordHash, UserRole role, UserStatus status, LocalDateTime createdAt) {
        this(nom, "", email, "", passwordHash, role, status, createdAt);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getNumTel() {
        return numTel;
    }

    public void setNumTel(String numTel) {
        this.numTel = numTel;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public static String SQLTable() {
        return """
                CREATE TABLE IF NOT EXISTS users (
                  id INT PRIMARY KEY AUTO_INCREMENT,
                  nom VARCHAR(120) NOT NULL,
                  prenom VARCHAR(120) NOT NULL DEFAULT '',
                  email VARCHAR(190) NOT NULL UNIQUE,
                  numTel VARCHAR(20) DEFAULT NULL,
                  password VARCHAR(255) NOT NULL,
                  role ENUM('ADMIN','CLIENT') NOT NULL DEFAULT 'CLIENT',
                  status ENUM('EN_ATTENTE','ACCEPTE','REFUSE') NOT NULL DEFAULT 'EN_ATTENTE',
                  createdAt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                );
                """;
    }
}