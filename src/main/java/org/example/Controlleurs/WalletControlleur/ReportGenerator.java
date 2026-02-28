package org.example.Controlleurs.WalletControlleur;

import org.example.Model.Wallet.ClassWallet.Wallet;
import org.example.Model.Wallet.ClassWallet.Transaction;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.FileNotFoundException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReportGenerator {

    public static void exportWalletReport(Wallet wallet, List<Transaction> transactions, String filePath)
            throws FileNotFoundException {

        PdfWriter writer = new PdfWriter(filePath);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Titre
        Paragraph title = new Paragraph("Rapport détaillé du wallet")
                .setTextAlignment(TextAlignment.CENTER)
                .setBold()
                .setFontSize(20);
        document.add(title);

        document.add(new Paragraph("\n"));

        // Informations du wallet
        document.add(new Paragraph("Informations générales").setBold().setFontSize(14));
        document.add(new Paragraph("Propriétaire : " + wallet.getNomProprietaire()));
        document.add(new Paragraph("Email : " + (wallet.getEmail() != null ? wallet.getEmail() : "Non renseigné")));
        document.add(new Paragraph("Téléphone : " + (wallet.getTelephone() != null ? wallet.getTelephone() : "Non renseigné")));
        document.add(new Paragraph("Solde actuel : " + String.format("%,.2f", wallet.getSolde()) + " " + wallet.getDevise()));
        document.add(new Paragraph("Date de création : " + wallet.getDateCreation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
        document.add(new Paragraph("Statut : " + wallet.getStatut()));

        document.add(new Paragraph("\n"));

        // Statistiques
        document.add(new Paragraph("Statistiques").setBold().setFontSize(14));

        double totalDepots = transactions.stream()
                .filter(t -> "DEPOT".equals(t.getType()))
                .mapToDouble(Transaction::getMontant)
                .sum();

        double totalRetraits = transactions.stream()
                .filter(t -> "RETRAIT".equals(t.getType()))
                .mapToDouble(Transaction::getMontant)
                .sum();

        double totalTransferts = transactions.stream()
                .filter(t -> "TRANSFERT".equals(t.getType()))
                .mapToDouble(Transaction::getMontant)
                .sum();

        document.add(new Paragraph("Total des dépôts : " + String.format("%,.2f", totalDepots) + " €"));
        document.add(new Paragraph("Total des retraits : " + String.format("%,.2f", Math.abs(totalRetraits)) + " €"));
        document.add(new Paragraph("Total des transferts : " + String.format("%,.2f", Math.abs(totalTransferts)) + " €"));
        document.add(new Paragraph("Nombre de transactions : " + transactions.size()));

        document.add(new Paragraph("\n"));

        // Tableau des transactions
        document.add(new Paragraph("Transactions").setBold().setFontSize(14));

        Table table = new Table(UnitValue.createPercentArray(new float[]{20, 15, 15, 30, 20}));
        table.setWidth(UnitValue.createPercentValue(100));

        // En-têtes
        table.addCell(new Cell().add(new Paragraph("Date").setBold()));
        table.addCell(new Cell().add(new Paragraph("Type").setBold()));
        table.addCell(new Cell().add(new Paragraph("Montant").setBold()));
        table.addCell(new Cell().add(new Paragraph("Description").setBold()));
        table.addCell(new Cell().add(new Paragraph("Statut").setBold()));

        // Données
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        for (Transaction t : transactions) {
            table.addCell(new Cell().add(new Paragraph(t.getDate_transaction().format(formatter))));
            table.addCell(new Cell().add(new Paragraph(t.getType())));
            table.addCell(new Cell().add(new Paragraph(String.format("%,.2f", t.getMontant()) + " €")));
            table.addCell(new Cell().add(new Paragraph(t.getDescription() != null ? t.getDescription() : "-")));

            // ✅ CORRIGÉ: Pas de getStatut(), on détermine le statut selon le type
            String statut = determinerStatut(t);
            table.addCell(new Cell().add(new Paragraph(statut)));
        }

        document.add(table);
        document.add(new Paragraph("\n"));

        // Pied de page
        document.add(new Paragraph("Rapport généré le " +
                java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                .setTextAlignment(TextAlignment.RIGHT)
                .setItalic()
                .setFontSize(10));

        document.close();
    }

    // ✅ Nouvelle méthode pour déterminer le statut d'une transaction
    private static String determinerStatut(Transaction t) {
        // Par défaut, toutes les transactions sont considérées comme complétées
        // car votre table transaction n'a pas de colonne statut

        // Vous pouvez personnaliser selon la logique métier
        if (t.getMontant() < 0) {
            return "Complété";
        }

        return "Complété";
    }
}