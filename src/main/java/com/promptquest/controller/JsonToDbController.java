package com.promptquest.controller;

import com.promptquest.service.JsonImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST Controller for creating complete database from JSON input
 * Creates DB file, tables, and imports data in one operation
 */
@RestController
@RequestMapping("/api/json-to-db")
@CrossOrigin(origins = "*")
public class JsonToDbController {

    private static final Logger logger = LoggerFactory.getLogger(JsonToDbController.class);

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private JsonImportService jsonImportService;

    /**
     * Create complete database from JSON input file
     * POST /api/json-to-db/create-from-json
     * Body: {
     *   "jsonFilePath": "file:input/promptquest-questions-test.json",
     *   "recreateDb": true
     * }
     */
    @PostMapping("/create-from-json")
    public ResponseEntity<Map<String, Object>> createDatabaseFromJson(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            String jsonFilePath = (String) request.getOrDefault("jsonFilePath", "file:input/promptquest-questions-test.json");
            Boolean recreateDb = (Boolean) request.getOrDefault("recreateDb", true);

            logger.info("Creating database from JSON file: {} (recreate: {})", jsonFilePath, recreateDb);

            // Step 1: Create db directory if it doesn't exist
            File dbDir = new File("db");
            if (!dbDir.exists()) {
                dbDir.mkdirs();
                logger.info("Created db directory");
            }

            // Step 2: Tables are auto-created by JPA (ddl-auto=create-drop)
            logger.info("Tables will be auto-created by JPA");

            // Step 3: Import JSON data
            int questionsImported = jsonImportService.importQuestionsFromJson(jsonFilePath, false);

            // Step 4: Verify database file exists
            File dbFile = new File("db/promptquest.db");
            boolean dbFileExists = dbFile.exists();

            // Step 5: Get statistics (safely)
            Map<String, Object> statistics = new HashMap<>();
            try {
                statistics = jsonImportService.getImportStatistics();
            } catch (Exception e) {
                logger.warn("Could not get statistics: {}", e.getMessage());
                statistics.put("note", "Statistics not available - tables may not be created yet");
            }

            response.put("success", true);
            response.put("message", String.format("Database created successfully with %d questions", questionsImported));
            response.put("questionsImported", questionsImported);
            response.put("jsonFilePath", jsonFilePath);
            response.put("databaseFileCreated", dbFileExists);
            response.put("databaseFilePath", dbFile.getAbsolutePath());
            response.put("databaseFileSize", dbFileExists ? dbFile.length() : 0);
            response.put("recreatedTables", recreateDb);
            response.put("statistics", statistics);

            logger.info("Database creation completed successfully. File: {}, Size: {} bytes",
                       dbFile.getAbsolutePath(), dbFileExists ? dbFile.length() : 0);

        } catch (Exception e) {
            logger.error("Error creating database from JSON", e);
            response.put("success", false);
            response.put("message", "Failed to create database: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Quick setup - create everything from default JSON file
     * POST /api/json-to-db/quick-setup
     */
    @PostMapping("/quick-setup")
    public ResponseEntity<Map<String, Object>> quickSetup() {
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("jsonFilePath", "file:input/promptquest-questions-test.json");
        requestData.put("recreateDb", true);

        return createDatabaseFromJson(requestData);
    }

    /**
     * Create database from different JSON file
     * POST /api/json-to-db/create-from-file
     * Body: {"fileName": "my-questions.json"}
     */
    @PostMapping("/create-from-file")
    public ResponseEntity<Map<String, Object>> createFromSpecificFile(@RequestBody Map<String, Object> request) {
        String fileName = (String) request.get("fileName");
        if (fileName == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "fileName is required");
            return ResponseEntity.badRequest().body(response);
        }

        String fullPath = "file:input/" + fileName;
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("jsonFilePath", fullPath);
        requestData.put("recreateDb", true);

        return createDatabaseFromJson(requestData);
    }

    /**
     * Reset database - clear all data and recreate from JSON
     * POST /api/json-to-db/reset
     */
    @PostMapping("/reset")
    public ResponseEntity<Map<String, Object>> resetDatabase() {
        Map<String, Object> response = new HashMap<>();

        try {
            logger.info("Resetting database - clearing all data");

            // Clear all data
            jsonImportService.clearAllQuestions();

            // Tables are managed by JPA
            logger.info("Tables managed by JPA (ddl-auto=create-drop)");

            // Import fresh data
            int questionsImported = jsonImportService.importDefaultQuestions();

            response.put("success", true);
            response.put("message", "Database reset successfully");
            response.put("questionsImported", questionsImported);
            try {
                response.put("statistics", jsonImportService.getImportStatistics());
            } catch (Exception e) {
                response.put("statistics", "Statistics not available");
            }

        } catch (Exception e) {
            logger.error("Error resetting database", e);
            response.put("success", false);
            response.put("message", "Failed to reset database: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Get database info
     * GET /api/json-to-db/info
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getDatabaseInfo() {
        Map<String, Object> response = new HashMap<>();

        try {
            File dbFile = new File("db/promptquest.db");
            boolean dbExists = dbFile.exists();

            response.put("success", true);
            response.put("databaseExists", dbExists);
            response.put("databasePath", dbFile.getAbsolutePath());

            if (dbExists) {
                response.put("databaseSize", dbFile.length());
                response.put("lastModified", dbFile.lastModified());

                // Get statistics if data exists
                try {
                    Map<String, Object> statistics = jsonImportService.getImportStatistics();
                    response.put("statistics", statistics);
                } catch (Exception e) {
                    response.put("statistics", "No data or tables not created");
                }
            }

            // Check if input JSON file exists
            File jsonFile = new File("input/promptquest-questions-test.json");
            response.put("inputJsonExists", jsonFile.exists());
            response.put("inputJsonPath", jsonFile.getAbsolutePath());

        } catch (Exception e) {
            logger.error("Error getting database info", e);
            response.put("success", false);
            response.put("message", "Failed to get database info: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Helper method to drop and create tables
     */
    private void dropAndCreateTables() throws Exception {
        // Read schema.sql file
        var schemaResource = resourceLoader.getResource("file:db/schema.sql");

        if (!schemaResource.exists()) {
            throw new RuntimeException("Schema file not found at db/schema.sql");
        }

        String schemaContent;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(schemaResource.getInputStream(), StandardCharsets.UTF_8))) {
            schemaContent = reader.lines().collect(Collectors.joining("\n"));
        }

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            // Drop existing tables
            try {
                statement.execute("DROP TABLE IF EXISTS test_results");
                statement.execute("DROP TABLE IF EXISTS questions");
                logger.info("Dropped existing tables");
            } catch (Exception e) {
                logger.warn("Could not drop tables: {}", e.getMessage());
            }

            // Create tables from schema
            String[] statements = schemaContent.split(";");
            for (String sql : statements) {
                sql = sql.trim();
                if (!sql.isEmpty() && !sql.startsWith("--")) {
                    statement.execute(sql);
                }
            }

            logger.info("Tables created from schema.sql");
        }
    }
}