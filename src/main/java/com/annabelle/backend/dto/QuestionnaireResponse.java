package com.annabelle.backend.dto;

import java.util.UUID;

public class QuestionnaireResponse {
    private final Long id;
    private final String title;
    private final UUID tenantId;
    private final Long creatorId;

    public QuestionnaireResponse(Long id, String title, UUID tenantId, Long creatorId) {
        this.id = id;
        this.title = title;
        this.tenantId = tenantId;
        this.creatorId = creatorId;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public Long getCreatorId() {
        return creatorId;
    }
}
