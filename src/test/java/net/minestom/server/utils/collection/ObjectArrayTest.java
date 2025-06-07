package net.minestom.server.utils.collection;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;

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

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void trim(boolean concurrent) {
        ObjectArray<String> array = concurrent ? ObjectArray.concurrent() : ObjectArray.singleThread();
        // zero case
        array.trim();
        assertArrayEquals(new String[0], array.arrayCopy(String.class));

        // 3 elements with a space
        array.set(0, "Hey");
        array.set(1, "Hey2");
        array.set(3, "Hey4");
        array.trim();
        assertArrayEquals(new String[]{"Hey", "Hey2", null, "Hey4"}, array.arrayCopy(String.class));

        // 4 elements without a space
        array.set(2, "Hey3");
        array.trim();
        assertArrayEquals(new String[]{"Hey", "Hey2", "Hey3", "Hey4"}, array.arrayCopy(String.class));

        // set trailing 2 elements with a null
        array.remove(2);
        array.remove(3);
        array.trim();
        assertArrayEquals(new String[]{"Hey", "Hey2"}, array.arrayCopy(String.class));

        // remove first element
        array.remove(0);
        array.trim();
        assertArrayEquals(new String[]{null, "Hey2"}, array.arrayCopy(String.class));

        // remove last element, forcing the array to shrink after trim
        array.remove(1);
        array.trim();
        assertArrayEquals(new String[0], array.arrayCopy(String.class));
    }
}
