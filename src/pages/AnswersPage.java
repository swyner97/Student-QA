package pages;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import logic.ClarificationsManager;
import logic.Result;
import logic.StatusData;
import model.Answer;
import model.Answers;
import model.Clarification;
import model.ContentType;
import model.Messages;
import model.ModerationFlag;
import model.NavigationBar;
import model.Question;
import model.Review;
import model.User;
/**
 * AnswersPage UI: shows answers for a question, allows submitting, editing, deleting
 */
public class AnswersPage {

    private static Answers answers;
    private static Question question;
    private Object parentPage;
    private User user;
    private Stage stage;

    private static TableView<Answer> answerTable;
    private TableView<Clarification> suggestionsTable;
    
    private TextArea answerDetails;
    private TextArea answerInput;
    private CheckBox markAsSolution;
    private Button submitButton;
    private Button updateAnswerButton;
    private Button deleteAnswerButton;
    private Button cancelEditButton;
    private Button postReviewButton;
    private Label submitTitle;
    private Answer editingAnswer = null;
  
    public AnswersPage(Object parentPage, Question question) {
        this.parentPage = parentPage;
        AnswersPage.question = question;
    }

    @SuppressWarnings("unused")
	public void show(Stage stage, User user) {
        this.user = user;
        answers = new Answers(StatusData.databaseHelper);
      

        stage.setTitle("Answers for Question #" + question.getQuestionId());

        BorderPane mainPane = new BorderPane();

        // Navigation bar
        NavigationBar navBar = new NavigationBar();
        mainPane.setTop(navBar);

        // Center content
        BorderPane content = new BorderPane();
        content.setPadding(new Insets(15));

        // Top section: Question info + Back button
        VBox topSection = new VBox(10);

        HBox headerBox = new HBox(10);
        headerBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Button backButton = new Button("← Back to Questions");
        backButton.setOnAction(e -> {
            if (parentPage instanceof MyQAPage) {
                ((MyQAPage) parentPage).show(stage, user);
            } else if (parentPage instanceof WelcomeLoginPage) {
                ((WelcomeLoginPage) parentPage).show(stage, user);
            }
        });

        Label pageTitle = new Label("Answers for Question #" + question.getQuestionId());
        pageTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        headerBox.getChildren().addAll(backButton, spacer, pageTitle);

        // Question info box
        VBox questionBox = new VBox(5);
        questionBox.setStyle("-fx-background-color: #f5f5f5; -fx-padding: 10; -fx-border-color: #ddd; -fx-border-width: 1;");

        Label questionTitle = new Label("Question: " + question.getTitle());
        questionTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Label questionAuthor = new Label("By: " + question.getAuthor() + " | Status: " + question.getStatusText());
        questionAuthor.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

        Label questionDesc = new Label(question.getDescription());
        questionDesc.setWrapText(true);
        questionDesc.setStyle("-fx-font-size: 12px;");

        questionBox.getChildren().addAll(questionTitle, questionAuthor, questionDesc);

        topSection.getChildren().addAll(headerBox, questionBox);

        // Submit Answer Section
        VBox submitSection = new VBox(10);
        submitSection.setPadding(new Insets(10, 0, 10, 0));
        submitSection.setStyle("-fx-background-color: #e8f5e9; -fx-padding: 15; -fx-border-color: #4caf50; -fx-border-width: 1;");

        submitTitle = new Label("Submit Your Answer");
        submitTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        answerInput = new TextArea();
        answerInput.setPromptText("Write your answer here...");
        answerInput.setWrapText(true);
        answerInput.setPrefRowCount(4);

        HBox submitButtonBox = new HBox(10);
        submitButtonBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        markAsSolution = new CheckBox("Mark as solution");

        // Only allow certain roles to mark answers as solutions
        User.Role role = user.getRole();

        boolean canMarkAsSolution =
                role == User.Role.ADMIN ||
                role == User.Role.INSTRUCTOR ||
                role == User.Role.REVIEWER ||
                role == User.Role.TA ||
                user.getName().equals(question.getAuthor());

        markAsSolution.setDisable(!canMarkAsSolution);

        if (!canMarkAsSolution) {
            markAsSolution.setTooltip(new Tooltip(
                "Only admins, instructors, reviewers, TAs, or the question author can mark answers as solutions"
            ));
        }

        submitButton = new Button("Submit Answer");
        submitButton.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; -fx-font-weight: bold;");

        updateAnswerButton = new Button("Update Answer");
        updateAnswerButton.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white; -fx-font-weight: bold;");
        updateAnswerButton.setVisible(false);
        updateAnswerButton.setManaged(false);

        deleteAnswerButton = new Button("Delete Answer");
        deleteAnswerButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold;");
        deleteAnswerButton.setVisible(false);
        deleteAnswerButton.setManaged(false);

        cancelEditButton = new Button("Cancel");
        cancelEditButton.setStyle("-fx-background-color: #9e9e9e; -fx-text-fill: white;");
        cancelEditButton.setVisible(false);
        cancelEditButton.setManaged(false);

        submitButton.setOnAction(e -> {
            String answerContent = answerInput.getText().trim();
            if (answerContent.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Empty Answer", "Please write an answer before submitting.");
                return;
            }

            boolean isSolution = markAsSolution.isSelected();
            submitAnswer(answerContent, isSolution);
            answerInput.clear();
            markAsSolution.setSelected(false);
            loadAnswers();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Your answer has been submitted!");
        });

        updateAnswerButton.setOnAction(e -> {
            String answerContent = answerInput.getText().trim();
            if (answerContent.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Empty Answer", "Please write an answer before updating.");
                return;
            }

            if (editingAnswer != null) {
                updateAnswer(answerContent);
            }
        });

        deleteAnswerButton.setOnAction(e -> {
            if (editingAnswer != null) {
                deleteAnswer(editingAnswer);
            }
        });

        cancelEditButton.setOnAction(e -> clearAnswerForm());

        submitButtonBox.getChildren().addAll(markAsSolution, submitButton, updateAnswerButton, deleteAnswerButton, cancelEditButton);
        submitSection.getChildren().addAll(submitTitle, answerInput, submitButtonBox);

        // Middle: Answers table
        VBox tableBox = new VBox(10);
        tableBox.setPadding(new Insets(10, 0, 0, 0));

        Label tableTitle = new Label("All Answers (" + getAnswerCount() + ")");
        tableTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        answerTable = new TableView<>();
        answerTable.setPrefHeight(200);

        TableColumn<Answer, Void> flagCol = new TableColumn<>("Flag");
        flagCol.setPrefWidth(80);

        flagCol.setCellFactory(col -> new TableCell<>() {
        	//private final Button flagBtn = new Button("🏳️");
        	private final Button flagBtn = new Button("\uD83C\uDFF3\uFE0F");
        	private final HBox actionBox = new HBox(5, flagBtn);
        	
        	{
        		flagBtn.setStyle("-fx-font-size: 14px; -fx-text-fill: green; -fx-background-color: transparent; -fx-cursor: hand;");
        		flagBtn.setOnAction(e -> {
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
				        List<ModerationFlag> existingFlags = StatusData.databaseHelper.loadAllModerationFlags();;
				        int currentUserId = StatusData.currUser.getId();

				        // Check if user already flagged this question
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
				        	showFlagPopup(contentType,  itemId);
				        }
				        
				        AnswersPage.reloadAnswers();
				        answerTable.refresh();
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


        		if (item instanceof Answer a) {
        			type = ContentType.ANSWER;
        			itemId = a.getAnswerId();
        		} else {
        			return;
        		}

        		final int finalItemId = itemId;
        		final String finalItemType = type.name();
        		
        		try {
        			List<ModerationFlag> flags = StatusData.databaseHelper.loadAllModerationFlags();
        			boolean isFlagged = flags.stream()
        					.anyMatch(f -> f.getItemType().equalsIgnoreCase(finalItemType) &&
        								f.getItemId() == finalItemId &&
        								f.getStatus().equalsIgnoreCase("OPEN"));

        			boolean canSee = flags.stream()
        		            .anyMatch(f -> f.getItemType().equalsIgnoreCase(finalItemType) &&
 		            			   f.getItemId() == finalItemId &&
 		            			   canSeeFlag(StatusData.currUser, List.of(f)));

        			if (isFlagged && canSee) {
        				flagBtn.setStyle("-fx-background-color: #ffcccc; -fx-text-fill: red; -fx-font-size: 14px;");
        			} else {
        				flagBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: green; -fx-font-size: 14px;");
        			}
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
        			Answer a = getTableRow().getItem();
        			updateFlagButtonStyle(a);
        			setGraphic(flagBtn);
        		}
        	}
    	});
        
        TableColumn<Answer, Integer> idCol = new TableColumn<>("Answer ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("answerId"));
        idCol.setPrefWidth(80);

        TableColumn<Answer, String> authorCol = new TableColumn<>("Author");
        authorCol.setCellValueFactory(new PropertyValueFactory<>("author"));
        authorCol.setPrefWidth(120);

        TableColumn<Answer, String> contentCol = new TableColumn<>("Content Preview");
        contentCol.setCellValueFactory(cellData -> {
            String answerContent = cellData.getValue().getContent();
            String preview = answerContent.length() > 50 ? answerContent.substring(0, 50) + "..." : answerContent;
            return new javafx.beans.property.SimpleStringProperty(preview);
        });
        contentCol.setPrefWidth(350);

        TableColumn<Answer, Void> reviewCol = new TableColumn<>("Reviews");
        reviewCol.setPrefWidth(120);

        reviewCol.setCellFactory(col -> new TableCell<Answer, Void>() {
            private final Button viewReviewsBtn = new Button("View Reviews");

            {
                viewReviewsBtn.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white;");
                viewReviewsBtn.setOnAction(event -> {
                    Answer answer = getTableRow().getItem();
                    if (answer != null && answerTable.getScene() != null) {
                        Stage owner = (Stage) answerTable.getScene().getWindow();
                        ReviewPage rp = new ReviewPage(AnswersPage.this);
                        rp.showForAnswer(owner, user, answer);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }

                setGraphic(viewReviewsBtn);
            }
        });


        TableColumn<Answer, Boolean> solutionCol = new TableColumn<>("Mark as\nSolution");
        solutionCol.setPrefWidth(80);
        
        solutionCol.setCellValueFactory(cellData -> 
        new SimpleBooleanProperty(cellData.getValue().isSolution()));

        solutionCol.setCellFactory(col -> new TableCell<Answer, Boolean>() {
        	private final ToggleButton solutionBtn = new ToggleButton();
        	
            {
            	solutionBtn.setOnAction(e -> {
            		Answer answer = getTableRow().getItem();
                    if (answer == null) return;

                    boolean current = answer.isSolution();
                    boolean newStatus = !current;

                    boolean canMarkAsSolution =
                        user.getRole() == User.Role.ADMIN ||
                        user.getRole() == User.Role.INSTRUCTOR ||
                        user.getRole() == User.Role.REVIEWER ||
                        user.getRole() == User.Role.TA ||
                        user.getId() == answer.getUserId();

                    if (!canMarkAsSolution) {
                        showAlert(Alert.AlertType.WARNING, "Permission Denied", "You are not allowed to mark solutions.");
                        solutionBtn.setSelected(current);
                        return;
                    }
                    
                    Result result = StatusData.answers.setSolution(answer.getAnswerId(), newStatus);
                    if (!result.isSuccess()) {
                        showAlert(Alert.AlertType.ERROR, "Error", result.getMessage());
                        solutionBtn.setSelected(current);  
                    } else {
                        answer.setSolution(newStatus);    
                        solutionBtn.setSelected(newStatus);
                        //updateToggleText(newStatus);
                        
                        loadAnswers();
                    }
            	});
            }
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    boolean isSolution = item != null && item;
                    solutionBtn.setSelected(isSolution);
                    updateToggleText(isSolution);
                    setGraphic(solutionBtn);
                }
            }
            private void updateToggleText(boolean isSolution) {
                if (isSolution) {
                	solutionBtn.setText("★");
                	solutionBtn.setStyle("-fx-background-color: gray; -fx-text-fill: gold;");
                } else {
                	solutionBtn.setText("★");
                	solutionBtn.setStyle("-fx-background-color: gray; -fx-text-fill: lightgray");
                }
            }
        });

        
    	
    	            
        TableColumn<Answer, Void> clarifyCol = new TableColumn<>("Suggestions");
        clarifyCol.setPrefWidth(100);
        
        clarifyCol.setCellFactory(_ -> new TableCell<>() {
        	private final Button suggestClarificationBtn = new Button("Suggest\nClarification");
        	private final HBox suggestClarification = new HBox(5, suggestClarificationBtn);
        	
        	{
        		//suggestClarificationBtn.setMinWidth(100);  
        		suggestClarificationBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        		suggestClarificationBtn.setWrapText(true);
        	    suggestClarificationBtn.setPrefWidth(90);   
        	    suggestClarificationBtn.setPrefHeight(25);    
        	    suggestClarificationBtn.setAlignment(Pos.CENTER);

        		
	            suggestClarificationBtn.setOnAction(_ -> {
	                Answer answer = getTableView().getItems().get(getIndex());
	                showClarificationPopup(answer);
	            });
        	}
        	 @Override
             protected void updateItem(Void item, boolean empty) {
                 super.updateItem(item, empty);
                 setGraphic(empty ? null : suggestClarification);
             }
           
        });
        
        // Edit button column
        TableColumn<Answer, Void> editCol = new TableColumn<>("Actions");
        editCol.setPrefWidth(80);
        editCol.setCellFactory(param -> new TableCell<Answer, Void>() {
            private final Button editBtn = new Button("Edit");

            {
                editBtn.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white;");
                editBtn.setOnAction(event -> {
                    Answer answer = getTableView().getItems().get(getIndex());
                    editAnswer(answer);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }

                Answer answer = getTableView().getItems().get(getIndex());
                boolean isAuthor = answer.getUserId() == user.getId();
                boolean isPrivileged = user.isPrivileged();


                // Show Edit button only if author or privileged user
                if (isAuthor || isPrivileged) {
                    editBtn.setDisable(false);
                    setGraphic(editBtn);
                } else {
                    setGraphic(null); // Hide completely
                }
            }
        });

        answerTable.getColumns().addAll(flagCol, idCol, authorCol, contentCol, solutionCol, clarifyCol, reviewCol, editCol);

        answerTable.setRowFactory(tv -> new TableRow<>() {
        	@Override
        	protected void updateItem(Answer answer, boolean empty) {
        		super.updateItem(answer, empty);
        		if (empty || answer == null) {
                    setStyle(""); 
                    return;
                }

        		 try {
        		        List<ModerationFlag> flags = StatusData.databaseHelper.loadAllModerationFlags().stream()
    	    		            .filter(f -> f.getItemType().equalsIgnoreCase("answer"))
    	    		            .filter(f -> f.getItemId() == answer.getAnswerId())
    	    		            .filter(f -> f.getStatus().equalsIgnoreCase("OPEN"))
    	    		            .collect(Collectors.toList());

        		        boolean isFlagged = !flags.isEmpty();
        		        if (isFlagged && canSeeFlag(StatusData.currUser, flags)) {
        		            setStyle("-fx-background-color: #ffcccc; -fx-text-fill: red; -fx-font-size: 14px;");
        		        } else {
        		            setStyle("");
        		        }
        		    } catch (SQLException e) {
        		        e.printStackTrace();
        		        setStyle(""); 
        		    }
        	}
        });
        
        answerTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
        	if (newVal != null) {
                displayAnswerDetails(newVal);
                loadSuggestionsForAnswer(newVal);
            }
        });

        tableBox.getChildren().addAll(tableTitle, answerTable);

        // Bottom: Answer details
        VBox detailsBox = new VBox(10);
        detailsBox.setPadding(new Insets(10, 0, 0, 0));

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

//        Label detailsTitle = new Label("Answer Details");
//        detailsTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
//        
        //answer details tab
        answerDetails = new TextArea();
        answerDetails.setEditable(false);
        answerDetails.setWrapText(true);
        answerDetails.setPrefRowCount(6);
        answerDetails.setPromptText("Select an answer to view full details...");

        ScrollPane detailsScroll = new ScrollPane(answerDetails);
        detailsScroll.setFitToWidth(true);
        detailsScroll.setStyle("-fx-background-color: transparent;");
        Tab detailsTab = new Tab("Answer Details", detailsScroll);
        
        //Suggestions Tab
        suggestionsTable = new TableView<>();

        TableColumn<Clarification, Void> flag2Col = new TableColumn<>("Flag");
        flag2Col.setPrefWidth(60);
        flag2Col.setCellFactory(col -> new TableCell<>() {
        	
        	private final Button flagBtn = new Button();
        	private final Label flagIcon = new Label("🚩\uFE0F");
        	
        	{
        		flagIcon.setStyle("-fx-font-size: 14px; -fx-text-fill: green;");
        		flagBtn.setGraphic(flagIcon);
        		flagBtn.setStyle("-fx-background-color: transparent; -fx-padding: 0;");
        		flagBtn.setOnAction(e -> {
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
    				        List<ModerationFlag> existingFlags = StatusData.databaseHelper.loadAllModerationFlags();;
    				        int currentUserId = StatusData.currUser.getId();

    				        // Check if user already flagged this question
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
    				        	showFlagPopup(contentType,  itemId);
    				        }
    				        
    				        AnswersPage.this.reloadAnswers();
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
        		
        		if (item instanceof Question q) {
    				type = ContentType.QUESTION;
    				itemId = q.getQuestionId();
    			} else if (item instanceof Answer a) {
    				type = ContentType.ANSWER;
    				itemId = a.getAnswerId();
    			} else if (item instanceof Review r) {
    				type = ContentType.REVIEW;
    				itemId = r.getReviewId();
    			} else if (item instanceof Clarification c) {
    				type = ContentType.SUGGESTION;
    				itemId = c.getId();
    			} else {
    				return;
    			}
        		
        		final int finalItemId = itemId;
        	    final String finalItemType = type.name();
        		
        	    try {
    			 
    		        List<ModerationFlag> flags = StatusData.databaseHelper.loadAllModerationFlags();
    		        boolean isFlagged = flags.stream()
    		            .anyMatch(f -> f.getItemType().equalsIgnoreCase(finalItemType) &&
    		            			   f.getItemId() == finalItemId &&
    		            			   f.getStatus().equalsIgnoreCase("OPEN"));

    		        boolean canSee = flags.stream()
        		            .anyMatch(f -> f.getItemType().equalsIgnoreCase(finalItemType) &&
 		            			   f.getItemId() == finalItemId &&
 		            			   canSeeFlag(StatusData.currUser, List.of(f)));

    		        if (isFlagged && canSee) {
    		            flagBtn.setStyle("-fx-background-color: #ffcccc; -fx-text-fill: red; -fx-font-size: 14px;");
    		        } else {
    		            flagBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: green; -fx-font-size: 14px;");
    		        }
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
        			Clarification c = getTableRow().getItem();
        			updateFlagButtonStyle(c);
        			setGraphic(flagBtn);
        		}
        	}
        });
        
        TableColumn<Clarification, String> author2Col = new TableColumn<>("User");
        author2Col.setCellValueFactory(new PropertyValueFactory<>("author"));
        author2Col.setPrefWidth(100);

        TableColumn<Clarification, String> content2Col = new TableColumn<>("Suggestion");
        content2Col.setCellValueFactory(new PropertyValueFactory<>("content"));
        content2Col.setPrefWidth(300);

        suggestionsTable.getColumns().addAll(flag2Col, author2Col, content2Col);
        Tab suggestionsTab = new Tab("View Suggestions", suggestionsTable);

        tabPane.getTabs().addAll(detailsTab, suggestionsTab);
        tabPane.setPrefHeight(220);
        tabPane.setStyle("-fx-background-color: white; " +
                "-fx-border-color: #2196f3; " +
                "-fx-border-radius: 5; -fx-background-radius: 5;"
        );
        
        Button makePublicBtn = new Button("Make Public");
        makePublicBtn.setOnAction(e -> {
            Clarification sel = suggestionsTable.getSelectionModel().getSelectedItem();
            Answer answer = answerTable.getSelectionModel().getSelectedItem();
            
            if (sel == null || answer == null) {
                showAlert(Alert.AlertType.WARNING, "No Suggestion Selected", "Please select a suggestion or question");
                return;
            }
            try {
                StatusData.databaseHelper.makeClarificationPublic(sel.getId());
                showAlert(Alert.AlertType.INFORMATION, "Success", "Suggestion made public");
                loadSuggestionsForAnswer(answer);
            } catch (SQLException ex) {
                ex.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Could not make suggestion public: " + ex.getMessage());
            }
        });
        
        Answer answer = answerTable.getSelectionModel().getSelectedItem();
        if (answer != null && (isPrivilegedRole(user) || user.getId() == answer.getUserId())) {
            detailsBox.getChildren().add(makePublicBtn);
        }
        
        Button suggestClarificationBtn = new Button("Suggest Clarification");
        suggestClarificationBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        suggestClarificationBtn.setOnAction(_ -> {
            Answer selected = answerTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.INFORMATION, "No Answer Selected", "Please select an answer.");
                return;
            }
            showClarificationPopup(selected);
        });
        
        Button followUpBtn = new Button("Ask FollowUp");
        followUpBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        
        followUpBtn.setOnAction(_ -> {
        	Answer selected = answerTable.getSelectionModel().getSelectedItem();
        	@SuppressWarnings("unused")
			int aId = selected.getAnswerId();
        	if (selected == null) {
        		showAlert(Alert.AlertType.INFORMATION, "No Question Selected", "Please selected a question.");
        		return;
        	}
        	//FollowUpQ followUpPopup = new FollowUpQ(user, questions, qId;
            //showFollowUpPopup(selected);
        });
        
        Button privateMsgBtn = new Button("Private Message Author");
        privateMsgBtn.setStyle("-fx-background-color: #f0ad4e; -fx-text-fill: black; -fx-font-weight: bold;");
        
        privateMsgBtn.setOnAction(_ -> {
        	Answer selected = answerTable.getSelectionModel().getSelectedItem();
        	if (selected == null) {
        		showAlert(Alert.AlertType.INFORMATION, "No Answer Selected", "Please select a question.");
        		return;
        	}
        	
        	int authorId = selected.getUserId();
        	System.out.println("Selected answer user_id: " + authorId);
        	User author; 
        	System.out.println("Selected Answer ID: " + selected.getAnswerId());
            System.out.println("Extracted authorId from answer: " + authorId);
        	
        	try {
        	   author = StatusData.databaseHelper.getUserById(authorId);
        	   
        	   if (author == null) {
	           		showAlert(Alert.AlertType.ERROR, "Author Not Found", "Author could not be found.");
	           		return;
           		}
           	
        	} catch (SQLException ex) {
        	    showAlert(Alert.AlertType.ERROR, "Database Error", "Could not retrieve author: " + ex.getMessage());
        	    ex.printStackTrace(); // optional: for debugging
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
        
        postReviewButton = new Button("Post Review");
        postReviewButton.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white;");
        
        postReviewButton.setOnAction(e -> {
            Answer selected = answerTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "No Answer Selected", "Select an answer before posting a review.");
                return;
            }

            // open ReviewPage showing reviews for this answer (in same stage)
            ReviewPage rp = new ReviewPage(this);
            // showForAnswer expects (Stage, User, Answer)
            rp.showForAnswer((Stage) answerTable.getScene().getWindow(), user, selected);
        });

        detailsBox.getChildren().addAll(answerDetails, postReviewButton);

        // Combine all sections
        VBox mainContent = new VBox(10);
        mainContent.getChildren().addAll(topSection, submitSection, tableBox, detailsBox);

        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");

        content.setCenter(scrollPane);
        mainPane.setCenter(content);

//        Scene scene = new Scene(mainPane, StatusData.WINDOW_WIDTH, StatusData.WINDOW_HEIGHT);
//        stage.setScene(scene);
//        stage.show();
        
        StatusData.setScene(stage, mainPane);

        answerTable.getSelectionModel().selectedItemProperty().addListener((_, _, newVal) -> {
        	if (newVal != null) {
        		displayAnswerDetails(newVal);
        		loadSuggestionsForAnswer(newVal);
        	}
        });
        loadAnswers();
    }

    private void submitAnswer(String content, boolean isSolution) {
        answers.create(
                user.getId(), // userId
                question.getQuestionId(), // questionId
                user.getName(), // author
                content // answer content
        );
    }
    
 
    @SuppressWarnings("unused")
	private void showClarificationPopup(Answer answer) {
		Stage popup = new Stage();
		popup.setTitle("Suggest Clarification");
		
		VBox layout = new VBox(10);
		layout.setPadding(new Insets(20));
		layout.setAlignment(Pos.CENTER_LEFT);
		
		Label titleA = new Label("Suggest Clarification for Answer # " + answer.getAnswerId());
		titleA.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
		
		TextArea input = new TextArea();
		input.setPromptText("Enter your suggestion here...");
		input.setWrapText(true);
		input.setPrefRowCount(5);
		input.setPrefWidth(150);
		
		Button submitButton = new Button("Submit");
		submitButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
		Button cancelButton = new Button("Cancel");
		
		HBox buttonBox = new HBox(10, cancelButton, submitButton);
		buttonBox.setAlignment(Pos.CENTER_RIGHT);
		
		layout.getChildren().addAll(titleA, input, buttonBox);
		
		Scene scene = new Scene(layout);
		popup.sizeToScene();
		popup.setScene(scene);
		popup.show();
		
		cancelButton.setOnAction(_ -> popup.close());
		
		submitButton.setOnAction(_ -> {
			String content = input.getText().trim();
			if (content.isEmpty()) {
				showAlert(Alert.AlertType.WARNING, "Empty Suggestion", "You must enter a suggestion before submitting");
				return;
			}
			
			ContentType contentType = ContentType.ANSWER;
			int contentId = answer.getAnswerId();
			
			try {
				ClarificationsManager clarifications = new ClarificationsManager(StatusData.databaseHelper);
				Result result = clarifications.create(
					    contentType,
					    contentId,
					    user.getId(),
					    -1,
					    user.getUserName(),
					    content
				);
				//System.out.println("Clarification create result: " + result.getMessage());
				//System.out.println("answer.getQId: " + answer.getQuestionId() + ", user.getId: " + user.getId() + ", recipientId: " + recipientId + ", user.getUserName: " + user.getUserName() + ", content: " + content);
				
				if (result.isSuccess()) {
					loadSuggestionsForAnswer(answer);
					showAlert(Alert.AlertType.INFORMATION, "Success", "Suggestion submitted successfully!");
					popup.close();
				}
				else {
					showAlert(Alert.AlertType.ERROR, "Error", result.getMessage());
				}
			} catch (SQLException ev) {
			    ev.printStackTrace();
			    showAlert(Alert.AlertType.ERROR, "Database Error", "Could not save clarification: " + ev.getMessage());
			}

		});

	}

    static void loadAnswers() {
        List<Answer> allAnswers = answers.readAll();
        List<Answer> questionAnswers = allAnswers.stream()
                .filter(a -> a.getQuestionId() == question.getQuestionId())
                .toList();
        answerTable.getItems().setAll(questionAnswers);

        // Update answer count (table title is first child of the VBox parent)
        if (answerTable.getParent() instanceof VBox) {
            VBox parent = (VBox) answerTable.getParent();
            if (!parent.getChildren().isEmpty() && parent.getChildren().get(0) instanceof Label) {
                Label tableTitle = (Label) parent.getChildren().get(0);
                tableTitle.setText("All Answers (" + questionAnswers.size() + ")");
            }
        }
    }

    private void loadSuggestionsForAnswer(Answer answer) {
      	 try {
      	        List<Clarification> list;
      	        if (isPrivilegedRole(user)) {
      	            // privileged: show *all* 
      	            list = StatusData.databaseHelper.loadClarificationsforA(answer.getAnswerId());
      	        } else {
      	            // nonprivileged
      	        	 List<Clarification> publicOnes = StatusData.databaseHelper.loadClarificationsforA(answer.getAnswerId())
      	                     .stream()
      	                     .filter(c -> c.isPublic())
      	                     .collect(Collectors.toList());
      	        	 
      	        	 List<Clarification> privateForUser = StatusData.databaseHelper.loadPrivateSuggestionsForUser(user.getId())
      	                     .stream()
      	                     .filter(c -> c.getAnswerId() == answer.getAnswerId())
      	                     .collect(Collectors.toList());
      	        	 
      	        	 list = new ArrayList<>();
      	             list.addAll(publicOnes);
      	             list.addAll(privateForUser);
      	        }
      	        
      	        suggestionsTable.getItems().setAll(list);
      	    } catch (SQLException e) {
      	        e.printStackTrace();
      	        // alert or set placeholder
      	    }
      }
      
   private boolean isPrivilegedRole(User user) {
       if (user == null || user.getRole() == null) return false;
       String role = user.getRole().name().toLowerCase();
       return role.contains("admin") || role.contains("staff") || role.contains("instructor") || role.contains("teacher") || role.contains("ta");
   }
   
       
    public static void reloadAnswers() {
        if (answerTable != null) {
            loadAnswers();
        }
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
		
		//reason for flag input
		Label reasonLabel = new Label("Reason for flagging:");
		TextArea reasonInput = new TextArea();
		reasonInput.setWrapText(true);
		reasonInput.setPromptText("Enter your reason here...");
		
		Button submitBtn = new Button("Submit");
		Button cancelBtn = new Button("Cancel");
		
		HBox buttons = new HBox(10, cancelBtn, submitBtn);
		layout.getChildren().addAll(userLabel, idLabel, reasonLabel, reasonInput, buttons);
		
		Scene scene = new Scene(layout, 400, 250);
		popup.setScene(scene);
		popup.initOwner(stage);
		popup.show();
		
		cancelBtn.setOnAction(event -> popup.close());
		
		submitBtn.setOnAction(event -> {
			String reason = reasonInput.getText().trim();
			if (reason.isEmpty()) {
				showAlert(Alert.AlertType.WARNING, "Missing Reason", "Please enter a reason for flagging.");
				return;
			}
			
			int userId = StatusData.currUser.getId();
			
			try {
				int flagID = StatusData.databaseHelper.insertModerationFlag(
						contentType.name().toLowerCase(),
						itemId, 
						userId, 
						reason
				);
				
		
				AnswersPage.this.reloadAnswers();

				if (StatusData.onFlagRefresh != null) {
				    StatusData.onFlagRefresh.run();
				}
				
				popup.close();
			} catch (SQLException ex) {
				ex.printStackTrace();
				showAlert(Alert.AlertType.ERROR, "Error", "Failed to flag question: " + ex.getMessage());
			}

		});
    }

    private int getAnswerCount() {
        List<Answer> allAnswers = answers.readAll();
        return (int) allAnswers.stream()
                .filter(a -> a.getQuestionId() == question.getQuestionId())
                .count();
    }

    private void displayAnswerDetails(Answer a) {
        StringBuilder details = new StringBuilder();
        details.append("Answer ID: ").append(a.getAnswerId()).append("\n");
        details.append("Question ID: ").append(a.getQuestionId()).append("\n");
        details.append("Author: ").append(a.getAuthor()).append("\n");
        details.append("Is Solution: ").append(a.isSolution() ? "Yes" : "No").append("\n");
        details.append("\n--- Full Answer Content ---\n\n");
        details.append(a.getContent());

        answerDetails.setText(details.toString());
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void editAnswer(Answer answer) {
        editingAnswer = answer;
        answerInput.setText(answer.getContent());
        markAsSolution.setSelected(answer.isSolution());

        submitTitle.setText("Edit Answer #" + answer.getAnswerId());
        submitButton.setVisible(false);
        submitButton.setManaged(false);
        updateAnswerButton.setVisible(true);
        updateAnswerButton.setManaged(true);
        deleteAnswerButton.setVisible(true);
        deleteAnswerButton.setManaged(true);
        cancelEditButton.setVisible(true);
        cancelEditButton.setManaged(true);
    }

    private void updateAnswer(String content) {
        if (editingAnswer == null) return;

        User.Role role = user.getRole();  
        boolean isAuthor = editingAnswer.getUserId() == user.getId();
        boolean isPrivileged =
                role == User.Role.ADMIN ||
                role == User.Role.STAFF ||
                role == User.Role.INSTRUCTOR ||
                role == User.Role.REVIEWER ||
                role == User.Role.TA; 

        // Only author or privileged roles can edit
        if (!isAuthor && !isPrivileged) {
            showAlert(Alert.AlertType.ERROR, "Permission Denied",
                    "Only the author or staff can edit this answer.");
            return;
        }

        Result result = answers.update(
                editingAnswer.getAnswerId(),
                editingAnswer.getQuestionId(),
                user,
                content,
                markAsSolution.isSelected()
        );

        if (result.isSuccess()) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Your answer has been updated!");
            clearAnswerForm();
            loadAnswers();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", result.getMessage());
        }
    }

    private void deleteAnswer(Answer answer) {
    	boolean isAuthor = answer.getUserId() == user.getId();
    	boolean isPrivileged = user.isPrivileged();

    	// Only author or privileged roles can delete
    	if (!isAuthor && !isPrivileged) {
    	    showAlert(Alert.AlertType.ERROR, "Permission Denied",
    	            "Only the author or privileged staff can delete this review.");
    	    return;
    	}

        // Confirm deletion
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete Answer?");
        confirmAlert.setContentText("Are you sure you want to delete this answer? This action cannot be undone.");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                Result result = answers.delete(answer.getAnswerId(), user);

                if (result.isSuccess()) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Answer deleted successfully!");
                    AnswersPage.reloadAnswers();
                    clearAnswerForm();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", result.getMessage());
                }
            }
        });
    }

    private boolean canSeeFlag(User user, List<ModerationFlag> flags) {
      	 String role = (user.getRole() != null)
                   ? user.getRole().name().toLowerCase()
                   : "";
      	 boolean isPrivileged = role.contains("admin") || role.contains("staff") || role.contains("instructor");
      	 boolean isFlagger = flags.stream().anyMatch(f -> f.getStaffId() == user.getId());
      	 return isPrivileged || isFlagger;
   }
    
    private void clearAnswerForm() {
        editingAnswer = null;
        answerInput.clear();
        markAsSolution.setSelected(false);

        submitTitle.setText("Submit Your Answer");
        submitButton.setVisible(true);
        submitButton.setManaged(true);
        updateAnswerButton.setVisible(false);
        updateAnswerButton.setManaged(false);
        deleteAnswerButton.setVisible(false);
        deleteAnswerButton.setManaged(false);
        cancelEditButton.setVisible(false);
        cancelEditButton.setManaged(false);
    }


}