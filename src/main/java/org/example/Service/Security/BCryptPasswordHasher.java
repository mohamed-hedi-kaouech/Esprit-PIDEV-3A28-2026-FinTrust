package org.example.Service.Security;

import org.mindrot.jbcrypt.BCrypt;

public class BCryptPasswordHasher implements PasswordHasher {
    @Override
    public String hash(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt(12));
    }

    @Override
    public boolean verify(String rawPassword, String hashedPassword) {
        if (rawPassword == null || hashedPassword == null || hashedPassword.isBlank()) {
            return false;
        }
        String hash = hashedPassword.trim();
        try {
            // BCrypt standard hash (new format used by the app).
            if (hash.startsWith("$2a$") || hash.startsWith("$2b$") || hash.startsWith("$2y$")) {
                return BCrypt.checkpw(rawPassword, hash);
            }
            // Legacy/plaintext fallback (old data compatibility only).
            return rawPassword.equals(hash);
        } catch (IllegalArgumentException e) {
            // Corrupted or legacy non-BCrypt value -> fallback equality check.
            return rawPassword.equals(hash);
        }
    }
}
