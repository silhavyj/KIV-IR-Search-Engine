package cz.zcu.kiv.ir.silhavyj.searchengine.query.lexer;

public class QueryLexerToken {

    private final QueryLexerTokenType type;
    private final String value;

    public QueryLexerToken(QueryLexerTokenType type, String value) {
        this.type = type;
        this.value = value;
    }

    public QueryLexerTokenType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "QueryLexerToken{" +
                "type=" + type +
                ", value='" + value + '\'' +
                '}';
    }
}
