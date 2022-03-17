package cz.zcu.kiv.ir.silhavyj.searchengine.query.parser;

import cz.zcu.kiv.ir.silhavyj.searchengine.index.Document;
import cz.zcu.kiv.ir.silhavyj.searchengine.index.IIndex;
import cz.zcu.kiv.ir.silhavyj.searchengine.index.SearchOperations;
import cz.zcu.kiv.ir.silhavyj.searchengine.query.lexer.IQueryLexer;
import cz.zcu.kiv.ir.silhavyj.searchengine.query.lexer.QueryLexerToken;

import java.util.*;

import static cz.zcu.kiv.ir.silhavyj.searchengine.query.lexer.QueryLexerTokenType.*;

public class QueryParseInfix implements IQueryParser {

    private static final String DEFAULT_ERR_MESSAGE = "Error has occurred";

    private final IQueryLexer lexer;
    private String errorMessage;
    private List<QueryLexerToken> postfixNotation;

    public QueryParseInfix(final IQueryLexer lexer) {
        this.lexer = lexer;
    }

    private int getPriority(QueryLexerToken token) {
        switch (token.getType()) {
            case AND_OPERATOR:
            case OR_OPERATOR:
                return 1;
            case NOT_OPERATOR:
                return 2;
            default:
                return -1;
        }
    }

    final List<QueryLexerToken> getPostfixNotation() throws IllegalArgumentException {
        final List<QueryLexerToken> result = new LinkedList<>();
        final Stack<QueryLexerToken> stack = new Stack<>();
        QueryLexerToken token;

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
        while (!stack.isEmpty()) {
            if (stack.peek().getType() == LEFT_PARENTHESES) {
                throw new IllegalArgumentException("Invalid expression");
            }
            result.add(stack.pop());
        }
        return result;
    }

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
                    result.add(new QueryLexerToken(NOT_OPERATOR, "!"));
                } else if (stack.peek() == 0) {
                    result.add(tokens.get(i));
                }
            } else if (tokens.get(i).getType() == NOT_OPERATOR) {
                if (stack.peek() == 1) {
                    result.add(new QueryLexerToken(AND_OPERATOR, "&"));
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

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public boolean isValidQuery(String query) {
        errorMessage = DEFAULT_ERR_MESSAGE;
        try {
            lexer.tokenize(query);
            postfixNotation = getPostfixNotation();
            int numberOfOperators = 0;
            for (final var token : postfixNotation) {
                switch (token.getType()) {
                    case IDENTIFIER:
                        numberOfOperators++;
                        break;
                    case NOT_OPERATOR:
                        if (numberOfOperators == 0) {
                            return false;
                        }
                        break;
                    default:
                        if (numberOfOperators < 2) {
                            return false;
                        }
                        numberOfOperators--;
                        break;
                }
            }
            return numberOfOperators == 1;
        } catch (Exception e) {
            errorMessage = e.getMessage();
            return false;
        }
    }

    @Override
    public Document search(IIndex index, String query) throws IllegalArgumentException {
        if (!isValidQuery(query)) {
            throw new IllegalArgumentException("Query syntax error");
        }
        String term;
        final Stack<QueryParserToken> stack = new Stack<>();
        QueryParserToken operand1;
        QueryParserToken operand2;
        Document result;

        for (final var token : postfixNotation) {
            switch (token.getType()) {
                case IDENTIFIER:
                    term = token.getValue();
                    if (index.getPreprocessor() != null) {
                        term = index.getPreprocessor().preprocess(term);
                    }
                    stack.push(new QueryParserToken(IDENTIFIER, index.getDocuments(term), term));
                    break;
                case NOT_OPERATOR:
                    operand1 = stack.pop();
                    result = SearchOperations.not(operand1.getDocument(), index.getAllDocumentIndexes());
                    stack.push(new QueryParserToken(IDENTIFIER, result, null));
                    break;
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
        return stack.pop().getDocument();
    }

    @Override
    public Set<String> getRelevantWords() {
        final Set<String> words = new HashSet<>();
        QueryLexerToken token;
        final ArrayList<QueryLexerToken> modifiedTokens = new ArrayList<>();
        lexer.resetToFirst();

        while (lexer.hasNextToken()) {
            token = lexer.getNextToken();
            if (token.getType() == OR_OPERATOR) {
                modifiedTokens.add(new QueryLexerToken(AND_OPERATOR, "&"));
            } else {
                modifiedTokens.add(token);
            }
        }
        final var simplifiedTokens = simplifyExpression(modifiedTokens);
        for (int i = 0; i < simplifiedTokens.size(); i++) {
            if (simplifiedTokens.get(i).getType() == IDENTIFIER) {
                words.add(simplifiedTokens.get(i).getValue());
            } else if (simplifiedTokens.get(i).getType() == NOT_OPERATOR) {
                i++;
            }
        }
        return words;
    }
}
