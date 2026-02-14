package pages;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import logic.SearchFunction;
import logic.StatusData;
import model.Answer;
import model.Answers;
import model.NavigationBar;
import model.Question;
import model.Questions;
import model.User;

import java.sql.SQLException;
import java.util.List;

import databasePart1.DatabaseHelper;


public class SearchPage {
	private Questions questions;
	private Answers answers;

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
		answers = new Answers(databaseHelper);
        questions = new Questions(databaseHelper);
        
        this.stage = primaryStage;
        stage.setTitle("Search Questions");
        BorderPane mainPane = new BorderPane();
        mainPane.setTop(new NavigationBar());
        
        //Top Section: filter options
        VBox filterOptions = new VBox(10);

		Label keywordSearchLabel = new Label("Search by Keyword:");
		TextField keywordField = new TextField();
		keywordField.setPromptText("Enter keyword...");
		
		Label authorSearchLabel = new Label("Search by Author:");
		TextField authorField = new TextField();
		authorField.setPromptText("Enter author...");
		
		CheckBox solvedOnly = new CheckBox("Only show questions marked as solved:");
		CheckBox unsolvedOnly = new CheckBox("Only show questions with no solutions:");
		Button searchButton = new Button("Search");
		
		filterOptions.getChildren().addAll(
				keywordSearchLabel, keywordField,
				authorSearchLabel, authorField,
				solvedOnly,
				unsolvedOnly,
				searchButton);

		//Search results table
		VBox tableBox = new VBox(10);
        tableBox.setPadding(new Insets(10, 0, 0, 0));

        Label tableTitle = new Label("Search Results");
        tableTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
       
        //Table header bar
        HBox tableHeader = new HBox(10, tableTitle);
        tableHeader.setAlignment(Pos.CENTER_LEFT);

        TableView<Question> resultsTable = new TableView<>();
        
        TableColumn<Question, Integer> idCol = new TableColumn<>("Question ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("questionId"));
        idCol.setPrefWidth(80);
        
        TableColumn<Question, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cellData ->
        	new ReadOnlyObjectWrapper<>(cellData.getValue().getStatusText())
        );
        statusCol.setPrefWidth(80);

        TableColumn<Question, String> authorCol = new TableColumn<>("Author");
        authorCol.setCellValueFactory(new PropertyValueFactory<>("author"));
        authorCol.setPrefWidth(120);

        TableColumn<Question, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(cellData -> {
        	String questionTitle = cellData.getValue().getTitle();
        	String titlePrev = questionTitle.length() > 30 ? questionTitle.substring(0, 30) + "..." : questionTitle;
        	return new javafx.beans.property.SimpleStringProperty(titlePrev);
        });
        TableColumn<Question, String> contentCol = new TableColumn<>("Question");
        contentCol.setCellValueFactory(cellData -> {
            String questionContent = cellData.getValue().getDescription();
            String preview = questionContent.length() > 50 ? questionContent.substring(0, 50) + "..." : questionContent;
            return new javafx.beans.property.SimpleStringProperty(preview);
        });
        contentCol.setPrefWidth(350);
        
        TableColumn<Question, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cellData -> {
            String timestamp = cellData.getValue().getTimestamp();
            return new javafx.beans.property.SimpleStringProperty(timestamp != null ? timestamp : "N/A");
        });
        dateCol.setPrefWidth(150);
        
        TableColumn<Question, Integer> answerCountCol = new TableColumn<>("Answer Count");
        answerCountCol.setCellValueFactory(cellData -> {
        	Question q = cellData.getValue();
        	int answerCount = 0;
			try {
				answerCount = databaseHelper.getAnswersByQuestionId(q.getQuestionId()).size();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	return new ReadOnlyObjectWrapper<>(answerCount);
        });
        answerCountCol.setPrefWidth(80);
        
        TableColumn<Question, Void> viewAnswersCol = new TableColumn<>("View Answers");
        viewAnswersCol.setPrefWidth(100);
        viewAnswersCol.setCellFactory(_ -> new TableCell<>() {
        	private final Button viewAnswersBtn = new Button("View Answers");
        	private final HBox answersBtnBox = new HBox(5, viewAnswersBtn);
        	{
        		viewAnswersBtn.setOnAction(e -> {
        			Question q = getTableView().getItems().get(getIndex());
        			List<Answer> answers;
					try {
						answers = databaseHelper.getAnswersByQuestionId(q.getQuestionId());
					
        			
		    			Stage dialog = new Stage();
		    			dialog.setTitle("Answers for Question #" + q.getQuestionId() + " " + q.getTitle() + "\n" + q.getDescription());
		    			
		    			ListView<Answer> listView = new ListView<>();
		    			listView.getItems().addAll(answers);
		    			listView.setCellFactory(lview -> new ListCell<>() {
		    				@Override
		    				protected void updateItem(Answer a, boolean empty) {
		    					super.updateItem(a, empty);
		    					if (empty || a == null) {
		    						setText(null);
		    					} else {
		    						String previewAns = a.getContent().length() > 50
		    								? a.getContent().substring(0, 50) + "..."
		    								: a.getContent();
		    						setText(previewAns);
		    					}
		    				}
		    			});
		    			
		    			TextArea fullAnswerArea = new TextArea();
		    			fullAnswerArea.setEditable(false);
		    			
		    			listView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, sel) -> {
		    				if (sel != null) {
		    					fullAnswerArea.setText(sel.getContent());
		    				}
		    			});
					
        			
	        			SplitPane split = new SplitPane();
	        			split.getItems().addAll(new VBox(new Label("Select an answer:"), listView),
	        											new VBox(new Label("Full answer:"), fullAnswerArea));
	        			split.setDividerPositions(0.3);
	        			
	        			Scene scene = new Scene(split, 600, 400);
	        			dialog.setScene(scene);
	        			dialog.showAndWait();
					} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
        		});
	
        	}
        	
        	@Override
        	protected void updateItem(Void item, boolean empty) {
	        	super.updateItem(item, empty);
	        	if (empty) setGraphic(null);
	        	else setGraphic(answersBtnBox);
        	}
        });
        
        resultsTable.getColumns().addAll(idCol, statusCol, titleCol, contentCol, answerCountCol, authorCol, dateCol);
//		resultsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
//			if (newVal != null) displayQuestionDetails(newVal);
//		});
		 // Add header and table to layout (ONLY ONCE)
        tableBox.getChildren().addAll(tableHeader, resultsTable);
		
        //SearchFunction searchFunction = new SearchFunction(questions, answers);
		
		searchButton.setOnAction(e -> {
			resultsTable.getItems().clear();
			String keyword = keywordField.getText().toLowerCase();
			String author = authorField.getText().toLowerCase();
			Boolean solvedQs = null;
			if (solvedOnly.isSelected() && !unsolvedOnly.isSelected()) {
				solvedQs = true;
			} else if (!solvedOnly.isSelected() && unsolvedOnly.isSelected()) {
				solvedQs = false;
			}
			
			//List<Question> filtered = questions.search(keyword, author, solvedQs);
			List<Question> filtered = databaseHelper.searchQuestions(keyword, author, solvedQs);
			resultsTable.getItems().setAll(filtered);	
		});
		
		HBox topSection = new HBox(20, filterOptions);
		VBox layout = new VBox(15, mainPane, topSection, tableBox);
		layout.setPadding(new javafx.geometry.Insets(20));
		StatusData.setScene(stage, layout);
	}

}

