# Use the base Dockerfile
FROM eclipse-temurin:17-jdk-focal

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]
