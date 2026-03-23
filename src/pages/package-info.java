/**
 * Contains all JavaFX UI page classes for the Student QA application.
 * <p>
 * Each class in this package corresponds to a distinct screen or view.
 * Pages interact with the {@code model}, {@code logic}, and {@code databasePart1}
 * layers to present data and handle user input. Most pages follow a pattern of
 * initializing UI controls, loading data on scene display, and defining
 * event handlers inline.
 *
 * <h2>Authentication and Account Management:</h2>
 * <ul>
 *   <li>{@link pages.FirstPage} – Displayed on first launch to set up the initial admin account.</li>
 *   <li>{@link pages.InitialAccessPage} – Entry point for returning users to log in or register.</li>
 *   <li>{@link pages.WelcomeLoginPage} – Login screen for existing users.</li>
 *   <li>{@link pages.RoleSelectionPage} – Allows users to select their role on login.</li>
 *   <li>{@link pages.InvitationPage} – Handles invitation-based account creation.</li>
 *   <li>{@link pages.ResetPasswordPage} – Provides password reset functionality.</li>
 *   <li>{@link pages.UpdateAccountPage} – Enables users to edit their profile and credentials.</li>
 * </ul>
 *
 * <h2>Home and Navigation:</h2>
 * <ul>
 *   <li>{@link pages.AdminHomePage} – Home dashboard for administrator users.</li>
 *   <li>{@link pages.AdminSetupPage} – Initial administrator configuration page.</li>
 *   <li>{@link pages.InstructorHomePage} – Home dashboard for instructor users.</li>
 *   <li>{@link pages.StaffPage} – Dashboard for staff users.</li>
 *   <li>{@link pages.UserQAMenu} – Main Q&amp;A menu for standard users.</li>
 * </ul>
 *
 * <h2>Question and Answer Management:</h2>
 * <ul>
 *   <li>{@link pages.AnswersPage} – Displays answers for a selected question.</li>
 *   <li>{@link pages.EditQuestionPage} – Interface for editing a submitted question.</li>
 *   <li>{@link pages.EditAnswerPage} – Interface for editing a submitted answer.</li>
 *   <li>{@link pages.MyPostsPage} – Shows questions and answers authored by the current user.</li>
 *   <li>{@link pages.MyQAPage} – Personal Q&amp;A view for the current user.</li>
 *   <li>{@link pages.SearchPage} – Keyword-based search across questions and answers.</li>
 * </ul>
 *
 * <h2>Review System:</h2>
 * <ul>
 *   <li>{@link pages.ReviewPage} – Displays reviews for a given answer.</li>
 *   <li>{@link pages.EditReviewPage} – Interface for editing a previously written review.</li>
 *   <li>{@link pages.ReviewerProfilePage} – Shows a reviewer's profile and trust rating.</li>
 *   <li>{@link pages.ReviewerRequestPage} – Allows users to request reviewer status.</li>
 *   <li>{@link pages.TrustedReviewersPage} – Interface for managing trusted reviewers.</li>
 *   <li>{@link pages.RankReviewer} – Displays reviewer rankings.</li>
 * </ul>
 *
 * <h2>Messaging and Notifications:</h2>
 * <ul>
 *   <li>{@link pages.MessagingPage} – Private messaging interface between users.</li>
 *   <li>{@link pages.MessagingPageLauncher} – Standalone launcher for the messaging page.</li>
 *   <li>{@link pages.NotificationsPage} – Displays unread notifications for the current user.</li>
 * </ul>
 *
 * <h2>Administration and Moderation:</h2>
 * <ul>
 *   <li>{@link pages.AdminRequestsPage} – Displays and manages user-submitted admin requests.</li>
 *   <li>{@link pages.RequestsPage} – General requests management page.</li>
 *   <li>{@link pages.ModerationHandlingPage} – Interface for handling flagged or moderated content.</li>
 * </ul>
 *
 * <h2>Profiles:</h2>
 * <ul>
 *   <li>{@link pages.ProfilePage} – Displays and edits the current user's profile.</li>
 * </ul>
 *
 * @author CSE360-Team11 Fall 2025
 */
package pages;
