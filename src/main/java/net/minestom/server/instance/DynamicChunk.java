package net.minestom.server.instance;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.LongArrayBinaryTag;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.CoordConversion;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.instance.heightmap.Heightmap;
import net.minestom.server.instance.heightmap.MotionBlockingHeightmap;
import net.minestom.server.instance.heightmap.WorldSurfaceHeightmap;
import net.minestom.server.instance.palette.Palette;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.CachedPacket;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.play.ChunkDataPacket;
import net.minestom.server.network.packet.server.play.UpdateLightPacket;
import net.minestom.server.network.packet.server.play.data.ChunkData;
import net.minestom.server.network.packet.server.play.data.LightData;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.snapshot.ChunkSnapshot;
import net.minestom.server.snapshot.SnapshotImpl;
import net.minestom.server.snapshot.SnapshotUpdater;
import net.minestom.server.utils.ArrayUtils;
import net.minestom.server.utils.validate.Check;
import net.minestom.server.world.DimensionType;
import net.minestom.server.world.biome.Biome;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static net.minestom.server.coordinate.CoordConversion.globalToSectionRelative;
import static net.minestom.server.network.NetworkBuffer.SHORT;

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
    private static final DynamicRegistry<Biome> BIOME_REGISTRY = MinecraftServer.getBiomeRegistry();

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
        final DimensionType instanceDim = instance.getCachedDimensionType();
        if (y >= instanceDim.maxY() || y < instanceDim.minY()) {
            LOGGER.warn("tried to set a block outside the world bounds, should be within [{}, {}): {}",
                    instanceDim.minY(), instanceDim.maxY(), y);
            return;
        }
        assertLock();

        this.lastChange = System.currentTimeMillis();
        this.chunkCache.invalidate();

        Section section = getSectionAt(y);

        int sectionRelativeX = globalToSectionRelative(x);
        int sectionRelativeZ = globalToSectionRelative(z);

        section.blockPalette().set(
                sectionRelativeX,
                globalToSectionRelative(y),
                sectionRelativeZ,
                block.stateId()
        );

        final int index = CoordConversion.chunkBlockIndex(x, y, z);
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

            var absoluteBlockPosition = new Vec(getChunkX() * 16 + x, y, getChunkZ() * 16 + z);
            final Block finalBlock = block;
            handler.onPlace(Objects.requireNonNullElseGet(placement,
                    () -> new BlockHandler.Placement(finalBlock, instance, absoluteBlockPosition)));
        }

        // UpdateHeightMaps
        if (needsCompleteHeightmapRefresh) calculateFullHeightmap();
        motionBlocking.refresh(sectionRelativeX, y, sectionRelativeZ, block);
        worldSurface.refresh(sectionRelativeX, y, sectionRelativeZ, block);
    }

    @Override
    public void setBiome(int x, int y, int z, @NotNull DynamicRegistry.Key<Biome> biome) {
        assertLock();
        this.chunkCache.invalidate();
        Section section = getSectionAt(y);

        var id = BIOME_REGISTRY.getId(biome.namespace());
        if (id == -1) throw new IllegalStateException("Biome has not been registered: " + biome.namespace());

        section.biomePalette().set(
                globalToSectionRelative(x) / 4,
                globalToSectionRelative(y) / 4,
                globalToSectionRelative(z) / 4, id);
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
    public void loadHeightmapsFromNBT(CompoundBinaryTag heightmapsNBT) {
        if (heightmapsNBT.get(motionBlockingHeightmap().NBTName()) instanceof LongArrayBinaryTag array) {
            motionBlockingHeightmap().loadFrom(array.value());
        }

        if (heightmapsNBT.get(worldSurfaceHeightmap().NBTName()) instanceof LongArrayBinaryTag array) {
            worldSurfaceHeightmap().loadFrom(array.value());
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
            final Point blockPosition = CoordConversion.chunkBlockIndexGetGlobal(index, chunkX, chunkZ);
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
                    entries.get(CoordConversion.chunkBlockIndex(x, y, z)) : null;
            if (entry != null || condition == Condition.CACHED) {
                return entry;
            }
        }
        // Retrieve the block from state id
        final Section section = getSectionAt(y);
        final int blockStateId = section.blockPalette()
                .get(globalToSectionRelative(x), globalToSectionRelative(y), globalToSectionRelative(z));
        return Objects.requireNonNullElse(Block.fromStateId((short) blockStateId), Block.AIR);
    }

    @Override
    public @NotNull DynamicRegistry.Key<Biome> getBiome(int x, int y, int z) {
        assertLock();
        final Section section = getSectionAt(y);
        final int id = section.biomePalette()
                .get(globalToSectionRelative(x) / 4, globalToSectionRelative(y) / 4, globalToSectionRelative(z) / 4);

        DynamicRegistry.Key<Biome> biome = BIOME_REGISTRY.getKey(id);
        Check.notNull(biome, "Biome with id {0} is not registered", id);
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
        final CompoundBinaryTag heightmapsNBT;
        synchronized (this) {
            heightmapsNBT = getHeightmapNBT();

            data = NetworkBuffer.makeArray(networkBuffer -> {
                for (Section section : sections) {
                    networkBuffer.write(SHORT, (short) section.blockPalette().count());
                    networkBuffer.write(Palette.BLOCK_SERIALIZER, section.blockPalette());
                    networkBuffer.write(Palette.BIOME_SERIALIZER, section.biomePalette());
                }
            });
        }

        return new ChunkDataPacket(chunkX, chunkZ,
                new ChunkData(heightmapsNBT, data, entries),
                createLightData(true)
        );
    }

    @NotNull UpdateLightPacket createLightPacket() {
        return new UpdateLightPacket(chunkX, chunkZ, createLightData(false));
    }

    protected LightData createLightData(boolean requiredFullChunk) {
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

    protected CompoundBinaryTag getHeightmapNBT() {
        if (needsCompleteHeightmapRefresh) calculateFullHeightmap();
        return CompoundBinaryTag.builder()
                .putLongArray(motionBlocking.NBTName(), motionBlocking.getNBT())
                .putLongArray(worldSurface.NBTName(), worldSurface.getNBT())
                .build();
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
