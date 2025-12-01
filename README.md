# PromptQuest

A comprehensive Spring Boot quiz application with a complete web interface for technical questions and assessments.

## Features

- 🎯 Interactive web-based quiz interface with advanced filtering
- 🔍 **Smart Question Filtering**: Filter questions by area, skills, and question count
- 🎛️ **Multi-Select Filters**: Choose multiple areas and skills for customized quizzes
- 📊 **Dynamic Question Count**: Select from 5 to 30 questions or use all available
- 🎲 **Random Question Selection**: Questions are randomly shuffled when count is limited
- 📈 **Real-time Filter Preview**: See exactly how many questions match your filters
- 🛠️ Admin panel for question management and file uploads
- 📊 Creates SQLite database from JSON files
- 🚀 Spring Boot + JPA/Hibernate integration
- 💾 Automatic database initialization on startup
- ✨ RESTful API for quiz operations
- 📱 Responsive single-page application
- 🔄 Real-time quiz scoring and feedback
- 📤 Web-based JSON file upload functionality

## Quick Start

### Prerequisites

**Java 17+, Maven 3.6+**

```bash
# Clone the repository
git clone https://github.com/mariyaLyapova/pqapp.git
cd pqapp

# Run the application
mvn spring-boot:run
```

**Access the application:**
- **Quiz Interface:** http://localhost:8081
- **Admin Panel:** http://localhost:8081/admin.html

The application will automatically:
- Initialize the SQLite database on first startup
- Import sample quiz questions if available
- Provide a complete quiz interface at the root URL
- Offer an admin panel for question management

## Quiz Interface Features

### Advanced Question Filtering

The quiz interface includes powerful filtering capabilities to customize your quiz experience:

#### 🔍 Filter Options

1. **Area Filter (Multi-Select)**
   - Choose from available question areas (e.g., Programming, Databases, etc.)
   - Select multiple areas to include questions from different domains
   - Visual display shows "All Areas" when everything is selected

2. **Skills Filter (Multi-Select)**
   - Filter by specific technical skills (e.g., Java, Spring Boot, Python, etc.)
   - Support for multi-skill selection (e.g., "Java + Spring Boot" for framework-specific tests)
   - Smart text display shows selected skills with "+X more" for multiple selections

3. **Question Count Filter**
   - Choose exactly how many questions for your quiz
   - Options: 5, 10, 15, 20, 25, 30 questions, or all available
   - Default: 10 questions for optimal quiz length

#### 🎲 Smart Question Selection

- **Random Shuffling**: When limiting question count, questions are randomly selected to ensure variety
- **Real-time Preview**: See exactly how many questions match your current filters
- **Filter Combinations**: Mix and match areas, skills, and counts for precise quiz targeting

#### 💡 Example Use Cases

- **Java Spring Boot Test**: Select "Programming" area + "Java, Spring Boot" skills + 15 questions
- **Full Stack Assessment**: Select "Frontend, Backend" areas + relevant skills + 25 questions
- **Quick Skills Check**: Select specific skill + 5 questions for rapid assessment
- **Comprehensive Review**: Select "All Areas" + "All Skills" + 30 questions for thorough testing

#### 🎨 User Experience

- **Intuitive Interface**: Clean, responsive design with consistent 300px filter controls
- **Visual Feedback**: Clear indication of selected filters and resulting question counts
- **Flexible Layout**: Area and Skills filters side-by-side, Question Count centered below
- **One-Click Reset**: "Clear Filters" button to instantly reset to all questions

## API Endpoints

### Quiz Operations

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/quiz/questions` | Get all quiz questions |
| GET | `/api/quiz/random/{count}` | Get random questions for quiz |
| POST | `/api/quiz/check` | Submit answers and get results |

### Admin Operations

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/admin/import-json` | Upload and import JSON question files |
| GET | `/api/admin/stats` | Get database statistics |
| DELETE | `/api/admin/clear` | Clear all questions from database |

### Data Management

The application uses automatic database initialization on startup. Use the included import script for easy data management:

```bash
# Import questions from JSON file
./import-questions.sh -f your-questions.json

# Import and start in background
./import-questions.sh -f questions.json -s

# Import without clearing existing data
./import-questions.sh -f questions.json --no-clear
```

### Example Usage

**Get Quiz Questions**
```bash
curl -X GET http://localhost:8081/api/quiz/questions
```

**Get 5 Random Questions**
```bash
curl -X GET http://localhost:8081/api/quiz/random/5
```

**Submit Quiz Answers**
```bash
curl -X POST http://localhost:8081/api/quiz/check \
  -H "Content-Type: application/json" \
  -d '[{"questionId": 1, "answer": "A"}, {"questionId": 2, "answer": "B"}]'
```

**Data Import Examples**
```bash
# See IMPORT-GUIDE.md for detailed instructions
./import-questions.sh -f input/promptquest-questions-test.json
./import-questions.sh -f questions.json -p 8080 --no-clear
```

## Admin Panel

The admin panel provides a user-friendly web interface for managing quiz questions:

### Features
- 📤 **File Upload**: Upload JSON files directly through the web interface
- 📊 **Database Statistics**: View question counts, difficulty distribution, and categories
- 🗑️ **Data Management**: Clear existing questions or append new ones
- ✅ **Import Validation**: Real-time feedback on file uploads and import status
- 📝 **Progress Tracking**: Visual progress indicators for import operations

### Access
Navigate to `http://localhost:8081/admin.html` after starting the application.

### Usage
1. **Upload JSON File**: Click "Choose File" and select your question JSON file
2. **Configure Import**: Choose whether to clear existing questions or append
3. **Import**: Click "Import Questions" to process the file
4. **Monitor Progress**: Watch real-time feedback and statistics updates

The admin panel supports the same JSON format as the command-line import tool.

## JSON Format

Create an `input/` directory and place your JSON files there. Expected format:

```json
{
  "questions": [
    {
      "question": "What does JVM stand for?",
      "options": [
        {"key": "A", "text": "Java Virtual Machine"},
        {"key": "B", "text": "Java Verification Manager"},
        {"key": "C", "text": "Java Version Manager"},
        {"key": "D", "text": "Java Variable Machine"}
      ],
      "answer": "A",
      "explanation": "JVM stands for Java Virtual Machine...",
      "difficulty": 1,
      "area": "Programming",
      "skill": "java",
      "degree": "junior"
    }
  ]
}
```

## Database Schema

The application creates a `questions` table with the following structure:

```sql
CREATE TABLE questions (
    id INTEGER PRIMARY KEY,
    question TEXT NOT NULL,
    option_a TEXT NOT NULL,
    option_b TEXT NOT NULL,
    option_c TEXT NOT NULL,
    option_d TEXT NOT NULL,
    correct_answer CHAR(1) NOT NULL,
    explanation TEXT,
    difficulty INTEGER,
    area TEXT,
    skill TEXT,
    degree TEXT
);
```

## Configuration

### Database Settings

The application uses SQLite by default. Configuration in `application.properties`:

```properties
# Database connection
spring.datasource.url=jdbc:sqlite:db/promptquest.db
spring.datasource.driver-class-name=org.sqlite.JDBC

# JPA/Hibernate Configuration
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.database-platform=org.hibernate.community.dialect.SQLiteDialect
spring.jpa.properties.hibernate.globally_quoted_identifiers=true

# Server Configuration
server.port=8081
```

### File Locations

- **JSON Input**: `input/` directory (not included in repository - create locally)
- **Database Output**: `db/promptquest.db` (auto-generated)
- **Schema**: `db/schema.sql` (reference only)

## Project Structure

```
prompt-quest-app/
├── src/main/java/com/promptquest/
│   ├── config/
│   │   └── DatabaseInitializer.java    # Auto-startup database setup
│   ├── controller/
│   │   └── QuizController.java         # Quiz operations API
│   ├── entity/
│   │   └── Question.java               # JPA entity
│   ├── repository/
│   │   └── QuestionRepository.java     # Data access layer
│   ├── service/
│   │   └── JsonImportService.java      # JSON import business logic
│   └── PromptQuestApplication.java     # Main application
├── src/main/resources/
│   ├── application.properties          # Spring configuration
│   └── static/
│       └── index.html                  # Complete quiz web interface
├── .gitignore                          # Git ignore rules
├── README.md                           # Project documentation
├── IMPORT-GUIDE.md                     # Import script documentation
├── import-questions.sh                 # Data import utility script
└── pom.xml                            # Maven dependencies
```

## Dependencies

Key dependencies used in this project:

- **Spring Boot Starter Web** - REST API support
- **Spring Boot Starter Data JPA** - Database operations
- **SQLite JDBC** - SQLite database driver
- **Hibernate Community Dialects** - SQLite dialect support
- **Jackson** - JSON processing

## Development

### Building the Project

```bash
mvn clean compile
```

### Running Tests

```bash
mvn test
```

### Creating a JAR

```bash
mvn clean package
java -jar target/promptquest-app-0.0.1-SNAPSHOT.jar
```

## Troubleshooting

### Common Issues

1. **Port 8081 already in use**
   - Stop other processes: `lsof -ti:8081 | xargs kill -9`
   - Or change port in `application.properties`

2. **JSON file not found**
   - Ensure JSON file is in `input/` directory
   - Check file path in API request

3. **Database creation fails**
   - Check `db/` directory permissions
   - Verify SQLite JDBC driver is available

### Logs

Application logs show detailed information about:
- Database creation progress
- JSON import statistics
- Error details and stack traces

Set log level in `application.properties`:
```properties
logging.level.com.promptquest=DEBUG
```

## Security Considerations

This application is designed for educational and demonstration purposes. For production use, consider implementing:

- **CORS Configuration**: Replace `@CrossOrigin(origins = "*")` with specific allowed domains
- **Rate Limiting**: Add request throttling to prevent abuse
- **Input Validation**: Add validation annotations for API inputs
- **HTTPS**: Always use HTTPS in production environments
- **Monitoring**: Add logging and monitoring for suspicious activities

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

For detailed contribution guidelines, see [CONTRIBUTING.md](CONTRIBUTING.md).

---

**Built with** ❤️ **using Spring Boot and SQLite**