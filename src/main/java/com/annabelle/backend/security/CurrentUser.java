package com.annabelle.backend.security;

import com.annabelle.backend.model.RoleName;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class CurrentUser {
    private final Long userId;
    private final UUID tenantId;
    private final String tenantName;
    private final String email;
    private final Set<RoleName> roles;

    public CurrentUser(Long userId, UUID tenantId, String tenantName, String email, Set<RoleName> roles) {
        this.userId = userId;
        this.tenantId = tenantId;
        this.tenantName = tenantName;
        this.email = email;
        this.roles = roles == null ? new HashSet<>() : new HashSet<>(roles);
    }

    public Long getUserId() {
        return userId;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public String getTenantName() {
        return tenantName;
    }

    public String getEmail() {
        return email;
    }

    public Set<RoleName> getRoles() {
        return Collections.unmodifiableSet(roles);
    }

    public boolean hasRole(RoleName roleName) {
        return roles.contains(roleName);
    }

    public boolean isTenantUser(UUID otherTenantId) {
        return tenantId != null && tenantId.equals(otherTenantId);
    }
}
