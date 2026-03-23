package pages;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import logic.*;
import model.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.geometry.*;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * The WelcomeLoginPage class displays a welcome screen for authenticated users.
 * It allows users to navigate to their respective pages based on their role or quit the application.
 */
public class WelcomeLoginPage {
    private Questions questions;

    private TableView<Question> questionTable;
    private TableView<Clarification> suggestionsTable;
    private TableView<Question> followUpTable;
    private TextArea questionDetails;
    private TextArea followUpDetails;
    private TextArea editDetails;
    private VBox editsBox = new VBox(10);

    private Stage stage;
    private User user;

    // Form fields
    private TextField titleField;
    private TextArea descriptionArea;

    @SuppressWarnings({ "unchecked", "unused", "deprecation" })
    public void show(Stage stage, User user) {

        // debug
        try {
            System.out.println("All UserRoles: " + StatusData.databaseHelper.allUserRoles(StatusData.currUser.getUserName()));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        this.stage = stage;
        this.user = user;

        questions = new Questions(StatusData.databaseHelper);

        stage.setTitle("Home");

        BorderPane mainPane = new BorderPane();

        // Navigation bar
        NavigationBar navBar = new NavigationBar();
        mainPane.setTop(navBar);

        // Center content
        BorderPane content = new BorderPane();
        content.setPadding(new Insets(15));

        // Create Question Form (collapsible)
        VBox createQuestionBox = createQuestionForm();

        // Initialize table — create a NEW table each time
        questionTable = new TableView<>();
        questionTable.setMinHeight(200);
        VBox.setVgrow(questionTable, Priority.ALWAYS);

        // Top: Table
        VBox tableBox = new VBox(10);
        VBox.setVgrow(tableBox, Priority.ALWAYS);

        Label tableTitle = new Label("Questions");
        tableTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        HBox tableHeader = new HBox(10, tableTitle);
        tableHeader.setAlignment(Pos.CENTER_LEFT);

        TableColumn<Question, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("questionId"));
        idCol.setPrefWidth(50);

        TableColumn<Question, String> authorCol = new TableColumn<>("Author");
        authorCol.setCellValueFactory(new PropertyValueFactory<>("author"));
        authorCol.setPrefWidth(120);

        TableColumn<Question, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol.setPrefWidth(300);

        TableColumn<Question, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cellData ->
            new ReadOnlyObjectWrapper<>(cellData.getValue().getStatusText())
        );
        statusCol.setPrefWidth(120);

        TableColumn<Question, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cellData -> {
            String timestamp = cellData.getValue().getTimestamp();
            return new SimpleStringProperty(timestamp != null ? timestamp : "N/A");
        });
        dateCol.setPrefWidth(160);

        // Answers column
        TableColumn<Question, Void> actionCol = new TableColumn<>("Answers");
        actionCol.setPrefWidth(110);
        actionCol.setCellFactory(_ -> new TableCell<>() {
            private final Button viewAnswersBtn = new Button("View Answers");
            private final HBox actionBox = new HBox(5, viewAnswersBtn);

            {
                viewAnswersBtn.setOnAction(_ -> {
                    Question question = getTableView().getItems().get(getIndex());
                    showAnswersPage(question);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : actionBox);
            }
        });

        // Clarification column
        TableColumn<Question, Void> clarifyCol = new TableColumn<>("Suggestions");
        clarifyCol.setPrefWidth(120);
        clarifyCol.setCellFactory(_ -> new TableCell<>() {
            private final Button suggestClarificationBtn = new Button("Suggest\nClarification");
            private final HBox suggestClarification = new HBox(5, suggestClarificationBtn);

            {
                suggestClarificationBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                suggestClarificationBtn.setWrapText(true);
                suggestClarificationBtn.setPrefWidth(90);
                suggestClarificationBtn.setPrefHeight(25);
                suggestClarificationBtn.setAlignment(Pos.CENTER);
                suggestClarificationBtn.setOnAction(_ -> {
                    Question question = getTableView().getItems().get(getIndex());
                    showClarificationPopup(question);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : suggestClarification);
            }
        });

        // Edit column
        TableColumn<Question, Void> editCol = new TableColumn<>("Edit");
        editCol.setPrefWidth(90);
        editCol.setCellFactory(_ -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final HBox actionBox = new HBox(5, editBtn);

            {
                editBtn.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white;");
                editBtn.setOnAction(_ -> {
                    Question question = getTableView().getItems().get(getIndex());
                    WelcomeLoginPage.this.showEditQuestionPage(question);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(null);
                setText(null);
                if (empty) return;

                Question question = getTableView().getItems().get(getIndex());
                if (question == null || user == null) return;

                String role = (user.getRole() != null) ? user.getRole().name().toLowerCase() : "";
                String author = (question.getAuthor() != null) ? question.getAuthor().trim().toLowerCase() : "";
                String name = (user.getName() != null) ? user.getName().trim().toLowerCase() : "";
                String username = (user.getUserName() != null) ? user.getUserName().trim().toLowerCase() : "";

                boolean isPrivileged = role.contains("admin") || role.contains("staff") ||
                        role.contains("instructor") || role.contains("teacher") ||
                        role.contains("reviewer") || role.contains("ta");
                boolean isAuthor = author.equals(name) || author.equals(username) || question.getUserId() == user.getId();

                setGraphic((isAuthor || isPrivileged) ? actionBox : null);
            }
        });

        // Flag column
        TableColumn<Question, Void> flagCol = new TableColumn<>("Flag");
        flagCol.setPrefWidth(60);
        flagCol.setCellFactory(_ -> new TableCell<>() {
            private final Button flagBtn = new Button("🏳️");

            {
                flagBtn.setStyle("-fx-font-size: 14px; -fx-text-fill: green; -fx-background-color: transparent; -fx-cursor: hand;");
                flagBtn.setOnAction(_ -> {
                    Object item = getTableRow().getItem();
                    if (item == null) return;

                    ContentType contentType;
                    int itemId;
                    if (item instanceof Question q) {
                        contentType = ContentType.QUESTION;
                        itemId = q.getQuestionId();
                    } else if (item instanceof Answer a) {
                        contentType = ContentType.ANSWER;
                        itemId = a.getAnswerId();
                    } else if (item instanceof Review r) {
                        contentType = ContentType.REVIEW;
                        itemId = r.getReviewId();
                    } else if (item instanceof Clarification c) {
                        contentType = ContentType.SUGGESTION;
                        itemId = c.getId();
                    } else {
                        return;
                    }

                    try {
                        List<ModerationFlag> existingFlags = StatusData.databaseHelper.loadAllModerationFlags();
                        ModerationFlag userFlag = existingFlags.stream()
                                .filter(f -> f.getItemType().equalsIgnoreCase(contentType.name()) &&
                                        f.getItemId() == itemId &&
                                        f.getStaffId() == StatusData.currUser.getId() &&
                                        f.getStatus().equalsIgnoreCase("OPEN"))
                                .findFirst()
                                .orElse(null);

                        if (userFlag != null) {
                            StatusData.databaseHelper.updateModerationFlagStatus(userFlag.getFlagId(), "CLOSED");
                            showAlert(Alert.AlertType.INFORMATION, "Unflagged", "You have removed your flag.");
                        } else {
                            showFlagPopup(contentType, itemId);
                        }

                        WelcomeLoginPage.this.reloadQuestions();
                        questionTable.refresh();
                        updateFlagButtonStyle(item);

                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        showAlert(Alert.AlertType.ERROR, "Error", "Flag toggle failed: " + ex.getMessage());
                    }
                });
            }

            private void updateFlagButtonStyle(Object item) {
                int itemId;
                ContentType type;

                if (item instanceof Question q) { type = ContentType.QUESTION; itemId = q.getQuestionId(); }
                else if (item instanceof Answer a) { type = ContentType.ANSWER; itemId = a.getAnswerId(); }
                else if (item instanceof Review r) { type = ContentType.REVIEW; itemId = r.getReviewId(); }
                else if (item instanceof Clarification c) { type = ContentType.SUGGESTION; itemId = c.getId(); }
                else { return; }

                final int finalItemId = itemId;
                final String finalItemType = type.name();

                try {
                    List<ModerationFlag> flags = StatusData.databaseHelper.loadAllModerationFlags();
                    boolean isFlagged = flags.stream().anyMatch(f ->
                            f.getItemType().equalsIgnoreCase(finalItemType) &&
                            f.getItemId() == finalItemId &&
                            f.getStatus().equalsIgnoreCase("OPEN"));
                    boolean canSee = flags.stream().anyMatch(f ->
                            f.getItemType().equalsIgnoreCase(finalItemType) &&
                            f.getItemId() == finalItemId &&
                            canSeeFlag(StatusData.currUser, List.of(f)));

                    flagBtn.setStyle(isFlagged && canSee
                            ? "-fx-background-color: #ffcccc; -fx-text-fill: red; -fx-font-size: 14px;"
                            : "-fx-background-color: transparent; -fx-text-fill: green; -fx-font-size: 14px;");
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    updateFlagButtonStyle(getTableRow().getItem());
                    setGraphic(flagBtn);
                }
            }
        });

        questionTable.getColumns().addAll(flagCol, idCol, authorCol, titleCol, statusCol, dateCol, actionCol, clarifyCol, editCol);
        questionTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        questionTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Question question, boolean empty) {
                super.updateItem(question, empty);
                if (empty || question == null) { setStyle(""); return; }
                try {
                    List<ModerationFlag> flags = StatusData.databaseHelper.loadAllModerationFlags().stream()
                            .filter(f -> f.getItemType().equalsIgnoreCase("question"))
                            .filter(f -> f.getItemId() == question.getQuestionId())
                            .filter(f -> f.getStatus().equalsIgnoreCase("OPEN"))
                            .collect(Collectors.toList());
                    setStyle(!flags.isEmpty() && canSeeFlag(StatusData.currUser, flags)
                            ? "-fx-background-color: #ffcccc;"
                            : "");
                } catch (SQLException e) {
                    e.printStackTrace();
                    setStyle("");
                }
            }
        });

        questionTable.getSelectionModel().selectedItemProperty().addListener((_, _, newVal) -> {
            if (newVal != null) {
                displayQuestionDetails(newVal);
                displayEditDetails(newVal);
                loadSuggestionsForQuestion(newVal);
                loadFollowUpsForQuestion(newVal);
            }
        });

        tableBox.getChildren().addAll(tableHeader, questionTable);

        // --------- Bottom: Details with Tabs ----------
        VBox detailsBox = new VBox(10);
        detailsBox.setPadding(new Insets(10, 0, 0, 0));
        VBox.setVgrow(detailsBox, Priority.ALWAYS);

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setMinHeight(250);
        VBox.setVgrow(tabPane, Priority.ALWAYS);
        tabPane.setStyle("-fx-background-color: white; -fx-border-color: #2196f3; -fx-border-radius: 5; -fx-background-radius: 5;");

        // Question Details Tab
        questionDetails = new TextArea();
        questionDetails.setEditable(false);
        questionDetails.setWrapText(true);
        questionDetails.setPrefRowCount(8);
        questionDetails.setPromptText("Select a question to view details...");

        ScrollPane detailsScroll = new ScrollPane(questionDetails);
        detailsScroll.setFitToWidth(true);
        detailsScroll.setFitToHeight(true);
        detailsScroll.setStyle("-fx-background-color: transparent;");
        HBox.setHgrow(detailsScroll, Priority.ALWAYS);

        editsBox.setPadding(new Insets(10));
        editsBox.setMinWidth(200);
        editsBox.setMaxWidth(300);
        HBox.setHgrow(editsBox, Priority.NEVER);

        HBox detailsContent = new HBox(10, detailsScroll, editsBox);
        detailsContent.setPadding(new Insets(10));
        detailsContent.setMinHeight(250);
        Tab detailsTab = new Tab("Question Details", detailsContent);

        // Suggestions Tab
        suggestionsTable = new TableView<>();
        VBox.setVgrow(suggestionsTable, Priority.ALWAYS);

        TableColumn<Clarification, Void> flag2Col = new TableColumn<>("Flag");
        flag2Col.setPrefWidth(60);
        flag2Col.setCellFactory(col -> new TableCell<>() {
            private final Button flagBtn = new Button("🏳️");

            {
                flagBtn.setStyle("-fx-font-size: 14px; -fx-text-fill: green; -fx-background-color: transparent; -fx-cursor: hand;");
                flagBtn.setOnAction(e -> {
                    Object item = getTableRow().getItem();
                    if (item == null) return;

                    ContentType contentType;
                    int itemId;
                    if (item instanceof Question q) { contentType = ContentType.QUESTION; itemId = q.getQuestionId(); }
                    else if (item instanceof Answer a) { contentType = ContentType.ANSWER; itemId = a.getAnswerId(); }
                    else if (item instanceof Review r) { contentType = ContentType.REVIEW; itemId = r.getReviewId(); }
                    else if (item instanceof Clarification c) { contentType = ContentType.SUGGESTION; itemId = c.getId(); }
                    else { return; }

                    try {
                        List<ModerationFlag> existingFlags = StatusData.databaseHelper.loadAllModerationFlags();
                        ModerationFlag userFlag = existingFlags.stream()
                                .filter(f -> f.getItemType().equalsIgnoreCase(contentType.name()) &&
                                        f.getItemId() == itemId &&
                                        f.getStaffId() == StatusData.currUser.getId() &&
                                        f.getStatus().equalsIgnoreCase("OPEN"))
                                .findFirst().orElse(null);

                        if (userFlag != null) {
                            StatusData.databaseHelper.updateModerationFlagStatus(userFlag.getFlagId(), "CLOSED");
                            showAlert(Alert.AlertType.INFORMATION, "Unflagged", "You have removed your flag.");
                        } else {
                            showFlagPopup(contentType, itemId);
                        }

                        WelcomeLoginPage.this.reloadQuestions();
                        suggestionsTable.refresh();
                        updateFlagButtonStyle(item);

                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        showAlert(Alert.AlertType.ERROR, "Error", "Flag toggle failed: " + ex.getMessage());
                    }
                });
            }

            private void updateFlagButtonStyle(Object item) {
                int itemId;
                ContentType type;
                if (item instanceof Question q) { type = ContentType.QUESTION; itemId = q.getQuestionId(); }
                else if (item instanceof Answer a) { type = ContentType.ANSWER; itemId = a.getAnswerId(); }
                else if (item instanceof Review r) { type = ContentType.REVIEW; itemId = r.getReviewId(); }
                else if (item instanceof Clarification c) { type = ContentType.SUGGESTION; itemId = c.getId(); }
                else { return; }

                final int finalItemId = itemId;
                final String finalItemType = type.name();

                try {
                    List<ModerationFlag> flags = StatusData.databaseHelper.loadAllModerationFlags();
                    boolean isFlagged = flags.stream().anyMatch(f ->
                            f.getItemType().equalsIgnoreCase(finalItemType) &&
                            f.getItemId() == finalItemId &&
                            f.getStatus().equalsIgnoreCase("OPEN"));
                    boolean canSee = flags.stream().anyMatch(f ->
                            f.getItemType().equalsIgnoreCase(finalItemType) &&
                            f.getItemId() == finalItemId &&
                            canSeeFlag(StatusData.currUser, List.of(f)));

                    flagBtn.setStyle(isFlagged && canSee
                            ? "-fx-background-color: #ffcccc; -fx-text-fill: red; -fx-font-size: 14px;"
                            : "-fx-background-color: transparent; -fx-text-fill: green; -fx-font-size: 14px;");
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    updateFlagButtonStyle(getTableRow().getItem());
                    setGraphic(flagBtn);
                }
            }
        });

        TableColumn<Clarification, Void> author2Col = new TableColumn<>("User");
        author2Col.setCellValueFactory(new PropertyValueFactory<>("author"));
        author2Col.setPrefWidth(130);

        TableColumn<Clarification, Void> contentCol = new TableColumn<>("Suggestion");
        contentCol.setCellValueFactory(new PropertyValueFactory<>("content"));
        contentCol.setPrefWidth(350);

        suggestionsTable.getColumns().addAll(flag2Col, author2Col, contentCol);
        suggestionsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        Tab suggestionsTab = new Tab("View Suggestions", suggestionsTable);

        // Follow Up Tab
        followUpTable = new TableView<>();
        VBox.setVgrow(followUpTable, Priority.ALWAYS);

        followUpDetails = new TextArea();
        followUpDetails.setWrapText(true);
        followUpDetails.setEditable(false);
        followUpDetails.setPrefRowCount(8);
        followUpDetails.setPromptText("Select a question to view details...");

        ScrollPane followUpDetailsScroll = new ScrollPane(followUpDetails);
        followUpDetailsScroll.setFitToWidth(true);
        followUpDetailsScroll.setFitToHeight(true);
        followUpDetailsScroll.setStyle("-fx-background-color: transparent;");
        HBox.setHgrow(followUpDetailsScroll, Priority.ALWAYS);

        TableColumn<Question, Void> followUpflagCol = new TableColumn<>("Flag");
        followUpflagCol.setPrefWidth(60);
        followUpflagCol.setCellFactory(col -> new TableCell<>() {
            private final Button flagBtn = new Button("🏳️");

            {
                flagBtn.setStyle("-fx-font-size: 14px; -fx-text-fill: green; -fx-background-color: transparent; -fx-cursor: hand;");
                flagBtn.setOnAction(e -> {
                    Object item = getTableRow().getItem();
                    if (item == null) return;

                    ContentType contentType;
                    int itemId;
                    if (item instanceof Question q) { contentType = ContentType.QUESTION; itemId = q.getQuestionId(); }
                    else if (item instanceof Answer a) { contentType = ContentType.ANSWER; itemId = a.getAnswerId(); }
                    else if (item instanceof Review r) { contentType = ContentType.REVIEW; itemId = r.getReviewId(); }
                    else if (item instanceof Clarification c) { contentType = ContentType.SUGGESTION; itemId = c.getId(); }
                    else { return; }

                    try {
                        List<ModerationFlag> existingFlags = StatusData.databaseHelper.loadAllModerationFlags();
                        ModerationFlag userFlag = existingFlags.stream()
                                .filter(f -> f.getItemType().equalsIgnoreCase(contentType.name()) &&
                                        f.getItemId() == itemId &&
                                        f.getStaffId() == StatusData.currUser.getId() &&
                                        f.getStatus().equalsIgnoreCase("OPEN"))
                                .findFirst().orElse(null);

                        if (userFlag != null) {
                            StatusData.databaseHelper.updateModerationFlagStatus(userFlag.getFlagId(), "CLOSED");
                            showAlert(Alert.AlertType.INFORMATION, "Unflagged", "You have removed your flag.");
                        } else {
                            showFlagPopup(contentType, itemId);
                        }

                        WelcomeLoginPage.this.reloadQuestions();
                        followUpTable.refresh();
                        updateFlagButtonStyle(item);

                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        showAlert(Alert.AlertType.ERROR, "Error", "Flag toggle failed: " + ex.getMessage());
                    }
                });
            }

            private void updateFlagButtonStyle(Object item) {
                int itemId;
                ContentType type;
                if (item instanceof Question q) { type = ContentType.QUESTION; itemId = q.getQuestionId(); }
                else if (item instanceof Answer a) { type = ContentType.ANSWER; itemId = a.getAnswerId(); }
                else if (item instanceof Review r) { type = ContentType.REVIEW; itemId = r.getReviewId(); }
                else if (item instanceof Clarification c) { type = ContentType.SUGGESTION; itemId = c.getId(); }
                else { return; }

                final int finalItemId = itemId;
                final String finalItemType = type.name();

                try {
                    List<ModerationFlag> flags = StatusData.databaseHelper.loadAllModerationFlags();
                    boolean isFlagged = flags.stream().anyMatch(f ->
                            f.getItemType().equalsIgnoreCase(finalItemType) &&
                            f.getItemId() == finalItemId &&
                            f.getStatus().equalsIgnoreCase("OPEN"));
                    boolean canSee = flags.stream().anyMatch(f ->
                            f.getItemType().equalsIgnoreCase(finalItemType) &&
                            f.getItemId() == finalItemId &&
                            canSeeFlag(StatusData.currUser, List.of(f)));

                    flagBtn.setStyle(isFlagged && canSee
                            ? "-fx-background-color: #ffcccc; -fx-text-fill: red; -fx-font-size: 14px;"
                            : "-fx-background-color: transparent; -fx-text-fill: green; -fx-font-size: 14px;");
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    updateFlagButtonStyle(getTableRow().getItem());
                    setGraphic(flagBtn);
                }
            }
        });

        TableColumn<Question, String> followUpIdCol = new TableColumn<>("ID");
        followUpIdCol.setCellValueFactory(cellData -> {
            Question q = cellData.getValue();
            String displayId = q.getFollowUp() == 0
                    ? String.valueOf(q.getQuestionId())
                    : q.getFollowUp() + "." + q.getQuestionId();
            return new SimpleStringProperty(displayId);
        });
        followUpIdCol.setPrefWidth(50);

        TableColumn<Question, String> followUpTitleCol = new TableColumn<>("Title");
        followUpTitleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        followUpTitleCol.setPrefWidth(180);

        TableColumn<Question, String> followUpAuthorCol = new TableColumn<>("Author");
        followUpAuthorCol.setCellValueFactory(new PropertyValueFactory<>("author"));
        followUpAuthorCol.setPrefWidth(130);

        TableColumn<Question, String> followUpDateCol = new TableColumn<>("Timestamp");
        followUpDateCol.setCellValueFactory(cellData -> {
            String timestamp = cellData.getValue().getTimestamp();
            return new SimpleStringProperty(timestamp != null ? timestamp : "N/A");
        });
        followUpDateCol.setPrefWidth(130);

        followUpTable.getColumns().addAll(followUpflagCol, followUpIdCol, followUpTitleCol, followUpAuthorCol, followUpDateCol);
        followUpTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        followUpTable.setPlaceholder(new Label("No follow-up questions available."));
        followUpTable.setRowFactory(tv -> {
            TableRow<Question> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 2) {
                    displayFollowUpQuestionDetails(row.getItem());
                }
            });
            return row;
        });

        HBox followUpTabContent = new HBox(10, followUpTable, followUpDetailsScroll);
        followUpTabContent.setPadding(new Insets(10));
        followUpTabContent.setMinHeight(250);
        HBox.setHgrow(followUpTable, Priority.ALWAYS);

        Tab followUpTab = new Tab("Follow Up Questions", followUpTabContent);

        tabPane.getTabs().addAll(detailsTab, suggestionsTab, followUpTab);

        // Action buttons
        Button makePublicBtn = new Button("Make Public");
        makePublicBtn.setOnAction(e -> {
            Clarification sel = suggestionsTable.getSelectionModel().getSelectedItem();
            Question q = questionTable.getSelectionModel().getSelectedItem();
            if (sel == null || q == null) {
                showAlert(Alert.AlertType.WARNING, "No Suggestion Selected", "Please select a suggestion or question");
                return;
            }
            try {
                StatusData.databaseHelper.makeClarificationPublic(sel.getId());
                showAlert(Alert.AlertType.INFORMATION, "Success", "Suggestion made public");
                loadSuggestionsForQuestion(q);
            } catch (SQLException ex) {
                ex.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Could not make suggestion public: " + ex.getMessage());
            }
        });

        Question question = questionTable.getSelectionModel().getSelectedItem();
        if (question != null && (isPrivilegedRole(user) || user.getId() == question.getUserId())) {
            detailsBox.getChildren().add(makePublicBtn);
        }

        Button suggestClarificationBtn = new Button("Suggest Clarification");
        suggestClarificationBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        suggestClarificationBtn.setOnAction(_ -> {
            Question selected = questionTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.INFORMATION, "No Question Selected", "Please select a question.");
                return;
            }
            showClarificationPopup(selected);
        });

        Button followUpBtn = new Button("Ask FollowUp");
        followUpBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        followUpBtn.setOnAction(_ -> {
            Question selected = questionTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.INFORMATION, "No Question Selected", "Please select a question.");
                return;
            }
            showFollowUpPopup(selected);
        });

        Button privateMsgBtn = new Button("Private Message Author");
        privateMsgBtn.setStyle("-fx-background-color: #f0ad4e; -fx-text-fill: black; -fx-font-weight: bold;");
        privateMsgBtn.setOnAction(_ -> {
            Question selected = questionTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.INFORMATION, "No Question Selected", "Please select a question.");
                return;
            }

            int authorId = selected.getUserId();
            User author;
            try {
                author = StatusData.databaseHelper.getUserById(authorId);
                if (author == null) {
                    showAlert(Alert.AlertType.ERROR, "Author Not Found", "Author could not be found.");
                    return;
                }
            } catch (SQLException ex) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Could not retrieve author: " + ex.getMessage());
                return;
            }

            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Send Private Message");
            dialog.setHeaderText("Send a private message to: " + author.getUserName());
            dialog.setContentText("Enter your message:");
            dialog.initOwner(stage);
            dialog.showAndWait().ifPresent(messageText -> {
                if (messageText.trim().isEmpty()) {
                    showAlert(Alert.AlertType.WARNING, "Empty Message", "Message cannot be empty.");
                    return;
                }
                Messages message = new Messages(user.getId(), author.getId(), messageText);
                message.setSenderName(user.getUserName());
                message.setRecipientName(author.getUserName());
                StatusData.databaseHelper.sendMessage(message);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Message sent to " + author.getUserName());
            });
        });

        HBox buttonBox = new HBox(10, suggestClarificationBtn, followUpBtn, privateMsgBtn);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(5, 0, 0, 0));

        detailsBox.getChildren().addAll(tabPane, buttonBox);

        // Combine all sections
        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(10));
        VBox.setVgrow(tableBox, Priority.ALWAYS);
        VBox.setVgrow(detailsBox, Priority.ALWAYS);
        mainContent.getChildren().addAll(createQuestionBox, tableBox, detailsBox);

        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        content.setCenter(scrollPane);
        mainPane.setCenter(content);

        StatusData.setScene(stage, mainPane);

        questionTable.getSelectionModel().selectedItemProperty().addListener((_, _, newVal) -> {
            if (newVal != null) {
                displayQuestionDetails(newVal);
                displayEditDetails(newVal);
                loadSuggestionsForQuestion(newVal);
                loadFollowUpsForQuestion(newVal);
            }
        });

        loadAllQuestions();
    }

    @SuppressWarnings("unused")
    private VBox createQuestionForm() {
        VBox formBox = new VBox(10);
        formBox.setPadding(new Insets(15));
        formBox.setStyle("-fx-background-color: #e3f2fd; -fx-border-color: #2196f3; -fx-border-width: 1; -fx-border-radius: 5; -fx-background-radius: 5;");

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label formTitle = new Label("Create New Question");
        formTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Button toggleButton = new Button("▼ Hide");
        toggleButton.setStyle("-fx-font-size: 10px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(formTitle, spacer, toggleButton);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10, 0, 0, 0));

        Label authorLabel = new Label("Author:");
        Label authorValue = new Label(user != null ? user.getName() : "");
        authorValue.setStyle("-fx-font-weight: bold;");

        Label titleLabel = new Label("Title:");
        titleField = new TextField();
        titleField.setPromptText("Enter question title...");
        titleField.setPrefWidth(500);

        Label descLabel = new Label("Description:");
        descriptionArea = new TextArea();
        descriptionArea.setPromptText("Enter question description...");
        descriptionArea.setPrefRowCount(4);
        descriptionArea.setPrefWidth(500);
        descriptionArea.setWrapText(true);

        grid.add(authorLabel, 0, 0);
        grid.add(authorValue, 1, 0);
        grid.add(titleLabel, 0, 1);
        grid.add(titleField, 1, 1);
        grid.add(descLabel, 0, 2);
        grid.add(descriptionArea, 0, 3, 2, 1);

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        Button submitButton = new Button("Post Question");
        submitButton.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20;");

        Button clearButton = new Button("Clear");
        clearButton.setStyle("-fx-padding: 8 20;");

        submitButton.setOnAction(_ -> createQuestion());
        clearButton.setOnAction(_ -> clearForm());

        buttonBox.getChildren().addAll(clearButton, submitButton);

        VBox formContent = new VBox(10);
        formContent.getChildren().addAll(grid, buttonBox);
        formContent.setVisible(false);
        formContent.setManaged(false);
        toggleButton.setText("▶ Show");

        toggleButton.setOnAction(_ -> {
            if (formContent.isVisible()) {
                formContent.setVisible(false);
                formContent.setManaged(false);
                toggleButton.setText("▶ Show");
            } else {
                formContent.setVisible(true);
                formContent.setManaged(true);
                toggleButton.setText("▼ Hide");
            }
        });

        formBox.getChildren().addAll(header, formContent);
        return formBox;
    }

    private void createQuestion() {
        String title = titleField.getText().trim();
        String description = descriptionArea.getText().trim();

        if (title.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter a title for your question.");
            return;
        }
        if (description.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter a description for your question.");
            return;
        }

        Result result = questions.create(user.getId(), user.getName(), title, description, null, 0);

        if (result.isSuccess()) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Your question has been posted!");
            clearForm();
            loadAllQuestions();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", result.getMessage());
        }
    }

    private void clearForm() {
        titleField.clear();
        descriptionArea.clear();
    }

    private void loadAllQuestions() {
        List<Question> allQuestions = StatusData.databaseHelper.loadAllQs();
        questionTable.getItems().setAll(allQuestions);

        List<Question> followUps = allQuestions.stream()
                .filter(q -> q.getFollowUp() != 0)
                .collect(Collectors.toList());

        if (followUpTable != null) {
            followUpTable.getItems().setAll(followUps);
        }
    }

    public void reloadQuestions() {
        if (questionTable != null) {
            loadAllQuestions();
        }
    }

    private void displayQuestionDetails(Question q) {
        StringBuilder details = new StringBuilder();
        details.append("Question ID: ").append(q.getQuestionId()).append("\n");
        details.append("Author: ").append(q.getAuthor()).append("\n");
        details.append("Title: ").append(q.getTitle()).append("\n");
        details.append("Status: ").append(q.getStatusText()).append("\n");
        details.append("Timestamp: ").append(q.getTimestamp() != null ? q.getTimestamp() : "N/A").append("\n");
        details.append("\nDescription:\n").append(q.getDescription());
        questionDetails.setText(details.toString());
    }

    private void displayFollowUpQuestionDetails(Question q) {
        StringBuilder details = new StringBuilder();
        details.append("Follow-Up ID: ").append(q.getQuestionId()).append("\n");
        details.append("Author: ").append(q.getAuthor()).append("\n");
        details.append("Title: ").append(q.getTitle()).append("\n");
        details.append("Status: ").append(q.getStatusText()).append("\n");
        details.append("Timestamp: ").append(q.getTimestamp() != null ? q.getTimestamp() : "N/A").append("\n");
        details.append("\nDescription:\n").append(q.getDescription());
        followUpDetails.setText(details.toString());
    }

    private void displayEditDetails(Question q) {
        StringBuilder details = new StringBuilder();
        details.append("Question ID: ").append(q.getQuestionId()).append("\n");
        details.append("Author: ").append(q.getAuthor()).append("\n");
        details.append("Title: ").append(q.getTitle()).append("\n");
        details.append("Status: ").append(q.getStatusText()).append("\n");
        details.append("Timestamp: ").append(q.getTimestamp() != null ? q.getTimestamp() : "N/A").append("\n");
        details.append("\nDescription:\n").append(q.getDescription());
        questionDetails.setText(details.toString());

        VBox editsList = new VBox(5);
        List<Edits> editHistory = q.getEditHistory();

        if (!editHistory.isEmpty()) {
            for (Edits edit : editHistory) {
                String summary = String.format(
                        "[%s] Edited by %s\nPrevious Title: %s\nPrevious Description:\n%s",
                        edit.getQuestionEditTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                        edit.getQuestionEditedBy(),
                        edit.getQuestionOldTitle(),
                        edit.getQuestionOldDescription()
                );
                Label editLabel = new Label(summary);
                editLabel.setWrapText(true);
                editLabel.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 8px; -fx-border-color: #ccc;");
                editsList.getChildren().add(editLabel);
            }
        } else {
            editsList.getChildren().add(new Label("No edits made."));
        }

        ScrollPane scrollableEdits = new ScrollPane(editsList);
        scrollableEdits.setFitToWidth(true);
        scrollableEdits.setPrefHeight(200);

        editsBox.getChildren().clear();
        Label editsHeader = new Label("Edit History:");
        editsHeader.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        editsBox.getChildren().addAll(editsHeader, scrollableEdits);
    }

    private void showAnswersPage(Question question) {
        new AnswersPage(this, question).show(stage, user);
    }

    private void showEditQuestionPage(Question question) {
        new EditQuestionPage(this, question, questions).show(stage, user);
        reloadQuestions();
        questionTable.getSelectionModel().clearSelection();
        questionTable.getSelectionModel().select(question);
    }

    private void showFlagPopup(ContentType contentType, int itemId) {
        Stage popup = new Stage();
        popup.setTitle("Flag Content");

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER_LEFT);

        Label userLabel = new Label("User: " + StatusData.currUser.getUserName());
        Label idLabel = new Label("UserID: " + StatusData.currUser.getId());
        userLabel.setStyle("-fx-font-weight: bold");
        idLabel.setStyle("-fx-font-weight: bold");

        Label reasonLabel = new Label("Reason for flagging:");
        TextArea reasonInput = new TextArea();
        reasonInput.setWrapText(true);
        reasonInput.setPromptText("Enter your reason here...");

        Button submitBtn = new Button("Submit");
        Button cancelBtn = new Button("Cancel");

        HBox buttons = new HBox(10, cancelBtn, submitBtn);
        layout.getChildren().addAll(userLabel, idLabel, reasonLabel, reasonInput, buttons);

        popup.setScene(new Scene(layout, 400, 250));
        popup.initOwner(stage);
        popup.show();

        cancelBtn.setOnAction(_ -> popup.close());
        submitBtn.setOnAction(_ -> {
            String reason = reasonInput.getText().trim();
            if (reason.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Missing Reason", "Please enter a reason for flagging.");
                return;
            }
            try {
                StatusData.databaseHelper.insertModerationFlag(
                        contentType.name().toLowerCase(), itemId, StatusData.currUser.getId(), reason);
                WelcomeLoginPage.this.reloadQuestions();
                if (StatusData.onFlagRefresh != null) StatusData.onFlagRefresh.run();
                popup.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to flag question: " + ex.getMessage());
            }
        });
    }

    @SuppressWarnings("unused")
    private void showClarificationPopup(Question question) {
        Stage popup = new Stage();
        popup.setTitle("Suggest Clarification");

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER_LEFT);

        Label titleQ = new Label("Suggest Clarification for Question # " + question.getQuestionId());
        titleQ.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        TextArea input = new TextArea();
        input.setPromptText("Enter your suggestion here...");
        input.setWrapText(true);
        input.setPrefRowCount(5);
        input.setPrefWidth(350);

        Button submitButton = new Button("Submit");
        submitButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        Button cancelButton = new Button("Cancel");

        HBox buttonBox = new HBox(10, cancelButton, submitButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        layout.getChildren().addAll(titleQ, input, buttonBox);

        Scene scene = new Scene(layout, 450, 300);
        popup.setScene(scene);
        popup.show();

        cancelButton.setOnAction(_ -> popup.close());
        submitButton.setOnAction(_ -> {
            String content = input.getText();
            if (content.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Empty Suggestion", "You must enter a suggestion before submitting");
                return;
            }
            try {
                ClarificationsManager clarifications = new ClarificationsManager(StatusData.databaseHelper);
                Result result = clarifications.create(
                        ContentType.QUESTION, question.getQuestionId(),
                        user.getId(), -1, user.getUserName(), content);
                if (result.isSuccess()) {
                    loadSuggestionsForQuestion(question);
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Suggestion submitted successfully!");
                    popup.close();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", result.getMessage());
                }
            } catch (SQLException ev) {
                ev.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Database Error", "Could not save clarification: " + ev.getMessage());
            }
        });
    }

    private void loadSuggestionsForQuestion(Question question) {
        try {
            List<Clarification> list;
            if (isPrivilegedRole(user)) {
                list = StatusData.databaseHelper.loadClarificationsforQ(question.getQuestionId());
            } else {
                List<Clarification> publicOnes = StatusData.databaseHelper
                        .loadClarificationsforQ(question.getQuestionId())
                        .stream().filter(Clarification::isPublic).collect(Collectors.toList());
                List<Clarification> privateForUser = StatusData.databaseHelper
                        .loadPrivateSuggestionsForUser(user.getId())
                        .stream().filter(c -> c.getQuestionId() == question.getQuestionId()).collect(Collectors.toList());
                list = new ArrayList<>();
                list.addAll(publicOnes);
                list.addAll(privateForUser);
            }
            suggestionsTable.getItems().setAll(list);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadFollowUpsForQuestion(Question question) {
        try {
            List<Question> followUps = StatusData.databaseHelper.loadAllQs().stream()
                    .filter(q -> q.getFollowUp() == question.getQuestionId())
                    .collect(Collectors.toList());
            followUpTable.getItems().setAll(followUps);
        } catch (Exception e) {
            e.printStackTrace();
            followUpTable.getItems().clear();
        }
    }

    private boolean isPrivilegedRole(User user) {
        if (user == null || user.getRole() == null) return false;
        String role = user.getRole().name().toLowerCase();
        return role.contains("admin") || role.contains("staff") || role.contains("instructor")
                || role.contains("teacher") || role.contains("ta");
    }

    private boolean canSeeFlag(User user, List<ModerationFlag> flags) {
        String role = (user.getRole() != null) ? user.getRole().name().toLowerCase() : "";
        boolean isPrivileged = role.contains("admin") || role.contains("staff") || role.contains("instructor");
        boolean isFlagger = flags.stream().anyMatch(f -> f.getStaffId() == user.getId());
        return isPrivileged || isFlagger;
    }

    @SuppressWarnings("unused")
    private void showFollowUpPopup(Question parentQuestion) {
        Stage popup = new Stage();
        popup.setTitle("Ask a Follow-Up Question");

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Following up on Question # " + parentQuestion.getQuestionId());
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        TextField titleField = new TextField();
        titleField.setPromptText("Enter a title for your follow-up...");

        TextArea descriptionField = new TextArea();
        descriptionField.setPromptText("Ask your follow-up question...");
        descriptionField.setWrapText(true);
        descriptionField.setPrefRowCount(5);
        descriptionField.setPrefWidth(350);

        Button submitButton = new Button("Post Follow-Up");
        submitButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        Button cancelButton = new Button("Cancel");

        HBox buttonBox = new HBox(10, cancelButton, submitButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        layout.getChildren().addAll(title, new Label("Title:"), titleField,
                new Label("Description:"), descriptionField, buttonBox);

        popup.setScene(new Scene(layout, 450, 300));
        popup.show();

        cancelButton.setOnAction(_ -> popup.close());
        submitButton.setOnAction(_ -> {
            String followUpTitle = titleField.getText().trim();
            String followUpDesc = descriptionField.getText().trim();

            if (followUpTitle.isEmpty() || followUpDesc.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Missing Fields", "Please enter both title and description.");
                return;
            }

            int followUpId = parentQuestion.getQuestionId();
            parentQuestion.setFollowUp(followUpId);

            Result result = questions.create(user.getId(), user.getName(), followUpTitle, followUpDesc, null, followUpId);

            if (result.isSuccess()) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Follow-up question posted successfully!");
                loadFollowUpsForQuestion(parentQuestion);
                popup.close();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", result.getMessage());
            }
        });
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}