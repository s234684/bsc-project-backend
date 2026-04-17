package com.annabelle.backend.controller;

import com.annabelle.backend.dto.QuestionnaireCreateRequest;
import com.annabelle.backend.dto.QuestionnaireResponse;
import com.annabelle.backend.dto.SubmissionRequest;
import com.annabelle.backend.dto.SubmissionResponse;
import com.annabelle.backend.service.QuestionnaireService;
import com.annabelle.backend.service.SubmissionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/questionnaire/{questionnaireId}/submission")
public class SubmissionController {
    private final SubmissionService submissionService;

    public SubmissionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @PostMapping
    public ResponseEntity<SubmissionResponse> create(
            @PathVariable Long questionnaireId,
            @RequestBody SubmissionRequest request
    ) {
        SubmissionResponse created = submissionService.createSubmission(questionnaireId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<SubmissionResponse>> list(@PathVariable Long questionnaireId) {
        return ResponseEntity.ok(submissionService.listQuestionnaireSubmissions(questionnaireId));
    }

}
