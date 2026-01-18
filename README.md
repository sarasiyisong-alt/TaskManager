# Task Management Application

A Task Management Application built with Spring Boot and PostgreSQL, fully containerized for easy deployment.

## Features

### Task Management
*   **Complete CRUD Operations**: Create, Read, Update, and Delete tasks efficiently.
*   **Status Workflow**: Manage tasks through a defined lifecycle (Pending -> Approved/Rejected).
*   **Dynamic Views**: Switch between a standard **List View** and an interactive **Calendar View** for better time management.
*   **Export Data**: Easily export task lists to CSV for external reporting and analysis.
*   **Priority & Assignment**: Set task priorities and assign them to specific users or yourself.

### User Management
*   **Role-Based Access Control (RBAC)**: Secure access with distinct roles:
    *   **Admin**: Full system control, user management.
    *   **Manager**: Enhanced task oversight.
    *   **User**: Standard task management capabilities.
*   **User Administration**: Admins can create, update, and manage user accounts directly from the dashboard.

### System & Security
*   **Secure Authentication**: Robust login system backed by Spring Security.
*   **Email Notifications**: Integration with MailHog to capture and display system emails during development.
*   **Containerized Deployment**: Zero-config setup using Docker and Docker Compose.


## Frontend Architecture & Design

*   **Framework**: [Alpine.js](https://alpinejs.dev/)
    *   Lightweight, purely behavior-driven framework for dynamic interactivity.
    *   Handles client-side state (modals, tabs, filtering) without complex build steps.
*   **Styling**: [Tailwind CSS](https://tailwindcss.com/) & [DaisyUI](https://daisyui.com/)
    *   Utility-first CSS architecture for highly customizable designs.
    *   DaisyUI adds semantic component classes to reduce class-name clutter.
*   **Design Philosophy**:
    *   **Aesthetic**: "Professional Minimalist" using a curated Zinc/Slate color palette.
    *   **Typography**: **Inter** font family for superior screen readability.
    *   **Experience**: Smooth micro-interactions, responsive layout, and immediate visual feedback.

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
