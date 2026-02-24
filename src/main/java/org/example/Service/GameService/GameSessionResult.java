package org.example.Service.GameService;

public record GameSessionResult(
        boolean valid,
        int grantedPoints,
        String message
) {
}

