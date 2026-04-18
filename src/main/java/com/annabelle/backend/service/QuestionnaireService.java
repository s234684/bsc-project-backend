package com.annabelle.backend.service;

import com.annabelle.backend.dto.QuestionnaireCreateRequest;
import com.annabelle.backend.dto.QuestionnaireResponse;
import com.annabelle.backend.exception.ApiException;
import com.annabelle.backend.model.Questionnaire;
import com.annabelle.backend.model.RoleName;
import com.annabelle.backend.model.Tenant;
import com.annabelle.backend.model.User;
import com.annabelle.backend.repository.QuestionnaireRepository;
import com.annabelle.backend.repository.TenantRepository;
import com.annabelle.backend.repository.UserRepository;
import com.annabelle.backend.security.AuthorizationService;
import com.annabelle.backend.security.CurrentUser;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestionnaireService {

    private final QuestionnaireRepository questionnaireRepository;
    private final AuthorizationService authorizationService;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final ValidationService validationService;

    public QuestionnaireService(
            QuestionnaireRepository questionnaireRepository,
            AuthorizationService authorizationService,
            TenantRepository tenantRepository,
            UserRepository userRepository,
           ValidationService validationService
    ) {
        this.questionnaireRepository = questionnaireRepository;
        this.authorizationService = authorizationService;
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
        this.validationService = validationService;
    }

    public QuestionnaireResponse createQuestionnaire(QuestionnaireCreateRequest request) {
        authorizationService.requireAuthenticated();
        authorizationService.requireRole(RoleName.INSTRUCTOR);

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
        return toResponse(saved);
    }

    public List<QuestionnaireResponse> getAllQuestionnairesForCurrentTenant() {
        authorizationService.requireAuthenticated();
        CurrentUser currentUser = authorizationService.currentUser();

        return questionnaireRepository.findAllByTenant_Id(currentUser.getTenantId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public QuestionnaireResponse getQuestionnaireById(Long questionnaireId) {
        authorizationService.requireAuthenticated();

        Questionnaire questionnaire = questionnaireRepository.findById(questionnaireId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Questionnaire not found"));

        authorizationService.requireTenant(questionnaire.getTenant().getId());
        return toResponse(questionnaire);
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
}
