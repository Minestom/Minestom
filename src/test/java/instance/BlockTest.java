package instance;

import net.minestom.server.instance.block.Block;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTInt;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BlockTest {

    @Test
    public void testEquality() {
        var nbt = new NBTCompound(Map.of("key", new NBTInt(5)));
        Block b1 = Block.CHEST;
        Block b2 = Block.CHEST;
        assertEquals(b1.withNbt(nbt), b2.withNbt(nbt));

        assertEquals(b1.withProperty("facing", "north").getProperty("facing"), "north");
        assertEquals(b1.withProperty("facing", "north"), b2.withProperty("facing", "north"));
    }
}
