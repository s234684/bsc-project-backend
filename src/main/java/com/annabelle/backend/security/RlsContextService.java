package com.annabelle.backend.security;

import com.annabelle.backend.exception.ApiException;
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

        if (currentUser == null || currentUser.getTenantId() == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "No current tenant");
        }

        entityManager
                .createNativeQuery("select set_config('app.current_tenant_id', :tenantId, true)")
                .setParameter("tenantId", currentUser.getTenantId().toString())
                .getSingleResult();
    }
}
