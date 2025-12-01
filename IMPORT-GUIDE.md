# PromptQuest JSON Import Guide

This guide explains how to import questions into the PromptQuest application using either the command-line script or the web-based admin panel.

## 🚀 Quick Start Options

### Option 1: Command Line (Automated)
```bash
# Make the script executable (first time only)
chmod +x import-questions.sh

# Import questions and start app with automatic initialization
./import-questions.sh -f your-questions.json
```

### Option 2: Web Admin Panel (User-Friendly)
1. Start the application: `mvn spring-boot:run`
2. Open browser to: `http://localhost:8081/admin.html`
3. Upload JSON file through the web interface

## 📋 Script Options

| Option | Description | Example |
|--------|-------------|---------|
| `-f, --file` | JSON file to import (required) | `-f questions.json` |
| `-p, --port` | Server port (default: 8081) | `-p 8080` |
| `-s, --start` | Start application in background | `-s` |
| `--no-clear` | Don't clear existing database | `--no-clear` |
| `-h, --help` | Show help message | `-h` |

## 📖 Usage Examples

### Basic Import
```bash
# Import questions from a JSON file
./import-questions.sh -f input/promptquest-questions-test.json
```

### Background Start
```bash
# Import and start app in background
./import-questions.sh -f questions.json -s
```

### Custom Port
```bash
# Import and run on different port
./import-questions.sh -f questions.json -p 8080
```

### Keep Existing Data
```bash
# Import without clearing existing questions
./import-questions.sh -f questions.json --no-clear
```

## 🌐 Web Admin Panel

The admin panel provides a user-friendly alternative to command-line imports:

### Access
Navigate to `http://localhost:8081/admin.html` after starting the application.

### Features
- **Drag & Drop File Upload**: Easy file selection with visual feedback
- **Import Configuration**: Choose to clear existing data or append
- **Real-time Progress**: Visual indicators during import process
- **Database Statistics**: Live view of question counts and distribution
- **Error Handling**: Clear feedback on import issues
- **Responsive Design**: Works on desktop and mobile devices

### Usage Steps
1. **Start Application**: `mvn spring-boot:run` (if not already running)
2. **Open Admin Panel**: Navigate to admin.html in your browser
3. **Select File**: Click "Choose File" or drag JSON file to upload area
4. **Configure Import**: Toggle "Clear existing questions" as needed
5. **Import**: Click "Import Questions" button
6. **Monitor**: Watch progress and view updated statistics

The web interface supports the same JSON format and validation as the command-line tool.

## 📁 File Structure

```
prompt-quest-app/
├── import-questions.sh     # Import script
├── input/                  # Directory for JSON files
│   └── promptquest-questions-test.json
├── db/                     # Database files (auto-created)
└── pom.xml
```

## 🔧 JSON File Format

Your JSON file should contain a wrapper object with a questions array:

```json
{
  "questions": [
    {
      "question": "What is the default access modifier for a class in Java?",
      "options": [
        {
          "key": "A",
          "text": "public"
        },
        {
          "key": "B",
          "text": "private"
        },
        {
          "key": "C",
          "text": "protected"
        },
        {
          "key": "D",
          "text": "package-private"
        }
      ],
      "answer": "D",
      "explanation": "In Java, if no access modifier is specified...",
      "difficulty": 1,
      "area": "backend",
      "skill": "java",
      "degree": "junior"
    }
  ]
}
```

### Required Fields
- `questions` - Array containing all question objects
- `question` - The question text
- `options` - Array of answer options with `key` (A/B/C/D) and `text`
- `answer` - Correct answer key (A, B, C, or D)
- `explanation` - Explanation of the answer
- `difficulty` - Difficulty level (1-5)
- `area` - Subject area (e.g., "backend", "frontend")
- `skill` - Specific skill (e.g., "java", "spring boot")
- `degree` - Experience level ("junior", "mid", "senior")

## 🌐 Accessing the Application

After the script completes, access the quiz at:
- **Web Interface**: http://localhost:8081 (or your custom port)
- **API Endpoints**:
  - `GET /api/quiz/questions` - Get all questions
  - `GET /api/quiz/random/{count}` - Get random questions
  - `POST /api/quiz/check` - Submit quiz answers

The application will automatically initialize the database with your JSON data on startup.

## 🔍 Troubleshooting

### Common Issues

1. **"Maven not found"**
   ```bash
   # Install Maven or add it to PATH
   export PATH=/path/to/maven/bin:$PATH
   ```

2. **"JSON file not found"**
   ```bash
   # Check file path and permissions
   ls -la your-questions.json
   ```

3. **"Port already in use"**
   ```bash
   # Use different port
   ./import-questions.sh -f questions.json -p 8082
   ```

4. **"Permission denied"**
   ```bash
   # Make script executable
   chmod +x import-questions.sh
   ```

### Log Files

When using background start (`-s`), check logs:
```bash
# View logs
tail -f import.log

# Stop background process
kill <PID>  # PID shown when starting
```

## 🔄 Re-importing Data

To import new questions:

1. **Clear existing data** (default):
   ```bash
   ./import-questions.sh -f new-questions.json
   ```

2. **Keep existing data**:
   ```bash
   ./import-questions.sh -f additional-questions.json --no-clear
   ```

## 💡 Tips

- Place JSON files in the `input/` directory for organization
- Use meaningful filenames (e.g., `java-basics-2024.json`)
- Test with small JSON files first
- The script uses Spring Boot's automatic initialization feature
- Use `--no-clear` to preserve existing questions in the database
- The database is created automatically on first startup

## 🔧 How It Works

The import script works through Spring Boot's automatic initialization:

1. **Environment Setup**: Sets Spring environment variables for database initialization
2. **File Preparation**: Copies your JSON file to the `input/` directory
3. **Application Startup**: Starts Spring Boot with automatic initialization enabled
4. **Database Creation**: `DatabaseInitializer.java` reads the environment variables and imports data automatically

## 🆘 Support

If you encounter issues:
1. Check the JSON file format matches the required structure
2. Verify Maven installation (`mvn --version`)
3. Ensure you're in the project root directory (where `pom.xml` exists)
4. Check file permissions on the JSON file
5. Review console output for error messages during Spring Boot startup