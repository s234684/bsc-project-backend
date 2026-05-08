package com.annabelle.backend.service;

import com.annabelle.backend.dto.QuestionnaireCreateRequest;
import com.annabelle.backend.dto.QuestionnaireResponse;
import com.annabelle.backend.exception.ApiException;
import com.annabelle.backend.model.*;
import com.annabelle.backend.repository.QuestionnaireRepository;
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
public class QuestionnaireService {

    private final QuestionnaireRepository questionnaireRepository;
    private final AuthorizationService authorizationService;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final ValidationService validationService;
    private final AuditService auditService;
    private final RlsContextService rlsContextService;

    public QuestionnaireService(
            QuestionnaireRepository questionnaireRepository,
            AuthorizationService authorizationService,
            TenantRepository tenantRepository,
            UserRepository userRepository,
            ValidationService validationService,
            AuditService auditService, RlsContextService rlsContextService
    ) {
        this.questionnaireRepository = questionnaireRepository;
        this.authorizationService = authorizationService;
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
        this.validationService = validationService;
        this.auditService = auditService;
        this.rlsContextService = rlsContextService;
    }

    @Transactional
    public QuestionnaireResponse createQuestionnaire(QuestionnaireCreateRequest request) {
        try {
            authorizationService.requireAuthenticated();
            authorizationService.requireRole(RoleName.INSTRUCTOR);
            rlsContextService.setTenant();
            validationService.validateQuestionnaireTitle(request.title());
            validationService.validateDefinitionJson(request.definitionJson());

            CurrentUser currentUser = authorizationService.currentUser();

            Tenant tenant = tenantRepository.findById(currentUser.getTenantId())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Tenant not found"));

            User creator = userRepository.findById(currentUser.getUserId())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

            Questionnaire questionnaire = new Questionnaire(
                    tenant,
                    request.title(),
                    creator,
                    request.definitionJson()
            );

            Questionnaire saved = questionnaireRepository.save(questionnaire);

            auditService.logSuccess(creator, AuditAction.QUESTIONNAIRE_CREATED);

            return toResponse(saved);
        } catch (ApiException ex) {
            if (isAuthorizationFailure(ex)) {
                auditService.logDenied(
                    authorizationService.currentUserOrNull(),
                    AuditAction.QUESTIONNAIRE_CREATED,
                    ex.getMessage()
                );
            }

            throw ex;
        }
    }

    @Transactional(readOnly = true)
    public List<QuestionnaireResponse> getAllQuestionnairesForCurrentTenant() {
        authorizationService.requireAuthenticated();
        rlsContextService.setTenant();

        CurrentUser currentUser = authorizationService.currentUser();

        return questionnaireRepository.findAllByTenant_Id(currentUser.getTenantId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public QuestionnaireResponse getQuestionnaireById(Long questionnaireId) {
        try {
            authorizationService.requireAuthenticated();
            rlsContextService.setTenant();

            Questionnaire questionnaire = questionnaireRepository.findById(questionnaireId)
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Questionnaire not found"));

            authorizationService.requireTenant(questionnaire.getTenant().getId());
            return toResponse(questionnaire);
        } catch (ApiException ex) {
            if (isAuthorizationFailure(ex)) {
                auditService.logDenied(
                        authorizationService.currentUserOrNull(),
                        AuditAction.CROSS_TENANT_ACCESS_DENIED,
                        ex.getMessage()
                );
            }
            throw ex;
        }
    }

    private QuestionnaireResponse toResponse(Questionnaire questionnaire) {
        return new QuestionnaireResponse(
                questionnaire.getId(),
                questionnaire.getTitle(),
                questionnaire.getTenant().getId(),
                questionnaire.getCreator() != null ? questionnaire.getCreator().getId() : null,
                questionnaire.getDefinitionJson()
        );
    }

    private boolean isAuthorizationFailure(ApiException ex) {
        return ex.getStatus() == HttpStatus.UNAUTHORIZED || ex.getStatus() == HttpStatus.FORBIDDEN;
    }
}
