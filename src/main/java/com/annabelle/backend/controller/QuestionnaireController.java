package com.annabelle.backend.controller;

import com.annabelle.backend.dto.QuestionnaireCreateRequest;
import com.annabelle.backend.dto.QuestionnaireResponse;
import com.annabelle.backend.service.QuestionnaireService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/questionnaire")
public class QuestionnaireController {

    private final QuestionnaireService questionnaireService;

    public QuestionnaireController(QuestionnaireService questionnaireService) {
        this.questionnaireService = questionnaireService;
    }

    @GetMapping
    public ResponseEntity<List<QuestionnaireResponse>> list() {
        return ResponseEntity.ok(questionnaireService.getAllQuestionnairesForCurrentTenant());
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuestionnaireResponse> byId(@PathVariable Long id) {
        return ResponseEntity.ok(questionnaireService.getQuestionnaireById(id));
    }

    @PostMapping
    public ResponseEntity<QuestionnaireResponse> create(@Valid @RequestBody QuestionnaireCreateRequest request) {
        QuestionnaireResponse created = questionnaireService.createQuestionnaire(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
