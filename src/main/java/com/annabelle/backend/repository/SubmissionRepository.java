package com.annabelle.backend.repository;

import com.annabelle.backend.model.Submission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findAllByUser_Id(Long userId);
    List<Submission> findAllByQuestionnaire_Id(Long questionnaireId);

}
