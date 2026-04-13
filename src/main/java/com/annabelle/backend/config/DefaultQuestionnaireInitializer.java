package com.annabelle.backend.config;

import com.annabelle.backend.model.Questionnaire;
import com.annabelle.backend.model.Tenant;
import com.annabelle.backend.repository.QuestionnaireRepository;
import com.annabelle.backend.repository.TenantRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Component
public class DefaultQuestionnaireInitializer implements ApplicationRunner {
    private static final String DEFAULT_TITLE = "Default Security Questionnaire";

    private final QuestionnaireRepository questionnaireRepository;
    private final TenantRepository tenantRepository;
    private final ObjectMapper objectMapper;

    public DefaultQuestionnaireInitializer(
            QuestionnaireRepository questionnaireRepository,
            TenantRepository tenantRepository,
            ObjectMapper objectMapper
    ) {
        this.questionnaireRepository = questionnaireRepository;
        this.tenantRepository = tenantRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        ClassPathResource resource = new ClassPathResource("questionnaire/default-questionnaire.json");
        try (InputStream inputStream = resource.getInputStream()) {
            String json = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

            objectMapper.readTree(json);

            Tenant tenant = tenantRepository.findAll().stream()
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No tenant exists to attach default questionnaire"));

            Questionnaire questionnaire = questionnaireRepository.findByTitle(DEFAULT_TITLE)
                    .orElseGet(Questionnaire::new);
            questionnaire.setTenant(tenant);
            questionnaire.setTitle(DEFAULT_TITLE);
            questionnaire.setDefinitionJson(json);

            questionnaireRepository.save(questionnaire);
        }
    }
}
