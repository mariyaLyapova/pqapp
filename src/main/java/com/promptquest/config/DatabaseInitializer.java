package com.promptquest.config;

import com.promptquest.service.JsonImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * Database initialization component that runs on application startup.
 * Automatically creates SQLite database and loads JSON data.
 */
@Component
public class DatabaseInitializer implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

    @Autowired
    private JsonImportService jsonImportService;

    @Value("${promptquest.auto-initialize:true}")
    private boolean autoInitialize;

    @Value("${promptquest.json-file-path:file:input/promptquest-questions-test.json}")
    private String jsonFilePath;

    @Value("${promptquest.clear-on-startup:true}")
    private boolean clearOnStartup;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!autoInitialize) {
            logger.info("Database auto-initialization is disabled");
            return;
        }

        logger.info("Starting database initialization...");

        try {
            // Step 1: Create db directory if it doesn't exist
            File dbDir = new File("db");
            if (!dbDir.exists()) {
                boolean created = dbDir.mkdirs();
                if (created) {
                    logger.info("Created database directory: {}", dbDir.getAbsolutePath());
                } else {
                    logger.warn("Failed to create database directory: {}", dbDir.getAbsolutePath());
                }
            } else {
                logger.info("Database directory already exists: {}", dbDir.getAbsolutePath());
            }

            // Step 2: Clear existing data if configured to do so
            if (clearOnStartup) {
                logger.info("Clearing existing data as configured");
                try {
                    jsonImportService.clearAllQuestions();
                    logger.info("Successfully cleared existing questions");
                } catch (Exception e) {
                    logger.info("No existing data to clear or tables don't exist yet: {}", e.getMessage());
                }
            }

            // Step 3: Check if JSON file exists
            File jsonFile = new File(jsonFilePath.replace("file:", ""));
            if (!jsonFile.exists()) {
                logger.error("JSON file not found at: {} (absolute: {})", jsonFilePath, jsonFile.getAbsolutePath());
                logger.error("Please ensure the JSON file exists before starting the application");
                return;
            }

            logger.info("Found JSON file: {} (size: {} bytes)", jsonFile.getAbsolutePath(), jsonFile.length());

            // Step 4: Import JSON data
            logger.info("Loading JSON data from: {}", jsonFilePath);
            int questionsImported = jsonImportService.importQuestionsFromJson(jsonFilePath, false);

            // Step 5: Verify database file was created
            File dbFile = new File("db/promptquest.db");
            boolean dbExists = dbFile.exists();

            // Step 6: Get statistics
            try {
                var statistics = jsonImportService.getImportStatistics();
                logger.info("Database initialization completed successfully:");
                logger.info("  - Questions imported: {}", questionsImported);
                logger.info("  - Database file created: {} ({})", dbExists, dbFile.getAbsolutePath());
                logger.info("  - Database file size: {} bytes", dbExists ? dbFile.length() : 0);
                logger.info("  - Statistics: {}", statistics);
            } catch (Exception e) {
                logger.warn("Could not retrieve statistics: {}", e.getMessage());
                logger.info("Database initialization completed with {} questions imported", questionsImported);
            }

        } catch (Exception e) {
            logger.error("Failed to initialize database on startup", e);
            logger.error("Application will continue but database may not be properly initialized");
            // Don't throw exception to prevent application startup failure
            // Just log the error and continue
        }
    }
}