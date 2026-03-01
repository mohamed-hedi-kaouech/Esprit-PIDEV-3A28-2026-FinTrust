package org.example.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

/**
 * Simple OCR service that attempts to invoke Tesseract CLI if available.
 * Also handles PDF and plain text files using PDFBox or Files.readString.
 * Falls back to empty string if extraction fails.
 */
public class OCRService {

    /**
     * Extract text from a file. If the file is a PDF it uses PDFBox's text stripper.
     * If the file is a plain text file it reads it directly. Otherwise it assumes
     * an image and invokes Tesseract.
     */
    public static String extractText(File file) {
        if (file == null) return "";
        String name = file.getName().toLowerCase();
        try {
            if (name.endsWith(".pdf")) {
                try (PDDocument doc = PDDocument.load(file)) {
                    PDFTextStripper stripper = new PDFTextStripper();
                    return stripper.getText(doc).trim();
                }
            } else if (name.endsWith(".txt")) {
                return Files.readString(file.toPath(), StandardCharsets.UTF_8);
            } else {
                // fallback to image OCR
                return extractTextFromImage(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private static String extractTextFromImage(File imageFile) {
        try {
            ProcessBuilder pb = new ProcessBuilder("tesseract", imageFile.getAbsolutePath(), "stdout");
            pb.redirectErrorStream(true);
            Process p = pb.start();
            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append('\n');
                }
            }
            int exit = p.waitFor();
            if (exit == 0) return sb.toString().trim();
            return "";
        } catch (Exception e) {
            // Tesseract not available or error — return empty
            return "";
        }
    }
}
