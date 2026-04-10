package com.annabelle.backend.security;

import com.annabelle.backend.model.RoleName;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthorizationService {

    private final CurrentUserHolder currentUserHolder;

    public AuthorizationService(CurrentUserHolder currentUserHolder) {
        this.currentUserHolder = currentUserHolder;
    }

    public CurrentUser currentUser() {
        CurrentUser user = currentUserHolder.getCurrentUser();
        if (user == null) {
            throw new IllegalStateException("No mock user is set for the current request");
        }
        return user;
    }

    public void requireRole(RoleName roleName) {
        if (!currentUser().hasRole(roleName)) {
            throw new IllegalStateException("Access denied: missing role " + roleName);
        }
    }

    public void requireTenant(UUID tenantId) {
        if (!currentUser().isTenantUser(tenantId)) {
            throw new IllegalStateException("Access denied: wrong tenant");
        }
    }

    public void requireAuthenticated() {
        if (currentUserHolder.getCurrentUser() == null) {
            throw new IllegalStateException("Authentication required");
        }
    }
}
