package org.example.Service.Security;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class CaptchaService {

    private static final int CHALLENGE_TTL_SECONDS = 120;
    private static final int TOKEN_TTL_SECONDS = 180;
    private static final int MAX_TRIES = 5;
    private static final int TOLERANCE = 6;
    private static final int PIECE_SIZE = 52;
    private static final int SLIDER_MAX = 260;

    private static final CaptchaService INSTANCE = new CaptchaService();

    private final Map<String, ChallengeData> challenges = new ConcurrentHashMap<>();
    private final Map<String, TokenData> tokens = new ConcurrentHashMap<>();

    private CaptchaService() {
    }

    public static CaptchaService getInstance() {
        return INSTANCE;
    }

    public CaptchaChallenge createChallenge(String fingerprint, String intent) {
        cleanupExpired();
        String challengeId = UUID.randomUUID().toString();
        int targetX = ThreadLocalRandom.current().nextInt(40, SLIDER_MAX - 40);
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(CHALLENGE_TTL_SECONDS);
        challenges.put(challengeId, new ChallengeData(challengeId, targetX, fingerprint, intent, expiresAt, 0));
        return new CaptchaChallenge(challengeId, targetX, SLIDER_MAX, PIECE_SIZE, PIECE_SIZE, CHALLENGE_TTL_SECONDS);
    }

    public CaptchaVerificationResult verify(String challengeId, int x, List<CaptchaTrailPoint> trail, String fingerprint, String intent) {
        cleanupExpired();
        ChallengeData challenge = challenges.get(challengeId);
        if (challenge == null) {
            return new CaptchaVerificationResult(false, "MISSING_CHALLENGE", null);
        }
        if (LocalDateTime.now().isAfter(challenge.expiresAt())) {
            challenges.remove(challengeId);
            return new CaptchaVerificationResult(false, "EXPIRED", null);
        }
        if (!safeEquals(challenge.fingerprint(), fingerprint) || !safeEquals(challenge.intent(), intent)) {
            return new CaptchaVerificationResult(false, "FINGERPRINT_MISMATCH", null);
        }
        if (challenge.tries() >= MAX_TRIES) {
            return new CaptchaVerificationResult(false, "MAX_TRIES", null);
        }

        challenges.put(challengeId, challenge.withTries(challenge.tries() + 1));

        if (Math.abs(x - challenge.targetX()) > TOLERANCE) {
            return new CaptchaVerificationResult(false, "MISALIGN", null);
        }

        String trailReason = validateTrail(trail);
        if (trailReason != null) {
            return new CaptchaVerificationResult(false, trailReason, null);
        }

        challenges.remove(challengeId);
        String token = UUID.randomUUID().toString();
        tokens.put(token, new TokenData(token, fingerprint, intent, LocalDateTime.now().plusSeconds(TOKEN_TTL_SECONDS), false));
        return new CaptchaVerificationResult(true, "OK", token);
    }

    public boolean consumeCaptchaToken(String token, String fingerprint, String intent) {
        cleanupExpired();
        if (token == null || token.isBlank()) return false;
        TokenData data = tokens.get(token);
        if (data == null || data.consumed()) return false;
        if (LocalDateTime.now().isAfter(data.expiresAt())) {
            tokens.remove(token);
            return false;
        }
        if (!safeEquals(data.fingerprint(), fingerprint) || !safeEquals(data.intent(), intent)) return false;
        tokens.put(token, data.consume());
        return true;
    }

    private String validateTrail(List<CaptchaTrailPoint> trail) {
        if (trail == null || trail.size() < 3) return "TRAIL_TOO_SHORT";

        // Nettoyage: certains events JavaFX arrivent avec meme timestamp.
        List<CaptchaTrailPoint> cleaned = new java.util.ArrayList<>();
        CaptchaTrailPoint prev = null;
        for (CaptchaTrailPoint p : trail) {
            if (p == null) continue;
            if (prev != null && p.t() == prev.t() && p.x() == prev.x()) continue;
            cleaned.add(p);
            prev = p;
        }

        if (cleaned.size() < 3) return "TRAIL_TOO_SHORT";

        CaptchaTrailPoint first = cleaned.get(0);
        CaptchaTrailPoint last = cleaned.get(cleaned.size() - 1);
        long duration = last.t() - first.t();
        if (duration < 400) return "TRAIL_TOO_FAST";

        int nonMonotonic = 0;
        for (int i = 1; i < cleaned.size(); i++) {
            int dx = cleaned.get(i).x() - cleaned.get(i - 1).x();
            long dt = cleaned.get(i).t() - cleaned.get(i - 1).t();
            if (dt < 0) return "TRAIL_INVALID_TIME";
            if (Math.abs(dx) > 140) return "TRAIL_IMPOSSIBLE_JUMP";
            if (dx < -10) nonMonotonic++;
        }
        if (nonMonotonic > 2) return "TRAIL_NON_HUMAN";
        return null;
    }

    private void cleanupExpired() {
        LocalDateTime now = LocalDateTime.now();
        challenges.entrySet().removeIf(e -> now.isAfter(e.getValue().expiresAt()));
        tokens.entrySet().removeIf(e -> now.isAfter(e.getValue().expiresAt()));
    }

    private boolean safeEquals(String a, String b) {
        return (a == null ? "" : a).equals(b == null ? "" : b);
    }

    private record ChallengeData(
            String challengeId,
            int targetX,
            String fingerprint,
            String intent,
            LocalDateTime expiresAt,
            int tries
    ) {
        private ChallengeData withTries(int newTries) {
            return new ChallengeData(challengeId, targetX, fingerprint, intent, expiresAt, newTries);
        }
    }

    private record TokenData(
            String token,
            String fingerprint,
            String intent,
            LocalDateTime expiresAt,
            boolean consumed
    ) {
        private TokenData consume() {
            return new TokenData(token, fingerprint, intent, expiresAt, true);
        }
    }
}
