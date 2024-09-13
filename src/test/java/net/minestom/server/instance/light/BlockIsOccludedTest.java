package net.minestom.server.instance.light;

import net.minestom.server.collision.Shape;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlockIsOccludedTest {
    @Test
    void blockAir() {
        Shape airBlock = Block.AIR.registry().collisionShape();
        
        for (BlockFace face : BlockFace.getValues()) {
            assertFalse(airBlock.isOccluded(airBlock, face));
        }
    }

    @Test
    void blockLantern() {
        Shape shape = Block.LANTERN.registry().collisionShape();
        Shape airBlock = Block.AIR.registry().collisionShape();

        for (BlockFace face : BlockFace.getValues()) {
            assertFalse(shape.isOccluded(airBlock, face));
        }
    }

    @Test
    void blockSpruceLeaves() {
        Shape shape = Block.SPRUCE_LEAVES.registry().collisionShape();
        Shape airBlock = Block.AIR.registry().collisionShape();

        for (BlockFace face : BlockFace.getValues()) {
            assertFalse(shape.isOccluded(airBlock, face));
        }
    }

    @Test
    void blockCauldron() {
        Shape shape = Block.CAULDRON.registry().collisionShape();
        Shape airBlock = Block.AIR.registry().collisionShape();

        for (BlockFace face : BlockFace.getValues()) {
            assertFalse(shape.isOccluded(airBlock, face));
        }
    }

    @Test
    void blockSlabBottomAir() {
        Shape shape = Block.SANDSTONE_SLAB.registry().collisionShape();
        Shape airBlock = Block.AIR.registry().collisionShape();

        assertTrue(shape.isOccluded(airBlock, BlockFace.BOTTOM));

        assertFalse(shape.isOccluded(airBlock, BlockFace.NORTH));
        assertFalse(shape.isOccluded(airBlock, BlockFace.SOUTH));
        assertFalse(shape.isOccluded(airBlock, BlockFace.EAST));
        assertFalse(shape.isOccluded(airBlock, BlockFace.WEST));
        assertFalse(shape.isOccluded(airBlock, BlockFace.TOP));
    }

    @Test
    void blockSlabTopEnchantingTable() {
        Shape shape1 = Block.SANDSTONE_SLAB.withProperty("type", "top").registry().collisionShape();
        Shape shape2 = Block.ENCHANTING_TABLE.registry().collisionShape();

        assertFalse(shape1.isOccluded(shape2, BlockFace.BOTTOM));

        assertTrue(shape1.isOccluded(shape2, BlockFace.NORTH));
        assertTrue(shape1.isOccluded(shape2, BlockFace.SOUTH));
        assertTrue(shape1.isOccluded(shape2, BlockFace.EAST));
        assertTrue(shape1.isOccluded(shape2, BlockFace.WEST));
        assertTrue(shape1.isOccluded(shape2, BlockFace.TOP));
    }

    @Test
    void blockStairWest() {
        Shape shape = Block.SANDSTONE_STAIRS.withProperties(Map.of(
                "facing", "west",
                "half", "bottom",
                "shape", "straight")).registry().collisionShape();

        Shape airBlock = Block.AIR.registry().collisionShape();

        assertTrue(shape.isOccluded(airBlock, BlockFace.WEST));
        assertTrue(shape.isOccluded(airBlock, BlockFace.BOTTOM));

        assertFalse(shape.isOccluded(airBlock, BlockFace.SOUTH));
        assertFalse(shape.isOccluded(airBlock, BlockFace.EAST));
        assertFalse(shape.isOccluded(airBlock, BlockFace.NORTH));
        assertFalse(shape.isOccluded(airBlock, BlockFace.TOP));
    }

    @Test
    void blockSlabBottomStone() {
        Shape shape = Block.SANDSTONE_SLAB.registry().collisionShape();
        Shape stoneBlock = Block.STONE.registry().collisionShape();

        assertTrue(shape.isOccluded(stoneBlock, BlockFace.BOTTOM));
        assertTrue(shape.isOccluded(stoneBlock, BlockFace.NORTH));
        assertTrue(shape.isOccluded(stoneBlock, BlockFace.SOUTH));
        assertTrue(shape.isOccluded(stoneBlock, BlockFace.EAST));
        assertTrue(shape.isOccluded(stoneBlock, BlockFace.WEST));
        assertTrue(shape.isOccluded(stoneBlock, BlockFace.TOP));
    }

    @Test
    void blockStone() {
        Shape shape = Block.STONE.registry().collisionShape();
        Shape airBlock = Block.AIR.registry().collisionShape();

        for (BlockFace face : BlockFace.getValues()) {
            assertTrue(shape.isOccluded(airBlock, face));
        }
    }

    @Test
    void blockStair() {
        Shape shape = Block.SANDSTONE_STAIRS.registry().collisionShape();
        Shape airBlock = Block.AIR.registry().collisionShape();

        assertTrue(shape.isOccluded(airBlock, BlockFace.NORTH));
        assertTrue(shape.isOccluded(airBlock, BlockFace.BOTTOM));

        assertFalse(shape.isOccluded(airBlock, BlockFace.SOUTH));
        assertFalse(shape.isOccluded(airBlock, BlockFace.EAST));
        assertFalse(shape.isOccluded(airBlock, BlockFace.WEST));
        assertFalse(shape.isOccluded(airBlock, BlockFace.TOP));
    }

    @Test
    void blockSlab() {
        Shape shape = Block.SANDSTONE_SLAB.registry().collisionShape();
        Shape airBlock = Block.AIR.registry().collisionShape();

        assertTrue(shape.isOccluded(airBlock, BlockFace.BOTTOM));

        assertFalse(shape.isOccluded(airBlock, BlockFace.NORTH));
        assertFalse(shape.isOccluded(airBlock, BlockFace.SOUTH));
        assertFalse(shape.isOccluded(airBlock, BlockFace.EAST));
        assertFalse(shape.isOccluded(airBlock, BlockFace.WEST));
        assertFalse(shape.isOccluded(airBlock, BlockFace.TOP));
    }

    @Test
    void blockSlabBottomAndSlabTop() {
        Shape shape1 = Block.SANDSTONE_SLAB.registry().collisionShape();
        Shape shape2 = Block.SANDSTONE_SLAB.withProperty("type", "top").registry().collisionShape();

        assertFalse(shape1.isOccluded(shape2, BlockFace.TOP));

        assertTrue(shape1.isOccluded(shape2, BlockFace.BOTTOM));
        assertTrue(shape1.isOccluded(shape2, BlockFace.EAST));
        assertTrue(shape1.isOccluded(shape2, BlockFace.WEST));
        assertTrue(shape1.isOccluded(shape2, BlockFace.NORTH));
        assertTrue(shape1.isOccluded(shape2, BlockFace.SOUTH));
    }

    @Test
    void blockSlabBottomAndSlabBottom() {
        Shape shape = Block.SANDSTONE_SLAB.registry().collisionShape();

        assertTrue(shape.isOccluded(shape, BlockFace.BOTTOM));
        assertTrue(shape.isOccluded(shape, BlockFace.TOP));

        assertFalse(shape.isOccluded(shape, BlockFace.EAST));
        assertFalse(shape.isOccluded(shape, BlockFace.WEST));
        assertFalse(shape.isOccluded(shape, BlockFace.NORTH));
        assertFalse(shape.isOccluded(shape, BlockFace.SOUTH));
    }

    @Test
    void blockStairAndSlabBottom() {
        Shape shape1 = Block.STONE_STAIRS.registry().collisionShape();
        Shape shape2 = Block.SANDSTONE_SLAB.registry().collisionShape();

        assertTrue(shape1.isOccluded(shape2, BlockFace.BOTTOM));
        assertTrue(shape1.isOccluded(shape2, BlockFace.NORTH));
        assertTrue(shape1.isOccluded(shape2, BlockFace.TOP));

        assertFalse(shape1.isOccluded(shape2, BlockFace.EAST));
        assertFalse(shape1.isOccluded(shape2, BlockFace.WEST));
        assertFalse(shape1.isOccluded(shape2, BlockFace.SOUTH));
    }

    @Test
    void blockStairAndSlabTop() {
        Shape shape1 = Block.STONE_STAIRS.registry().collisionShape();
        Shape shape2 = Block.SANDSTONE_SLAB.withProperty("type", "top").registry().collisionShape();

        assertTrue(shape1.isOccluded(shape2, BlockFace.NORTH));
        assertTrue(shape1.isOccluded(shape2, BlockFace.BOTTOM));
        assertTrue(shape1.isOccluded(shape2, BlockFace.EAST));
        assertTrue(shape1.isOccluded(shape2, BlockFace.WEST));
        assertTrue(shape1.isOccluded(shape2, BlockFace.SOUTH));

        assertFalse(shape1.isOccluded(shape2, BlockFace.TOP));
    }
}
