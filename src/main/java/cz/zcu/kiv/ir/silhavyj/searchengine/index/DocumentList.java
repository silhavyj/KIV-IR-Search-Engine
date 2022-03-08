package cz.zcu.kiv.ir.silhavyj.searchengine.index;

public class DocumentList {

    private Document first;
    private Document last;
    private int count;
    private final String word;

    public DocumentList(final String word) {
        this.word = word;
        count = 0;
        first = null;
        last = null;
    }

    public Document getFirst() {
        return first;
    }

    public int getCount() {
        return count;
    }

    public void add(final Document document) {
        if (first == null) {
            first = document;
            last = first;
        } else if (document.getIndex() == last.getIndex()) {
            last.increaseWordCount();
        } else {
            last.setNext(document);
            last = document;
        }
        count++;
    }
}
