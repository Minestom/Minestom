package net.minestom.server.utils.collection;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class ObjectPoolTest {

    @Test
    public void pool() {
        var pool = ObjectPool.pool(Byte.MAX_VALUE, Object::new);
        Set<Object> pooledObjects = Collections.newSetFromMap(new IdentityHashMap<>());

        assertEquals(0, pool.count());
        var object = pool.get();
        pooledObjects.add(object);

        object = pool.get();
        assertTrue(pooledObjects.add(object));

        pool.add(object);
        assertEquals(1, pool.count());
        object = pool.get();
        assertEquals(0, pool.count());
        assertFalse(pooledObjects.add(object));
    }

    @Test
    public void autoClose() {
        var pool = ObjectPool.pool(Byte.MAX_VALUE, Object::new);
        assertEquals(0, pool.count());
        try (var ignored = pool.hold()) {
            assertEquals(0, pool.count());
        }
        assertEquals(1, pool.count());

        try (var ignored = pool.hold()) {
            assertEquals(0, pool.count());
        }
        assertEquals(1, pool.count());
    }

    @Test
    public void unpooled() {
        var pool = ObjectPool.pool(0, Object::new);
        Set<Object> pooledObjects = Collections.newSetFromMap(new IdentityHashMap<>());
        assertEquals(0, pool.count());

        var holder = pool.hold();
        assertEquals(0, pool.count(), "hold increased count");
        holder.close();
        assertThrows(IllegalStateException.class, holder::get);
        assertEquals(0, pool.count(), "exception increased count");

        var object = pool.get();
        pooledObjects.add(object);
        object = pool.get();
        assertTrue(pooledObjects.add(object), "should not return same object");

        assertEquals(2, pooledObjects.size());
    }
}
