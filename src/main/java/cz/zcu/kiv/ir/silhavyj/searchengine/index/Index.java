package cz.zcu.kiv.ir.silhavyj.searchengine.index;

import cz.zcu.kiv.ir.silhavyj.searchengine.preprocessing.IPreprocessor;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.util.*;

/***
 * @author Jakub Silhavy
 *
 * This class represents an inverted index. It is filled during
 * preprocessing and havily used when evaluating a query.
 */
public class Index implements IIndex {

    /*** Inverted index (key = word, value = linked list) */
    private final Map<String, DocumentList> invertedIndex;

    /*** Set of all documents that have been indexed */
    private final Set<Integer> allDocumentIndexes;

    /*** Set of all file paths to all documents */
    private final Set<String> filePaths;

    /*** Map of file paths (key = document index, value = path) */
    private final Map<Integer, String> indexFilePaths;

    /*** Map of all documents (key = document index, value = bag of words) */
    private final Map<Integer, BagOfWords> bagOfWords;

    /*** Instance of a preprocessor */
    private final IPreprocessor preprocessor;

    /*** Number of documents that have been indexed */
    private final IntegerProperty documentCount;

    /*** Number of terms occurred during indexing */
    private final IntegerProperty termCount;

    /*** Number of tokens occurred during indexing */
    private final IntegerProperty tokenCount;

    /***
     * Creates an instance of the class.
     * @param preprocessor instance of a word preprocessor
     */
    public Index(final IPreprocessor preprocessor) {
        this.preprocessor = preprocessor;
        invertedIndex = new HashMap<>();
        allDocumentIndexes = new TreeSet<>();
        indexFilePaths = new HashMap<>();
        filePaths = new HashSet<>();
        bagOfWords = new HashMap<>();
        documentCount = new SimpleIntegerProperty(0);
        termCount = new SimpleIntegerProperty(0);
        tokenCount = new SimpleIntegerProperty(0);
    }

    /***
     * Return the total number of documents.
     * @return number of documents that have been indexed
     */
    @Override
    public int getDocumentCount() {
        return documentCount.get();
    }

    /***
     * Updates the number of documents that have been indexed.
     * @param value new value (number of documents)
     */
    private void setDocumentCount(int value) {
        documentCount.set(value);
    }

    /***
     * Return document count property that is
     * used to update the view.
     * @return document count property
     */
    @Override
    public IntegerProperty documentCountProperty() {
        return documentCount;
    }

    /***
     * Return the total number of terms.
     * @return number of terms that were encountered during indexing
     */
    @Override
    public int getTermCount() {
        return termCount.get();
    }

    /***
     * Updates the number of terms.
     * @param value new value (number of terms)
     */
    private void setTermCount(int value) {
        termCount.set(value);
    }

    /***
     * Return term count property that is
     * used to update the view.
     * @return term count property
     */
    @Override
    public IntegerProperty termCountProperty() {
        return termCount;
    }

    /***
     * Returns number of tokens occurred during indexing.
     * @return total number of tokens
     */
    @Override
    public int getTokenCount() {
        return tokenCount.get();
    }

    /***
     * Updates the number of tokens.
     * @param value new value (number of tokens)
     */
    private void setTokenCount(int value) {
        tokenCount.set(value);
    }

    /***
     * Return token count property that is
     * used to update the view.
     * @return token count property
     */
    @Override
    public IntegerProperty tokenCountProperty() {
        return tokenCount;
    }

    /***
     * Calculates TF-IDF for a given document and a unique
     * set of words (query).
     * @param index index of a document
     * @param relevantTerms relevant words of a query
     * @return value of TF-IDF
     */
    @Override
    public double calculateTF_IDF(int index, Set<String> relevantTerms) {
        // Create one bag-of-words that will hold all words of both documents.
        final var bag = new BagOfWords();

        // Get the bag of words of the document.
        final var bow1 = bagOfWords.get(index);

        // Create a bag-of-words out of the given words of a query
        final var bow2 = new BagOfWords();
        bow2.addAllWords(relevantTerms);

        // Union both bags into the overall bag-of-words.
        bag.addAllWords(bow1);
        bag.addAllWords(bow2);

        double normDoc1 = 0;
        double normDoc2 = 0;
        double multi = 0;

        for (var word : bag.getWords()) {
            double IDF = 0;
            double val1 = 0;
            double val2 = 0;

            if (bow1.contains(word)) {
                val1 = 1 + Math.log10(bow1.getNumberOfOccurrences(word));
            }
            if (bow2.contains(word)) {
                val2 = 1 + Math.log10(bow2.getNumberOfOccurrences(word));
            }

            if (invertedIndex.containsKey(word)) {
                IDF = Math.log10((double)getDocumentCount() / invertedIndex.get(word).getCount());
            }

            val1 *= IDF;
            val2 *= IDF;

            multi += val1 * val2;
            normDoc1 += val1 * val1;
            normDoc2 += val2 * val2;
        }
        normDoc1 = Math.sqrt(normDoc1);
        normDoc2 = Math.sqrt(normDoc2);

        if (normDoc1 * normDoc2 == 0)
            return 0;

        return multi / (normDoc1 * normDoc2);
    }

    /***
     * Calculates cosine similarity for a given document and relevant words of a query.
     * @param index index of a document
     * @param relevantTerms relevant words of a query
     * @return cosine similarity
     */
    @Override
    public double calculateCosineSimilarity(int index, Set<String> relevantTerms) {
        // Create one bag-of-words that will hold all words of both documents.
        final var bag = new BagOfWords();

        // Get the bag of words of the document.
        final var bow1 = bagOfWords.get(index);

        // Create a bag-of-words out of the given words of a query
        final var bow2 = new BagOfWords();
        bow2.addAllWords(relevantTerms);

        // Union both bags into the overall bag-of-words.
        bag.addAllWords(bow1);
        bag.addAllWords(bow2);

        double normDoc1 = 0;
        double normDoc2 = 0;
        double multi = 0;

        for (var word : bag.getWords()) {
            double val1 = bow1.getNumberOfOccurrences(word);
            double val2 = bow2.getNumberOfOccurrences(word);

            multi += val1 * val2;
            normDoc1 += val1 * val1;
            normDoc2 += val2 * val2;

            index++;
        }
        normDoc1 = Math.sqrt(normDoc1);
        normDoc2 = Math.sqrt(normDoc2);

        if (normDoc1 * normDoc2 == 0)
            return 0;
        return multi / (normDoc1 * normDoc2);

    }

    /***
     * Adds a document into the index.
     * @param term term that has been found in a document
     * @param documentIndex index of the document where the term's been found
     * @param filePath path to the document
     */
    @Override
    public void addDocument(final String term, int documentIndex, final String filePath) {
        // Create a new document (item of a linked list)
        Document document = new Document(documentIndex);

        // Check if the term is seen for the first time.
        if (!invertedIndex.containsKey(term)) {
            invertedIndex.put(term, new DocumentList());
            // Increment the term count.
            setTermCount(getTermCount() + 1);
        }

        // Check if the document has been previously used. If not,
        // create a new record - bag of words, path
        if (!indexFilePaths.containsKey(documentIndex)) {
            indexFilePaths.put(documentIndex, filePath);
            bagOfWords.put(documentIndex, new BagOfWords());
        }

        // Get the wrapper (linked list) by the term and append the document to the end.
        final var documentList = invertedIndex.get(term);
        documentList.add(document);

        // Add the term into the bag of words and add
        // the document into the set of all documents as well.
        bagOfWords.get(documentIndex).addWord(term);
        allDocumentIndexes.add(documentIndex);

        // Increment the token count.
        setTokenCount(getTokenCount() + 1);
    }

    /***
     * Returns a linked list of all documents.
     * @return head of a linked list of all documents.
     */
    @Override
    public Document getAllDocumentIndexes() {
        Document head = null;
        Document currentDoc = null;

        for (final var index : allDocumentIndexes) {
            if (head == null) {
                head = new Document(index);
                currentDoc = head;
            } else {
                currentDoc.setNext(new Document(index));
                currentDoc = currentDoc.getNext();
            }
        }
        return head;
    }

    /***
     * Returns a filepath of a given document
     * @param documentIndex index of a document
     * @return filepath of a give document
     * @throws IllegalArgumentException if the document has not been indexed yet
     */
    @Override
    public String getFilePath(int documentIndex) throws IllegalArgumentException {
        if (!indexFilePaths.containsKey(documentIndex)) {
            throw new IllegalArgumentException("Document has not been indexed yet");
        }
        return indexFilePaths.get(documentIndex);
    }

    /***
     * Returns a linked list (row in the index) of a given term
     * @param term given term
     * @return linked list (row in the index) of a given term
     */
    @Override
    public Document getDocuments(String term) {
        if (invertedIndex.containsKey(term)) {
            return invertedIndex.get(term).getFirst();
        }
        // Return an empty linked list of documents.
        return new Document();
    }

    /***
     * Indexes a document given as a piece of text
     * @param text content of a document to be indexed
     * @param filePath filepath of the document
     * @return True, if the document's been indexed successfully. False, otherwise.
     */
    @Override
    public boolean index(final String text, final String filePath) {
        // Check if the filepath is already indexed
        if (!filePaths.contains(filePath)) {
            filePaths.add(filePath);

            // Preprocess the text and index it term by term.
            preprocessor.tokenize(text).forEach(token -> addDocument(token, getDocumentCount(), filePath));

            // Increment the document count.
            setDocumentCount(getDocumentCount() + 1);
            return true;
        }
        return false;
    }

    /***
     * Returns the instance of a preprocessor used within the index.
     * @return preprocessor of the index
     */
    @Override
    public final IPreprocessor getPreprocessor() {
        return preprocessor;
    }
}
