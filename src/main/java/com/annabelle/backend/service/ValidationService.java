package com.annabelle.backend.service;

import com.annabelle.backend.repository.QuestionnaireRepository;
import com.annabelle.backend.repository.SubmissionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class ValidationService {
    private static final int MAX_ANSWER_LENGTH = 255;
    private static final int MAX_QUESTION_LENGTH = 500;
    private static final int MAX_TITLE_LENGTH = 200;

    private final QuestionnaireRepository questionnaireRepository;
    private final SubmissionRepository submissionRepository;
    private final ObjectMapper objectMapper;

    public ValidationService(QuestionnaireRepository questionnaireRepository, SubmissionRepository submissionRepository, ObjectMapper objectMapper) {
        this.questionnaireRepository = questionnaireRepository;
        this.submissionRepository = submissionRepository;
        this.objectMapper = objectMapper;
    }

    public JsonNode parseIntoRoot(String jsonString) {
        if (jsonString == null || jsonString.isBlank()) {
            throw new IllegalStateException("JSON must not be empty");
        }

        try {
            return objectMapper.readTree(jsonString);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Invalid JSON", e);
        }
    }

    public void checkFirstSubmission(Long questionnaireId, Long userId) {
        if (submissionRepository.findByQuestionnaire_IdAndUser_Id(questionnaireId, userId) != null){
            throw new IllegalStateException("User has already submitted");
        }

    }
    public void validateSubmission(Long questionnaireId, String submissionJson) {
        String questionnaireJson = questionnaireRepository.findDefinitionJsonById(questionnaireId);
        if (questionnaireJson == null || questionnaireJson.isBlank()) {
            throw new IllegalStateException("Questionnaire not found or missing definition JSON");
        }

        JsonNode submissionRoot = parseIntoRoot(submissionJson);
        JsonNode answers = submissionRoot.get("answers");
        if (answers == null || !answers.isArray()) {
            throw new IllegalStateException("answers must be an array");
        }
        if (answers.isEmpty()) {
            throw new IllegalStateException("answers must not be empty");
        }

        JsonNode questionnaireRoot = parseIntoRoot(questionnaireJson);
        JsonNode questions = questionnaireRoot.get("questions");
        if (questions == null || !questions.isArray()) {
            throw new IllegalStateException("questions must be an array");
        }
        if (questions.isEmpty()) {
            throw new IllegalStateException("questions must not be empty");
        }

        Set<String> answerKeys = extractKeys(answers, "answer");
        Set<String> questionKeys = extractKeys(questions, "question");

        if (!answerKeys.equals(questionKeys)) {
            throw new IllegalStateException("answer keys do not match question keys");
        }

        for (JsonNode answer : answers) {
            if (answer == null || !answer.isObject()) {
                throw new IllegalStateException("each answer must be an object");
            }

            JsonNode value = answer.get("value");
            if (value == null || value.isNull()) {
                throw new IllegalStateException("answer value is null");
            }
            if (!value.isTextual()) {
                throw new IllegalStateException("answer value must be a string");
            }

            String answerText = value.asText();
            if (answerText.isBlank()) {
                throw new IllegalStateException("answer value cannot be blank");
            }
            if (answerText.length() > MAX_ANSWER_LENGTH) {
                throw new IllegalStateException("answer value cannot be longer than " + MAX_ANSWER_LENGTH + " characters");
            }
        }
    }

    public void validateDefinitionJson(String definitionJson) {
        if (definitionJson == null || definitionJson.isBlank()) {
            throw new IllegalStateException("Questionnaire definition JSON must not be empty");
        }

        JsonNode root = parseIntoRoot(definitionJson);

        JsonNode questions = root.get("questions");
        if (questions == null || !questions.isArray() || questions.isEmpty()) {
            throw new IllegalStateException("Questionnaire definition must contain a non-empty questions array");
        }

        Set<String> questionKeys = new HashSet<>();

        for (JsonNode question : questions) {
            if (question == null || !question.isObject()) {
                throw new IllegalStateException("each question must be an object");
            }

            JsonNode key = question.get("key");
            JsonNode text = question.get("text");

            if (key == null || key.isNull()) {
                throw new IllegalStateException("Each question must have a non-empty key");
            }
            if (!key.isTextual() || key.asText().isBlank()) {
                throw new IllegalStateException("Each question must have a non-empty key");
            }

            String keyText = key.asText();
            if (!questionKeys.add(keyText)) {
                throw new IllegalStateException("duplicate question key: " + keyText);
            }

            if (text == null || text.isNull() || !text.isTextual() || text.asText().isBlank()) {
                throw new IllegalStateException("Each question must have a non-empty text");
            }

            if (text.asText().length() > MAX_QUESTION_LENGTH) {
                throw new IllegalStateException("question text cannot be longer than " + MAX_QUESTION_LENGTH + " characters");
            }
        }
    }

    public void validateQuestionnaireTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalStateException("Questionnaire title must not be empty");
        }
        if (title.length() > MAX_TITLE_LENGTH) {
            throw new IllegalStateException("Questionnaire title cannot be longer than " + MAX_TITLE_LENGTH + " characters");
        }
    }

    private Set<String> extractKeys(JsonNode items, String itemType) {
        Set<String> keys = new HashSet<>();

        for (JsonNode item : items) {
            if (item == null || !item.isObject()) {
                throw new IllegalStateException("each " + itemType + " must be an object");
            }

            JsonNode key = item.get("key");
            if (key == null || key.isNull()) {
                throw new IllegalStateException(itemType + " key is null");
            }
            if (!key.isTextual()) {
                throw new IllegalStateException(itemType + " key must be a string");
            }

            String keyText = key.asText();
            if (keyText.isBlank()) {
                throw new IllegalStateException(itemType + " key cannot be blank");
            }

            if (!keys.add(keyText)) {
                throw new IllegalStateException("duplicate " + itemType + " key: " + keyText);
            }
        }

        return keys;
    }
}
