package cz.zcu.kiv.ir.silhavyj.searchengine.gui;

import cz.zcu.kiv.ir.silhavyj.searchengine.index.IIndex;
import cz.zcu.kiv.ir.silhavyj.searchengine.index.Index;
import cz.zcu.kiv.ir.silhavyj.searchengine.preprocessing.EnglishPreprocessor;
import cz.zcu.kiv.ir.silhavyj.searchengine.preprocessing.IPreprocessor;
import cz.zcu.kiv.ir.silhavyj.searchengine.query.lexer.QueryLexer;
import cz.zcu.kiv.ir.silhavyj.searchengine.query.parser.IQueryParser;
import cz.zcu.kiv.ir.silhavyj.searchengine.query.parser.QueryParser;
import cz.zcu.kiv.ir.silhavyj.searchengine.utils.IOUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.json.JSONObject;

public class MainController {

    @FXML
    private MenuBar menuBar;

    @FXML
    private MenuItem addJSONDocumentMenuItem;

    @FXML
    private MenuItem addTXTDocumentMenuItem;

    @FXML
    private Button searchBtn;

    final IPreprocessor englishPreprocessor = new EnglishPreprocessor("stopwords.txt");
    final IIndex index = new Index(englishPreprocessor);
    final IQueryParser queryParser = new QueryParser(new QueryLexer());

    private void disableUserInput(boolean disable) {
        addJSONDocumentMenuItem.setDisable(disable);
        addTXTDocumentMenuItem.setDisable(disable);
        searchBtn.setDisable(disable);
    }

    @FXML
    private void addJSONDocument() {
        final Stage stage = (Stage)menuBar.getScene().getWindow();
        final FileChooser fileChooser = new FileChooser();

        fileChooser.setTitle("Select a document to index");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("JSON", "*.json"));

        final var files = fileChooser.showOpenMultipleDialog(stage);
        disableUserInput(true);

        // TODO make sure it terminates when the application terminates
        new Thread(() -> {
            if (files != null) {
                for (final var file : files) {
                    final var content = IOUtils.readFile(file.getAbsolutePath());
                    try {
                        JSONObject data = new JSONObject(content);
                        if (index.index((String)data.get("article"), file.getAbsolutePath())) {
                            System.out.println(file.getName() + " has been indexed");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            disableUserInput(false);
        }).start();
    }

    @FXML
    private void addTXTDocument() {
        // TODO
    }
}