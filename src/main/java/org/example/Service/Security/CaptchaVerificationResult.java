package org.example.Service.Security;

public record CaptchaVerificationResult(
        boolean verified,
        String reason,
        String captchaToken
) {
}