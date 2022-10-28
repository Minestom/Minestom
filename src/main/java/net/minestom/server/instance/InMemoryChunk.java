package net.minestom.server.instance;

import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteList;
import it.unimi.dsi.fastutil.bytes.ByteLists;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMaps;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import net.minestom.server.entity.pathfinding.PFColumnarSpace;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.ChunkDataPacket;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.world.biomes.Biome;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A mutable, non-thread-safe, in-memory chunk impl.
 */
class InMemoryChunk implements Chunk {

    private final Int2ObjectMap<Section> sections = new Int2ObjectOpenHashMap<>();

    public InMemoryChunk() {
    }

    @Override
    public @NotNull Section getSectionAt(int blockY) {
        return getSection(ChunkUtils.toSectionRelativeCoordinate(blockY));
    }

    @Override
    public @NotNull Section getSection(int sectionY) {
        return sections.computeIfAbsent(sectionY, s -> Section.inMemory());
    }

    @Override
    public void setBlock(int x, int y, int z, @NotNull Block block) {
        getSectionAt(y).setBlock(x, y, z, block);
    }

    @Override
    public void reset() {
        sections.forEach((y, section) -> section.clear());
    }

    @Override
    public int getMinSection() {
        return sections.keySet().intStream().min().orElse(0);
    }

    @Override
    public int getMaxSection() {
        return sections.keySet().intStream().max().orElse(0);
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        throw new UnsupportedOperationException("InMemoryChunk is always mutable");
    }

    @Override
    public void setColumnarSpace(PFColumnarSpace columnarSpace) {
        throw new UnsupportedOperationException("InMemoryChunk does not support pathfinding");
    }

    @Override
    public boolean isLoaded() {
        return true;
    }

    @Override
    public CompletableFuture<Void> unload() {
        throw new UnsupportedOperationException("InMemoryChunk cannot be unloaded");
    }

    @Override
    public ByteList getSkyLight(int sectionY) {
        return getSection(sectionY).getSkyLight();
    }

    @Override
    public ByteList getBlockLight(int sectionY) {
        return getSection(sectionY).getBlockLight();
    }

    @Override
    public void setSkyLight(int sectionY, ByteList light) {
        getSection(sectionY).setSkyLight(light);
    }

    @Override
    public void setBlockLight(int sectionY, ByteList light) {
        getSection(sectionY).setBlockLight(light);
    }

    @Override
    public @UnknownNullability Block getBlock(int x, int y, int z, @NotNull Condition condition) {
        return getSectionAt(y).getBlock(x, y, z, condition);
    }

    @Override
    public void setBiome(int x, int y, int z, @NotNull Biome biome) {
        getSectionAt(y).setBiome(x, y, z, biome);
    }

    @Override
    public @NotNull Biome getBiome(int x, int y, int z) {
        return getSectionAt(y).getBiome(x, y, z);
    }
}
