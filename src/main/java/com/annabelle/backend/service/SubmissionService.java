package com.annabelle.backend.service;

import com.annabelle.backend.dto.SubmissionRequest;
import com.annabelle.backend.dto.SubmissionResponse;
import com.annabelle.backend.exception.ApiException;
import com.annabelle.backend.model.*;
import com.annabelle.backend.repository.QuestionnaireRepository;
import com.annabelle.backend.repository.SubmissionRepository;
import com.annabelle.backend.repository.TenantRepository;
import com.annabelle.backend.repository.UserRepository;
import com.annabelle.backend.security.AuthorizationService;
import com.annabelle.backend.security.CurrentUser;
import com.annabelle.backend.security.RlsContextService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SubmissionService {

    private final AuthorizationService authorizationService;
    private final TenantRepository tenantRepository;
    private final QuestionnaireRepository questionnaireRepository;
    private final UserRepository userRepository;
    private final SubmissionRepository submissionRepository;
    private final ValidationService validationService;
    private final GapProfileService gapProfileService;
    private final AuditService auditService;
    private final RlsContextService rlsContextService;

    public SubmissionService(
            AuthorizationService authorizationService,
            TenantRepository tenantRepository,
            QuestionnaireRepository questionnaireRepository,
            UserRepository userRepository,
            SubmissionRepository submissionRepository,
            ValidationService validationService,
            GapProfileService gapProfileService,
            AuditService auditService, RlsContextService rlsContextService
    ) {
        this.authorizationService = authorizationService;
        this.tenantRepository = tenantRepository;
        this.questionnaireRepository = questionnaireRepository;
        this.userRepository = userRepository;
        this.submissionRepository = submissionRepository;
        this.validationService = validationService;
        this.gapProfileService = gapProfileService;
        this.auditService = auditService;
        this.rlsContextService = rlsContextService;
    }

    @Transactional
    public SubmissionResponse createSubmission(Long questionnaireId, SubmissionRequest submissionRequest) {
        try {
            authorizationService.requireAuthenticated();
            rlsContextService.setTenant();
            CurrentUser currentUser = authorizationService.currentUser();

            Questionnaire questionnaire = questionnaireRepository.findById(questionnaireId)
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Questionnaire not found"));

            authorizationService.requireTenant(questionnaire.getTenant().getId());

            validationService.checkFirstSubmission(questionnaireId, currentUser.getUserId());
            validationService.validateSubmission(questionnaireId, submissionRequest.answerJson());

            Tenant tenant = tenantRepository.findById(currentUser.getTenantId())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Tenant not found"));

            User user = userRepository.findById(currentUser.getUserId())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

            Submission submission = new Submission(
                    tenant,
                    questionnaire,
                    user,
                    submissionRequest.answerJson()
            );

            Submission saved = submissionRepository.save(submission);

            gapProfileService.createForSubmission(saved);

            auditService.logSuccess(user, AuditAction.SUBMISSION_CREATED);

            return toResponse(saved);
        } catch (ApiException ex) {
            if (isAuthorizationFailure(ex)) {
                auditService.logDenied(
                        authorizationService.currentUserOrNull(),
                        AuditAction.SUBMISSION_CREATED,
                        ex.getMessage()
                );
            }

            throw ex;
        }
    }

    @Transactional(readOnly = true)
    public List<SubmissionResponse> listUserSubmissions() {
        authorizationService.requireAuthenticated();
        rlsContextService.setTenant();
        CurrentUser currentUser = authorizationService.currentUser();

        return submissionRepository.findAllByUser_Id(currentUser.getUserId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SubmissionResponse> listQuestionnaireSubmissions(Long questionnaireId) {
        try {
            authorizationService.requireAuthenticated();
            authorizationService.requireRole(RoleName.INSTRUCTOR);
            rlsContextService.setTenant();

            Questionnaire questionnaire = questionnaireRepository.findById(questionnaireId)
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Questionnaire not found"));

            authorizationService.requireTenant(questionnaire.getTenant().getId());

            return submissionRepository.findAllByQuestionnaire_Id(questionnaireId)
                    .stream()
                    .map(this::toResponse)
                    .toList();
        } catch (ApiException ex) {
            if (isAuthorizationFailure(ex)) {
                auditService.logDenied(
                        authorizationService.currentUserOrNull(),
                        AuditAction.AUTHORIZATION_DENIED,
                        ex.getMessage()
                );
            }
            throw ex;
        }
    }

    private SubmissionResponse toResponse(Submission submission) {
        return new SubmissionResponse(
                submission.getId(),
                submission.getQuestionnaire().getId(),
                submission.getUser().getId(),
                submission.getQuestionnaire().getTenant().getId(),
                submission.getAnswerJson()
        );
    }

    private boolean isAuthorizationFailure(ApiException ex) {
        return ex.getStatus() == HttpStatus.UNAUTHORIZED || ex.getStatus() == HttpStatus.FORBIDDEN;
    }
}
