package utils;

import cz.zcu.kiv.ir.silhavyj.searchengine.index.Document;

import java.util.List;

public class DocumentUtils {

    public static Document createDocumentIndex(final List<Integer> indexes) {
        Document documentIndex = null;
        Document head = null;
        for (final var index : indexes) {
            if (documentIndex == null) {
                documentIndex = new Document(index);
                head = documentIndex;
            } else {
                documentIndex.setNext(new Document(index));
                documentIndex = documentIndex.getNext();
            }
        }
        return head;
    }
}
