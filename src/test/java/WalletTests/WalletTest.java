package WalletTests;

import org.example.Model.Wallet.ClassWallet.Wallet;
import org.example.Model.Wallet.EnumWallet.WalletDevise;
import org.example.Model.Wallet.EnumWallet.WalletStatut;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class WalletTest {

    private Wallet wallet;

    @BeforeEach
    void setUp() {
        wallet = new Wallet("Jean Dupont", 1000.0, WalletDevise.EUR);
    }

    @Test
    void testConstructeur() {
        assertEquals("Jean Dupont", wallet.getNom_proprietaire());
        assertEquals(1000.0, wallet.getSolde());
        assertEquals(WalletDevise.EUR, wallet.getDevise());
        assertEquals(WalletStatut.DRAFT, wallet.getStatut());
        assertNotNull(wallet.getDate_creation());
    }

    @Test
    void testGettersSetters() {
        wallet.setId_wallet(5);
        assertEquals(5, wallet.getId_wallet());

        wallet.setNom_proprietaire("Marie Curie");
        assertEquals("Marie Curie", wallet.getNom_proprietaire());

        wallet.setSolde(2500.0);
        assertEquals(2500.0, wallet.getSolde());

        wallet.setDevise(WalletDevise.USD);
        assertEquals(WalletDevise.USD, wallet.getDevise());

        wallet.setStatut(WalletStatut.ACTIVE);
        assertEquals(WalletStatut.ACTIVE, wallet.getStatut());
    }
}