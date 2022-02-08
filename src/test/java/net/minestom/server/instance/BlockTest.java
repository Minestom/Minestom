package net.minestom.server.instance;

import net.minestom.server.instance.block.Block;
import net.minestom.server.tag.Tag;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class BlockTest {

    @Test
    public void testNBT() {
        Block block = Block.CHEST;
        assertFalse(block.hasNbt());
        assertNull(block.nbt());

        var nbt = new NBTCompound(Map.of("key", NBT.Int(5)));
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
    public void testProperty() {
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

        assertThrows(Exception.class, () -> block.withProperty("random", "randomKey"));
    }

    @Test
    public void testEquality() {
        var nbt = new NBTCompound(Map.of("key", NBT.Int(5)));
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
}
