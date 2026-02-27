package org.example.Service.WalletService;

import org.example.Model.Wallet.ClassWallet.ClientRisk;
import org.example.Model.Wallet.ClassWallet.Wallet;
import org.example.Model.Wallet.ClassWallet.Transaction;
import org.example.Model.Wallet.ClassWallet.Cheque;
import okhttp3.*;
import org.json.JSONObject;
import org.json.JSONArray;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

public class BankingIAService {

    private static final String API_KEY = "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";
    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";

    private OkHttpClient client;
    private WalletService walletService;
    private TransactionService transactionService;
    private ChequeService chequeService;
    private ScoreConfianceService scoreConfianceService;

    public BankingIAService() {
        this.client = new OkHttpClient();
        this.walletService = new WalletService();
        this.transactionService = new TransactionService();
        this.chequeService = new ChequeService();
        this.scoreConfianceService = new ScoreConfianceService();
    }

    /**
     * Analyse complète d'un client avec recommandations bancaires
     */
    public BankingAnalysis analyserClient(int clientId, int walletId) {
        // Récupérer toutes les données réelles
        Wallet wallet = walletService.getWalletById(walletId);
        List<Transaction> transactions = transactionService.getTransactionsByWallet(walletId);
        List<Cheque> cheques = chequeService.getChequesByWallet(walletId);

        // Calculer les métriques
        ClientMetrics metrics = calculerMetrics(wallet, transactions, cheques);

        // Construire le prompt pour OpenAI
        String prompt = construirePromptComplet(wallet, transactions, cheques, metrics);

        // Appeler OpenAI (méthode NON statique)
        String analyseIA = appelerOpenAI(prompt);

        // Générer la décision finale
        Decision finale = prendreDecision(metrics, analyseIA);

        return new BankingAnalysis(metrics, analyseIA, finale);
    }

    /**
     * Calcule toutes les métriques pertinentes
     */
    private ClientMetrics calculerMetrics(Wallet wallet, List<Transaction> transactions, List<Cheque> cheques) {
        ClientMetrics metrics = new ClientMetrics();

        // Informations de base
        metrics.nom = wallet.getNomProprietaire();
        metrics.solde = wallet.getSolde();
        metrics.ancienneteMois = ChronoUnit.MONTHS.between(
                wallet.getDateCreation(), LocalDateTime.now());

        // Analyse des transactions
        metrics.totalTransactions = transactions.size();
        metrics.montantMoyenTransaction = transactions.stream()
                .mapToDouble(Transaction::getMontant)
                .average().orElse(0);

        // Transactions par type
        metrics.nbDepots = transactions.stream()
                .filter(t -> "DEPOT".equals(t.getType()))
                .count();
        metrics.nbRetraits = transactions.stream()
                .filter(t -> "RETRAIT".equals(t.getType()))
                .count();
        metrics.nbTransferts = transactions.stream()
                .filter(t -> "TRANSFERT".equals(t.getType()))
                .count();

        // Comportement à risque
        metrics.retraitsEleves = transactions.stream()
                .filter(t -> "RETRAIT".equals(t.getType()) && t.getMontant() > 1000)
                .count();

        // Analyse des chèques
        metrics.totalCheques = cheques.size();
        metrics.chequesRefuses = cheques.stream()
                .filter(c -> "REJETE".equals(c.getStatut()))
                .count();
        metrics.chequesEnAttente = cheques.stream()
                .filter(c -> "EMIS".equals(c.getStatut()) || "RESERVE".equals(c.getStatut()))
                .count();
        metrics.chequesPayes = cheques.stream()
                .filter(c -> "PAYE".equals(c.getStatut()))
                .count();

        // Stabilité du solde
        metrics.joursNegatifs = wallet.isEnDecouvert() ? 1 : 0;
        metrics.variationSolde = calculerVariationSolde(transactions, wallet);

        // Score de confiance
        metrics.scoreConfiance = scoreConfianceService.calculerScore(wallet, transactions).getScoreGlobal();

        return metrics;
    }

    private double calculerVariationSolde(List<Transaction> transactions, Wallet wallet) {
        if (transactions.isEmpty()) return 0;
        double max = transactions.stream().mapToDouble(Transaction::getMontant).max().orElse(0);
        double min = transactions.stream().mapToDouble(Transaction::getMontant).min().orElse(0);
        return max - min;
    }

    /**
     * Construit un prompt détaillé avec toutes les données réelles
     */
    private String construirePromptComplet(Wallet wallet, List<Transaction> transactions,
                                           List<Cheque> cheques, ClientMetrics metrics) {

        // Extraire les 10 dernières transactions
        String dernieresTransactions = transactions.stream()
                .sorted((t1, t2) -> t2.getDate_transaction().compareTo(t1.getDate_transaction()))
                .limit(10)
                .map(t -> String.format("  - %s: %s %.2f€ (%s)",
                        t.getDate_transaction().toLocalDate(),
                        t.getType(),
                        t.getMontant(),
                        t.getDescription() != null ? t.getDescription() : ""))
                .collect(Collectors.joining("\n"));

        // Extraire les chèques récents
        String chequesRecents = cheques.stream()
                .sorted((c1, c2) -> c2.getDate_emission().compareTo(c1.getDate_emission()))
                .limit(5)
                .map(c -> String.format("  - %s: %.2f€ pour %s [%s]",
                        c.getNumero_cheque(),
                        c.getMontant(),
                        c.getBeneficiaire(),
                        c.getStatut()))
                .collect(Collectors.joining("\n"));

        return String.format(
                "Tu es un conseiller bancaire expert. Analyse ce client et donne des recommandations précises.\n\n" +
                        "=== DONNÉES CLIENT ===\n" +
                        "Nom: %s\n" +
                        "Ancienneté: %d mois\n" +
                        "Solde actuel: %.2f €\n" +
                        "Score de confiance: %d/100\n\n" +

                        "=== ACTIVITÉ BANCAIRE ===\n" +
                        "Total transactions: %d\n" +
                        "  - Dépôts: %d\n" +
                        "  - Retraits: %d\n" +
                        "  - Transferts: %d\n" +
                        "Montant moyen transaction: %.2f €\n" +
                        "Retraits élevés (>1000€): %d\n\n" +

                        "=== CHÈQUES ===\n" +
                        "Total chèques émis: %d\n" +
                        "Chèques rejetés: %d\n" +
                        "Chèques payés: %d\n" +
                        "Chèques en attente: %d\n\n" +

                        "=== DERNIERS CHÈQUES ===\n%s\n\n" +

                        "=== DERNIÈRES TRANSACTIONS ===\n%s\n\n" +

                        "=== QUESTIONS À RÉPONDRE ===\n" +
                        "1. PRÉDICTION: Quel sera le comportement de ce client dans les 3 prochains mois? (amélioration, stabilité, dégradation)\n" +
                        "2. CHÉQUIER: Doit-on accepter ou refuser sa demande de chéquier? Pourquoi?\n" +
                        "3. CLASSEMENT: Dans quelle catégorie classer ce client? (VIP, Standard, À surveiller, Risqué)\n" +
                        "4. PRIVILÈGES: Quels privilèges spécifiques lui accorder? (plafonds, frais, services)\n" +
                        "5. RECOMMANDATIONS: Quelles actions concrètes recommandez-vous?\n\n" +

                        "Format de réponse (en français):\n" +
                        "=== ANALYSE ===\n" +
                        "[Résumé en 2 phrases]\n\n" +
                        "=== PRÉDICTION ===\n" +
                        "[Prédiction détaillée]\n\n" +
                        "=== DÉCISION CHÉQUIER ===\n" +
                        "[ACCEPTER/REFUSER] - [Raison]\n\n" +
                        "=== CLASSEMENT ===\n" +
                        "[Catégorie] - [Justification]\n\n" +
                        "=== PRIVILÈGES ===\n" +
                        "[Liste des privilèges]\n\n" +
                        "=== RECOMMANDATIONS ===\n" +
                        "[Actions recommandées]",

                metrics.nom, metrics.ancienneteMois, metrics.solde, metrics.scoreConfiance,
                metrics.totalTransactions, metrics.nbDepots, metrics.nbRetraits, metrics.nbTransferts,
                metrics.montantMoyenTransaction, metrics.retraitsEleves,
                metrics.totalCheques, metrics.chequesRefuses, metrics.chequesPayes, metrics.chequesEnAttente,
                chequesRecents,
                dernieresTransactions
        );
    }

    /**
     * Appelle l'API OpenAI - MÉTHODE NON STATIQUE
     */
    private String appelerOpenAI(String prompt) {  // ← NON STATIQUE
        JSONObject json = new JSONObject();
        json.put("model", "gpt-3.5-turbo");

        JSONArray messages = new JSONArray();

        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", "Tu es un analyste bancaire expert. Réponds en français de manière professionnelle et concise.");

        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);

        messages.put(systemMessage);
        messages.put(userMessage);

        json.put("messages", messages);
        json.put("max_tokens", 600);
        json.put("temperature", 0.7);

        Request request = new Request.Builder()
                .url(API_URL)
                .header("Authorization", "Bearer " + API_KEY)
                .post(RequestBody.create(
                        json.toString(),
                        MediaType.parse("application/json")
                ))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                return "❌ Erreur API: " + response.code() + " - " + response.message();
            }

            String responseBody = response.body().string();
            JSONObject jsonResponse = new JSONObject(responseBody);

            return jsonResponse.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");

        } catch (IOException e) {
            e.printStackTrace();
            return "❌ Erreur de connexion: " + e.getMessage();
        }
    }

    /**
     * Méthode de test (non statique)
     */
    public void testConnexion() {  // ← NON STATIQUE
        String testPrompt = "Réponds simplement 'OK' si tu reçois ce message.";

        try {
            String reponse = appelerOpenAI(testPrompt);
            System.out.println("✅ TEST RÉUSSI ! Réponse: " + reponse);
        } catch (Exception e) {
            System.err.println("❌ ÉCHEC: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ... (le reste du code: prendreDecision, ClientMetrics, Decision, BankingAnalysis)

    /**
     * Prend une décision finale basée sur les métriques et l'analyse IA
     */
    private Decision prendreDecision(ClientMetrics metrics, String analyseIA) {
        Decision decision = new Decision();

        if (metrics.chequesRefuses > 2) {
            decision.chequierAutorise = false;
            decision.raisonChequier = "❌ REFUSÉ: " + metrics.chequesRefuses + " chèque(s) rejeté(s)";
        } else if (metrics.solde < -1000) {
            decision.chequierAutorise = false;
            decision.raisonChequier = "❌ REFUSÉ: Découvert important (" + String.format("%.2f", metrics.solde) + "€)";
        } else if (metrics.scoreConfiance >= 70) {
            decision.chequierAutorise = true;
            decision.raisonChequier = "✅ ACCEPTÉ: Client fiable avec bon score (" + metrics.scoreConfiance + "/100)";
        } else if (metrics.scoreConfiance >= 50) {
            decision.chequierAutorise = true;
            decision.raisonChequier = "✅ ACCEPTÉ sous conditions: Score moyen (" + metrics.scoreConfiance + "/100)";
        } else {
            decision.chequierAutorise = false;
            decision.raisonChequier = "❌ REFUSÉ: Score trop faible (" + metrics.scoreConfiance + "/100)";
        }

        if (metrics.scoreConfiance >= 80 && metrics.chequesRefuses == 0 && metrics.solde > 10000) {
            decision.classement = "🌟🌟🌟🌟🌟 VIP";
            decision.privileges =
                    "• 💳 Plafond de retrait: 5000€/jour\n" +
                            "• ✅ Validation automatique des chèques\n" +
                            "• 💰 Frais bancaires réduits de 50%\n" +
                            "• 👨‍💼 Conseiller personnel dédié\n" +
                            "• 📉 Découvert autorisé: 2000€";
        } else if (metrics.scoreConfiance >= 60 && metrics.chequesRefuses <= 1) {
            decision.classement = "🌟🌟🌟 STANDARD";
            decision.privileges =
                    "• 💳 Plafond de retrait: 1000€/jour\n" +
                            "• 📄 Validation normale des chèques\n" +
                            "• 💰 Frais bancaires standards\n" +
                            "• 📊 Découvert autorisé: 500€";
        } else if (metrics.scoreConfiance >= 40) {
            decision.classement = "⚠️ À SURVEILLER";
            decision.privileges =
                    "• 💳 Plafond de retrait: 300€/jour\n" +
                            "• 🔒 Validation manuelle obligatoire\n" +
                            "• 📋 Surveillance renforcée\n" +
                            "• ❌ Découvert non autorisé";
        } else {
            decision.classement = "🔴 RISQUÉ";
            decision.privileges =
                    "• 💳 Plafond de retrait: 100€/jour\n" +
                            "• 🔒 Validation manuelle stricte\n" +
                            "• ⚠️ Compte sous surveillance\n" +
                            "• 🚫 Chéquier bloqué\n" +
                            "• 📞 Contact conseiller obligatoire";
        }

        decision.analyseComplete = analyseIA;
        return decision;
    }

    /**
     * Classe pour les métriques calculées
     */
    public static class ClientMetrics {
        public String nom;
        public double solde;
        public long ancienneteMois;
        public int totalTransactions;
        public long nbDepots, nbRetraits, nbTransferts;
        public double montantMoyenTransaction;
        public long retraitsEleves;
        public int totalCheques;
        public long chequesRefuses;
        public long chequesPayes;
        public long chequesEnAttente;
        public int joursNegatifs;
        public double variationSolde;
        public int scoreConfiance;

        @Override
        public String toString() {
            return String.format(
                    "👤 %s | Score: %d/100 | Solde: %.2f€ | Rejets: %d | Chèques: %d",
                    nom, scoreConfiance, solde, chequesRefuses, totalCheques
            );
        }
    }

    /**
     * Classe pour la décision finale
     */
    public static class Decision {
        public boolean chequierAutorise;
        public String raisonChequier;
        public String classement;
        public String privileges;
        public String analyseComplete;

        public String getResume() {
            return String.format(
                    "═══════════════════════════════════\n" +
                            "📋 DÉCISION FINALE\n" +
                            "═══════════════════════════════════\n\n" +
                            "📌 CHÉQUIER: %s\n" +
                            "   %s\n\n" +
                            "📌 CLASSEMENT: %s\n\n" +
                            "📌 PRIVILÈGES:\n%s",
                    chequierAutorise ? "✅ ACCEPTÉ" : "❌ REFUSÉ",
                    raisonChequier,
                    classement,
                    privileges
            );
        }
    }

    /**
     * Classe pour le résultat complet
     */
    public static class BankingAnalysis {
        public ClientMetrics metrics;
        public String analyseIA;
        public Decision decision;

        public BankingAnalysis(ClientMetrics metrics, String analyseIA, Decision decision) {
            this.metrics = metrics;
            this.analyseIA = analyseIA;
            this.decision = decision;
        }

        public String getRapportComplet() {
            return "🔍 RAPPORT D'ANALYSE BANCAIRE\n" +
                    "══════════════════════════════════════════════════\n\n" +
                    "📊 MÉTRIQUES:\n" + metrics.toString() + "\n\n" +
                    "🧠 ANALYSE IA:\n" + analyseIA + "\n\n" +
                    decision.getResume();
        }
    }
}