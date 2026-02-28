package org.example.Service.UserService;

public class SignupRequest {
    private final String nom;
    private final String prenom;
    private final String email;
    private final String numTel;
    private final String password;
    private final String confirmPassword;

    public SignupRequest(String nom, String prenom, String email, String numTel, String password, String confirmPassword) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.numTel = numTel;
        this.password = password;
        this.confirmPassword = confirmPassword;
    }

    public String getNom() {
        return nom;
    }

    public String getEmail() {
        return email;
    }

    public String getPrenom() {
        return prenom;
    }

    public String getNumTel() {
        return numTel;
    }

    public String getPassword() {
        return password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }
}