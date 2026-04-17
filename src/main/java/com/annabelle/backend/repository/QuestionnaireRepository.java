package com.annabelle.backend.repository;

import com.annabelle.backend.model.Questionnaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuestionnaireRepository extends JpaRepository<Questionnaire, Long> {
    List<Questionnaire> findAllByTenant_Id(UUID tenantId);
    Optional<Questionnaire> findByTitle(String title);

    @Query("select q.definitionJson from Questionnaire q where q.id = :questionnaireId")
    String findDefinitionJsonById(@Param("questionnaireId") Long questionnaireId);
}
