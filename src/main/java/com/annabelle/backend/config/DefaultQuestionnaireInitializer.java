package com.annabelle.backend.config;

import com.annabelle.backend.model.Questionnaire;
import com.annabelle.backend.model.Tenant;
import com.annabelle.backend.exception.ApiException;
import com.annabelle.backend.repository.QuestionnaireRepository;
import com.annabelle.backend.repository.TenantRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
public class DefaultQuestionnaireInitializer implements ApplicationRunner {
    private static final String DEFAULT_TITLE = "Default Security Questionnaire";
    private static final UUID DEFAULT_TENANT_ID = UUID.fromString("2ed04e60-2033-441b-aa15-804420c74cbf");

    private final QuestionnaireRepository questionnaireRepository;
    private final TenantRepository tenantRepository;
    private final ObjectMapper objectMapper;
    private final EntityManager entityManager;

    public DefaultQuestionnaireInitializer(
            QuestionnaireRepository questionnaireRepository,
            TenantRepository tenantRepository,
            ObjectMapper objectMapper,
            EntityManager entityManager
    ) {
        this.questionnaireRepository = questionnaireRepository;
        this.tenantRepository = tenantRepository;
        this.objectMapper = objectMapper;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        ClassPathResource resource = new ClassPathResource("questionnaire/default-questionnaire.json");
        try (InputStream inputStream = resource.getInputStream()) {
            String json = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

            objectMapper.readTree(json);

            Tenant tenant = tenantRepository.findById(DEFAULT_TENANT_ID)
                    .orElseGet(() -> tenantRepository.findAll().stream()
                            .findFirst()
                            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No tenant exists to attach default questionnaire")));

            entityManager
                    .createNativeQuery("select set_config('app.current_tenant_id', :tenantId, true)")
                    .setParameter("tenantId", tenant.getId().toString())
                    .getSingleResult();

            Questionnaire questionnaire = questionnaireRepository.findByTitle(DEFAULT_TITLE)
                    .orElseGet(Questionnaire::new);
            questionnaire.setTenant(tenant);
            questionnaire.setTitle(DEFAULT_TITLE);
            questionnaire.setDefinitionJson(json);

            questionnaireRepository.save(questionnaire);
        }
    }
}
