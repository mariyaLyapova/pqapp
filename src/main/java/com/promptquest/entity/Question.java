package com.promptquest.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

/**
 * Question entity for PromptQuest application
 * Represents a multiple-choice question with options and metadata
 */
@Entity
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "Question text cannot be blank")
    private String question;

    @Column(name = "option_a", nullable = false)
    @NotBlank(message = "Option A cannot be blank")
    private String optionA;

    @Column(name = "option_b", nullable = false)
    @NotBlank(message = "Option B cannot be blank")
    private String optionB;

    @Column(name = "option_c", nullable = false)
    @NotBlank(message = "Option C cannot be blank")
    private String optionC;

    @Column(name = "option_d", nullable = false)
    @NotBlank(message = "Option D cannot be blank")
    private String optionD;

    @Column(name = "correct_answer", nullable = false, length = 1)
    @Pattern(regexp = "[ABCD]", message = "Correct answer must be A, B, C, or D")
    private String correctAnswer;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Column
    @Min(value = 1, message = "Difficulty must be between 1 and 5")
    @Max(value = 5, message = "Difficulty must be between 1 and 5")
    private Integer difficulty;

    @Column
    private String area;

    @Column
    private String skill;

    @Column
    @Pattern(regexp = "junior|mid|senior", message = "Degree must be junior, mid, or senior")
    private String degree;

    // Default constructor
    public Question() {}

    // Constructor for easy creation
    public Question(String question, String optionA, String optionB, String optionC, String optionD,
                   String correctAnswer, String explanation, Integer difficulty, String area, String skill, String degree) {
        this.question = question;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctAnswer = correctAnswer;
        this.explanation = explanation;
        this.difficulty = difficulty;
        this.area = area;
        this.skill = skill;
        this.degree = degree;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getOptionA() {
        return optionA;
    }

    public void setOptionA(String optionA) {
        this.optionA = optionA;
    }

    public String getOptionB() {
        return optionB;
    }

    public void setOptionB(String optionB) {
        this.optionB = optionB;
    }

    public String getOptionC() {
        return optionC;
    }

    public void setOptionC(String optionC) {
        this.optionC = optionC;
    }

    public String getOptionD() {
        return optionD;
    }

    public void setOptionD(String optionD) {
        this.optionD = optionD;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public Integer getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Integer difficulty) {
        this.difficulty = difficulty;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getSkill() {
        return skill;
    }

    public void setSkill(String skill) {
        this.skill = skill;
    }

    public String getDegree() {
        return degree;
    }

    public void setDegree(String degree) {
        this.degree = degree;
    }

    @Override
    public String toString() {
        return "Question{" +
                "id=" + id +
                ", question='" + question + '\'' +
                ", correctAnswer='" + correctAnswer + '\'' +
                ", difficulty=" + difficulty +
                ", area='" + area + '\'' +
                ", skill='" + skill + '\'' +
                ", degree='" + degree + '\'' +
                '}';
    }
}