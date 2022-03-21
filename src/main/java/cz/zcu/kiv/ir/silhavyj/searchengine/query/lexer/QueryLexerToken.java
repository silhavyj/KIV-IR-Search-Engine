package cz.zcu.kiv.ir.silhavyj.searchengine.query.lexer;

/***
 * @author Jakub Silhavy
 *
 * This class represents a lexer token.
 * It holds the type of the token as well as its value.
 */
public class QueryLexerToken {

    /*** Type of the token */
    private final QueryLexerTokenType type;

    /*** Token value (e.g. name of an identifier) */
    private final String value;

    /***
     * Creates an instance of the class.
     * @param type token type
     * @param value token value
     */
    public QueryLexerToken(QueryLexerTokenType type, String value) {
        this.type = type;
        this.value = value;
    }

    /***
     * Returns the type of the token.
     * @return type of the token
     */
    public QueryLexerTokenType getType() {
        return type;
    }

    /***
     * Return the value of the token.
     * @return value of the token
     */
    public String getValue() {
        return value;
    }

    /***
     * Returns a string representation of the token.
     * @return string representation of the token
     */
    @Override
    public String toString() {
        return "QueryLexerToken{" +
                "type=" + type +
                ", value='" + value + '\'' +
                '}';
    }
}
