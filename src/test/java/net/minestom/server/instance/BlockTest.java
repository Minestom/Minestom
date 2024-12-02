package net.minestom.server.instance;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.tag.Tag;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BlockTest {

    @Test
    public void testNBT() {
        Block block = Block.CHEST;
        assertFalse(block.hasNbt());
        assertNull(block.nbt());

        var nbt = CompoundBinaryTag.builder().putInt("key", 5).build();
        block = block.withNbt(nbt);
        assertTrue(block.hasNbt());
        assertEquals(block.nbt(), nbt);

        block = block.withNbt(null);
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

        assertEquals(block.withProperty("facing", "north").getProperty("facing"), "north");
        assertNotEquals(block.withProperty("facing", "north"), block.withProperty("facing", "south"));
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

        assertEquals(b1.withProperty("facing", "north").getProperty("facing"), "north");
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
        Point start = Block.LANTERN.registry().collisionShape().relativeStart();
        Point end = Block.LANTERN.registry().collisionShape().relativeEnd();

        assertEquals(start, new Vec(0.3125, 0, 0.3125));
        assertEquals(end, new Vec(0.6875, 0.5625, 0.6875));
    }

    @Test
    public void testDuplicateProperties() {
        HashSet<Integer> assignedStates = new HashSet<>();
        for (Block block : Block.values()) {
            for (Block blockWithState : block.possibleStates()) {
                assertTrue(assignedStates.add(blockWithState.stateId()));
            }
        }
    }

    @Test
    public void testStateIdConversion() {
        for (Block block : Block.values()) {
            for (Block blockWithState : block.possibleStates()) {
                assertEquals(blockWithState, Block.fromStateId(blockWithState.stateId()));
            }
        }
    }
}
