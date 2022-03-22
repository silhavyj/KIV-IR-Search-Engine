package cz.zcu.kiv.ir.silhavyj.searchengine.gui;

import com.github.pemistahl.lingua.api.Language;
import com.github.pemistahl.lingua.api.LanguageDetector;
import com.github.pemistahl.lingua.api.LanguageDetectorBuilder;
import cz.zcu.kiv.ir.silhavyj.searchengine.fetcher.BBCNewsProcessor;
import cz.zcu.kiv.ir.silhavyj.searchengine.fetcher.ISiteProcessor;
import cz.zcu.kiv.ir.silhavyj.searchengine.index.Document;
import cz.zcu.kiv.ir.silhavyj.searchengine.index.IIndex;
import cz.zcu.kiv.ir.silhavyj.searchengine.index.Index;
import cz.zcu.kiv.ir.silhavyj.searchengine.preprocessing.CzechPreprocessor;
import cz.zcu.kiv.ir.silhavyj.searchengine.preprocessing.EnglishPreprocessor;
import cz.zcu.kiv.ir.silhavyj.searchengine.query.lexer.QueryLexer;
import cz.zcu.kiv.ir.silhavyj.searchengine.query.parser.IQueryParser;
import cz.zcu.kiv.ir.silhavyj.searchengine.query.parser.QueryParseInfix;
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
import org.jsoup.Jsoup;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;

import static com.github.pemistahl.lingua.api.Language.*;
import static java.time.LocalDateTime.now;

/***
 * @author Jakub Silhavy
 *
 * This class handles all inputs from the user.
 */
public class MainController implements Initializable {

    /*** Folder to store documents the user fetched from the BBC News*/
    private static final String FETCHED_DATA_FOLDER = "fetched-data";

    /*** File containing Czech stop words */
    private static final String STOPWORDS_CZECH = "stopwords-cs.txt";

    /*** File containing English stop words */
    private static final String STOPWORDS_ENGLISH = "stopwords-en.txt";

    /*** Bar menu in the top left corner */
    @FXML
    private MenuBar menuBar;

    /*** Menu item for adding documents from the disk */
    @FXML
    private MenuItem addJSONDocumentMenuItem;

    /*** Search button to perform a search */
    @FXML
    private Button searchBtn;

    /*** Status label (information about what's going on, error messages, etc.) */
    @FXML
    private Label statusLabel;

    /*** Text field where the user inputs a search query */
    @FXML
    private TextField queryTextField;

    /*** Tab pane holding the results of a search */
    @FXML
    private TabPane resultsTabPane;

    /*** Slider the user uses to adjust the number of displayed results of a document */
    @FXML
    private Slider topResultsCountSlider;

    /*** Label displaying the maximum of results to be shown to the user */
    @FXML
    private Label topResultsCountLabel;

    /*** TreeView containing information about different indexes within the application */
    @FXML
    private TreeView<String> indexTreeView;

    /*** Radio button to select searching in the Czech language */
    @FXML
    private RadioButton czechLanguageRadioBtn;

    /*** Radio button to select searching in the English language */
    @FXML
    private RadioButton englishLanguageRadioBtn;

    /*** Radio button to select ranking by TF-IDF */
    @FXML
    private RadioButton tfidfRadioButton;

    /*** Radio button to select ranking by cosine similarity */
    @FXML
    private RadioButton cosineSimilarityRadioButton;

    /*** Button to terminate importing documents */
    @FXML
    private Button stopLoadingBtn;

    /*** Root item (displaying information about different indexes) */
    private TreeItem<String> treeRootItem;

    /*** Flag if the stop button should be displayed or not */
    private boolean stopDocumentLoading;

    /*** Instance of a query parser */
    private final IQueryParser queryParser = new QueryParseInfix(new QueryLexer());

    /*** Map of different indexes by their languages */
    private final Map<String, IIndex> languageIndexes = new HashMap<>();

    /*** Instance of a language detector */
    private final LanguageDetector languageDetector = LanguageDetectorBuilder.fromLanguages(ENGLISH, CZECH, SLOVAK).build();

    /*** Instance of a site processor */
    private ISiteProcessor siteProcessor;

    /***
     * Initializes the class.
     * @param url unused
     * @param resourceBundle unused
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Instantiate the site processor
        siteProcessor = new BBCNewsProcessor();

        // Bind the slider and the label (number of results to
        // be displayed to the user).
        topResultsCountSlider.valueProperty().addListener((observableValue, oldValue, newValue) -> {
            if (newValue == null) {
                topResultsCountLabel.setText("null");
                return;
            }
            topResultsCountLabel.setText(String.format("(%d)", Math.round(newValue.intValue())));
        });

        // Create the root node of the tree view.
        treeRootItem = new TreeItem<>("index");
        indexTreeView.setRoot(treeRootItem);

        // Hide the stop button.
        stopLoadingBtn.setVisible(false);
    }

    /***
     * Disables any user input.
     * This method is called after the user submits documents
     * to be added to the application. All user inputs are enabled
     * either when the insertion finishes or when they click the stop button.
     * @param disable flag if the user input should be enabled or disabled
     */
    private void disableUserInput(boolean disable) {
        addJSONDocumentMenuItem.setDisable(disable);
        searchBtn.setDisable(disable);
        queryTextField.setDisable(disable);
        stopLoadingBtn.setVisible(disable);
    }

    /***
     * Returns a JSON document by a given index.
     * It gets the path of the document on the disk, reads it,
     * and parses it.
     * @param documentIndex index of the document to be parsed
     * @param index Index in which the document is held
     * @return JSON object, if all goes well. Null, otherwise.
     */
    private JSONObject getJSONDocument(int documentIndex, final IIndex index) {
        final String rawData = IOUtils.readFile(index.getFilePath(documentIndex));
        try {
            return new JSONObject(rawData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /***
     * Displays the results of a search to the user.
     * Each document is displayed in a separate tab.
     * @param resultIndexer List of documents matching the query (sorted)
     * @param ranks map of ranks (each document has its own value)
     * @param count maximum number of documents to be displayed
     * @param timeOfSearchInMS how much time the query took in milliseconds
     * @param index Index in which the search was performed
     * @param language Language in which the search was performed
     */
    private void displayResults(final List<Integer> resultIndexer, final HashMap<Integer, Double> ranks, int count, long timeOfSearchInMS, final IIndex index, final Language language) {
        JSONObject data;
        int totalNumberOfDocument = 0;
        int documentIndex;
        final var iter = resultIndexer.listIterator();

        // Iterate through all documents and display them to the user.
        while (iter.hasNext() && count > 0) {
            // Get the current document index.
            documentIndex = iter.next();

            // Read the documents from the disk, and if it was loaded successfully,
            // create a new tab for it.
            data = getJSONDocument(documentIndex, index);
            if (data != null) {
                final var resultTab = createResultTab(data, documentIndex, index, ranks);
                resultsTabPane.getTabs().add(resultTab);
            }
            count--;
            totalNumberOfDocument++;
        }

        // Finish calculating how many results we found in total and
        // display it into the status bar (label).
        while (iter.hasNext()) {
            totalNumberOfDocument++;
            iter.next();
        }
        statusLabel.setStyle("-fx-background-color: GREEN");
        statusLabel.setText(language + " - found " + totalNumberOfDocument + " matching documents (" + timeOfSearchInMS + "ms)");
    }

    /***
     * Creates a new tab for a given document
     * @param data document in a JSON format (it contains the text as well as its metadata)
     * @param documentIndex index of the document
     * @param index Index in which the document is held
     * @param ranks map of ranks
     * @return new tab holding information about the document
     */
    private Tab createResultTab(final JSONObject data, int documentIndex, final IIndex index, final HashMap<Integer, Double> ranks) {
        Tab tab = new Tab();
        tab.setText(String.valueOf(Paths.get(index.getFilePath(documentIndex)).getFileName()));
        tab.setContent(createTabBody(data, documentIndex, index, ranks));
        tab.setOnClosed(e -> resultsTabPane.getTabs().remove(tab));
        return tab;
    }

    /***
     * Creates the body of a tab that hold information about a document.
     * @param data document in a JSON format (it contains the text as well as its metadata)
     * @param documentIndex index of the document
     * @param index Index in which the document is held
     * @param ranks map of ranks
     * @return new body holding information about the document
     */
    private VBox createTabBody(final JSONObject data, int documentIndex, final IIndex index, final HashMap<Integer, Double> ranks) {
        VBox vBox = new VBox();
        vBox.setSpacing(5);
        vBox.setPadding(new Insets(20, 20, 20, 20));

        // Display the author's name (if present)
        if (data.has("author")) {
            vBox.getChildren().add(createMetadataInfo("Author", (String)data.get("author")));
        }

        // Display the datetime (if present).
        if (data.has("datetime")) {
            vBox.getChildren().add(createMetadataInfo("Datetime", (String)data.get("datetime")));
        }

        // Display the subject (if present).
        if (data.has("subject")) {
            vBox.getChildren().add(createMetadataInfo("Subject", (String)data.get("subject")));
        }

        // Display the rank of the document.
        vBox.getChildren().add(createMetadataInfo("Rank", "" + ranks.get(documentIndex)));

        // Display the location of the document on the disk.
        vBox.getChildren().add(createMetadataInfo("Location", index.getFilePath(documentIndex)));

        vBox.getChildren().add(new Separator());

        // Display the title of the document.
        vBox.getChildren().add(createTitle((String)data.get("title")));

        // Display the URL (if present).
        if (data.has("url")) {
            vBox.getChildren().add(createHyperlink((String)data.get("url")));
        }

        // Display the content of the document.
        vBox.getChildren().add(createTextArea((String)data.get("article")));
        return vBox;
    }

    /***
     * Creates a hyperlink for the URL of a document.
     * @param url link of the document
     * @return HBox holding the URL of a document
     */
    private HBox createHyperlink(final String url) {
        HBox hBox = new HBox();
        hBox.setSpacing(10);
        Hyperlink hyperlink = new Hyperlink();
        hyperlink.setText(url);
        hBox.getChildren().add(hyperlink);
        return hBox;
    }

    /***
     * Creates a piece of metadata about the document (e.g, author, or datetime).
     * @param key key e.g. 'Author'
     * @param value value e.g. 'John Smith'
     * @return HBox containing the piece of metadata
     */
    private HBox createMetadataInfo(final String key, final String value) {
        HBox hBox = new HBox();
        hBox.setSpacing(10);
        hBox.getChildren().add(createLabel(key, true));
        hBox.getChildren().add(createLabel(value, false));
        return hBox;
    }

    /***
     * Creates a label.
     * @param text text to be displayed in the label
     * @param key flag if the label is the key of a piece of metadata e.g. 'Author'
     * @return create Label
     */
    private Label createLabel(final String text, boolean key) {
        Label label = new Label();
        label.setText(text);

        // Make it bold if the label is a key.
        if (key) {
            label.setStyle("-fx-font-weight: bold");
        }
        return label;
    }

    /***
     * Creates the title of a document.
     * @param title title of the document
     * @return HBox containing the created title of a document.
     */
    private HBox createTitle(final String title) {
        HBox hBox = new HBox();
        hBox.setSpacing(10);
        VBox.setMargin(hBox, new Insets(5, 0, 0, 0));

        // Create a label.
        final var titleLabel = createLabel(title, true);

        // Make sure the title is capable of displaying multiple lines.
        titleLabel.setWrapText(true);
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
    private void stopDocumentLoading() {
        stopDocumentLoading = true;
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
            stopDocumentLoading = false;
            disableUserInput(true);
            int processedDocuments = 0;
            double progress;
            long startTime = System.currentTimeMillis();
            statusLabel.setStyle("-fx-background-color: GREEN");

            for (final var file : files) {
                if (stopDocumentLoading) {
                    break;
                }
                final var content = IOUtils.readFile(file.getAbsolutePath());
                processedDocuments++;
                JSONObject data = parseJSON(content);
                if (data != null) {
                    final String article = data.get("title") + " " + data.get("article");
                    var language = languageDetector.detectLanguageOf(article);
                    if (language == SLOVAK) {
                        language = CZECH;
                    }
                    if (!languageIndexes.containsKey(language.toString())) {
                        switch (language) {
                            case CZECH:
                                index = new Index(new CzechPreprocessor(STOPWORDS_CZECH));
                                languageIndexes.put(language.toString(), index);
                                treeRootItem.getChildren().add(createIndexTreeRecord(index, language.toString()));
                                break;
                            case ENGLISH:
                                index = new Index(new EnglishPreprocessor(STOPWORDS_ENGLISH));
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
                    }
                } else {
                    System.out.print("Failed to parse document " + file.getName());
                }
                progress = (double) processedDocuments / files.size() * 100.0;
                double finalProgress = progress;
                long timeStamp = (long)((System.currentTimeMillis() - startTime) * 0.0000167);
                if (finalProgress >= 100) {
                    Platform.runLater(() -> statusLabel.setText("Done " + String.format("%.2f", finalProgress) + "% | " + timeStamp + " min"));
                } else {
                    Platform.runLater(() -> statusLabel.setText("Indexing in progress " + String.format("%.2f", finalProgress) + "% | " + timeStamp + " min"));
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
                } else if (cosineSimilarityRadioButton.isSelected() && !relevantTerms.isEmpty()) {
                    ranks.put(currentDocument.getIndex(), index.calculateCosineSimilarity(currentDocument.getIndex(), relevantTerms));
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
                    final String article = data.get().get("title") + " "+ data.get().get("article");
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
                                index = new Index(new CzechPreprocessor(STOPWORDS_CZECH));
                                languageIndexes.put(language.toString(), index);
                                treeRootItem.getChildren().add(createIndexTreeRecord(index, language.toString()));
                                break;
                            case ENGLISH:
                                index = new Index(new EnglishPreprocessor(STOPWORDS_ENGLISH));
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
            System.err.printf("Could not fetch the content of %s%n", url);
        }
        return Optional.empty();
    }
}