package org.example.pii.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PiiDetectionService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "\\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern IBAN_PATTERN = Pattern.compile(
            "\\b[A-Z]{2}\\d{2}[A-Z0-9]{11,30}\\b",
            Pattern.CASE_INSENSITIVE
    );

    // TN RIB often appears as long numeric block (commonly 20 digits)
    private static final Pattern RIB_PATTERN = Pattern.compile("\\b\\d{20}\\b");

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "(\\+\\d{1,3}[\\s.-]?)?(\\(?\\d{2,4}\\)?[\\s.-]?)?\\d{6,10}"
    );

    // Candidate card number: 13..19 digits with optional spaces/hyphens
    private static final Pattern CARD_CANDIDATE_PATTERN = Pattern.compile(
            "\\b(?:\\d[ -]*?){13,19}\\b"
    );

    // Passport-like token (generic): 1-2 letters + 6-9 digits
    private static final Pattern PASSPORT_PATTERN = Pattern.compile(
            "\\b[A-Z]{1,2}\\d{6,9}\\b",
            Pattern.CASE_INSENSITIVE
    );

    // CIN-like token in Maghreb contexts: 8 digits
    private static final Pattern CIN_PATTERN = Pattern.compile("\\b\\d{8}\\b");

    public List<String> detect(String text) {
        String safe = text == null ? "" : text;
        String lower = safe.toLowerCase(Locale.ROOT);
        Set<String> detected = new LinkedHashSet<>();

        if (EMAIL_PATTERN.matcher(safe).find()) {
            detected.add("EMAIL");
        }

        if (containsIban(safe)) {
            detected.add("IBAN");
        }

        if (containsRib(safe, lower)) {
            detected.add("RIB");
        }

        if (containsCardNumber(safe, lower)) {
            detected.add("CARD_NUMBER");
        }

        if (containsPhone(safe, lower)) {
            detected.add("PHONE");
        }

        if (containsCinOrPassport(safe, lower)) {
            detected.add("CIN_PASSPORT");
        }

        return new ArrayList<>(detected);
    }

    private boolean containsIban(String text) {
        Matcher matcher = IBAN_PATTERN.matcher(text);
        while (matcher.find()) {
            String iban = normalizeAlphaNum(matcher.group());
            if (isValidIban(iban)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsRib(String text, String lower) {
        if (RIB_PATTERN.matcher(text).find()) {
            return true;
        }
        return lower.contains("rib");
    }

    private boolean containsPhone(String text, String lower) {
        if (lower.contains("telephone") || lower.contains("tel ") || lower.contains("phone")) {
            return true;
        }

        Matcher matcher = PHONE_PATTERN.matcher(text);
        while (matcher.find()) {
            String digits = matcher.group().replaceAll("\\D", "");
            if (digits.length() >= 8 && digits.length() <= 15) {
                return true;
            }
        }
        return false;
    }

    private boolean containsCardNumber(String text, String lower) {
        if (lower.contains("carte bancaire") || lower.contains("card number") || lower.contains("numero carte")) {
            return true;
        }

        Matcher matcher = CARD_CANDIDATE_PATTERN.matcher(text);
        while (matcher.find()) {
            String digits = matcher.group().replaceAll("\\D", "");
            if (digits.length() >= 13 && digits.length() <= 19 && passesLuhn(digits)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsCinOrPassport(String text, String lower) {
        if (lower.contains("cin") || lower.contains("passeport") || lower.contains("passport")) {
            return true;
        }

        if (PASSPORT_PATTERN.matcher(text).find()) {
            return true;
        }

        return CIN_PATTERN.matcher(text).find();
    }

    private boolean passesLuhn(String digits) {
        int sum = 0;
        boolean alternate = false;

        for (int i = digits.length() - 1; i >= 0; i--) {
            int n = digits.charAt(i) - '0';
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n -= 9;
                }
            }
            sum += n;
            alternate = !alternate;
        }

        return sum % 10 == 0;
    }

    private boolean isValidIban(String iban) {
        if (iban.length() < 15 || iban.length() > 34) {
            return false;
        }

        String rearranged = iban.substring(4) + iban.substring(0, 4);
        StringBuilder numeric = new StringBuilder();
        for (char c : rearranged.toCharArray()) {
            if (Character.isDigit(c)) {
                numeric.append(c);
            } else if (Character.isLetter(c)) {
                numeric.append(Character.toUpperCase(c) - 'A' + 10);
            } else {
                return false;
            }
        }

        int mod = 0;
        for (int i = 0; i < numeric.length(); i++) {
            int digit = numeric.charAt(i) - '0';
            mod = (mod * 10 + digit) % 97;
        }

        return mod == 1;
    }

    private String normalizeAlphaNum(String s) {
        return s == null ? "" : s.replaceAll("[^A-Za-z0-9]", "");
    }
}
