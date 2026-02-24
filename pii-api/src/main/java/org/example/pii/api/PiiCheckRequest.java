package org.example.pii.api;

import jakarta.validation.constraints.NotNull;

public class PiiCheckRequest {
    @NotNull
    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
