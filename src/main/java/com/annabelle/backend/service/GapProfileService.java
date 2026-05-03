package com.annabelle.backend.service;

import com.annabelle.backend.dto.GapProfileResponse;
import com.annabelle.backend.exception.ApiException;
import com.annabelle.backend.model.*;
import com.annabelle.backend.repository.GapProfileRepository;
import com.annabelle.backend.repository.QuestionnaireRepository;
import com.annabelle.backend.repository.UserRepository;
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
    private final AuditService auditService;
    private final UserRepository userRepository;

    public GapProfileService(
            GapProfileRepository gapProfileRepository,
            QuestionnaireRepository questionnaireRepository,
            AuthorizationService authorizationService, AuditService auditService, UserRepository userRepository
    ) {
        this.gapProfileRepository = gapProfileRepository;
        this.questionnaireRepository = questionnaireRepository;
        this.authorizationService = authorizationService;
        this.auditService = auditService;
        this.userRepository = userRepository;
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
        try {
            authorizationService.requireAuthenticated();
            authorizationService.requireAnyRole(RoleName.MANAGER, RoleName.INSTRUCTOR);

            CurrentUser currentUser = authorizationService.currentUser();
            User user = currentUserEntity();

            Questionnaire questionnaire = questionnaireRepository.findById(questionnaireId)
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Questionnaire not found"));

            authorizationService.requireTenant(questionnaire.getTenant().getId());

            List<GapProfileResponse> responses = gapProfileRepository.findAllByQuestionnaire_IdAndTenant_Id(
                            questionnaireId,
                            currentUser.getTenantId()
                    )
                    .stream()
                    .map(this::toResponse)
                    .toList();

            auditService.logSuccess(user, AuditAction.GAP_PROFILE_VIEWED);

            return responses;
        } catch (ApiException ex) {
            if (isAuthorizationFailure(ex)) {
                auditService.logDenied(
                        authorizationService.currentUserOrNull(),
                        AuditAction.GAP_PROFILE_VIEWED,
                        ex.getMessage()
                );
            }
            throw ex;
        }
    }

    public GapProfileResponse getOwnGapProfile(Long questionnaireId) {
        try {
            authorizationService.requireAuthenticated();
            authorizationService.requireRole(RoleName.PARTICIPANT);

            CurrentUser currentUser = authorizationService.currentUser();
            User user = currentUserEntity();

            Questionnaire questionnaire = questionnaireRepository.findById(questionnaireId)
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Questionnaire not found"));

            authorizationService.requireTenant(questionnaire.getTenant().getId());

            GapProfile gapProfile = gapProfileRepository.findByQuestionnaire_IdAndParticipant_IdAndTenant_Id(
                            questionnaireId,
                            currentUser.getUserId(),
                            currentUser.getTenantId()
                    )
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Gap profile not found"));

            auditService.logSuccess(user, AuditAction.GAP_PROFILE_VIEWED);

            return toResponse(gapProfile);
        } catch (ApiException ex) {
            if (isAuthorizationFailure(ex)) {
                auditService.logDenied(
                        authorizationService.currentUserOrNull(),
                        AuditAction.GAP_PROFILE_VIEWED,
                        ex.getMessage()
                );
            }
            throw ex;
        }
    }

    public GapProfileResponse getGapProfile(Long gapProfileId) {
        try {
            authorizationService.requireAuthenticated();

            CurrentUser currentUser = authorizationService.currentUser();
            User user = currentUserEntity();

            GapProfile gapProfile = gapProfileRepository.findByIdAndTenant_Id(
                            gapProfileId,
                            currentUser.getTenantId()
                    )
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Gap profile not found"));

            authorizationService.requireOwnerOrAnyRole(
                    gapProfile.getParticipant().getId(),
                    RoleName.MANAGER,
                    RoleName.INSTRUCTOR
            );

            auditService.logSuccess(user, AuditAction.GAP_PROFILE_VIEWED);

            return toResponse(gapProfile);
        } catch (ApiException ex) {
            if (isAuthorizationFailure(ex)){
            auditService.logDenied(
                    authorizationService.currentUserOrNull(),
                    AuditAction.GAP_PROFILE_VIEWED,
                    ex.getMessage()
            );}
            throw ex;
        }
    }

    private User currentUserEntity() {
        CurrentUser currentUser = authorizationService.currentUser();

        return userRepository.findById(currentUser.getUserId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
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
                gapProfile.getParticipant().getEmail(),
                gapProfile.getQuestionnaire().getId(),
                gapProfile.getTenant().getId(),
                gapProfile.getObservedLevel(),
                gapProfile.getTargetLevel(),
                gapProfile.getGapValue(),
                gapProfile.getGapCategory(),
                gapProfile.getCreatedAt()
        );
    }

    private boolean isAuthorizationFailure(ApiException ex) {
        return ex.getStatus() == HttpStatus.UNAUTHORIZED || ex.getStatus() == HttpStatus.FORBIDDEN;
    }
}
