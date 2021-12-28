package utils;

import net.minestom.server.utils.Paginator;
import org.jetbrains.annotations.Range;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestPaginator {

    private static final int SIZE = 27;

    private List<Integer> values;

    @BeforeEach
    public void init() {
        values = new ArrayList<>();

        for (int i = 0; i < SIZE; i++)
            values.add(i);
    }

    @Test
    public void noValues() {
        assertNull(Paginator.get(new ArrayList<Integer>(), 1, 5));
        assertNull(Paginator.get(new ArrayList<Integer>(), 1, 5, 0));
        assertNull(Paginator.get(new ArrayList<Integer>(), 1, 5, 1));
    }

    @Test
    public void getPageWhoDoesntExists() {
        assertNull(Paginator.get(values, Integer.MIN_VALUE, 7));
        assertNull(Paginator.get(values, -1, 7));
        assertNull(Paginator.get(values, 6, 5));
        assertNull(Paginator.get(values, 6, 7));
        assertNull(Paginator.get(values, Integer.MAX_VALUE, 7));
    }
    @Test
    public void getPage() {
        testGetPage(values, 0, 5, 5);
        testGetPage(values, 1, 5, 5);
        testGetPage(values, 1, 8, 8);
        testGetPage(values, 3, 8, 3);
        testGetPage(values, 3, 7, 6);
        testGetPage(values, SIZE - 1, 1, 1);
    }
    public <T> void testGetPage(List<T> values, int pageIndex, int pageSize, int pageSizeExpected) {
        final List<T> page = Paginator.get(values, pageIndex, pageSize);
        assertNotNull(page);
        assertEquals(pageSizeExpected, page.size());
    }

}
