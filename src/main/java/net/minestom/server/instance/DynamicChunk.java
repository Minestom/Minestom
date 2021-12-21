package net.minestom.server.instance;

import com.extollit.gaming.ai.path.model.ColumnarOcclusionFieldList;
import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.pathfinding.PFBlock;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.network.packet.server.CachedPacket;
import net.minestom.server.network.packet.server.play.ChunkDataPacket;
import net.minestom.server.network.packet.server.play.UpdateLightPacket;
import net.minestom.server.utils.ArrayUtils;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.Utils;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.world.biomes.Biome;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a {@link Chunk} which store each individual block in memory.
 * <p>
 * WARNING: not thread-safe.
 */
public class DynamicChunk extends Chunk {

    protected final Int2ObjectAVLTreeMap<Section> sectionMap = new Int2ObjectAVLTreeMap<>();

    // Key = ChunkUtils#getBlockIndex
    protected final Int2ObjectOpenHashMap<Block> entries = new Int2ObjectOpenHashMap<>();
    protected final Int2ObjectOpenHashMap<Block> tickableMap = new Int2ObjectOpenHashMap<>();

    private long lastChange;
    private final CachedPacket chunkCache = new CachedPacket(this::createChunkPacket);
    private final CachedPacket lightCache = new CachedPacket(this::createLightPacket);

    public DynamicChunk(@NotNull Instance instance, @Nullable Biome[] biomes, int chunkX, int chunkZ) {
        super(instance, biomes, chunkX, chunkZ, true);
    }

    @Override
    public void setBlock(int x, int y, int z, @NotNull Block block) {
        this.lastChange = System.currentTimeMillis();
        this.chunkCache.invalidate();
        this.lightCache.invalidate();
        // Update pathfinder
        if (columnarSpace != null) {
            final ColumnarOcclusionFieldList columnarOcclusionFieldList = columnarSpace.occlusionFields();
            final var blockDescription = PFBlock.get(block);
            columnarOcclusionFieldList.onBlockChanged(x, y, z, blockDescription, 0);
        }
        Section section = getSection(ChunkUtils.getSectionAt(y));
        section.setBlockAt(x, y, z, block.stateId());

        final int index = ChunkUtils.getBlockIndex(x, y, z);
        // Handler
        final BlockHandler handler = block.handler();
        if (handler != null || block.hasNbt() || block.registry().isBlockEntity()) {
            this.entries.put(index, block);
        } else {
            this.entries.remove(index);
        }
        // Block tick
        if (handler != null && handler.isTickable()) {
            this.tickableMap.put(index, block);
        } else {
            this.tickableMap.remove(index);
        }
    }

    @Override
    public @NotNull Map<Integer, Section> getSections() {
        return sectionMap;
    }

    @Override
    public @NotNull Section getSection(int section) {
        return sectionMap.computeIfAbsent(section, key -> new Section());
    }

    @Override
    public void tick(long time) {
        if (tickableMap.isEmpty()) return;
        tickableMap.int2ObjectEntrySet().fastForEach(entry -> {
            final int index = entry.getIntKey();
            final Block block = entry.getValue();
            final BlockHandler handler = block.handler();
            if (handler == null) return;
            final Point blockPosition = ChunkUtils.getBlockPosition(index, chunkX, chunkZ);
            handler.tick(new BlockHandler.Tick(block, instance, blockPosition));
        });
    }

    @Override
    public @Nullable Block getBlock(int x, int y, int z, @NotNull Condition condition) {
        // Verify if the block object is present
        if (condition != Condition.TYPE) {
            final Block entry = !entries.isEmpty() ?
                    entries.get(ChunkUtils.getBlockIndex(x, y, z)) : null;
            if (entry != null || condition == Condition.CACHED) {
                return entry;
            }
        }
        // Retrieve the block from state id
        final Section section = getOptionalSection(y);
        if (section == null) return Block.AIR; // Section is unloaded
        final short blockStateId = section.getBlockAt(x, y, z);
        if (blockStateId == -1) return Block.AIR; // Section is empty
        return Objects.requireNonNullElse(Block.fromStateId(blockStateId), Block.AIR);
    }

    @Override
    public long getLastChangeTime() {
        return lastChange;
    }

    @Override
    public void sendChunk(@NotNull Player player) {
        if (!isLoaded()) return;
        player.sendPackets(lightCache, chunkCache);
    }

    @Override
    public void sendChunk() {
        if (!isLoaded()) return;
        if (getViewers().isEmpty()) return;
        sendPacketsToViewers(lightCache, chunkCache);
    }

    @NotNull
    @Override
    public Chunk copy(@NotNull Instance instance, int chunkX, int chunkZ) {
        DynamicChunk dynamicChunk = new DynamicChunk(instance, biomes.clone(), chunkX, chunkZ);
        for (var entry : sectionMap.int2ObjectEntrySet()) {
            dynamicChunk.sectionMap.put(entry.getIntKey(), entry.getValue().clone());
        }
        dynamicChunk.entries.putAll(entries);
        return dynamicChunk;
    }

    @Override
    public void reset() {
        this.sectionMap.values().forEach(Section::clear);
        this.entries.clear();
    }

    private synchronized @NotNull ChunkDataPacket createChunkPacket() {
        ChunkDataPacket packet = new ChunkDataPacket();
        packet.biomes = biomes;
        packet.chunkX = chunkX;
        packet.chunkZ = chunkZ;
        packet.sections = sectionMap.clone(); // TODO deep clone
        packet.entries = entries.clone();

        // TODO: don't hardcode heightmaps
        // Heightmap
        int dimensionHeight = getInstance().getDimensionType().getHeight();
        int[] motionBlocking = new int[16 * 16];
        int[] worldSurface = new int[16 * 16];
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                motionBlocking[x + z * 16] = 0;
                worldSurface[x + z * 16] = dimensionHeight - 1;
            }
        }
        final int bitsForHeight = MathUtils.bitsToRepresent(dimensionHeight);
        packet.heightmapsNBT = new NBTCompound()
                .setLongArray("MOTION_BLOCKING", Utils.encodeBlocks(motionBlocking, bitsForHeight))
                .setLongArray("WORLD_SURFACE", Utils.encodeBlocks(worldSurface, bitsForHeight));

        return packet;
    }

    private synchronized @NotNull UpdateLightPacket createLightPacket() {
        List<byte[]> skyLights = new ArrayList<>();
        List<byte[]> blockLights = new ArrayList<>();

        UpdateLightPacket updateLightPacket = new UpdateLightPacket();
        updateLightPacket.chunkX = getChunkX();
        updateLightPacket.chunkZ = getChunkZ();

        updateLightPacket.skyLight = skyLights;
        updateLightPacket.blockLight = blockLights;

        final var sections = getSections();
        for (var entry : sections.entrySet()) {
            final int index = entry.getKey() + 1;
            final Section section = entry.getValue();

            final var skyLight = section.getSkyLight();
            final var blockLight = section.getBlockLight();

            if (!ArrayUtils.empty(skyLight)) {
                skyLights.add(skyLight);
                updateLightPacket.skyLightMask.set(index);
            }
            if (!ArrayUtils.empty(blockLight)) {
                blockLights.add(blockLight);
                updateLightPacket.blockLightMask.set(index);
            }
        }
        return updateLightPacket;
    }

    private @Nullable Section getOptionalSection(int y) {
        final int sectionIndex = ChunkUtils.getSectionAt(y);
        return sectionMap.get(sectionIndex);
    }
}
