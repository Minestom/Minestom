package net.minestom.server.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ObjectArrayTest {

    @Test
    public void testArray() {
        ObjectArray<String> array = new ObjectArray<>();

        array.set(50, "Hey");
        assertEquals("Hey", array.get(50));

        array.set(0, "Hey2");
        assertEquals("Hey2", array.get(0));
        assertEquals("Hey", array.get(50));

        array.trim();

        array.set(250, "Hey3");
        assertEquals("Hey3", array.get(250));
        assertEquals("Hey2", array.get(0));
        assertEquals("Hey", array.get(50));
    }
}
