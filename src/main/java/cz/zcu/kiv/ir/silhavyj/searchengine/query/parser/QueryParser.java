package cz.zcu.kiv.ir.silhavyj.searchengine.query.parser;

import cz.zcu.kiv.ir.silhavyj.searchengine.query.lexer.IQueryLexer;
import cz.zcu.kiv.ir.silhavyj.searchengine.query.lexer.QueryLexerToken;
import cz.zcu.kiv.ir.silhavyj.searchengine.query.lexer.QueryLexerTokenType;

public class QueryParser implements IQueryParser {

    private final IQueryLexer lexer;

    private QueryLexerToken currentToken;
    private String errorMessage;

    public QueryParser(IQueryLexer lexer) {
        this.lexer = lexer;
        errorMessage = "";
    }

    private boolean nextQueryToken() {
        if (lexer != null && lexer.hasNextToken()) {
            currentToken = lexer.getNextToken();
            return true;
        }
        errorMessage = "Query is incomplete";
        return false;
    }

    private boolean processExpression() {
        switch (currentToken.getType()) {
            case NOT_OPERATOR:
                return processNot();
            case AND_OPERATOR:
            case OR_OPERATOR:
                return processAndOr();
            case IDENTIFIER:
                return nextQueryToken();
            default:
                errorMessage = "Invalid clause";
                return false;
        }
    }

    private boolean parseBeginningOfExpression() {
        if (!nextQueryToken()) {
            return false;
        }
        if (currentToken.getType() != QueryLexerTokenType.LEFT_PARENTHESES) {
            errorMessage = "Missing (";
            return false;
        }
        return nextQueryToken();
    }

    private boolean parseEndOfExpression() {
        if (currentToken.getType() != QueryLexerTokenType.RIGHT_PARENTHESES) {
            errorMessage = "Missing )";
            return false;
        }
        return nextQueryToken();
    }

    private boolean processNot() {
        if (!parseBeginningOfExpression()) {
            return false;
        }
        if (!processExpression()) {
            return false;
        }
        return parseEndOfExpression();
    }

    private boolean processAndOr() {
        if (!parseBeginningOfExpression()) {
            return false;
        }
        if (!processArguments()) {
            return false;
        }
        return parseEndOfExpression();
    }

    private boolean processArguments() {
        while (true) {
            if (!processExpression()) {
                return false;
            }
            if (currentToken.getType() == QueryLexerTokenType.COMMA) {
                if (!nextQueryToken()) {
                    return false;
                }
                continue;
            }
            break;
        }
        return true;
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public boolean isValidQuery(final String query) {
        try {
            lexer.tokenize(query);
            if (!nextQueryToken()) {
                return false;
            }
            if (currentToken.getType() != QueryLexerTokenType.PERCENTAGE) {
                errorMessage = "Missing starting symbol";
                return false;
            }
            if (!nextQueryToken()) {
                return false;
            }
            if (!processExpression()) {
                return false;
            }
            if (currentToken.getType() != QueryLexerTokenType.PERCENTAGE) {
                errorMessage = "Missing trailing symbol";
                return false;
            }
            if (lexer.hasNextToken()) {
                errorMessage = "Query has not been fully parsed";
                return false;
            }
        } catch (IllegalArgumentException ex) {
            errorMessage = ex.getMessage();
            return false;
        }
        return true;
    }
}
