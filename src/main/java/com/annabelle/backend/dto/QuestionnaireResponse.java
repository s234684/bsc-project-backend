package com.annabelle.backend.dto;

import java.util.UUID;

public class QuestionnaireResponse {
    private final Long id;
    private final String title;
    private final UUID tenantId;
    private final Long creatorId;
    private final String definitionJson;

    public QuestionnaireResponse(Long id, String title, UUID tenantId, Long creatorId, String definitionJson) {
        this.id = id;
        this.title = title;
        this.tenantId = tenantId;
        this.creatorId = creatorId;
        this.definitionJson = definitionJson;
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

    public String getDefinitionJson() {
        return definitionJson;
    }
}
