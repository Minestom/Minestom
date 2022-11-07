package net.minestom.server.utils.block;

import net.minestom.server.coordinate.Vec;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class BlockIteratorTest {
    @Test
    public void moveExactYBlock() {
        Vec v1 = new Vec(41.21612735985405, 21, -51.0);
        Vec v2 = new Vec(0.7195944535812537, 0.5000002169949778, -0.6943945725414694);
        BlockIterator iterator = new BlockIterator(v1, v2, 0, 2);

        assertEquals(new Vec(41, 21, -51), iterator.next());
        assertEquals(new Vec(41, 21, -52), iterator.next());
        assertEquals(new Vec(42, 21, -52), iterator.next());
        assertEquals(new Vec(42, 21, -53), iterator.next());
        assertEquals(new Vec(42, 22, -53), iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void moveExactBlock() {
        Vec v1 = new Vec(41, 21, -51.0);
        Vec v2 = new Vec(1, 1, 1);
        BlockIterator iterator = new BlockIterator(v1, v2, 0, 2);

        assertEquals(new Vec(41, 21, -51), iterator.next());
        assertEquals(new Vec(42, 21, -51), iterator.next());
        assertEquals(new Vec(42, 22, -51), iterator.next());
        assertEquals(new Vec(42, 22, -50), iterator.next());
        assertFalse(iterator.hasNext());
    }
}
