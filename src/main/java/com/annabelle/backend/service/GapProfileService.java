package com.annabelle.backend.service;

import com.annabelle.backend.dto.GapProfileResponse;
import com.annabelle.backend.exception.ApiException;
import com.annabelle.backend.model.GapCategory;
import com.annabelle.backend.model.GapProfile;
import com.annabelle.backend.model.Questionnaire;
import com.annabelle.backend.model.RoleName;
import com.annabelle.backend.model.Submission;
import com.annabelle.backend.repository.GapProfileRepository;
import com.annabelle.backend.repository.QuestionnaireRepository;
import com.annabelle.backend.security.AuthorizationService;
import com.annabelle.backend.security.CurrentUser;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class GapProfileService {
    private static final double TARGET_LEVEL = 5.0;

    private final GapProfileRepository gapProfileRepository;
    private final QuestionnaireRepository questionnaireRepository;
    private final AuthorizationService authorizationService;

    public GapProfileService(
            GapProfileRepository gapProfileRepository,
            QuestionnaireRepository questionnaireRepository,
            AuthorizationService authorizationService
    ) {
        this.gapProfileRepository = gapProfileRepository;
        this.questionnaireRepository = questionnaireRepository;
        this.authorizationService = authorizationService;
    }

    public GapProfile createForSubmission(Submission submission) {
        double observedLevel = ThreadLocalRandom.current().nextDouble(1.0, 5.0);
        double gapValue = TARGET_LEVEL - observedLevel;
        GapCategory gapCategory = categorize(gapValue);

        GapProfile gapProfile = new GapProfile(
                submission.getTenant(),
                submission.getQuestionnaire(),
                submission.getUser(),
                submission,
                observedLevel,
                TARGET_LEVEL,
                gapValue,
                gapCategory,
                Instant.now()
        );

        return gapProfileRepository.save(gapProfile);
    }

    public List<GapProfileResponse> listQuestionnaireGapProfiles(Long questionnaireId) {
        authorizationService.requireAuthenticated();
        CurrentUser currentUser = authorizationService.currentUser();

        boolean canViewTenantProfiles = currentUser.hasRole(RoleName.MANAGER)
                || currentUser.hasRole(RoleName.INSTRUCTOR);

        if (!canViewTenantProfiles) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Access denied");
        }

        Questionnaire questionnaire = questionnaireRepository.findById(questionnaireId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Questionnaire not found"));

        authorizationService.requireTenant(questionnaire.getTenant().getId());

        return gapProfileRepository.findAllByQuestionnaire_IdAndTenant_Id(
                        questionnaireId,
                        currentUser.getTenantId()
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public GapProfileResponse getOwnGapProfile(Long questionnaireId) {
        authorizationService.requireAuthenticated();
        authorizationService.requireRole(RoleName.PARTICIPANT);

        CurrentUser currentUser = authorizationService.currentUser();

        Questionnaire questionnaire = questionnaireRepository.findById(questionnaireId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Questionnaire not found"));

        authorizationService.requireTenant(questionnaire.getTenant().getId());

        GapProfile gapProfile = gapProfileRepository.findByQuestionnaire_IdAndParticipant_IdAndTenant_Id(
                        questionnaireId,
                        currentUser.getUserId(),
                        currentUser.getTenantId()
                )
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Gap profile not found"));

        return toResponse(gapProfile);
    }

    public GapProfileResponse getGapProfile(Long gapProfileId) {
        authorizationService.requireAuthenticated();
        CurrentUser currentUser = authorizationService.currentUser();

        GapProfile gapProfile = gapProfileRepository.findByIdAndTenant_Id(
                        gapProfileId,
                        currentUser.getTenantId()
                )
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Gap profile not found"));

        boolean isOwner = gapProfile.getParticipant().getId().equals(currentUser.getUserId());
        boolean canViewTenantProfiles = currentUser.hasRole(RoleName.MANAGER)
                || currentUser.hasRole(RoleName.INSTRUCTOR);

        if (!isOwner && !canViewTenantProfiles) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Access denied");
        }

        return toResponse(gapProfile);
    }

    private GapCategory categorize(double gapValue) {
        if (gapValue <= 0) {
            return GapCategory.LOW;
        }

        if (gapValue <= 1.5) {
            return GapCategory.MEDIUM;
        }

        return GapCategory.HIGH;
    }

    private GapProfileResponse toResponse(GapProfile gapProfile) {
        return new GapProfileResponse(
                gapProfile.getId(),
                gapProfile.getParticipant().getId(),
                gapProfile.getQuestionnaire().getId(),
                gapProfile.getTenant().getId(),
                gapProfile.getObservedLevel(),
                gapProfile.getTargetLevel(),
                gapProfile.getGapValue(),
                gapProfile.getGapCategory(),
                gapProfile.getCreatedAt()
        );
    }
}
