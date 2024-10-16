# Chat Api

This is the backend of ChatApp(chat-app), a messaging application that provides a REST API for user registration, authentication, friend management, real-time messaging, and notifications. 

The backend uses Spring Boot with PostgreSQL for production, H2 for testing, and STOMP over WebSocket for real-time messaging. Spring Security handles authentication and authorization using JWT (JSON Web Tokens).

## Tech Stack
- **Spring Boot:** Backend framework for building RESTful APIs.
- **Spring Security:**  Provides authentication and authorization mechanisms.
- **JWT (JSON Web Tokens):** Secures the API and authenticates users.
- **STOMP:**  Real-time messaging and notifications.
- **PostgreSQL:**  Production database.
- **H2:** In-memory database for testing.
- **Spring Data JPA:** For database interaction.
- **Lombok:** Reduces boilerplate code for model classes.

## Setup Instructions

Follow these instructions to set up and run the project locally for development and testing purposes.

```
-> git clone https://github.com/moetez96/chat-api.git
-> cd chat-api
-> mvn clean install
-> mvn spring-boot:run
```
