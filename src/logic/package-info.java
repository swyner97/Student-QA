/**
 * Contains business logic and utility classes for the Student QA application.
 * <p>
 * This package acts as the layer between the UI ({@code pages}) and the data
 * layer ({@code model} and {@code databasePart1}), providing input validation,
 * shared application state, search utilities, and operation result handling.
 *
 * <h2>Class Summary:</h2>
 * <ul>
 *   <li>{@link logic.StatusData} – Holds shared application state such as the current
 *       user, primary stage, and references to model managers.</li>
 *   <li>{@link logic.PasswordRecognizer} – Validates passwords against security and
 *       formatting rules.</li>
 *   <li>{@link logic.UserNameRecognizer} – Validates usernames against format rules.</li>
 *   <li>{@link logic.EmailRecognizer} – Validates email address format.</li>
 *   <li>{@link logic.Result} – A lightweight wrapper representing the success or failure
 *       of an operation, optionally carrying an error message.</li>
 *   <li>{@link logic.SearchFunction} – Provides keyword-based search utilities for
 *       filtering questions, answers, or users.</li>
 *   <li>{@link logic.ClarificationsManager} – Manages creation, retrieval, and storage
 *       of clarification messages.</li>
 * </ul>
 *
 * @author CSE360-Team11 Fall 2025
 */
package logic;
