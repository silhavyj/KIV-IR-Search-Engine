package cz.zcu.kiv.ir.silhavyj.searchengine.index;

import cz.zcu.kiv.ir.silhavyj.searchengine.preprocessing.IPreprocessor;
import javafx.beans.property.IntegerProperty;

public interface IIndex {

    void addDocument(final String term, int documentIndex, final String filePath);
    Document getAllDocumentIndexes();
    String getFilePath(int documentIndex) throws IllegalArgumentException;
    Document getDocuments(final String term);
    boolean index(final String text, final String filePath);
    IPreprocessor getPreprocessor();
    int getDocumentCount();
    IntegerProperty documentCountProperty();
    int getTermCount();
    IntegerProperty termCountProperty();
    int getTokenCount();
    IntegerProperty tokenCountProperty();
}
