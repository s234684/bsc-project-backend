package com.annabelle.backend.security;

import com.annabelle.backend.exception.ApiException;
import com.annabelle.backend.model.RoleName;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Arrays;
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
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return user;
    }

    public void requireRole(RoleName roleName) {
        if (!currentUser().hasRole(roleName)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Access denied: missing role " + roleName);
        }
    }

    public void requireTenant(UUID tenantId) {
        if (!currentUser().isTenantUser(tenantId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Access denied: wrong tenant");
        }
    }

    public void requireAuthenticated() {
        if (currentUserHolder.getCurrentUser() == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
    }

    public void requireAnyRole(RoleName... roleNames) {
        CurrentUser user = currentUser();

        boolean hasAllowedRole = Arrays.stream(roleNames)
                .anyMatch(user::hasRole);

        if (!hasAllowedRole) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Access denied: missing required role");
        }
    }

    public void requireOwner(Long ownerUserId) {
        CurrentUser user = currentUser();

        if (ownerUserId == null || !ownerUserId.equals(user.getUserId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Access denied: not resource owner");
        }
    }

    public void requireOwnerOrAnyRole(Long ownerUserId, RoleName... roleNames) {
        CurrentUser user = currentUser();

        boolean isOwner = ownerUserId != null && ownerUserId.equals(user.getUserId());
        boolean hasAllowedRole = Arrays.stream(roleNames)
                .anyMatch(user::hasRole);

        if (!isOwner && !hasAllowedRole) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Access denied");
        }
    }

    public CurrentUser currentUserOrNull() {
        return currentUserHolder.getCurrentUser();
    }

}
