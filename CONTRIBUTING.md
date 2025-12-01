# Contributing to PromptQuest

Thank you for your interest in contributing to PromptQuest! This document outlines the process for contributing to this project.

## Getting Started

1. **Fork the repository** on GitHub
2. **Clone your fork** locally:
   ```bash
   git clone https://github.com/your-username/pqapp.git
   cd pqapp
   ```
3. **Create a feature branch** from main:
   ```bash
   git checkout -b feature/your-feature-name
   ```

## Development Setup

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- Docker (optional, for containerized development)

### Local Development
```bash
# Install dependencies and run tests
mvn clean compile test

# Start the application
mvn spring-boot:run
```

### Docker Development
```bash
# Build and run with Docker
docker-compose up -d

# View logs
docker-compose logs -f
```

## Making Changes

### Code Style
- Follow Java conventions and Spring Boot best practices
- Use meaningful variable and method names
- Add appropriate comments for complex logic
- Ensure all public methods have proper documentation

### Testing
- Add unit tests for new functionality
- Ensure all existing tests pass
- Test both API endpoints and web interface

### Documentation
- Update README.md if adding new features
- Update API documentation for new endpoints
- Add examples for new functionality

## Submitting Changes

1. **Commit your changes** with descriptive messages:
   ```bash
   git add .
   git commit -m "Add feature: description of what you added"
   ```

2. **Push to your fork**:
   ```bash
   git push origin feature/your-feature-name
   ```

3. **Create a Pull Request** on GitHub with:
   - Clear title and description
   - Screenshots for UI changes
   - Reference to any related issues

## Pull Request Guidelines

### Requirements
- [ ] Code compiles without errors
- [ ] All tests pass
- [ ] Documentation is updated
- [ ] Feature works in both local and Docker environments
- [ ] No security vulnerabilities introduced

### Review Process
1. Automated tests will run on your PR
2. Code review by maintainers
3. Address any feedback or requested changes
4. Merge once approved

## Issue Reporting

When reporting bugs or requesting features:

1. **Check existing issues** first
2. **Use clear, descriptive titles**
3. **Provide detailed descriptions** with:
   - Steps to reproduce (for bugs)
   - Expected vs actual behavior
   - Environment details (OS, Java version, etc.)
   - Screenshots if applicable

## Feature Requests

We welcome feature requests! Please:
- Explain the use case and benefit
- Provide mockups or examples if applicable
- Consider if it fits the project's scope and goals

## Areas for Contribution

- 🐛 **Bug fixes**: Check open issues labeled "bug"
- 🚀 **New features**: Quiz types, filtering options, UI improvements
- 📚 **Documentation**: Improve guides, add examples
- 🔧 **Performance**: Optimize database queries, improve loading times
- 🎨 **UI/UX**: Enhance the web interface design
- 🔒 **Security**: Implement security best practices
- ⚡ **Testing**: Add more comprehensive test coverage

## Questions?

Feel free to:
- Open an issue for discussion
- Reach out to maintainers
- Check existing documentation

## Code of Conduct

Please be respectful and constructive in all interactions. We aim to maintain a welcoming community for all contributors.

---

Thank you for contributing to PromptQuest! 🎯