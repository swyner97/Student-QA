package pages;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import logic.StatusData;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.sql.SQLException;
import java.sql.Timestamp;

import databasePart1.DatabaseHelper;
import model.Answer;
import model.Messages;
import model.ModerationFlag;
import model.ModerationNote;
import model.NavigationBar;
import model.Question;
import model.User;

public class ModerationHandlingPage {
	private final DatabaseHelper databaseHelper;
    private final User currentUser;
    
    private Stage stage;

    public ModerationHandlingPage(DatabaseHelper dbHelper, User user) {
        this.databaseHelper = dbHelper;
        this.currentUser = user;
    }

	public void show(Stage stage) {
        BorderPane root = new BorderPane();
        root.setTop(new NavigationBar());

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(15));

        Label header = new Label("Moderation Center");
        header.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        
        Label flagsLabel = new Label("Moderation Flags / Activity Logs");
		TableView<ModerationFlag> flagsTable = new TableView<>();
		flagsTable.setPrefHeight(250);
		
        //TableView<User> flaggedUserTable = new TableView<>();
        TableView<ModerationFlag> flagTable = new TableView<>();

        //ModerationFlag Table
        TableColumn<ModerationFlag, String> typeCol = new TableColumn<>("Item Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("itemType"));

        TableColumn<ModerationFlag, Integer> idCol = new TableColumn<>("Item ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("itemId"));

        TableColumn<ModerationFlag, String> reasonCol = new TableColumn<>("Reason");
        reasonCol.setCellValueFactory(new PropertyValueFactory<>("reason"));

        TableColumn<ModerationFlag, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        TableColumn<ModerationFlag, String> fNotesCol = new TableColumn<>("Notes");
		fNotesCol.setPrefWidth(200);
		fNotesCol.setCellValueFactory(cellData -> {
			ModerationFlag flag = cellData.getValue();
			if (flag == null) {
				return new SimpleStringProperty("");
			}
			try {
				var notes = StatusData.databaseHelper.loadModerationNotesForFlag(flag.getFlagId());
				if (notes == null || notes.isEmpty()) {
					return new SimpleStringProperty("");
				}
				
				List<String> noteTexts = new ArrayList<>();
				for (ModerationNote note : notes) {
					noteTexts.add(note.getNoteText());
				}
				
				String joined = String.join(" | ", noteTexts);
				return new SimpleStringProperty(joined);
			} catch (SQLException e) {
				e.printStackTrace();
				return new SimpleStringProperty("[Error loading notes]");
			}
		});
		
		TableColumn<ModerationFlag, String> fContentCol = new TableColumn<>("Flagged Content");
		fContentCol.setPrefWidth(300);
		fContentCol.setCellValueFactory(cellData -> {
			ModerationFlag flag = cellData.getValue();
			if (flag == null) {
				return new SimpleStringProperty("");
			}
			
			String itemType = flag.getItemType().toLowerCase();
		    String content = StatusData.databaseHelper.retrieveFlaggedContent(itemType, flag.getItemId());
		    
			return new SimpleStringProperty(content);
		});
		
        TableColumn<ModerationFlag, String> dateCol = new TableColumn<>("Created At");
        dateCol.setCellValueFactory(cellData -> {
            Timestamp ts = cellData.getValue().getCreatedAt();
            return new SimpleStringProperty(ts.toLocalDateTime().format(formatter));
        });

        flagTable.getColumns().addAll(typeCol, idCol, fContentCol, statusCol, reasonCol, fNotesCol, dateCol);

        // Load data
        ObservableList<ModerationFlag> flags = null;
		try {
			flags = FXCollections.observableArrayList(
			    databaseHelper.loadAllModerationFlags()
			);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        flagTable.setItems(flags);

        // Row double-click or cell button to view content
        flagTable.setRowFactory(tv -> {
            TableRow<ModerationFlag> row = new TableRow<>();
            row.setOnMouseClicked(evt -> {
                if (!row.isEmpty() && evt.getClickCount() == 2) {
                    ModerationFlag flag = row.getItem();
                    showFlagDetailPopup(flag, stage);
                }
            });
            return row;
        });

     // Questions section
 		Label questionsLabel = new Label("Questions");
 		TableView<Question> questionsTable = new TableView<>();
 		questionsTable.setPrefHeight(200);
 		
 		TableColumn<Question, Integer> qIdCol = new TableColumn<>("Q-ID");
 		qIdCol.setCellValueFactory(new PropertyValueFactory<>("questionId"));
 		qIdCol.setPrefWidth(50);
 		
 		TableColumn<Question, String> qTitleCol = new TableColumn<>("Title");
 		qTitleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
 		qTitleCol.setPrefWidth(130);
 		
 		TableColumn<Question, String> qAuthorCol = new TableColumn<>("Author");
 		qAuthorCol.setCellValueFactory(new PropertyValueFactory<>("author"));
 		
 		TableColumn<Question, String> qContentCol = new TableColumn<>("Question Content");
 		qContentCol.setCellValueFactory(new PropertyValueFactory<>("description"));
 		qContentCol.setPrefWidth(500);
 		
 		questionsTable.getColumns().addAll(qIdCol, qTitleCol, qAuthorCol, qContentCol);
 		
 		List<Question> allQuestions = StatusData.databaseHelper.loadAllQs(); //StatusData.questions.search(null, null, null);
 		questionsTable.getItems().setAll(allQuestions);
 		
 		VBox questionsSection = new VBox(5, questionsLabel, questionsTable);
 		questionsSection.setPadding(new Insets(10));
 		VBox.setVgrow(questionsTable, Priority.NEVER);
 		
 		// Answers section
 		Label answersLabel = new Label("Answers for Selected Question");
 		TableView<Answer> answersTable = new TableView<>();
 		answersTable.setPrefHeight(200);
 		
 		TableColumn<Answer, Integer> aIdCol = new TableColumn<>("A-ID");
 		aIdCol.setCellValueFactory(new PropertyValueFactory<>("answerId"));
 		aIdCol.setPrefWidth(50);
 		
 		TableColumn<Answer, Integer> aAuthorCol = new TableColumn<>("Author");
 		aAuthorCol.setCellValueFactory(new PropertyValueFactory<>("author"));
 		
 		TableColumn<Answer, String> aContentCol = new TableColumn<>("Content");
 		aContentCol.setCellValueFactory(new PropertyValueFactory<>("content"));
 		aContentCol.setPrefWidth(500);
 		
 		answersTable.getColumns().addAll(aIdCol, aAuthorCol, aContentCol);
 		
 		questionsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldQ, newQ) -> {
 			if (newQ != null) {
 				//List<Answer> answers = StatusData.answers.search(newQ.getQuestionId(), null, null);
 				//replacing the previous List<answer> to see if calling from the DB will populate the table
 				List<Answer> answers = StatusData.databaseHelper.loadAnswersForQs(newQ.getQuestionId());
 				answersTable.getItems().setAll(answers);
 			} else {
 				answersTable.getItems().clear();
 			}
 		});
 		
 		VBox answersSection = new VBox(5, answersLabel, answersTable);
 		answersSection.setPadding(new Insets(10));
 		VBox.setVgrow(answersTable,  Priority.NEVER);
     		
 		Button flagQuestionsButton = new Button("Flag Selected Question");
		Button flagAnswerButton = new Button("Flag Selected Answer");
		Button resolveButton = new Button("Mark Flag as Resolved");
		Button dismissButton = new Button("Dismiss Flag");
		Button addNoteButton = new Button("Add Note to Flag");
		
//		flagQuestionsButton.setOnAction(e -> handleFlagQuestion(questionsTable, flagTable));
//		flagAnswerButton.setOnAction(e -> handleFlagAnswer(answersTable, flagTable));

	    resolveButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");
	    resolveButton.setOnAction(e -> {
	    	ModerationFlag flag = flagTable.getSelectionModel().getSelectedItem();
			if (flag == null) {
				showAlert(Alert.AlertType.WARNING, "No Flag Selected", "Please select a flag first.");
				return;
			}
	        try {
	            StatusData.databaseHelper.updateModerationFlagStatus(flag.getFlagId(), "closed");
	            showAlert(Alert.AlertType.INFORMATION, "Flag Update", "Flag updated to " + "CLOSED" + ".");
				reloadFlags(flagTable);
	        } catch (SQLException ex) {
	            ex.printStackTrace();
	            showAlert(Alert.AlertType.ERROR, "Error", "Failed to resolve flag.");
	        }
	    });
	    
		//dismissButton.setOnAction(e -> handleUpdateFlagStatus(flagTable, "DISMISSED"));
		addNoteButton.setOnAction(e -> {
			Dialog<String> noteDialog = new Dialog<>();
			//replyDialog.setTitle("Messaging " + recipientUserName +  ":");
			noteDialog.setHeaderText("Notes:");
			noteDialog.setContentText("Add notes");
			
			 // Set button types
		    ButtonType noteButtonType = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
		    noteDialog.getDialogPane().getButtonTypes().addAll(noteButtonType, ButtonType.CANCEL);
		    
		    //Text area for reply 
		    TextArea noteArea = new TextArea();
		    noteArea.setPromptText("Add your notes...");
		    noteArea.setWrapText(true);
		    noteArea.setPrefRowCount(10);
		    noteArea.setPrefColumnCount(40);
		    
		    noteDialog.getDialogPane().setContent(noteArea);
		    
		    noteDialog.setResultConverter(dialogButton -> {
		    		if (dialogButton == noteButtonType) {
		    			return noteArea.getText();
		    		}
		    		return null;
		    });
		    
			noteDialog.showAndWait().ifPresent(noteText -> {
				ModerationFlag flag = flagTable.getSelectionModel().getSelectedItem();
				if (noteText.trim().isEmpty()) {
					new Alert(Alert.AlertType.WARNING, "No new notes.").show();
					return;
				}
				
				try {
					StatusData.databaseHelper.insertModerationNote(flag.getFlagId(), StatusData.currUser.getId(), noteArea.getText().trim());
					showAlert(Alert.AlertType.INFORMATION, "Note Added", "Your note has been added.");
					reloadFlags(flagsTable);
				} catch (SQLException ex) {
					ex.printStackTrace();
					showAlert(Alert.AlertType.ERROR, "Error Adding Note", "Error adding note: " + ex.getMessage());
				}
			});		
		});
		
		HBox flagButtons = new HBox(10, flagQuestionsButton, flagAnswerButton, resolveButton, dismissButton, addNoteButton);
		flagButtons.setPadding(new Insets(5, 0, 0, 0));
		
		VBox flagsSection = new VBox(5, flagsLabel, flagTable, flagButtons);
		flagsSection.setPadding(new Insets(10));
		VBox.setVgrow(flagTable, Priority.NEVER);
		
		// Layout
		VBox mainContent = new VBox(15, questionsSection, answersSection, flagsSection);
		mainContent.setPadding(new Insets(10));
		VBox.setVgrow(mainContent,  Priority.ALWAYS);
		
		ScrollPane scrollPane = new ScrollPane(mainContent);
		scrollPane.setFitToWidth(true);
		
		root.setCenter(scrollPane);
//        layout.getChildren().addAll(header, flagTable);
//        root.setCenter(layout);

        
        stage.setTitle("Instructor Moderation Center");
        StatusData.setScene(stage, root);
    }

	
    private void showFlagDetailPopup(ModerationFlag flag, Stage owner) {
        // Popup with full item content, notes list, add note field, and resolve button
    	Stage popup = new Stage();
    	popup.setTitle("Flag Details: Flag ID #" + flag.getFlagId());
    	
    	TabPane flagTabs = new TabPane();
    	
    	HBox topSection = new HBox(20);
    	topSection.setPadding(new Insets(15));
        topSection.setAlignment(Pos.TOP_LEFT);
        topSection.setFillHeight(true);
        
    	//flagged content info
        VBox contentBox = new VBox(10);
        Label contentHeader = new Label("Flagged Content:");
        contentHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        Label contentDetails = new Label();
        contentDetails.setWrapText(true);

        User flaggedUser = null;
        String flaggedContent = null;
        String itemType = null;
        
        try {
        	itemType = flag.getItemType().toLowerCase();
        	int itemId = flag.getItemId();
        	//debug
        	System.out.println("DEBUG: retrieving user for itemType=" + itemType + " itemId=" + itemId);
            flaggedUser = databaseHelper.retrieveFlaggedUser(itemType, flag.getItemId());
            flaggedContent = databaseHelper.retrieveFlaggedContent(flag.getFormattedItemType(), flag.getItemId());
            
            String userInfo = (flaggedUser != null)
                    ? String.format("User ID: %d\nUsername: %s\nName: %s\nRoll: %s\nEmail: %s\n\nContent:\n%s",
                        flaggedUser.getId(),
                        flaggedUser.getUserName(),
                        flaggedUser.getName() != null ? flaggedUser.getName() : "N/A",	
    	                flaggedUser.getRoleName(),
    	                flaggedUser.getEmail() != null ? flaggedUser.getEmail() : "N/A",
    	                flaggedContent != null ? flaggedContent : "(No content)")
                    : "Unable to load flagged user info.";

                contentDetails.setText(userInfo);
        } catch (Exception e) {
        	contentDetails.setText("Error loading flagged content.");
            e.printStackTrace();
        }
          
        contentDetails.setMaxWidth(Double.MAX_VALUE);
        contentDetails.setMaxHeight(Double.MAX_VALUE);
        contentBox.getChildren().addAll(contentHeader, contentDetails);
        contentBox.setPrefWidth(300);
    	
        //flag details info
        VBox flagBox = new VBox(10);
        Label flagHeader = new Label("Flag Details:");
        flagHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
    	
    	String flaggedContentType = flag.getFormattedItemType();
    	int flaggedContentId = flag.getItemId();
    	//Details for flag
    	Label details = new Label(String.format(
    	        "Item Type: %s\nItem ID: %d\nFlagged By (User ID): %d\nStatus: %s\nReason: %s\nCreated At: %s",
    	        flaggedContentType,
    	        flaggedContentId,
    	        flag.getStaffId(),
    	        flag.getStatus(),
    	        flag.getReason() != null ? flag.getReason() : "N/A",
    	        flag.getCreatedAt().toString()
	    ));
	    details.setWrapText(true);
	    
	    flagBox.getChildren().addAll(flagHeader, details);
	    flagBox.setPrefWidth(300);
	    details.setMaxWidth(Double.MAX_VALUE); 
	    details.setMaxHeight(Double.MAX_VALUE);
	   
	    HBox.setHgrow(contentBox, Priority.ALWAYS);
	    HBox.setHgrow(flagBox, Priority.ALWAYS);
	    topSection.getChildren().addAll(contentBox, flagBox);

	    VBox topWrapper = new VBox(topSection);
	    topWrapper.setPadding(new Insets(10));
	    topWrapper.setSpacing(10);

	    ScrollPane topScrollPane = new ScrollPane(topWrapper);
	    topScrollPane.setFitToWidth(true);  
	    topScrollPane.setFitToHeight(false); 
	    topScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
	    topScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
	    
	    String flaggedUserName = databaseHelper.retrieveFlaggedUser(flaggedContentType, flaggedContentId).getUserName();
	    int flaggedUserId = databaseHelper.retrieveFlaggedUser(flaggedContentType, flaggedContentId).getId();
	    
	    Button messageUser = new Button("Message " + flaggedUserName);
	    //messageUser.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
	    //contentTextLayout.getChildren().addAll(messageUser);
	    messageUser.setOnAction(e -> {
	    	int recipientId = 0;
			try {
				recipientId = databaseHelper.getUserById(flaggedUserId).getId();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	        		showReplyDialog(recipientId);
	    });

	    // Notes (if any)
	    //Notes section header
	    VBox notesBox = new VBox(5);
	    notesBox.setPadding(new Insets(10, 0, 0, 0));
	    Label notesHeader = new Label("Notes:");
	    notesHeader.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

	    notesBox.getChildren().add(new Label("Notes:"));
	    VBox notesList = new VBox(5);
	    try {
	        List<ModerationNote> notes = StatusData.databaseHelper.loadModerationNotesForFlag(flag.getFlagId());
	        for (ModerationNote note : notes) {
	            Label noteLabel = new Label(String.format("- [%s] Staff #%d: %s",
	                note.getCreatedAt(), note.getStaffId(), note.getNoteText()));
	            noteLabel.setWrapText(true);
	            notesList.getChildren().add(noteLabel);
	        }
	    } catch (SQLException e) {
	        notesList.getChildren().add(new Label("Error loading notes."));
	        e.printStackTrace();
	    }
	    
	    //scroll pane for displayed notes
	    ScrollPane scrollableNotes = new ScrollPane(notesList);
	    scrollableNotes.setFitToWidth(true);
	    scrollableNotes.setPrefHeight(150); 

	    // Add note field
	    TextArea noteInput = new TextArea();
	    noteInput.setPromptText("Add a note...");
	    noteInput.setWrapText(true);
	    noteInput.setPrefRowCount(10);

	    Button addNoteButton = new Button("Add Note");
	    addNoteButton.setOnAction(e -> {
	        String noteText = noteInput.getText().trim();
	        if (!noteText.isEmpty()) {
	            try {
	                StatusData.databaseHelper.insertModerationNote(flag.getFlagId(), currentUser.getId(), noteText);
	                popup.close();
	                showFlagDetailPopup(flag, owner); // Refresh popup
	            } catch (SQLException ex) {
	                ex.printStackTrace();
	                showAlert(Alert.AlertType.ERROR, "Error", "Failed to save note.");
	            }
	        }
	    });

	    // Resolve button
	    Button resolveButton = new Button("Resolve Flag");
	    resolveButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");
	    resolveButton.setOnAction(e -> {
	        try {
	            StatusData.databaseHelper.updateModerationFlagStatus(flag.getFlagId(), "closed");
	            popup.close();
	            showAlert(Alert.AlertType.INFORMATION, "Resolved", "Flag has been resolved.");
	            show(owner); // Refresh the main table view
	        } catch (SQLException ex) {
	            ex.printStackTrace();
	            showAlert(Alert.AlertType.ERROR, "Error", "Failed to resolve flag.");
	        }
	    });
	    
	   
	    HBox actionButtons = new HBox(10, addNoteButton, resolveButton);
	    actionButtons.setAlignment(Pos.CENTER_RIGHT);
	    
	    VBox bottomSection = new VBox(10, messageUser, notesHeader, scrollableNotes, noteInput, actionButtons);
	    bottomSection.setPadding(new Insets(10));
	    
	    VBox layout = new VBox(10, topScrollPane, bottomSection);
	    layout.setPadding(new Insets(10));

	    //tab2
	    VBox messagesLayout = new VBox(10);
	    messagesLayout.setStyle("-fx-padding: 15;");
	    
	    //placeholder
	    Label messagesHeader = new Label("Messages about this Flag:");
	    messagesHeader.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

	    ListView<String> messageList = new ListView<>();
	    messageList.setPrefHeight(400);
	    // TODO: Replace with actual message data retrieval:
	    messageList.setItems(FXCollections.observableArrayList(
	        "Staff #190: Please provide more info.",
	        "Bob: Here's the GitHub repo link.",
	        "Staff #190: Got it, thanks!"
	    ));

	    messagesLayout.getChildren().addAll(messagesHeader, messageList);
	    
	    
	    Tab infoTab = new Tab("Info", layout);
	    infoTab.setClosable(false);
	 
	    Tab messagesTab = new Tab("Messages", messagesLayout);
	    messagesTab.setClosable(false);

	    // Add both tabs
	    flagTabs.getTabs().addAll(infoTab, messagesTab);
	    
	    popup.setScene(new Scene(flagTabs, 950, 650));
	    popup.initOwner(owner);
	    popup.show();
	}
    
    private void showReplyDialog(int recipientId) {
    	User recipient = null;
		try {
			recipient = databaseHelper.getUserById(recipientId);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	String recipientUserName = recipient.getUserName();
		Dialog<String> replyDialog = new Dialog<>();
		//replyDialog.setTitle("Messaging " + recipientUserName +  ":");
		replyDialog.setHeaderText("Messaging " + recipientUserName +  ":");
		replyDialog.setContentText("Your message:");
		
		 // Set button types
	    ButtonType sendButtonType = new ButtonType("Send", ButtonBar.ButtonData.OK_DONE);
	    replyDialog.getDialogPane().getButtonTypes().addAll(sendButtonType, ButtonType.CANCEL);
	    
	    //Text area for reply 
	    TextArea replyArea = new TextArea();
	    replyArea.setPromptText("Enter your reply...");
	    replyArea.setWrapText(true);
	    replyArea.setPrefRowCount(10);
	    replyArea.setPrefColumnCount(40);
	    
	    replyDialog.getDialogPane().setContent(replyArea);
	    
	    replyDialog.setResultConverter(dialogButton -> {
	    		if (dialogButton == sendButtonType) {
	    			return replyArea.getText();
	    		}
	    		return null;
	    });
	    
		replyDialog.showAndWait().ifPresent(replyText -> {
			if (replyText.trim().isEmpty()) {
				new Alert(Alert.AlertType.WARNING, "Message cannot be empty.").show();
				return;
			}
			
			Messages reply = new Messages(
					currentUser.getId(),
					recipientId,
					replyText
			);
			reply.setSenderName(currentUser.getUserName());
			reply.setRecipientName(recipientUserName);
			
			databaseHelper.sendMessage(reply);
			new Alert(Alert.AlertType.INFORMATION, "Reply sent successfully!").show();
			//MessagingPage.loadMessages();
		});		
	}

    private void reloadFlags(TableView<ModerationFlag> table) {
		try {
			var flags = StatusData.databaseHelper.loadAllModerationFlags();
			table.getItems().setAll(flags);
		} catch (SQLException ex) {
			ex.printStackTrace();
			showAlert(Alert.AlertType.ERROR, "Error Loading Flags", "Error laoding flags: " + ex.getMessage());
		}
	}
	
	public void refreshUsers() {
	   	 if (this.stage != null) {
	   	        show(this.stage);
   	    }
	}
	
	private void showAlert(AlertType error, String string, String string2) {
		// TODO Auto-generated method stub
		
    }
}

