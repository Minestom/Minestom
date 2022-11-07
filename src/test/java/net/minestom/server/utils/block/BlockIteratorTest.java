package net.minestom.server.utils.block;

import net.minestom.server.coordinate.Vec;
import org.junit.jupiter.api.Test;

public class BlockIteratorTest {
    @Test
    public void testException() {
        {
            Vec v1 = new Vec(41.21612735985405, 21, -51.0);
            Vec v2 = new Vec(0.7195944535812537, 0.5000002169949778, -0.6943945725414694);
            BlockIterator iterator = new BlockIterator(v1, v2, 0, 2);

            System.out.println(iterator.hasNext());
        }

        {
            Vec v1 = new Vec(42, 21, -51.0);
            Vec v2 = new Vec(0.1, 0, 0);
            BlockIterator iterator = new BlockIterator(v1, v2, 0, 2);
        }

        {
            Vec v1 = new Vec(42, 210, -51.0);
            Vec v2 = new Vec(0.1, 0, 0);
            BlockIterator iterator = new BlockIterator(v1, v2, 0, 2);
        }

        {
            Vec v1 = new Vec(42, 0.0000000000000000001, -51.0);
            Vec v2 = new Vec(0.1, 0, 0);
            BlockIterator iterator = new BlockIterator(v1, v2, 0, 2);
        }
    }

    @Test
    public void testNoException() {
        {
            Vec v1 = new Vec(41.21612735985405, 21.0000001, -51.0);
            Vec v2 = new Vec(0.7195944535812537, 0.5000002169949778, -0.6943945725414694);
            BlockIterator iterator = new BlockIterator(v1, v2, 0, 2);
        }

        {
            Vec v1 = new Vec(41.21612735985405, 21.5, -51.0);
            Vec v2 = new Vec(0.7195944535812537, 0.5000002169949778, -0.6943945725414694);
            BlockIterator iterator = new BlockIterator(v1, v2, 0, 2);
        }

        {
            Vec v1 = new Vec(41.21612735985405, 19.999999, -51.0);
            Vec v2 = new Vec(0.7195944535812537, 0.5000002169949778, -0.6943945725414694);
            BlockIterator iterator = new BlockIterator(v1, v2, 0, 2);
        }
    }
}
