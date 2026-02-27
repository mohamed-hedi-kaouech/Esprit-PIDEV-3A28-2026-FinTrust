package WalletTests;  // Important : correspond au nom du dossier

import org.example.Model.Wallet.ClassWallet.Transaction;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class TransactionTest {

    private Transaction transaction;

    @BeforeEach
    void setUp() {
        System.out.println("Initialisation du test...");
        transaction = new Transaction(100.0, "DEPOT", 1);
    }

    @Test
    void testConstructeurParDefaut() {
        Transaction t = new Transaction();
        assertNotNull(t.getDate_transaction());
        assertEquals(0.0, t.getMontant());
    }

    @Test
    void testConstructeurAvecParametres() {
        Transaction t = new Transaction(500.0, "RETRAIT", 2);
        assertEquals(500.0, t.getMontant());
        assertEquals("RETRAIT", t.getType());
        assertEquals(2, t.getId_wallet());
    }

    @Test
    void testGettersSetters() {
        transaction.setId_transaction(10);
        assertEquals(10, transaction.getId_transaction());

        transaction.setMontant(250.0);
        assertEquals(250.0, transaction.getMontant());

        transaction.setType("TRANSFERT");
        assertEquals("TRANSFERT", transaction.getType());
    }

    @AfterEach
    void tearDown() {
        System.out.println("Nettoyage après le test...");
    }
}