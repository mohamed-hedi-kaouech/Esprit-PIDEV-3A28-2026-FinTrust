package UserTests;

import org.example.Service.Security.BCryptPasswordHasher;
import org.example.Service.Security.PasswordHasher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PasswordHasherTest {

    @Test
    void shouldHashAndVerifyPassword() {
        PasswordHasher hasher = new BCryptPasswordHasher();
        String rawPassword = "Secret123";

        String hash = hasher.hash(rawPassword);

        assertNotEquals(rawPassword, hash);
        assertTrue(hasher.verify(rawPassword, hash));
        assertFalse(hasher.verify("Wrong123", hash));
    }
}
