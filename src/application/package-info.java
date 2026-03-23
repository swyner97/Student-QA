/**
 * Contains the main entry point for the Student QA JavaFX application.
 *
 * <p>The {@link application.StartApp} class bootstraps the application by
 * establishing the database connection, initializing shared state via
 * {@link logic.StatusData}, seeding default reviewer accounts and sample
 * reviews on first run, and routing the user to either
 * {@link pages.FirstPage} (empty database) or
 * {@link pages.InitialAccessPage} (returning users).
 *
 * @author CSE360-Team11 Fall 2025
 */
package application;
