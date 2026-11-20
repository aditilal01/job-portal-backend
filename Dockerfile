# Build the Spring Boot application with Java 17.
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml settings.xml ./
COPY src ./src
RUN mvn --batch-mode --no-transfer-progress -s settings.xml clean package -DskipTests

# Run on a slim Java 17 runtime.
FROM eclipse-temurin:17-jre
WORKDIR /app
RUN groupadd --system spring && useradd --system --gid spring spring
COPY --from=build /workspace/target/job-portal-backend-1.0.0.jar app.jar
RUN mkdir -p /app/uploads/resumes && chown -R spring:spring /app
USER spring
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
