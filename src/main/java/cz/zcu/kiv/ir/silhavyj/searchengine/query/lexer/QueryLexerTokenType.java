package cz.zcu.kiv.ir.silhavyj.searchengine.query.lexer;

/***
 * @author Jakub Silhavy
 *
 * This enumeration represtns different special
 * symbol that may occur within a query.
 */
public enum QueryLexerTokenType {

    LEFT_PARENTHESES,  // '('
    RIGHT_PARENTHESES, // ')'
    OR_OPERATOR,       // '|'
    AND_OPERATOR,      // '&'
    NOT_OPERATOR,      // '!'
    IDENTIFIER
}
