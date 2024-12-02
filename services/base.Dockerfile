# Base image for all services
FROM eclipse-temurin:17-jdk-focal

# Set working directory
WORKDIR /app

# Copy the jar file
COPY target/*.jar app.jar

# Default command
ENTRYPOINT ["java", "-jar", "app.jar"]
