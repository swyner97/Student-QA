package pages;

import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import logic.StatusData;
import model.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;


/**
 * Staff moderation dashboard: shows questions/answers/clarifications and moderation flags.
 */
public class StaffPage {
	
	private Stage stage;
	
	public void show(Stage stage) {
		this.stage = stage;
		
		// Top navigation
		BorderPane root = new BorderPane();
		root.setTop(new NavigationBar());
		
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
		
		// Moderation Flags/Activity
		Label flagsLabel = new Label("Moderation Flags / Activity Logs");
		TableView<ModerationFlag> flagsTable = new TableView<>();
		flagsTable.setPrefHeight(250);
		
		TableColumn<ModerationFlag, Integer> fIdCol = new TableColumn<>("Flag ID");
		fIdCol.setCellValueFactory(new PropertyValueFactory<>("flagId"));
		
		TableColumn<ModerationFlag, String> fTypeCol = new TableColumn<>("Item Type");
		fTypeCol.setCellValueFactory(new PropertyValueFactory<>("itemType"));
		
		TableColumn<ModerationFlag, Integer> fTypeIdCol = new TableColumn<>("Item ID");
		fTypeIdCol.setCellValueFactory(new PropertyValueFactory<>("itemId"));
		
		TableColumn<ModerationFlag, String> fStatusCol = new TableColumn<>("Flag Status");
		fStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
		fStatusCol.setPrefWidth(100);
		
		TableColumn<ModerationFlag, String> fReasonCol = new TableColumn<>("Reason for Flag");
		fReasonCol.setCellValueFactory(new PropertyValueFactory<>("reason"));
		fReasonCol.setPrefWidth(100);
		
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
			
			String itemType = flag.getItemType().toUpperCase();
		    String content = StatusData.databaseHelper.retrieveFlaggedContent(itemType, flag.getItemId());
		    
			return new SimpleStringProperty(content);
		});
		
		flagsTable.getColumns().addAll(fIdCol, fTypeCol, fTypeIdCol, fStatusCol, fReasonCol, fNotesCol, fContentCol);
		//reloadFlags(flagsTable);  setting to observable list so user can double click
		// Load flags from DB
		List<ModerationFlag> flags = new ArrayList<>();
		try {
			flags = StatusData.databaseHelper.loadAllModerationFlags();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		flagsTable.setItems(FXCollections.observableArrayList(flags));
		
		//allow the double click function
		// Let user double-click to see details
		flagsTable.setRowFactory(tv -> {
		    TableRow<ModerationFlag> row = new TableRow<>();
		    row.setOnMouseClicked(evt -> {
		        if (!row.isEmpty() && evt.getClickCount() == 2) {
		            ModerationFlag flag = row.getItem();
		            showFlagDetailPopup(flag, stage);
		        }
		    });
		    return row;
		});
		
		Button flagQuestionsButton = new Button("Flag Selected Question");
		Button flagAnswerButton = new Button("Flag Selected Answer");
		Button resolveButton = new Button("Mark Flag as Resolved");
		Button dismissButton = new Button("Dismiss Flag");
		Button addNoteButton = new Button("Add Note to Flag");
		
		flagQuestionsButton.setOnAction(e -> handleFlagQuestion(questionsTable, flagsTable));
		flagAnswerButton.setOnAction(e -> handleFlagAnswer(answersTable, flagsTable));
		resolveButton.setOnAction(e -> handleUpdateFlagStatus(flagsTable, "RESOLVED"));
		dismissButton.setOnAction(e -> handleUpdateFlagStatus(flagsTable, "DISMISSED"));
		addNoteButton.setOnAction(e -> handleAddNote(flagsTable));
		
		HBox flagButtons = new HBox(10, flagQuestionsButton, flagAnswerButton, resolveButton, dismissButton, addNoteButton);
		flagButtons.setPadding(new Insets(5, 0, 0, 0));
		
		VBox flagsSection = new VBox(5, flagsLabel, flagsTable, flagButtons);
		flagsSection.setPadding(new Insets(10));
		VBox.setVgrow(flagsTable, Priority.NEVER);
		
		// Layout
		VBox mainContent = new VBox(15, questionsSection, answersSection, flagsSection);
		mainContent.setPadding(new Insets(10));
		VBox.setVgrow(mainContent,  Priority.ALWAYS);
		
		ScrollPane scrollPane = new ScrollPane(mainContent);
		scrollPane.setFitToWidth(true);
		
		root.setCenter(scrollPane);
		
		StatusData.setScene(stage, root);
		stage.setTitle("Staff Moderation Dashboard");
		stage.show();
	}
	
	
	// Helper methods
	private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
	
	private String askForText(String title, String prompt) {
		TextInputDialog dialog = new TextInputDialog();
		dialog.setTitle(title);
		dialog.setHeaderText(null);
		dialog.setContentText(prompt);
		return dialog.showAndWait().orElse(null);
	}
	
	private void showFlagDetailPopup(ModerationFlag flag, Stage ownerStage) {
	    Stage popup = new Stage();
	    popup.initOwner(ownerStage);

	    VBox box = new VBox(10);
	    box.setPadding(new Insets(10));

	    Label type = new Label("Type: " + flag.getItemType());
	    Label id = new Label("Item ID: " + flag.getItemId());
	    Label reason = new Label("Reason: " + flag.getReason());

	    
	    String itemType = flag.getItemType().toUpperCase();
	    String content = StatusData.databaseHelper.retrieveFlaggedContent(itemType, flag.getItemId());
	    
	    if (content == null) {
	    	content = "[Error loading content]";
	    }

	    Label contentLabel = new Label("Content: " + content);
	    contentLabel.setWrapText(true);

	    box.getChildren().addAll(type, id, reason, contentLabel);

	    Button resolveButton = new Button("Mark Flag as Resolved");
		Button dismissButton = new Button("Dismiss Flag");
		Button addNoteButton = new Button("Add Note to Flag");
		
		resolveButton.setOnAction(e -> {
			 try {
			        StatusData.databaseHelper.updateModerationFlagStatus(flag.getFlagId(), "closed");
			        showAlert(Alert.AlertType.INFORMATION, "Flag Updated", "Flag marked as resolved.");
			        reloadFlagsFromPopup(ownerStage);
		 } catch (SQLException ex) {
			        ex.printStackTrace();
			        showAlert(Alert.AlertType.ERROR, "Error", "Failed to update flag.");
		 }
		});
		
		dismissButton.setOnAction(e -> {
		    try {
		        StatusData.databaseHelper.updateModerationFlagStatus(flag.getFlagId(), "DISMISSED");
		        showAlert(Alert.AlertType.INFORMATION, "Flag Updated", "Flag dismissed.");
		        reloadFlagsFromPopup(ownerStage);
		    } catch (SQLException ex) {
		        ex.printStackTrace();
		        showAlert(Alert.AlertType.ERROR, "Error", "Failed to dismiss flag.");
		    }
		});
		
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


		addNoteButton.setOnAction(e -> {
			String noteText = noteInput.getText().trim();
	        if (!noteText.isEmpty()) {
	            try {
	                StatusData.databaseHelper.insertModerationNote(flag.getFlagId(), StatusData.currUser.getId(), noteText);
	                popup.close();
	                showFlagDetailPopup(flag, ownerStage); // Refresh popup
	            } catch (SQLException ex) {
	                ex.printStackTrace();
	                showAlert(Alert.AlertType.ERROR, "Error", "Failed to save note.");
	            }
	        }
		});
		
		HBox buttonBox = new HBox(10, resolveButton, dismissButton, addNoteButton);
		VBox layout = new VBox(10, box, notesBox, scrollableNotes, noteInput, buttonBox);
		layout.setPadding(new Insets(10));
		
	    Scene scene = new Scene(layout, 600, 450);
	    popup.setScene(scene);
	    popup.setTitle("Flag Details");
	    popup.show();
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
	
	private void reloadFlagsFromPopup(Stage ownerStage) {
	    show(ownerStage);
	}
	
	private void handleFlagQuestion(TableView<Question> questionsTable, TableView<ModerationFlag> flagsTable) {
		Question q = questionsTable.getSelectionModel().getSelectedItem();
		if (q == null) {
			showAlert(Alert.AlertType.WARNING, "No Question Selected", "Please select a question to flag.");
			return;
		}
		String reason = askForText("Flag Question", "Enter a reason for flagging this question:");
		if (reason == null) return;
		
		try {
			StatusData.databaseHelper.insertModerationFlag(
					"QUESTION",
					q.getQuestionId(),
					StatusData.currUser.getId(),
					reason
			);
			showAlert(Alert.AlertType.INFORMATION, "Question Flagged", "The question has been flagged.");
			reloadFlags(flagsTable);
		} catch (SQLException ex) {
			ex.printStackTrace();
			showAlert(Alert.AlertType.ERROR, "Error Flagging Question", "Error flagging question: " + ex.getMessage());
		}
	}
	
	private void handleFlagAnswer(TableView<Answer> answersTable, TableView<ModerationFlag> flagsTable) {
		Answer a = answersTable.getSelectionModel().getSelectedItem();
		if (a == null) {
			showAlert(Alert.AlertType.WARNING, "No Answer Selected", "Please select an answer to flag.");
			return;
		}
		String reason = askForText("Flag Answer", "Enter a reason for flagging this answer:");
		if (reason == null) return;
		
		try {
			StatusData.databaseHelper.insertModerationFlag("ANSWER", a.getAnswerId(),StatusData.currUser.getId(), reason);
			reloadFlags(flagsTable);
		} catch (SQLException ex) {
			ex.printStackTrace();
			showAlert(Alert.AlertType.ERROR, "Error Flagging Answer", "Error flagging answer: " + ex.getMessage());
		}
	}
	
	private void handleUpdateFlagStatus(TableView<ModerationFlag> flagsTable, String newStatus) {
		ModerationFlag flag = flagsTable.getSelectionModel().getSelectedItem();
		if (flag == null) {
			showAlert(Alert.AlertType.WARNING, "No Flag Selected", "Please select a flag first.");
			return;
		}
		try {
			StatusData.databaseHelper.updateModerationFlagStatus(flag.getFlagId(), newStatus);
			showAlert(Alert.AlertType.INFORMATION, "Flag Update", "Flag updated to " + newStatus + ".");
			reloadFlags(flagsTable);
		} catch (SQLException ex) {
			ex.printStackTrace();
			showAlert(Alert.AlertType.ERROR, "Error Updating Flag", "Error updating flag: " + ex.getMessage());
		}
	}
	
	private void handleAddNote(TableView<ModerationFlag> flagsTable) {
		ModerationFlag flag = flagsTable.getSelectionModel().getSelectedItem();
		if (flag == null) {
			showAlert(Alert.AlertType.WARNING, "No Flag Selected", "Please select a flag first.");
			return;
		}
		
		String note = askForText("Add Note", "Enter an internal note for this flag:");
		if (note == null) return;
		
		try {
			StatusData.databaseHelper.insertModerationNote(flag.getFlagId(), StatusData.currUser.getId(), note);
			showAlert(Alert.AlertType.INFORMATION, "Note Added", "Your note has been added.");
			reloadFlags(flagsTable);
		} catch (SQLException ex) {
			ex.printStackTrace();
			showAlert(Alert.AlertType.ERROR, "Error Adding Note", "Error adding note: " + ex.getMessage());
		}
	}
	
	public void refreshUsers() {
	   	 if (this.stage != null) {
	   	        show(this.stage);
	   	    }
	    }
	   
}
