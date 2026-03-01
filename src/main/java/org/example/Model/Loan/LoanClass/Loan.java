package org.example.Model.Loan.LoanClass;

import org.example.Model.Loan.LoanEnum.LoanStatus;
import org.example.Model.Loan.LoanEnum.LoanType;
import org.example.Model.Loan.LoanEnum.RepaymentStatus;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Loan {

    private int loanId;
    private LoanType loanType;
    private double amount;
    private int duration; // months
    private LoanStatus status;
    private double interestRate;
    private double remainingPrincipal;
    private LocalDateTime createdAt;
    private int id_user;


    // ======================
    // MAIN CONSTRUCTOR
    // ======================
    public Loan(LoanType loanType, double amount, int duration,int id_user) {

        this.loanType = loanType;
        this.amount = amount;
        this.duration = duration;
        this.interestRate = loanType.getInterestRate();
        this.remainingPrincipal = amount;
        this.status = LoanStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.id_user=id_user;
    }
    public Loan(LoanType loanType, double amount, int duration) {

        this.loanType = loanType;
        this.amount = amount;
        this.duration = duration;
        this.interestRate = loanType.getInterestRate();
        this.remainingPrincipal = amount;
        this.status = LoanStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.id_user=id_user;
    }

    // Empty constructor (for DB mapping)
    public Loan() {}

    public Loan(double amount, int duration, double interestRate, double amount1) {
    }

    // ======================
    // GETTERS & SETTERS
    // ======================

    public int getLoanId() { return loanId; }
    public void setLoanId(int loanId) { this.loanId = loanId; }

    public LoanType getLoanType() { return loanType; }
    public void setLoanType(LoanType loanType) { this.loanType = loanType; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public LoanStatus getStatus() { return status; }
    public void setStatus(LoanStatus status) { this.status = status; }

    public double getInterestRate() { return interestRate; }
    public void setInterestRate(double interestRate) { this.interestRate = interestRate; }

    public double getRemainingPrincipal() { return remainingPrincipal; }
    public void setRemainingPrincipal(double remainingPrincipal) { this.remainingPrincipal = remainingPrincipal; }

    public LocalDateTime getCreationDate() { return createdAt; }
    public void setCreationDate(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public int getId_user() {
        return id_user;
    }

    public void setId_user(int id_user) {
        this.id_user = id_user;
    }

    @Override
    public String toString() {
        return "Loan{" +
                "loanId=" + loanId +
                ", loanType=" + loanType +
                ", amount=" + amount +
                ", duration=" + duration +
                ", status=" + status +
                ", remainingPrincipal=" + remainingPrincipal +
                '}';
    }

    public static String SQLTable() {
        return """
        CREATE TABLE IF NOT EXISTS loan (
            loanId INT PRIMARY KEY AUTO_INCREMENT,
            loanType VARCHAR(50) NOT NULL,
            amount DECIMAL(12,2) NOT NULL,
            duration INT NOT NULL,
            interest_rate DECIMAL(5,2) NOT NULL,
            remaining_principal DECIMAL(12,2) NOT NULL,
            status VARCHAR(20) NOT NULL,
            createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        );
    """;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Loan)) return false;
        Loan loan = (Loan) o;
        return loanId == loan.loanId;
    }
}
