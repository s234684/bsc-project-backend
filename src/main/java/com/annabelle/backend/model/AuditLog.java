package com.annabelle.backend.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "audit_log")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @ManyToOne
    @JoinColumn(name = "actor_user_id")
    private User actorUser;

    @Column(name = "actor_email")
    private String actorEmail;

    @Column(name = "actor_roles")
    private String actorRoles;

    @ManyToOne
    @JoinColumn(name = "actor_tenant_id")
    private Tenant actorTenant;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false)
    private AuditAction action;

    @Enumerated(EnumType.STRING)
    @Column(name = "outcome", nullable = false)
    private AuditOutcome outcome;

    @Column(name = "reason")
    private String reason;

    @Column(name = "request_path", columnDefinition = "TEXT")
    private String requestPath;

    public AuditLog() {
    }

    public AuditLog(
            User actorUser,
            String actorEmail,
            String actorRoles,
            Tenant actorTenant,
            AuditAction action,
            AuditOutcome outcome,
            String reason,
            String requestPath
    ) {
        this.createdAt = Instant.now();
        this.actorUser = actorUser;
        this.actorEmail = actorEmail;
        this.actorRoles = actorRoles;
        this.actorTenant = actorTenant;
        this.action = action;
        this.outcome = outcome;
        this.reason = reason;
        this.requestPath = requestPath;
    }

    public Long getId() {
        return id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public User getActorUser() {
        return actorUser;
    }

    public String getActorEmail() {
        return actorEmail;
    }

    public String getActorRoles() {
        return actorRoles;
    }

    public Tenant getActorTenant() {
        return actorTenant;
    }

    public AuditAction getAction() {
        return action;
    }

    public AuditOutcome getOutcome() {
        return outcome;
    }

    public String getReason() {
        return reason;
    }

    public String getRequestPath() {
        return requestPath;
    }
}