package org.example.Service.WalletService;

import org.example.Model.Wallet.ClassWallet.Wallet;
import java.time.LocalDateTime;

public class AgiosService {

    private static final double TAUX_AGIOS_ANNUEL = 0.15; // 15% par an

    /**
     * Calcule les agios sans créer de TransactionService
     */
    public double calculerAgios(Wallet wallet, long jours) {
        if (!wallet.isEnDecouvert()) {
            return 0;
        }

        double montantDecouvert = wallet.getMontantDecouvert();
        double tauxJournalier = TAUX_AGIOS_ANNUEL / 365;
        double agios = montantDecouvert * tauxJournalier * jours;

        // Arrondir à 3 décimales
        agios = Math.round(agios * 1000.0) / 1000.0;

        return agios;
    }

    /**
     * Calcule les agios sans créer de transaction
     * La transaction sera créée par TransactionService
     */
    public double calculerAgiosMensuels(Wallet wallet) {
        // Simuler 30 jours de découvert
        return calculerAgios(wallet, 30);
    }
}