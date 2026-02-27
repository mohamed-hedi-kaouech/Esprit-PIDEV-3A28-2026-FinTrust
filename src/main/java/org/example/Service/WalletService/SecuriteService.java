package org.example.Service.WalletService;

import org.example.Model.Wallet.ClassWallet.Wallet;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class SecuriteService {

    private static final int SEUIL_TRANSACTIONS_RAPIDES = 3; // 3 transactions
    private static final int DELAI_SECONDES = 60; // en 60 secondes
    private static final int DELAI_TRES_RAPIDE = 10; // 10 secondes (ultra rapide)
    private static final int DUREE_BLOCAGE_MINUTES = 15; // Blocage 15 minutes

    // Stocker l'historique des transactions par wallet
    private Map<Integer, List<LocalDateTime>> historiqueTransactions = new HashMap<>();

    private WalletService walletService;
    private EmailService emailService;
    private SmsService smsService;

    public SecuriteService() {
        this.walletService = new WalletService();
        this.emailService = new EmailService();
        this.smsService = new SmsService();
    }

    /**
     * Vérifie si le wallet peut effectuer une transaction
     */
    public boolean verifierSecurite(Wallet wallet) {
        // Vérifier si le compte est bloqué
        if (wallet.isEstBloque()) {
            // Vérifier si le blocage est expiré
            if (wallet.getDateDerniereTentative() != null) {
                long minutesDepuisBlocage = ChronoUnit.MINUTES.between(
                        wallet.getDateDerniereTentative(), LocalDateTime.now());

                if (minutesDepuisBlocage >= DUREE_BLOCAGE_MINUTES) {
                    // Débloquer automatiquement après 15 minutes
                    debloquerCompte(wallet);
                    return true;
                }
            }
            return false; // Compte toujours bloqué
        }

        return true; // Compte non bloqué
    }

    /**
     * Vérifie les transactions rapides avec détection intelligente
     */
    public boolean verifierTransactionsRapides(Wallet wallet) {
        LocalDateTime maintenant = LocalDateTime.now();
        int walletId = wallet.getIdWallet();

        // Récupérer l'historique des transactions pour ce wallet
        List<LocalDateTime> historique = historiqueTransactions.computeIfAbsent(
                walletId, k -> new ArrayList<>());

        // Ajouter la tentative actuelle
        historique.add(maintenant);

        // Nettoyer l'historique (garder seulement les 5 dernières minutes)
        historique.removeIf(date ->
                ChronoUnit.MINUTES.between(date, maintenant) > 5);

        System.out.println("\n🔍 ANALYSE DE SÉCURITÉ pour " + wallet.getNomProprietaire());
        System.out.println("📊 Transactions dans les 5 dernières minutes: " + historique.size());

        // Si c'est la première transaction
        if (historique.size() <= 1) {
            System.out.println("✅ Première transaction - OK");
            return true;
        }

        // Analyser les écarts entre transactions
        int compteurRapide = 0;
        LocalDateTime precedente = null;

        for (LocalDateTime date : historique) {
            if (precedente != null) {
                long secondesEcart = ChronoUnit.SECONDS.between(precedente, date);

                System.out.println("⏱️ Écart avec précédente: " + secondesEcart + " secondes");

                // Détection de transactions ultra-rapides (< 10 secondes)
                if (secondesEcart < DELAI_TRES_RAPIDE) {
                    compteurRapide += 2; // Compte double !
                    System.out.println("⚠️ Transactions ultra-rapides! Compteur +2");
                }
                // Détection de transactions rapides (< 60 secondes)
                else if (secondesEcart < DELAI_SECONDES) {
                    compteurRapide++;
                    System.out.println("⚠️ Transactions rapides! Compteur +1");
                }
            }
            precedente = date;
        }

        // Calculer la moyenne des écarts
        double moyenneEcart = calculerMoyenneEcart(historique);
        System.out.println("📈 Moyenne des écarts: " + String.format("%.1f", moyenneEcart) + " secondes");
        System.out.println("🎯 Score de risque: " + compteurRapide);

        // Seuil de blocage : si le compteur atteint 3 ou si la moyenne est < 30 secondes
        if (compteurRapide >= SEUIL_TRANSACTIONS_RAPIDES || moyenneEcart < 30) {
            bloquerCompte(wallet, "Transactions anormalement rapides détectées");
            return false;
        }

        // Mettre à jour les compteurs dans le wallet
        wallet.setTentativesEchouees(compteurRapide);
        wallet.setDateDerniereTentative(maintenant);
        walletService.mettreAJourWallet(wallet);

        return true;
    }

    /**
     * Calcule la moyenne des écarts entre transactions
     */
    private double calculerMoyenneEcart(List<LocalDateTime> historique) {
        if (historique.size() <= 1) return 0;

        long totalEcart = 0;
        int nombreEcarts = 0;
        LocalDateTime precedente = null;

        for (LocalDateTime date : historique) {
            if (precedente != null) {
                totalEcart += ChronoUnit.SECONDS.between(precedente, date);
                nombreEcarts++;
            }
            precedente = date;
        }

        return nombreEcarts > 0 ? (double) totalEcart / nombreEcarts : 0;
    }

    /**
     * Enregistre une tentative de transaction
     */
    public void enregistrerTentative(Wallet wallet, boolean succes) {
        LocalDateTime maintenant = LocalDateTime.now();

        if (!succes) {
            // Transaction échouée
            wallet.setTentativesEchouees(wallet.getTentativesEchouees() + 1);
            wallet.setDateDerniereTentative(maintenant);

            if (wallet.getTentativesEchouees() >= SEUIL_TRANSACTIONS_RAPIDES) {
                bloquerCompte(wallet, "Trop de tentatives échouées");
            }

            walletService.mettreAJourWallet(wallet);
        } else {
            // Transaction réussie - on utilise la méthode de vérification rapide
            verifierTransactionsRapides(wallet);
        }
    }

    /**
     * Bloque le compte
     */
    private void bloquerCompte(Wallet wallet, String raison) {
        wallet.setEstBloque(true);
        wallet.setDateDerniereTentative(LocalDateTime.now());
        walletService.mettreAJourWallet(wallet);

        String message = String.format(
                "🔒 Votre compte %s a été bloqué pour sécurité.\n" +
                        "Raison : %s\n" +
                        "Il sera automatiquement débloqué dans %d minutes.\n" +
                        "Si vous n'êtes pas à l'origine de ces transactions, contactez votre conseiller.",
                wallet.getNomProprietaire(), raison, DUREE_BLOCAGE_MINUTES
        );

        System.out.println("\n🚫 COMPTE BLOQUÉ: " + wallet.getNomProprietaire() + " - " + raison);
        System.out.println("📧 Notification envoyée à " + wallet.getEmail());

        // Notifications
        if (wallet.getEmail() != null && !wallet.getEmail().isEmpty()) {
            emailService.envoyerEmailSimple(
                    wallet.getEmail(),
                    "🔒 Alerte sécurité - Compte bloqué",
                    message
            );
        }

        if (wallet.getTelephone() != null && !wallet.getTelephone().isEmpty()) {
            smsService.envoyerCodeSms(
                    wallet.getTelephone(),
                    "Compte bloqué pour sécurité. Contactez votre conseiller."
            );
        }
    }

    /**
     * Débloque le compte
     */
    private void debloquerCompte(Wallet wallet) {
        wallet.setEstBloque(false);
        wallet.setTentativesEchouees(0);
        walletService.mettreAJourWallet(wallet);

        System.out.println("✅ Compte débloqué: " + wallet.getNomProprietaire());

        if (wallet.getEmail() != null && !wallet.getEmail().isEmpty()) {
            emailService.envoyerEmailSimple(
                    wallet.getEmail(),
                    "✅ Compte débloqué",
                    "Votre compte a été automatiquement débloqué. Vous pouvez à nouveau effectuer des transactions."
            );
        }
    }

    /**
     * Nettoie l'historique (optionnel)
     */
    public void nettoyerHistorique(int walletId) {
        historiqueTransactions.remove(walletId);
    }
}