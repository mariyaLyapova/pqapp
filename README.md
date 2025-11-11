# PromptQuest Database Creator

A Spring Boot application that creates SQLite database files from JSON quiz data using JPA/Hibernate.

## Features

- 📊 Creates SQLite database from JSON files
- 🚀 Spring Boot + JPA/Hibernate integration
- 💾 Automatic table creation and data import
- ✨ Simple REST API for database operations

## Quick Start

### Prerequisites

- Java 17 or higher
- Maven 3.6+

### Running the Application

1. **Clone the repository**
```bash
git clone <repository-url>
cd prompt-quest-app
```

2. **Run the application**
```bash
mvn spring-boot:run
```

3. **Create database from JSON**
```bash
curl -X POST http://localhost:8081/api/json-to-db/quick-setup
```

This will:
- Create `db/promptquest.db` SQLite file
- Import all questions from `input/promptquest-questions-test.json`
- Return success confirmation with statistics

## API Endpoints

### Database Creation

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/json-to-db/quick-setup` | Create database with default JSON file |
| POST | `/api/json-to-db/create-from-json` | Create database with custom JSON file |
| POST | `/api/json-to-db/reset` | Reset and recreate database |
| GET | `/api/json-to-db/info` | Get database information |

### Example Usage

**Quick Setup (Recommended)**
```bash
curl -X POST http://localhost:8081/api/json-to-db/quick-setup
```

**Custom JSON File**
```bash
curl -X POST http://localhost:8081/api/json-to-db/create-from-json \
  -H "Content-Type: application/json" \
  -d '{"jsonFilePath": "file:input/your-file.json", "recreateDb": true}'
```

**Check Database Info**
```bash
curl -X GET http://localhost:8081/api/json-to-db/info
```

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
│   ├── controller/
│   │   └── JsonToDbController.java     # REST API endpoints
│   ├── entity/
│   │   └── Question.java               # JPA entity
│   ├── repository/
│   │   └── QuestionRepository.java     # Data access layer
│   ├── service/
│   │   └── JsonImportService.java      # Business logic
│   └── PromptQuestApplication.java     # Main application
├── src/main/resources/
│   └── application.properties          # Configuration
├── db/
│   └── schema.sql                      # Database schema reference
├── .gitignore                          # Git ignore rules
├── README.md                           # Project documentation
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

## License

This project is open source and available under the [MIT License](LICENSE).

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

**Built with** ❤️ **using Spring Boot and SQLite**