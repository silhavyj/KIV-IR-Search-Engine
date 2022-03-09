package cz.zcu.kiv.ir.silhavyj.searchengine;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    private static final String TITLE = "KIV/IR - Search Engine - silhavyj";
    private static final String VIEW_FXML_FILE = "view.fxml";

    private static final int WINDOW_MIN_WIDTH = 720;
    private static final int WINDOW_MIN_HEIGHT = 540;

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource(VIEW_FXML_FILE));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle(TITLE);
        stage.setScene(scene);
        stage.setMinHeight(WINDOW_MIN_HEIGHT);
        stage.setMinWidth(WINDOW_MIN_WIDTH);
        stage.show();
    }
}