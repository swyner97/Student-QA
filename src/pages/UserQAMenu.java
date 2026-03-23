package pages;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import logic.StatusData;
import model.NavigationBar;

public class UserQAMenu extends Application {
	
	@Override
	public void start(Stage primaryStage) {
		primaryStage.setTitle("Question & Answer Menu");
		
		Button searchQsButton = new Button("Search Questions");
		Button searchAsButton = new Button("Search Answers");
		Button searchMineButton = new Button("Search My Posts");
		
		searchQsButton.setOnAction(e -> {
			SearchPage searchQsPage = new SearchPage();
			searchQsPage.show(new Stage(), StatusData.currUser);
		});
		
		searchAsButton.setOnAction(e -> {
			SearchAsPage searchAsPage = new SearchAsPage();
			searchAsPage.show(StatusData.primaryStage, StatusData.currUser);
		});
		
		searchMineButton.setOnAction(e -> {
			MyPostsPage searchMinePage = new MyPostsPage();
			Stage searchMineStage = new Stage();
			searchMinePage.show(searchMineStage);
		});
		
		
		VBox layout = new VBox(20, searchQsButton, searchAsButton, searchMineButton);
	    layout.setStyle("-fx-alignment: center; -fx-padding: 30;");
	    
	    BorderPane borderPane = new BorderPane();
	    borderPane.setTop(new NavigationBar());
	    borderPane.setCenter(layout);
	    
	   Scene scene = new Scene(borderPane, StatusData.WINDOW_WIDTH, StatusData.WINDOW_HEIGHT);
	   primaryStage.setScene(scene);
	   primaryStage.show();
	   
	}

}