package cz.zcu.kiv.ir.silhavyj.searchengine.query.lexer;

import java.util.*;

/***
 * @author Jakub Silhavy
 *
 * This class works as a lexer of a query that the
 * user inputs into the application. Its purpose
 * is to tokenize a raw string. The tokens will be then
 * used by the parser to evaluate the query.
 */
public class QueryLexer implements IQueryLexer {

    /*** Opening parenthesis */
    private static final char LEFT_PARENTHESES  = '(';

    /*** Closing parenthesis */
    private static final char RIGHT_PARENTHESES = ')';

    /*** OR operator */
    private static final char OR_OPERATOR       = '|';

    /*** AND operator */
    private static final char AND_OPERATOR      = '&';

    /*** NOT operator */
    private static final char NOT_OPERATOR      = '!';

    /*** Escape character (it can be used t escape the predefined symbols) */
    private static final char ESCAPE_CHARACTER  = '\\';

    /*** List of tokens found in the input string */
    private List<QueryLexerToken> tokens = null;

    /*** Token list iterator. It is used by the parser (stream of tokens) */
    private ListIterator<QueryLexerToken> currentToken = null;

    /*** Map containing special predefined symbols. */
    private static final Map<Character, QueryLexerTokenType> specialSymbols = new HashMap<>();

    // Initialize the map of special symbols
    static {
        specialSymbols.put(LEFT_PARENTHESES,  QueryLexerTokenType.LEFT_PARENTHESES);
        specialSymbols.put(RIGHT_PARENTHESES, QueryLexerTokenType.RIGHT_PARENTHESES);
        specialSymbols.put(OR_OPERATOR,       QueryLexerTokenType.OR_OPERATOR);
        specialSymbols.put(AND_OPERATOR,      QueryLexerTokenType.AND_OPERATOR);
        specialSymbols.put(NOT_OPERATOR,      QueryLexerTokenType.NOT_OPERATOR);
    }

    /***
     * Checks if the character passed as a parameter could be a part of an identifier.
     * @param c examined character
     * @return True, if the character is not a special symbol not a white symbol. False, otherwise.
     */
    private boolean isIdentifierCharacter(char c) {
        return (!specialSymbols.containsKey(c) && !Character.isWhitespace(c)) || c == ESCAPE_CHARACTER;
    }

    /***
     * Tokenizes a query user inputs into the application
     * @param text input query
     * @throws IllegalArgumentException when the query is null or empty
     */
    @Override
    public void tokenize(String text) throws IllegalArgumentException {
        // Make sure the query is not an empty string.
        if (text == null || text.isEmpty()) {
            throw new IllegalArgumentException("Input text cannot be null or empty");
        }

        // Initialize a new list of tokens.
        tokens = new LinkedList<>();

        // Iterate through the query character by character.
        for (int i = 0; i < text.length(); i++) {
            // Skip white spaces.
            if (Character.isWhitespace(text.charAt(i))) {
                continue;
            }
            // Check if the current character is a special symbol ('&', '|', ...).
            if (specialSymbols.containsKey(text.charAt(i))) {
                tokens.add(new QueryLexerToken(specialSymbols.get(text.charAt(i)), "" + text.charAt(i)));
            } else {
                final StringBuilder stringBuilder = new StringBuilder();

                // Collect all characters making up an identifier.
                while (i < text.length() && isIdentifierCharacter(text.charAt(i))) {
                    if (text.charAt(i) == ESCAPE_CHARACTER) {
                        i++;
                        if (i == text.length()) {
                            break;
                        }
                    }
                    stringBuilder.append(text.charAt(i));
                    i++;
                }
                if (stringBuilder.length() > 0) {
                    // If there are two identifiers (words) not separated by '&' or '|',
                    // insert '&' in between them (implicit concatenation).
                    if (tokens.size() >= 1 && tokens.get(tokens.size() - 1).getType() == QueryLexerTokenType.IDENTIFIER) {
                        tokens.add(new QueryLexerToken(QueryLexerTokenType.AND_OPERATOR, "&"));
                    }
                    // Append the token.
                    tokens.add(new QueryLexerToken(QueryLexerTokenType.IDENTIFIER, stringBuilder.toString()));
                }
                i--;
            }
        }
        // Set the iterator to the first token of the list of tokens.
        resetToFirst();
    }

    /***
     * Check if there is another token that has not been retrieved.
     * @return True, if the last token has not been reached. False, otherwise.
     */
    @Override
    public boolean hasNextToken() {
        return currentToken.hasNext();
    }

    /***
     * Retrieves the next token.
     * @return next token of the list of tokens
     */
    @Override
    public QueryLexerToken getNextToken() {
        return currentToken.next();
    }

    /***
     * Resets the iterator back to the first token.
     */
    @Override
    public void resetToFirst() {
        currentToken = tokens.listIterator();
    }
}
