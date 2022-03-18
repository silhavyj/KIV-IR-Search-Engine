package query;

import cz.zcu.kiv.ir.silhavyj.searchengine.query.lexer.QueryLexer;
import cz.zcu.kiv.ir.silhavyj.searchengine.query.parser.IQueryParser;
import cz.zcu.kiv.ir.silhavyj.searchengine.query.parser.QueryParseInfix;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertFalse;

public class InvalidQueryTest {

    private static IQueryParser queryParser;

    @BeforeClass
    public static void setUpBeforeClass() {
        queryParser = new QueryParseInfix(new QueryLexer());
    }

    @Test
    public void testQuery_01() {
        assertFalse(queryParser.isValidQuery("!(test*123"));
    }

    @Test
    public void testQuery_02() {
        assertFalse(queryParser.isValidQuery("| a | b |"));
    }

    @Test
    public void testQuery_03() {
        assertFalse(queryParser.isValidQuery("| a | b )"));
    }

    @Test
    public void testQuery_04() {
        assertFalse(queryParser.isValidQuery(")a("));
    }

    @Test
    public void testQuery_05() {
        assertFalse(queryParser.isValidQuery("15 & 14 | !"));
    }

    @Test
    public void testQuery_06() {
        assertFalse(queryParser.isValidQuery("| & !"));
    }

    @Test
    public void testQuery_07() {
        assertFalse(queryParser.isValidQuery(null));
    }

    @Test
    public void testQuery_08() {
        assertFalse(queryParser.isValidQuery(""));
    }
}
