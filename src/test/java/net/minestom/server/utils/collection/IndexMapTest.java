package net.minestom.server.utils.collection;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IndexMapTest {
    @Test
    public void test() {
        IndexMap<String> map = new IndexMap<>();
        for (int i = 0; i < 1000; i++) {
            assertEquals(i, map.get("test" + i));
            for (int j = 0; j < i; j++) {
                assertEquals(j, map.get("test" + j));
            }
        }
    }
}
