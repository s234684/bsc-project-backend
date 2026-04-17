package com.annabelle.backend.dto;

import java.util.UUID;

public record SubmissionResponse(
        Long submissionId,
        Long questionnaireId,
        Long userId,
        UUID tenantId,
        String answerJson
) {
}
