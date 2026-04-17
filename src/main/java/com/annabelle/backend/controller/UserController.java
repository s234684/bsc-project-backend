package com.annabelle.backend.controller;

import com.annabelle.backend.dto.SubmissionResponse;
import com.annabelle.backend.service.SubmissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/me")
public class UserController {
    private final SubmissionService submissionService;

    public UserController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @GetMapping( "/submissions")
    public ResponseEntity<List<SubmissionResponse>> list() {
        return ResponseEntity.ok(submissionService.listUserSubmissions());
    }
}