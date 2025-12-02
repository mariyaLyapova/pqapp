package com.promptquest.controller;

import com.promptquest.entity.Question;
import com.promptquest.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@Controller
@CrossOrigin(origins = "*") // Allow frontend access
public class QuizController {

    @Autowired
    private QuestionRepository questionRepository;

    // Web interface routes
    @GetMapping("/")
    public String root() {
        return "redirect:/index.html";
    }

    // Get all questions for the quiz
    @GetMapping("/api/quiz/questions")
    @ResponseBody
    public List<Map<String, Object>> getAllQuestions() {
        List<Question> questions = questionRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Question q : questions) {
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

    // Check answers and get results
    @PostMapping("/api/quiz/check")
    @ResponseBody
    public Map<String, Object> checkAnswers(@RequestBody Map<String, String> answers) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> questionResults = new ArrayList<>();

        int correct = 0;
        int total = answers.size();

        for (String questionIdStr : answers.keySet()) {
            Long questionId = Long.parseLong(questionIdStr);
            String userAnswer = answers.get(questionIdStr);

            Optional<Question> questionOpt = questionRepository.findById(questionId);
            if (questionOpt.isPresent()) {
                Question question = questionOpt.get();
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
        List<Question> allQuestions = questionRepository.findAll();
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
}