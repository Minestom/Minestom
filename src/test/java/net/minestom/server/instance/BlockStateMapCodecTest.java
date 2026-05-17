package net.minestom.server.instance;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.adventure.MinestomAdventure;
import net.minestom.server.codec.Transcoder;
import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static net.minestom.server.instance.block.Block.BLOCK_STATE_MAP_CODEC;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BlockStateMapCodecTest {
    @Test
    public void testEquivalence() throws IOException {
        String particleString = "{Properties:{lit:\"true\"},Name:\"copper_bulb\"}";
        CompoundBinaryTag nbt = MinestomAdventure.NBT_CODEC.decode(particleString);
        Block block = BLOCK_STATE_MAP_CODEC.decode(Transcoder.NBT,nbt).orElseThrow();
        assertEquals("true", block.getProperty("lit"));
        assertEquals(block.defaultState().getProperty("powered"), block.getProperty("powered"));
        assertEquals("minecraft:copper_bulb", block.name());
        BinaryTag newNBT = BLOCK_STATE_MAP_CODEC.encode(Transcoder.NBT,block).orElseThrow();
        String newString = MinestomAdventure.NBT_CODEC.encode((CompoundBinaryTag) newNBT);
        assertEquals(particleString, newString);
    }

    @Test
    public void testDefaultBlockState() throws IOException {
        String defaultFacing = Block.SPRUCE_STAIRS.defaultState().getProperty("facing");
        assert defaultFacing != null;
        Block block = Block.SPRUCE_STAIRS.withProperty("facing", defaultFacing);
        BinaryTag nbt = BLOCK_STATE_MAP_CODEC.encode(Transcoder.NBT, block).orElseThrow();
        String nbtString = MinestomAdventure.NBT_CODEC.encode((CompoundBinaryTag) nbt);
        assertEquals("{Name:\"spruce_stairs\"}", nbtString);
    }

    @Test
    public void testPropertyBlockState() throws IOException {
        Block block = Block.SPRUCE_STAIRS.withProperty("facing", "south");
        BinaryTag nbt = BLOCK_STATE_MAP_CODEC.encode(Transcoder.NBT, block).orElseThrow();
        String nbtString = MinestomAdventure.NBT_CODEC.encode((CompoundBinaryTag) nbt);
        assertEquals("{Properties:{facing:\"south\"},Name:\"spruce_stairs\"}", nbtString);
    }
}
