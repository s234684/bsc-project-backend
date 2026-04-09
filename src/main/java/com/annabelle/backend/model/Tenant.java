package com.annabelle.backend.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "tenants")
public class Tenant {
    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false)
    private String name;

    public Tenant() {
    }

    public Tenant(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
