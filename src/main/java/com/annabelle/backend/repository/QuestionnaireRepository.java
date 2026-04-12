package com.annabelle.backend.repository;

import com.annabelle.backend.model.Questionnaire;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface QuestionnaireRepository extends JpaRepository<Questionnaire, Long> {
    List<Questionnaire> findAllByTenant_Id(UUID tenantId);
}
