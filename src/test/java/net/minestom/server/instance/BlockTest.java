package net.minestom.server.instance;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockEntityType;
import net.minestom.server.tag.Tag;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BlockTest {

    @Test
    public void testOutlineShapeDiffersFromCollisionShape() {
        // Torches have no collision but the client still outlines and targets them.
        assertEquals(Vec.ZERO, Block.TORCH.collisionShape().relativeEnd());
        assertEquals(new Vec(0.375, 0, 0.375), Block.TORCH.outlineShape().relativeStart());
        assertEquals(new Vec(0.625, 0.625, 0.625), Block.TORCH.outlineShape().relativeEnd());

        // Fences collide 1.5 blocks tall but the client sees a 1 block tall outline.
        assertEquals(1.5, Block.OAK_FENCE.collisionShape().relativeEnd().y());
        assertEquals(1.0, Block.OAK_FENCE.outlineShape().relativeEnd().y());
    }

    @Test
    public void testInteractionAndVisualShapes() {
        // The cauldron basin is ray traced in addition to its outline.
        assertEquals(new Vec(0.125, 0.25, 0.125), Block.CAULDRON.interactionShape().relativeStart());
        assertEquals(new Vec(0.875, 1.0, 0.875), Block.CAULDRON.interactionShape().relativeEnd());
        assertEquals(Vec.ZERO, Block.STONE.interactionShape().relativeEnd());

        // Soul sand sinks the player but is drawn as a full cube.
        assertEquals(0.875, Block.SOUL_SAND.collisionShape().relativeEnd().y());
        assertEquals(1.0, Block.SOUL_SAND.visualShape().relativeEnd().y());
    }

    @Test
    public void testOutlineShapeMatchesCollisionShapeForFullBlocks() {
        assertEquals(Block.STONE.collisionShape().relativeStart(), Block.STONE.outlineShape().relativeStart());
        assertEquals(Block.STONE.collisionShape().relativeEnd(), Block.STONE.outlineShape().relativeEnd());
    }

    @Test
    public void testNBT() {
        Block block = Block.CHEST;
        assertFalse(block.hasNbt());
        assertNull(block.nbt());

        var nbt = CompoundBinaryTag.builder().putInt("key", 5).build();
        block = block.withNbt(nbt);
        assertTrue(block.hasNbt());
        assertEquals(block.nbt(), nbt);
        assertSame(block, block.withNbt(nbt));

        block = block.withNbt(null);
        assertSame(Block.CHEST, block);
        assertFalse(block.hasNbt());
        assertNull(block.nbt());

        var value = block.getTag(Tag.String("key").defaultValue("Default"));
        assertEquals("Default", value);
    }

    @Test
    public void validProperties() {
        Block block = Block.CHEST;
        assertEquals(block.properties(), Objects.requireNonNull(Block.fromBlockId(block.id())).properties());

        // Default state may change, but the test is required to ensure the `properties` method is working
        assertEquals(Map.of("facing", "north",
                "type", "single",
                "waterlogged", "false"), block.properties());

        for (var possible : block.possibleStates()) {
            assertEquals(possible, block.withProperties(possible.properties()));
        }

        assertEquals("north", block.withProperty("facing", "north").getProperty("facing"));
        assertNotEquals(block.withProperty("facing", "north"), block.withProperty("facing", "south"));
    }

    @Test
    public void testState() {
        assertEquals("minecraft:dirt", Block.DIRT.state());
        assertEquals(Block.DIRT, Block.fromState("minecraft:dirt"));
        assertEquals(Block.CHEST, Block.fromState("minecraft:chest"));
        assertEquals(Block.CHEST, Block.fromState("minecraft:chest[]"));
        assertEquals(Block.CHEST.withProperty("facing", "north"), Block.fromState("minecraft:chest[facing=north]"));
        assertNull(Block.fromState("invalid namespace:dirt"));
        assertNull(Block.fromState("invalid namespace:chest[facing=north]"));
    }

    @Test
    public void invalidProperties() {
        Block block = Block.CHEST;
        assertThrows(Exception.class, () -> block.withProperty("random", "randomKey"));
        assertThrows(Exception.class, () -> block.withProperties(Map.of("random", "randomKey")));
    }

    @Test
    public void testEquality() {
        var nbt = CompoundBinaryTag.builder().putInt("key", 5).build();
        Block b1 = Block.CHEST;
        Block b2 = Block.CHEST;
        assertEquals(b1.withNbt(nbt), b2.withNbt(nbt));

        assertEquals("north", b1.withProperty("facing", "north").getProperty("facing"));
        assertEquals(b1.withProperty("facing", "north"), b2.withProperty("facing", "north"));
    }

    @Test
    public void testMutability() {
        Block block = Block.CHEST;
        assertThrows(Exception.class, () -> block.properties().put("facing", "north"));
        assertThrows(Exception.class, () -> block.withProperty("facing", "north").properties().put("facing", "south"));
    }

    @Test
    public void testShape() {
        Point start = Block.LANTERN.collisionShape().relativeStart();
        Point end = Block.LANTERN.collisionShape().relativeEnd();

        assertEquals(new Vec(0.3125, 0, 0.3125), start);
        assertEquals(new Vec(0.6875, 0.5625, 0.6875), end);
    }

    @Test
    public void testDuplicateProperties() {
        HashSet<Integer> assignedStates = new HashSet<>();
        for (Block block : Block.values()) {
            for (Block blockWithState : block.possibleStates()) {
                assertTrue(assignedStates.add(blockWithState.stateId()));
            }
        }
        assertEquals(Block.statesCount(), assignedStates.size());
    }

    @Test
    public void testStateConversions() {
        for (Block block : Block.values()) {
            for (Block blockWithState : block.possibleStates()) {
                assertSame(blockWithState, Block.fromStateId(blockWithState.stateId()));
                assertSame(blockWithState, Block.fromState(blockWithState.state()));
                assertSame(blockWithState, block.withProperties(blockWithState.properties()));
                blockWithState.properties().forEach((property, value) ->
                        assertEquals(value, blockWithState.getProperty(property)));
            }
        }
    }

    @Test
    void testBlockEntityRegistryLoading() {
        // Sanity to ensure we correctly load block entity types
        assertEquals(BlockEntityType.SIGN, Block.OAK_SIGN.blockEntityType());
    }
}
