# Database Startup Initialization Test

This document describes how to test the new automatic database initialization feature.

## Changes Made

1. **Created DatabaseInitializer.java**: Automatic startup component that initializes the database
2. **Updated application.properties**: Added configuration options for auto-initialization
3. **Modified ddl-auto setting**: Changed from `create-drop` to `update` for persistent data

## Configuration Options

Add these to `application.properties` to control startup behavior:

```properties
# Enable/disable automatic database initialization on startup
promptquest.auto-initialize=true

# JSON file path to load on startup
promptquest.json-file-path=file:input/promptquest-questions-test.json

# Whether to clear existing data before importing
promptquest.clear-on-startup=true
```

## Testing Steps

### 1. Clean Start Test
```bash
# Remove existing database
rm -f db/promptquest.db

# Start the application
./mvnw spring-boot:run

# Check logs for initialization messages
# Should see: "Starting database initialization..."
# Should see: "Questions imported: X"
# Should see: "Database file created: true"
```

### 2. Restart Test (with existing data)
```bash
# Start application again (database already exists)
./mvnw spring-boot:run

# Check logs - should handle existing data gracefully
# If clear-on-startup=true, should clear and reload
# If clear-on-startup=false, should skip or append
```

### 3. Verify Database
```bash
# Check database file was created
ls -la db/promptquest.db

# Connect to database and verify data
sqlite3 db/promptquest.db
.tables
SELECT COUNT(*) FROM questions;
.quit
```

### 4. Test API Still Works
```bash
# Test the info endpoint
curl http://localhost:8081/api/json-to-db/info

# Should return database info showing questions exist
```

## Configuration Scenarios

### Scenario 1: Auto-initialization Enabled (Default)
```properties
promptquest.auto-initialize=true
promptquest.clear-on-startup=true
```
- Database and JSON load automatically on startup
- Existing data is cleared before loading

### Scenario 2: Auto-initialization Disabled
```properties
promptquest.auto-initialize=false
```
- No automatic initialization
- Must use REST APIs to initialize database manually

### Scenario 3: Preserve Existing Data
```properties
promptquest.auto-initialize=true
promptquest.clear-on-startup=false
```
- Database loads on startup
- Existing data is preserved (may create duplicates)

## Expected Log Output

When starting with auto-initialization enabled:

```
[DatabaseInitializer] Starting database initialization...
[DatabaseInitializer] Created database directory: /path/to/db
[DatabaseInitializer] Clearing existing data as configured
[DatabaseInitializer] Found JSON file: /path/to/input/promptquest-questions-test.json (size: XXXX bytes)
[DatabaseInitializer] Loading JSON data from: file:input/promptquest-questions-test.json
[JsonImportService] Starting JSON import from: file:input/promptquest-questions-test.json
[JsonImportService] Successfully imported X questions
[DatabaseInitializer] Database initialization completed successfully:
[DatabaseInitializer]   - Questions imported: X
[DatabaseInitializer]   - Database file created: true (/path/to/db/promptquest.db)
[DatabaseInitializer]   - Database file size: XXXX bytes
```

## Troubleshooting

### JSON File Not Found
If you see: "JSON file not found at: ..."
- Verify the file exists at `input/promptquest-questions-test.json`
- Check the `promptquest.json-file-path` configuration

### Database Creation Failed
If database file is not created:
- Check file permissions in the `db/` directory
- Verify SQLite driver is available in classpath

### Import Errors
If questions are not imported:
- Check JSON file format
- Review application logs for detailed error messages
- Verify JsonImportService is working correctly