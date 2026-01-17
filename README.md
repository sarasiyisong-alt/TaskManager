# Task Management Application

## Project Overview
This is a standard Task Management Application built with Spring Boot and PostgreSQL. It allows users to register, log in, and manage tasks with different statuses. The application supports role-based access control (Admin, User) and includes an email notification system using MailHog for development.

## Features
*   **User Authentication & Authorization**: Secure login and registration with Spring Security; Role-based permissions (Admin vs. Standard User).
*   **Task Management**: Create, Read, Update, and Delete (CRUD) tasks. Track task status (e.g., PENDING, IN_PROGRESS, COMPLETED).
*   **Dashboard UI**: A user-friendly calendar or list view to visualize and manage tasks. Default showing all tasks of the current user and their subordinates for current week. User can switch to different time range and filter by status.
*   **Email Notifications**: Integration with MailHog to capture and view email notifications sent by the application.

## Tech Stack
*   **Backend**: Java 17, Spring Boot 3.4.1
    *   Spring Web
    *   Spring Data JPA
    *   Spring Security
    *   Spring Boot Starter Mail
*   **Frontend**: HTML5, CSS3, JavaScript, Thymeleaf (implied by usage, or just static resources serving)
*   **Database**: PostgreSQL 15
*   **Tools**: Maven, Docker, Docker Compose

## Project Structure
```
src/main/java/com/example/taskmanager
├── config/             # Configuration classes (e.g., SecurityConfig)
├── controller/         # REST Controllers for handling HTTP requests
├── entity/             # JPA Entities (User, Task, Role)
├── repository/         # Data Access logic (Spring Data JPA Repositories)
└── service/            # Business logic layer
```

## Getting Started

### Prerequisites
Ensure you have the following installed:
*   [Java 17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
*   [Maven](https://maven.apache.org/download.cgi)
*   [Docker](https://www.docker.com/products/docker-desktop/) & Docker Compose

### Installation & Setup

1.  **Clone the repository**
    ```bash
    git clone https://github.com/sarasiyisong-alt/TaskManager.git
    cd TaskManager
    ```

2.  **Start Infrastructure (Database & MailHog)**
    Use Docker Compose to spin up the PostgreSQL database and MailHog service.
    ```bash
    docker-compose up -d
    ```
    This command will start:
    *   **PostgreSQL** on port `5432`
    *   **MailHog SMTP** on port `1025`
    *   **MailHog Web UI** on port `8025`

3.  **Build the Application**
    ```bash
    mvn clean install
    ```

4.  **Run the Application**
    ```bash
    mvn spring-boot:run
    ```
    *Alternatively, you can run the generated JAR file from the `target` directory.*

### Accessing the Application
*   **Web Application**: Open [http://localhost:8080](http://localhost:8080) in your browser.
*   **MailHog UI**: Open [http://localhost:8025](http://localhost:8025) to view captured emails.
*   **API / Backward Compatibility**: The backend runs on port `8080` by default.

## Default Credentials (if applicable)
*   The `docker-compose.yml` configures the database with:
    *   User: `taskuser`
    *   Password: `taskpass`
    *   Database: `taskmanager`
