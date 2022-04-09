package net.minestom.server.utils.collection;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ObjectArrayTest {

    @Test
    public void testArray() {
        ObjectArray<String> array = new ObjectArray<>();

        array.set(50, "Hey");
        assertEquals("Hey", array.get(50));
        assertNull(array.get(49));
        assertNull(array.get(51));

        array.set(0, "Hey2");
        assertEquals("Hey2", array.get(0));
        assertEquals("Hey", array.get(50));

        array.trim();
        assertEquals("Hey2", array.get(0));
        assertEquals("Hey", array.get(50));

        array.set(250, "Hey3");
        assertEquals("Hey3", array.get(250));
        assertEquals("Hey2", array.get(0));
        assertEquals("Hey", array.get(50));

        assertNull(array.get(49));
        assertNull(array.get(251));
        assertNull(array.get(Integer.MAX_VALUE));
    }
}
