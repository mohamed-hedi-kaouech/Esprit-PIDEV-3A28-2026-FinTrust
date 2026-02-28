package org.example.Service.KycService;

public class KycSubmitResult {
    private final boolean success;
    private final String message;

    private KycSubmitResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public static KycSubmitResult success(String message) {
        return new KycSubmitResult(true, message);
    }

    public static KycSubmitResult failure(String message) {
        return new KycSubmitResult(false, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}