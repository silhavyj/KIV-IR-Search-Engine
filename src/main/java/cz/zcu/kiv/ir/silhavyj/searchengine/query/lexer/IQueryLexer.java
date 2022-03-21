package cz.zcu.kiv.ir.silhavyj.searchengine.query.lexer;

/***
 * @author Jakub Silhavy
 *
 * Interface defining functionality of a query lexer.
 */
public interface IQueryLexer {

    /***
     * Tokenizes a query user inputs into the application
     * @param text input query
     * @throws IllegalArgumentException when the query is null or empty
     */
    void tokenize(final String text) throws IllegalArgumentException;

    /***
     * Check if there is another token that has not been retrieved.
     * @return True, if the last token has not been reached. False, otherwise.
     */
    boolean hasNextToken();

    /***
     * Retrieves the next token.
     * @return next token of the list of tokens
     */
    QueryLexerToken getNextToken();

    /***
     * Resets the iterator back to the first token.
     */
    void resetToFirst();
}
