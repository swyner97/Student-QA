/**
 * Contains the database access layer for the Student QA application.
 * <p>
 * This package provides the {@link databasePart1.DatabaseHelper} class, which
 * serves as the central interface between the application and its underlying
 * SQL database. It handles connection management, schema initialization, and
 * all raw SQL operations for users, questions, answers, reviews, clarifications,
 * notifications, moderation, and administrative data.
 * <p>
 * All other packages that need to read or write persistent data do so through
 * {@code DatabaseHelper}, keeping database logic centralized and separate from
 * business and UI concerns.
 *
 * @author CSE360-Team11 Fall 2025
 */
package databasePart1;
