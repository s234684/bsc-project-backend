package com.annabelle.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record QuestionnaireCreateRequest(
        @NotBlank
        @Size(max = 200)
        String title,
        @NotBlank
        String definitionJson
) {
}
