package com.annabelle.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record SubmissionRequest(@NotBlank String answerJson) {
}
