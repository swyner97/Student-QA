package pages;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import databasePart1.DatabaseHelper;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import logic.StatusData;
import model.Answer;
import model.NavigationBar;
import model.Question;
import model.User;

public class SearchPage {
    private final DatabaseHelper databaseHelper;
    private final User currentUser;
    private Stage stage;

    public SearchPage() {
        this(StatusData.databaseHelper, StatusData.currUser);
    }

    public SearchPage(DatabaseHelper databaseHelper, User currentUser) {
        this.databaseHelper = databaseHelper;
        this.currentUser = currentUser;
    }

    public void show(Stage primaryStage) {
        show(primaryStage, currentUser);
    }

    public void show(Stage primaryStage, User currentUser) {
        this.stage = primaryStage;
        stage.setTitle("Search Questions");

        BorderPane mainPane = new BorderPane();
        mainPane.setTop(new NavigationBar());

        TextField keywordField = new TextField();
        keywordField.setPromptText("Search questions and answers");

        TextField authorField = new TextField();
        authorField.setPromptText("Search by author");

        CheckBox solvedOnly = new CheckBox("Only show questions marked as solved");
        CheckBox unsolvedOnly = new CheckBox("Only show questions with no solutions");

        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("Filter by date");

        Button searchButton = new Button("Search");

        TableView<SearchResultRow> resultsTable = new TableView<>();

        VBox searchSection = searchSection(
                keywordField,
                authorField,
                solvedOnly,
                unsolvedOnly,
                datePicker,
                searchButton,
                resultsTable
        );

        VBox tableBox = createResultsTable(resultsTable);

        VBox layout = new VBox(18, mainPane, searchSection, tableBox);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #ffffff;");

        Runnable runSearch = () -> performSearch(
                keywordField,
                authorField,
                solvedOnly,
                unsolvedOnly,
                datePicker,
                resultsTable
        );

        searchButton.setOnAction(e -> runSearch.run());

        keywordField.textProperty().addListener((obs, oldVal, newVal) -> runSearch.run());
        authorField.textProperty().addListener((obs, oldVal, newVal) -> runSearch.run());
        datePicker.valueProperty().addListener((obs, oldVal, newVal) -> runSearch.run());
        solvedOnly.selectedProperty().addListener((obs, oldVal, newVal) -> runSearch.run());
        unsolvedOnly.selectedProperty().addListener((obs, oldVal, newVal) -> runSearch.run());

        StatusData.setScene(stage, layout);

        runSearch.run();
    }

    private VBox searchSection(
            TextField keywordField,
            TextField authorField,
            CheckBox solvedOnly,
            CheckBox unsolvedOnly,
            DatePicker datePicker,
            Button searchButton,
            TableView<SearchResultRow> resultsTable
    ) {
        keywordField.setPrefHeight(38);
        keywordField.setMinHeight(38);
        keywordField.setMaxWidth(Double.MAX_VALUE);
        keywordField.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 18;
            -fx-border-color: #dadce0;
            -fx-border-radius: 18;
            -fx-padding: 8 14 8 14;
            -fx-font-size: 14px;
            -fx-text-fill: #202124;
            -fx-prompt-text-fill: #5f6368;
        """);

        Button searchIcon = new Button("🔍");
        searchIcon.setFocusTraversable(false);
        searchIcon.setStyle(flatIconStyle());

        Button clearButton = new Button("✕");
        clearButton.setFocusTraversable(false);
        clearButton.setStyle(flatIconStyle());
        clearButton.setOnAction(e -> keywordField.clear());

        Button advancedToggle = new Button("▾");
        advancedToggle.setFocusTraversable(false);
        advancedToggle.setStyle(flatIconStyle());

        HBox searchBar = new HBox(8, searchIcon, keywordField, clearButton, advancedToggle);
        searchBar.setAlignment(Pos.CENTER_LEFT);
        searchBar.setPadding(new Insets(8, 12, 8, 12));
        searchBar.setPrefHeight(56);
        searchBar.setMaxWidth(820);
        searchBar.setStyle("""
            -fx-background-color: #f8f9fa;
            -fx-background-radius: 28;
            -fx-border-radius: 28;
            -fx-border-color: #dadce0;
        """);

        HBox.setHgrow(keywordField, Priority.ALWAYS);

        VBox advancedFilters = new VBox(10);
        advancedFilters.setPadding(new Insets(14, 16, 16, 16));
        advancedFilters.setMaxWidth(820);
        advancedFilters.setStyle("""
            -fx-background-color: #f8f9fa;
            -fx-background-radius: 18;
            -fx-border-radius: 18;
            -fx-border-color: #dadce0;
        """);

        Label advancedTitle = new Label("Advanced search");
        advancedTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #202124;");

        GridPane filterGrid = new GridPane();
        filterGrid.setHgap(12);
        filterGrid.setVgap(10);

        Label authorLabel = new Label("Author:");
        Label dateLabel = new Label("Date:");

        authorLabel.setStyle("-fx-text-fill: #3c4043;");
        dateLabel.setStyle("-fx-text-fill: #3c4043;");

        authorField.setPrefWidth(280);
        authorField.setStyle(textFieldStyle());

        datePicker.setPrefWidth(280);
        datePicker.setStyle(textFieldStyle());

        VBox authorBox = new VBox(4, authorLabel, authorField);
        VBox dateBox = new VBox(4, dateLabel, datePicker);

        filterGrid.add(authorBox, 0, 0);
        filterGrid.add(dateBox, 1, 0);
        filterGrid.add(solvedOnly, 0, 1);
        filterGrid.add(unsolvedOnly, 1, 1);

        solvedOnly.setStyle("-fx-text-fill: #202124;");
        unsolvedOnly.setStyle("-fx-text-fill: #202124;");

        HBox actionsRow = new HBox(10);
        actionsRow.setAlignment(Pos.CENTER_RIGHT);
        searchButton.setStyle(primaryButtonStyle());
        actionsRow.getChildren().add(searchButton);

        advancedFilters.getChildren().addAll(advancedTitle, filterGrid, actionsRow);

        advancedFilters.setManaged(false);
        advancedFilters.setVisible(false);

        advancedToggle.setOnAction(e -> {
            boolean show = !advancedFilters.isVisible();
            advancedFilters.setVisible(show);
            advancedFilters.setManaged(show);
        });

        VBox searchWrapper = new VBox(10, searchBar, advancedFilters);
        searchWrapper.setAlignment(Pos.TOP_CENTER);
        searchWrapper.setPadding(new Insets(0, 0, 4, 0));

        return searchWrapper;
    }

    private VBox createResultsTable(TableView<SearchResultRow> resultsTable) {
        VBox tableBox = new VBox(10);
        tableBox.setPadding(new Insets(10, 0, 0, 0));

        Label tableTitle = new Label("Search Results");
        tableTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #202124;");

        HBox tableHeader = new HBox(10, tableTitle);
        tableHeader.setAlignment(Pos.CENTER_LEFT);

        TableColumn<SearchResultRow, String> sectionCol = new TableColumn<>("Section");
        sectionCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getSectionLabel())
        );
        sectionCol.setPrefWidth(120);

        TableColumn<SearchResultRow, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTypeLabel())
        );
        typeCol.setPrefWidth(90);

        TableColumn<SearchResultRow, String> referenceCol = new TableColumn<>("Reference");
        referenceCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getReferenceLabel())
        );
        referenceCol.setPrefWidth(120);

        TableColumn<SearchResultRow, String> authorCol = new TableColumn<>("Author");
        authorCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getAuthorLabel())
        );
        authorCol.setPrefWidth(120);

        TableColumn<SearchResultRow, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTitleLabel())
        );
        titleCol.setPrefWidth(180);

        TableColumn<SearchResultRow, String> previewCol = new TableColumn<>("Preview");
        previewCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getPreviewLabel())
        );
        previewCol.setPrefWidth(330);

        TableColumn<SearchResultRow, String> detailsCol = new TableColumn<>("Details");
        detailsCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDetailsLabel())
        );
        detailsCol.setPrefWidth(140);

        TableColumn<SearchResultRow, Void> actionCol = new TableColumn<>("Action");
        actionCol.setPrefWidth(120);
        actionCol.setCellFactory(_ -> new TableCell<>() {
            private final Button actionButton = new Button();
            private final HBox box = new HBox(actionButton);

            {
                box.setAlignment(Pos.CENTER);
                actionButton.setStyle(primaryButtonStyle());
                actionButton.setOnAction(e -> {
                    SearchResultRow row = getCurrentRow();
                    if (row == null) {
                        return;
                    }
                    if (row.isQuestionRow()) {
                        showQuestionAnswersDialog(row.getQuestion());
                    } else if (row.isAnswerRow()) {
                        showAnswerDialog(row.getAnswer());
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                SearchResultRow row = getCurrentRow();

                if (empty || row == null || row.isSectionRow() || row.isInfoRow()) {
                    setGraphic(null);
                    return;
                }

                actionButton.setText(row.isQuestionRow() ? "View Answers" : "View Answer");
                setGraphic(box);
            }

            private SearchResultRow getCurrentRow() {
                int index = getIndex();
                if (index < 0 || index >= getTableView().getItems().size()) {
                    return null;
                }
                return getTableView().getItems().get(index);
            }
        });

        resultsTable.getColumns().addAll(
                sectionCol,
                typeCol,
                referenceCol,
                authorCol,
                titleCol,
                previewCol,
                detailsCol,
                actionCol
        );

        resultsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        resultsTable.setPlaceholder(new Label("Search results will appear here."));

        resultsTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(SearchResultRow item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setStyle("");
                    setDisable(false);
                    return;
                }

                if (item.isSectionRow()) {
                    setStyle("""
                        -fx-background-color: #f1f3f4;
                        -fx-font-weight: bold;
                    """);
                    setDisable(true);
                } else if (item.isInfoRow()) {
                    setStyle("""
                        -fx-font-style: italic;
                        -fx-text-fill: #5f6368;
                    """);
                    setDisable(true);
                } else {
                    setStyle("");
                    setDisable(false);
                }
            }
        });

        tableBox.getChildren().addAll(tableHeader, resultsTable);
        return tableBox;
    }

    private void performSearch(
            TextField keywordField,
            TextField authorField,
            CheckBox solvedOnly,
            CheckBox unsolvedOnly,
            DatePicker datePicker,
            TableView<SearchResultRow> resultsTable
    ) {
        resultsTable.getItems().clear();

        String keywordRaw = keywordField.getText() == null ? "" : keywordField.getText().trim();
        String authorRaw = authorField.getText() == null ? "" : authorField.getText().trim();
        LocalDate selectedDate = datePicker.getValue();

        if (keywordRaw.isEmpty()
                && authorRaw.isEmpty()
                && selectedDate == null
                && !solvedOnly.isSelected()
                && !unsolvedOnly.isSelected()) {
            return;
        }

        String keyword = keywordRaw.toLowerCase();
        String author = authorRaw.toLowerCase();

        Boolean solvedQs = null;
        if (solvedOnly.isSelected() && !unsolvedOnly.isSelected()) {
            solvedQs = true;
        } else if (!solvedOnly.isSelected() && unsolvedOnly.isSelected()) {
            solvedQs = false;
        }

        try {
            List<Question> allQuestions = databaseHelper.loadAllQs();
            List<Answer> allAnswers = databaseHelper.loadAllAnswers();

            List<SearchResultRow> rows = new ArrayList<>();
            List<SearchResultRow> questionRows = new ArrayList<>();
            List<SearchResultRow> answerRows = new ArrayList<>();

            for (Question q : allQuestions) {
                boolean keywordMatch =
                        keyword.isEmpty()
                                || safeText(q.getTitle()).toLowerCase().contains(keyword)
                                || safeText(q.getDescription()).toLowerCase().contains(keyword)
                                || safeText(q.getAuthor()).toLowerCase().contains(keyword);

                boolean authorMatch =
                        author.isEmpty()
                                || safeText(q.getAuthor()).toLowerCase().contains(author);

                boolean solvedMatch =
                        solvedQs == null || q.isResolved() == solvedQs;

                boolean dateMatch =
                        selectedDate == null || matchesDate(q.getTimestamp(), selectedDate);

                if (keywordMatch && authorMatch && solvedMatch && dateMatch) {
                    int answerCount = databaseHelper.getAnswersByQuestionId(q.getQuestionId()).size();
                    questionRows.add(SearchResultRow.question(q, answerCount));
                }
            }

            for (Answer a : allAnswers) {
                boolean keywordMatch =
                        keyword.isEmpty()
                                || safeText(a.getContent()).toLowerCase().contains(keyword)
                                || safeText(a.getAuthor()).toLowerCase().contains(keyword);

                boolean authorMatch =
                        author.isEmpty()
                                || safeText(a.getAuthor()).toLowerCase().contains(author);

                boolean dateMatch =
                        selectedDate == null || matchesDate(a.getTimestamp(), selectedDate);

                if (keywordMatch && authorMatch && dateMatch) {
                    answerRows.add(SearchResultRow.answer(a));
                }
            }

            if (!questionRows.isEmpty()) {
                rows.add(SearchResultRow.section("Questions"));
                rows.addAll(questionRows);
            }

            if (!answerRows.isEmpty()) {
                rows.add(SearchResultRow.section("Answers"));
                rows.addAll(answerRows);
            }

            if (rows.isEmpty()) {
                rows.add(SearchResultRow.info("No results found."));
            }

            resultsTable.getItems().setAll(rows);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showQuestionAnswersDialog(Question q) {
        try {
            List<Answer> answersForQuestion = databaseHelper.getAnswersByQuestionId(q.getQuestionId());

            Stage dialog = new Stage();
            dialog.setTitle("Answers for Question #" + q.getQuestionId());

            Label questionTitle = new Label("Question: " + safeText(q.getTitle()));
            questionTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

            TextArea questionArea = new TextArea(safeText(q.getDescription()));
            questionArea.setEditable(false);
            questionArea.setWrapText(true);

            ListView<Answer> listView = new ListView<>();
            listView.getItems().addAll(answersForQuestion);
            listView.setCellFactory(lview -> new ListCell<>() {
                @Override
                protected void updateItem(Answer a, boolean empty) {
                    super.updateItem(a, empty);
                    if (empty || a == null) {
                        setText(null);
                    } else {
                        setText(preview(safeText(a.getContent()), 80));
                    }
                }
            });

            TextArea fullAnswerArea = new TextArea();
            fullAnswerArea.setEditable(false);
            fullAnswerArea.setWrapText(true);

            listView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, sel) -> {
                if (sel != null) {
                    fullAnswerArea.setText(safeText(sel.getContent()));
                }
            });

            VBox left = new VBox(8, new Label("Select an answer:"), listView);
            VBox right = new VBox(8, new Label("Full answer:"), fullAnswerArea);
            left.setPadding(new Insets(10));
            right.setPadding(new Insets(10));

            VBox questionBox = new VBox(6, questionTitle, questionArea);
            questionBox.setPadding(new Insets(10));

            SplitPane split = new SplitPane(left, right);
            split.setDividerPositions(0.35);

            BorderPane root = new BorderPane();
            root.setTop(questionBox);
            root.setCenter(split);

            Scene scene = new Scene(root, 760, 520);
            dialog.setScene(scene);
            dialog.showAndWait();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void showAnswerDialog(Answer answer) {
        Stage dialog = new Stage();
        dialog.setTitle("Answer Details");

        Label title = new Label("Answer");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Label questionRef = new Label("Related question ID: " + answer.getQuestionId());

        TextArea contentArea = new TextArea(safeText(answer.getContent()));
        contentArea.setEditable(false);
        contentArea.setWrapText(true);

        VBox root = new VBox(10, title, questionRef, contentArea);
        root.setPadding(new Insets(12));

        Scene scene = new Scene(root, 600, 380);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private boolean matchesDate(String timestamp, LocalDate selectedDate) {
        if (timestamp == null || timestamp.isBlank()) {
            return false;
        }

        try {
            LocalDateTime dateTime;
            try {
                dateTime = LocalDateTime.parse(timestamp); 
            } catch (Exception ex) {
                dateTime = LocalDateTime.parse(
                        timestamp,
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                );
            }
            return dateTime.toLocalDate().equals(selectedDate);
        } catch (Exception e) {
            return false;
        }
    }

    private static String safeText(String text) {
        return text == null ? "" : text;
    }

    private static String preview(String text, int maxLength) {
        String safe = safeText(text).trim();
        if (safe.length() <= maxLength) {
            return safe;
        }
        return safe.substring(0, maxLength) + "...";
    }

    private String flatIconStyle() {
        return """
            -fx-background-color: transparent;
            -fx-text-fill: #5f6368;
            -fx-font-size: 14px;
            -fx-cursor: hand;
            -fx-padding: 4 8 4 8;
        """;
    }

    private String primaryButtonStyle() {
        return """
            -fx-background-color: #1a73e8;
            -fx-text-fill: white;
            -fx-background-radius: 18;
            -fx-padding: 8 16 8 16;
            -fx-font-weight: bold;
            -fx-cursor: hand;
        """;
    }

    private String textFieldStyle() {
        return """
            -fx-background-color: white;
            -fx-background-radius: 14;
            -fx-border-color: #dadce0;
            -fx-border-radius: 14;
            -fx-padding: 8 12 8 12;
            -fx-font-size: 13px;
        """;
    }

    private static class SearchResultRow {
        private final boolean sectionRow;
        private final boolean infoRow;

        private final String sectionLabel;
        private final String typeLabel;
        private final String referenceLabel;
        private final String authorLabel;
        private final String titleLabel;
        private final String previewLabel;
        private final String detailsLabel;

        private final Question question;
        private final Answer answer;

        private SearchResultRow(
                boolean sectionRow,
                boolean infoRow,
                String sectionLabel,
                String typeLabel,
                String referenceLabel,
                String authorLabel,
                String titleLabel,
                String previewLabel,
                String detailsLabel,
                Question question,
                Answer answer
        ) {
            this.sectionRow = sectionRow;
            this.infoRow = infoRow;
            this.sectionLabel = sectionLabel;
            this.typeLabel = typeLabel;
            this.referenceLabel = referenceLabel;
            this.authorLabel = authorLabel;
            this.titleLabel = titleLabel;
            this.previewLabel = previewLabel;
            this.detailsLabel = detailsLabel;
            this.question = question;
            this.answer = answer;
        }

        static SearchResultRow section(String label) {
            return new SearchResultRow(
                    true,
                    false,
                    label,
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    null,
                    null
            );
        }

        static SearchResultRow info(String message) {
            return new SearchResultRow(
                    false,
                    true,
                    "",
                    "",
                    "",
                    "",
                    message,
                    "",
                    "",
                    null,
                    null
            );
        }

        static SearchResultRow question(Question q, int answerCount) {
            return new SearchResultRow(
                    false,
                    false,
                    "",
                    "Question",
                    "Question #" + q.getQuestionId(),
                    safeText(q.getAuthor()),
                    safeText(q.getTitle()),
                    preview(safeText(q.getDescription()), 90),
                    answerCount + " answers",
                    q,
                    null
            );
        }

        static SearchResultRow answer(Answer a) {
            return new SearchResultRow(
                    false,
                    false,
                    "",
                    "Answer",
                    "Question #" + a.getQuestionId(),
                    safeText(a.getAuthor()),
                    "Answer #" + a.getAnswerId(),
                    preview(safeText(a.getContent()), 90),
                    a.isSolution() ? "Marked as solution" : "Answer",
                    null,
                    a
            );
        }

        boolean isSectionRow() {
            return sectionRow;
        }

        boolean isInfoRow() {
            return infoRow;
        }

        boolean isQuestionRow() {
            return question != null;
        }

        boolean isAnswerRow() {
            return answer != null;
        }

        String getSectionLabel() {
            return sectionLabel;
        }

        String getTypeLabel() {
            return typeLabel;
        }

        String getReferenceLabel() {
            return referenceLabel;
        }

        String getAuthorLabel() {
            return authorLabel;
        }

        String getTitleLabel() {
            return titleLabel;
        }

        String getPreviewLabel() {
            return previewLabel;
        }

        String getDetailsLabel() {
            return detailsLabel;
        }

        Question getQuestion() {
            return question;
        }

        Answer getAnswer() {
            return answer;
        }
    }
}