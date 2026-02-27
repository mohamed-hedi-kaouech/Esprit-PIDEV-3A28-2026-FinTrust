package WalletTests;

import org.example.Model.Wallet.ClassWallet.Transaction;
import org.example.Model.Wallet.ClassWallet.Wallet;
import org.example.Model.Wallet.EnumWallet.WalletDevise;
import org.example.Model.Wallet.EnumWallet.WalletStatut;
import org.example.Service.WalletService.TransactionService;
import org.example.Service.WalletService.WalletService;
import org.example.Utils.MaConnexion;
import org.junit.jupiter.api.*;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class TransactionServiceTest {

    private TransactionService transactionService;
    private WalletService walletService;
    private Connection connection;
    private Wallet testWallet;

    @BeforeEach
    void setUp() {
        System.out.println("=== Préparation du test ===");

        // Initialiser les services
        transactionService = new TransactionService();
        walletService = new WalletService();
        connection = MaConnexion.getInstance().getCnx();

        // Nettoyer la base de données avant chaque test
        nettoyerBaseDeDonnees();

        // Créer un wallet de test
        testWallet = new Wallet("Test User", 1000.0, WalletDevise.EUR);
        testWallet.setStatut(WalletStatut.ACTIVE);
        walletService.ajouterWallet(testWallet);

        System.out.println("Wallet de test créé avec ID: " + testWallet.getId_wallet());
    }

    @AfterEach
    void tearDown() {
        System.out.println("=== Nettoyage après le test ===");
        nettoyerBaseDeDonnees();
    }

    private void nettoyerBaseDeDonnees() {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("DELETE FROM transaction");
            stmt.executeUpdate("DELETE FROM wallet");
            System.out.println("Base de données nettoyée");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("Test ajouter une transaction de dépôt")
    void testAjouterTransactionDepot() {
        System.out.println("Test: Dépôt d'argent");

        // ARRANGE - Préparer les données
        Transaction transaction = new Transaction(500.0, "DEPOT", testWallet.getId_wallet());
        double soldeAvant = walletService.getWalletById(testWallet.getId_wallet()).getSolde();
        System.out.println("Solde avant dépôt: " + soldeAvant);

        // ACT - Exécuter l'action
        boolean resultat = transactionService.ajouterTransaction(transaction);
        double soldeApres = walletService.getWalletById(testWallet.getId_wallet()).getSolde();

        // ASSERT - Vérifier les résultats
        assertTrue(resultat, "La transaction devrait réussir");
        assertEquals(soldeAvant + 500, soldeApres, "Le solde devrait augmenter de 500");
        assertTrue(transaction.getId_transaction() > 0, "Un ID devrait être généré");

        System.out.println("Solde après dépôt: " + soldeApres);
        System.out.println("Test réussi ✓");
    }

    @Test
    @DisplayName("Test ajouter une transaction de retrait valide")
    void testAjouterTransactionRetraitValide() {
        System.out.println("Test: Retrait d'argent (solde suffisant)");

        Transaction transaction = new Transaction(300.0, "RETRAIT", testWallet.getId_wallet());
        double soldeAvant = walletService.getWalletById(testWallet.getId_wallet()).getSolde();

        boolean resultat = transactionService.ajouterTransaction(transaction);
        double soldeApres = walletService.getWalletById(testWallet.getId_wallet()).getSolde();

        assertTrue(resultat);
        assertEquals(soldeAvant - 300, soldeApres);

        System.out.println("Test réussi ✓");
    }

    @Test
    @DisplayName("Test ajouter une transaction de retrait avec solde insuffisant")
    void testAjouterTransactionRetraitInsuffisant() {
        System.out.println("Test: Retrait d'argent (solde insuffisant)");

        Transaction transaction = new Transaction(2000.0, "RETRAIT", testWallet.getId_wallet());
        double soldeAvant = walletService.getWalletById(testWallet.getId_wallet()).getSolde();

        // Vérifier que l'exception est lancée
        Exception exception = assertThrows(RuntimeException.class, () -> {
            transactionService.ajouterTransaction(transaction);
        });

        double soldeApres = walletService.getWalletById(testWallet.getId_wallet()).getSolde();

        assertTrue(exception.getMessage().contains("Solde insuffisant"));
        assertEquals(soldeAvant, soldeApres, "Le solde ne devrait pas changer");

        System.out.println("Test réussi ✓");
    }

    @Test
    @DisplayName("Test récupérer les transactions d'un wallet")
    void testGetTransactionsByWallet() {
        System.out.println("Test: Récupération des transactions");

        // Ajouter 2 transactions
        Transaction t1 = new Transaction(100.0, "DEPOT", testWallet.getId_wallet());
        Transaction t2 = new Transaction(50.0, "RETRAIT", testWallet.getId_wallet());

        transactionService.ajouterTransaction(t1);
        transactionService.ajouterTransaction(t2);

        List<Transaction> transactions = transactionService.getTransactionsByWallet(testWallet.getId_wallet());

        assertEquals(2, transactions.size());
        System.out.println("Nombre de transactions trouvées: " + transactions.size());
        System.out.println("Test réussi ✓");
    }

    @Test
    @DisplayName("Test modifier une transaction")
    void testModifierTransaction() {
        System.out.println("Test: Modification d'une transaction");

        Transaction transaction = new Transaction(100.0, "DEPOT", testWallet.getId_wallet());
        transactionService.ajouterTransaction(transaction);

        transaction.setMontant(150.0);
        transaction.setType("RETRAIT");

        boolean resultat = transactionService.modifierTransaction(transaction);

        assertTrue(resultat);

        List<Transaction> transactions = transactionService.getTransactionsByWallet(testWallet.getId_wallet());
        assertEquals(150.0, transactions.get(0).getMontant());
        assertEquals("RETRAIT", transactions.get(0).getType());

        System.out.println("Test réussi ✓");
    }

    @Test
    @DisplayName("Test supprimer une transaction")
    void testSupprimerTransaction() {
        System.out.println("Test: Suppression d'une transaction");

        Transaction transaction = new Transaction(100.0, "DEPOT", testWallet.getId_wallet());
        transactionService.ajouterTransaction(transaction);
        int id = transaction.getId_transaction();

        boolean resultat = transactionService.supprimerTransaction(id);

        assertTrue(resultat);

        List<Transaction> transactions = transactionService.getTransactionsByWallet(testWallet.getId_wallet());
        assertEquals(0, transactions.size());

        System.out.println("Test réussi ✓");
    }
}