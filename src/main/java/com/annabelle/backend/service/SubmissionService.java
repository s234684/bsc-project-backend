package com.annabelle.backend.service;

import com.annabelle.backend.dto.SubmissionRequest;
import com.annabelle.backend.dto.SubmissionResponse;
import com.annabelle.backend.model.Questionnaire;
import com.annabelle.backend.model.RoleName;
import com.annabelle.backend.model.Submission;
import com.annabelle.backend.model.Tenant;
import com.annabelle.backend.model.User;
import com.annabelle.backend.repository.QuestionnaireRepository;
import com.annabelle.backend.repository.SubmissionRepository;
import com.annabelle.backend.repository.TenantRepository;
import com.annabelle.backend.repository.UserRepository;
import com.annabelle.backend.security.AuthorizationService;
import com.annabelle.backend.security.CurrentUser;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubmissionService {

    private final AuthorizationService authorizationService;
    private final TenantRepository tenantRepository;
    private final QuestionnaireRepository questionnaireRepository;
    private final UserRepository userRepository;
    private final SubmissionRepository submissionRepository;
    private final ValidationService validationService;

    public SubmissionService(
            AuthorizationService authorizationService,
            TenantRepository tenantRepository,
            QuestionnaireRepository questionnaireRepository,
            UserRepository userRepository,
            SubmissionRepository submissionRepository,
            ValidationService validationService
    ) {
        this.authorizationService = authorizationService;
        this.tenantRepository = tenantRepository;
        this.questionnaireRepository = questionnaireRepository;
        this.userRepository = userRepository;
        this.submissionRepository = submissionRepository;
        this.validationService = validationService;
    }

    public SubmissionResponse createSubmission(Long questionnaireId, SubmissionRequest submissionRequest) {
        authorizationService.requireAuthenticated();

        CurrentUser currentUser = authorizationService.currentUser();

        Questionnaire questionnaire = questionnaireRepository.findById(questionnaireId)
                .orElseThrow(() -> new IllegalStateException("Questionnaire not found"));

        authorizationService.requireTenant(questionnaire.getTenant().getId());

        validationService.validateSubmission(questionnaireId, submissionRequest.answerJson());

        Tenant tenant = tenantRepository.findById(currentUser.getTenantId())
                .orElseThrow(() -> new IllegalStateException("Tenant not found"));

        User user = userRepository.findById(currentUser.getUserId())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        Submission submission = new Submission(
                tenant,
                questionnaire,
                user,
                submissionRequest.answerJson()
        );

        Submission saved = submissionRepository.save(submission);
        return toResponse(saved);
    }

    public List<SubmissionResponse> listUserSubmissions() {
        authorizationService.requireAuthenticated();
        CurrentUser currentUser = authorizationService.currentUser();

        return submissionRepository.findAllByUser_Id(currentUser.getUserId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<SubmissionResponse> listQuestionnaireSubmissions(Long questionnaireId) {
        authorizationService.requireAuthenticated();
        authorizationService.requireRole(RoleName.MANAGER);

        Questionnaire questionnaire = questionnaireRepository.findById(questionnaireId)
                .orElseThrow(() -> new IllegalStateException("Questionnaire not found"));

        authorizationService.requireTenant(questionnaire.getTenant().getId());

        return submissionRepository.findAllByQuestionnaire_Id(questionnaireId)
                .stream()
                .map(this::toResponse)
                .toList();
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
}
