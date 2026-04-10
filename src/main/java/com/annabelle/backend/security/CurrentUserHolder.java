package com.annabelle.backend.security;

public class CurrentUserHolder {
    private static final ThreadLocal<CurrentUser> CURRENT_USER = new ThreadLocal<>();

    public CurrentUser getCurrentUser() {
        return CURRENT_USER.get();
    }

    public void setCurrentUser(CurrentUser user) {
        CURRENT_USER.set(user);
    }

    public void clear() {
        CURRENT_USER.remove();
    }
}
