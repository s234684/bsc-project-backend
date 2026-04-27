package com.annabelle.backend.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "gap_profiles")
public class GapProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(optional = false)
    @JoinColumn(name = "questionnaire_id", nullable = false)
    private Questionnaire questionnaire;

    @ManyToOne(optional = false)
    @JoinColumn(name = "participant_id", nullable = false)
    private User participant;

    @OneToOne(optional = false)
    @JoinColumn(name = "submission_id", nullable = false, unique = true)
    private Submission submission;

    @Column(name = "observed_level", nullable = false)
    private double observedLevel;

    @Column(name = "target_level", nullable = false)
    private double targetLevel;

    @Column(name = "gap_value", nullable = false)
    private double gapValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "gap_category", nullable = false)
    private GapCategory gapCategory;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public GapProfile() {
    }

    public GapProfile(
            Tenant tenant,
            Questionnaire questionnaire,
            User participant,
            Submission submission,
            double observedLevel,
            double targetLevel,
            double gapValue,
            GapCategory gapCategory,
            Instant createdAt
    ) {
        this.tenant = tenant;
        this.questionnaire = questionnaire;
        this.participant = participant;
        this.submission = submission;
        this.observedLevel = observedLevel;
        this.targetLevel = targetLevel;
        this.gapValue = gapValue;
        this.gapCategory = gapCategory;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public Questionnaire getQuestionnaire() {
        return questionnaire;
    }

    public User getParticipant() {
        return participant;
    }

    public Submission getSubmission() {
        return submission;
    }

    public double getObservedLevel() {
        return observedLevel;
    }

    public double getTargetLevel() {
        return targetLevel;
    }

    public double getGapValue() {
        return gapValue;
    }

    public GapCategory getGapCategory() {
        return gapCategory;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
