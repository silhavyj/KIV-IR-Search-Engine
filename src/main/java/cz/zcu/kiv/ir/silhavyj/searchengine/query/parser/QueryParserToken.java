package cz.zcu.kiv.ir.silhavyj.searchengine.query.parser;

import cz.zcu.kiv.ir.silhavyj.searchengine.index.Document;
import cz.zcu.kiv.ir.silhavyj.searchengine.query.lexer.QueryLexerTokenType;

public class QueryParserToken {

    private final QueryLexerTokenType type;
    private final Document document;
    private final String term;

    public QueryParserToken(QueryLexerTokenType type, Document document, String term) {
        this.type = type;
        this.document = document;
        this.term = term;
    }

    public QueryLexerTokenType getType() {
        return type;
    }

    public Document getDocument() {
        return document;
    }

    public String getTerm() {
        return term;
    }

    @Override
    public String toString() {
        return "QueryParserToken{" +
                "type=" + type +
                ", document=" + document +
                ", term='" + term + '\'' +
                '}';
    }
}
