package com.promptquest.controller;

import com.promptquest.entity.Question;
import com.promptquest.repository.QuestionRepository;
import com.promptquest.service.BigQueryQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@Controller
@CrossOrigin(origins = "*") // Allow frontend access
public class QuizController {

    @Autowired(required = false)
    private QuestionRepository questionRepository;

    @Autowired(required = false)
    private BigQueryQuestionService bigQueryQuestionService;

    // Web interface routes
    @GetMapping("/")
    public String root() {
        return "redirect:/index.html";
    }

    // Get all questions for the quiz
    @GetMapping("/api/quiz/questions")
    @ResponseBody
    public List<Map<String, Object>> getAllQuestions() {
        List<Question> questions = getQuestions();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Question q : questions) {
            Map<String, Object> questionData = new HashMap<>();
            questionData.put("id", q.getId());
            questionData.put("question", q.getQuestion());
            questionData.put("optionA", q.getOptionA());
            questionData.put("optionB", q.getOptionB());
            questionData.put("optionC", q.getOptionC());
            questionData.put("optionD", q.getOptionD());
            questionData.put("correctAnswer", q.getCorrectAnswer());
            questionData.put("explanation", q.getExplanation());
            questionData.put("area", q.getArea());
            questionData.put("skill", q.getSkill());
            questionData.put("difficulty", q.getDifficulty());
            questionData.put("degree", q.getDegree());
            result.add(questionData);
        }
        return result;
    }

    // Check answers and get results
    @PostMapping("/api/quiz/check")
    @ResponseBody
    public Map<String, Object> checkAnswers(@RequestBody Map<String, String> answers) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> questionResults = new ArrayList<>();

        int correct = 0;
        int total = answers.size();

        // Sort question IDs to maintain order
        List<Long> sortedIds = new ArrayList<>();
        for (String questionIdStr : answers.keySet()) {
            sortedIds.add(Long.parseLong(questionIdStr));
        }
        Collections.sort(sortedIds);

        for (Long questionId : sortedIds) {
            String userAnswer = answers.get(String.valueOf(questionId));

            Question question = findQuestionById(questionId);
            if (question != null) {
                boolean isCorrect = question.getCorrectAnswer().equals(userAnswer);
                if (isCorrect) correct++;

                Map<String, Object> questionResult = new HashMap<>();
                questionResult.put("id", questionId);
                questionResult.put("question", question.getQuestion());
                questionResult.put("userAnswer", userAnswer);
                questionResult.put("correctAnswer", question.getCorrectAnswer());
                questionResult.put("isCorrect", isCorrect);
                questionResult.put("explanation", question.getExplanation());
                questionResult.put("optionA", question.getOptionA());
                questionResult.put("optionB", question.getOptionB());
                questionResult.put("optionC", question.getOptionC());
                questionResult.put("optionD", question.getOptionD());
                questionResult.put("difficulty", question.getDifficulty());
                questionResult.put("area", question.getArea());
                questionResult.put("skill", question.getSkill());
                questionResult.put("degree", question.getDegree());

                questionResults.add(questionResult);
            }
        }

        result.put("totalQuestions", total);
        result.put("correctAnswers", correct);
        result.put("score", total > 0 ? (double) correct / total * 100 : 0);
        result.put("questionResults", questionResults);

        return result;
    }

    // Get random questions (optional, for variety)
    @GetMapping("/api/quiz/random/{count}")
    @ResponseBody
    public List<Map<String, Object>> getRandomQuestions(@PathVariable int count) {
        List<Question> allQuestions = getQuestions();
        Collections.shuffle(allQuestions);

        int limit = Math.min(count, allQuestions.size());
        List<Question> randomQuestions = allQuestions.subList(0, limit);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Question q : randomQuestions) {
            Map<String, Object> questionData = new HashMap<>();
            questionData.put("id", q.getId());
            questionData.put("question", q.getQuestion());
            questionData.put("optionA", q.getOptionA());
            questionData.put("optionB", q.getOptionB());
            questionData.put("optionC", q.getOptionC());
            questionData.put("optionD", q.getOptionD());
            questionData.put("area", q.getArea());
            questionData.put("skill", q.getSkill());
            questionData.put("difficulty", q.getDifficulty());
            questionData.put("degree", q.getDegree());
            result.add(questionData);
        }
        return result;
    }

    // Helper methods to work with both SQLite and BigQuery
    private List<Question> getQuestions() {
        if (bigQueryQuestionService != null) {
            return bigQueryQuestionService.getAllQuestions();
        } else if (questionRepository != null) {
            return questionRepository.findAll();
        }
        return new ArrayList<>();
    }

    private Question findQuestionById(Long id) {
        if (bigQueryQuestionService != null) {
            List<Question> questions = bigQueryQuestionService.getAllQuestions();
            return questions.stream()
                    .filter(q -> q.getId().equals(id))
                    .findFirst()
                    .orElse(null);
        } else if (questionRepository != null) {
            Optional<Question> question = questionRepository.findById(id);
            return question.orElse(null);
        }
        return null;
    }
}
