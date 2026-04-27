package com.annabelle.backend.repository;

import com.annabelle.backend.model.GapProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GapProfileRepository extends JpaRepository<GapProfile, Long> {
    List<GapProfile> findAllByQuestionnaire_IdAndTenant_Id(Long questionnaireId, UUID tenantId);

    Optional<GapProfile> findByQuestionnaire_IdAndParticipant_IdAndTenant_Id(
            Long questionnaireId,
            Long participantId,
            UUID tenantId
    );

    Optional<GapProfile> findByIdAndTenant_Id(Long id, UUID tenantId);
}
