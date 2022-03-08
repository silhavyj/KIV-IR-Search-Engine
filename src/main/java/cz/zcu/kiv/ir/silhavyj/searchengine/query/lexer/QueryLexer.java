package cz.zcu.kiv.ir.silhavyj.searchengine.query.lexer;

import java.util.*;

public class QueryLexer implements IQueryLexer {

    private static final char LEFT_PARENTHESES  = '(';
    private static final char RIGHT_PARENTHESES = ')';
    private static final char COMMA             = ',';
    private static final char OR_OPERATOR       = '|';
    private static final char AND_OPERATOR      = '&';
    private static final char NOT_OPERATOR      = '!';

    public static final char QUERY_SURROUNDING_CHARACTER = '%';

    private static final char ESCAPE_CHARACTER  = '\\';

    private List<QueryLexerToken> tokens = null;
    private ListIterator<QueryLexerToken> currentToken = null;

    private static final Map<Character, QueryLexerTokenType> specialSymbols = new HashMap<>();

    static {
        specialSymbols.put(LEFT_PARENTHESES,  QueryLexerTokenType.LEFT_PARENTHESES);
        specialSymbols.put(RIGHT_PARENTHESES, QueryLexerTokenType.RIGHT_PARENTHESES);
        specialSymbols.put(COMMA,             QueryLexerTokenType.COMMA);
        specialSymbols.put(OR_OPERATOR,       QueryLexerTokenType.OR_OPERATOR);
        specialSymbols.put(AND_OPERATOR,      QueryLexerTokenType.AND_OPERATOR);
        specialSymbols.put(NOT_OPERATOR,      QueryLexerTokenType.NOT_OPERATOR);
        specialSymbols.put(QUERY_SURROUNDING_CHARACTER,        QueryLexerTokenType.PERCENTAGE);
    }

    private boolean isIdentifierCharacter(char c) {
        return (!specialSymbols.containsKey(c) && !Character.isWhitespace(c)) || c == ESCAPE_CHARACTER;
    }

    @Override
    public void tokenize(String text) throws IllegalArgumentException {
        if (text == null || text.isEmpty()) {
            throw new IllegalArgumentException("Input text cannot be null or empty");
        }
        tokens = new LinkedList<>();
        for (int i = 0; i < text.length(); i++) {
            if (Character.isWhitespace(text.charAt(i))) {
                continue;
            }
            if (specialSymbols.containsKey(text.charAt(i))) {
                tokens.add(new QueryLexerToken(specialSymbols.get(text.charAt(i)), "" + text.charAt(i)));
            } else {
                final StringBuilder stringBuilder = new StringBuilder();
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
                    tokens.add(new QueryLexerToken(QueryLexerTokenType.IDENTIFIER, stringBuilder.toString()));
                }
                i--;
            }
        }
        resetToFirst();
    }

    @Override
    public boolean hasNextToken() {
        return currentToken.hasNext();
    }

    @Override
    public QueryLexerToken getNextToken() {
        return currentToken.next();
    }

    @Override
    public void resetToFirst() {
        currentToken = tokens.listIterator();
    }
}
