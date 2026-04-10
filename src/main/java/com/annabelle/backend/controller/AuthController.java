package com.annabelle.backend.controller;

import com.annabelle.backend.security.AuthorizationService;
import com.annabelle.backend.security.CurrentUser;
import org.springframework.http.HttpStatus;
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

    // Optional: if you prefer a 401 instead of a generic 500 from IllegalStateException,
    // you can keep this here or replace it with a global exception handler later.
    @GetMapping("/me-safe")
    public ResponseEntity<?> meSafe() {
        try {
            authorizationService.requireAuthenticated();
            return ResponseEntity.ok(authorizationService.currentUser());
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
        }
    }
}
