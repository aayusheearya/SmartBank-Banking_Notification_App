# Stage 1: Build the application
FROM maven:3.8.5-openjdk-17 AS build
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Run the application
# Switching to 'eclipse-temurin', which is more reliable
FROM eclipse-temurin:17-jre
COPY --from=build /target/*.jar app.jar
# This matches your server.port=9098
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]