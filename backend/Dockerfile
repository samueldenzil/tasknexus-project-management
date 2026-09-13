# Stage 1: Build the application
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copy the Maven wrapper and pom.xml
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Give execute permissions to the maven wrapper
RUN chmod +x ./mvnw

# Download dependencies (this caches them in a separate Docker layer)
RUN ./mvnw dependency:go-offline

# Copy the actual source code
COPY src ./src

# Build the application (skipping tests to speed up the build)
RUN ./mvnw clean package -DskipTests

# Stage 2: Create the minimal production image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the default Spring Boot port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

