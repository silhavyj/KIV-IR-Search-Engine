package operation;

import cz.zcu.kiv.ir.silhavyj.searchengine.index.SearchOperations;
import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedList;

import static org.junit.Assert.assertEquals;
import static utils.DocumentUtils.createDocumentIndex;

public class ANDQueryTest {

    @Test
    public void testQuery_01() {
        final var doc1 = createDocumentIndex(new LinkedList<>(Arrays.asList(1, 2)));
        final var doc2 = createDocumentIndex(new LinkedList<>(Arrays.asList(0, 1, 2, 3, 5)));
        final var actual = createDocumentIndex(new LinkedList<>(Arrays.asList(1, 2)));
        final var expected = SearchOperations.and(doc1, doc2);
        assertEquals(expected, actual);
    }

    @Test
    public void testQuery_02() {
        final var doc1 = createDocumentIndex(new LinkedList<>(Arrays.asList(1, 2)));
        final var doc2 = createDocumentIndex(new LinkedList<>(Arrays.asList(0)));
        final var expected = SearchOperations.and(doc1, doc2);
        assertEquals(null, expected);
    }

    @Test
    public void testQuery_03() {
        final var doc1 = createDocumentIndex(new LinkedList<>(Arrays.asList(1, 2, 15, 101)));
        final var doc2 = createDocumentIndex(new LinkedList<>(Arrays.asList(101)));
        final var actual = createDocumentIndex(new LinkedList<>(Arrays.asList(101)));
        final var expected = SearchOperations.and(doc1, doc2);
        assertEquals(actual, expected);
    }

    @Test
    public void testQuery_04() {
        final var doc1 = createDocumentIndex(new LinkedList<>(Arrays.asList(7, 15, 21, 155)));
        final var doc2 = createDocumentIndex(new LinkedList<>(Arrays.asList(7, 15, 21, 155)));
        final var actual = createDocumentIndex(new LinkedList<>(Arrays.asList(7, 15, 21, 155)));
        final var expected = SearchOperations.and(doc1, doc2);
        assertEquals(actual, expected);
    }

    @Test
    public void testQuery_05() {
        final var doc1 = createDocumentIndex(new LinkedList<>(Arrays.asList()));
        final var doc2 = createDocumentIndex(new LinkedList<>(Arrays.asList()));
        final var actual = createDocumentIndex(new LinkedList<>(Arrays.asList()));
        final var expected = SearchOperations.and(doc1, doc2);
        assertEquals(actual, expected);
    }

    @Test
    public void testQuery_06() {
        final var doc1 = createDocumentIndex(new LinkedList<>(Arrays.asList(1001, 1002, 1003, 1004)));
        final var doc2 = createDocumentIndex(new LinkedList<>(Arrays.asList(1004, 1005, 1006, 1007)));
        final var actual = createDocumentIndex(new LinkedList<>(Arrays.asList(1004)));
        final var expected = SearchOperations.and(doc1, doc2);
        assertEquals(actual, expected);
    }

    @Test
    public void testQuery_07() {
        final var doc1 = createDocumentIndex(new LinkedList<>(Arrays.asList(1001, 1002, 1003, 1004)));
        final var doc2 = createDocumentIndex(new LinkedList<>(Arrays.asList(1005, 1006, 1007, 1008)));
        final var actual = createDocumentIndex(new LinkedList<>(Arrays.asList()));
        final var expected = SearchOperations.and(doc1, doc2);
        assertEquals(actual, expected);
    }

    @Test
    public void testQuery_08() {
        final var doc1 = createDocumentIndex(new LinkedList<>(Arrays.asList(1, 3, 5, 7)));
        final var doc2 = createDocumentIndex(new LinkedList<>(Arrays.asList(2, 4, 6, 8)));
        final var actual = createDocumentIndex(new LinkedList<>(Arrays.asList()));
        final var expected = SearchOperations.and(doc1, doc2);
        assertEquals(actual, expected);
    }
}
