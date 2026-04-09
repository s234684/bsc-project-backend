package com.annabelle.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "questionnaires")
public class Questionnaire {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // potentially change to many to many ? bc there might be one basic questionnaire for everyone
    @ManyToOne
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(nullable = false)
    private String title;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User creator;

    public Questionnaire() {
    }

    public Questionnaire(Tenant tenant, String title, User creator) {
        this.tenant = tenant;
        this.title = title;
        this.creator = creator;
    }

    public Long getId() {
        return id;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }
}
