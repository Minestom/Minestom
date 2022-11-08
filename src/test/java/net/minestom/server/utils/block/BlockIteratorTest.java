package net.minestom.server.utils.block;

import net.minestom.server.coordinate.Vec;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class BlockIteratorTest {
    @Test
    public void blockIteratorRandom() {
        for (int i = 0; i < 10000000; ++i) {
            double r1 = Math.random() * 1000 - 500;
            double r2 = Math.random() * 1000 - 500;
            double r3 = Math.random() * 1000 - 500;

            if (Math.random() < 0.1) r1 = (int)r1;
            if (Math.random() < 0.1) r2 = (int)r2;
            if (Math.random() < 0.1) r3 = (int)r3;

            Vec v1 = new Vec(r1, r2, r3);

            double r4 = Math.random() * 1000 - 500;
            double r5 = Math.random() * 1000 - 500;
            double r6 = Math.random() * 1000 - 500;

            if (Math.random() < 0.1) r4 = (int)r4;
            if (Math.random() < 0.1) r5 = (int)r5;
            if (Math.random() < 0.1) r6 = (int)r6;

            Vec v2 = new Vec(r4, r5, r6);
            BlockIterator iterator = new BlockIterator(v1, v2, 0, (int) (Math.random() * 100));

            while (iterator.hasNext()) {
                iterator.next();
            }
        }
    }

    @Test
    public void testBroken() {
        Vec s = new Vec(0, -0.6, 0);
        Vec e = new Vec(-1, 1, 2);
        BlockIterator iterator = new BlockIterator(s, e, 0, 40);
    }

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
        Vec e = new Vec(2, -1, 0);
        BlockIterator iterator = new BlockIterator(s, e, 0, 4);

        assertEquals(new Vec(0, 0, 0), iterator.next());
        assertEquals(new Vec(1, 0, 0), iterator.next());
        assertEquals(new Vec(2, -1, 0), iterator.next());
        assertEquals(new Vec(3, -1, 0), iterator.next());
        assertFalse(iterator.hasNext());
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