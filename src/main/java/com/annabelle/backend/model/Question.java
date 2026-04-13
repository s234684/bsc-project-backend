package com.annabelle.backend.model;

import jakarta.persistence.*;

@Entity
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Questionnaire questionnaire;

    private String text;
    //private String type; add later
    private int orderIndex;

    public Question() {}

    public Question(Questionnaire questionnaire, String text, int orderIndex) {
        this.questionnaire = questionnaire;
        this.text = text;
        this.orderIndex = orderIndex;
    }

}
