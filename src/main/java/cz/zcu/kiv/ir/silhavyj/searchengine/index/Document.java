package cz.zcu.kiv.ir.silhavyj.searchengine.index;

/***
 * @author Jakub Silhavy
 *
 * This class represents one record stored in
 * an inverted index. It's basically one element of
 * a linked list that holds information about a particular
 * document.
 */
public class Document {

    /*** Flag that the document has not yet been initialized. */
    public static final int UNINITIALIZED = -1;

    /*** Index of the document. */
    private final int index;

    /*** Pointer to the next item in the linked list. */
    private Document next;

    /*** Creates an instance of the class. */
    public Document() {
        this(UNINITIALIZED);
    }

    /***
     * Creates an instance of the class.
     * @param index index of the document this item represents
     */
    public Document(int index) {
        this(index, null);
    }

    /***
     * Creates an instance of the class.
     * @param index index of the document this item represents
     * @param next pointer to the next item in the linked list
     */
    public Document(int index, final Document next) {
        this.index = index;
        this.next = next;
    }

    /***
     * Returns if the document has been initialized.
     * @return True, if the document has been initialized. False, otherwise.
     */
    public boolean isUninitialized() {
        return index == UNINITIALIZED;
    }

    /***
     * Returns the index of the document.
     * @return index of the document.
     */
    public int getIndex() {
        return index;
    }

    /***
     * Returns the pointer to the next element.
     * @return pointer to the next element
     */
    public Document getNext() {
        return next;
    }

    /***
     * Sets the pointer to the next element
     * @param next the next element (document) in the linked list
     */
    public void setNext(Document next) {
        this.next = next;
    }

    /***
     * Compares two instance of Document.
     * @param obj instance of an object.
     * @return True if the two documents hold the same values. False, otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        // If it's not an instance of Document, return false.
        if (!(obj instanceof Document)) {
            return false;
        }

        // Go through the rest of the chain and
        // compare the indexes of every pair of Documents.
        // If there's at least one mismatch, return false.
        Document doc1 = this;
        Document doc2 = (Document)obj;
        while (doc1 != null && doc2 != null) {
            if (doc1.getIndex() != doc2.getIndex()) {
                return false;
            }
            doc1 = doc1.getNext();
            doc2 = doc2.getNext();
        }
        return doc1 == null && doc2 == null;
    }
}
