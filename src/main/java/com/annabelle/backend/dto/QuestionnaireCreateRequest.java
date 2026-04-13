package com.annabelle.backend.dto;

public record QuestionnaireCreateRequest(
        String title,
        String definitionJson
) {
}
