package com.promptquest.controller;

import com.promptquest.service.JsonImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller for admin operations including JSON file import
 */
@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    private static final String UPLOAD_DIR = "temp/uploads/";

    @Autowired
    private JsonImportService jsonImportService;

    /**
     * Import questions from uploaded JSON file
     */
    @PostMapping("/import-json")
    public ResponseEntity<?> importJsonFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "clearExisting", defaultValue = "false") boolean clearExisting) {

        logger.info("Received file upload request: {} ({}), clearExisting: {}",
                    file.getOriginalFilename(), file.getSize(), clearExisting);

        // Validate file
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("No file selected"));
        }

        if (!file.getOriginalFilename().toLowerCase().endsWith(".json")) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Only JSON files are allowed"));
        }

        if (file.getSize() > 10 * 1024 * 1024) { // 10MB limit
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("File size exceeds 10MB limit"));
        }

        try {
            // Create upload directory if it doesn't exist
            Path uploadPath = Path.of(UPLOAD_DIR);
            Files.createDirectories(uploadPath);

            // Save uploaded file temporarily
            String fileName = "import_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            logger.info("File saved temporarily at: {}", filePath);

            // Import questions using the service
            int importedCount = jsonImportService.importQuestionsFromJson(
                    "file:" + filePath.toAbsolutePath().toString(),
                    clearExisting
            );

            // Clean up temporary file
            Files.deleteIfExists(filePath);
            logger.info("Temporary file cleaned up: {}", filePath);

            // Return success response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Questions imported successfully");
            response.put("importedCount", importedCount);

            logger.info("Successfully imported {} questions", importedCount);
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            logger.error("File I/O error during import: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to process file: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Error during import: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Import failed: " + e.getMessage()));
        }
    }

    /**
     * Get current database statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        try {
            Map<String, Object> stats = jsonImportService.getImportStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Error getting statistics: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to get statistics: " + e.getMessage()));
        }
    }

    /**
     * Helper method to create error response
     */
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("message", message);
        return error;
    }
}