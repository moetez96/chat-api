FROM maven:3.8.2-openjdk-17-slim AS build
LABEL authors="moetez ayari"

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

FROM openjdk:17.0.1-jdk-slim

COPY --from=build /target/chat-api.jar /chat-api.jar

EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE}

ENTRYPOINT ["java", "-jar", "/chat-api.jar"]
