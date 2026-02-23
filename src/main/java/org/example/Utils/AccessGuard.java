package org.example.Utils;

import org.example.Model.User.User;
import org.example.Model.User.UserRole;

public final class AccessGuard {
    private AccessGuard() {
    }

    public static boolean canAccessFullModules() {
        return SessionContext.getInstance().hasFullAccess();
    }

    public static boolean canAccessAdminPages() {
        User user = SessionContext.getInstance().getCurrentUser();
        return user != null && user.getRole() == UserRole.ADMIN;
    }

    public static boolean canAccessKycPages() {
        User user = SessionContext.getInstance().getCurrentUser();
        return user != null && user.getRole() == UserRole.CLIENT;
    }
}
