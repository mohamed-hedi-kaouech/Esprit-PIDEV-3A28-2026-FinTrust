package org.example.Utils;

import org.example.Model.Kyc.KycStatus;
import org.example.Model.User.User;
import org.example.Model.User.UserRole;

public class SessionContext {

    private static SessionContext instance;

    private User currentUser;
    private KycStatus currentKycStatus;
    private String currentKycComment;

    private SessionContext() {}

    public static SessionContext getInstance() {
        if (instance == null) instance = new SessionContext();
        return instance;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public KycStatus getCurrentKycStatus() {
        return currentKycStatus;
    }

    public void setCurrentKycStatus(KycStatus currentKycStatus) {
        this.currentKycStatus = currentKycStatus;
    }

    public String getCurrentKycComment() {
        return currentKycComment;
    }

    public void setCurrentKycComment(String currentKycComment) {
        this.currentKycComment = currentKycComment;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean isAdmin() {
        return currentUser != null && currentUser.getRole() == UserRole.ADMIN;
    }

    public boolean isClient() {
        return currentUser != null && currentUser.getRole() == UserRole.CLIENT;
    }

    public boolean hasFullAccess() {
        if (isAdmin()) {
            return true;
        }
        return isClient() && currentKycStatus == KycStatus.APPROUVE;
    }

    public boolean hasLimitedAccess() {
        return isClient() && currentKycStatus != KycStatus.APPROUVE;
    }

    public void logout() {
        currentUser = null;
        currentKycStatus = null;
        currentKycComment = null;
    }
}
