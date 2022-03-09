package cz.zcu.kiv.ir.silhavyj.searchengine.query.parser;

import cz.zcu.kiv.ir.silhavyj.searchengine.index.Document;
import cz.zcu.kiv.ir.silhavyj.searchengine.index.IIndex;
import cz.zcu.kiv.ir.silhavyj.searchengine.index.SearchOperations;
import cz.zcu.kiv.ir.silhavyj.searchengine.query.lexer.IQueryLexer;
import cz.zcu.kiv.ir.silhavyj.searchengine.query.lexer.QueryLexerToken;
import cz.zcu.kiv.ir.silhavyj.searchengine.query.lexer.QueryLexerTokenType;
import javafx.util.Pair;

import java.util.*;

import static cz.zcu.kiv.ir.silhavyj.searchengine.query.lexer.QueryLexer.QUERY_SURROUNDING_CHARACTER;
import static cz.zcu.kiv.ir.silhavyj.searchengine.query.lexer.QueryLexerTokenType.*;

public class QueryParser implements IQueryParser {

    private static final int NUMBER_OF_WORKERS = 5;

    private final IQueryLexer lexer;
    private QueryLexerToken currentToken;
    private String errorMessage;
    private final Stack<Document> operands;
    private QueryLexerTokenType clauseOperator;
    private IIndex index;
    private int totalNumberOfOperations;
    private boolean clauseFinished;

    public QueryParser(IQueryLexer lexer) {
        this.lexer = lexer;
        operands = new Stack<>();
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
        if (currentToken.getType() != LEFT_PARENTHESES) {
            errorMessage = "Missing (";
            return false;
        }
        return nextQueryToken();
    }

    private boolean parseEndOfExpression() {
        if (currentToken.getType() != RIGHT_PARENTHESES) {
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
            if (currentToken.getType() == COMMA) {
                if (!nextQueryToken()) {
                    return false;
                }
                continue;
            }
            break;
        }
        return true;
    }

    private synchronized Pair<Document, Document> getTwoOperands() {
        if (operands.size() < 2) {
            return null;
        }
        final var operand1 = operands.pop();
        final var operand2 = operands.pop();
        return new Pair<>(operand1, operand2);
    }

    private synchronized void addResultOfOperation(final Document document) {
        if (document != null) {
            operands.push(document);
            totalNumberOfOperations--;
            if (totalNumberOfOperations == 0) {
                clauseFinished = true;
            }
        }
    }

    class Worker extends Thread {

        @Override
        public void run() {
            Pair<Document, Document> twoOperands;
            Document result = null;

            while (!clauseFinished) {
                twoOperands = getTwoOperands();
                if (twoOperands == null) {
                    // TODO wait
                } else {
                    switch (clauseOperator) {
                        case OR_OPERATOR:
                            result = SearchOperations.or(twoOperands.getKey(), twoOperands.getValue());
                            break;
                        case AND_OPERATOR:
                            result = SearchOperations.and(twoOperands.getKey(), twoOperands.getValue());
                            break;
                    }
                    addResultOfOperation(result);
                }
            }
        }
    }

    private void performOperation() {
        if (clauseOperator == NOT_OPERATOR) {
            var operand1 = operands.pop();
            operands.push(SearchOperations.not(operand1, index.getAllDocumentIndexes()));
            return;
        }

        Document result = null;
        Document operand1;
        Document operand2;
        while (operands.size() != 1) {
            operand1 = operands.pop();
            operand2 = operands.pop();
            switch (clauseOperator) {
                case OR_OPERATOR:
                    result = SearchOperations.or(operand1, operand2);
                    break;
                case AND_OPERATOR:
                    result = SearchOperations.and(operand1, operand2);
                    break;
            }
            operands.push(result);
        }

        // TODO
        /* clauseFinished = false;
        totalNumberOfOperations = operands.size() - 1;

        LinkedList<Worker> workers = new LinkedList<>();
        for (int i = 0; i < NUMBER_OF_WORKERS; i++) {
            workers.add(new Worker());
            workers.getLast().start();
        }
        for (final var worker : workers) {
            try {
                worker.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } */
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
            if (currentToken.getType() != PERCENTAGE) {
                errorMessage = "Missing starting symbol";
                return false;
            }
            if (!nextQueryToken()) {
                return false;
            }
            if (!processExpression()) {
                return false;
            }
            if (currentToken.getType() != PERCENTAGE) {
                errorMessage = "Missing trailing symbol";
                return false;
            }
            if (lexer.hasNextToken()) {
                errorMessage = "Query has not been fully parsed";
                return false;
            }
        } catch (IllegalArgumentException e) {
            errorMessage = e.getMessage();
            return false;
        }
        return true;
    }

    @Override
    public Document search(final IIndex index, String query) throws IllegalArgumentException {
        this.index = index;
        query = QUERY_SURROUNDING_CHARACTER + query + QUERY_SURROUNDING_CHARACTER;
        if (!isValidQuery(query)) {
            throw new IllegalArgumentException("Query syntax error");
        }

        String term;
        QueryLexerToken lexerToken;
        final Stack<QueryParserToken> stack = new Stack<>();

        lexer.resetToFirst();
        while (lexer.hasNextToken()) {
            lexerToken = lexer.getNextToken();
            switch (lexerToken.getType()) {
                case AND_OPERATOR:
                case OR_OPERATOR:
                case NOT_OPERATOR:
                case LEFT_PARENTHESES:
                    stack.push(new QueryParserToken(lexerToken.getType(), null, null));
                    break;
                case IDENTIFIER:
                    term = lexerToken.getValue();
                    if (index.getPreprocessor() != null) {
                        term = index.getPreprocessor().preprocess(term);
                    }
                    stack.push(new QueryParserToken(IDENTIFIER, index.getDocuments(term), term));
                    break;
                case RIGHT_PARENTHESES:
                    operands.empty();
                    final Set<String> seenOperands = new HashSet<>();
                    QueryParserToken stackTop;

                    while (!stack.isEmpty() && stack.peek().getType() != LEFT_PARENTHESES) {
                        stackTop = stack.pop();
                        if (!seenOperands.contains(stackTop.getTerm())) {
                            seenOperands.add(stackTop.getTerm());
                            operands.add(stackTop.getDocument());
                        }
                    }
                    stack.pop();
                    clauseOperator = stack.pop().getType();
                    performOperation();
                    stack.add(new QueryParserToken(IDENTIFIER, operands.pop(), null));
                    break;
            }
        }
        if (stack.empty()) {
            throw new IllegalArgumentException("Result is of the query empty");
        }
        return stack.pop().getDocument();
    }
}
