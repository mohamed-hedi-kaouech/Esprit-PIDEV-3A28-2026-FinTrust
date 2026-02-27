package org.example.Model.Wallet.ClassWallet;

import java.time.LocalDateTime;

public class ScoreConfiance {
    private int id;
    private int walletId;
    private int scoreGlobal;
    private int anciennete;
    private int transactions;
    private int stabilite;
    private String niveau;
    private String recommandation;
    private LocalDateTime dateCalcul;

    // Constructeurs
    public ScoreConfiance() {}

    public ScoreConfiance(int walletId, int scoreGlobal, int anciennete,
                          int transactions, int stabilite, String niveau,
                          String recommandation) {
        this.walletId = walletId;
        this.scoreGlobal = scoreGlobal;
        this.anciennete = anciennete;
        this.transactions = transactions;
        this.stabilite = stabilite;
        this.niveau = niveau;
        this.recommandation = recommandation;
        this.dateCalcul = LocalDateTime.now();
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getWalletId() { return walletId; }
    public void setWalletId(int walletId) { this.walletId = walletId; }

    public int getScoreGlobal() { return scoreGlobal; }
    public void setScoreGlobal(int scoreGlobal) { this.scoreGlobal = scoreGlobal; }

    public int getAnciennete() { return anciennete; }
    public void setAnciennete(int anciennete) { this.anciennete = anciennete; }

    public int getTransactions() { return transactions; }
    public void setTransactions(int transactions) { this.transactions = transactions; }

    public int getStabilite() { return stabilite; }
    public void setStabilite(int stabilite) { this.stabilite = stabilite; }

    public String getNiveau() { return niveau; }
    public void setNiveau(String niveau) { this.niveau = niveau; }

    public String getRecommandation() { return recommandation; }
    public void setRecommandation(String recommandation) { this.recommandation = recommandation; }

    public LocalDateTime getDateCalcul() { return dateCalcul; }
    public void setDateCalcul(LocalDateTime dateCalcul) { this.dateCalcul = dateCalcul; }

    // Méthode utilitaire pour déterminer le niveau à partir du score
    public static String getNiveauFromScore(int score) {
        if (score >= 80) return "⭐ Élevé";
        if (score >= 50) return "🔸 Moyen";
        return "⚠️ Faible";
    }
    // Ajoutez cette méthode si elle n'existe pas
    public int getPenalites() {
        // Si vous n'avez pas de champ penalites, retournez 0 ou calculez-le
        return 0;
    }
}