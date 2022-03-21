package cz.zcu.kiv.ir.silhavyj.searchengine.query.parser;

import cz.zcu.kiv.ir.silhavyj.searchengine.index.Document;
import cz.zcu.kiv.ir.silhavyj.searchengine.index.IIndex;
import cz.zcu.kiv.ir.silhavyj.searchengine.index.SearchOperations;
import cz.zcu.kiv.ir.silhavyj.searchengine.query.lexer.IQueryLexer;
import cz.zcu.kiv.ir.silhavyj.searchengine.query.lexer.QueryLexerToken;

import java.util.*;

import static cz.zcu.kiv.ir.silhavyj.searchengine.query.lexer.QueryLexerTokenType.*;

/***
 * @author Jakub Silhavy
 *
 * This class represents a query parser. It evaluates a query
 * when the user searches for documents.
 */
public class QueryParseInfix implements IQueryParser {

    /*** Default error message */
    private static final String DEFAULT_ERR_MESSAGE = "Error has occurred";

    /*** AND operator */
    private static final String BRACKET_REMOVAL_AND_OPERATOR = "&";

    /*** NOT operator */
    private static final String BRACKET_REMOVAL_NOT_OPERATOR = "!";

    /*** Instance of a query lexer (used for query tokenization) */
    private final IQueryLexer lexer;

    /*** Error message */
    private String errorMessage;

    /*** Query written as a list of tokens in a postfix notation */
    private List<QueryLexerToken> postfixNotation;

    /***
     * Creates an instance of the class.
     * @param lexer instance of a query lexer
     */
    public QueryParseInfix(final IQueryLexer lexer) {
        this.lexer = lexer;
    }

    /***
     * Returns the priority of an operator.
     * This method is used when converting a query from an infix
     * notation to a postfix notation.
     * @param token operator
     * @return the priority of the operator
     */
    private int getPriority(QueryLexerToken token) {
        switch (token.getType()) {
            case AND_OPERATOR: // AND and OR have the same priority
            case OR_OPERATOR:
                return 1;
            case NOT_OPERATOR: // NOT has the highest priority
                return 2;
            default:
                return -1;     // Everything else has a priority of -1 (not an operator)
        }
    }

    /***
     * Converts a query written in an infix notation into a postfix notation.
     * The query is retrieved through the query parser.
     * https://www.geeksforgeeks.org/stack-set-2-infix-to-postfix/
     * @return List of tokens which represent the query written in a postfix notation
     * @throws IllegalArgumentException if the user enters an invalid expression
     */
    final List<QueryLexerToken> getPostfixNotation() throws IllegalArgumentException {
        final List<QueryLexerToken> result = new LinkedList<>();
        final Stack<QueryLexerToken> stack = new Stack<>();
        QueryLexerToken token;

        // Iterate through all tokens given by the lexer.
        while (lexer.hasNextToken()) {
            token = lexer.getNextToken();
            switch (token.getType()) {
                case IDENTIFIER:
                    result.add(token);
                    break;
                case LEFT_PARENTHESES:
                    stack.push(token);
                    break;
                case RIGHT_PARENTHESES:
                    while (!stack.isEmpty() && stack.peek().getType() != LEFT_PARENTHESES) {
                        result.add(stack.pop());
                    }
                    if (result.isEmpty()) {
                        throw new IllegalArgumentException("Invalid expression");
                    }
                    stack.pop();
                    break;
                default:
                    while (!stack.isEmpty() && getPriority(token) < getPriority(stack.peek())) {
                        result.add(stack.pop());
                    }
                    stack.push(token);
                    break;
            }
        }
        // Pop what's left off of the stack.
        while (!stack.isEmpty()) {
            if (stack.peek().getType() == LEFT_PARENTHESES) {
                throw new IllegalArgumentException("Invalid expression");
            }
            result.add(stack.pop());
        }
        return result;
    }

    /***
     * Simplifies an expression written in an infix notation.
     * This method is used when finding relevant words in a query.
     * Essentially, it does nothing but gets reid of all parentheses.
     * https://www.geeksforgeeks.org/remove-brackets-algebraic-string-containing-operators/
     * @param tokens list of tokens written in an infix notation
     * @return simplified expression (with no parentheses)
     */
    private ArrayList<QueryLexerToken> simplifyExpression(ArrayList<QueryLexerToken> tokens) {
        final ArrayList<QueryLexerToken> result = new ArrayList<>();
        final Stack<Integer> stack = new Stack<>();
        int i = 0;

        stack.push(0);
        while (i < tokens.size()) {
            if (tokens.get(i).getType() == LEFT_PARENTHESES && i == 0) {
                i++;
                continue;
            }
            if (tokens.get(i).getType() == AND_OPERATOR) {
                if (stack.peek() == 1) {
                    result.add(new QueryLexerToken(NOT_OPERATOR, BRACKET_REMOVAL_NOT_OPERATOR));
                } else if (stack.peek() == 0) {
                    result.add(tokens.get(i));
                }
            } else if (tokens.get(i).getType() == NOT_OPERATOR) {
                if (stack.peek() == 1) {
                    result.add(new QueryLexerToken(AND_OPERATOR, BRACKET_REMOVAL_AND_OPERATOR));
                } else {
                    result.add(tokens.get(i));
                }
            } else if (tokens.get(i).getType() == LEFT_PARENTHESES && i > 0) {
                if (tokens.get(i - 1).getType() == NOT_OPERATOR) {
                    int inverted = (stack.peek() == 1) ? 0 : 1;
                    stack.push(inverted);
                } else if (tokens.get(i - 1).getType() == AND_OPERATOR) {
                    stack.push(stack.peek());
                }
            } else if (tokens.get(i).getType() == RIGHT_PARENTHESES) {
                if (stack.size() > 1) {
                    stack.pop();
                }
            } else {
                result.add(tokens.get(i));
            }
            i++;
        }
        return result;
    }

    /***
     * Returns the error message.
     * @return the error message
     */
    @Override
    public String getErrorMessage() {
        return errorMessage;
    }

    /***
     * Checks if the query passed in as a string is valid or not.
     * @param query query written in an infix notation
     * @return True, if the query is valid. False, otherwise.
     */
    @Override
    public boolean isValidQuery(String query) {
        // Set the default error message.
        errorMessage = DEFAULT_ERR_MESSAGE;

        try {
            // Tokenize the query.
            lexer.tokenize(query);

            // Convert the query into a postfix notation
            postfixNotation = getPostfixNotation();
            int numberOfOperators = 0;

            // Iterate over all tokens and check if all operations
            // have the expected number of operands.
            for (final var token : postfixNotation) {
                switch (token.getType()) {
                    // Increase the number of operands
                    case IDENTIFIER:
                        numberOfOperators++;
                        break;

                    // Make sure that there is at least one operand
                    // for the NOT operation.
                    case NOT_OPERATOR:
                        if (numberOfOperators == 0) {
                            return false;
                        }
                        break;
                    // Make sure there are at least wro operands
                    // for the AND or OR operation. The total number
                    // of operands then decreases by one.
                    default:
                        if (numberOfOperators < 2) {
                            return false;
                        }
                        numberOfOperators--;
                        break;
                }
            }
            // There must be exactly one operand left (the result)
            return numberOfOperators == 1;
        } catch (Exception e) {
            errorMessage = e.getMessage();
            return false;
        }
    }

    /***
     * Performs a search
     * @param index index in which the search will be performed
     * @param query query the user entered (string, infix notation)
     * @return Linked list of Documents representing the result of the search.
     * @throws IllegalArgumentException if the user entered an invalid query
     */
    @Override
    public Document search(IIndex index, String query) throws IllegalArgumentException {
        // Make sure the entered query is valid.
        if (!isValidQuery(query)) {
            throw new IllegalArgumentException("Query syntax error");
        }
        String term;
        final Stack<QueryParserToken> stack = new Stack<>();
        QueryParserToken operand1;
        QueryParserToken operand2;
        Document result;

        // Iterate through the query represented as a list
        // of tokens in a postfix notation and evaluate the
        // query using a stack.
        for (final var token : postfixNotation) {
            switch (token.getType()) {
                // Store the identifier on the stack.
                case IDENTIFIER:
                    term = token.getValue();
                    if (index.getPreprocessor() != null) {
                        term = index.getPreprocessor().preprocess(term);
                    }
                    stack.push(new QueryParserToken(IDENTIFIER, index.getDocuments(term), term));
                    break;
                // Perform NOT.
                case NOT_OPERATOR:
                    operand1 = stack.pop();
                    result = SearchOperations.not(operand1.getDocument(), index.getAllDocumentIndexes());
                    stack.push(new QueryParserToken(IDENTIFIER, result, null));
                    break;
                // Perform AND, OR.
                default:
                    operand1 = stack.pop();
                    operand2 = stack.pop();
                    if (token.getType() == OR_OPERATOR) {
                        result = SearchOperations.or(operand1.getDocument(), operand2.getDocument());
                    } else {
                        result = SearchOperations.and(operand1.getDocument(), operand2.getDocument());
                    }
                    stack.push(new QueryParserToken(IDENTIFIER, result, null));
                    break;
            }
        }
        // Return the result of the search.
        return stack.pop().getDocument();
    }

    /***
     * Returns relevant words of a query
     * @return relevant words of a query as a Set (uniqueness)
     */
    @Override
    public Set<String> getRelevantWords() {
        QueryLexerToken token;
        final Set<String> words = new HashSet<>();
        final ArrayList<QueryLexerToken> modifiedTokens = new ArrayList<>();

        // Reset the lexer back to the first token.
        lexer.resetToFirst();

        // Replace all OR operator with AND operators.
        while (lexer.hasNextToken()) {
            token = lexer.getNextToken();
            if (token.getType() == OR_OPERATOR) {
                modifiedTokens.add(new QueryLexerToken(AND_OPERATOR, BRACKET_REMOVAL_AND_OPERATOR));
            } else {
                modifiedTokens.add(token);
            }
        }

        // Simplify the expression (remove all parentheses).
        final var simplifiedTokens = simplifyExpression(modifiedTokens);

        // Iterate through all tokens and collect those that
        // are not prefixed with the NOT operator.
        for (int i = 0; i < simplifiedTokens.size(); i++) {
            if (simplifiedTokens.get(i).getType() == IDENTIFIER) {
                words.add(simplifiedTokens.get(i).getValue());
            } else if (simplifiedTokens.get(i).getType() == NOT_OPERATOR) {
                i++;
            }
        }
        // Return the relevant words.
        return words;
    }
}
