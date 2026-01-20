# UserService

A Spring Boot-based user management microservice with JWT authentication, role-based access control, and MySQL persistence.

## Features

- User registration and authentication
- JWT token generation and validation
- MySQL database integration
- Service discovery with Eureka

## Technologies

- Java
- Spring Boot
- Spring Data JPA
- MySQL
- Maven
- Eureka (Service Discovery)

## Getting Started

### Prerequisites

- Java 17+
- Maven
- MySQL Server
- Eureka Server (for service discovery)

### Configuration

Set the following environment variables or update `src/main/resources/application.properties`:

- `PORT`: Port for the service (e.g., 8080)
- `SECRET_KEY`: Secret key for JWT signing

Example `.env`:
PORT=8080 SECRET_KEY=your_jwt_secret

### Database Setup

Create a MySQL database named `UserService`:

```sql
CREATE DATABASE UserService;
```

The schema will be initialized automatically using Flyway migrations.

### Build and run the Project

`mvn clean install`
`mvn spring-boot:run`

The service will be available at http://localhost:<PORT_NUMBER>.

### API Endpoints
- POST /signup - Register a new user 
- POST /login - Authenticate and receive JWT 
- POST /logout - Invalidate JWT (optional, depends on implementation)
- POST /validate - Validate JWT token

### Service Discovery

Ensure Eureka server is running at http://localhost:8761/eureka. 