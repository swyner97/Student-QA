package model;

import java.sql.SQLException;
import java.util.List;


import javafx.animation.Timeline;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import logic.*;
import pages.*;


public class NavigationBar extends ToolBar {

    private Button notificationButton;
    private Timeline notificationTimeline;

    public NavigationBar() {
        // Set styling for the navigation bar
        this.setStyle("-fx-background-color: #2c3e50; -fx-padding: 5;");

        // Home button
        Button welcomePageButton = createStyledButton("🏠 Home");
        welcomePageButton.setOnAction(_ -> {
            if (StatusData.primaryStage != null)
                new WelcomeLoginPage().show(StatusData.primaryStage, StatusData.currUser);
        });
        this.getItems().add(welcomePageButton);
        this.getItems().add(new Separator(Orientation.VERTICAL));


        MenuButton searchMenu = createStyledMenuButton("🔍 Search");
        MenuItem searchGeneral = new MenuItem("General Search");
        searchGeneral.setOnAction(_ -> {
            UserQAMenu qaMenuPage = new UserQAMenu();
            qaMenuPage.start(StatusData.primaryStage);
        });
        MenuItem searchQs = new MenuItem("Search Questions");
        searchQs.setOnAction(_ -> {
            SearchPage searchQsPage = new SearchPage();
            searchQsPage.show(StatusData.primaryStage, StatusData.currUser);
        });
        MenuItem searchAs = new MenuItem("Search Answers");
        searchAs.setOnAction(_ -> {
            SearchAsPage searchAsPage = new SearchAsPage();
            searchAsPage.show(StatusData.primaryStage, StatusData.currUser);
        });
        MenuItem searchMyPosts = new MenuItem("My Posts");
        searchMyPosts.setOnAction(_ -> {
            MyPostsPage postsPage = new MyPostsPage();
            Stage postsStage = new Stage();
            postsPage.show(postsStage);
        });
        searchMenu.getItems().addAll(searchGeneral, searchQs, searchAs, new SeparatorMenuItem(), searchMyPosts);
        this.getItems().add(searchMenu);
        this.getItems().add(new Separator(Orientation.VERTICAL));


        notificationButton = createStyledButton("Notifications");
        notificationButton.setOnAction(_ -> {
            if (StatusData.currUser != null && StatusData.primaryStage != null) {
                NotificationsPage notifPage = new NotificationsPage();
                notifPage.show(StatusData.primaryStage, StatusData.currUser);

                updateNotificationButton();
            }
        });
        this.getItems().add(notificationButton);
        this.getItems().add(new Separator(Orientation.VERTICAL));


        // Role-based left-side buttons (Instructor, Staff) — Admin handled below with combined tab
        if (StatusData.currUser != null && StatusData.currUser.getRole() == User.Role.INSTRUCTOR) {
            Button instructorHomePageButton = createStyledButton("Instructor Home");
            instructorHomePageButton.setOnAction(_ -> new InstructorHomePage().show(StatusData.primaryStage));
            Button instructorModerationButton = createStyledButton("Moderation Center");
            instructorModerationButton.setOnAction(_ -> new ModerationHandlingPage(StatusData.databaseHelper, StatusData.currUser).show(StatusData.primaryStage));
            this.getItems().addAll(instructorHomePageButton, instructorModerationButton);
        }

        if (StatusData.currUser != null && (StatusData.currUser.getRole() == User.Role.STAFF)) {
            Button staffModerationButton = createStyledButton("🛡 Staff Moderation");
            staffModerationButton.setOnAction(_ -> {
                StaffPage staffPage = new StaffPage();
                staffPage.show(StatusData.primaryStage);
            });
            this.getItems().add(staffModerationButton);
        }

        // Spacer to push user/menu items to the right (preserves ToolBar look)
        Region spacer = new Region();
        spacer.setMinWidth(Region.USE_PREF_SIZE);
        spacer.setPrefWidth(Region.USE_COMPUTED_SIZE);
        spacer.setMaxWidth(Double.MAX_VALUE);
        this.getItems().add(spacer);

        // ----- ADMIN TAB-----
        if (StatusData.currUser != null && StatusData.currUser.getRole() == User.Role.ADMIN) {
            MenuButton adminMenu = createStyledMenuButton("⚙️ Admin");
            
            MenuItem userMgmt = new MenuItem("User Management");
            userMgmt.setOnAction(_ -> {
            	AdminHomePage homePage = new AdminHomePage();
            	Stage stage = (Stage) this.getScene().getWindow();
            	homePage.show(stage, StatusData.currUser.getUserName());
            });
            
            MenuItem adminRequestsItem = new MenuItem("Admin Requests");
            adminRequestsItem.setOnAction(_ -> {
                AdminRequestsPage requestsPage = new AdminRequestsPage();
                Stage stage = (Stage) this.getScene().getWindow();
                requestsPage.show(stage, StatusData.currUser);
            });

            MenuItem invitationsItem = new MenuItem("Invitations");
            invitationsItem.setOnAction(_ -> {
                InvitationPage inv = new InvitationPage();
                inv.show(StatusData.databaseHelper, StatusData.primaryStage);
            });

            adminMenu.getItems().addAll(userMgmt, new SeparatorMenuItem(), adminRequestsItem, new SeparatorMenuItem(), invitationsItem);

            this.getItems().add(adminMenu);
        }

        // Build the user MenuButton (keeps original MenuButton UI)
        User currentUser = StatusData.currUser;
        String usernameLabel = (currentUser != null) ? currentUser.getUserName() : "Guest";
        MenuButton userMenu = createStyledMenuButton(usernameLabel);

        MenuItem profilePageButton = new MenuItem("👤 Profile");
        profilePageButton.setOnAction(_ -> {
            if (currentUser != null) new ProfilePage().show(StatusData.primaryStage, currentUser);
        });

        MenuItem myQA = new MenuItem("💬 My Q&A");
        myQA.setOnAction(_ -> {
            if (currentUser != null) {
                MyQAPage myQAPage = new MyQAPage();
                myQAPage.show(StatusData.primaryStage, currentUser);
            }
        });

        MenuItem messagesPageItem = new MenuItem("✉️ Messages");
        messagesPageItem.setOnAction(_ -> {
            MessagingPage messagingPage = new MessagingPage();
            messagingPage.show(StatusData.primaryStage);
        });

        MenuItem trustedReviewers = new MenuItem("Trusted Reviewers");
        trustedReviewers.setOnAction(_ -> {
            TrustedReviewersPage trustedPage = new TrustedReviewersPage();
            trustedPage.show(StatusData.primaryStage);
        });

        MenuItem reviewsPage = new MenuItem("⭐ Reviews");
        reviewsPage.setOnAction(_ -> {
            ReviewPage myReviewsPage = new ReviewPage(null); // adjust ctor if needed
            myReviewsPage.show(StatusData.primaryStage, currentUser);
        });

        // Reviews Menu Item - reviewer ranking
        MenuItem reviewerRank = new MenuItem("⭐ Reviewer Ranking");
        reviewerRank.setOnAction(_ -> {
            RankReviewer reviewerRankPage = new RankReviewer();
            reviewerRankPage.show(StatusData.primaryStage);
        });

        // Keep Admin Requests also accessible here for users with permission
        MenuItem adminRequestsInUserMenu = new MenuItem("Admin Requests");
        adminRequestsInUserMenu.setOnAction(_ -> {
            if (StatusData.currUser == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Not signed in.");
                alert.showAndWait();
                return;
            }
            User.Role role = StatusData.currUser.getRole();
            if (role == User.Role.INSTRUCTOR || role == User.Role.STAFF || role == User.Role.ADMIN) {
                AdminRequestsPage requestsPage = new AdminRequestsPage();
                Stage stage = (Stage) this.getScene().getWindow();
                requestsPage.show(stage, StatusData.currUser);
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Access Denied");
                alert.setContentText("Only instructors, staff, and admins can access admin requests.");
                alert.showAndWait();
            }
        });

        userMenu.getItems().addAll(
            profilePageButton,
            new SeparatorMenuItem(),
            myQA,
            new SeparatorMenuItem(),
            messagesPageItem,
            new SeparatorMenuItem(),
            trustedReviewers,
            new SeparatorMenuItem(),
            reviewsPage,
            reviewerRank,
            new SeparatorMenuItem(),
            adminRequestsInUserMenu
        );

        this.getItems().add(userMenu);
        this.getItems().add(new Separator(Orientation.VERTICAL));

        // Request Reviewer MenuButton (same UI as first code)
        if (currentUser != null) {
            try {
                List<String> userRoles = StatusData.databaseHelper.allUserRoles(currentUser.getUserName());
                boolean isReviewer = userRoles != null && userRoles.contains(User.Role.REVIEWER.name());
                boolean isInstructor = userRoles != null && userRoles.contains(User.Role.INSTRUCTOR.name());
                boolean isAdmin = userRoles != null && userRoles.contains(User.Role.ADMIN.name());
                boolean isStudent = userRoles != null && userRoles.contains(User.Role.STUDENT.name());
                boolean isStaff = userRoles != null && userRoles.contains(User.Role.STAFF.name());

                if (!isReviewer && !isInstructor && !isAdmin && !isStaff) {
                    MenuButton requestButton = createStyledMenuButton("Think you're ready to review?");
                    requestButton.setStyle("-fx-background-color: #345e49; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 15 5 15; -fx-background-radius: 3;");
                    requestButton.setOnMouseEntered(_ ->
                        requestButton.setStyle("-fx-background-color: #4f8f6f; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 15 5 15; -fx-background-radius: 3;")
                    );
                    requestButton.setOnMouseExited(_ ->
                        requestButton.setStyle("-fx-background-color: #345e49; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 15 5 15; -fx-background-radius: 3;")
                    );

                    MenuItem requestReviewer = new MenuItem("Request reviewer access");
                    requestReviewer.setOnAction(_ -> {
                        try {
                            StatusData.databaseHelper.reviewerRequest(currentUser.getUserName());
                            System.out.println("Request has been sent!");
                            Alert sent = new Alert(Alert.AlertType.INFORMATION, "Reviewer request sent.");
                            sent.showAndWait();
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                            Alert err = new Alert(Alert.AlertType.ERROR, "Failed to send request: " + ex.getMessage());
                            err.showAndWait();
                        }
                    });
                    requestButton.getItems().add(requestReviewer);

                    this.getItems().add(requestButton);
                    this.getItems().add(new Separator(Orientation.VERTICAL));
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        // Logout Button (right side)
        Button logOutButton = createStyledButton("🚪 Logout");
        logOutButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 15 5 15; -fx-background-radius: 3;");
        logOutButton.setOnMouseEntered(_ ->
            logOutButton.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 15 5 15; -fx-background-radius: 3;")
        );
        logOutButton.setOnMouseExited(_ ->
            logOutButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 15 5 15; -fx-background-radius: 3;")
        );
        logOutButton.setOnAction(_ -> {
            try {
                if (StatusData.databaseHelper != null) {
                    StatusData.databaseHelper.closeConnection();
                    StatusData.databaseHelper.connectToDatabase();
                }
                StatusData.currUser = null;
            } catch (SQLException e) { /* ignore */ }
            new InitialAccessPage(StatusData.databaseHelper).show(StatusData.primaryStage);
        });
        this.getItems().add(logOutButton);
    }

    // Check for notifications and update button
    private void updateNotificationButton() {
        if (notificationButton == null || StatusData.currUser == null) return;

        try {
            int unreadCount = StatusData.databaseHelper.getUnreadNotificationCount(StatusData.currUser.getId());

            if (unreadCount > 0) {
                // Yellow button with count
                notificationButton.setText("🔔 " + unreadCount);
                notificationButton.setStyle(
                    "-fx-background-color: #f39c12; -fx-text-fill: white; " +
                    "-fx-font-weight: bold; -fx-padding: 5 15 5 15; -fx-background-radius: 3;"
                );
                notificationButton.setOnMouseEntered(_ ->
                    notificationButton.setStyle(
                        "-fx-background-color: #e67e22; -fx-text-fill: white; " +
                        "-fx-font-weight: bold; -fx-padding: 5 15 5 15; -fx-background-radius: 3;"
                    )
                );
                notificationButton.setOnMouseExited(_ ->
                    notificationButton.setStyle(
                        "-fx-background-color: #f39c12; -fx-text-fill: white; " +
                        "-fx-font-weight: bold; -fx-padding: 5 15 5 15; -fx-background-radius: 3;"
                    )
                );
            } else {
                // Normal button
                notificationButton.setText("🔔 Notifications");
                notificationButton.setStyle(
                    "-fx-background-color: #34495e; -fx-text-fill: white; " +
                    "-fx-font-weight: bold; -fx-padding: 5 15 5 15; -fx-background-radius: 3;"
                );
                notificationButton.setOnMouseEntered(_ ->
                    notificationButton.setStyle(
                        "-fx-background-color: #1abc9c; -fx-text-fill: white; " +
                        "-fx-font-weight: bold; -fx-padding: 5 15 5 15; -fx-background-radius: 3;"
                    )
                );
                notificationButton.setOnMouseExited(_ ->
                    notificationButton.setStyle(
                        "-fx-background-color: #34495e; -fx-text-fill: white; " +
                        "-fx-font-weight: bold; -fx-padding: 5 15 5 15; -fx-background-radius: 3;"
                    )
                );
            }
        } catch (Exception e) {
            System.err.println("Error updating notification button: " + e.getMessage());
        }
    }


    // Styling helpers
    private Button createStyledButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: #34495e; -fx-text-fill: white; " +
                       "-fx-font-weight: bold; -fx-padding: 5 15 5 15; -fx-background-radius: 3;");
        button.setOnMouseEntered(_ ->
            button.setStyle("-fx-background-color: #1abc9c; -fx-text-fill: white; " +
                           "-fx-font-weight: bold; -fx-padding: 5 15 5 15; -fx-background-radius: 3;"));
        button.setOnMouseExited(_ ->
            button.setStyle("-fx-background-color: #34495e; -fx-text-fill: white; " +
                           "-fx-font-weight: bold; -fx-padding: 5 15 5 15; -fx-background-radius: 3;"));
        return button;
    }

    private MenuButton createStyledMenuButton(String text) {
        MenuButton menuButton = new MenuButton(text);
        menuButton.setStyle("-fx-background-color: #34495e; -fx-text-fill: white; " +
                           "-fx-font-weight: bold; -fx-padding: 5 15 5 15; -fx-background-radius: 3;");
        menuButton.setOnMouseEntered(_ ->
            menuButton.setStyle("-fx-background-color: #1abc9c; -fx-text-fill: white; " +
                               "-fx-font-weight: bold; -fx-padding: 5 15 5 15; -fx-background-radius: 3;"));
        menuButton.setOnMouseExited(_ ->
            menuButton.setStyle("-fx-background-color: #34495e; -fx-text-fill: white; " +
                               "-fx-font-weight: bold; -fx-padding: 5 15 5 15; -fx-background-radius: 3;"));
        return menuButton;
    }

    /**
     * Stop background tasks (timers) started by this navigation bar.
     * Call this from the window/controller teardown to avoid background threads running.
     */
    public void stopBackgroundTasks() {
        if (notificationTimeline != null) {
            notificationTimeline.stop();
            notificationTimeline = null;
        }
    }
}
