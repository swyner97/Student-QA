package pages;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
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
import java.util.stream.Collectors;

import databasePart1.DatabaseHelper;

public class SearchAsPage {
	private Questions questions;
	private Answers answers;

	private final DatabaseHelper databaseHelper;
	private final User currentUser;
	private Stage stage;
	
	public SearchAsPage() {
		this(StatusData.databaseHelper, StatusData.currUser);
	}
	public SearchAsPage(DatabaseHelper databaseHelper, User currentUser) {
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
        stage.setTitle("Search Answers");
        BorderPane mainPane = new BorderPane();
        mainPane.setTop(new NavigationBar());
        
      //Top Section: filter options
        VBox filterOptions = new VBox(10);
        HBox labelBoxes = new HBox(10);
        
		Label questionIdLabel = new Label("Search by Question ID:");
		TextField questionIdField = new TextField();
		questionIdField.setPromptText("Enter question ID...");
		
		Label keywordLabel = new Label("Search by Keyword:");
		TextField keywordField = new TextField();
		keywordField.setPromptText("Enter keyword...");
		
		Label authorSearchLabel = new Label("Search by Author:");
		TextField authorField = new TextField();
		authorField.setPromptText("Enter author...");
		
		CheckBox resolveOnlyBox = new CheckBox("Only show solutions");
		
		Button searchButton = new Button("Search");
		VBox labelVBox1 = new VBox(5, questionIdLabel, questionIdField);
		VBox labelVBox2 = new VBox(5, keywordLabel, keywordField);
		VBox labelVBox3 = new VBox(5, authorSearchLabel, authorField);
		labelBoxes.getChildren().addAll(labelVBox1, labelVBox2, labelVBox3);
		filterOptions.getChildren().addAll(
				labelBoxes,
				resolveOnlyBox,
				searchButton);
		
		//Search results table
		VBox tableBox = new VBox(10);
        tableBox.setPadding(new Insets(10, 0, 0, 0));

        Label tableTitle = new Label("Search Results");
        tableTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        //Table header bar
        HBox tableHeader = new HBox(10, tableTitle);
        tableHeader.setAlignment(Pos.CENTER_LEFT);
        
        TableView<Answer> resultsTable = new TableView<>();
        
        TableColumn<Answer, Integer> idCol = new TableColumn<>("Answer ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("answerId"));
        idCol.setPrefWidth(80);
        
        TableColumn<Answer, Boolean> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cellData ->
        	new SimpleBooleanProperty(cellData.getValue().isSolution())
        );
        statusCol.setPrefWidth(60);

        TableColumn<Answer, String> authorCol = new TableColumn<>("Author");
        authorCol.setCellValueFactory(new PropertyValueFactory<>("author"));
        authorCol.setPrefWidth(120);

        TableColumn<Answer, String> subjCol = new TableColumn<>("Question Title");
		
		subjCol.setCellValueFactory(cellData -> {
			try {
	        	int qId = cellData.getValue().getQuestionId();
	        	Question q;
	        	q = databaseHelper.getQuestionById(qId);
	        	String qTitle = q.getTitle();
	        	return new javafx.beans.property.SimpleStringProperty(qTitle);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;	
        });
		subjCol.setPrefWidth(100);
        
        TableColumn<Answer, String> contentCol = new TableColumn<>("Answer");
        contentCol.setCellValueFactory(cellData -> {
            String answerContent = cellData.getValue().getContent();
            String preview = answerContent.length() > 50 ? answerContent.substring(0, 50) + "..." : answerContent;
            return new javafx.beans.property.SimpleStringProperty(preview);
        });
        contentCol.setPrefWidth(200);
        
        TableColumn<Answer, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cellData -> {
            String timestamp = cellData.getValue().getTimestamp();
            return new javafx.beans.property.SimpleStringProperty(timestamp != null ? timestamp : "N/A");
        });
        dateCol.setPrefWidth(150);
        
        TableColumn<Answer, Void> viewQCol = new TableColumn<>("View Question");
        viewQCol.setPrefWidth(100);
        viewQCol.setCellFactory(_ -> new TableCell<>() {
        	private final Button viewQBtn = new Button("View Question");
        	private final HBox answersBtnBox = new HBox(5, viewQBtn);
        	{
        		viewQBtn.setOnAction(e -> {
        			Answer a = getTableView().getItems().get(getIndex());
        			List<Question> question;
					try {
						int questionId = a.getQuestionId();
			        	Question q = databaseHelper.getQuestionById(questionId);
			        	Stage dialog = new Stage();
		    			dialog.setTitle("Question #" + q.getQuestionId());
		    			
		    			TextArea qDetailArea = new TextArea();
		    			qDetailArea.setEditable(false);
		    			qDetailArea.setWrapText(true);
		    			qDetailArea.setText("Title: " + q.getTitle() + "\n\nDescription:\n" + q.getDescription());
		    	        qDetailArea.setPrefRowCount(8);
		    	        
		    	        ScrollPane detailsScroll = new ScrollPane(qDetailArea);
		    	        detailsScroll.setFitToWidth(true);
		    	        detailsScroll.setStyle("-fx-background-color: transparent;");
		    	        Tab detailsTab = new Tab("Question Details", detailsScroll);
		    	        
		    	        VBox contentBox = new VBox(new Label("Question Details:"), detailsScroll);
		    	        contentBox.setPadding(new Insets(10));
		    	        
		    	        Scene scene = new Scene(contentBox, 600, 400);
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
        	
        resultsTable.getColumns().addAll(idCol, statusCol, contentCol, subjCol, authorCol, viewQCol, dateCol);
        // Add header and table to layout (ONLY ONCE)
        tableBox.getChildren().addAll(tableHeader, resultsTable);

		//SearchFunction searchFunction = new SearchFunction(questions, answers);
		searchButton.setOnAction(e -> {
			resultsTable.getItems().clear();
			String keyword = keywordField.getText().toLowerCase();
			String author = authorField.getText();
			boolean solutionOnly = resolveOnlyBox.isSelected();

			List<Answer> filtered = databaseHelper.searchAnswers(keyword, author, solutionOnly);
			resultsTable.getItems().setAll(filtered);
		});
		
		HBox topSection = new HBox(20, filterOptions);
		VBox layout = new VBox(15, mainPane, topSection, tableBox);
		layout.setPadding(new javafx.geometry.Insets(20));

		StatusData.setScene(stage, layout);
	}
}

