package org.example.pii.api;

import java.util.List;

public class PiiCheckResponse {
    private final boolean allowed;
    private final List<String> detected;
    private final String reason;

    public PiiCheckResponse(boolean allowed, List<String> detected, String reason) {
        this.allowed = allowed;
        this.detected = detected;
        this.reason = reason;
    }

    public boolean isAllowed() {
        return allowed;
    }

    public List<String> getDetected() {
        return detected;
    }

    public String getReason() {
        return reason;
    }
}
