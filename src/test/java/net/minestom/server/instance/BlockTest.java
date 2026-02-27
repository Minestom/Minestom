package net.minestom.server.instance;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockEntityType;
import net.minestom.server.instance.block.property.Property;
import net.minestom.server.instance.block.property.enums.BedPart;
import net.minestom.server.instance.block.property.enums.Facing;
import net.minestom.server.instance.block.property.enums.RedstoneWireSide;
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
    public void testState() {
        assertEquals("minecraft:dirt", Block.DIRT.state());
        assertEquals(Block.DIRT, Block.fromState("minecraft:dirt"));
        assertEquals(Block.CHEST, Block.fromState("minecraft:chest"));
        assertEquals(Block.CHEST, Block.fromState("minecraft:chest[]"));
        assertEquals(Block.CHEST.withProperty("facing", "north"), Block.fromState("minecraft:chest[facing=north]"));
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

    @Test
    void testBlockEntityRegistryLoading() {
        // Sanity to ensure we correctly load block entity types
        assertEquals(BlockEntityType.SIGN, Block.OAK_SIGN.registry().blockEntityType());
    }

    @Test
    public void testTypedBooleanProperty() {
        final Block block = Block.OAK_FENCE;
        assertEquals("false", block.getProperty("south"));
        assertFalse(block.getProperty(Property.SOUTH));

        final Block withSouth = block.withProperty(Property.SOUTH, true);
        assertEquals("true", withSouth.getProperty("south"));
        assertTrue(withSouth.getProperty(Property.SOUTH));
    }

    @Test
    public void testTypedIntegerProperty() {
        final Block block = Block.WHEAT;
        assertEquals("0", block.getProperty("age"));
        assertEquals(0, block.getProperty(Property.AGE));

        final Block withAge = block.withProperty(Property.AGE, 5);
        assertEquals("5", withAge.getProperty("age"));
        assertEquals(5, withAge.getProperty(Property.AGE));
    }

    @Test
    public void testTypedEnumProperty() {
        final Block block = Block.RED_BED;
        assertEquals("foot", block.getProperty("part"));
        assertEquals(BedPart.FOOT, block.getProperty(Property.BED_PART));
        assertEquals("north", block.getProperty("facing")); // property "facing" has subsets, test it as well.
        assertEquals(Facing.NORTH, block.getProperty(Property.FACING));

        final Block withPart = block.withProperty(BedPart.HEAD);
        assertEquals("head", withPart.getProperty("part"));
        assertEquals(BedPart.HEAD, withPart.getProperty(Property.BED_PART));

        final Block withFacing = block.withProperty(Property.FACING, Facing.EAST);
        assertEquals("east", withFacing.getProperty("facing"));
        assertEquals(Facing.EAST, withFacing.getProperty(Property.FACING));

        // `RedstoneWireSide` is associated with multiple properties
        assertThrows(IllegalArgumentException.class, () -> Block.REDSTONE_WIRE.withProperty(RedstoneWireSide.UP));
    }
}
