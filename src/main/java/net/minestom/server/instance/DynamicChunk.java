package net.minestom.server.instance;

import com.extollit.gaming.ai.path.model.ColumnarOcclusionFieldList;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.pathfinding.PFBlock;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.instance.heightmap.*;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.CachedPacket;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.play.ChunkDataPacket;
import net.minestom.server.network.packet.server.play.UpdateLightPacket;
import net.minestom.server.network.packet.server.play.data.ChunkData;
import net.minestom.server.network.packet.server.play.data.LightData;
import net.minestom.server.snapshot.ChunkSnapshot;
import net.minestom.server.snapshot.SnapshotImpl;
import net.minestom.server.snapshot.SnapshotUpdater;
import net.minestom.server.utils.ArrayUtils;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.world.biomes.Biome;
import net.minestom.server.world.biomes.BiomeManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static net.minestom.server.utils.chunk.ChunkUtils.toSectionRelativeCoordinate;

/**
 * Represents a {@link Chunk} which store each individual block in memory.
 * <p>
 * WARNING: not thread-safe.
 */
public class DynamicChunk extends Chunk {
    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicChunk.class);

    protected List<Section> sections;

    private boolean needsCompleteHeightmapRefresh = true;

    protected Heightmap motionBlocking = new MotionBlockingHeightmap(this);
    protected Heightmap worldSurface = new WorldSurfaceHeightmap(this);

    // Key = ChunkUtils#getBlockIndex
    protected final Int2ObjectOpenHashMap<Block> entries = new Int2ObjectOpenHashMap<>(0);
    protected final Int2ObjectOpenHashMap<Block> tickableMap = new Int2ObjectOpenHashMap<>(0);

    private long lastChange;
    final CachedPacket chunkCache = new CachedPacket(this::createChunkPacket);
    private static final BiomeManager BIOME_MANAGER = MinecraftServer.getBiomeManager();

    public DynamicChunk(@NotNull Instance instance, int chunkX, int chunkZ) {
        super(instance, chunkX, chunkZ, true);
        var sectionsTemp = new Section[maxSection - minSection];
        Arrays.setAll(sectionsTemp, value -> new Section());
        this.sections = List.of(sectionsTemp);
    }

    @Override
    public void setBlock(int x, int y, int z, @NotNull Block block,
                         @Nullable BlockHandler.Placement placement,
                         @Nullable BlockHandler.Destroy destroy) {
        if(y >= instance.getDimensionType().getMaxY() || y < instance.getDimensionType().getMinY()) {
            LOGGER.warn("tried to set a block outside the world bounds, should be within [{}, {}): {}",
                    instance.getDimensionType().getMinY(), instance.getDimensionType().getMaxY(), y);
            return;
        }
        assertLock();

        this.lastChange = System.currentTimeMillis();
        this.chunkCache.invalidate();

        // Update pathfinder
        if (columnarSpace != null) {
            final ColumnarOcclusionFieldList columnarOcclusionFieldList = columnarSpace.occlusionFields();
            final var blockDescription = PFBlock.get(block);
            columnarOcclusionFieldList.onBlockChanged(x, y, z, blockDescription, 0);
        }
        Section section = getSectionAt(y);

        int sectionRelativeX = toSectionRelativeCoordinate(x);
        int sectionRelativeZ = toSectionRelativeCoordinate(z);

        section.blockPalette().set(
                sectionRelativeX,
                toSectionRelativeCoordinate(y),
                sectionRelativeZ,
                block.stateId()
        );

        final int index = ChunkUtils.getBlockIndex(x, y, z);
        // Handler
        final BlockHandler handler = block.handler();
        final Block lastCachedBlock;
        if (handler != null || block.hasNbt() || block.registry().isBlockEntity()) {
            lastCachedBlock = this.entries.put(index, block);
        } else {
            lastCachedBlock = this.entries.remove(index);
        }
        // Block tick
        if (handler != null && handler.isTickable()) {
            this.tickableMap.put(index, block);
        } else {
            this.tickableMap.remove(index);
        }

        // Update block handlers
        var blockPosition = new Vec(x, y, z);
        if (lastCachedBlock != null && lastCachedBlock.handler() != null) {
            // Previous destroy
            lastCachedBlock.handler().onDestroy(Objects.requireNonNullElseGet(destroy,
                    () -> new BlockHandler.Destroy(lastCachedBlock, instance, blockPosition)));
        }
        if (handler != null) {
            // New placement
            final Block finalBlock = block;
            handler.onPlace(Objects.requireNonNullElseGet(placement,
                    () -> new BlockHandler.Placement(finalBlock, instance, blockPosition)));
        }

        // UpdateHeightMaps
        if (needsCompleteHeightmapRefresh) calculateFullHeightmap();
        motionBlocking.refresh(sectionRelativeX, y, sectionRelativeZ, block);
        worldSurface.refresh(sectionRelativeX, y, sectionRelativeZ, block);
    }

    @Override
    public void setBiome(int x, int y, int z, @NotNull Biome biome) {
        assertLock();
        this.chunkCache.invalidate();
        Section section = getSectionAt(y);

        var id = BIOME_MANAGER.getId(biome);
        if (id == -1) throw new IllegalStateException("Biome has not been registered: " + biome.namespace());

        section.biomePalette().set(
                toSectionRelativeCoordinate(x) / 4,
                toSectionRelativeCoordinate(y) / 4,
                toSectionRelativeCoordinate(z) / 4, id);
    }

    @Override
    public @NotNull List<Section> getSections() {
        return sections;
    }

    @Override
    public @NotNull Section getSection(int section) {
        return sections.get(section - minSection);
    }

    @Override
    public @NotNull Heightmap motionBlockingHeightmap() {
        return motionBlocking;
    }

    @Override
    public @NotNull Heightmap worldSurfaceHeightmap() {
        return worldSurface;
    }

    @Override
    public void loadHeightmapsFromNBT(NBTCompound heightmapsNBT) {
        if (heightmapsNBT.contains(motionBlockingHeightmap().NBTName())) {
            motionBlockingHeightmap().loadFrom(heightmapsNBT.getLongArray(motionBlockingHeightmap().NBTName()));
        }

        if (heightmapsNBT.contains(worldSurfaceHeightmap().NBTName())) {
            worldSurfaceHeightmap().loadFrom(heightmapsNBT.getLongArray(worldSurfaceHeightmap().NBTName()));
        }
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
        assertLock();
        if (y < minSection * CHUNK_SECTION_SIZE || y >= maxSection * CHUNK_SECTION_SIZE)
            return Block.AIR; // Out of bounds

        // Verify if the block object is present
        if (condition != Condition.TYPE) {
            final Block entry = !entries.isEmpty() ?
                    entries.get(ChunkUtils.getBlockIndex(x, y, z)) : null;
            if (entry != null || condition == Condition.CACHED) {
                return entry;
            }
        }
        // Retrieve the block from state id
        final Section section = getSectionAt(y);
        final int blockStateId = section.blockPalette()
                .get(toSectionRelativeCoordinate(x), toSectionRelativeCoordinate(y), toSectionRelativeCoordinate(z));
        return Objects.requireNonNullElse(Block.fromStateId((short) blockStateId), Block.AIR);
    }

    @Override
    public @NotNull Biome getBiome(int x, int y, int z) {
        assertLock();
        final Section section = getSectionAt(y);
        final int id = section.biomePalette()
                .get(toSectionRelativeCoordinate(x) / 4, toSectionRelativeCoordinate(y) / 4, toSectionRelativeCoordinate(z) / 4);

        Biome biome = BIOME_MANAGER.getById(id);
        if (biome == null) {
            throw new IllegalStateException("Biome with id " + id + " is not registered");
        }

        return biome;
    }

    @Override
    public long getLastChangeTime() {
        return lastChange;
    }

    @Override
    public @NotNull SendablePacket getFullDataPacket() {
        return chunkCache;
    }

    @Override
    public @NotNull Chunk copy(@NotNull Instance instance, int chunkX, int chunkZ) {
        DynamicChunk dynamicChunk = new DynamicChunk(instance, chunkX, chunkZ);
        dynamicChunk.sections = sections.stream().map(Section::clone).toList();
        dynamicChunk.entries.putAll(entries);
        return dynamicChunk;
    }

    @Override
    public void reset() {
        for (Section section : sections) section.clear();
        this.entries.clear();
    }

    @Override
    public void invalidate() {
        this.chunkCache.invalidate();
    }

    private @NotNull ChunkDataPacket createChunkPacket() {
        final byte[] data;
        final NBTCompound heightmapsNBT;
        synchronized (this) {
            heightmapsNBT = getHeightmapNBT();

            data = NetworkBuffer.makeArray(networkBuffer -> {
                for (Section section : sections) networkBuffer.write(section);
            });
        }

        return new ChunkDataPacket(chunkX, chunkZ,
                new ChunkData(heightmapsNBT, data, entries),
                createLightData()
        );
    }

    @NotNull UpdateLightPacket createLightPacket() {
        return new UpdateLightPacket(chunkX, chunkZ, createLightData());
    }

    protected LightData createLightData() {
        BitSet skyMask = new BitSet();
        BitSet blockMask = new BitSet();
        BitSet emptySkyMask = new BitSet();
        BitSet emptyBlockMask = new BitSet();
        List<byte[]> skyLights = new ArrayList<>();
        List<byte[]> blockLights = new ArrayList<>();

        int index = 0;
        for (Section section : sections) {
            index++;
            final byte[] skyLight = section.skyLight().array();
            final byte[] blockLight = section.blockLight().array();
            if (skyLight.length != 0) {
                skyLights.add(skyLight);
                skyMask.set(index);
            } else {
                emptySkyMask.set(index);
            }
            if (blockLight.length != 0) {
                blockLights.add(blockLight);
                blockMask.set(index);
            } else {
                emptyBlockMask.set(index);
            }
        }
        return new LightData(
                skyMask, blockMask,
                emptySkyMask, emptyBlockMask,
                skyLights, blockLights
        );
    }

    private NBTCompound getHeightmapNBT() {
        if (needsCompleteHeightmapRefresh) calculateFullHeightmap();
        return NBT.Compound(Map.of(
                motionBlocking.NBTName(), motionBlocking.getNBT(),
                worldSurface.NBTName(), worldSurface.getNBT()
        ));
    }

    private void calculateFullHeightmap() {
        int startY = Heightmap.getHighestBlockSection(this);

        motionBlocking.refresh(startY);
        worldSurface.refresh(startY);

        needsCompleteHeightmapRefresh = false;
    }

    @Override
    public @NotNull ChunkSnapshot updateSnapshot(@NotNull SnapshotUpdater updater) {
        Section[] clonedSections = new Section[sections.size()];
        for (int i = 0; i < clonedSections.length; i++)
            clonedSections[i] = sections.get(i).clone();
        var entities = instance.getEntityTracker().chunkEntities(chunkX, chunkZ, EntityTracker.Target.ENTITIES);
        final int[] entityIds = ArrayUtils.mapToIntArray(entities, Entity::getEntityId);
        return new SnapshotImpl.Chunk(minSection, chunkX, chunkZ,
                clonedSections, entries.clone(), entityIds, updater.reference(instance),
                tagHandler().readableCopy());
    }

    private void assertLock() {
        assert Thread.holdsLock(this) : "Chunk must be locked before access";
    }
}
