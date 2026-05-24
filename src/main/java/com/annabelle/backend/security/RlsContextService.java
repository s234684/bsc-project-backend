package com.annabelle.backend.security;

import com.annabelle.backend.exception.ApiException;
import com.annabelle.backend.model.RoleName;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class RlsContextService {

    private final CurrentUserHolder currentUserHolder;

    @PersistenceContext
    private EntityManager entityManager;

    public RlsContextService(CurrentUserHolder currentUserHolder) {
        this.currentUserHolder = currentUserHolder;
    }

    public void setTenant() {
        CurrentUser currentUser = currentUserHolder.getCurrentUser();

        if (currentUser == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        if (currentUser.hasRole(RoleName.PLATFORM_ADMIN)) {
            throw new ApiException(HttpStatus.FORBIDDEN,
                    "Access denied: platform administrators cannot access tenant data");
        }

        if (currentUser.getTenantId() == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "No current tenant");
        }

        entityManager
                .createNativeQuery("select set_config('app.current_tenant_id', :tenantId, true)")
                .setParameter("tenantId", currentUser.getTenantId().toString())
                .getSingleResult();
    }
}
