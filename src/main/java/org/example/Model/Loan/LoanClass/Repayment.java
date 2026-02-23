package org.example.Model.Loan.LoanClass;

import org.example.Model.Loan.LoanEnum.RepaymentStatus;

public class Repayment {
    private int repayId;
    private int loanId;
    private int month;
    private double startingBalance;
    private double monthlyPayment;
    private double capitalPart;
    private double interestPart;
    private double remainingBalance;
    private RepaymentStatus status;

    // Constructor for INSERT (without ID)
    public Repayment(int loanId,
                     int month,
                     double startingBalance,
                     double monthlyPayment,
                     double capitalPart,
                     double interestPart,
                     double remainingBalance,
                     RepaymentStatus status) {
        this.loanId = loanId;
        this.month = month;
        this.startingBalance = startingBalance;
        this.monthlyPayment = monthlyPayment;
        this.capitalPart = capitalPart;
        this.interestPart = interestPart;
        this.remainingBalance = remainingBalance;
        this.status = status;
    }


    // Constructor for SELECT (with ID)
        public Repayment(int repayId,
                         int loanId,
                         int month,
                         double startingBalance,
                         double amount,
                         double capitalPart,
                         double interestPart,
                         double remainingBalance,
                         RepaymentStatus status) {

            this.repayId = repayId;
            this.loanId = loanId;
            this.month = month;
            this.startingBalance = startingBalance;
            this.monthlyPayment = amount;
            this.capitalPart = capitalPart;
            this.interestPart = interestPart;
            this.remainingBalance = remainingBalance;
            this.status = status;
        }

    public Repayment() {

    }

    // Getters & Setters
    public int getRepayId() {
        return repayId;
    }

    public void setRepayId(int repayId) {
        this.repayId = repayId;
    }

    public int getLoanId() {
        return loanId;
    }

    public void setLoanId(int loanId) {
        this.loanId = loanId;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public double getStartingBalance() {
        return startingBalance;
    }

    public void setStartingBalance(double startingBalance) {
        this.startingBalance = startingBalance;
    }

    public double getCapitalPart() {
        return capitalPart;
    }

    public void setCapitalPart(double capitalPart) {
        this.capitalPart = capitalPart;
    }

    public double getInterestPart() {
        return interestPart;
    }

    public void setInterestPart(double interestPart) {
        this.interestPart = interestPart;
    }

    public RepaymentStatus getStatus() {
        return status;
    }

    public void setStatus(RepaymentStatus status) {
        this.status = status;
    }

    public double getMonthlyPayment() {
        return monthlyPayment;
    }

    public void setMonthlyPayment(double monthlyPayment) {
        this.monthlyPayment = monthlyPayment;
    }

    public double getRemainingBalance() {
        return remainingBalance;
    }

    public void setRemainingBalance(double remainingBalance) {
        this.remainingBalance = remainingBalance;
    }

    public static String SQLTable() {
        return """
            CREATE TABLE IF NOT EXISTS repayment (
                repayId INT PRIMARY KEY AUTO_INCREMENT,
                loanId INT NOT NULL,
                month INT NOT NULL,
            
                startingBalance DECIMAL(10,2) NOT NULL,
                monthlyPayment DECIMAL(10,2) NOT NULL,
                capitalPart DECIMAL(10,2) NOT NULL,
                interestPart DECIMAL(10,2) NOT NULL,
                remainingBalance DECIMAL(10,2) NOT NULL,
            
                status VARCHAR(20) NOT NULL,
            
                CONSTRAINT fk_repayment_loan
                    FOREIGN KEY (loanId)
                    REFERENCES loan(loanId)
                    ON DELETE CASCADE
            );
                """;
    }
}