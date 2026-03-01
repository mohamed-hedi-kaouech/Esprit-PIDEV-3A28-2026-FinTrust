package org.example.Service.BudgetService;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.example.Model.Budget.Item;

/**
 * Service to generate invoice PDFs for items
 */
public class InvoiceGeneratorService {

    /**
     * Generate an invoice PDF for a single item
     * @param item The item to create invoice for
     * @return Path to the generated PDF
     * @throws Exception if PDF generation fails
     */
    public static Path generateItemInvoice(Item item) throws Exception {
        String fileName = sanitizeFileName(item.getLibelle()) + "_facture.pdf";
        Path output = Paths.get(System.getProperty("user.home"), "Downloads", fileName);
        
        Document document = new Document(PageSize.A4, 40, 40, 40, 40);
        PdfWriter.getInstance(document, new FileOutputStream(output.toFile()));
        document.open();

        // Invoice Header
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 28, Font.BOLD, BaseColor.DARK_GRAY);
        Paragraph title = new Paragraph("FACTURE", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(5);
        document.add(title);

        // Separator line
        Paragraph separator = new Paragraph("_".repeat(80));
        separator.setAlignment(Element.ALIGN_CENTER);
        separator.setSpacingAfter(20);
        document.add(separator);

        // Invoice Info Section
        addInvoiceInfo(document, item);

        // Empty space
        document.add(new Paragraph("\n"));

        // Items Table
        addItemTable(document, item);

        // Empty space
        document.add(new Paragraph("\n"));

        // Totals Section
        addTotalsSection(document, item);

        // Footer
        addFooter(document);

        document.close();
        return output;
    }

    private static void addInvoiceInfo(Document document, Item item) throws DocumentException {
        Font labelFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.DARK_GRAY);
        Font valueFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.BLACK);

        // Create a table for invoice info
        com.itextpdf.text.pdf.PdfPTable infoTable = new com.itextpdf.text.pdf.PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.getDefaultCell().setBorder(com.itextpdf.text.Rectangle.NO_BORDER);

        // Invoice Number and Date
        addInfoRow(infoTable, "N° Facture:", generateInvoiceNumber(), labelFont, valueFont);
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String currentDate = sdf.format(new Date());
        addInfoRow(infoTable, "Date:", currentDate, labelFont, valueFont);

        addInfoRow(infoTable, "Catégorie:", item.getCategorie().getNomCategorie(), labelFont, valueFont);

        document.add(infoTable);
    }

    private static void addInfoRow(com.itextpdf.text.pdf.PdfPTable table, String label, String value, 
                                   Font labelFont, Font valueFont) {
        com.itextpdf.text.pdf.PdfPCell labelCell = new com.itextpdf.text.pdf.PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
        labelCell.setPaddingBottom(5);

        com.itextpdf.text.pdf.PdfPCell valueCell = new com.itextpdf.text.pdf.PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
        valueCell.setPaddingBottom(5);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private static void addItemTable(Document document, Item item) throws DocumentException {
        com.itextpdf.text.pdf.PdfPTable itemTable = new com.itextpdf.text.pdf.PdfPTable(4);
        itemTable.setWidthPercentage(100);
        itemTable.setSpacingBefore(10);
        itemTable.setSpacingAfter(10);

        Font headerFont = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, BaseColor.WHITE);
        Font cellFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.BLACK);

        // Headers
        addHeader(itemTable, "Item", headerFont);
        addHeader(itemTable, "Quantité", headerFont);
        addHeader(itemTable, "Prix Unitaire", headerFont);
        addHeader(itemTable, "Montant", headerFont);

        // Item row
        addCell(itemTable, item.getLibelle(), cellFont);
        addCell(itemTable, "1", cellFont);
        addCell(itemTable, String.format("%.2f DT", item.getMontant()), cellFont);
        addCell(itemTable, String.format("%.2f DT", item.getMontant()), cellFont);

        document.add(itemTable);
    }

    private static void addTotalsSection(Document document, Item item) throws DocumentException {
        Font labelFont = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, BaseColor.DARK_GRAY);
        Font valueFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, new BaseColor(41, 128, 185));

        com.itextpdf.text.pdf.PdfPTable totalsTable = new com.itextpdf.text.pdf.PdfPTable(2);
        totalsTable.setWidthPercentage(50);
        totalsTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalsTable.getDefaultCell().setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
        totalsTable.getDefaultCell().setPadding(8);

        com.itextpdf.text.pdf.PdfPCell totalLabelCell = new com.itextpdf.text.pdf.PdfPCell(new Phrase("TOTAL À PAYER:", labelFont));
        totalLabelCell.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
        totalLabelCell.setBackgroundColor(new BaseColor(236, 240, 241));
        totalLabelCell.setPadding(10);
        totalsTable.addCell(totalLabelCell);

        com.itextpdf.text.pdf.PdfPCell totalValueCell = new com.itextpdf.text.pdf.PdfPCell(
                new Phrase(String.format("%.2f DT", item.getMontant()), valueFont));
        totalValueCell.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
        totalValueCell.setBackgroundColor(new BaseColor(236, 240, 241));
        totalValueCell.setPadding(10);
        totalValueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalsTable.addCell(totalValueCell);

        document.add(totalsTable);
    }

    private static void addFooter(Document document) throws DocumentException {
        document.add(new Paragraph("\n"));
    }

    private static void addHeader(com.itextpdf.text.pdf.PdfPTable table, String text, Font font) {
        com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(new BaseColor(52, 152, 219));
        cell.setPadding(10);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private static void addCell(com.itextpdf.text.pdf.PdfPTable table, String text, Font font) {
        com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(new Phrase(text, font));
        cell.setPadding(8);
        cell.setBorderColor(new BaseColor(189, 195, 199));
        table.addCell(cell);
    }

    /**
     * Generate a unique invoice number based on current timestamp
     */
    private static String generateInvoiceNumber() {
        long timestamp = System.currentTimeMillis();
        return "FAC-" + (timestamp / 1000);
    }

    /**
     * Sanitize filename from item name
     */
    private static String sanitizeFileName(String itemName) {
        return itemName.replaceAll("[^a-zA-Z0-9_-]", "_");
    }
}
