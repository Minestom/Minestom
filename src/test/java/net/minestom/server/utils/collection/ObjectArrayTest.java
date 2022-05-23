package net.minestom.server.utils.collection;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ObjectArrayTest {

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void testArray(boolean concurrent) {
        ObjectArray<String> array = concurrent ? ObjectArray.concurrent() : ObjectArray.singleThread();

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
