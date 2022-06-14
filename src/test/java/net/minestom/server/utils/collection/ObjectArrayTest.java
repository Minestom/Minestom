package net.minestom.server.utils.collection;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

public class ObjectArrayTest {

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void objectArray(boolean concurrent) {
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

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void arrayCopy(boolean concurrent) {
        ObjectArray<String> array = concurrent ? ObjectArray.concurrent() : ObjectArray.singleThread();

        array.set(1, "Hey");
        String[] copyCache = array.arrayCopy(String.class);
        assertArrayEquals(new String[]{null, "Hey"}, copyCache);

        array.set(2, "Hey2");
        assertArrayEquals(new String[]{null, "Hey", "Hey2"}, array.arrayCopy(String.class));
        assertArrayEquals(new String[]{null, "Hey"}, copyCache, "The copy cache should not be modified");
    }
}
