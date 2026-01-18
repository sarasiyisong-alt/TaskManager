# Task Management Application

![Demo](assets/demo.webp)

A Task Management Application built with Spring Boot and PostgreSQL, fully containerized for easy deployment.

## Prerequisites

*   [Docker](https://www.docker.com/products/docker-desktop/) & Docker Compose

## Quick Start

1.  **Clone the repository**
    ```bash
    git clone https://github.com/sarasiyisong-alt/TaskManager.git
    cd TaskManager
    ```

2.  **Start Application**
    Run the following command to build and start the entire application stack (App, Database, MailHog):
    ```bash
    docker-compose up --build
    ```
    *Wait a few minutes for the services to start.*

3.  **Access Application**
    *   **App URL**: [http://localhost:8080](http://localhost:8080)
    *   **MailHog (Emails)**: [http://localhost:8025](http://localhost:8025)

## Default Credentials

Use these credentials to log in:

| Role | Username | Password |
| :--- | :--- | :--- |
| **Standard User** | `user` | `password` |
| **Manager** | `manager` | `password` |
| **Admin** | `admin` | `password` |

*Note: The `admin` account may have reduced functionality in the current seed data; `manager` is recommended for testing task features.*
