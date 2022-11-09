package net.minestom.server.utils.block;

import net.minestom.server.coordinate.Vec;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class BlockIteratorTest {
    @Test
    public void test2dOffset() {
        Vec s = new Vec(0,  0.1, 0);
        Vec e = new Vec(2, 1, 0);
        BlockIterator iterator = new BlockIterator(s, e, 0, 4);

        assertEquals(new Vec(0, 0, 0), iterator.next());
        assertEquals(new Vec(1, 0, 0), iterator.next());
        assertEquals(new Vec(1, 1, 0), iterator.next());
        assertEquals(new Vec(2, 1, 0), iterator.next());
        assertEquals(new Vec(3, 1, 0), iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void test2dpp() {
        Vec s = new Vec(0,  0, 0);
        Vec e = new Vec(2, 1, 0);
        BlockIterator iterator = new BlockIterator(s, e, 0, 4);

        assertEquals(new Vec(0, 0, 0), iterator.next());
        assertEquals(new Vec(1, 0, 0), iterator.next());
        assertEquals(new Vec(2, 1, 0), iterator.next());
        assertEquals(new Vec(3, 1, 0), iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void test2dpn() {
        Vec s = new Vec(0,  0, 0);
        Vec e = new Vec(1, 1, 1);
        BlockIterator iterator = new BlockIterator(s, e, 0, 6);

        while (iterator.hasNext()) {
            var out = iterator.next();
            System.out.println("OUT " + out + "\n");
        }

        // assertEquals(new Vec(-1, 0, 0), iterator.next());
        // assertEquals(new Vec(0, 0, 0), iterator.next());
        // assertEquals(new Vec(-1, -1, 0), iterator.next());

        // assertEquals(new Vec(0, -1, 0), iterator.next());
        // assertEquals(new Vec(1, -1, 0), iterator.next());
        // assertEquals(new Vec(2, -2, 0), iterator.next());
        // assertEquals(new Vec(3, -2, 0), iterator.next());
        // assertFalse(iterator.hasNext());
    }

    @Test
    public void test2dnn() {
        Vec s = new Vec(0,  0, 0);
        Vec e = new Vec(-2, -1, 0);
        BlockIterator iterator = new BlockIterator(s, e, 0, 4);

        assertEquals(new Vec(-1, -1, 0), iterator.next());
        assertEquals(new Vec(-2, -1, 0), iterator.next());
        assertEquals(new Vec(-3, -2, 0), iterator.next());
        assertEquals(new Vec(-4, -2, 0), iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void failing() {
        // START Vec[x=0.7, y=42.0, z=1.1] Vec[x=0.6, y=0.0, z=-0.6] 1
        Vec s = new Vec(0.0,  0, 0);
        Vec e = new Vec(-2, 1, 0);
        BlockIterator iterator = new BlockIterator(s, e, 0, 4);

        while (iterator.hasNext()) {
            System.out.println(iterator.next());
        }
    }

    @Test
    public void testComplex() {
        Vec s = new Vec(0.3,  44, 0.3);
        Vec e = new Vec(0, -10, 0);
        BlockIterator iterator = new BlockIterator(s, e, 0, 10);

        while (iterator.hasNext()) {
            System.out.println(iterator.next());
        }
    }
}