/**
 * Contains the core data model classes for the Student QA application.
 * <p>
 * These classes represent the main domain objects in the system — users, questions,
 * answers, reviews, clarifications, messages, and administrative structures.
 * They serve as the bridge between the database layer ({@code databasePart1}) and
 * the application logic ({@code logic}) and UI ({@code pages}) layers.
 * Each class encapsulates relevant fields, constructors, and getter/setter methods
 * for managing data integrity.
 *
 * <h2>User Roles:</h2>
 * <ul>
 *   <li>{@link model.User} – Base class defining shared attributes and behaviors for all user roles.</li>
 *   <li>{@link model.Admin} – Administrative user with system management privileges.</li>
 *   <li>{@link model.Instructor} – Instructor-type user with content management permissions.</li>
 *   <li>{@link model.Reviewer} – User who provides reviews and assessments on answers.</li>
 *   <li>{@link model.Staff} – Staff-type user with elevated access.</li>
 *   <li>{@link model.Student} – Standard student user.</li>
 * </ul>
 *
 * <h2>Q&amp;A Entities:</h2>
 * <ul>
 *   <li>{@link model.Question} – A question posted by a user, including title, body, and metadata.</li>
 *   <li>{@link model.Questions} – Manages CRUD operations and collections of {@link model.Question} objects.</li>
 *   <li>{@link model.Answer} – An answer associated with a specific question.</li>
 *   <li>{@link model.Answers} – Manages CRUD operations and collections of {@link model.Answer} objects.</li>
 *   <li>{@link model.Review} – A reviewer's evaluation of an answer.</li>
 *   <li>{@link model.Reviews} – Manages CRUD operations and collections of {@link model.Review} objects.</li>
 *   <li>{@link model.Clarification} – A clarification message tied to a question or answer.</li>
 * </ul>
 *
 * <h2>Administrative Structures:</h2>
 * <ul>
 *   <li>{@link model.AdminRequest} – A user-submitted request to an administrator.</li>
 *   <li>{@link model.AdminAction} – Records an administrative action taken on a request.</li>
 *   <li>{@link model.StaffRequest} – A request submitted to or managed by staff.</li>
 *   <li>{@link model.ModerationFlag} – Flags content for moderation review.</li>
 *   <li>{@link model.ModerationNote} – Notes attached to a moderation flag.</li>
 * </ul>
 *
 * <h2>Messaging and Notifications:</h2>
 * <ul>
 *   <li>{@link model.Messages} – Encapsulates private message data exchanged between users.</li>
 *   <li>{@link model.Notification} – Represents a notification sent to a user.</li>
 * </ul>
 *
 * <h2>Other:</h2>
 * <ul>
 *   <li>{@link model.ContentType} – Enum defining the type of content (e.g., QUESTION, ANSWER).</li>
 *   <li>{@link model.Edits} – Tracks edit history for questions or answers.</li>
 *   <li>{@link model.NavigationBar} – Defines the application's navigational toolbar UI component.</li>
 * </ul>
 *
 * @author CSE360-Team11 Fall 2025
 */
package model;
