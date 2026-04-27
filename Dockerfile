# Multi-stage build for Spring Boot application
FROM gradle:8.10.2-jdk21 as builder

WORKDIR /build

# Copy gradle files
COPY gradlew gradlew.bat build.gradle settings.gradle ./
COPY gradle/ gradle/

# Copy source code
COPY src/ src/

# Build the application
RUN ./gradlew build -x test

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the built JAR from the builder stage
COPY --from=builder /build/build/libs/*.jar app.jar

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

