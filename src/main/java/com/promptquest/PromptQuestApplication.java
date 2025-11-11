package com.promptquest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot Application class for PromptQuest
 *
 * PromptQuest is a quiz application that manages multiple-choice questions
 * with support for importing from JSON and tracking user performance.
 */
@SpringBootApplication
public class PromptQuestApplication {

    public static void main(String[] args) {
        SpringApplication.run(PromptQuestApplication.class, args);
    }
}