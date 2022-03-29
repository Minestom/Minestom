package net.minestom.server.instance.light;

import net.minestom.server.collision.Shape;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BlockIsOccludedTest {

    @Test
    public void blockAir() {
        Shape shape = Block.AIR.registry().shape();
        for (BlockFace face : BlockFace.values()) {
            assertFalse(shape.isOccluded(face));
        }
    }

    @Test
    public void blockLantern() {
        Shape shape = Block.LANTERN.registry().shape();
        for (BlockFace face : BlockFace.values()) {
            assertFalse(shape.isOccluded(face));
        }
    }

    @Test
    public void blockCauldron() {
        Shape shape = Block.CAULDRON.registry().shape();
        for (BlockFace face : BlockFace.values()) {
            assertFalse(shape.isOccluded(face));
        }
    }

    @Test
    public void blockEnchantingTable() {
        Shape shape = Block.ENCHANTING_TABLE.registry().shape();
        assertTrue(shape.isOccluded(BlockFace.BOTTOM));

        assertFalse(shape.isOccluded(BlockFace.NORTH));
        assertFalse(shape.isOccluded(BlockFace.SOUTH));
        assertFalse(shape.isOccluded(BlockFace.EAST));
        assertFalse(shape.isOccluded(BlockFace.WEST));
        assertFalse(shape.isOccluded(BlockFace.TOP));
    }

    @Test
    public void blockStone() {
        Shape shape = Block.STONE.registry().shape();
        for (BlockFace face : BlockFace.values()) {
            assertTrue(shape.isOccluded(face));
        }
    }

    @Test
    public void blockStair() {
        Shape shape = Block.SANDSTONE_STAIRS.registry().shape();
        assertTrue(shape.isOccluded(BlockFace.NORTH));
        assertTrue(shape.isOccluded(BlockFace.BOTTOM));

        assertFalse(shape.isOccluded(BlockFace.SOUTH));
        assertFalse(shape.isOccluded(BlockFace.EAST));
        assertFalse(shape.isOccluded(BlockFace.WEST));
        assertFalse(shape.isOccluded(BlockFace.TOP));
    }

    @Test
    public void blockSlab() {
        Shape shape = Block.SANDSTONE_SLAB.registry().shape();
        assertTrue(shape.isOccluded(BlockFace.BOTTOM));

        assertFalse(shape.isOccluded(BlockFace.NORTH));
        assertFalse(shape.isOccluded(BlockFace.SOUTH));
        assertFalse(shape.isOccluded(BlockFace.EAST));
        assertFalse(shape.isOccluded(BlockFace.WEST));
        assertFalse(shape.isOccluded(BlockFace.TOP));
    }

    @Test
    public void blockEnchantingTableAndSlabTop() {
        Shape shape1 = Block.ENCHANTING_TABLE.registry().shape();
        Shape shape2 = Block.SANDSTONE_SLAB.withProperty("type", "top").registry().shape();

        assertFalse(shape1.isAdditionOccluded(shape2, BlockFace.TOP));

        assertTrue(shape1.isAdditionOccluded(shape2, BlockFace.BOTTOM));
        assertTrue(shape1.isAdditionOccluded(shape2, BlockFace.EAST));
        assertTrue(shape1.isAdditionOccluded(shape2, BlockFace.WEST));
        assertTrue(shape1.isAdditionOccluded(shape2, BlockFace.NORTH));
        assertTrue(shape1.isAdditionOccluded(shape2, BlockFace.SOUTH));
    }

    @Test
    public void blockEnchantingTableAndSlabBottom() {
        Shape shape1 = Block.ENCHANTING_TABLE.registry().shape();
        Shape shape2 = Block.SANDSTONE_SLAB.registry().shape();

        assertTrue(shape1.isAdditionOccluded(shape2, BlockFace.BOTTOM));
        assertTrue(shape1.isAdditionOccluded(shape2, BlockFace.TOP));

        assertFalse(shape1.isAdditionOccluded(shape2, BlockFace.EAST));
        assertFalse(shape1.isAdditionOccluded(shape2, BlockFace.WEST));
        assertFalse(shape1.isAdditionOccluded(shape2, BlockFace.NORTH));
        assertFalse(shape1.isAdditionOccluded(shape2, BlockFace.SOUTH));
    }

    @Test
    public void blockStairAndSlabBottom() {
        Shape shape1 = Block.STONE_STAIRS.registry().shape();
        Shape shape2 = Block.SANDSTONE_SLAB.registry().shape();

        assertTrue(shape1.isAdditionOccluded(shape2, BlockFace.BOTTOM));
        assertTrue(shape1.isAdditionOccluded(shape2, BlockFace.NORTH));
        assertTrue(shape1.isAdditionOccluded(shape2, BlockFace.TOP));

        assertFalse(shape1.isAdditionOccluded(shape2, BlockFace.EAST));
        assertFalse(shape1.isAdditionOccluded(shape2, BlockFace.WEST));
        assertFalse(shape1.isAdditionOccluded(shape2, BlockFace.SOUTH));
    }

    @Test
    public void blockStairAndSlabTop() {
        Shape shape1 = Block.STONE_STAIRS.registry().shape();
        Shape shape2 = Block.SANDSTONE_SLAB.withProperty("type", "top").registry().shape();

        assertTrue(shape1.isAdditionOccluded(shape2, BlockFace.NORTH));
        assertTrue(shape1.isAdditionOccluded(shape2, BlockFace.BOTTOM));
        assertTrue(shape1.isAdditionOccluded(shape2, BlockFace.EAST));
        assertTrue(shape1.isAdditionOccluded(shape2, BlockFace.WEST));
        assertTrue(shape1.isAdditionOccluded(shape2, BlockFace.SOUTH));

        assertFalse(shape1.isAdditionOccluded(shape2, BlockFace.TOP));
    }
}
