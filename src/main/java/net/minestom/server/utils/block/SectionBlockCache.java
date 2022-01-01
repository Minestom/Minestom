package net.minestom.server.utils.block;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

public class SectionBlockCache implements BlockCache {
    private final Int2ObjectMap<Block> blocks = new Int2ObjectOpenHashMap<>();

    @Override
    public void setBlock(int x, int y, int z, @NotNull Block block) {
        blocks.put(getIndex(x,y,z), block);
    }

    @Override
    public @UnknownNullability Block getBlock(int x, int y, int z, @NotNull Condition condition) {
        return blocks.get(getIndex(x,y,z));
    }

    public void apply(Chunk chunk, int section) {
        blocks.forEach((index, block) -> chunk.setBlock(index >> 8, ((index >> 4) & 0xF) + section * Chunk.CHUNK_SECTION_SIZE, index & 0xF, block));
    }

    private int getIndex(int x, int y, int z) {
        return (((x << 4) | (y & 0xF)) << 4) | (z & 0xF);
    }
}