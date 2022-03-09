package query;

import cz.zcu.kiv.ir.silhavyj.searchengine.query.lexer.QueryLexer;
import cz.zcu.kiv.ir.silhavyj.searchengine.query.parser.QueryParser;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ValidQueryTest {

    private static QueryParser queryParser;

    @BeforeClass
    public static void setUpBeforeClass() {
        queryParser = new QueryParser(new QueryLexer());
    }

    @Test
    public void testQuery_01() {
        assertTrue(queryParser.isValidQuery("%&(y,y)%"));
    }

    @Test
    public void testQuery_02() {
        assertTrue(queryParser.isValidQuery("%&(\\),y)%"));
    }

    @Test
    public void testQuery_03() {
        assertTrue(queryParser.isValidQuery("%&  (te15*st, |(a,bsad), hey)%"));
    }

    @Test
    public void testQuery_04() {
        assertTrue(queryParser.isValidQuery("%!(test*123)%"));
    }

    @Test
    public void testQuery_05() {
        assertTrue(queryParser.isValidQuery("%!(test*123\\,)%"));
    }

    @Test
    public void testQuery_06() {
        assertTrue(queryParser.isValidQuery("%&(cat, dog, |(glass, peter), !(kettle))%"));
    }

    @Test
    public void testQuery_07() {
        assertTrue(queryParser.isValidQuery("%|(&(a,b),&(c,d),&(d,f))%"));
    }

    @Test
    public void testQuery_08() {
        assertTrue(queryParser.isValidQuery("%|(|(a,b),x)%"));
    }

    @Test
    public void testQuery_09() {
        assertTrue(queryParser.isValidQuery("%|(a,b,x)%"));
    }

    @Test
    public void testQuery_10() {
        assertTrue(queryParser.isValidQuery("%|(a)%"));
    }

    @Test
    public void testQuery_11() {
        assertTrue(queryParser.isValidQuery("%&(hell0)%"));
    }

    @Test
    public void testQuery_12() {
        assertTrue(queryParser.isValidQuery("%|(&(a,b),&(c,d),&(d,f))%"));
    }
}
