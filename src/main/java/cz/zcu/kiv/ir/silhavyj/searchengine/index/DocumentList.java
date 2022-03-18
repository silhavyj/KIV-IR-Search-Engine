package cz.zcu.kiv.ir.silhavyj.searchengine.index;

/***
 * @author Jakub Silhavy
 *
 * This class represents a "row" in an inverted index.
 * It's a list of chained documents that contain a certain word.
 */
public class DocumentList {

    /*** Head (first element) of the linked list */
    private Document first;

    /*** Tail (last element) of the linked list */
    private Document last;

    /*** Number of document that make up the linked list */
    private int count;

    /***
     * Creates an instance of the class.
     */
    public DocumentList() {
        count = 0;
        first = null;
        last = null;
    }

    /***
     * Returns the first element of the linked list.
     * @return first element of the linked list.
     */
    public Document getFirst() {
        return first;
    }

    /***
     * Returns the number of documents stored in the linked list.
     * @return number of documents making up the linked list
     */
    public int getCount() {
        return count;
    }

    /***
     * Adds a document into the linked list.
     * @param document instance of Document to be added to the linked list
     */
    public void add(final Document document) {
        // If the linked list is empty, first = last = document.
        // Otherwise, make sure that the document has not been added yet.
        if (first == null) {
            first = document;
            last = first;
            count++;
        } else if (document.getIndex() != last.getIndex()) {
            last.setNext(document);
            last = document;
            count++;
        }
    }
}
