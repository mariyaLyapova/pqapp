-- PromptQuest Database Schema for Spring Boot
-- SQLite database for quiz questions

-- Main questions table
CREATE TABLE questions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    question TEXT NOT NULL,
    option_a TEXT NOT NULL,
    option_b TEXT NOT NULL,
    option_c TEXT NOT NULL,
    option_d TEXT NOT NULL,
    correct_answer CHAR(1) NOT NULL CHECK (correct_answer IN ('A', 'B', 'C', 'D')),
    explanation TEXT,
    difficulty INTEGER CHECK (difficulty BETWEEN 1 AND 5),
    area TEXT,
    skill TEXT,
    degree TEXT CHECK (degree IN ('junior', 'mid', 'senior'))
);

-- Test results table for user performance tracking
CREATE TABLE test_results (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_name TEXT,
    question_id INTEGER,
    selected_answer CHAR(1),
    is_correct BOOLEAN,
    answered_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    time_taken_seconds INTEGER,
    FOREIGN KEY (question_id) REFERENCES questions(id)
);

-- Indexes for better query performance
CREATE INDEX idx_skill_difficulty ON questions(skill, difficulty);
CREATE INDEX idx_area ON questions(area);
CREATE INDEX idx_degree ON questions(degree);
CREATE INDEX idx_test_results_user ON test_results(user_name);