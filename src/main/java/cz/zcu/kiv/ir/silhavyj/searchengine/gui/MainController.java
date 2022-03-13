package cz.zcu.kiv.ir.silhavyj.searchengine.gui;

import cz.zcu.kiv.ir.silhavyj.searchengine.fetcher.BBCNewsProcessor;
import cz.zcu.kiv.ir.silhavyj.searchengine.fetcher.ISiteProcessor;
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

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;

import com.github.pemistahl.lingua.api.*;
import org.jsoup.Jsoup;

import static com.github.pemistahl.lingua.api.Language.*;
import static java.time.LocalDateTime.now;

public class MainController implements Initializable {

    private static final String FETCHED_DATA_FOLDER = "fetched-data";

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
    ISiteProcessor siteProcessor;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        siteProcessor = new BBCNewsProcessor();
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

    private JSONObject getJSONDocument(int documentIndex, final IIndex index) {
        final String rawData = IOUtils.readFile(index.getFilePath(documentIndex));
        try {
            return new JSONObject(rawData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void displayResults(final List<Integer> resultIndexer, final HashMap<Integer, Double> ranks, int count, long timeOfSearchInMS, final IIndex index, final Language language) {
        JSONObject data;
        int totalNumberOfDocument = 0;

        int documentIndex;
        final var iter = resultIndexer.listIterator();

        while (iter != null && iter.hasNext() && count > 0) {
            documentIndex = iter.next();
            data = getJSONDocument(documentIndex, index);
            if (data != null) {
                final var resultTab = createResultTab(data, documentIndex, index, ranks);
                resultsTabPane.getTabs().add(resultTab);
            }
            count--;
            totalNumberOfDocument++;
        }
        while (iter != null && iter.hasNext()) {
            totalNumberOfDocument++;
            iter.next();
        }
        statusLabel.setStyle("-fx-background-color: GREEN");
        statusLabel.setText(language + " - found " + totalNumberOfDocument + " matching documents (" + timeOfSearchInMS + "ms)");
    }

    private Tab createResultTab(final JSONObject data, int documentIndex, final IIndex index, final HashMap<Integer, Double> ranks) {
        Tab tab = new Tab();
        tab.setText(String.valueOf(Paths.get(index.getFilePath(documentIndex)).getFileName()));
        tab.setContent(createTabBody(data, documentIndex, index, ranks));
        tab.setOnClosed(e -> resultsTabPane.getTabs().remove(tab));
        return tab;
    }

    private VBox createTabBody(final JSONObject data, int documentIndex, final IIndex index, final HashMap<Integer, Double> ranks) {
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
        vBox.getChildren().add(createMetadataInfo("Rank", "" + ranks.get(documentIndex)));
        vBox.getChildren().add(createMetadataInfo("Location", index.getFilePath(documentIndex)));
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
        final var resultList = new ArrayList<Integer>();
        final var ranks = new HashMap<Integer, Double>();

        try {
            start = System.currentTimeMillis();
            result = queryParser.search(index, query);

            final var relevantWords = queryParser.getRelevantWords();
            final Set<String> relevantTerms = new HashSet<>();
            for (final var word : relevantWords) {
                relevantTerms.add(index.getPreprocessor().preprocess(word));
            }

            Document currentDocument = result;
            while (currentDocument != null) {
                resultList.add(currentDocument.getIndex());
                if (tfidfRadioButton.isSelected() && !relevantTerms.isEmpty()) {
                    ranks.put(currentDocument.getIndex(), index.calculateTF_IDF(currentDocument.getIndex(), relevantTerms));
                } else {
                    ranks.put(currentDocument.getIndex(), 0.0);
                }
                currentDocument = currentDocument.getNext();
            }
            resultList.sort((x, y) -> Double.compare(ranks.get(y), ranks.get(x)));
            end = System.currentTimeMillis();
            timeOfSearchInMS = end - start;
        } catch (Exception e) {
            statusLabel.setStyle("-fx-background-color: RED");
            if (queryParser.getErrorMessage() != null && !queryParser.getErrorMessage().equals("")) {
                statusLabel.setText(queryParser.getErrorMessage());
            } else {
                statusLabel.setText(e.getMessage());
            }
            return;
        }
        if (result == null || result.isUninitialized()) {
            statusLabel.setStyle("-fx-background-color: RED");
            statusLabel.setText(language + " - no results were found");
        } else {
            displayResults(resultList, ranks, (int)topResultsCountSlider.getValue(), timeOfSearchInMS, index, language);
        }
    }

    @FXML
    private void closeApplication() {
        Platform.exit();
        System.exit(0);
    }

    @FXML
    private void fetchDocumentFromURL() {
        // TODO
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText("Enter desired URL");
        final var result = dialog.showAndWait();
        if (result.isPresent()) {
            statusLabel.setStyle("-fx-background-color: RED");
            statusLabel.setText("fetching data...");
            final String url = result.get();
            final var webpage = fetchSiteContent(url);
            if (webpage.isEmpty()) {
                statusLabel.setStyle("-fx-background-color: RED");
                statusLabel.setText("invalid URL");
            } else {
                final var data = siteProcessor.processSite(webpage.get());
                if (data.isEmpty()) {
                    statusLabel.setStyle("-fx-background-color: RED");
                    statusLabel.setText("failed to fetch data from the given URL");
                } else {
                    final String article = (String)data.get().get("article");
                    final String filename = FETCHED_DATA_FOLDER + "/" + now() + ".json";
                    IOUtils.createDirectoryIfMissing(FETCHED_DATA_FOLDER);
                    IOUtils.writeToFile(filename, data.get().toString());
                    final var file = new File(filename);

                    var language = languageDetector.detectLanguageOf(article);
                    if (language == SLOVAK) {
                        language = CZECH;
                    }
                    IIndex index;
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
                                return;
                        }
                    }
                    index = languageIndexes.get(language.toString());
                    if (index == null || !index.index(article, file.getAbsolutePath())) {
                        statusLabel.setStyle("-fx-background-color: RED");
                        statusLabel.setText("failed to index the document");
                    } else {
                        statusLabel.setStyle("-fx-background-color: GREEN");
                        statusLabel.setText("document has been successfully fetched and indexed");
                    }
                }
            }
        }
    }

    private Optional<org.jsoup.nodes.Document> fetchSiteContent(final String url) {
        try {
            var connection = Jsoup.connect(url);
            var document = connection.get();
            return Optional.of(document);
        } catch (Exception e) {
            System.err.println(String.format("Could not fetch the content of %s", url));
        }
        return Optional.empty();
    }
}