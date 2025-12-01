# Multi-stage build for better image optimization

# Build stage
FROM maven:3.9.5-eclipse-temurin-17 AS builder

# Set working directory
WORKDIR /app

# Copy pom.xml and download dependencies (for better caching)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Create app directory and user for security
RUN groupadd -r appgroup && useradd -r -g appgroup appuser
WORKDIR /app

# Create necessary directories
RUN mkdir -p db input logs && chown -R appuser:appgroup /app

# Copy the jar file from build stage
COPY --from=builder /app/target/promptquest-app-*.jar app.jar
RUN chown appuser:appgroup app.jar

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8081/actuator/health || exit 1

# Environment variables
ENV JAVA_OPTS="-Xms512m -Xmx1024m"

# Run the application
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]