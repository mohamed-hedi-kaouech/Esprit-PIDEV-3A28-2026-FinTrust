package org.example.Model.Loan.LoanClass;

import org.example.Model.Loan.LoanEnum.LoanStatus;

import java.time.LocalDateTime;
import java.util.List;

public class Loan {

    //Attributs
    private int loanId;
    private double amount;
    private int duration;
    private LoanStatus status;              // duration in months
    private double interestRate;         // annual interest rate
    private double remainingPrincipal;
    private List<Repayment> repayments;
    private LocalDateTime createdAt;

    // Constructor (called AFTER eligibility check)
    public Loan(
            double amount,
            int duration,
            double interestRate,
            double remainingPrincipal) {

        this.amount = amount;
        this.duration = duration;
        this.interestRate = interestRate;
        this.remainingPrincipal = remainingPrincipal ;
        this.status = LoanStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }
    public Loan(
            int loanId,
            double amount,
            int duration,
            LoanStatus status,
            double interestRate,
            double remainingPrincipal
            ) {
        this.loanId= loanId;
        this.amount = amount;
        this.duration = duration;
        this.interestRate = interestRate;
        this.remainingPrincipal = remainingPrincipal;
        this.status = status ;
        this.createdAt = LocalDateTime.now();
    }



    public Loan() {}

    // toString
    // ======================

    // ======================
    // Getters & Setters
    // ======================

    public int getLoanId() {
        return loanId;
    }

    public void setLoanId(int id) {
        this.loanId = id;
    }
    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(double interestRate) {
        this.interestRate = interestRate;
    }

    /*public double getMonthlyPayment() {
        return monthlyPayment;
    }*/

    /*public void setMonthlyPayment(double monthlyPayment) {this.monthlyPayment = monthlyPayment;}*/

    public double getRemainingPrincipal() {
        return remainingPrincipal;
    }

    public void setRemainingPrincipal(double remainingPrincipal) {
        this.remainingPrincipal = remainingPrincipal;
    }

    public LoanStatus getStatus() {
        return status;
    }

    public void setStatus(LoanStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreationDate() {
        return createdAt;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.createdAt = creationDate;
    }

    @Override
    public String toString() {
        return "Loan{" +
                "id=" + loanId +
                ", amount=" + amount +
                ", duration=" + duration +
                ", interestRate=" + interestRate +
                ", remainingPrincipal=" + remainingPrincipal +
                ", status=" + status +
                ", creationDate=" + createdAt +
                '}';
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Loan loan = (Loan) o;

        return this.loanId == loan.loanId ;
    }
}
