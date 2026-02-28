package org.example.Model.Loan.LoanEnum;

public enum LoanType {

    HOUSING("Prêt Immobilier", 6.75, 150000, 20),
    CAR("Prêt Auto", 10.0, 50000, 5),
    PERSONAL("Prêt Personnel", 8.25, 25000, 3);

    private final String label;
    private final double interestRate;
    private final double maxAmount;
    private final int maxYears;

    LoanType(String label,
             double interestRate,
             double maxAmount,
             int maxYears) {

        this.label = label;
        this.interestRate = interestRate;
        this.maxAmount = maxAmount;
        this.maxYears = maxYears;
    }

    public String getLabel() {
        return label;
    }

    public double getInterestRate() {
        return interestRate;
    }

    public double getMaxAmount() {
        return maxAmount;
    }

    public int getMaxYears() {
        return maxYears;
    }

    public int getMaxMonths() {
        return maxYears * 12;
    }

    // ======================
    // Validation Helper
    // ======================
    public boolean isValidAmount(double amount) {
        return amount > 0 && amount <= maxAmount;
    }

    public boolean isValidDuration(int months) {
        return months > 0 && months <= getMaxMonths();
    }

    // ======================
    // Safe DB Conversion
    // ======================
    public static LoanType fromString(String value) {
        for (LoanType type : LoanType.values()) {
            if (type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid LoanType: " + value);
    }
}
