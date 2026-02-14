# CSE360 Team Project

## Overview

This is a **JavaFX-based Q&A and Peer Review Management System** designed for an educational environment. The application enables students to ask questions, get answers from peers and instructors, and participate in a trusted reviewer system for quality assurance. Instructors and administrators can manage content, moderate discussions, and oversee the platform.

## Project Information
- **Course**: CSE 360 - Software Engineering
- **Language:** Java
- **UI Framework:** JavaFX
- **Database:** SQL-based (DatabaseHelper)
- **Project Type:** Desktop Application

---

## Table of Contents

1. [Features](#features)
2. [Project Structure](#project-structure)
3. [Technology Stack](#technology-stack)
4. [Getting Started](#getting-started)
5. [Building and Running](#building-and-running)
6. [Code Organization](#code-organization)
7. [Database](#database)
8. [Key Classes and Models](#key-classes-and-models)
9. [Development Workflow](#development-workflow)
10. [Documentation](#documentation)
11. [Team Resources](#team-resources)

---

## Features

### User Roles & Authentication
- **Student:** Post questions, provide answers, rate reviewers, request clarifications
- **Instructor:** Manage course content, moderate discussions, approve posts
- **Reviewer/Trusted Reviewer:** Review and rank student solutions, provide feedback
- **Staff:** Support user management and requests
- **Admin:** Full platform control, user management, system configuration

### Core Functionality

#### Q&A System
- Post and search questions across the platform
- Provide answers with multimedia support
- Edit questions and answers with version history tracking
- Follow-up questions for clarification
- Mark solutions as resolved

#### Trusted Reviewer System
- Student request process to become trusted reviewers
- Reviewer ranking and reputation system
- Admin approval/verification workflow
- Reviewer profile management

#### Moderation & Content Management
- Flag inappropriate content
- Moderation notes and handling workflow
- Admin interface for content review
- Clarification request management

#### User Management
- User authentication and role-based access control
- Profile management with customizable information
- Account updates and password management
- Invitation codes for staff/admin setup

#### Communication
- Peer-to-peer messaging system
- Notifications for activity updates
- Admin request tracking and responses

#### Search & Discovery
- Full-text search for questions and answers
- Tag-based categorization
- Filter by user, date, and relevance

---

## Project Structure

```
pub_app11-main/
├── src/
│   ├── application/              # Main application entry point
│   │   └── StartCSE360.java      # Application launcher
│   ├── model/                    # Data models and entities
│   │   ├── User.java             # Base user class
│   │   ├── Student.java
│   │   ├── Instructor.java
│   │   ├── Admin.java
│   │   ├── Staff.java
│   │   ├── Reviewer.java
│   │   ├── Question.java
│   │   ├── Answer.java
│   │   ├── Review.java
│   │   ├── ModerationFlag.java
│   │   ├── ModerationNote.java
│   │   ├── Messages.java
│   │   ├── Notification.java
│   │   ├── AdminRequest.java
│   │   ├── StaffRequest.java
│   │   └── ... (other models)
│   ├── pages/                    # UI pages and components
│   │   ├── FirstPage.java        # Initial page flow
│   │   ├── WelcomeLoginPage.java # Login interface
│   │   ├── RoleSelectionPage.java
│   │   ├── AdminHomePage.java
│   │   ├── InstructorHomePage.java
│   │   ├── MyQAPage.java
│   │   ├── SearchAsPage.java
│   │   ├── ReviewPage.java
│   │   ├── ModerationHandlingPage.java
│   │   ├── MessagingPage.java
│   │   ├── NotificationsPage.java
│   │   ├── TrustedReviewersPage.java
│   │   └── ... (25+ UI pages)
│   ├── logic/                    # Business logic and utilities
│   │   ├── StatusData.java       # Application state management
│   │   ├── SearchFunction.java   # Search implementation
│   │   ├── ClarificationsManager.java
│   │   ├── UserNameRecognizer.java
│   │   ├── PasswordRecognizer.java
│   │   ├── EmailRecognizer.java
│   │   ├── Result.java
│   │   └── UserQAMenu.java
│   ├── databasePart1/
│   │   └── DatabaseHelper.java   # Database connection and operations
│   ├── legal/                    # License and legal files
│   └── pics/                     # UI images and icons
├── tests/                        # JUnit test cases
│   └── application/
├── doc/                          # JavaDoc documentation
├── 360CSEDesignDocs/            # UML diagrams (Astah files)
│   ├── AskQSeqDiagram.asta
│   ├── InstructorInteractionTrustedRevSD.asta
│   ├── InvitationCodeSetRoleSD.asta
│   ├── ManualSetRolesSD.asta
│   ├── MarkSolutionSequence.asta
│   ├── ModerationFlag-NoteHandling.asta
│   ├── RolesUseCase.asta
│   └── StudentInteractionTrustedRevSD.asta
├── README.md                
└── .gitignore
```

---

## Technology Stack

- **Language:** Java 11+
- **GUI Framework:** JavaFX
- **Database:** SQL 
- **Build Tool:** Standard Java tooling
- **Version Control:** Git
- **Documentation:** JavaDoc, Astah UML

---

## Getting Started

### Prerequisites

- Java Development Kit (JDK) 11 or higher
- JavaFX SDK (compatible with your JDK version)
- Java IDE (Eclipse, IntelliJ IDEA, or NetBeans recommended)
- SQL Database (MySQL, PostgreSQL, or SQLite)

### Installation

1. **Clone the repository:**
   ```bash
   git clone <repository-url>
   cd pub_app11-main
   ```

2. **Set Up JavaFX**

Download JavaFX SDK from [openjfx.io](https://openjfx.io/)

Add JavaFX libraries to your IDE:

**For Eclipse:**
    1. Right-click project → Build Path → Configure Build Path
    2. Add External JARs → Select all JavaFX lib/*.jar files
    3. Run Configurations → VM arguments:
```
--module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml
```
    **For IntelliJ IDEA:**
    1. File → Project Structure → Libraries → + → Java
    2. Select JavaFX lib folder
    3. Run → Edit Configurations → VM options:
```
--module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml
```

3. **Configure Database:**
    1. Create a new database:
    ```sql
    CREATE DATABASE student_qa_system;
    ```

    2. Update database connection in `DatabaseHelper.java`:
    ```java
    private static final String DB_URL = "jdbc:mysql://localhost:3306/student_qa_system";
    private static final String DB_USER = "your_username";
    private static final String DB_PASSWORD = "your_password";
    ```
    3. Run the application - tables will be auto-created on first run

4. **Compile and Run:**
    **Option A: Using IDE**
    - Right-click on `InitialAccessPage.java` or main launcher
    - Run as Java Application

    **Option B: Command Line**
    ```bash
    javac --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls *.java
    java --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls InitialAccessPage
    ```
---

## Code Organization

### Application Package (`application/`)
- **StartCSE360.java:** Main application class that extends `javafx.application.Application`
- Handles window initialization (3000x700 resolution)
- Manages database connection and lifecycle

### Model Package (`model/`)
Contains all data entity classes:
- **User hierarchy:** User → Student, Instructor, Admin, Staff, Reviewer
- **Content:** Question, Answer, Answers, Review, Reviews
- **Interaction:** Messages, Notification, FollowUpQ, Clarification
- **Management:** ModerationFlag, ModerationNote, AdminRequest, StaffRequest
- **UI Component:** NavigationBar

### Pages Package (`pages/`)
Contains 30+ JavaFX UI pages organized by functionality:
- **Authentication:** WelcomeLoginPage, RoleSelectionPage
- **Home Pages:** AdminHomePage, InstructorHomePage, FirstPage
- **Content Management:** MyQAPage, SearchAsPage, EditQuestionPage, EditAnswerPage
- **Moderation:** ModerationHandlingPage, AdminRequestsPage
- **Communication:** MessagingPage, NotificationsPage
- **Reviewer System:** TrustedReviewersPage, ReviewerProfilePage, RequestsPage
- **Profile:** ProfilePage, UpdateAccountPage

### Logic Package (`logic/`)
Business logic and utilities:
- **StatusData.java:** Central state management for the application
- **SearchFunction.java:** Query and filtering logic
- **Recognizer classes:** Validation for username, email, password
- **ClarificationsManager.java:** Follow-up question management
- **UserQAMenu.java:** User interaction workflows

### Database Package (`databasePart1/`)
- **DatabaseHelper.java:** JDBC connection, CRUD operations, schema management

---

## Key Classes and Models

### User Model
```java
public class User {
    private int id;
    private String userName;
    private String password;
    private Role role;
    private String name;
    private String email;
    private String phone;
    private String bio;
    // ... methods
}
```

**Roles:** STUDENT, REVIEWER, INSTRUCTOR, STAFF, ADMIN

### Question Model
```java
public class Question {
    private int questionId;
    private int userId;
    private String author;
    private String title;
    private String description;
    private String timestamp;
    private boolean resolved;
    private List<String> tags;
    private List<Answer> answers;
    private List<Edits> editHistory;
}
```

### Answer Model
```java
public class Answer {
    private int answerId;
    private int questionId;
    private int userId;
    private String author;
    private String content;
    private String timestamp;
    private boolean isMarked; // Marked as solution
}
```

### Review Model
```java
public class Review {
    private int reviewId;
    private int submitterId;
    private int reviewerId;
    private int answerId;
    private String feedback;
    private int rating;
}
```

---

## Database

### Connection
The `DatabaseHelper` class manages all database operations:
- Establishes JDBC connections
- Executes CRUD operations
- Manages schema and migrations

### Database Tables (inferred)
- **users** - User accounts with roles
- **questions** - Posted questions
- **answers** - Answer submissions
- **reviews** - Peer reviews
- **messages** - User communications
- **moderation_flags** - Flagged content
- **trusted_reviewers** - Reviewer relationships
- **notifications** - User notifications
- **admin_requests** - Admin tasks
- And more...

For detailed schema information, check the database initialization files or JavaDoc for DatabaseHelper.

---

## Documentation

### JavaDoc
- **Location:** `doc/` directory
- Generated from source code comments
- View online: [JavaDoc Index](doc/index.html)

### Design Documentation
- **UML Diagrams:** `360CSEDesignDocs/` - Sequence diagrams created with Astah
  - AskQSeqDiagram.asta
  - InstructorInteractionTrustedRevSD.asta
  - ModerationFlag-NoteHandling.asta
  - And more...

### Design Documents
- Detailed implementation plans
- Allocation of work among team members
- Testing strategies

---

## Building and Deployment

### System Requirements
- **JDK Version:** 11 or higher
- **Memory:** Minimum 2GB RAM
- **Database:** MySQL 5.7+ or PostgreSQL 10+

### Window Configuration
- **Width:** 3000 pixels
- **Height:** 700 pixels

### Database Connection
The application automatically manages database connections:
- Connection established in `StartCSE360.start()`
- Graceful shutdown on application close
- Connection pooling recommended for production

---

## FAQ

### How do I connect to the database?
The database configuration is in `DatabaseHelper.java`. Update the connection URL, username, and password as needed for your environment.

### How are user roles determined?
User roles are assigned during registration or by administrators. The system supports:
- **STUDENT** - Can ask questions, answer, request reviewer status
- **REVIEWER** - Can review student answers (appointed by admin)
- **INSTRUCTOR** - Can moderate and manage course content
- **STAFF** - Support role with limited permissions
- **ADMIN** - Full system access

### Can users have multiple roles?
Yes, the User model includes a `roles` list that can contain multiple role assignments.

### How is content moderation handled?
Content can be flagged by any user. Flags are reviewed on the ModerationHandlingPage by instructors and admins. Moderation notes track actions taken.

### What is the trusted reviewer system?
It allows students to request and be approved as "trusted reviewers" through an invitation and verification process, enabling them to review peer work.

---

**Last Updated:** February 2026
**Project Phase:** Phase 4 (Active)
