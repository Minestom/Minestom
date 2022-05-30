package net.minestom.server.utils.collection;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AutoIncrementMapTest {
    @Test
    public void test() {
        AutoIncrementMap<String> map = new AutoIncrementMap<>();
        for (int i = 0; i < 1000; i++) {
            assertEquals(i, map.get("test" + i));
            for (int j = 0; j < i; j++) {
                assertEquals(j, map.get("test" + j));
            }
        }
    }
}
