package org.example.Service.WalletService;

import org.example.Model.Wallet.ClassWallet.Transaction;
import org.example.Model.Wallet.ClassWallet.Wallet;
import java.time.LocalDateTime;

public class FraisBancaireService {

    private WalletService walletService;
    private TransactionService transactionService;
    private static final int ID_COMPTE_BANQUE = 101;  // ← Ton ID du compte banque

    public FraisBancaireService() {
        this.walletService = new WalletService();
        this.transactionService = new TransactionService();
    }

    public boolean appliquerFrais(String typeOperation, double montantOperation, Wallet walletClient) {
        double frais = 0;
        String description = "";

        switch (typeOperation) {
            case "RETRAIT":
                frais = 1.0 + (montantOperation * 0.003);
                description = "Frais de retrait - " + walletClient.getNomProprietaire();
                break;

            case "TRANSFERT":
                frais = montantOperation * 0.01;
                if (frais < 1) frais = 1;
                if (frais > 20) frais = 20;
                description = "Frais de transfert - " + walletClient.getNomProprietaire();  // ← CORRIGÉ !
                break;

            case "CHEQUE_REJETE":
                frais = 20.0;
                description = "Frais de rejet de chèque - " + walletClient.getNomProprietaire();
                break;

            case "TENUE_COMPTE":
                frais = 5.0;
                description = "Frais de tenue de compte (mensuel) - " + walletClient.getNomProprietaire();
                break;

            case "DEPOT":
                return true;

            default:
                return true;
        }

        if (walletClient.getSoldeDisponible() < frais) {
            System.out.println("❌ Client " + walletClient.getNomProprietaire() +
                    " - Solde insuffisant pour frais de " + frais + " TND");
            return false;
        }

        try {
            // ✅ Afficher ce qui va être enregistré
            System.out.println("📝 Enregistrement frais: " + frais + " TND - Description: '" + description + "'");

            // Débiter le client
            Transaction fraisClient = new Transaction(frais, "RETRAIT", walletClient.getId_wallet());
            fraisClient.setDate_transaction(LocalDateTime.now());
            fraisClient.setDescription(description);  // ← IMPORTANT : Ajouter la description !
            transactionService.ajouterTransaction(fraisClient);
            System.out.println("   💸 Débité du client: -" + frais + " TND");

            // Créditer la banque
            Transaction creditBanque = new Transaction(frais, "DEPOT", ID_COMPTE_BANQUE);
            creditBanque.setDate_transaction(LocalDateTime.now());
            creditBanque.setDescription(description);  // ← IMPORTANT : Ajouter la description !
            transactionService.ajouterTransaction(creditBanque);
            System.out.println("   🏦 Crédité à la banque: +" + frais + " TND");

            System.out.println("✅ " + description + " - " + frais + " TND prélevés sur " +
                    walletClient.getNomProprietaire());
            return true;

        } catch (Exception e) {
            System.err.println("❌ Erreur prélèvement frais: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public void afficherRevenus() {
        Wallet compteBanque = walletService.getWalletById(ID_COMPTE_BANQUE);
        System.out.println("\n💰 **REVENUS FINTRUST** 💰");
        System.out.println("Solde du compte banque: " + compteBanque.getSolde() + " TND");

        java.util.List<Transaction> transactions = transactionService.getTransactionsByWallet(ID_COMPTE_BANQUE);

        double total = 0;
        System.out.println("Détail des revenus:");
        for (Transaction t : transactions) {
            System.out.println("   • " + t.getDate_transaction() + " | " + t.getDescription() + " | +" + t.getMontant() + " TND");
            total += t.getMontant();
        }
        System.out.println("TOTAL PERÇU: " + total + " TND\n");
    }

    public int getIdCompteBanque() {
        return ID_COMPTE_BANQUE;
    }
}