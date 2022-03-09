package cz.zcu.kiv.ir.silhavyj.searchengine.index;

import cz.zcu.kiv.ir.silhavyj.searchengine.preprocessing.IPreprocessor;

import java.util.*;

public class Index implements IIndex {

    private final Map<String, DocumentList> invertedIndex;
    private final Set<Integer> allDocumentIndexes;
    private final Set<String> filePaths;
    private final Map<Integer, String> indexFilePaths;
    private final IPreprocessor preprocessor;
    private int currentIndex;

    public Index(final IPreprocessor preprocessor) {
        this.preprocessor = preprocessor;
        invertedIndex = new HashMap<>();
        allDocumentIndexes = new TreeSet<>();
        indexFilePaths = new HashMap<>();
        filePaths = new HashSet<>();
        currentIndex = 0;
    }

    @Override
    public void addDocument(final String term, int documentIndex, final String filePath) {
        Document document = new Document(documentIndex);
        if (!invertedIndex.containsKey(term)) {
            invertedIndex.put(term, new DocumentList(term));
        }
        if (!indexFilePaths.containsKey(documentIndex)) {
            indexFilePaths.put(documentIndex, filePath);
        }
        final var documentList = invertedIndex.get(term);
        documentList.add(document);
        allDocumentIndexes.add(documentIndex);
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
            preprocessor.tokenize(text).forEach(token -> addDocument(token, currentIndex, filePath));
            currentIndex++;
            return true;
        }
        return false;
    }

    @Override
    public final IPreprocessor getPreprocessor() {
        return preprocessor;
    }
}
