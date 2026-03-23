# CSE360 Team Project

## Overview

This project is a **Java desktop application** designed to support a Q&A and peer review system in an educational environment. It allows students to ask and answer questions, while instructors, reviewers, staff, and administrators manage content, moderation, and system workflows.

The application follows a modular structure separating UI, business logic, data models, and database access.

---

## Project Information

* **Course:** CSE 360 - Software Engineering
* **Language:** Java
* **Framework:** JavaFX
* **Database:** JDBC-based (configured in `DatabaseHelper.java`)
* **Project Type:** Desktop Application

---

## Project Status

Originally developed as part of CSE 360 (Software Engineering), this project is now being actively maintained and extended independently.

### Ongoing Improvements
- Refactoring and code cleanup for maintainability
- Enhancing UI/UX of JavaFX pages
- Improving database structure and queries
- Expanding feature set beyond original course scope
- Fixing bugs and improving performance

### Future Plans
- Improve application architecture and modularity
- Add more robust search and filtering
- Enhance reviewer ranking system
- Improve testing coverage

---

## Features

### User Roles

* **Student:** Ask and answer questions
* **Instructor:** Manage and moderate content
* **Reviewer:** Review and provide feedback on answers
* **Staff:** Assist with system operations
* **Admin:** Full system control and user management

### Core Functionality

* Post, edit, and search questions
* Submit and manage answers
* Peer review and feedback system
* Content moderation and flagging
* Messaging and notifications
* Role-based access control

---

## Project Structure

```
project-root/
├── src/
│   ├── application/        # Application entry point
│   ├── databasePart1/      # Database access layer
│   ├── logic/              # Business logic and utilities
│   ├── model/              # Data models and entities
│   ├── pages/              # JavaFX UI components
│   └── resource-files/     # Static assets (images, fonts)
│
├── tests/                  # Unit tests
├── 360CSEDesignDocs/       # UML and design diagrams
├── doc/                    # Generated JavaDoc documentation
├── README.md
└── .gitignore
```

---

## Technology Stack

* **Java** (JDK 11+ recommended)
* **JavaFX** for UI
* **JDBC** for database connectivity
* **Git** for version control

---

## Getting Started

### Prerequisites

* JDK 11 or higher
* JavaFX SDK
* Java IDE (Eclipse or IntelliJ recommended)
* A running SQL database

---

### Installation

1. **Clone the repository**

```bash
git clone <repository-url>
cd pub_app11-main
```

2. **Set up JavaFX**

* Download JavaFX SDK from [https://openjfx.io/](https://openjfx.io/)
* Add the `lib` folder to your project libraries
* Add VM options when running:

```
--module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml
```

3. **Configure Database**

* Open `DatabaseHelper.java`
* Update the database connection settings:

```java
private static final String DB_URL = "...";
private static final String DB_USER = "...";
private static final String DB_PASSWORD = "...";
```

4. **Run the Application**

* Run the main class (e.g., `StartApp.java`) from your IDE

> Note: Using an IDE is recommended due to JavaFX setup and multi-package structure.

---

## Code Organization

### application/

Contains the main application entry point and startup logic.

### model/

Defines core data structures such as users, questions, answers, and reviews.

### logic/

Handles business logic, validation, and application workflows.

### pages/

Implements JavaFX UI screens for different user interactions.

### databasePart1/

Provides database connectivity and CRUD operations using JDBC.

---

## Testing

* Unit tests are located in the `tests/` directory
* Focus on core components such as database operations and data models

---

## Documentation

### JavaDoc

* Located in the `doc/` directory
* Generated from source code comments

### Design Documents

* Located in `360CSEDesignDocs/`
* Includes UML diagrams and sequence diagrams

---

## Notes

* IDE configuration files (e.g., `.vscode`, `.settings`) and compiled files (`bin/`) are excluded from documentation for clarity
* The project is structured for maintainability and modular development

---

## Authors

CSE 360 Team Project Group

---

## License

This project is for educational purposes only.
