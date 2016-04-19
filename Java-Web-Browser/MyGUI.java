package assignment3;


import java.io.FileOutputStream;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Optional;

import assignment2.appropriate;
import assignment2.before;
import assignment2.display;
import assignment2.download;
import assignment2.home;
import assignment2.it;
import assignment2.open;
import assignment2.topLeftY;
import assignment2.topleftX;
import assignment2.MyGUI.MouseHandlerClass;
import assignment2.MyGUI.NameValuePair;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker.State;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.web.WebView;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;


public class MyGUI extends Application {
	protected WebEngine engine;
	protected String currentURL = "http://www.google.ca/";
	protected WebView browserView;//this is so that browserView still exists outside of main
	private MouseHandlerClass mouseHandler;
	private Scene scene;
	private BorderPane root;
	private VBox topMenuLayout;
	private MenuBar menuBar;
	private Menu fileMenu;
	private MenuItem quitItem;
	private Menu bookMarksMenu;
	private ArrayList<String> bookmarkItems;
	private Menu helpMenu;
	private MenuItem getHelpItem;
	private CheckMenuItem showHistoryItem;
	private MenuItem showAboutItem;
	private HBox browserBar;
	private Button backButton;
	private Button forwardButton;
	private TextField URLField;
	private Button bookmarkButton;
	private ListView<String> historyView;
	private WebHistory webHistory;

	public static void main(String[] args) {		launch(args);	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		// instantiates the mouse handler class
		this.mouseHandler = new MouseHandlerClass();
		// create the bookmark button object
		this.bookmarkButton = new Button("Add Bookmark");

		/***********************************MENU BAR********************************************/
		// creates the menu bar object
		this.menuBar = new MenuBar();

		/*****************************FILE MENU************************************/
		// create the file menu object
		this.fileMenu = new Menu("File");
		// create a quit menu item for the file menu
		this.quitItem = new MenuItem("Quit");
		// set the quit menu item to execute Platform.exit() on clicked
		this.quitItem.setOnAction(event -> {	
			saveSettings();
			Platform.exit(); });
		// adds the quit menu item to the file menu
		this.fileMenu.getItems().add(quitItem);		

		/***********************************BOOKMARKS MENU********************************************/
		// create the bookmarks menu object
		this.bookMarksMenu = new Menu("Bookmarks");
		// function that adds a url to the bookmarks menu
		addBookmark(currentURL);		

		/***********************************SETTINGS MENU********************************************/


		/*******************************HELP MENU************************************/
		// create the help menu object
		this.helpMenu = new Menu("Help");
		// create the get help menu item
		this.getHelpItem = new MenuItem("Get Help for Java Class");
		// set the get help menu item's action
		this.getHelpItem.setOnAction((event) -> {
			// code from lab 4 gets formatted into here
			// then the string is passed to the WebView engine for processing
			// Should I make a new private inner class with all these methods in them, and this part of the code just calls those functions and constructors????
			// move this into a method

			TextInputDialog dialog = new TextInputDialog("Type here");
			dialog.setTitle("Get Help for a Java Class");
			dialog.setHeaderText("Use this wizard to research your java class");
			dialog.setContentText("Which class do you want to research?");

			// Traditional way to get the response value.
			Optional<String> result = dialog.showAndWait();
			if (result.isPresent()){
				this.javaSearch(result.get());
			}
		});

		// create the history check menu item
		this.showHistoryItem = new CheckMenuItem("Show History");
		//this.showHistoryItem.setSelected(true);
		this.showHistoryItem.setOnAction((event) -> {
			// history opening/closing will consist of two animations - a parallel and a sequential
			if (!this.showHistoryItem.isSelected()) {
				hideHistory();
			} else {
				showHistory();
			}
		});

		// creates the show about menu item
		this.showAboutItem = new MenuItem("About");
		// sets the about menu item action to show an alert displaying info about the program
		this.showAboutItem.setOnAction((event) -> {
			// move this into a method
			Alert aboutInfo = new Alert(  AlertType.INFORMATION  );
			aboutInfo.setTitle("About dis GUI");
			aboutInfo.setHeaderText("INFORMATION ");
			aboutInfo.setContentText("Basic GUI" + "\n" + "VERSION 1.0" + "\n" + "Designed by Carlos Smith Romero" + "\n" + "student number 040561285" + "\n" + "Admit it- this is the best basic GUI you ever saw!");
			aboutInfo.show();
		});
		// add the new items to the help menu
		this.helpMenu.getItems().addAll(this.getHelpItem, this.showHistoryItem, this.showAboutItem);	

		// add all of the menus and menu items to the menu bar
		this.menuBar.getMenus().addAll(fileMenu, bookMarksMenu, helpMenu);

		/*******************************BROWSER BAR*********************************************/
		// creates the browser bar flowpane object
		this.browserBar = new HBox();

		// creates the back button object
		this.backButton = new Button("<");
		// sets the back button object's action to the goBack method
		this.backButton.setOnMouseClicked(this.mouseHandler);

		// creates the address bar textfield object for inputting URLs
		this.URLField = new TextField("A Text Field");
		// sets the URL field to occupy all available space in the browser bar
		this.browserBar.setHgrow(URLField, Priority.ALWAYS);
		// sets the url field to accept click and double click events
		this.URLField.setOnMouseClicked(this.mouseHandler);
		// sets the current url variable to update when new text is inputted
		//this.URLField.setOnKeyTyped((event) -> {this.currentURL = this.URLField.getText();});
		// sets the web view to load the given url in the browser bar when enter is pressed
		this.URLField.setOnKeyPressed(new EventHandler<KeyEvent>(){
			@Override
			public void handle(KeyEvent event) {
				if (event.getCode().equals(KeyCode.ENTER)){
					currentURL = URLField.getText();
					engine.load(currentURL);
				}
			}
		});

		// create the bookmark button object
		this.bookmarkButton = new Button("Add Bookmark");
		// sets the bookmark button object's action to run the add bookmark method
		this.bookmarkButton.setOnMouseClicked(this.mouseHandler);

		// create the forward button object
		this.forwardButton = new Button(">");
		// sets the forward button object's action to the goForward method
		this.forwardButton.setOnMouseClicked(this.mouseHandler);

		// add all the browser bar menu objects to the browser bar flowpane
		this.browserBar.getChildren().addAll(this.backButton, this.URLField, this.bookmarkButton, this.forwardButton);

		/*******************************WEB VIEW*********************************************/
		// initialises the browser view object
		this.browserView = new WebView();
		// gets the web engine object from the browser view
		this.engine = this.browserView.getEngine();

		//This is a 3-parameter Lambda function for listening for changes
		// of state for the web page loader.				VVV  VVV         VVV
		engine.getLoadWorker().stateProperty().addListener(( ov, oldState,  newState)->
		{
			// This if statement gets run if the new page load succeeded.
			if (newState == State.SUCCEEDED) {
				// set the back and forward buttons to enabled/disabled, depending on our history index position
				this.backButton.setDisable(false);
				this.forwardButton.setDisable(false);
				this.bookmarkButton.setDisable(false);
				if (this.webHistory.getCurrentIndex() == 0){
					this.backButton.setDisable(true);
				} else if (this.webHistory.getCurrentIndex() == this.webHistory.getEntries().size()-1){
					this.forwardButton.setDisable(true);
				}
				// bookmarks button is disabled if that page already exists in the menu. 	
				for (int i = 0; i < this.bookMarksMenu.getItems().size(); i++){
					if (this.bookMarksMenu.getItems().get(i).getText().equals(this.currentURL)){
						this.bookmarkButton.setDisable(true);
					}
				}
				// sets the text field to the current url once the page is loaded
				this.URLField.setText(this.engine.getLocation());
			}
		});

		/*******************************WEB HISTORY*********************************************/
		// gets the web history object that manages the browser's history
		this.webHistory = engine.getHistory();
		// adds a listener to the web history to listen for new additions
		this.webHistory.getEntries().addListener(new ListChangeListener<WebHistory.Entry>() {
			@Override
			public void onChanged(ListChangeListener.Change <? extends WebHistory.Entry> change) {
				System.out.println("Detected a change!");
				change.next();
				// if an item was removed, remove it from the history view
				for (WebHistory.Entry e : change.getRemoved()) {
					historyView.getItems().remove(e.getUrl());
					historyView.refresh();
				}
				// if an item was added, add it to the history view
				for (WebHistory.Entry e : change.getAddedSubList()) {
					historyView.getItems().add(e.getUrl());
					historyView.refresh();
				}
			}
		});
		// creates the history listview object
		this.historyView = new ListView<String>();
		// sets the history view items to respond to on click events, moving to the correct part of the history timeline
		this.historyView.setOnMouseClicked(this.mouseHandler);


		/*****************************SET STAGE*******************************/
		// creates the root borderpane gui object
		this.root = new BorderPane();
		// creates a vertical layout for the menu and browser bars
		this.topMenuLayout = new VBox();
		this.topMenuLayout.getChildren().addAll(this.menuBar, this.browserBar);
		// set the ui elements to the correct places in the window
		this.root.setTop(topMenuLayout);
		this.root.setCenter(this.browserView);
		//this.root.setRight(this.historyView);
		this.hideHistory();
		// sets the scene for the primary stage, passing in the root gui
		primaryStage.setTitle("Assignment 2 GUI");
		this.scene = new Scene(root, 800, 800);
		primaryStage.setScene(scene);
		primaryStage.show();


		// monitor the location url, and if newLoc ends with one of the download file endings, create a new DownloadTask.
		engine.locationProperty().addListener(new ChangeListener<String>() {
			@Override public void changed(ObservableValue<? extends String> observableValue, String oldLoc, String newLocation) {
				if(newLocation.endsWith(/* one of the download endings*/  ) )
				{
					DownloadBar newDownload = new DownloadBar(newLocation);		  
				}
			}
		});	

	}



	// Tell the engine to go back 1 page in the history
	public void goBack()
	{    
		final WebHistory history = engine.getHistory();
		ObservableList<WebHistory.Entry> entryList = history.getEntries();
		int currentIndex = history.getCurrentIndex();

		if (currentIndex > 0)
		{			// 		  VVV  This is a no-parameter Lambda function run();
			Platform.runLater( () -> { 
				history.go(-1); 
				final String nextAddress = history.getEntries().get(currentIndex - 1).getUrl();
			} );
		}
	}

	//Tell the engine to go forward 1 page in the history
	public void goForward()
	{    
		final WebHistory history = engine.getHistory();
		ObservableList<WebHistory.Entry> entryList = history.getEntries();
		int currentIndex = history.getCurrentIndex();

		if(currentIndex + 1 < entryList.size())
		{	    					//This is a no-parameter Lambda function run();
			Platform.runLater( () -> { 
				history.go(1); 
				final String nextAddress = history.getEntries().get(currentIndex + 1).getUrl();
			});
		}    
	}

	public void addBookmark(String s){
		// create a new menu item, with the input string as the title
		MenuItem newBookMark = new MenuItem(s);
		// set the bookmark menu item's on click event to load the specified page
		newBookMark.setOnAction( (event) -> { this.engine.load(s); });
		this.bookMarksMenu.getItems().addAll(newBookMark);
		this.bookmarkButton.setDisable(true);
	}

	// called when a history view item is selected
	public void goToHistoryItem(int index){
		// calculates the difference between the current history index and the index of the selected item in the history view
		int diff = index - this.webHistory.getCurrentIndex();
		// tells the web history object to move the web view's history forward or backward
		this.webHistory.go(diff);
	}

	public void showHistory(){
		root.setRight(historyView);

		TranslateTransition tt = new TranslateTransition(Duration.millis(500), this.historyView);
		tt.setByX(-300);			// How much to move X
		//tt.setByY(0); 			// How much to move Y
		tt.setAutoReverse(true);	// Should it go back to the start values?

		ScaleTransition st = new ScaleTransition(Duration.millis(1000), this.historyView);
		st.setFromX(1f);
		st.setFromY(0.1f);
		st.setToX(1f); 		
		st.setToY(1f);		
		//st.setCycleCount(2);
		st.setAutoReverse(true);

		SequentialTransition sqt = new SequentialTransition(tt, st);
		sqt.play(); //Play ft first, then tt once st finishes

	}

	public void hideHistory(){
		// a simultaneous translation, as the history pane slides to the right while shrinking
		TranslateTransition tt = new TranslateTransition(Duration.millis(1000), this.historyView);
		tt.setByX(300);			// How much to move X
		//tt.setByY(0); 			// How much to move Y
		tt.setAutoReverse(true);	// Should it go back to the start values?
		tt.setCycleCount(1);		// How many times to loop? Animation.INDEFINITE means forever

		ScaleTransition st = new ScaleTransition(Duration.millis(1000), this.historyView);
		st.setFromX(1f);
		st.setFromY(1f);
		st.setToX(0.01f); 		
		st.setToY(0.01f);		
		//st.setCycleCount(2);
		st.setAutoReverse(true);

		ParallelTransition pt = new ParallelTransition(st, tt);  // Run both st and tt at the same time
		pt.setOnFinished(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				root.getChildren().remove(historyView);
			}
		});
		pt.play();  // Start the animation
	}

	public void javaSearch (String s){
		// adds the class String to the end of a standard google search URL
		String googleSearchURL = "http://www.google.com/search?" + "q=java+" + s;
		// load the searchURL into the WebEngine
		this.engine.load(googleSearchURL);
	}

	private class MouseHandlerClass implements EventHandler<MouseEvent>
	{
		// this class will handle various mouse events
		@Override
		public void handle(MouseEvent event) {
			// checks the source of the event, and handles accordingly
			if (event.getSource().equals(URLField)){
				if(event.getButton().equals(MouseButton.PRIMARY)){
					// on single click, selects all text
					URLField.selectAll();
					if(event.getClickCount() == 2){
						// on double click, removes all text
						System.out.println("Double clicked");
						URLField.clear();
					}
				}
			}

			if (event.getSource().equals(historyView)){
				if (historyView.getItems().isEmpty()){
					return;
				}
				// calls the appropriate history method to go to that page
				goToHistoryItem(historyView.getSelectionModel().getSelectedIndex());
			}

			if (event.getSource().equals(bookmarkButton)){
				addBookmark(currentURL);
			}

			if (event.getSource().equals(backButton)){
				goBack();
			}

			if (event.getSource().equals(forwardButton)){
				goForward();
			}

		}

	}

}
