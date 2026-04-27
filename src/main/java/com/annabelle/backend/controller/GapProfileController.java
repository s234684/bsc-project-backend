package com.annabelle.backend.controller;

import com.annabelle.backend.dto.GapProfileResponse;
import com.annabelle.backend.service.GapProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/questionnaire/{questionnaireId}/gapprofiles")
public class GapProfileController {
    private final GapProfileService gapProfileService;

    public GapProfileController(GapProfileService gapProfileService) {
        this.gapProfileService = gapProfileService;
    }

    @GetMapping
    public ResponseEntity<List<GapProfileResponse>> list(@PathVariable Long questionnaireId) {
        return ResponseEntity.ok(gapProfileService.listQuestionnaireGapProfiles(questionnaireId));
    }

    @GetMapping("/me")
    public ResponseEntity<GapProfileResponse> own(@PathVariable Long questionnaireId) {
        return ResponseEntity.ok(gapProfileService.getOwnGapProfile(questionnaireId));
    }

    @GetMapping("/{gapProfileId}")
    public ResponseEntity<GapProfileResponse> byId(
            @PathVariable Long questionnaireId,
            @PathVariable Long gapProfileId
    ) {
        return ResponseEntity.ok(gapProfileService.getGapProfile(gapProfileId));
    }
}
