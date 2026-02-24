package org.example;

import java.time.LocalDateTime;
import java.util.List;

import org.example.Model.Publication.Publication;
import org.example.Service.PublicationService.PublicationService;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        PublicationService publicationService = new PublicationService();

        // ===============================
        // 1️⃣ Création de publications
        // ===============================
        Publication[] publications = {

                new Publication(
                        "Lancement de la plateforme",
                        "Nous sommes heureux d’annoncer le lancement officiel de la plateforme.",
                        "Actualité",
                        "PUBLIÉ",
                        true,
                        LocalDateTime.now()
                ),
                new Publication(
                        "Maintenance programmée",
                        "Une maintenance est prévue ce week-end.",
                        "Information",
                        "BROUILLON",
                        false,
                        LocalDateTime.now()
                )
        };

        for (Publication p : publications) {
            boolean success = publicationService.create(p);
            if (success) {
                System.out.println(" Publication ajoutée avec succès (ID = " + p.getIdPublication() + ")");
            } else {
                System.out.println(" Échec lors de l'ajout de la publication : " + p.getTitre());
            }
        }

        // ===============================
        // 2️⃣ Affichage de toutes les publications
        // ===============================
        System.out.println("\n Liste des publications :");
        List<Publication> allPublications = publicationService.findAll();
        for (Publication p : allPublications) {
            System.out.println(
                    "ID: " + p.getIdPublication()
                            + " | Titre: " + p.getTitre()
                            + " | Catégorie: " + p.getCategorie()
                            + " | Statut: " + p.getStatut()
                            + " | Visible: " + p.isEstVisible()
                            + " | Date: " + p.getDatePublication()
            );
        }

        // ===============================
        // 3️⃣ Recherche d'une publication par ID
        // ===============================
        System.out.println("\n Recherche de la publication ID = 1...");
        Publication publicationFound = publicationService.find(1);
        if (publicationFound != null) {
            System.out.println(" Publication trouvée : "
                    + publicationFound.getTitre()
                    + " (" + publicationFound.getStatut() + ")");
        } else {
            System.out.println(" Aucune publication trouvée avec cet ID.");
        }

        // ===============================
        // 4️⃣ Suppression d'une publication
        // ===============================
        Publication p = publications[0]; // par exemple

        System.out.println("\n🗑 Suppression de la publication ID = " + p.getIdPublication());

        boolean deleted = publicationService.delete(p.getIdPublication());

        System.out.println(deleted
                ? "✔ Publication supprimée avec succès."
                : "✖ Échec de la suppression."
        );


    }

    }
