package cz.zcu.kiv.ir.silhavyj.searchengine;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/***
 * @author Jakub Silhavy
 *
 * This class represents the main class of the application.
 * It inherets from Application which is a class that comes
 * with JavaFX (GUI window).
 */
public class Main extends Application {

    /*** Title of the main window  */
    private static final String TITLE = "KIV/IR - Search Engine - silhavyj";

    /*** Name of the file that defines the view (components, etc.) */
    private static final String VIEW_FXML_FILE = "view.fxml";

    /*** Default width of the main window */
    private static final int WINDOW_MIN_WIDTH = 1000;

    /*** Default height of the main window */
    private static final int WINDOW_MIN_HEIGHT = 640;

    /***
     * Entry point of the application
     * @param args arguments passed in from the command line (unused)
     */
    public static void main(String[] args) {
        launch();
    }

    /***
     * Start method that loads up the main window.
     * @param stage the main stage of the application
     * @throws IOException if something goes wrong
     */
    @Override
    public void start(Stage stage) throws IOException {
        // Load the view.
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource(VIEW_FXML_FILE));
        Scene scene = new Scene(fxmlLoader.load());

        // Set up the main stage.
        stage.setTitle(TITLE);
        stage.setScene(scene);
        stage.setMinHeight(WINDOW_MIN_HEIGHT);
        stage.setMinWidth(WINDOW_MIN_WIDTH);
        stage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });

        // Show the window.
        stage.show();
    }
}