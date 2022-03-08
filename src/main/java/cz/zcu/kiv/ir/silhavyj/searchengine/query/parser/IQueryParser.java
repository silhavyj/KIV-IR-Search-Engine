package cz.zcu.kiv.ir.silhavyj.searchengine.query.parser;

public interface IQueryParser {

    String getErrorMessage();
    boolean isValidQuery(final String query);
}
