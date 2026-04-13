package com.annabelle.backend.config;

import com.annabelle.backend.model.Questionnaire;
import com.annabelle.backend.model.Tenant;
import com.annabelle.backend.repository.QuestionnaireRepository;
import com.annabelle.backend.repository.TenantRepository;
import com.annabelle.backend.repository.UserRepository;
import com.annabelle.backend.security.AuthorizationService;
import com.annabelle.backend.service.QuestionnaireService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
public class DefaultQuestionnaireInitializer implements ApplicationRunner {

    private final QuestionnaireRepository questionnaireRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public DefaultQuestionnaireInitializer(
            QuestionnaireRepository questionnaireRepository,
            TenantRepository tenantRepository,
            UserRepository userRepository,
            ObjectMapper objectMapper
    ) {
        this.questionnaireRepository = questionnaireRepository;
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (questionnaireRepository.count() > 0) {
            return;
        }

        ClassPathResource resource = new ClassPathResource("default-questionnaire.json");
        try (InputStream inputStream = resource.getInputStream()) {
            String json = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

            objectMapper.readTree(json);

            Tenant tenant = tenantRepository.findAll().stream()
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No tenant exists to attach default questionnaire"));

            Questionnaire questionnaire = new Questionnaire();
            questionnaire.setTenant(tenant);
            questionnaire.setTitle("Default Security Questionnaire");
            questionnaire.setDefinitionJson(json);

            questionnaireRepository.save(questionnaire);
        }
    }
}
