package org.example.Model.AdminOps;

public enum AdminTaskStatus {
    TODO,
    DOING,
    DONE;

    public static AdminTaskStatus fromDb(String value) {
        if (value == null) return TODO;
        try {
            return AdminTaskStatus.valueOf(value.trim().toUpperCase());
        } catch (Exception e) {
            return TODO;
        }
    }
}

