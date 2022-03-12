package cz.zcu.kiv.ir.silhavyj.searchengine.gui;

import cz.zcu.kiv.ir.silhavyj.searchengine.index.Document;
import cz.zcu.kiv.ir.silhavyj.searchengine.index.IIndex;
import cz.zcu.kiv.ir.silhavyj.searchengine.index.Index;
import cz.zcu.kiv.ir.silhavyj.searchengine.preprocessing.CzechPreprocessor;
import cz.zcu.kiv.ir.silhavyj.searchengine.preprocessing.EnglishPreprocessor;
import cz.zcu.kiv.ir.silhavyj.searchengine.query.lexer.QueryLexer;
import cz.zcu.kiv.ir.silhavyj.searchengine.query.parser.IQueryParser;
import cz.zcu.kiv.ir.silhavyj.searchengine.query.parser.QueryParser;
import cz.zcu.kiv.ir.silhavyj.searchengine.utils.IOUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.net.URL;
import java.nio.file.Paths;
import java.util.*;

import com.github.pemistahl.lingua.api.*;
import static com.github.pemistahl.lingua.api.Language.*;

public class MainController implements Initializable {

    @FXML
    private MenuBar menuBar;

    @FXML
    private MenuItem addJSONDocumentMenuItem;

    @FXML
    private Button searchBtn;

    @FXML
    private Label statusLabel;

    @FXML
    private TextField queryTextField;

    @FXML
    private TabPane resultsTabPane;

    @FXML
    private Slider topResultsCountSlider;

    @FXML
    private Label topResultsCountLabel;

    @FXML
    private TreeView<String> indexTreeView;

    @FXML
    private RadioButton czechLanguageRadioBtn;

    @FXML
    private RadioButton englishLanguageRadioBtn;

    @FXML
    private RadioButton tfidfRadioButton;

    private TreeItem<String> treeRootItem;

    final IQueryParser queryParser = new QueryParser(new QueryLexer());

    final Map<String, IIndex> languageIndexes = new HashMap<>();
    final LanguageDetector languageDetector = LanguageDetectorBuilder.fromLanguages(ENGLISH, CZECH, SLOVAK).build();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        topResultsCountSlider.valueProperty().addListener((observableValue, oldValue, newValue) -> {
            if (newValue == null) {
                topResultsCountLabel.setText("null");
                return;
            }
            topResultsCountLabel.setText(String.format("(%d)", Math.round(newValue.intValue())));
        });

        treeRootItem = new TreeItem<>("index");
        indexTreeView.setRoot(treeRootItem);
    }

    private void disableUserInput(boolean disable) {
        addJSONDocumentMenuItem.setDisable(disable);
        searchBtn.setDisable(disable);
        queryTextField.setDisable(disable);
    }

    private JSONObject getJSONDocument(final Document document, final IIndex index) {
        final String rawData = IOUtils.readFile(index.getFilePath(document.getIndex()));
        try {
            return new JSONObject(rawData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void displayResults(Document document, int count, long timeOfSearchInMS, final IIndex index, final Language language) {
        JSONObject data;
        int totalNumberOfDocument = 0;
        while (document != null && count > 0) {
            data = getJSONDocument(document, index);
            if (data != null) {
                final var resultTab = createResultTab(data, document, index);
                resultsTabPane.getTabs().add(resultTab);
                document = document.getNext();
            }
            count--;
            totalNumberOfDocument++;
        }
        while (document != null) {
            totalNumberOfDocument++;
            document = document.getNext();
        }
        statusLabel.setStyle("-fx-background-color: GREEN");
        statusLabel.setText(language + " - found " + totalNumberOfDocument + " matching documents (" + timeOfSearchInMS + "ms)");
    }

    private Tab createResultTab(final JSONObject data, final Document document, final IIndex index) {
        Tab tab = new Tab();
        tab.setText(String.valueOf(Paths.get(index.getFilePath(document.getIndex())).getFileName()));
        tab.setContent(createTabBody(data, document, index));
        tab.setOnClosed(e -> resultsTabPane.getTabs().remove(tab));
        return tab;
    }

    private VBox createTabBody(final JSONObject data, final Document document, final IIndex index) {
        VBox vBox = new VBox();
        vBox.setSpacing(5);
        vBox.setPadding(new Insets(20, 20, 20, 20));

        if (data.has("author")) {
            vBox.getChildren().add(createMetadataInfo("Author", (String)data.get("author")));
        }
        if (data.has("datetime")) {
            vBox.getChildren().add(createMetadataInfo("Datetime", (String)data.get("datetime")));
        }
        if (data.has("subject")) {
            vBox.getChildren().add(createMetadataInfo("Subject", (String)data.get("subject")));
        }
        vBox.getChildren().add(createMetadataInfo("Location", index.getFilePath(document.getIndex())));
        vBox.getChildren().add(createSeparator());
        vBox.getChildren().add(createTitle((String)data.get("title")));
        if (data.has("url")) {
            vBox.getChildren().add(createHyperlink((String)data.get("url")));
        }
        vBox.getChildren().add(createTextArea((String)data.get("article")));

        return vBox;
    }

    private HBox createHyperlink(final String url) {
        HBox hBox = new HBox();
        hBox.setSpacing(10);
        Hyperlink hyperlink = new Hyperlink();
        hyperlink.setText(url);
        hBox.getChildren().add(hyperlink);
        return hBox;
    }

    private HBox createMetadataInfo(final String key, final String value) {
        HBox hBox = new HBox();
        hBox.setSpacing(10);
        hBox.getChildren().add(createLabel(key, true));
        hBox.getChildren().add(createLabel(value, false));
        return hBox;
    }

    private Separator createSeparator() {
        return new Separator();
    }

    private Label createLabel(final String text, boolean key) {
        Label label = new Label();
        label.setText(text);
        if (key) {
            label.setStyle("-fx-font-weight: bold");
        }
        return label;
    }

    private HBox createTitle(final String title) {
        HBox hBox = new HBox();
        hBox.setSpacing(10);
        VBox.setMargin(hBox, new Insets(5, 0, 0, 0));

        final var titleLabel = createLabel(title, true);
        titleLabel.setStyle("-fx-font-size: 18px");

        hBox.getChildren().add(titleLabel);
        return hBox;
    }

    private TextArea createTextArea(final String content) {
        TextArea textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setText(content);
        textArea.setWrapText(true);
        VBox.setVgrow(textArea, Priority.ALWAYS);
        VBox.setMargin(textArea, new Insets(10, 0, 0, 0));
        return textArea;
    }

    private HBox createDocumentCountInfo(final IIndex index) {
        HBox hBox = new HBox();
        final var desc = createLabel("documents: ", false);
        final var value = createLabel("0", false);
        index.documentCountProperty().addListener((observableValue, oldValue, newValue) -> {
            if (newValue == null) {
                Platform.runLater(() -> value.setText("null"));
            } else {
                Platform.runLater(() -> value.setText(newValue.toString()));
            }
        });
        hBox.getChildren().addAll(desc, value);
        return hBox;
    }

    private HBox createTokenCountInfo(final IIndex index) {
        HBox hBox = new HBox();
        final var desc = createLabel("tokens: ", false);
        final var value = createLabel("0", false);
        index.tokenCountProperty().addListener((observableValue, oldValue, newValue) -> {
            if (newValue == null) {
                Platform.runLater(() -> value.setText("null"));
            } else {
                Platform.runLater(() -> value.setText(newValue.toString()));
            }
        });
        hBox.getChildren().addAll(desc, value);
        return hBox;
    }

    private HBox createTermCountInfo(final IIndex index) {
        HBox hBox = new HBox();
        final var desc = createLabel("terms: ", false);
        final var value = createLabel("0", false);
        index.termCountProperty().addListener((observableValue, oldValue, newValue) -> {
            if (newValue == null) {
                Platform.runLater(() -> value.setText("null"));
            } else {
                Platform.runLater(() -> value.setText(newValue.toString()));
            }
        });
        hBox.getChildren().addAll(desc, value);
        return hBox;
    }

    private TreeItem createIndexTreeRecord(final IIndex index, final String name) {
        TreeItem treeItem = new TreeItem(name);
        treeItem.getChildren().add(new TreeItem<>(createDocumentCountInfo(index)));
        treeItem.getChildren().add(new TreeItem<>(createTermCountInfo(index)));
        treeItem.getChildren().add(new TreeItem<>(createTokenCountInfo(index)));
        return treeItem;
    }

    JSONObject parseJSON(final String text) {
        try {
            JSONObject data = new JSONObject(text);
            if (data.has("article") && data.has("title")) {
                return data;
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    @FXML
    private void addJSONDocument() {
        final Stage stage = (Stage)menuBar.getScene().getWindow();
        final FileChooser fileChooser = new FileChooser();

        fileChooser.setTitle("Select a document to index");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("JSON", "*.json"));

        final var files = fileChooser.showOpenMultipleDialog(stage);
        if (files == null) {
            return;
        }
        final var loaderWorker = new Thread(() -> {
            IIndex index;
            disableUserInput(true);
            int processedDocuments = 0;
            double progress;
            long startTime = System.currentTimeMillis();
            statusLabel.setStyle("-fx-background-color: GREEN");

            for (final var file : files) {
                final var content = IOUtils.readFile(file.getAbsolutePath());
                processedDocuments++;
                JSONObject data = parseJSON(content);
                if (data != null) {
                    final String article = (String)data.get("article");
                    var language = languageDetector.detectLanguageOf(article);
                    if (language == SLOVAK) {
                        language = CZECH;
                    }
                    if (!languageIndexes.containsKey(language.toString())) {
                        switch (language) {
                            case CZECH:
                                index = new Index(new CzechPreprocessor("stopwords-cs.txt"));
                                languageIndexes.put(language.toString(), index);
                                treeRootItem.getChildren().add(createIndexTreeRecord(index, language.toString()));
                                break;
                            case ENGLISH:
                                index = new Index(new EnglishPreprocessor("stopwords-en.txt"));
                                languageIndexes.put(language.toString(), index);
                                treeRootItem.getChildren().add(createIndexTreeRecord(index, language.toString()));
                                break;
                            default:
                                System.out.println("Language detected in " + file.getName() + " is not supported");
                                continue;
                        }
                    }
                    index = languageIndexes.get(language.toString());
                    if (index == null || !index.index(article, file.getAbsolutePath())) {
                        System.out.println("Failed to index document " + file.getName());
                    } else {
                        progress = (double) processedDocuments / files.size() * 100.0;
                        double finalProgress = progress;
                        long timeStamp = (long)((System.currentTimeMillis() - startTime) * 0.0000167);
                        if (finalProgress >= 100) {
                            Platform.runLater(() -> statusLabel.setText("Done " + String.format("%.2f", finalProgress) + "% | " + timeStamp + " min"));
                        } else {
                            Platform.runLater(() -> statusLabel.setText("Indexing in progress " + String.format("%.2f", finalProgress) + "% | " + timeStamp + " min"));
                        }
                    }
                } else {
                    System.out.print("Failed to parse document " + file.getName());
                }
            }
            disableUserInput(false);
        });
        loaderWorker.setDaemon(true);
        loaderWorker.start();
    }

    @FXML
    private void search() {
        resultsTabPane.getTabs().clear();

        final String query = queryTextField.getText();
        if (query == null || query.isEmpty()) {
            statusLabel.setStyle("-fx-background-color: RED");
            statusLabel.setText("Query is empty");
            return;
        }

        long end;
        long start;
        long timeOfSearchInMS;

        Language language;
        if (czechLanguageRadioBtn.isSelected()) {
            language = CZECH;
        } else if (englishLanguageRadioBtn.isSelected()) {
            language = ENGLISH;
        } else {
            language = languageDetector.detectLanguageOf(query);
        }

        if (!languageIndexes.containsKey(language.toString())) {
            statusLabel.setStyle("-fx-background-color: RED");
            statusLabel.setText("There are no files indexed in the " + language + " language");
            return;
        }
        IIndex index = languageIndexes.get(language.toString());

        Document result;
        try {
            start = System.currentTimeMillis();
            result = queryParser.search(index, query);
            end = System.currentTimeMillis();
            timeOfSearchInMS = end - start;
            if (tfidfRadioButton.isSelected()) {
                // TODO sort them according to their TF-IDF values
            }
        } catch (Exception e) {
            statusLabel.setStyle("-fx-background-color: RED");
            statusLabel.setText(queryParser.getErrorMessage());
            return;
        }
        if (result == null || result.isUninitialized()) {
            statusLabel.setStyle("-fx-background-color: RED");
            statusLabel.setText(language + " - no results were found");
        } else {
            displayResults(result, (int)topResultsCountSlider.getValue(), timeOfSearchInMS, index, language);
        }
    }

    @FXML
    private void closeApplication() {
        Platform.exit();
        System.exit(0);
    }
}