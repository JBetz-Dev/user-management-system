# User Management System

A full-stack user management system built from scratch to understand web development fundamentals without relying on frameworks or external libraries.

## Background & Learning Story

This project represents a deliberate choice to build core web technologies from the ground up rather than immediately reaching for frameworks like Spring Boot or React. The goal was to understand what these tools actually do under the hood before using them.

This was also my first exploration of ES6 modules and modular frontend architecture, transitioning from single-file JavaScript to a component-based approach. The project serves as preparation for future work with React and Spring Boot, ensuring I understand the fundamentals these frameworks abstract away.

## Key Design Decisions & Limitations

**From-Scratch Implementation:**
- Custom HTTP/1.1 server without web frameworks
- Manual JSON parsing and HTTP request/response handling
- Vanilla JavaScript with modular component architecture
- JDBC-based database operations without ORM

**Acknowledged Tradeoffs:**
- Simplicity and learning over production-ready features
- Basic session management vs. token-based authentication
- Custom implementations vs. battle-tested libraries
- Educational value prioritized over performance optimization

## Architecture & Learning Outcomes

**Technical Focus:**
- **Modularization**: Component-based frontend, layered backend architecture
- **DRY Principle**: Reusable form components, shared HTTP message handling
- **Single Responsibility**: Separated concerns across services, DAOs, and handlers
- **Layered Architecture**: Clear separation between HTTP handling, business logic, and data access

**Learning Outcomes:**
- **HTTP Protocol**: Request/response structure, headers, status codes, binary content handling
- **JSON Processing**: Structure, parsing, and serialization without external libraries
- **Web Server Architecture**: Connection handling, routing, session management
- **Full-Stack Integration**: Frontend-backend communication, state management, error flows
- **ES6 Modules**: Component organization, import/export patterns, modular design

## Current Features

- **User Authentication**: Registration and login with secure password hashing
- **Session Management**: Cookie-based sessions with automatic expiry
- **Profile Management**: Email and password updates with validation
- **User Listing**: CRUD demonstration (User Area placeholder functionality)
- **File Serving**: Static file delivery with session-based access control
- **Error Handling**: Comprehensive error flows with user-friendly messaging

## Future Roadmap

**Technical Refinements:**
- Improved routing and flow control
- Connection pooling for database operations
- Extracted error handling middleware
- Enhanced frontend modal and component systems

**Feature Additions:**
- Admin roles and user management
- Password reset functionality
- Email notifications
- Enhanced user area functionality

## Getting Started

**Prerequisites:**
- Java 21+
- PostgreSQL database
- Modern web browser

**Setup:**
1. Create a `.env` file in the `src` directory with your database configuration:
```
DB_URL=localhost
DB_PORT=5432
DB_NAME=yourDBnamehere
DB_USER=yourDBuserhere
DB_PASSWORD=yourDBpasswordhere
```
2. Set up your PostgreSQL database with a `users` table:
```sql
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(25) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL
);
```
3. Build then run the HttpServer class:
```
./gradlew build
./gradlew run
```
4. Navigate to `http://localhost:9000` in your browser

*Note: Docker containerization with automated database setup and sample data coming in future updates.*

## Technology Stack

**Backend:**
- Java 21 with virtual threads
- Gradle build system
- JDBC for database operations
- Custom HTTP/1.1 server implementation
- BCrypt for password hashing

**Frontend:**
- Vanilla JavaScript with ES6 modules
- CSS3 with modern styling techniques
- Component-based architecture

**Database:**
- PostgreSQL

## Project Structure

```
├── .idea/                   # IntelliJ IDEA project files
├── gradle/                  # Gradle wrapper files
├── src/
│   ├── main/java/           # Backend HTTP server and user management
│   ├── js/                  # Frontend JavaScript modules
│   │   ├── assets/          # Static assets (images, icons)
│   │   ├── components/      # Reusable UI components
│   │   │   └── forms/       # Form-specific components
│   │   ├── pages/           # Page-specific scripts
│   │   ├── services/        # API and session management
│   │   ├── utils/           # Validation and constants
│   │   └── main.js          # Frontend application entry point
│   ├── styles.css           # Application styling
│   └── *.html               # Application pages
├── .env                     # Database configuration
├── build.gradle.kts         # Gradle build configuration
└── settings.gradle.kts      # Gradle project settings
```

---

*This project prioritizes understanding over convenience, exploring the foundational technologies that modern web frameworks build upon.*
