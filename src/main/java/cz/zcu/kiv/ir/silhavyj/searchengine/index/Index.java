package cz.zcu.kiv.ir.silhavyj.searchengine.index;

import cz.zcu.kiv.ir.silhavyj.searchengine.preprocessing.IPreprocessor;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.util.*;

public class Index implements IIndex {

    private final Map<String, DocumentList> invertedIndex;
    private final Set<Integer> allDocumentIndexes;
    private final Set<String> filePaths;
    private final Map<Integer, String> indexFilePaths;
    private final Map<Integer, BagOfWords> bagOfWords;
    private final IPreprocessor preprocessor;
    private final IntegerProperty documentCount;
    private final IntegerProperty termCount;
    private final IntegerProperty tokenCount;

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

    @Override
    public int getDocumentCount() {
        return documentCount.get();
    }

    private void setDocumentCount(int value) {
        documentCount.set(value);
    }

    @Override
    public IntegerProperty documentCountProperty() {
        return documentCount;
    }

    @Override
    public int getTermCount() {
        return termCount.get();
    }

    private void setTermCount(int value) {
        termCount.set(value);
    }

    @Override
    public IntegerProperty termCountProperty() {
        return termCount;
    }

    @Override
    public int getTokenCount() {
        return tokenCount.get();
    }

    private void setTokenCount(int value) {
        tokenCount.set(value);
    }

    @Override
    public IntegerProperty tokenCountProperty() {
        return tokenCount;
    }

    @Override
    public double calculateTF_IDF(int index, Set<String> relevantTerms) {
        final var bow = bagOfWords.get(index);
        double rank = 0;
        double tf;
        double idf;

        for (final var term : relevantTerms) {
            tf = bow.getNumberOfOccurrences(term);
            if (invertedIndex.containsKey(term)) {
                idf = Math.log(invertedIndex.get(term).getCount()) / Math.log(getDocumentCount());
            } else {
                idf = 0;
            }
            rank += (tf * idf);
        }
        return rank;
    }

    @Override
    public void addDocument(final String term, int documentIndex, final String filePath) {
        Document document = new Document(documentIndex);
        if (!invertedIndex.containsKey(term)) {
            invertedIndex.put(term, new DocumentList());
            setTermCount(getTermCount() + 1);
        }
        if (!indexFilePaths.containsKey(documentIndex)) {
            indexFilePaths.put(documentIndex, filePath);
            bagOfWords.put(documentIndex, new BagOfWords());
        }
        final var documentList = invertedIndex.get(term);
        bagOfWords.get(documentIndex).addWord(term);
        documentList.add(document);
        allDocumentIndexes.add(documentIndex);
        setTokenCount(getTokenCount() + 1);
    }

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

    @Override
    public String getFilePath(int documentIndex) throws IllegalArgumentException {
        if (!indexFilePaths.containsKey(documentIndex)) {
            throw new IllegalArgumentException("Document has not been indexed yet");
        }
        return indexFilePaths.get(documentIndex);
    }

    @Override
    public Document getDocuments(String term) {
        if (invertedIndex.containsKey(term)) {
            return invertedIndex.get(term).getFirst();
        }
        return new Document();
    }

    @Override
    public boolean index(final String text, final String filePath) {
        if (!filePaths.contains(filePath)) {
            filePaths.add(filePath);
            preprocessor.tokenize(text).forEach(token -> addDocument(token, getDocumentCount(), filePath));
            setDocumentCount(getDocumentCount() + 1);
            return true;
        }
        return false;
    }

    @Override
    public final IPreprocessor getPreprocessor() {
        return preprocessor;
    }
}
