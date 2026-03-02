package org.example.Model.Publication;

import java.time.LocalDateTime;

/**
 * Entité Feedback
 * Représente la réaction d'un utilisateur
 * à une publication bancaire.
 */
public class Feedback {

    // ===============================
    // Attributs
    // ===============================

    private int idFeedback;
    private final int idPublication;
    private final int idUser; // correspond à user.id
    private final String commentaire;
    private final String typeReaction; // LIKE / DISLIKE / COMMENT
    private final LocalDateTime dateFeedback;

    // ===============================
    // Constructeur sans ID
    // ===============================

    public Feedback(int idPublication, int idUser,
                    String commentaire, String typeReaction) {

        this.idPublication = idPublication;
        this.idUser = idUser;
        this.commentaire = commentaire;
        this.typeReaction = typeReaction;
        this.dateFeedback = LocalDateTime.now();
    }

    // ===============================
    // Constructeur complet
    // ===============================

    public Feedback(int idFeedback, int idPublication, int idUser,
                    String commentaire, String typeReaction,
                    LocalDateTime dateFeedback) {

        this.idFeedback = idFeedback;
        this.idPublication = idPublication;
        this.idUser = idUser;
        this.commentaire = commentaire;
        this.typeReaction = typeReaction;
        this.dateFeedback = dateFeedback;
    }

    // ===============================
    // Getters
    // ===============================

    public int getIdFeedback() {
        return idFeedback;
    }

    public int getIdPublication() {
        return idPublication;
    }

    public int getIdUser() {
        return idUser;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public String getTypeReaction() {
        return typeReaction;
    }

    public LocalDateTime getDateFeedback() {
        return dateFeedback;
    }

    @Override
    public String toString() {
        return "Feedback{" +
                "idFeedback=" + idFeedback +
                ", idPublication=" + idPublication +
                ", idUser=" + idUser +
                ", commentaire='" + commentaire + '\'' +
                ", typeReaction='" + typeReaction + '\'' +
                ", dateFeedback=" + dateFeedback +
                '}';
    }}

 
