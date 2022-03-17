package query.prefix;

import cz.zcu.kiv.ir.silhavyj.searchengine.index.IIndex;
import cz.zcu.kiv.ir.silhavyj.searchengine.index.Index;
import cz.zcu.kiv.ir.silhavyj.searchengine.query.lexer.QueryLexer;
import cz.zcu.kiv.ir.silhavyj.searchengine.query.parser.QueryParserPrefix;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

public class RelevantWordsTest {

    private static QueryParserPrefix queryParser;
    private static IIndex index;

    @BeforeClass
    public static void setUpBeforeClass() {
        queryParser = new QueryParserPrefix(new QueryLexer());
        index = new Index(null);
    }

    @Test
    public void testRelevantWords_01() {
        queryParser.search(index, "&(cat,dog,cow)");
        assertEquals(Stream.of("cat", "dog", "cow").collect(Collectors.toCollection(HashSet::new)), queryParser.getRelevantWords());
    }

    @Test
    public void testRelevantWords_02() {
        queryParser.search(index, "&(cat,!(dog),cow)");
        assertEquals(Stream.of("cat", "cow").collect(Collectors.toCollection(HashSet::new)), queryParser.getRelevantWords());
    }

    @Test
    public void testRelevantWords_03() {
        queryParser.search(index, "!(|(cat,!(dog),cow))");
        assertEquals(Stream.of("dog").collect(Collectors.toCollection(HashSet::new)), queryParser.getRelevantWords());
    }

    @Test
    public void testRelevantWords_04() {
        queryParser.search(index, "!(&(cat,dog,cow))");
        assertEquals(Stream.of().collect(Collectors.toCollection(HashSet::new)), queryParser.getRelevantWords());
    }

    @Test
    public void testRelevantWords_05() {
        queryParser.search(index, "!(!(!(computer)))");
        assertEquals(Stream.of().collect(Collectors.toCollection(HashSet::new)), queryParser.getRelevantWords());
    }

    @Test
    public void testRelevantWords_06() {
        queryParser.search(index, "!(!(computer))");
        assertEquals(Stream.of("computer").collect(Collectors.toCollection(HashSet::new)), queryParser.getRelevantWords());
    }

    @Test
    public void testRelevantWords_07() {
        queryParser.search(index, "!(&(computer, |(cat, !(cow),!(dog))))");
        assertEquals(Stream.of("cow", "dog").collect(Collectors.toCollection(HashSet::new)), queryParser.getRelevantWords());
    }

    @Test
    public void testRelevantWords_08() {
        queryParser.search(index, "!(|(!(A),!(B),!(C)))");
        assertEquals(Stream.of("A", "B", "C").collect(Collectors.toCollection(HashSet::new)), queryParser.getRelevantWords());
    }
}
