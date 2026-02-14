package pages;

import javafx.collections.FXCollections;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.beans.property.SimpleStringProperty;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

import logic.*;

import model.*;
import databasePart1.*;

public class MessagingPage {
	private final DatabaseHelper databaseHelper = StatusData.databaseHelper;
	private final User currentUser = StatusData.currUser;
	
	private TableView<Messages> messageTable;
	private TableView<Messages> sentMessageTable;
	private TableView<Clarification> suggestionTable;
	private TextArea messageContentArea;
	private TextArea suggestionContentArea;
	private ComboBox<String> recipientDropDown;
	
	Label unreadCountLabel;
	Label unreadSuggestionsCount;
	
	public void show(Stage stage) {
		stage.setTitle("Messages");
		
		BorderPane mainPane = new BorderPane();
		
		NavigationBar navBar = new NavigationBar();
        mainPane.setTop(navBar);
        // Center content
        BorderPane content = new BorderPane();
        content.setPadding(new Insets(15));
        
        Label title = new Label("Your Messages");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        unreadCountLabel = new Label();
        unreadCountLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        unreadSuggestionsCount = new Label();
        unreadSuggestionsCount.setStyle("-fx-text-fill: blue; -fx-font-weight: bold;");
        HBox headerBox = new HBox(10, title, unreadCountLabel, unreadSuggestionsCount);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        messageTable = new TableView<>();
        messageTable.setPrefHeight(250);
        
        TableColumn<Messages, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().isRead() ? "" : "🆕"));
        statusCol.setPrefWidth(60);
        
        TableColumn<Messages, String> fromCol = new TableColumn<>("From");
        fromCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSenderName()));
        fromCol.setPrefWidth(120);
        
        TableColumn<Messages, String> sentToCol = new TableColumn<>("To");
        sentToCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRecipientName()));
        sentToCol.setPrefWidth(120);
        
        TableColumn<Clarification, String> sugFromCol = new TableColumn<>("From");
        sugFromCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAuthor()));
        sugFromCol.setPrefWidth(120);
        
        TableColumn<Messages, String> previewCol = new TableColumn<>("Message");
        previewCol.setCellValueFactory(data -> new SimpleStringProperty(
        		data.getValue().getMessage().length() > 30 ?
        		data.getValue().getMessage().substring(0, 30) + "..." :
        		data.getValue().getMessage()
        ));
        previewCol.setPrefWidth(250);
        
        TableColumn<Messages, String> sentPreviewCol = new TableColumn<>("Message");
        sentPreviewCol.setCellValueFactory(data -> new SimpleStringProperty(
            data.getValue().getMessage().length() > 30 ?
            data.getValue().getMessage().substring(0, 30) + "..." :
            data.getValue().getMessage()
        ));
        sentPreviewCol.setPrefWidth(250);
        
        TableColumn<Clarification, String> sugPreviewCol = new TableColumn<>("Suggestion");
        sugPreviewCol.setCellValueFactory(data -> new SimpleStringProperty(
            data.getValue().getContent().length() > 30 ?
            data.getValue().getContent().substring(0, 30) + "..." :
            data.getValue().getContent()
        ));
        
       TableColumn<Clarification, String> fromQuestionCol = new TableColumn<>("Question Title");
        fromQuestionCol.setCellValueFactory(data -> new SimpleStringProperty(
        		data.getValue().getQuestionTitle().length() > 30 ?
        		data.getValue().getQuestionTitle().substring(0, 30) + "...":
        		data.getValue().getQuestionTitle()
        ));
        fromQuestionCol.setPrefWidth(200);
        
        TableColumn<Messages, String> dateCol = new TableColumn<>("Time");
        dateCol.setCellValueFactory(data -> new SimpleStringProperty(
        		data.getValue().getTimestamp().format(DateTimeFormatter.ofPattern("yyy-MM-dd HH:mm"))
        ));
        dateCol.setPrefWidth(150);
        
        TableColumn<Messages, String> sentDateCol = new TableColumn<>("Time");
        sentDateCol.setCellValueFactory(data -> new SimpleStringProperty(
            data.getValue().getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        ));
        sentDateCol.setPrefWidth(150);
        
        TableColumn<Clarification, String> sugDateCol = new TableColumn<>("Date");
        sugDateCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        ));
        sugDateCol.setPrefWidth(150);
        
        messageTable.getColumns().addAll(statusCol, fromCol, previewCol, dateCol);
        
        messageTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
        	if (newVal != null) displayMessageContent(newVal);
        });
        
        messageContentArea = new TextArea();
        messageContentArea.setEditable(false);
        messageContentArea.setWrapText(true);
        messageContentArea.setPrefRowCount(6);
        
        Button writeBtn = new Button("Write New Message");
        writeBtn.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white; -fx-font-weight: bold;");
        writeBtn.setOnAction(e -> writePopup(stage));
        
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Label fullMessageLabel = new Label("Full Message:");
        messageContentArea = new TextArea();
        messageContentArea.setEditable(false);
        messageContentArea.setWrapText(true);
        messageContentArea.setPrefRowCount(10);
        
        //Reply button
        Button replyBtn = new Button("Reply");
        replyBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        replyBtn.setOnAction(e -> {
        	Messages selectedMsg = messageTable.getSelectionModel().getSelectedItem();
        	if (selectedMsg != null) {
        		showReplyDialog(selectedMsg);
        	}
        });
        
        VBox receivedBox = new VBox(10, messageTable, fullMessageLabel, messageContentArea, replyBtn);
        Tab receivedTab = new Tab("📩 Received", receivedBox);
        
        //Sent Messages Tab
        sentMessageTable = new TableView<>();
        TextArea sentContentArea = new TextArea();
        sentContentArea.setEditable(false);
        sentContentArea.setWrapText(true);
        
        sentMessageTable.setPrefHeight(250);
        sentMessageTable.getColumns().addAll(sentToCol, sentPreviewCol, sentDateCol);
        sentMessageTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
        	if (newVal != null) sentContentArea.setText(newVal.getMessage());
        });
        
        VBox sentBox = new VBox(10, sentMessageTable, new Label("Sent Message:"), sentContentArea);
        Tab sentTab = new Tab("📤 Sent", sentBox);
        
        //Suggestions tab
        suggestionTable = new TableView<>();
        suggestionContentArea = new TextArea();
        suggestionContentArea.setEditable(false);
        suggestionContentArea.setWrapText(true);
        suggestionContentArea.setPrefRowCount(10);
        
        suggestionTable.setPrefHeight(250);
        suggestionTable.getColumns().addAll(sugFromCol, fromQuestionCol, sugPreviewCol, sugDateCol);
        suggestionTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
        	if (newVal != null) displaySuggestionContent(newVal);
        });
        
        Button makePublicBtn = new Button("Make Public");
        makePublicBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;");
        makePublicBtn.setOnAction(e -> {
            Clarification selected = suggestionTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                try {
                    databaseHelper.makeClarificationPublic(selected.getId());
                    new Alert(Alert.AlertType.INFORMATION, "Suggestion is now public.").show();
                    loadMessages();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    new Alert(Alert.AlertType.ERROR, "Failed to make suggestion public.").show();
                }
            }
        });
        
        Button editQuestionBtn = new Button("Edit Question");
        editQuestionBtn.setOnAction(e -> {
            Clarification selected = suggestionTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
            	try {
                    Question q = StatusData.databaseHelper.getQuestionById(selected.getQuestionId());
                    if (q != null) {
                        showEditQuestionPopup(q);  // Use popup instead of page
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });
        
        Button messageSuggestorBtn = new Button("Message Suggestor");
        messageSuggestorBtn.setOnAction(e -> {
            Clarification selected = suggestionTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                int suggestorId = selected.getAuthorId();
                showReplyToSuggDialog(suggestorId);
            }
        });
        HBox buttonBox = new HBox(10, makePublicBtn, editQuestionBtn, messageSuggestorBtn);
        VBox suggestionBox = new VBox(10, suggestionTable, new Label("Full Suggestion:"), suggestionContentArea, buttonBox);
        Tab suggestionTab = new Tab("💡 Suggestions", suggestionBox);
        //suggestionTab.setContent(suggestionBox);
        
        tabPane.getTabs().addAll(receivedTab, sentTab, suggestionTab);
        VBox messageBox = new VBox(10, headerBox, tabPane, writeBtn);
        messageBox.setPadding(new Insets(15));
        
        VBox contentBox = new VBox(15);
        contentBox.setPadding(new Insets(15));
    
        contentBox.getChildren().add(messageBox);
        mainPane.setCenter(contentBox);
        
//        Scene scene = new Scene(mainPane,  StatusData.WINDOW_WIDTH, StatusData.WINDOW_HEIGHT);
//        stage.setScene(scene);
//        stage.show();

        StatusData.setScene(stage, mainPane);
        
        loadMessages();  
	}
	
	private void displayMessageContent(Messages msg) {
		messageContentArea.setText(msg.getMessage());
		
		try {
			databaseHelper.markMessagesAsRead(msg.getId());
			loadMessages();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}
	
	private void displaySuggestionContent(Clarification c) {
		suggestionContentArea.setText(c.getContent());
		StringBuilder details = new StringBuilder();
		
		try {
			Question question = StatusData.databaseHelper.getQuestionById(c.getQuestionId());
			
			if (question != null) {
	            details.append("🔹 Question Title: ").append(question.getTitle()).append("\n");
	            details.append("🔹 Question Body: ").append(question.getDescription()).append("\n");
	            details.append("🔹 Question Timestamp: ").append(question.getTimestamp()).append("\n\n");
	        }
			
			details.append("🔸 From: ").append(c.getAuthor()).append("\n");
	        details.append("🔸 Suggestion Timestamp: ").append(c.getTimestamp()).append("\n");
	        details.append("🔸 Suggestion:\n").append(c.getContent()).append("\n\n");
	        
	        // Load other suggestions for the same question
	        List<Clarification> allSuggestions = StatusData.databaseHelper.loadClarificationsforQ(c.getQuestionId());
	        details.append("📌 Other Suggestions for This Question:\n");
	        for (Clarification other : allSuggestions) {
	            if (other.getId() != c.getId()) {
	                details.append("• ").append(other.getAuthor())
	                       .append(" (").append(other.getTimestamp()).append("): ")
	                       .append(other.getContent().length() > 50
	                               ? other.getContent().substring(0, 50) + "..."
	                               : other.getContent())
	                       .append("\n");
	            }
	        }
	        
	        suggestionContentArea.setText(details.toString());
	        
			databaseHelper.markClarificationAsRead(c.getId());
			loadMessages();
		} catch (SQLException ex) {
			ex.printStackTrace();
			suggestionContentArea.setText("⚠ Failed to load suggestion details.");
		}
	}
	
	private void showReplyDialog(Messages msg) {
		Dialog<String> replyDialog = new Dialog<>();
		replyDialog.setTitle("Reply");
		replyDialog.setHeaderText("Reply to: " + msg.getSenderName());
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
					msg.getSenderId(),
					replyText
			);
			reply.setSenderName(currentUser.getUserName());
			reply.setRecipientName(msg.getSenderName());
			
			databaseHelper.sendMessage(reply);
			new Alert(Alert.AlertType.INFORMATION, "Reply sent successfully!").show();
			loadMessages();
		});		
	}
	
	private void loadMessages() {
		try {
			List<Messages> inboxMessages = databaseHelper.getMessagesForUser(currentUser.getId());
			List<Messages> sentMessages = databaseHelper.getSentMessagesForUser(currentUser.getId());
			List<Clarification> suggestions = databaseHelper.loadAllSuggestionsForUser(currentUser.getId());
			
			//*******debugging
			/*System.out.println("Current user ID: " + currentUser.getId());
			System.out.println("Clarifications count: " + suggestions.size());
			for (Clarification c : suggestions) {
			    System.out.println(c.getRecipientId() + " from: " + c.getAuthorId() + " to: " + c.getRecipientId() + " content: " + c.getContent());
			}*/
			/*System.out.println("Current user ID: " + currentUser.getId());
			System.out.println("Messages fetched: " + messages.size());
			for (Messages msg : messages) {
			    System.out.println(msg.getId() + " from: " + msg.getSenderId() + " to: " + msg.getRecipientId() + " content: " + msg.getMessage());
			}*/
			//***********
			//ObservableList<Messages> inbox = FXCollections.observableArrayList();
			//ObservableList<Messages> sent = FXCollections.observableArrayList();
			
			//count unread messages
			long unreadCount = inboxMessages.stream().filter(m -> !m.isRead()).count();
			long unreadSuggestions = suggestions.stream().filter(c -> !c.isRead()).count();
			unreadCountLabel.setText("Unread PMs: " + unreadCount);
			unreadSuggestionsCount.setText("Unread Suggestions: " + unreadSuggestions);

			messageTable.setItems(FXCollections.observableArrayList(inboxMessages));
			messageTable.setRowFactory(tv -> new TableRow<>() {
				@Override
				protected void updateItem(Messages item, boolean empty) {
					super.updateItem(item, empty);
					if (item == null || empty) {
						setStyle("");
					} else if (!item.isRead()) {
						setStyle("-fx-font-weight: bold;");
					} else {
						setStyle("");
					}
				}
			});
			
	        sentMessageTable.setItems(FXCollections.observableArrayList(sentMessages));
	        
	       /* System.out.println("Loaded clarifications: " + suggestions.size());
	        for (Clarification c : suggestions) {
	            System.out.println("From: " + c.getAuthor() + ", Content: " + c.getContent() + ", QuestionTitle: " + c.getQuestionTitle());
	        }*/
			suggestionTable.setItems(FXCollections.observableArrayList(suggestions));
			suggestionTable.setRowFactory(tv -> new TableRow<>() {
				@Override
				protected void updateItem(Clarification item, boolean empty) {
					super.updateItem(item, empty);
					if (item == null || empty) {
						setStyle("");
					} else if (!item.isRead()) {
						setStyle("-fx-font-weight: bold;");
					} else {
						setStyle("");
					}
				}
			});
			
		} catch (Exception e) {
			e.printStackTrace();
			messageTable.setPlaceholder(new Label("Failed to load messages."));
		}
	}
	
	private void showReplyToSuggDialog(int recipientId) {
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
	
	private void writePopup(Stage owner) {
		Stage popup = new Stage();
		popup.setTitle("Compose Message");
		
		VBox layout = new VBox(10);
		layout.setPadding(new Insets(20));
		
		ComboBox<User> recipientBox = new ComboBox<>();
		recipientBox.setPromptText("Select recipient");
		recipientBox.setItems(FXCollections.observableArrayList(databaseHelper.getAllUsersExcept(currentUser.getId())));
		
		TextArea messageArea = new TextArea();
		messageArea.setPromptText("Enter your message...");
		messageArea.setWrapText(true);
		messageArea.setPrefRowCount(6);
		
		Button sendBtn = new Button("Send Message");
		sendBtn.setOnAction(e -> {
			User recipient = recipientBox.getValue();
			String content = messageArea.getText().trim();
			if (recipient == null) {
				Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a recipient.");
				alert.show();
				return;
			}
			
			if (content.isEmpty()) {
				Alert alert = new Alert(Alert.AlertType.WARNING, "Message cannot be empty.");
				alert.show();
				return;
			}
			
			Messages msg = new Messages(currentUser.getId(), recipient.getId(), content);
			databaseHelper.sendMessage(msg);
			popup.close();
			loadMessages();
		});
		
		layout.getChildren().addAll(new Label("To:"), recipientBox, new Label("Message:"), messageArea, sendBtn);
		popup.setScene(new Scene(layout, 400, 300));
		popup.initOwner(owner);
		popup.show();
	}
	
	private void showEditQuestionPopup(Question question) {
	    Stage popup = new Stage();
	    popup.initOwner(StatusData.primaryStage);
	    popup.setTitle("Edit Question");

	    VBox layout = new VBox(15);
	    layout.setPadding(new Insets(20));

	    TextField titleField = new TextField(question.getTitle());
	    TextArea descriptionArea = new TextArea(question.getDescription());
	    descriptionArea.setWrapText(true);
	    descriptionArea.setPrefRowCount(8);

	    HBox buttonBox = new HBox(10);
	    buttonBox.setAlignment(Pos.CENTER_RIGHT);
	    
	    Button cancelButton = new Button("Cancel");
	    cancelButton.setOnAction(e -> popup.close());
	    
	    Button deleteButton = new Button("Delete");
	    deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
	    deleteButton.setOnAction(e -> {
	        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this question?", ButtonType.OK, ButtonType.CANCEL);
	        confirm.setHeaderText("Delete Confirmation");
	        confirm.showAndWait().ifPresent(response -> {
	            if (response == ButtonType.OK) {
	                Result result = StatusData.questions.delete(question.getQuestionId(), question.getUserId(), StatusData.currUser);
	                if (result.isSuccess()) {
	                    new Alert(Alert.AlertType.INFORMATION, "Question deleted.").show();
	                    popup.close();
	                } else {
	                    new Alert(Alert.AlertType.ERROR, result.getMessage()).show();
	                }
	            }
	        });
	    });

	    
	    Button saveButton = new Button("Save");
	    saveButton.setOnAction(e -> {
	        String newTitle = titleField.getText().trim();
	        String newDesc = descriptionArea.getText().trim();

	        if (newTitle.isEmpty() || newDesc.isEmpty()) {
	            new Alert(Alert.AlertType.WARNING, "Title and Description required.").show();
	            return;
	        }

	        Result result = StatusData.questions.update(
	            question.getQuestionId(),
	            question.getUserId(),
	            newTitle,
	            newDesc,
	            question.isResolved(),
	            question.getTags(),
	            StatusData.currUser
	        );

	        if (result.isSuccess()) {
	            new Alert(Alert.AlertType.INFORMATION, "Updated!").show();
	            popup.close();
	        } else {
	            new Alert(Alert.AlertType.ERROR, result.getMessage()).show();
	        }
	    });

	    buttonBox.getChildren().addAll(cancelButton, deleteButton, saveButton);
	    
	    layout.getChildren().addAll(
	        new Label("Title:"), titleField,
	        new Label("Description:"), descriptionArea,
	        buttonBox
	    );

	    Scene scene = new Scene(layout, 500, 420);
	    popup.setScene(scene);
	    popup.showAndWait();
	}
	
}
