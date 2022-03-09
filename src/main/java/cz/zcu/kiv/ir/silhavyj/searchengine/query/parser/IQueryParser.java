package cz.zcu.kiv.ir.silhavyj.searchengine.query.parser;

import cz.zcu.kiv.ir.silhavyj.searchengine.index.Document;
import cz.zcu.kiv.ir.silhavyj.searchengine.index.IIndex;

public interface IQueryParser {

    String getErrorMessage();
    boolean isValidQuery(final String query);
    Document search(final IIndex index, String query) throws IllegalArgumentException;
}