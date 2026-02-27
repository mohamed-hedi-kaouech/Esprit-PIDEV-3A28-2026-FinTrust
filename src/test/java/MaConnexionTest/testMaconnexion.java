package MaConnexionTest;

import org.example.Utils.MaConnexion;
import org.junit.jupiter.api.Test;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;

class testMaconnexion {

    @Test
    void testSingletonInstance() {
        MaConnexion instance1 = MaConnexion.getInstance();
        MaConnexion instance2 = MaConnexion.getInstance();

        // Vérifie que c'est la même instance
        assertSame(instance1, instance2);
    }

    @Test
    void testConnectionNotNull() {
        MaConnexion connexion = MaConnexion.getInstance();
        Connection cnx = connexion.getCnx();

        assertNotNull(cnx);
    }

    @Test
    void testLoadDatabaseDoesNotThrow() {
        MaConnexion connexion = MaConnexion.getInstance();
        assertDoesNotThrow(connexion::loadDatabase);
    }
}

