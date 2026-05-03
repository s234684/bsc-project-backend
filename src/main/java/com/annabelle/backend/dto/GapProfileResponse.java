package com.annabelle.backend.dto;

import com.annabelle.backend.model.GapCategory;

import java.time.Instant;
import java.util.UUID;

public record GapProfileResponse(
        Long id,
        Long participantId,
        String participantEmail,
        Long questionnaireId,
        UUID tenantId,
        double observedLevel,
        double targetLevel,
        double gapValue,
        GapCategory gapCategory,
        Instant createdAt
) {
}
