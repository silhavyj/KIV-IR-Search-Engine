package cz.zcu.kiv.ir.silhavyj.searchengine.index;

public class Document {

    public static final int UNINITIALIZED = -1;
    public static final String UNDEFINED_PATH = "";

    private final int index;
    private int wordCount;
    private Document next;

    public Document() {
        this(UNINITIALIZED);
    }

    public Document(int index) {
        this(index, null);
    }

    public Document(int index, final Document next) {
        this.index = index;
        this.next = next;
        wordCount = 0;
    }

    public int getWordCount() {
        return wordCount;
    }

    public void increaseWordCount() {
        this.wordCount += 1;
    }

    public int getIndex() {
        return index;
    }

    public Document getNext() {
        return next;
    }

    public void setNext(Document next) {
        this.next = next;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Document)) {
            return false;
        }

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

    @Override
    public String toString() {
        final StringBuilder stringBuilder = new StringBuilder();
        Document currentDoc = this;
        while (currentDoc != null) {
            stringBuilder.append(currentDoc.getIndex()).append(" ");
            currentDoc = currentDoc.getNext();
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        return "[" + stringBuilder + "]";
    }
}
