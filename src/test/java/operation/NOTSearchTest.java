package operation;

import cz.zcu.kiv.ir.silhavyj.searchengine.index.SearchOperations;
import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedList;

import static org.junit.Assert.assertEquals;
import static operation.DocumentIndexUtils.createDocumentIndex;

public class NOTSearchTest {

    @Test
    public void testQuery_01() {
        final var doc1 = createDocumentIndex(new LinkedList<>(Arrays.asList(6, 7, 8)));
        final var doc2 = createDocumentIndex(new LinkedList<>(Arrays.asList(1, 2, 3, 4, 5)));
        final var actual = createDocumentIndex(new LinkedList<>(Arrays.asList(1, 2, 3, 4, 5)));
        final var expected = SearchOperations.not(doc1, doc2);
        assertEquals(expected, actual);
    }

    @Test
    public void testQuery_02() {
        final var doc1 = createDocumentIndex(new LinkedList<>(Arrays.asList(1, 2, 3)));
        final var doc2 = createDocumentIndex(new LinkedList<>(Arrays.asList(1, 2, 3, 4, 5)));
        final var actual = createDocumentIndex(new LinkedList<>(Arrays.asList(4, 5)));
        final var expected = SearchOperations.not(doc1, doc2);
        assertEquals(expected, actual);
    }

    @Test
    public void testQuery_03() {
        final var doc1 = createDocumentIndex(new LinkedList<>(Arrays.asList(1, 2, 3)));
        final var doc2 = createDocumentIndex(new LinkedList<>(Arrays.asList(1, 2, 3)));
        final var actual = createDocumentIndex(new LinkedList<>(Arrays.asList()));
        final var expected = SearchOperations.not(doc1, doc2);
        assertEquals(expected, actual);
    }

    @Test
    public void testQuery_04() {
        final var doc1 = createDocumentIndex(new LinkedList<>(Arrays.asList()));
        final var doc2 = createDocumentIndex(new LinkedList<>(Arrays.asList()));
        final var actual = createDocumentIndex(new LinkedList<>(Arrays.asList()));
        final var expected = SearchOperations.not(doc1, doc2);
        assertEquals(expected, actual);
    }

    @Test
    public void testQuery_05() {
        final var doc1 = createDocumentIndex(new LinkedList<>(Arrays.asList(1, 2, 3, 4)));
        final var doc2 = createDocumentIndex(new LinkedList<>(Arrays.asList(1, 2)));
        final var actual = createDocumentIndex(new LinkedList<>(Arrays.asList()));
        final var expected = SearchOperations.not(doc1, doc2);
        assertEquals(expected, actual);
    }

    @Test
    public void testQuery_06() {
        final var doc1 = createDocumentIndex(new LinkedList<>(Arrays.asList(1)));
        final var doc2 = createDocumentIndex(new LinkedList<>(Arrays.asList(1, 2)));
        final var actual = createDocumentIndex(new LinkedList<>(Arrays.asList(2)));
        final var expected = SearchOperations.not(doc1, doc2);
        assertEquals(expected, actual);
    }

    @Test
    public void testQuery_07() {
        final var doc1 = createDocumentIndex(new LinkedList<>(Arrays.asList(0)));
        final var doc2 = createDocumentIndex(new LinkedList<>(Arrays.asList(1, 2)));
        final var actual = createDocumentIndex(new LinkedList<>(Arrays.asList(1, 2)));
        final var expected = SearchOperations.not(doc1, doc2);
        assertEquals(expected, actual);
    }
}
