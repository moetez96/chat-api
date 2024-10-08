FROM maven:3.8.2-openjdk-17-slim AS build
LABEL authors="moetez ayari"

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

FROM openjdk:17.0.1-jdk-slim

COPY --from=build /target/chat-api.jar /chat-api.jar

EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE}
ENV JDBC_HOST=${JDBC_HOST}
ENV JDBC_PASSWORD=${JDBC_PASSWORD}
ENV JDBC_PORT=${JDBC_PORT}
ENV JDBC_USERNAME=${JDBC_USERNAME}
ENV JWT_EXPIRATION=${JWT_EXPIRATION}
ENV JWT_SECRET=${JWT_SECRET}

ENTRYPOINT ["java", "-jar", "/chat-api.jar"]
