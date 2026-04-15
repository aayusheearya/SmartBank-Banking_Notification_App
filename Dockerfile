# Stage 1: Build the application
FROM maven:3.8.5-openjdk-17 AS build
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM openjdk:17-jdk-slim
COPY --from=build /target/*.jar app.jar
# This matches your server.port=9098
EXPOSE 9098
ENTRYPOINT ["java", "-jar", "app.jar"]