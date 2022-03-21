package cz.zcu.kiv.ir.silhavyj.searchengine.query.parser;

import cz.zcu.kiv.ir.silhavyj.searchengine.index.Document;
import cz.zcu.kiv.ir.silhavyj.searchengine.query.lexer.QueryLexerTokenType;

/***
 * @author Jakub Silhavy
 *
 * This class represents a parser token which is used during
 * query evaluation.
 */
public class QueryParserToken {

    /*** Token type */
    private final QueryLexerTokenType type;

    /*** List of Documents associated with the token (e.g. a lost of all documents that contain the word 'cat') */
    private final Document document;

    /** Term associated with the token (e.g. the word 'cat') */
    private final String term;

    /***
     * Creates an instance of the token.
     * @param type token type
     * @param document list of documents associated with the token
     * @param term term associated with the document
     */
    public QueryParserToken(QueryLexerTokenType type, Document document, String term) {
        this.type = type;
        this.document = document;
        this.term = term;
    }

    /***
     * Return the list of documents associated with the token.
     * @return list of documents associated with the token
     */
    public Document getDocument() {
        return document;
    }

    /***
     * Returns a string representation of the token
     * @return string representation of the token
     */
    @Override
    public String toString() {
        return "QueryParserToken{" +
                "type=" + type +
                ", document=" + document +
                ", term='" + term + '\'' +
                '}';
    }
}
