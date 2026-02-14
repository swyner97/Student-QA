/**
 * The {@code model} package defines the core data structures and entities used throughout the application.
 * <p>
 * These classes represent the main objects in the system—such as users, questions, answers, messages, and reviews—
 * and serve as the bridge between the database layer and the application logic. 
 * Each class typically encapsulates relevant fields, constructors, and getter/setter methods
 * for managing data integrity and enabling easy interaction with the {@code databasePart1} package.
 *
 * <h2>Class Summary:</h2>
 * <ul>
 *   <li>{@link Admin} – Represents an administrative user with system management privileges.</li>
 *   <li>{@link Answer} – Defines a single answer associated with a specific question.</li>
 *   <li>{@link Answers} – Provides utility or collection-level management for multiple {@code Answer} objects.</li>
 *   <li>{@link Clarification} – Represents a user-submitted clarification or suggestion related to a question or answer.</li>
 *   <li>{@link FollowUpQ} – Represents a follow-up question tied to an existing question for further discussion.</li>
 *   <li>{@link Instructor} – Represents an instructor-type user with permissions for reviewing or managing content.</li>
 *   <li>{@link Messages} – Encapsulates private message data exchanged between users.</li>
 *   <li>{@link NavigationBar} – Defines elements of the application's navigational UI structure.</li>
 *   <li>{@link Question} – Represents an individual question posted by a user, including title, description, and metadata.</li>
 *   <li>{@link Questions} – Handles operations and collections of {@code Question} objects (e.g., loading, sorting, filtering).</li>
 *   <li>{@link Review} – Represents a single review entity, often linked to a user or post evaluation.</li>
 *   <li>{@link Reviewer} – Defines a reviewer-type user who provides feedback or assessments on questions or answers.</li>
 *   <li>{@link Reviews} – Manages collections of {@code Review} objects, such as retrieving or storing multiple reviews.</li>
 *   <li>{@link Student} – Represents a standard student user within the system.</li>
 *   <li>{@link User} – The base user class defining shared attributes and behaviors for all user roles (student, instructor, admin, staff and reviewer.).</li>
 * </ul>
 *
 * <p>
 * Together, these classes form the foundation of the system’s domain model, ensuring a consistent and organized representation of data.
 * They are used by higher-level components in the {@code logic} and {@code pages} packages to implement business logic and UI behavior.
 * </p>
 */

package model;