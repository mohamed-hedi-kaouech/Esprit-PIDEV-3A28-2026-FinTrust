package org.example.Service.UserService;

public class PasswordResetResult {
    private final boolean success;
    private final String message;

    private PasswordResetResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public static PasswordResetResult success(String msg) {
        return new PasswordResetResult(true, msg);
    }
    public static PasswordResetResult failure(String msg) {
        return new PasswordResetResult(false, msg);
    }
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
}