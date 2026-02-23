package org.example.Model.AdminOps;

public enum AdminTaskPriority {
    LOW,
    MEDIUM,
    HIGH,
    URGENT;

    public static AdminTaskPriority fromDb(String value) {
        if (value == null) return MEDIUM;
        try {
            return AdminTaskPriority.valueOf(value.trim().toUpperCase());
        } catch (Exception e) {
            return MEDIUM;
        }
    }
}

