package org.example.Service.AnalyticsService;

import org.example.Model.Kyc.Kyc;
import org.example.Model.Kyc.KycStatus;
import org.example.Model.User.User;
import org.example.Repository.KycRepository;
import org.example.Repository.UserRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserScoreService {

    private final UserRepository userRepository;
    private final KycRepository kycRepository;
    private final AnalyticsService analyticsService;

    public UserScoreService() {
        this.userRepository = new UserRepository();
        this.kycRepository = new KycRepository();
        this.analyticsService = new AnalyticsService();
    }

    public List<UserHealthScore> computeHealthScores() {
        List<User> users = userRepository.findAll();
        Map<Integer, UserSegmentItem> segmentMap = new HashMap<>();
        for (UserSegmentItem segment : analyticsService.getUserSegments()) {
            segmentMap.put(segment.userId(), segment);
        }

        List<UserHealthScore> scores = new ArrayList<>();
        for (User user : users) {
            if (user.getRole() == null || !"CLIENT".equals(user.getRole().name())) {
                continue;
            }

            int score = 0;
            List<String> reasons = new ArrayList<>();

            UserSegmentItem segment = segmentMap.get(user.getId());
            if (segment != null && segment.lastSuccessfulLogin() != null &&
                    ChronoUnit.DAYS.between(segment.lastSuccessfulLogin(), LocalDateTime.now()) <= 7) {
                score += 10;
                reasons.add("+10 login recent");
            }

            if (isProfileComplete(user)) {
                score += 10;
                reasons.add("+10 profil complet");
            }

            if (analyticsService.hasSuccessfulOtpForUser(user.getId())) {
                score += 20;
                reasons.add("+20 OTP valide");
            }

            if (segment != null && segment.failedLogins30Days() >= 5) {
                score -= 30;
                reasons.add("-30 echecs login >= 5");
            }

            UserHealthCategory category;
            if (score >= 30) {
                category = UserHealthCategory.GREEN;
            } else if (score >= 10) {
                category = UserHealthCategory.WARNING;
            } else {
                category = UserHealthCategory.RISK;
            }

            String explanation = reasons.isEmpty() ? "Aucune regle declenchee" : String.join(", ", reasons);
            scores.add(new UserHealthScore(user.getId(), user.getEmail(), score, category, explanation));
        }

        scores.sort(Comparator.comparingInt(UserHealthScore::score).reversed());
        return scores;
    }

    private boolean isProfileComplete(User user) {
        if (isBlank(user.getNom()) || isBlank(user.getPrenom()) || isBlank(user.getEmail()) || isBlank(user.getNumTel())) {
            return false;
        }
        Kyc kyc = kycRepository.findByUserId(user.getId()).orElse(null);
        if (kyc == null) return false;
        if (isBlank(kyc.getCin()) || isBlank(kyc.getAdresse()) || kyc.getDateNaissance() == null) return false;
        return kyc.getStatut() == KycStatus.APPROUVE || kyc.getStatut() == KycStatus.EN_ATTENTE;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
