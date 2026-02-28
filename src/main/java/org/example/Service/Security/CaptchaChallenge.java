package org.example.Service.Security;

public record CaptchaChallenge(
        String challengeId,
        int targetX,
        int sliderMax,
        int pieceWidth,
        int pieceHeight,
        int expiresInSeconds
) {
}