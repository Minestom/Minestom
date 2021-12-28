package instance;

import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.block.BlockUtils;
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
    }

    @Test
    public void testProperty() {
        Block block = Block.CHEST;
        assertEquals(block.properties(), Objects.requireNonNull(Block.fromBlockId(block.id())).properties());

        for (var possible : block.possibleStates()) {
            assertEquals(possible, block.withProperties(possible.properties()));
        }

        assertEquals(block.withProperty("facing", "north").getProperty("facing"), "north");
        assertNotEquals(block.withProperty("facing", "north"), block.withProperty("facing", "south"));

        assertThrows(Exception.class, () -> block.withProperty("random", "randomKey"));
    }

    @Test
    public void parseProperties() {
        assertEquals(Map.of(), BlockUtils.parseProperties("random test without brackets"));
        assertEquals(Map.of(), BlockUtils.parseProperties("[]"));
        assertEquals(Map.of(), BlockUtils.parseProperties("[    ]"));
        assertEquals(Map.of("facing", "east"), BlockUtils.parseProperties("[facing=east]"));
        assertEquals(Map.of("facing", "east", "key", "value"), BlockUtils.parseProperties("[facing=east,key=value ]"));
        assertEquals(Map.of("facing", "east", "key", "value"), BlockUtils.parseProperties("[ facing = east, key= value ]"));

        // Verify until the limit of 10 entries
        for (int i = 0; i < 13; i++) {
            StringBuilder properties = new StringBuilder("[");
            for (int j = 0; j < i; j++) {
                properties.append("key").append(j).append("=value").append(j);
                if (j != i - 1) properties.append(",");
            }
            properties.append("]");

            var map = BlockUtils.parseProperties(properties.toString());
            assertEquals(i, map.size());
            for (int j = 0; j < i; j++) {
                assertEquals("value" + j, map.get("key" + j));
            }
        }
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
