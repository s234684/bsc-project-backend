package com.annabelle.backend.dto;

import java.util.UUID;

public class SubmissionResponse {
    private final Long submissionId;
    private final Long questionnaireId;
    private final UUID tenantId;
    private final Long userId;
    private final String answerJson;

    public SubmissionResponse(Long submissionId, Long questionnaireId, Long userId,  UUID tenantId, String answer) {
        this.submissionId = submissionId;
        this.questionnaireId = questionnaireId;
        this.tenantId = tenantId;
        this.userId = userId;
        this.answerJson = answer;
    }
}
