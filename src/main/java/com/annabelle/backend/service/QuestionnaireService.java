package com.annabelle.backend.service;

import com.annabelle.backend.dto.QuestionnaireCreateRequest;
import com.annabelle.backend.dto.QuestionnaireResponse;
import com.annabelle.backend.model.Questionnaire;
import com.annabelle.backend.model.RoleName;
import com.annabelle.backend.model.Tenant;
import com.annabelle.backend.model.User;
import com.annabelle.backend.repository.QuestionnaireRepository;
import com.annabelle.backend.repository.TenantRepository;
import com.annabelle.backend.repository.UserRepository;
import com.annabelle.backend.security.AuthorizationService;
import com.annabelle.backend.security.CurrentUser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestionnaireService {

    private final QuestionnaireRepository questionnaireRepository;
    private final AuthorizationService authorizationService;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public QuestionnaireService(
            QuestionnaireRepository questionnaireRepository,
            AuthorizationService authorizationService,
            TenantRepository tenantRepository,
            UserRepository userRepository,
            ObjectMapper objectMapper
    ) {
        this.questionnaireRepository = questionnaireRepository;
        this.authorizationService = authorizationService;
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    public QuestionnaireResponse createQuestionnaire(QuestionnaireCreateRequest request) {
        authorizationService.requireAuthenticated();
        authorizationService.requireRole(RoleName.INSTRUCTOR);

        validateDefinitionJson(request.definitionJson());

        CurrentUser currentUser = authorizationService.currentUser();

        Tenant tenant = tenantRepository.findById(currentUser.getTenantId())
                .orElseThrow(() -> new IllegalStateException("Tenant not found"));

        User creator = userRepository.findById(currentUser.getUserId())
                .orElseThrow(() -> new IllegalStateException("User not found"));

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
                .orElseThrow(() -> new IllegalStateException("Questionnaire not found"));

        authorizationService.requireTenant(questionnaire.getTenant().getId());
        return toResponse(questionnaire);
    }

    private void validateDefinitionJson(String definitionJson) {
        if (definitionJson == null || definitionJson.isBlank()) {
            throw new IllegalStateException("Questionnaire definition JSON must not be empty");
        }

        try {
            JsonNode root = objectMapper.readTree(definitionJson);

            JsonNode questions = root.get("questions");
            if (questions == null || !questions.isArray() || questions.isEmpty()) {
                throw new IllegalStateException("Questionnaire definition must contain a non-empty questions array");
            }

            for (JsonNode question : questions) {
                JsonNode key = question.get("key");
                JsonNode text = question.get("text");

                if (key == null || key.asText().isBlank()) {
                    throw new IllegalStateException("Each question must have a non-empty key");
                }

                if (text == null || text.asText().isBlank()) {
                    throw new IllegalStateException("Each question must have a non-empty text");
                }
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Invalid questionnaire definition JSON", ex);
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
}
