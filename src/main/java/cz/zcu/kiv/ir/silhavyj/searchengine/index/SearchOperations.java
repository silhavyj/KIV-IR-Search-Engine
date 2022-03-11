package cz.zcu.kiv.ir.silhavyj.searchengine.index;

public class SearchOperations {

    public static Document not(Document docs, Document allDocs) {
        Document previousDoc = null;
        Document currentDoc = allDocs;

        while (currentDoc != null && docs != null) {
            if (currentDoc.getIndex() < docs.getIndex()) {
                previousDoc = currentDoc;
                currentDoc = currentDoc.getNext();
            } else if (currentDoc.getIndex() > docs.getIndex()) {
                docs = docs.getNext();
            } else {
                if (previousDoc != null) {
                    previousDoc.setNext(currentDoc.getNext());
                    currentDoc = currentDoc.getNext();
                } else {
                    allDocs = allDocs.getNext();
                    currentDoc = allDocs;
                }
            }
        }
        return allDocs;
    }

    public static Document and(Document docs1, Document docs2) {
        if ((docs1 == null || docs2 == null) || (docs1.isUninitialized() && docs2.isUninitialized())) {
            return null;
        }

        Document result = null;
        Document currentDoc = null;

        while (docs1 != null && docs2 != null) {
            if (docs1.getIndex() == docs2.getIndex()) {
                if (result == null) {
                    result = new Document(docs1.getIndex());
                    currentDoc = result;
                } else {
                    currentDoc.setNext(new Document(docs1.getIndex()));
                    currentDoc = currentDoc.getNext();
                }
                docs1 = docs1.getNext();
                docs2 = docs2.getNext();
            } else if (docs1.getIndex() < docs2.getIndex()) {
                docs1 = docs1.getNext();
            } else {
                docs2 = docs2.getNext();
            }
        }
        return result;
    }

    public static Document or(Document docs1, Document docs2) {
        if (docs1 == null && docs2 == null) {
            return null;
        }
        if (docs1 == null) {
            return docs2;
        }
        if (docs2 == null) {
            return docs1;
        }
        if (docs1.isUninitialized()) {
            return docs2;
        }
        if (docs2.isUninitialized()) {
            return docs1;
        }

        Document currentDoc;
        if (docs1.getIndex() < docs2.getIndex()) {
            currentDoc = new Document(docs1.getIndex());
            docs1 = docs1.getNext();
        } else {
            currentDoc = new Document(docs2.getIndex());
            docs2 = docs2.getNext();
        }

        Document result = currentDoc;
        while (docs1 != null && docs2 != null) {
            if (docs1.getIndex() < docs2.getIndex()) {
                if (docs1.getIndex() != currentDoc.getIndex()) {
                    currentDoc.setNext(new Document(docs1.getIndex()));
                }
                docs1 = docs1.getNext();
            } else {
                if (docs2.getIndex() != currentDoc.getIndex()) {
                    currentDoc.setNext(new Document(docs2.getIndex()));
                }
                docs2 = docs2.getNext();
            }
            if (currentDoc.getNext() != null) {
                currentDoc = currentDoc.getNext();
            }
        }
        while (docs1 != null) {
            if (docs1.getIndex() != currentDoc.getIndex()) {
                currentDoc.setNext(new Document(docs1.getIndex()));
                currentDoc = currentDoc.getNext();
            }
            docs1 = docs1.getNext();
        }
        while (docs2 != null) {
            if (docs2.getIndex() != currentDoc.getIndex()) {
                currentDoc.setNext(new Document(docs2.getIndex()));
                currentDoc = currentDoc.getNext();
            }
            docs2 = docs2.getNext();
        }
        return result;
    }
}
