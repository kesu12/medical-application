FROM maven:4.0.0-rc-4-eclipse-temurin-21 AS build
WORKDIR /backend
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests


FROM openjdk:21-ea-1-jdk
WORKDIR /backend
COPY --from=build /backend/target/*.jar /app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar", "by.bsuir.medical_application.DemoApplication"]