package cz.zcu.kiv.ir.silhavyj.searchengine.index;

public interface IIndex {

    void addDocument(final String term, int documentIndex, final String filePath);
    Document getAllDocumentIndexes();
    String getFilePath(int documentIndex) throws IllegalArgumentException;
    Document getDocuments(final String term);
}
