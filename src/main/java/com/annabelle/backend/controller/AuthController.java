package com.annabelle.backend.controller;

import com.annabelle.backend.security.AuthorizationService;
import com.annabelle.backend.security.CurrentUser;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthorizationService authorizationService;

    public AuthController(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    @GetMapping("/me")
    public ResponseEntity<CurrentUser> me() {
        authorizationService.requireAuthenticated();
        return ResponseEntity.ok(authorizationService.currentUser());
    }

    @GetMapping("/me-safe")
    public ResponseEntity<CurrentUser> meSafe() {
        authorizationService.requireAuthenticated();
        return ResponseEntity.ok(authorizationService.currentUser());
    }
}
