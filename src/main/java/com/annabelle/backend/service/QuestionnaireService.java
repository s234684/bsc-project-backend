package com.annabelle.backend.service;

import com.annabelle.backend.model.Questionnaire;
import com.annabelle.backend.model.RoleName;
import com.annabelle.backend.model.Tenant;
import com.annabelle.backend.model.User;
import com.annabelle.backend.repository.QuestionnaireRepository;
import com.annabelle.backend.repository.TenantRepository;
import com.annabelle.backend.repository.UserRepository;
import com.annabelle.backend.security.AuthorizationService;
import com.annabelle.backend.security.CurrentUser;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestionnaireService {

    private final QuestionnaireRepository questionnaireRepository;
    private final AuthorizationService authorizationService;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;

    public QuestionnaireService(
            QuestionnaireRepository questionnaireRepository,
            AuthorizationService authorizationService,
            TenantRepository tenantRepository,
            UserRepository userRepository
    ) {
        this.questionnaireRepository = questionnaireRepository;
        this.authorizationService = authorizationService;
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
    }

    public Questionnaire createQuestionnaire(String questionnaireTitle) {
        authorizationService.requireAuthenticated();
        authorizationService.requireRole(RoleName.INSTRUCTOR);

        CurrentUser currentUser = authorizationService.currentUser();

        Tenant tenant = tenantRepository.findById(currentUser.getTenantId())
                .orElseThrow(() -> new IllegalStateException("Tenant not found"));

        User creator = userRepository.findById(currentUser.getUserId())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        Questionnaire questionnaire = new Questionnaire(tenant, questionnaireTitle, creator);
        return questionnaireRepository.save(questionnaire);
    }

    public List<Questionnaire> getAllQuestionnairesForCurrentTenant() {
        authorizationService.requireAuthenticated();
        CurrentUser currentUser = authorizationService.currentUser();
        return questionnaireRepository.findAllByTenant_Id(currentUser.getTenantId());
    }

    public Questionnaire getQuestionnaireById(Long questionnaireId) {
        authorizationService.requireAuthenticated();

        Questionnaire questionnaire = questionnaireRepository.findById(questionnaireId)
                .orElseThrow(() -> new IllegalStateException("Questionnaire not found"));

        authorizationService.requireTenant(questionnaire.getTenant().getId());
        return questionnaire;
    }
}
