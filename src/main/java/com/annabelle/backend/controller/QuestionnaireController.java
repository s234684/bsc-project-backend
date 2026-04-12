package com.annabelle.backend.controller;

import com.annabelle.backend.model.Questionnaire;
import com.annabelle.backend.service.QuestionnaireService;
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
    public ResponseEntity<List<Questionnaire>> list() {
        return ResponseEntity.ok(questionnaireService.getAllQuestionnairesForCurrentTenant());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Questionnaire> byId(@PathVariable Long id) {
        return ResponseEntity.ok(questionnaireService.getQuestionnaireById(id));
    }

    @PostMapping
    public ResponseEntity<Questionnaire> create(@RequestBody CreateQuestionnaireRequest request) {
        Questionnaire created = questionnaireService.createQuestionnaire(request.title());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    public record CreateQuestionnaireRequest(String title) {
    }
}
