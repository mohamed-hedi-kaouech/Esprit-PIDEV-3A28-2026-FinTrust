package org.example.moderation.api;

import java.util.List;

public class ModerationCheckResponse {
    private final boolean allowed;
    private final String reason;
    private final String code;
    private final List<String> categories;

    public ModerationCheckResponse(boolean allowed, String reason, String code, List<String> categories) {
        this.allowed = allowed;
        this.reason = reason;
        this.code = code;
        this.categories = categories;
    }

    public boolean isAllowed() {
        return allowed;
    }

    public String getReason() {
        return reason;
    }

    public String getCode() {
        return code;
    }

    public List<String> getCategories() {
        return categories;
    }
}
