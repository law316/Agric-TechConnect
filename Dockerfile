# Use a lightweight Java image for running Spring Boot
FROM eclipse-temurin:17-jdk-alpine

# Set the working directory inside the container
WORKDIR /app

# Copy Maven wrapper and pom.xml first to leverage Docker cache
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies (this step uses caching)
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B

# Copy the entire project
COPY src src

# Package the application
RUN ./mvnw clean package -DskipTests

# Expose the port your Spring Boot app runs on
EXPOSE 8080

# 🔥 FIX: run whatever jar Maven builds
CMD ["sh", "-c", "java -jar target/*.jar"]
