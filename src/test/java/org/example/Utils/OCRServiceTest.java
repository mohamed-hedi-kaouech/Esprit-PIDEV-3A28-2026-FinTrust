package org.example.Utils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class OCRServiceTest {
    @BeforeAll
    public static void initToolkit() {
        // ensure PDFBox / other libs loaded
    }

    @Test
    public void textFileShouldReturnContent() throws Exception {
        Path temp = Files.createTempFile("ocrtest", ".txt");
        Files.writeString(temp, "Hello World");
        String result = OCRService.extractText(temp.toFile());
        assertEquals("Hello World", result.trim());
        Files.deleteIfExists(temp);
    }

    @Test
    public void pdfFileShouldReturnContent() throws Exception {
        Path temp = Files.createTempFile("ocrpdf", ".pdf");
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA, 12);
                cs.newLineAtOffset(100, 700);
                cs.showText("PDF Test 123");
                cs.endText();
            }
            doc.save(temp.toFile());
        }
        String result = OCRService.extractText(temp.toFile());
        assertTrue(result.contains("PDF Test 123"));
        Files.deleteIfExists(temp);
    }
}
