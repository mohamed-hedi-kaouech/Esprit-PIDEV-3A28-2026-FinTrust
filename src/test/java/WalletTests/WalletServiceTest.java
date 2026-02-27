package WalletTests;

import org.example.Model.Wallet.ClassWallet.Wallet;
import org.example.Model.Wallet.EnumWallet.WalletDevise;
import org.example.Model.Wallet.EnumWallet.WalletStatut;
import org.example.Service.WalletService.WalletService;
import org.example.Utils.MaConnexion;
import org.junit.jupiter.api.*;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class WalletServiceTest {

    private WalletService walletService;
    private Connection connection;

    @BeforeEach
    void setUp() {
        System.out.println("=== Préparation du test WalletService ===");

        walletService = new WalletService();
        connection = MaConnexion.getInstance().getCnx();

        // Nettoyer la base de données
        nettoyerBaseDeDonnees();
    }

    @AfterEach
    void tearDown() {
        System.out.println("=== Nettoyage après test ===");
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
    @DisplayName("Test ajouter un wallet")
    void testAjouterWallet() {
        System.out.println("Test: Ajout d'un wallet");

        Wallet wallet = new Wallet("Jean Dupont", 1000.0, WalletDevise.EUR);
        wallet.setStatut(WalletStatut.DRAFT);

        boolean resultat = walletService.ajouterWallet(wallet);

        assertTrue(resultat);
        assertTrue(wallet.getId_wallet() > 0);

        System.out.println("Wallet ajouté avec ID: " + wallet.getId_wallet());
        System.out.println("Test réussi ✓");
    }

    @Test
    @DisplayName("Test récupérer tous les wallets")
    void testGetAllWallets() {
        System.out.println("Test: Récupération de tous les wallets");

        walletService.ajouterWallet(new Wallet("User1", 100.0, WalletDevise.EUR));
        walletService.ajouterWallet(new Wallet("User2", 200.0, WalletDevise.USD));

        List<Wallet> wallets = walletService.getAllWallets();

        assertEquals(2, wallets.size());
        System.out.println("Nombre de wallets trouvés: " + wallets.size());
        System.out.println("Test réussi ✓");
    }

    @Test
    @DisplayName("Test getWalletById")
    void testGetWalletById() {
        System.out.println("Test: Récupération d'un wallet par ID");

        Wallet wallet = new Wallet("Marie Curie", 500.0, WalletDevise.EUR);
        wallet.setStatut(WalletStatut.ACTIVE);
        walletService.ajouterWallet(wallet);
        int id = wallet.getId_wallet();

        Wallet trouve = walletService.getWalletById(id);

        assertNotNull(trouve);
        assertEquals("Marie Curie", trouve.getNom_proprietaire());
        assertEquals(500.0, trouve.getSolde());
        assertEquals(WalletDevise.EUR, trouve.getDevise());

        System.out.println("Wallet trouvé: " + trouve.getNom_proprietaire());
        System.out.println("Test réussi ✓");
    }

    @Test
    @DisplayName("Test walletExiste")
    void testWalletExiste() {
        System.out.println("Test: Vérification si wallet existe");

        Wallet wallet = new Wallet("Test", 100.0, WalletDevise.EUR);
        walletService.ajouterWallet(wallet);
        int idExistant = wallet.getId_wallet();

        boolean existe = walletService.walletExiste(idExistant);
        boolean pasExiste = walletService.walletExiste(99999);

        assertTrue(existe);
        assertFalse(pasExiste);

        System.out.println("Test réussi ✓");
    }

    @Test
    @DisplayName("Test mettre à jour le solde")
    void testMettreAJourSolde() {
        System.out.println("Test: Mise à jour du solde");

        Wallet wallet = new Wallet("Test", 1000.0, WalletDevise.EUR);
        walletService.ajouterWallet(wallet);
        int id = wallet.getId_wallet();

        boolean resultat = walletService.mettreAJourSolde(id, 1500.0);

        assertTrue(resultat);

        Wallet misAJour = walletService.getWalletById(id);
        assertEquals(1500.0, misAJour.getSolde());

        System.out.println("Nouveau solde: " + misAJour.getSolde());
        System.out.println("Test réussi ✓");
    }
}