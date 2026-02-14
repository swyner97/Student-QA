package application;

import static org.junit.jupiter.api.Assertions.*;

import model.User;
import model.User.Role;
import pages.ReviewerProfilePage;

import org.junit.jupiter.api.Test;


/**
 * Unit tests for the {@link ReviewerProfilePage}.
 * <p>
 * These tests verify that the profile-editing permission logic behaves correctly
 * based on user role, ownership of the profile, and null-safety requirements.
 * <br><br>
 * The key rule tested:
 * A profile c an be edited only if the logged-in user is the sam person
 * and has a role of {@code User.Role.REVIEWER} or {@code User.Role.ADMIN}
 */
public class ReviewerProfilePageTests {

	User testReviewer = User.createUser(42, "testReviewerUserName", "Password123!", User.Role.REVIEWER, "testReviewerName", "e@mail.com", null);
	User testAdmin = User.createUser(43, "testAdminUserName", "Password123!", User.Role.ADMIN, "testAdminName", "e@mail.com", null);
	User testReviewer2 = User.createUser(44, "testReviewerUserName2", "Password123!", User.Role.REVIEWER, "testReviewerName2", "e@mail.com", null);
	User testStudent = User.createUser(45, "testStudentUserName", "Password123!", User.Role.STUDENT, "testStudentName", "e@mail.com", null);
	
	
	/**
	 * Tests that a reviewer is allowed to edit their own profile.
	 * <p>
	 * This verifies the positive case where the current user and
	 * the profile owner are the same reviewer.
	 */
	@Test
	void canEditProfile_returnsTrue_ReviewerEditingOwnProfile() {
		boolean canEdit = ReviewerProfilePage.canEditProfile(testReviewer, testReviewer);
		assertTrue(canEdit, "Reviewer should be able to edit their own profile.");
	}
	
	
	/**
	 * Tests that an administrator is allowed to edit their own profile.
	 * <p>
	 * Admins have elevated permissions, so they should pass the same
	 * self-editing rule as reviewers.
	 */
	@Test
	void canEditProfile_ReturnsTrue_AdminEditingOwnProfile() {
		boolean canEdit = ReviewerProfilePage.canEditProfile(testAdmin, testAdmin);
		assertTrue(canEdit, "Admin should be able to edit their own profile.");
	}
	
	
	/**
	 * Tests that a reviewer cannot edit someone else's profile.
	 * <p>
	 * Even though both users are reviewers, editing permissions apply
	 * only to one's own profile.
	 */
	@Test
	void canEditProfile_returnsFalse_DifferentUser() {
		boolean canEdit = ReviewerProfilePage.canEditProfile(testReviewer2, testReviewer);
		assertFalse(canEdit, "Reviewer should NOT be able to edit another reviewer's profile.");
	}
	
	
	/**
	 * Tests that the method safely handles null values.
	 * <p>
	 * All combinations of null current user and/or null reviewer
	 * should return {@code false} and must not throw exceptions. 
	 */
	@Test
	void canEditProfile_handlesNullUsersSafely() {
		assertFalse(ReviewerProfilePage.canEditProfile(null, testReviewer));
		assertFalse(ReviewerProfilePage.canEditProfile(testReviewer, null));
		assertFalse(ReviewerProfilePage.canEditProfile(null, null));
	}
}
