package org.example.Service.UserService;

import org.example.Model.User.User;
import org.example.Model.Kyc.KycStatus;

public class LoginResult {
    private final boolean success;
    private final String message;
    private final User user;
    private final KycStatus kycStatus;
    private final String kycComment;

    private LoginResult(boolean success, String message, User user, KycStatus kycStatus, String kycComment) {
        this.success = success;
        this.message = message;
        this.user = user;
        this.kycStatus = kycStatus;
        this.kycComment = kycComment;
    }

    public static LoginResult success(User user, String message) {
        return new LoginResult(true, message, user, null, null);
    }

    public static LoginResult success(User user, String message, KycStatus kycStatus, String kycComment) {
        return new LoginResult(true, message, user, kycStatus, kycComment);
    }

    public static LoginResult failure(String message) {
        return new LoginResult(false, message, null, null, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public User getUser() {
        return user;
    }

    public KycStatus getKycStatus() {
        return kycStatus;
    }

    public String getKycComment() {
        return kycComment;
    }
}