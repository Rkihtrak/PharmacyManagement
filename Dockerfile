# Build stage
FROM openjdk:17-jdk-slim AS builder
WORKDIR /app
RUN apt-get update && apt-get install -y tar
COPY . .
RUN ./mvnw clean package -DskipTests

# Run stage
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"] 