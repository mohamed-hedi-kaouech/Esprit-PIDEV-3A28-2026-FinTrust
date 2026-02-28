package org.example.Service.BudgetService;

import org.example.Model.Budget.Item;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Service to generate well-formatted invoice text files for OCR items
 */
public class OCRInvoiceFormatterService {

    /**
     * Generate a formatted invoice text file for OCR items
     * @param item The item to create invoice for
     * @param ocrText The original OCR text extracted
     * @return Path to the generated formatted invoice
     * @throws Exception if file generation fails
     */
    public static Path generateFormattedOCRInvoice(Item item, String ocrText) throws Exception {
        String fileName = sanitizeFileName(item.getLibelle()) + "_facture_ocr.txt";
        Path output = Paths.get(System.getProperty("user.home"), "Documents", fileName);
        
        StringBuilder invoice = new StringBuilder();
        
        // Header
        invoice.append("═".repeat(80)).append("\n");
        invoice.append(" ".repeat(25)).append("FACTURE OCR").append("\n");
        invoice.append("═".repeat(80)).append("\n\n");
        
        // Invoice Info
        invoice.append("─── INFORMATIONS ───────────────────────────────────────────────────────────\n");
        invoice.append(String.format("N° Facture:        FAC-%d\n", System.currentTimeMillis() / 1000));
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        invoice.append(String.format("Date:              %s\n", sdf.format(new Date())));
        
        invoice.append(String.format("Catégorie:         %s\n", item.getCategorie().getNomCategorie()));
        invoice.append("\n");
        
        // Item Table Header
        invoice.append("─── ARTICLES ───────────────────────────────────────────────────────────────\n");
        invoice.append(String.format("%-40s %12s %12s %12s\n", "Item", "Quantité", "Prix Unit.", "Montant"));
        invoice.append("-".repeat(80)).append("\n");
        
        // Item Row
        invoice.append(String.format("%-40s %12s %12s %12s\n", 
            truncate(item.getLibelle(), 40),
            "1",
            String.format("%.2f DT", item.getMontant()),
            String.format("%.2f DT", item.getMontant())
        ));
        
        invoice.append("\n");
        
        // Totals Section
        invoice.append("─── TOTAL ──────────────────────────────────────────────────────────────────\n");
        invoice.append(String.format("%70s: %9.2f DT\n", "TOTAL À PAYER", item.getMontant()));
        
        invoice.append("\n");
        
        // Footer with OCR text
        invoice.append("═".repeat(80)).append("\n");
        invoice.append("TEXTE OCR EXTRAIT (Original)\n");
        invoice.append("═".repeat(80)).append("\n");
        invoice.append(ocrText == null ? "(Aucun texte extrait)" : ocrText);
        invoice.append("\n");
        
        invoice.append("═".repeat(80)).append("\n");
        
        // Write to file
        Files.writeString(output, invoice.toString());
        return output;
    }

    /**
     * Truncate string to max length
     */
    private static String truncate(String str, int maxLength) {
        return str.length() <= maxLength ? str : str.substring(0, maxLength - 3) + "...";
    }

    /**
     * Sanitize filename
     */
    private static String sanitizeFileName(String itemName) {
        return itemName.replaceAll("[^a-zA-Z0-9_-]", "_");
    }
}
