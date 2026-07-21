package net.minestom.server.instance.light;

import net.minestom.server.collision.Shape;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BlockIsOccludedTest {
    @Test
    public void blockAir() {
        Shape airBlock = Block.AIR.occlusionShape();

        for (BlockFace face : BlockFace.values()) {
            assertFalse(airBlock.isOccluded(airBlock, face));
        }
    }

    @Test
    public void blockLantern() {
        Shape shape = Block.LANTERN.occlusionShape();
        Shape airBlock = Block.AIR.occlusionShape();

        for (BlockFace face : BlockFace.values()) {
            assertFalse(shape.isOccluded(airBlock, face));
        }
    }

    @Test
    public void blockSpruceLeaves() {
        Shape shape = Block.SPRUCE_LEAVES.occlusionShape();
        Shape airBlock = Block.AIR.occlusionShape();

        for (BlockFace face : BlockFace.values()) {
            assertFalse(shape.isOccluded(airBlock, face));
        }
    }

    @Test
    public void blockCauldron() {
        Shape shape = Block.CAULDRON.occlusionShape();
        Shape airBlock = Block.AIR.occlusionShape();

        for (BlockFace face : BlockFace.values()) {
            assertFalse(shape.isOccluded(airBlock, face));
        }
    }

    @Test
    public void blockSlabBottomAir() {
        Shape shape = Block.SANDSTONE_SLAB.occlusionShape();
        Shape airBlock = Block.AIR.occlusionShape();

        assertTrue(shape.isOccluded(airBlock, BlockFace.BOTTOM));

        assertFalse(shape.isOccluded(airBlock, BlockFace.NORTH));
        assertFalse(shape.isOccluded(airBlock, BlockFace.SOUTH));
        assertFalse(shape.isOccluded(airBlock, BlockFace.EAST));
        assertFalse(shape.isOccluded(airBlock, BlockFace.WEST));
        assertFalse(shape.isOccluded(airBlock, BlockFace.TOP));
    }

    @Test
    public void blockSlabTopEnchantingTable() {
        Shape shape1 = Block.SANDSTONE_SLAB.withProperty("type", "top").occlusionShape();
        Shape shape2 = Block.ENCHANTING_TABLE.occlusionShape();

        assertFalse(shape1.isOccluded(shape2, BlockFace.BOTTOM));

        assertTrue(shape1.isOccluded(shape2, BlockFace.NORTH));
        assertTrue(shape1.isOccluded(shape2, BlockFace.SOUTH));
        assertTrue(shape1.isOccluded(shape2, BlockFace.EAST));
        assertTrue(shape1.isOccluded(shape2, BlockFace.WEST));
        assertTrue(shape1.isOccluded(shape2, BlockFace.TOP));
    }

    @Test
    public void blockStairWest() {
        Shape shape = Block.SANDSTONE_STAIRS.withProperties(Map.of(
                "facing", "west",
                "half", "bottom",
                "shape", "straight")).occlusionShape();

        Shape airBlock = Block.AIR.occlusionShape();

        assertTrue(shape.isOccluded(airBlock, BlockFace.WEST));
        assertTrue(shape.isOccluded(airBlock, BlockFace.BOTTOM));

        assertFalse(shape.isOccluded(airBlock, BlockFace.SOUTH));
        assertFalse(shape.isOccluded(airBlock, BlockFace.EAST));
        assertFalse(shape.isOccluded(airBlock, BlockFace.NORTH));
        assertFalse(shape.isOccluded(airBlock, BlockFace.TOP));
    }

    @Test
    public void blockSlabBottomStone() {
        Shape shape = Block.SANDSTONE_SLAB.occlusionShape();
        Shape stoneBlock = Block.STONE.occlusionShape();

        assertTrue(shape.isOccluded(stoneBlock, BlockFace.BOTTOM));
        assertTrue(shape.isOccluded(stoneBlock, BlockFace.NORTH));
        assertTrue(shape.isOccluded(stoneBlock, BlockFace.SOUTH));
        assertTrue(shape.isOccluded(stoneBlock, BlockFace.EAST));
        assertTrue(shape.isOccluded(stoneBlock, BlockFace.WEST));
        assertTrue(shape.isOccluded(stoneBlock, BlockFace.TOP));
    }

    @Test
    public void blockStone() {
        Shape shape = Block.STONE.occlusionShape();
        Shape airBlock = Block.AIR.occlusionShape();

        for (BlockFace face : BlockFace.values()) {
            assertTrue(shape.isOccluded(airBlock, face));
        }
    }

    @Test
    public void blockStair() {
        Shape shape = Block.SANDSTONE_STAIRS.occlusionShape();
        Shape airBlock = Block.AIR.occlusionShape();

        assertTrue(shape.isOccluded(airBlock, BlockFace.NORTH));
        assertTrue(shape.isOccluded(airBlock, BlockFace.BOTTOM));

        assertFalse(shape.isOccluded(airBlock, BlockFace.SOUTH));
        assertFalse(shape.isOccluded(airBlock, BlockFace.EAST));
        assertFalse(shape.isOccluded(airBlock, BlockFace.WEST));
        assertFalse(shape.isOccluded(airBlock, BlockFace.TOP));
    }

    @Test
    public void blockSlab() {
        Shape shape = Block.SANDSTONE_SLAB.occlusionShape();
        Shape airBlock = Block.AIR.occlusionShape();

        assertTrue(shape.isOccluded(airBlock, BlockFace.BOTTOM));

        assertFalse(shape.isOccluded(airBlock, BlockFace.NORTH));
        assertFalse(shape.isOccluded(airBlock, BlockFace.SOUTH));
        assertFalse(shape.isOccluded(airBlock, BlockFace.EAST));
        assertFalse(shape.isOccluded(airBlock, BlockFace.WEST));
        assertFalse(shape.isOccluded(airBlock, BlockFace.TOP));
    }

    @Test
    public void blockSlabBottomAndSlabTop() {
        Shape shape1 = Block.SANDSTONE_SLAB.occlusionShape();
        Shape shape2 = Block.SANDSTONE_SLAB.withProperty("type", "top").occlusionShape();

        assertFalse(shape1.isOccluded(shape2, BlockFace.TOP));

        assertTrue(shape1.isOccluded(shape2, BlockFace.BOTTOM));
        assertTrue(shape1.isOccluded(shape2, BlockFace.EAST));
        assertTrue(shape1.isOccluded(shape2, BlockFace.WEST));
        assertTrue(shape1.isOccluded(shape2, BlockFace.NORTH));
        assertTrue(shape1.isOccluded(shape2, BlockFace.SOUTH));
    }

    @Test
    public void blockSlabBottomAndSlabBottom() {
        Shape shape = Block.SANDSTONE_SLAB.occlusionShape();

        assertTrue(shape.isOccluded(shape, BlockFace.BOTTOM));
        assertTrue(shape.isOccluded(shape, BlockFace.TOP));

        assertFalse(shape.isOccluded(shape, BlockFace.EAST));
        assertFalse(shape.isOccluded(shape, BlockFace.WEST));
        assertFalse(shape.isOccluded(shape, BlockFace.NORTH));
        assertFalse(shape.isOccluded(shape, BlockFace.SOUTH));
    }

    @Test
    public void blockStairAndSlabBottom() {
        Shape shape1 = Block.STONE_STAIRS.occlusionShape();
        Shape shape2 = Block.SANDSTONE_SLAB.occlusionShape();

        assertTrue(shape1.isOccluded(shape2, BlockFace.BOTTOM));
        assertTrue(shape1.isOccluded(shape2, BlockFace.NORTH));
        assertTrue(shape1.isOccluded(shape2, BlockFace.TOP));

        assertFalse(shape1.isOccluded(shape2, BlockFace.EAST));
        assertFalse(shape1.isOccluded(shape2, BlockFace.WEST));
        assertFalse(shape1.isOccluded(shape2, BlockFace.SOUTH));
    }

    @Test
    public void blockStairAndSlabTop() {
        Shape shape1 = Block.STONE_STAIRS.occlusionShape();
        Shape shape2 = Block.SANDSTONE_SLAB.withProperty("type", "top").occlusionShape();

        assertTrue(shape1.isOccluded(shape2, BlockFace.NORTH));
        assertTrue(shape1.isOccluded(shape2, BlockFace.BOTTOM));
        assertTrue(shape1.isOccluded(shape2, BlockFace.EAST));
        assertTrue(shape1.isOccluded(shape2, BlockFace.WEST));
        assertTrue(shape1.isOccluded(shape2, BlockFace.SOUTH));

        assertFalse(shape1.isOccluded(shape2, BlockFace.TOP));
    }

    @Test
    public void occlusionShapeLeaves() {
        Shape shape = Block.OAK_LEAVES.occlusionShape();
        Shape airBlock = Block.AIR.occlusionShape();

        for (BlockFace face : BlockFace.values()) {
            assertFalse(shape.isOccluded(airBlock, face));
        }
    }

    @Test
    public void collisionShapeLeaves() {
        Shape shape = Block.OAK_LEAVES.collisionShape();
        Shape airBlock = Block.AIR.collisionShape();

        for (BlockFace face : BlockFace.values()) {
            assertTrue(shape.isOccluded(airBlock, face));
        }
    }
}
