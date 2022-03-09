package search;

import cz.zcu.kiv.ir.silhavyj.searchengine.index.IIndex;
import cz.zcu.kiv.ir.silhavyj.searchengine.index.Index;
import cz.zcu.kiv.ir.silhavyj.searchengine.query.lexer.IQueryLexer;
import cz.zcu.kiv.ir.silhavyj.searchengine.query.lexer.QueryLexer;
import cz.zcu.kiv.ir.silhavyj.searchengine.query.parser.IQueryParser;
import cz.zcu.kiv.ir.silhavyj.searchengine.query.parser.QueryParser;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedList;

import static org.junit.Assert.assertEquals;
import static utils.DocumentUtils.createDocumentIndex;

public class SearchTest {

    private static IIndex index;
    private static IQueryLexer queryLexer;
    private static IQueryParser queryParser;

    @BeforeClass
    public static void setUpBeforeClass() {
        index = new Index(null);

        index.addDocument("cat", 0, "");
        index.addDocument("cat", 1, "");
        index.addDocument("cat", 2, "");
        index.addDocument("cat", 4, "");

        index.addDocument("dog", 1, "");
        index.addDocument("dog", 2, "");
        index.addDocument("dog", 3, "");

        index.addDocument("cow", 1, "");
        index.addDocument("cow", 5, "");
        index.addDocument("cow", 13, "");

        queryLexer = new QueryLexer();
        queryParser = new QueryParser(queryLexer);
    }

    @Test
    public void testSearch_01() {
        final var actual = queryParser.search(index, "|(&(!(cat),cow),dog)");
        final var expected = createDocumentIndex(new LinkedList<>(Arrays.asList(1, 2, 3, 5, 13)));
        assertEquals(expected, actual);
    }

    @Test
    public void testSearch_02() {
        final var actual = queryParser.search(index, "&(!(cat),cow)");
        final var expected = createDocumentIndex(new LinkedList<>(Arrays.asList(5, 13)));
        assertEquals(expected, actual);
    }

    @Test
    public void testSearch_03() {
        final var actual = queryParser.search(index, "!(cat)");
        final var expected = createDocumentIndex(new LinkedList<>(Arrays.asList(3, 5, 13)));
        assertEquals(expected, actual);
    }

    @Test
    public void testSearch_04() {
        final var actual = queryParser.search(index, "|(dog,cow,cat)");
        final var expected = createDocumentIndex(new LinkedList<>(Arrays.asList(0, 1, 2, 3, 4, 5, 13)));
        assertEquals(expected, actual);
    }

    @Test
    public void testSearch_05() {
        final var actual = queryParser.search(index, "|(dog,cow,cat)");
        final var expected = queryParser.search(index, "|(dog,cat,cow)");
        assertEquals(expected, actual);
    }

    @Test
    public void testSearch_06() {
        final var actual = queryParser.search(index, "|(dog,dog)");
        final var expected = createDocumentIndex(new LinkedList<>(Arrays.asList(1, 2, 3)));
        assertEquals(expected, actual);
    }

    @Test
    public void testSearch_07() {
        final var actual = queryParser.search(index, "|(dog,&(dog,cow))");
        final var expected = createDocumentIndex(new LinkedList<>(Arrays.asList(1, 2, 3)));
        assertEquals(expected, actual);
    }

    @Test
    public void testSearch_08() {
        final var actual = queryParser.search(index, "|(dog,&(dog,cow))");
        final var expected = queryParser.search(index, "dog");
        assertEquals(expected, actual);
    }

    @Test
    public void testSearch_09() {
        final var actual = queryParser.search(index, "&(dog,dog,cow,cow,cat,cat)");
        final var expected = createDocumentIndex(new LinkedList<>(Arrays.asList(1)));
        assertEquals(expected, actual);
    }

    @Test
    public void testSearch_10() {
        final var actual = queryParser.search(index, "&(dog)");
        final var expected = createDocumentIndex(new LinkedList<>(Arrays.asList(1, 2, 3)));
        assertEquals(expected, actual);
    }

    @Test
    public void testSearch_11() {
        final var actual = queryParser.search(index, "|(cat)");
        final var expected = queryParser.search(index, "cat");
        assertEquals(expected, actual);
    }

    @Test
    public void testSearch_12() {
        final var actual = queryParser.search(index, "&(!(cat), !(dog), !(dog), cow)");
        final var expected = createDocumentIndex(new LinkedList<>(Arrays.asList(5, 13)));
        assertEquals(expected, actual);
    }

    @Test
    public void testSearch_13() {
        final var actual = queryParser.search(index, "&(!(cat), !(dog), !(cow))");
        final var expected = createDocumentIndex(new LinkedList<>(Arrays.asList()));
        assertEquals(expected, actual);
    }

    @Test
    public void testSearch_14() {
        final var actual = queryParser.search(index, "|(cat, c++)");
        final var expected = createDocumentIndex(new LinkedList<>(Arrays.asList(0, 1, 2, 4)));
        assertEquals(expected, actual);
    }
}
