package net.minestom.server.instance;

import com.extollit.gaming.ai.path.model.ColumnarOcclusionFieldList;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.pathfinding.PFBlock;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.network.packet.server.play.ChunkDataPacket;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.world.biomes.Biome;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

/**
 * Represents a {@link Chunk} which store each individual block in memory.
 * <p>
 * WARNING: not thread-safe.
 */
public class DynamicChunk extends Chunk {

    protected final TreeMap<Integer, Section> sectionMap = new TreeMap<>();

    // Key = ChunkUtils#getBlockIndex
    protected final Int2ObjectOpenHashMap<BlockEntry> entries = new Int2ObjectOpenHashMap<>();
    protected final Int2ObjectOpenHashMap<BlockHandler> tickableMap = new Int2ObjectOpenHashMap<>();

    private long lastChangeTime;

    private SoftReference<ChunkDataPacket> cachedPacket = new SoftReference<>(null);
    private long cachedPacketTime;

    public DynamicChunk(@NotNull Instance instance, @Nullable Biome[] biomes, int chunkX, int chunkZ) {
        super(instance, biomes, chunkX, chunkZ, true);
    }

    @Override
    public void setBlock(int x, int y, int z, @NotNull Block block) {
        this.lastChangeTime = System.currentTimeMillis();
        // Update pathfinder
        if (columnarSpace != null) {
            final ColumnarOcclusionFieldList columnarOcclusionFieldList = columnarSpace.occlusionFields();
            final var blockDescription = PFBlock.get(block);
            columnarOcclusionFieldList.onBlockChanged(x, y, z, blockDescription, 0);
        }
        Section section = retrieveSection(y);
        section.setBlockAt(x, y, z, block.stateId());

        final int index = ChunkUtils.getBlockIndex(x, y, z);
        // Handler
        final BlockHandler handler = block.handler();
        final NBTCompound nbt = block.nbt();
        if (handler != null || nbt != null) {
            this.entries.put(index, new BlockEntry(handler, nbt));
        } else {
            this.entries.remove(index);
        }
        // Tickable
        if (handler != null && handler.isTickable()) {
            this.tickableMap.put(index, handler);
        } else {
            this.tickableMap.remove(index);
        }
    }

    @Override
    public @NotNull TreeMap<Integer, Section> getSections() {
        return sectionMap;
    }

    @Override
    public @NotNull Section getSection(int section) {
        return sectionMap.computeIfAbsent(section, key -> new Section());
    }

    @Override
    public void tick(long time) {
        if (tickableMap.isEmpty())
            return;
        for (var entry : tickableMap.int2ObjectEntrySet()) {
            final int index = entry.getIntKey();

            final int x = ChunkUtils.blockIndexToChunkPositionX(index);
            final int y = ChunkUtils.blockIndexToChunkPositionY(index);
            final int z = ChunkUtils.blockIndexToChunkPositionZ(index);
            final Vec blockPosition = new Vec(x, y, z);

            final Block block = getBlock(blockPosition);
            entry.getValue().tick(new BlockHandler.Tick(block, instance, blockPosition));
        }
    }

    @Override
    public @NotNull Block getBlock(int x, int y, int z) {
        final Section section = retrieveSection(y);
        final short blockStateId = section.getBlockAt(x, y, z);
        if (blockStateId == -1) {
            return Block.AIR;
        }
        Block block = Block.fromStateId(blockStateId);
        if (block == null) {
            return Block.AIR;
        }
        final int index = ChunkUtils.getBlockIndex(x, y, z);
        final var entry = entries.get(index);
        if (entry != null) {
            final BlockHandler handler = entry.handler;
            final NBTCompound nbt = entry.nbtCompound;
            if (handler != null) {
                block = block.withHandler(handler);
            }
            if (nbt != null) {
                block = block.withNbt(nbt);
            }
        }
        return block;
    }

    @Override
    public long getLastChangeTime() {
        return lastChangeTime;
    }

    @NotNull
    @Override
    public ChunkDataPacket createChunkPacket() {
        ChunkDataPacket packet = cachedPacket.get();
        if (packet != null && cachedPacketTime == getLastChangeTime()) {
            return packet;
        }
        packet = new ChunkDataPacket(getIdentifier(), getLastChangeTime());
        packet.biomes = biomes;
        packet.chunkX = chunkX;
        packet.chunkZ = chunkZ;
        packet.sections = (Map<Integer, Section>) sectionMap.clone(); // TODO deep clone
        packet.entries = entries.clone();

        this.cachedPacketTime = getLastChangeTime();
        this.cachedPacket = new SoftReference<>(packet);
        return packet;
    }

    @NotNull
    @Override
    public Chunk copy(@NotNull Instance instance, int chunkX, int chunkZ) {
        DynamicChunk dynamicChunk = new DynamicChunk(instance, biomes.clone(), chunkX, chunkZ);
        for (var entry : sectionMap.entrySet()) {
            dynamicChunk.sectionMap.put(entry.getKey(), entry.getValue().clone());
        }
        dynamicChunk.entries.putAll(entries);
        return dynamicChunk;
    }

    @Override
    public void reset() {
        this.sectionMap.values().forEach(Section::clear);
        this.entries.clear();
    }

    private @NotNull Section retrieveSection(int y) {
        final int sectionIndex = ChunkUtils.getSectionAt(y);
        return getSection(sectionIndex);
    }

    @ApiStatus.Internal
    public static class BlockEntry {
        private static final Cache<NBTCompound, NBTCompound> NBT_CACHE = Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .weakValues()
                .build();

        private final BlockHandler handler;
        private final NBTCompound nbtCompound;

        public BlockEntry(BlockHandler handler, NBTCompound nbtCompound) {
            this.handler = handler;
            this.nbtCompound = nbtCompound != null ? NBT_CACHE.get(nbtCompound, compound -> nbtCompound) : null;
        }

        public BlockHandler handler() {
            return handler;
        }

        public NBTCompound nbtCompound() {
            return nbtCompound;
        }
    }
}
