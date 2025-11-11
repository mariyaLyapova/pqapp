package com.promptquest.repository;

import com.promptquest.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Question entity
 * Provides custom query methods for test interface
 */
@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    // Find questions by skill
    List<Question> findBySkillIgnoreCase(String skill);

    // Find questions by area
    List<Question> findByAreaIgnoreCase(String area);

    // Find questions by difficulty
    List<Question> findByDifficulty(Integer difficulty);

    // Find questions by degree
    List<Question> findByDegreeIgnoreCase(String degree);

    // Find questions by skill and difficulty
    List<Question> findBySkillIgnoreCaseAndDifficulty(String skill, Integer difficulty);

    // Find questions by area and degree
    List<Question> findByAreaIgnoreCaseAndDegreeIgnoreCase(String area, String degree);

    // Get random questions (SQLite compatible)
    @Query(value = "SELECT * FROM questions ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<Question> findRandomQuestions(@Param("limit") int limit);

    // Get random questions by skill
    @Query(value = "SELECT * FROM questions WHERE LOWER(skill) = LOWER(:skill) ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<Question> findRandomQuestionsBySkill(@Param("skill") String skill, @Param("limit") int limit);

    // Get random questions by difficulty
    @Query(value = "SELECT * FROM questions WHERE difficulty = :difficulty ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<Question> findRandomQuestionsByDifficulty(@Param("difficulty") Integer difficulty, @Param("limit") int limit);

    // Get questions for a custom test (skill, difficulty, and count)
    @Query(value = "SELECT * FROM questions WHERE LOWER(skill) = LOWER(:skill) AND difficulty = :difficulty ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<Question> findQuestionsForTest(@Param("skill") String skill, @Param("difficulty") Integer difficulty, @Param("limit") int limit);

    // Get all unique skills
    @Query("SELECT DISTINCT q.skill FROM Question q ORDER BY q.skill")
    List<String> findAllSkills();

    // Get all unique areas
    @Query("SELECT DISTINCT q.area FROM Question q ORDER BY q.area")
    List<String> findAllAreas();

    // Get all unique degrees
    @Query("SELECT DISTINCT q.degree FROM Question q ORDER BY q.degree")
    List<String> findAllDegrees();

    // Count questions by skill
    long countBySkillIgnoreCase(String skill);

    // Count questions by difficulty
    long countByDifficulty(Integer difficulty);
}