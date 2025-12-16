package com.promptquest.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.promptquest.entity.Question;
import com.promptquest.repository.QuestionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for importing questions from JSON file
 * Converts the PromptQuest JSON format to Question entities
 */
@Service
public class JsonImportService {

    private static final Logger logger = LoggerFactory.getLogger(JsonImportService.class);

    @Autowired(required = false)
    private QuestionRepository questionRepository;

    @Autowired(required = false)
    private BigQueryQuestionService bigQueryQuestionService;

    @Autowired
    private ResourceLoader resourceLoader;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Import questions from JSON file
     * @param jsonFilePath path to the JSON file (e.g., "classpath:data/questions.json" or "file:input/questions.json")
     * @return number of questions imported
     */
    @Transactional
    public int importQuestionsFromJson(String jsonFilePath) {
        try {
            logger.info("Starting import from: {}", jsonFilePath);

            Resource resource = resourceLoader.getResource(jsonFilePath);
            if (!resource.exists()) {
                throw new RuntimeException("File not found: " + jsonFilePath);
            }

            JsonNode rootNode = objectMapper.readTree(resource.getInputStream());
            JsonNode questionsNode = rootNode.get("questions");

            if (questionsNode == null || !questionsNode.isArray()) {
                throw new RuntimeException("Invalid JSON format: 'questions' array not found");
            }

            int importedCount = 0;

            for (JsonNode questionNode : questionsNode) {
                Question question = convertJsonToQuestion(questionNode);
                
                // Use the appropriate service based on what's available
                if (bigQueryQuestionService != null) {
                    bigQueryQuestionService.saveQuestion(question);
                } else if (questionRepository != null) {
                    questionRepository.save(question);
                } else {
                    throw new RuntimeException("No data storage service available");
                }
                
                importedCount++;
                logger.debug("Imported question {}: {}", importedCount, question.getQuestion().substring(0, Math.min(50, question.getQuestion().length())));
            }

            logger.info("Successfully imported {} questions", importedCount);
            return importedCount;

        } catch (IOException e) {
            logger.error("Error reading JSON file: {}", e.getMessage());
            throw new RuntimeException("Failed to read JSON file: " + jsonFilePath, e);
        } catch (Exception e) {
            logger.error("Error importing questions: {}", e.getMessage());
            throw new RuntimeException("Failed to import questions", e);
        }
    }

    /**
     * Convert JSON node to Question entity
     */
    private Question convertJsonToQuestion(JsonNode questionNode) {
        Question question = new Question();

        // Basic question data
        question.setQuestion(questionNode.get("question").asText());
        question.setCorrectAnswer(questionNode.get("answer").asText());
        question.setExplanation(questionNode.get("explanation").asText());
        question.setDifficulty(questionNode.get("difficulty").asInt());
        question.setArea(questionNode.get("area").asText());
        question.setSkill(questionNode.get("skill").asText());
        question.setDegree(questionNode.get("degree").asText());

        // Parse options
        Map<String, String> options = new HashMap<>();
        JsonNode optionsNode = questionNode.get("options");

        if (optionsNode != null && optionsNode.isArray()) {
            for (JsonNode option : optionsNode) {
                String key = option.get("key").asText();
                String text = option.get("text").asText();
                options.put(key, text);
            }
        }

        // Set options (with defaults if missing)
        question.setOptionA(options.getOrDefault("A", ""));
        question.setOptionB(options.getOrDefault("B", ""));
        question.setOptionC(options.getOrDefault("C", ""));
        question.setOptionD(options.getOrDefault("D", ""));

        return question;
    }

    /**
     * Import from default location (input/promptquest-questions-test.json)
     */
    public int importDefaultQuestions() {
        return importQuestionsFromJson("file:input/promptquest-questions-test.json");
    }

    /**
     * Clear all existing questions
     */
    @Transactional
    public void clearAllQuestions() {
        logger.info("Clearing all existing questions");
        if (bigQueryQuestionService != null) {
            bigQueryQuestionService.deleteAllQuestions();
        } else if (questionRepository != null) {
            questionRepository.deleteAll();
        } else {
            throw new RuntimeException("No data storage service available");
        }
        logger.info("All questions cleared");
    }

    /**
     * Import with option to clear existing data first
     */
    @Transactional
    public int importQuestionsFromJson(String jsonFilePath, boolean clearExisting) {
        if (clearExisting) {
            clearAllQuestions();
        }
        return importQuestionsFromJson(jsonFilePath);
    }

    /**
     * Get import statistics
     */
    public Map<String, Object> getImportStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        if (bigQueryQuestionService != null) {
            // BigQuery stats
            stats.put("totalQuestions", bigQueryQuestionService.countQuestions());
            stats.put("skills", bigQueryQuestionService.getDistinctSkills());
            stats.put("areas", bigQueryQuestionService.getDistinctAreas());
            stats.put("degrees", bigQueryQuestionService.getDistinctDegrees());
            stats.put("difficultyDistribution", bigQueryQuestionService.getDifficultyDistribution());
        } else if (questionRepository != null) {
            // SQLite stats
            stats.put("totalQuestions", questionRepository.count());
            stats.put("skills", questionRepository.findAllSkills());
            stats.put("areas", questionRepository.findAllAreas());
            stats.put("degrees", questionRepository.findAllDegrees());

            // Count by difficulty
            Map<Integer, Long> difficultyCount = new HashMap<>();
            for (int i = 1; i <= 5; i++) {
                difficultyCount.put(i, questionRepository.countByDifficulty(i));
            }
            stats.put("difficultyDistribution", difficultyCount);
        } else {
            throw new RuntimeException("No data storage service available");
        }

        return stats;
    }
}