# STAGE 1: Download Dependencies
FROM eclipse-temurin:25-jdk-alpine AS dependencies
WORKDIR /build

# Copy the Maven wrapper and pom.xml first
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Grant execution rights and download dependencies offline
RUN chmod +x ./mvnw
RUN ./mvnw dependency:go-offline -B

# STAGE 2: Build the Application
FROM dependencies AS builder

# Copy the actual source code
COPY src ./src

# Compile and package the application
RUN ./mvnw clean package -DskipTests

# STAGE 3: Create Lightweight Runtime Image
FROM eclipse-temurin:25-jre-alpine AS runtime
WORKDIR /app

# Security best practice: Create and use a non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy ONLY the compiled JAR from the builder stage
COPY --from=builder /build/target/*.jar app.jar

# Expose the application port
EXPOSE 8080

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]