package net.minestom.server.instance.block;

import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EnvTest
public class BlockFaceTest {
    @Test
    public void testAdd() {
        assertEquals(BlockFace.NORTH, BlockFace.SOUTH.add(BlockFace.SOUTH));
        assertEquals(BlockFace.EAST, BlockFace.WEST.add(BlockFace.SOUTH));
        assertEquals(BlockFace.SOUTH, BlockFace.EAST.add(BlockFace.EAST));
        assertEquals(BlockFace.WEST, BlockFace.NORTH.add(BlockFace.WEST));
    }

    @Test
    public void testSubtract() {
        assertEquals(BlockFace.SOUTH, BlockFace.NORTH.subtract(BlockFace.SOUTH));
        assertEquals(BlockFace.WEST, BlockFace.EAST.subtract(BlockFace.SOUTH));
        assertEquals(BlockFace.EAST, BlockFace.EAST.subtract(BlockFace.NORTH));
        assertEquals(BlockFace.NORTH, BlockFace.EAST.subtract(BlockFace.EAST));
    }
}
