package org.example.Service.LoanService;

import org.example.Model.Loan.LoanClass.Loan;

public class RiskService {

    public int calculateRiskScore(Loan loan) {

        int score = 0;

        // Risk from amount
        if (loan.getAmount() >= 20000) {
            score += 50;
        } else if (loan.getAmount() >= 10000) {
            score += 30;
        } else {
            score += 10;
        }

        // Risk from duration
        if (loan.getDuration() >= 60) {
            score += 40;
        } else if (loan.getDuration() >= 36) {
            score += 25;
        } else {
            score += 10;
        }

        return score; // max = 90
    }

    public String classifyRisk(int score) {
        if (score >= 70) return "HIGH";
        if (score >= 40) return "MEDIUM";
        return "LOW";
    }
}