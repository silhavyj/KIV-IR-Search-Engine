package cz.zcu.kiv.ir.silhavyj.searchengine.index;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class Index {

    private final Map<String, DocumentList> invertedIndex;
    private final Set<Integer> allDocumentIndexes;
    private final Map<Integer, String> filePaths;

    public Index() {
        invertedIndex = new HashMap<>();
        allDocumentIndexes = new TreeSet<>();
        filePaths = new HashMap<>();
    }

    public void addDocument(final String term, int documentIndex, final String filePath) {
        Document document = new Document(documentIndex);
        if (!invertedIndex.containsKey(term)) {
            invertedIndex.put(term, new DocumentList(term));
        }
        if (!filePaths.containsKey(documentIndex)) {
            filePaths.put(documentIndex, filePath);
        }
        final var documentList = invertedIndex.get(term);
        documentList.add(document);
        allDocumentIndexes.add(documentIndex);
    }

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
}
