package cz.zcu.kiv.ir.silhavyj.searchengine.index;

/***
 * @author Jakub Silhavy
 *
 * This class is a utility class that provides function for
 * performing AND, OR, and NOT operations on two lists of Documents.
 * Each of the two operands represents one record (lineked list)
 * in an inverted index.
 */
public class SearchOperations {

    /***
     * Calculates NOT operation of a given chained list of Documents.
     * Essentially, the NOT operator represents a complement within the
     * set of all documents. The input linked lists are assumed to be sorted
     * prior to calling this method.
     * @param docs linked list of document (input operand, e.g !cat)
     * @param allDocs linked list of all documents that have been indexed.
     * @return Out of all documents in the index, it will return those which
     *         not appear in the original one (complement).
     */
    public static Document not(Document docs, Document allDocs) {
        Document previousDoc = null;
        Document currentDoc = allDocs;

        // Go through all documents that have been indexed and
        // remove those which in 'docs'.
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

    /***
     * Calculates AND operation between dow linked lists of Documents.
     * The input linked lists are assumed to be sorted prior to calling this method.
     * @param docs1 fist sorted list of Documents
     * @param docs2 second sorted list of Documents
     * @return intersection of the two lists of Documents
     */
    public static Document and(Document docs1, Document docs2) {
        // Make sure both lists are not empty.
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

    /***
     * Calculates OR operation between dow linked lists of Documents.
     * The input linked lists are assumed to be sorted prior to calling this method.
     * @param docs1 fist sorted list of Documents
     * @param docs2 second sorted list of Documents
     * @return union of the two lists of Documents (sorted as well)
     */
    public static Document or(Document docs1, Document docs2) {
        // If both lists are null, there's nothing to calculate.
        if (docs1 == null && docs2 == null) {
            return null;
        }

        // If the first list is empty, then the result is the second one.
        // If the second list is empty, then the result is the first one.
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

        // First step, so we can be sure that currentDoc
        // is initialized.
        Document currentDoc;
        if (docs1.getIndex() < docs2.getIndex()) {
            currentDoc = new Document(docs1.getIndex());
            docs1 = docs1.getNext();
        } else {
            currentDoc = new Document(docs2.getIndex());
            docs2 = docs2.getNext();
        }


        // Keep on iterating until you reach the end of
        // one of the lists.
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

        // Append what's left from list 1.
        while (docs1 != null) {
            if (docs1.getIndex() != currentDoc.getIndex()) {
                currentDoc.setNext(new Document(docs1.getIndex()));
                currentDoc = currentDoc.getNext();
            }
            docs1 = docs1.getNext();
        }

        // Append what's left from list 2.
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
