package org.example.Service.BudgetService;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.example.Model.Budget.Categorie;
import org.example.Model.Budget.Item;

/**
 * Service to generate budget reports in PDF format
 */
public class BudgetReportService {

    /**
     * Generate a budget report PDF for all categories with their items
     * @param categories List of categories with items
     * @param filePath Path where to save the PDF
     * @throws Exception if PDF generation fails
     */
    public static void generateBudgetReport(List<Categorie> categories, String filePath) throws Exception {
        Document document = new Document(PageSize.A4, 40, 40, 40, 40);
        PdfWriter.getInstance(document, new FileOutputStream(filePath));
        document.open();

        // Title
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 24, Font.BOLD, BaseColor.DARK_GRAY);
        Paragraph title = new Paragraph("Rapport Budget", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(10);
        document.add(title);

        // Date
        Font dateFont = new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC, BaseColor.GRAY);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Paragraph dateP = new Paragraph("Généré le: " + sdf.format(new Date()), dateFont);
        dateP.setAlignment(Element.ALIGN_CENTER);
        dateP.setSpacingAfter(20);
        document.add(dateP);

        // Summary Section
        addSummarySection(document, categories);

        // Separator
        addSeparator(document);

        // Categories and Items Section
        addCategoriesSection(document, categories);

        document.close();
    }

    private static void addSummarySection(Document document, List<Categorie> categories) throws DocumentException {
        Font sectionFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, BaseColor.WHITE);
        PdfPCell headerCell = new PdfPCell(new Phrase("RÉSUMÉ", sectionFont));
        headerCell.setBackgroundColor(new BaseColor(52, 73, 94));
        headerCell.setPadding(10);
        headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);

        PdfPTable summaryTable = new PdfPTable(1);
        summaryTable.setWidthPercentage(100);
        summaryTable.addCell(headerCell);

        // Calculate totals
        double totalBudget = 0;
        double totalDépensé = 0;
        double totalSeuil = 0;

        for (Categorie cat : categories) {
            totalBudget += cat.getBudgetPrevu();
            totalSeuil += cat.getSeuilAlerte();
            if (cat.getItems() != null) {
                for (Item item : cat.getItems()) {
                    totalDépensé += item.getMontant();
                }
            }
        }

        // Summary data
        PdfPTable dataTable = new PdfPTable(2);
        dataTable.setWidthPercentage(100);
        dataTable.setSpacingBefore(10);
        dataTable.setSpacingAfter(10);

        Font labelFont = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, BaseColor.DARK_GRAY);
        Font valueFont = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL, new BaseColor(41, 128, 185));

        addSummaryRow(dataTable, "Nombre de catégories:", String.valueOf(categories.size()), labelFont, valueFont);
        addSummaryRow(dataTable, "Budget Total Prévu:", String.format("%.2f DT", totalBudget), labelFont, valueFont);
        addSummaryRow(dataTable, "Montant Dépensé:", String.format("%.2f DT", totalDépensé), labelFont, valueFont);
        addSummaryRow(dataTable, "Seuil Total:", String.format("%.2f DT", totalSeuil), labelFont, valueFont);

        // Budget restant
        double remaining = totalBudget - totalDépensé;
        BaseColor remainingColor = remaining >= 0 ? new BaseColor(39, 174, 96) : new BaseColor(231, 76, 60);
        Font remainingFont = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, remainingColor);
        addSummaryRow(dataTable, "Budget Restant:", String.format("%.2f DT", remaining), labelFont, remainingFont);

        summaryTable.addCell(dataTable);
        document.add(summaryTable);
    }

    private static void addSummaryRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setPadding(8);
        labelCell.setBackgroundColor(new BaseColor(236, 240, 241));
        labelCell.setBorderColor(new BaseColor(189, 195, 199));

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setPadding(8);
        valueCell.setBackgroundColor(new BaseColor(236, 240, 241));
        valueCell.setBorderColor(new BaseColor(189, 195, 199));

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private static void addSeparator(Document document) throws DocumentException {
        Paragraph separator = new Paragraph(" ");
        separator.setSpacingBefore(15);
        separator.setSpacingAfter(15);
        document.add(separator);
    }

    private static void addCategoriesSection(Document document, List<Categorie> categories) throws DocumentException {
        Font categoryFont = new Font(Font.FontFamily.HELVETICA, 13, Font.BOLD, BaseColor.WHITE);
        Font itemHeaderFont = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, BaseColor.DARK_GRAY);
        Font itemFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.BLACK);

        for (Categorie category : categories) {
            // Category Header
            PdfPCell categoryHeaderCell = new PdfPCell(new Phrase(category.getNomCategorie(), categoryFont));
            categoryHeaderCell.setBackgroundColor(new BaseColor(52, 152, 219));
            categoryHeaderCell.setPadding(10);
            categoryHeaderCell.setHorizontalAlignment(Element.ALIGN_LEFT);

            PdfPTable categoryTable = new PdfPTable(1);
            categoryTable.setWidthPercentage(100);
            categoryTable.setSpacingAfter(10);
            categoryTable.addCell(categoryHeaderCell);

            // Category Info
            PdfPTable infoTable = new PdfPTable(4);
            infoTable.setWidthPercentage(100);
            infoTable.setSpacingBefore(5);
            infoTable.setSpacingAfter(10);

            // Headers
            addTableHeader(infoTable, "Budget Prévu", itemHeaderFont);
            addTableHeader(infoTable, "Seuil d'Alerte", itemHeaderFont);
            addTableHeader(infoTable, "Dépensé", itemHeaderFont);
            addTableHeader(infoTable, "Restant", itemHeaderFont);

            // Values
            double spent = 0;
            if (category.getItems() != null) {
                for (Item item : category.getItems()) {
                    spent += item.getMontant();
                }
            }
            double remaining = category.getBudgetPrevu() - spent;

            addTableCell(infoTable, String.format("%.2f DT", category.getBudgetPrevu()), itemFont);
            addTableCell(infoTable, String.format("%.2f DT", category.getSeuilAlerte()), itemFont);
            addTableCell(infoTable, String.format("%.2f DT", spent), itemFont);

            BaseColor remainingColor = remaining >= 0 ? new BaseColor(39, 174, 96) : new BaseColor(231, 76, 60);
            Font remainingFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, remainingColor);
            PdfPCell remainingCell = new PdfPCell(new Phrase(String.format("%.2f DT", remaining), remainingFont));
            remainingCell.setPadding(8);
            remainingCell.setBorderColor(new BaseColor(189, 195, 199));
            infoTable.addCell(remainingCell);

            categoryTable.addCell(infoTable);
            document.add(categoryTable);

            // Items Table
            if (category.getItems() != null && !category.getItems().isEmpty()) {
                addItemsTable(document, category.getItems(), itemHeaderFont, itemFont);
            } else {
                Font emptyFont = new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC, BaseColor.GRAY);
                Paragraph empty = new Paragraph("Aucun item pour cette catégorie", emptyFont);
                empty.setAlignment(Element.ALIGN_CENTER);
                empty.setSpacingBefore(5);
                empty.setSpacingAfter(10);
                document.add(empty);
            }

            addSeparator(document);
        }
    }

    private static void addItemsTable(Document document, List<Item> items, Font headerFont, Font cellFont) throws DocumentException {
        PdfPTable itemsTable = new PdfPTable(3);
        itemsTable.setWidthPercentage(100);
        itemsTable.setSpacingBefore(5);
        itemsTable.setSpacingAfter(10);

        // Table headers
        addTableHeader(itemsTable, "N°", headerFont);
        addTableHeader(itemsTable, "Nom de l'Item", headerFont);
        addTableHeader(itemsTable, "Montant", headerFont);

        // Items data
        int index = 1;
        for (Item item : items) {
            addTableCell(itemsTable, String.valueOf(index), cellFont);
            addTableCell(itemsTable, item.getLibelle(), cellFont);
            addTableCell(itemsTable, String.format("%.2f DT", item.getMontant()), cellFont);
            index++;
        }

        document.add(itemsTable);
    }

    private static void addTableHeader(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(new BaseColor(236, 240, 241));
        cell.setPadding(8);
        cell.setBorderColor(new BaseColor(189, 195, 199));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private static void addTableCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(8);
        cell.setBorderColor(new BaseColor(189, 195, 199));
        table.addCell(cell);
    }
}
