package assignment3;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;


public class DownloadBar extends HBox {

	private static Stage downloadWindow = null;
	private static VBox downloadTasks;
	private static TextArea messageArea;

	private Text filenameLabel;
	private ProgressBar progressBar;
	private Button cancelButton;
	private String fileUrl = "";
	private File file;
	private String downloadDirectory = "";
	private Boolean cancelled = false;


	/** Calling this function will guarantee that the downloadTasks VBox is created and visible.
	 * @return A Stage that will show each downloadTask's progress
	 */
	public Stage getDownloadWindow()
	{
		if(downloadWindow == null)
		{
			//Create a new borderPane for the download window
			BorderPane downloadRoot = new BorderPane();
			downloadTasks = new VBox();
			//downloadTasks will contain rows of DownloadTask objects, which are HBoxes
			downloadRoot.setCenter(downloadTasks);

			//The bottom of the window will be the message box for download tasks
			downloadRoot.setBottom(messageArea = new TextArea());
			downloadWindow = new Stage();
			downloadWindow.setScene(new Scene(downloadRoot, 400, 600));

			//When closing the window, set the variable downloadWindow to null
			downloadWindow.setOnCloseRequest(event -> downloadWindow = null);
		}
		return downloadWindow;
	}

	/**The constructor for a DownloadTask
	 *
	 * @param newLocation  The String URL of a file to download
	 */
	public DownloadBar(String newLocation)
	{
		// get the value of the download directory variable that was set in the main GUI class
		String directory = MyGUI.downloadDirectory;
		// store the download url in a private variable
		this.fileUrl = newLocation;

		// get the filename at the end of new location
		String fileName = "";
		int u = newLocation.lastIndexOf('/');
		if (u > 0) {
			fileName = newLocation.substring(u+1);
		}
		//See if the filename at the end of newLocation exists on your hard drive.
		file = new File(directory + fileName);
		String extension = "";
		int counter = 1;
		// If the file already exists, then add (1), (2), ... (n) until you find a new filename that doesn't exist.
		while(file.exists())
		{
			int i = fileName.lastIndexOf('.');
			if (i > 0) {
				extension = fileName.substring(i+1);
			}
			file = new File( file.getPath() + "\\" + (file.getName() + counter++) + "." + extension);
		}

		//Create the window if it doesn't exist. After this call, the VBox and TextArea should exist.
		getDownloadWindow();

		//Add a Text label for the filename
		filenameLabel = new Text(file.getName());
		this.getChildren().add(filenameLabel);
		//Add a ProgressBar to show the progress of the task
		progressBar = new ProgressBar();
		progressBar.setProgress(0.0);
		this.getChildren().add(progressBar);
		//Add a cancel button that asks the user for confirmation, and cancel the task if the user agrees
		cancelButton = new Button("Cancel");
		cancelButton.setOnMouseClicked(new EventHandler<MouseEvent>(){
			@Override
			public void handle(MouseEvent event) {
				// if the click count > 0, we know the mouse was clicked in this event
				if (event.getClickCount() > 0){
					cancelled = true;
				}
			}
		});
		this.getChildren().add(cancelButton);

		// adds the download task to the download window
		downloadTasks.getChildren().add(this);

		//Start the download
		DownloadTask aFileDownload = new DownloadTask();
		new Thread(aFileDownload).start();
	}



	/**This class represents a task that will be run in a separate thread. It will run call(),
	 *  and then call cancelled, or failed depending on whether the task was cancelled
	 *  or failed. If it was not, then it will call succeeded() after call() finishes.
	 */
	private class DownloadTask extends Task<String>
	{
		private static final int BUFFER_SIZE = 4096;
		// This should start the download. Look at the downloadFile() function at:
		//  http://www.codejava.net/java-se/networking/use-httpurlconnection-to-download-file-from-an-http-url
		//Take that function but change it so that it updates the progress bar as it iterates through the while loop.
		//Here is a tutorial on how to upgrade a progress bar:
		//	https://docs.oracle.com/javase/8/javafx/user-interface-tutorial/progress.htm
		@Override
		protected String call() throws Exception {
			// TODO: fix up this code to work with the progress bar, etc...
			URL url = new URL(fileUrl);
			HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
			int responseCode = httpConn.getResponseCode();

			// check the HTTP response code
			if (responseCode == HttpURLConnection.HTTP_OK) {
				String fileName = "";
				String disposition = httpConn.getHeaderField("Content-Disposition");
				String contentType = httpConn.getContentType();
				int contentLength = httpConn.getContentLength();

				if (disposition != null) {
					// extract file name from the header field
					int index = disposition.indexOf("filename=");
					if (index > 0) {
						fileName = disposition.substring(index + 10,
								disposition.length() - 1);
					}
				} else {
					// extracts file name from URL
					fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1,
							fileUrl.length());
				}

				/*
	            System.out.println("Content-Type = " + contentType);
	            System.out.println("Content-Disposition = " + disposition);
	            System.out.println("Content-Length = " + contentLength);
	            System.out.println("fileName = " + fileName);
				 */

				// open input and output streams
				InputStream inputStream = httpConn.getInputStream();
				String saveFilePath = downloadDirectory + File.separator + fileName;

				FileOutputStream outputStream = new FileOutputStream(saveFilePath);

				// read the incoming bytes into a buffer and progressively write them to disc
				int bytesRead = -1;
				int totalBytesRead = 0;
				int totalFileSize = httpConn.getContentLength();
				byte[] buffer = new byte[BUFFER_SIZE];
				while ((bytesRead = inputStream.read(buffer)) != -1) {
					// calculate the amount of the file downloaded versus the amount remaining
					totalBytesRead += bytesRead;
					double downloadPercent = 100.0 * (float) totalBytesRead / totalFileSize;
					// update the progress bar with the percentage completion
					progressBar.setProgress(downloadPercent);
					// write the new bytes to the outputStream
					outputStream.write(buffer, 0, bytesRead);
				}

				// close the input and output streams
				outputStream.close();
				inputStream.close();

				// System.out.println("File downloaded");
			} else {
				// if we reach here, there is no file to download or something else has gone wrong
				// TODO: throw an error here of some kind
				// System.out.println("No file to download. Server replied HTTP code: " + responseCode);
			}
			// close the web connection
			httpConn.disconnect();

			return "Finished";
		}

		//Write the code here to handle a successful completion of the call() function.
		@Override
		protected void succeeded() {
			// TODO: An animation and audio queue here, as the download bar "falls off" the window
			// TODO: A successfully downloaded .exe file should prompt the user to run the file
			// BONUS MARKS part B and D
			super.succeeded();
			// the download succeeded so remove the download bar from the task list
			downloadTasks.getChildren().remove(this);
			// print a success message to the text area featuring the filename
			messageArea.appendText(file.getName() + " was successfully downloaded.");
		}

		//Write the code here to handle the task being cancelled before call() finishes.
		@Override
		protected void cancelled() {
			super.cancelled();
			// the download failed so remove the download bar from the task list
			downloadTasks.getChildren().remove(this);
			// print a failure message to the text area featuring the filename
			messageArea.appendText(file.getName() + " failed to download.");
			// ensure the file is removed from the file system
			file.delete();
		}

		@Override
		protected void failed() {
			super.failed();
			// the download failed so remove the download bar from the task list
			downloadTasks.getChildren().remove(this);
			// print a failure message to the text area featuring the filename
			messageArea.appendText(file.getName() + " failed to download.");
			// ensure the file is removed from the file system
			file.delete();
		}
	}
}
