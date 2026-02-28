package org.example.Utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class to parse OCR text from invoices and extract key information:
 * - Item name
 * - Amount
 * - Date
 */
public class InvoiceOCRParser {

    public static class InvoiceData {
        public String itemName;
        public Double amount;
        public LocalDate date;

        public InvoiceData(String itemName, Double amount, LocalDate date) {
            this.itemName = itemName;
            this.amount = amount;
            this.date = date;
        }

        @Override
        public String toString() {
            return "InvoiceData{" +
                    "itemName='" + itemName + '\'' +
                    ", amount=" + amount +
                    ", date=" + date +
                    '}';
        }
    }

    /**
     * Parse OCR text from invoice and extract item name, amount, and date
     */
    public static InvoiceData parseInvoiceText(String ocrText) {
        if (ocrText == null || ocrText.trim().isEmpty()) {
            return new InvoiceData(null, null, null);
        }

        String itemName = extractItemName(ocrText);
        Double amount = extractAmount(ocrText);
        LocalDate date = extractDate(ocrText);

        return new InvoiceData(itemName, amount, date);
    }

    /**
     * Extract item name from OCR text
     * Looks for common invoice patterns like "Item:", "Article:", "Produit:", "Désignation:", etc.
     * Also handles table formats from generated invoices
     */
    private static String extractItemName(String text) {
        text = text.toLowerCase();

        // Pattern 1: Table format with pipes/columns - "Item" header followed by the actual item name
        // Handles formats like "Item | Quantité" followed by "ItemName | 1"
        Pattern tablePattern = Pattern.compile(
                "item\\s*[|\\s]{1,5}.*?[\\n\\r]+\\s*([\\w\\s\\-&().,éèêôûàâäön]+?)\\s*[|\\s]{1,5}",
                Pattern.CASE_INSENSITIVE
        );
        Matcher tableMatcher = tablePattern.matcher(text);
        if (tableMatcher.find()) {
            String name = tableMatcher.group(1).trim();
            name = name.replaceAll("\\s+", " ");
            if (!name.isEmpty() && name.length() > 2 && !name.contains("total") && !name.contains("montant")) {
                return name;
            }
        }

        // Pattern 2: After keywords like "item", "article", "produit", "désignation", "description"
        Pattern[] patterns = {
                Pattern.compile("(?:item|article|produit|désignation|description|nom)[:\\s]+([\\w\\s\\-&().,éèêôûàâäön]+?)(?:[\\n\\r,]|total|montant|prix|quantité|qté)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("^\\s*([\\w\\s\\-&().,éèêôûàâäön]+?)\\s+(?:\\d+[-\\s]?\\d*[.,]\\d+|\\d+\\s*(?:DT|€|\\$|TND))[\\n\\r]", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE),
        };

        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                String name = matcher.group(1).trim();
                // Clean up common artifacts
                name = name.replaceAll("\\s+", " ");
                if (!name.isEmpty() && name.length() > 2 && !name.contains("total") && !name.contains("montant")) {
                    return name;
                }
            }
        }

        // Fallback: take first non-empty line that looks like it could be an item
        String[] lines = text.split("[\\n\\r]+");
        for (String line : lines) {
            line = line.trim();
            // Fixed operator precedence: check if line is long and doesn't have many numbers, OR contains article/produit keywords
            if ((line.length() > 3 && !line.matches(".*\\d{2,}.*")) || line.contains("item") || line.contains("article") || line.contains("produit")) {
                return line;
            }
        }

        return null;
    }

    /**
     * Extract monetary amount from OCR text
     * Looks for patterns like "12.50", "12,50", "12.50 DT", "12,50€", etc.
     */
    private static Double extractAmount(String text) {
        Double result = null;

        // Priority 1: Find amounts close to currency keywords like "montant", "total", "prix"
        Pattern keywordPattern = Pattern.compile(
                "(?:montant|total|prix|amount|tarif)[:\\s]+(\\d+(?:[.,]\\d{1,3})?)",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL
        );
        Matcher keywordMatcher = keywordPattern.matcher(text);
        if (keywordMatcher.find()) {
            result = parseAmount(keywordMatcher.group(1));
            if (result != null) return result;
        }

        // Priority 2: Find amounts with explicit currency symbols (DT, €, $, TND)
        // This avoids picking up years from dates
        Pattern currencyPattern = Pattern.compile("(\\d+[.,]\\d{1,3})\\s*(?:DT|€|\\$|TND)", Pattern.CASE_INSENSITIVE);
        Matcher currencyMatcher = currencyPattern.matcher(text);
        Double maxCurrencyAmount = null;
        while (currencyMatcher.find()) {
            Double amount = parseAmount(currencyMatcher.group(1));
            if (amount != null && (maxCurrencyAmount == null || amount > maxCurrencyAmount)) {
                maxCurrencyAmount = amount;
            }
        }
        if (maxCurrencyAmount != null) {
            return maxCurrencyAmount;
        }

        // Priority 3: Find amounts with decimal separators (most likely to be monetary amounts)
        Pattern decimalPattern = Pattern.compile("\\b(\\d+[.,]\\d{1,3})\\b");
        Matcher decimalMatcher = decimalPattern.matcher(text);
        Double maxDecimalAmount = null;
        while (decimalMatcher.find()) {
            Double amount = parseAmount(decimalMatcher.group(1));
            if (amount != null && (maxDecimalAmount == null || amount > maxDecimalAmount)) {
                maxDecimalAmount = amount;
            }
        }
        if (maxDecimalAmount != null) {
            return maxDecimalAmount;
        }

        // Priority 4: Last resort - look for whole numbers between 2 and 999999
        // (avoids years like 2024 which are typically 4 digits but too high in context)
        Pattern wholePattern = Pattern.compile("\\b([0-9]{1,6})\\b");
        Matcher wholeMatcher = wholePattern.matcher(text);
        Double maxWholeAmount = null;
        while (wholeMatcher.find()) {
            String numStr = wholeMatcher.group(1);
            // Skip 4-digit numbers that look like years (1900-2100)
            if (numStr.length() == 4) {
                try {
                    int year = Integer.parseInt(numStr);
                    if (year >= 1900 && year <= 2100) {
                        continue; // Skip years
                    }
                } catch (Exception e) {
                    // Continue
                }
            }
            Double amount = parseAmount(numStr);
            if (amount != null && amount >= 1 && amount < 100000 && 
                (maxWholeAmount == null || amount > maxWholeAmount)) {
                maxWholeAmount = amount;
            }
        }

        return maxWholeAmount;
    }

    /**
     * Parse a string amount like "12.50" or "12,50" and return as double
     */
    private static Double parseAmount(String amountStr) {
        if (amountStr == null) return null;
        try {
            // Normalize: replace comma with dot for parsing
            String normalized = amountStr.replace(",", ".");
            double value = Double.parseDouble(normalized);
            // Validate it's a reasonable amount (between 0.01 and 1,000,000)
            if (value > 0.01 && value < 1000000) {
                return value;
            }
        } catch (NumberFormatException e) {
            // Ignore
        }
        return null;
    }

    /**
     * Extract date from OCR text
     * Looks for common date patterns: DD/MM/YYYY, DD-MM-YYYY, YYYY-MM-DD, etc.
     */
    private static LocalDate extractDate(String text) {
        // Common date patterns
        DateTimeFormatter[] formatters = {
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("dd-MM-yyyy"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                DateTimeFormatter.ofPattern("dd.MM.yyyy"),
                DateTimeFormatter.ofPattern("d/M/yyyy"),
                DateTimeFormatter.ofPattern("d-M-yyyy"),
                DateTimeFormatter.ofPattern("yyyy/MM/dd")
        };

        // Pattern to find potential date strings
        Pattern datePattern = Pattern.compile(
                "\\b(\\d{1,4}[/\\-.]\\d{1,2}[/\\-.]\\d{1,4})\\b"
        );

        Matcher matcher = datePattern.matcher(text);
        while (matcher.find()) {
            String dateStr = matcher.group(1);
            for (DateTimeFormatter formatter : formatters) {
                try {
                    return LocalDate.parse(dateStr, formatter);
                } catch (DateTimeParseException e) {
                    // Try next formatter
                }
            }
        }

        // Fallback: look for month names
        Pattern monthPattern = Pattern.compile(
                "\\b(\\d{1,2})\\s+(janvier|février|mars|avril|mai|juin|juillet|août|septembre|octobre|novembre|décembre|january|february|march|april|may|june|july|august|september|october|november|december)\\s+(\\d{4})\\b",
                Pattern.CASE_INSENSITIVE
        );

        Matcher monthMatcher = monthPattern.matcher(text);
        if (monthMatcher.find()) {
            try {
                int day = Integer.parseInt(monthMatcher.group(1));
                String monthStr = monthMatcher.group(2).toLowerCase();
                int year = Integer.parseInt(monthMatcher.group(3));
                int month = getMonthNumber(monthStr);
                if (month > 0 && day > 0 && day <= 31 && year > 1900 && year < 2100) {
                    return LocalDate.of(year, month, day);
                }
            } catch (Exception e) {
                // Ignore
            }
        }

        return null;
    }

    private static int getMonthNumber(String monthStr) {
        switch (monthStr) {
            case "janvier":
            case "january":
                return 1;
            case "février":
            case "february":
                return 2;
            case "mars":
            case "march":
                return 3;
            case "avril":
            case "april":
                return 4;
            case "mai":
            case "may":
                return 5;
            case "juin":
            case "june":
                return 6;
            case "juillet":
            case "july":
                return 7;
            case "août":
            case "august":
                return 8;
            case "septembre":
            case "september":
                return 9;
            case "octobre":
            case "october":
                return 10;
            case "novembre":
            case "november":
                return 11;
            case "décembre":
            case "december":
                return 12;
            default:
                return -1;
        }
    }
}
