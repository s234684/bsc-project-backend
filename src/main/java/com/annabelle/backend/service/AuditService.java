package com.annabelle.backend.service;

import com.annabelle.backend.exception.ApiException;
import com.annabelle.backend.model.AuditAction;
import com.annabelle.backend.model.AuditLog;
import com.annabelle.backend.model.AuditOutcome;
import com.annabelle.backend.model.Role;
import com.annabelle.backend.model.Tenant;
import com.annabelle.backend.model.User;
import com.annabelle.backend.repository.AuditLogRepository;
import com.annabelle.backend.repository.TenantRepository;
import com.annabelle.backend.repository.UserRepository;
import com.annabelle.backend.security.CurrentUser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class AuditService {
    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final HttpServletRequest request;

    public AuditService(
            AuditLogRepository auditLogRepository,
            UserRepository userRepository,
            TenantRepository tenantRepository,
            HttpServletRequest request
    ) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.request = request;
    }

    public void logSuccess(User user, AuditAction action) {
        if (user == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Authenticated user is required for successful audit events");
        }

        save(
                user,
                user.getEmail(),
                rolesToString(user),
                user.getTenant(),
                action,
                AuditOutcome.SUCCESS,
                null
        );
    }

    public void logDenied(User user, AuditAction action, String reason) {
        save(
                user,
                user != null ? user.getEmail() : null,
                user != null ? rolesToString(user) : "ANONYMOUS",
                user != null ? user.getTenant() : null,
                action,
                AuditOutcome.DENIED,
                reason
        );
    }

    public void logDenied(CurrentUser currentUser, AuditAction action, String reason) {
        if (currentUser == null) {
            logDeniedAnonymous(action, reason);
            return;
        }

        User actorUser = userRepository.findById(currentUser.getUserId()).orElse(null);

        if (actorUser != null) {
            logDenied(actorUser, action, reason);
            return;
        }

        Tenant actorTenant = currentUser.getTenantId() != null
                ? tenantRepository.findById(currentUser.getTenantId()).orElse(null)
                : null;

        save(
                null,
                currentUser.getEmail(),
                currentUser.getRoles()
                        .stream()
                        .map(Enum::name)
                        .collect(Collectors.joining(",")),
                actorTenant,
                action,
                AuditOutcome.DENIED,
                reason
        );
    }

    public void logDeniedAnonymous(AuditAction action, String reason) {
        save(
                null,
                null,
                "ANONYMOUS",
                null,
                action,
                AuditOutcome.DENIED,
                reason
        );
    }

    private void save(
            User actorUser,
            String actorEmail,
            String actorRoles,
            Tenant actorTenant,
            AuditAction action,
            AuditOutcome outcome,
            String reason
    ) {
        if (action == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Audit action is required");
        }

        if (outcome == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Audit outcome is required");
        }

        AuditLog auditLog = new AuditLog(
                actorUser,
                actorEmail,
                actorRoles,
                actorTenant,
                action,
                outcome,
                reason,
                currentRequestPath()
        );

        try {
            auditLogRepository.save(auditLog);
        } catch (RuntimeException ignored) {
            // Do not break the original business request if audit logging fails.
            // In production, this should be logged with a logger.
        }
    }

    private String currentRequestPath() {
        return request.getRequestURI();
    }

    private String rolesToString(User user) {
        return user.getRoles()
                .stream()
                .map(Role::getName)
                .map(Enum::name)
                .collect(Collectors.joining(","));
    }
}
