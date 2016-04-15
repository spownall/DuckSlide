package assignment3;


import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker.State;
import javafx.stage.Stage;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;


public class MyGUI extends Application {
	protected WebEngine engine;
	
	public static void main(String[] args) {		launch(args);	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		WebView browserView = new WebView();
		engine = browserView.getEngine();
		

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

}
