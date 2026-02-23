package org.example.Utils;

import org.example.Model.User.User;
import org.example.Model.User.UserRole;
import org.example.Model.Kyc.KycStatus;

public final class SessionContext {
    private static final SessionContext INSTANCE = new SessionContext();

    private User currentUser;
    private KycStatus currentKycStatus;
    private String currentKycComment;

    private SessionContext() {
    }

    public static SessionContext getInstance() {
        return INSTANCE;
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

    public boolean hasFullAccess() {
        if (isAdmin()) {
            return true;
        }
        return currentUser != null && currentUser.getRole() == UserRole.CLIENT && currentKycStatus == KycStatus.APPROUVE;
    }

    public boolean hasLimitedAccess() {
        return currentUser != null && currentUser.getRole() == UserRole.CLIENT && currentKycStatus != KycStatus.APPROUVE;
    }

    public void logout() {
        currentUser = null;
        currentKycStatus = null;
        currentKycComment = null;
    }
}
