package query.prefix;

import cz.zcu.kiv.ir.silhavyj.searchengine.query.lexer.QueryLexer;
import cz.zcu.kiv.ir.silhavyj.searchengine.query.parser.QueryParserPrefix;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertFalse;

public class InvalidQueryTest {

    private static QueryParserPrefix queryParser;

    @BeforeClass
    public static void setUpBeforeClass() {
        queryParser = new QueryParserPrefix(new QueryLexer());
    }

    @Test
    public void testQuery_01() {
        assertFalse(queryParser.isValidQuery("%!(test*123%"));
    }

    @Test
    public void testQuery_02() {
        assertFalse(queryParser.isValidQuery("%!(test*123, |(cat, dog))%"));
    }

    @Test
    public void testQuery_03() {
        assertFalse(queryParser.isValidQuery("%|(&(a,b),&(c,d),&(d,f)%"));
    }

    @Test
    public void testQuery_04() {
        assertFalse(queryParser.isValidQuery("%|(&(a,b)%"));
    }

    @Test
    public void testQuery_05() {
        assertFalse(queryParser.isValidQuery("%!(hey,hello,hi)%"));
    }
}
