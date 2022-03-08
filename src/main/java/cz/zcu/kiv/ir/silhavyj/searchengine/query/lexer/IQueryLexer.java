package cz.zcu.kiv.ir.silhavyj.searchengine.query.lexer;

public interface IQueryLexer {

    void tokenize(final String text) throws IllegalArgumentException;
    boolean hasNextToken();
    QueryLexerToken getNextToken();
    void resetToFirst();
}
