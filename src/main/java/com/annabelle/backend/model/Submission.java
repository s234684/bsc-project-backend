package com.annabelle.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "responses")
public class Submission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne
    @JoinColumn(name = "questionnaire_id", nullable = false)
    private Questionnaire questionnaire;

    // TODO: change this to one to one ? in the database or only one response per questionnaire
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String answer;

    public Submission() {
    }

    public Submission(Tenant tenant, Questionnaire questionnaire, User user, String answer) {
        this.tenant = tenant;
        this.questionnaire = questionnaire;
        this.user = user;
        this.answer = answer;
    }
}
