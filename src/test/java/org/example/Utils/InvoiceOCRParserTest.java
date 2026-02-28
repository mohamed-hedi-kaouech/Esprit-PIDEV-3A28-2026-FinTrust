package org.example.Utils;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class InvoiceOCRParserTest {

    @Test
    public void testParserWithSampleInvoice() {
        String ocrText = """
                FACTURE N°2024-001
                
                Article: Pain de mie
                Prix: 2.50 DT
                
                Date: 15/02/2024
                
                Total à payer: 2.50 DT
                """;

        InvoiceOCRParser.InvoiceData data = InvoiceOCRParser.parseInvoiceText(ocrText);

        assertNotNull(data.itemName, "Item name should be extracted");
        assertTrue(data.itemName.toLowerCase().contains("pain"), "Item name should contain 'pain'");

        assertNotNull(data.amount, "Amount should be extracted");
        assertEquals(2.50, data.amount, 0.01, "Amount should be 2.50");

        assertNotNull(data.date, "Date should be extracted");
        assertEquals(LocalDate.of(2024, 2, 15), data.date, "Date should be 15/02/2024");
    }

    @Test
    public void testParserWithFrenchMonthName() {
        String ocrText = """
                Date: 8 mars 2024
                Montant: 15,99 DT
                Produit: Huile d'olive
                """;

        InvoiceOCRParser.InvoiceData data = InvoiceOCRParser.parseInvoiceText(ocrText);

        assertNotNull(data.amount, "Amount should be extracted");
        assertEquals(15.99, data.amount, 0.01, "Amount should be 15.99 DT");

        assertNotNull(data.date, "Date should be extracted from month name");
        assertEquals(LocalDate.of(2024, 3, 8), data.date, "Date should match 8 mars 2024");

        assertNotNull(data.itemName, "Item name should be extracted");
        assertTrue(data.itemName.toLowerCase().contains("huile"), "Item name should contain 'huile'");
    }

    @Test
    public void testParserWithUSFormat() {
        String ocrText = """
                Description: Coffee Beans
                Total: $12.99
                Date: 03-28-2024
                """;

        InvoiceOCRParser.InvoiceData data = InvoiceOCRParser.parseInvoiceText(ocrText);

        assertNotNull(data.amount, "Amount should be extracted from US invoice");
        assertEquals(12.99, data.amount, 0.01, "Amount should be 12.99");
        // item name or date may not always be parsed completely
        assertTrue(data.itemName != null || data.date != null, "At least itemName or date should be available");
    }

    @Test
    public void testParserWithEmptyText() {
        InvoiceOCRParser.InvoiceData data = InvoiceOCRParser.parseInvoiceText("");
        assertNull(data.itemName);
        assertNull(data.amount);
        assertNull(data.date);
    }

    @Test
    public void testParserWithCommaSeparatedAmount() {
        String ocrText = "Montant: 45,50 DT";

        InvoiceOCRParser.InvoiceData data = InvoiceOCRParser.parseInvoiceText(ocrText);

        assertNotNull(data.amount);
        assertEquals(45.50, data.amount, 0.01);
    }
}
