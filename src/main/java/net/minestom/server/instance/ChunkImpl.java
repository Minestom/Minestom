package net.minestom.server.instance;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.LongArrayBinaryTag;
import net.minestom.server.MinecraftServer;
import net.minestom.server.ServerFlag;
import net.minestom.server.Viewable;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.Block.Getter.Condition;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.instance.palette.Palette;
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
import net.minestom.server.tag.TagHandler;
import net.minestom.server.utils.ArrayUtils;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import static net.minestom.server.coordinate.CoordConversion.*;
import static net.minestom.server.instance.ChunkLight.checkSkyOcclusion;
import static net.minestom.server.instance.ChunkLight.relightSection;
import static net.minestom.server.instance.light.LightCompute.EMPTY_CONTENT;
import static net.minestom.server.network.NetworkBuffer.SHORT;

@NotNullByDefault
final class ChunkImpl implements Chunk {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChunkImpl.class);

    static Viewable instanceChunkView(Instance instance, int chunkX, int chunkZ) {
        final List<SharedInstance> shared = instance instanceof InstanceContainer instanceContainer ?
                instanceContainer.getSharedInstances() : List.of();
        return instance.getEntityTracker().viewable(shared, chunkX, chunkZ);
    }

    private final Instance instance;
    private final int chunkX, chunkZ;
    final DimensionType dimension;
    private final int minSection, maxSection;
    private final List<Section> sections;
    private final AtomicLong version = new AtomicLong(1);

    boolean needsCompleteHeightmapRefresh = true;
    final Heightmap motionBlocking;
    final Heightmap worldSurface;

    // Options
    final long flags;

    private volatile boolean loaded = true;
    private final Viewable viewable;

    final CachedPacket chunkCache = new CachedPacket(this::createChunkPacket);
    final CachedPacket partialLightCache = new CachedPacket(this::createLightPacket);

    // Light data
    private final ReentrantLock packetGenerationLock = new ReentrantLock();
    int highestBlock;
    private boolean doneInit = false;
    private boolean freezeInvalidation = false;
    private final AtomicInteger resendTimer = new AtomicInteger(-1);
    private int @Nullable [] occlusionMap;
    private @Nullable LightData partialLightData;
    private @Nullable LightData fullLightData;

    // Data
    private final TagHandler tagHandler = TagHandler.newHandler();

    ChunkImpl(Instance instance, int chunkX, int chunkZ, long flags, Viewable viewable, List<Section> sections) {
        this.instance = instance;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        final DimensionType dimension = instance.getCachedDimensionType();
        this.dimension = dimension;
        this.minSection = dimension.minY() / SECTION_SIZE;
        this.maxSection = (dimension.minY() + dimension.height()) / SECTION_SIZE;
        this.flags = flags;
        this.viewable = viewable;
        if (sections.size() != maxSection - minSection) {
            throw new IllegalArgumentException("Invalid sections size: " + sections.size() + ", expected: " + (maxSection - minSection));
        }
        this.sections = List.copyOf(sections);

        motionBlocking = Heightmap.motionBlocking(this);
        worldSurface = Heightmap.worldSurface(this);
    }

    ChunkImpl(Instance instance, int chunkX, int chunkZ, long flags, Viewable viewable) {
        this.instance = instance;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        final DimensionType dimension = instance.getCachedDimensionType();
        this.dimension = dimension;
        this.minSection = dimension.minY() / SECTION_SIZE;
        this.maxSection = (dimension.minY() + dimension.height()) / SECTION_SIZE;
        this.flags = flags;
        this.viewable = viewable;
        var sectionsTemp = new Section[maxSection - minSection];
        Arrays.setAll(sectionsTemp, value -> Section.section());
        this.sections = List.of(sectionsTemp);

        motionBlocking = Heightmap.motionBlocking(this);
        worldSurface = Heightmap.worldSurface(this);
    }

    @Override
    public List<Section> getSections() {
        return sections;
    }

    @Override
    public Section getSection(int section) {
        return sections.get(section - minSection);
    }

    @Override
    public Heightmap motionBlockingHeightmap() {
        return motionBlocking;
    }

    @Override
    public Heightmap worldSurfaceHeightmap() {
        return worldSurface;
    }

    @Override
    public void loadHeightmapsFromNBT(CompoundBinaryTag heightmaps) {
        if (heightmaps.get(motionBlockingHeightmap().type().name()) instanceof LongArrayBinaryTag array) {
            motionBlockingHeightmap().loadFrom(array.value());
        }
        if (heightmaps.get(worldSurfaceHeightmap().type().name()) instanceof LongArrayBinaryTag array) {
            worldSurfaceHeightmap().loadFrom(array.value());
        }
    }

    @Override
    public void tick(long time) {
        int i = 0;
        for (Section section : sections) {
            final int sectionY = i++ + minSection;
            SectionImpl impl = (SectionImpl) section;
            if (impl.tickableMap().isEmpty()) continue;
            impl.tickableMap().int2ObjectEntrySet().fastForEach(entry -> {
                final int index = entry.getIntKey();
                final Block block = entry.getValue();
                final BlockHandler handler = block.handler();
                if (handler == null) return;
                final int localX = sectionBlockIndexGetX(index);
                final int localY = sectionBlockIndexGetY(index);
                final int localZ = sectionBlockIndexGetZ(index);
                final Point blockPosition = new BlockVec(localX + chunkX * SECTION_SIZE,
                        localY + sectionY * SECTION_SIZE,
                        localZ + chunkZ * SECTION_SIZE);
                handler.tick(new BlockHandler.Tick(block, instance, blockPosition));
            });
        }

        // Lighting
        if (hasLightEngine() && doneInit && resendTimer.get() > 0) {
            if (resendTimer.decrementAndGet() == 0) {
                sendLighting();
            }
        }
    }

    void sendLighting() {
        if (!isLoaded()) return;
        sendPacketToViewers(partialLightCache);
    }

    @Override
    public SendablePacket getFullDataPacket() {
        return chunkCache;
    }

    @Override
    public Chunk copy(Instance instance, int chunkX, int chunkZ) {
        final List<Section> sections = this.sections.stream().map(Section::clone).toList();
        Viewable viewable = instanceChunkView(instance, chunkX, chunkZ);
        return new ChunkImpl(instance, chunkX, chunkZ, flags, viewable, sections);
    }

    @Override
    public void reset() {
        for (Section section : sections) section.clear();
    }

    @Override
    public Instance getInstance() {
        return instance;
    }

    @Override
    public int getChunkX() {
        return chunkX;
    }

    @Override
    public int getChunkZ() {
        return chunkZ;
    }

    @Override
    public int getMinSection() {
        return minSection;
    }

    @Override
    public int getMaxSection() {
        return maxSection;
    }

    @Override
    public boolean shouldGenerate() {
        return (flags & GENERATE_FLAG) != 0;
    }

    @Override
    public boolean isReadOnly() {
        return (flags & READONLY_FLAG) != 0;
    }

    @Override
    public boolean hasLightEngine() {
        return (flags & LIGHT_ENGINE_FLAG) != 0;
    }

    @Override
    public boolean isLoaded() {
        return loaded;
    }

    @Override
    public void unload() {
        this.loaded = false;
    }

    @Override
    public TagHandler tagHandler() {
        return tagHandler;
    }

    @Override
    public void invalidate() {
        this.version.incrementAndGet();
        for (Section section : sections) section.invalidate();
        this.needsCompleteHeightmapRefresh = true;
        this.chunkCache.invalidate();
        this.partialLightCache.invalidate();
        this.partialLightData = null;
        this.fullLightData = null;
    }

    @Override
    public boolean addViewer(Player player) {
        return viewable.addViewer(player);
    }

    @Override
    public boolean removeViewer(Player player) {
        return viewable.removeViewer(player);
    }

    @Override
    public Set<Player> getViewers() {
        return viewable.getViewers();
    }

    private ChunkDataPacket createChunkPacket() {
        final byte[] data;
        final Map<Heightmap.Type, long[]> heightmaps;
        synchronized (instance) {
            heightmaps = getHeightmaps();
            NetworkBuffer.Type<Palette> biomeSerializer = Palette.biomeSerializer(MinecraftServer.getBiomeRegistry().size());
            data = NetworkBuffer.makeArray(networkBuffer -> {
                for (Section section : sections) {
                    networkBuffer.write(SHORT, (short) section.blockPalette().count());
                    networkBuffer.write(Palette.BLOCK_SERIALIZER, section.blockPalette());
                    networkBuffer.write(biomeSerializer, section.biomePalette());
                }
            });
        }
        return new ChunkDataPacket(chunkX, chunkZ,
                new ChunkData(heightmaps, data, chunkEntries()),
                createLightData(true)
        );
    }

    UpdateLightPacket createLightPacket() {
        return new UpdateLightPacket(chunkX, chunkZ, createLightData(false));
    }

    private LightData createLightData(boolean requiredFullChunk) {
        if (hasLightEngine()) return computeLightData(requiredFullChunk);
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

    LightData computeLightData(boolean requiredFullChunk) {
        packetGenerationLock.lock();
        try {
            if (requiredFullChunk) {
                if (fullLightData != null) {
                    return fullLightData;
                }
            } else {
                if (partialLightData != null) {
                    return partialLightData;
                }
            }

            BitSet skyMask = new BitSet();
            BitSet blockMask = new BitSet();
            BitSet emptySkyMask = new BitSet();
            BitSet emptyBlockMask = new BitSet();
            List<byte[]> skyLights = new ArrayList<>();
            List<byte[]> blockLights = new ArrayList<>();

            int chunkMin = dimension.minY();
            int highestNeighborBlock = dimension.minY();
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    Chunk neighborChunk = instance.getChunk(chunkX + i, chunkZ + j);
                    if (neighborChunk == null) continue;
                    ChunkImpl impl = (ChunkImpl) neighborChunk;
                    if (impl.hasLightEngine()) {
                        impl.getOcclusionMap();
                        highestNeighborBlock = Math.max(highestNeighborBlock, impl.highestBlock);
                    }
                }
            }

            int index = 0;
            for (Section section : sections) {
                boolean wasUpdatedBlock = false;
                boolean wasUpdatedSky = false;

                if (section.blockLight().requiresUpdate()) {
                    relightSection(instance, this.chunkX, index + minSection, chunkZ, ChunkLight.LightType.BLOCK);
                    wasUpdatedBlock = true;
                } else if (requiredFullChunk || section.blockLight().requiresSend()) {
                    wasUpdatedBlock = true;
                }

                if (section.skyLight().requiresUpdate()) {
                    relightSection(instance, this.chunkX, index + minSection, chunkZ, ChunkLight.LightType.SKY);
                    wasUpdatedSky = true;
                } else if (requiredFullChunk || section.skyLight().requiresSend()) {
                    wasUpdatedSky = true;
                }

                final int sectionMinY = index * 16 + chunkMin;
                index++;
                if ((wasUpdatedSky) && this.instance.getCachedDimensionType().hasSkylight() && sectionMinY <= (highestNeighborBlock + 16)) {
                    final byte[] skyLight = section.skyLight().array();

                    if (skyLight.length != 0 && skyLight != EMPTY_CONTENT) {
                        skyLights.add(skyLight);
                        skyMask.set(index);
                    } else {
                        emptySkyMask.set(index);
                    }
                }

                if (wasUpdatedBlock) {
                    final byte[] blockLight = section.blockLight().array();

                    if (blockLight.length != 0 && blockLight != EMPTY_CONTENT) {
                        blockLights.add(blockLight);
                        blockMask.set(index);
                    } else {
                        emptyBlockMask.set(index);
                    }
                }
            }

            LightData lightData = new LightData(skyMask, blockMask,
                    emptySkyMask, emptyBlockMask,
                    skyLights, blockLights);

            if (requiredFullChunk) {
                this.fullLightData = lightData;
            } else {
                this.partialLightData = lightData;
            }


            return lightData;
        } finally {
            packetGenerationLock.unlock();
        }
    }

    private Map<Heightmap.Type, long[]> getHeightmaps() {
        if (needsCompleteHeightmapRefresh) calculateFullHeightmap();
        return Map.of(
                motionBlocking.type(), motionBlocking.getNBT(),
                worldSurface.type(), worldSurface.getNBT()
        );
    }

    void calculateFullHeightmap() {
        final int startY = Heightmap.getHighestBlockSection(this);
        this.motionBlocking.refresh(startY);
        this.worldSurface.refresh(startY);
        this.needsCompleteHeightmapRefresh = false;
    }

    void handlePlacement(int x, int y, int z, Block block,
                         @Nullable BlockHandler.Placement placement,
                         @Nullable BlockHandler.Destroy destroy) {
        if (y >= dimension.maxY() || y < dimension.minY()) {
            LOGGER.warn("tried to set a block outside the world bounds, should be within [{}, {}): {}",
                    dimension.minY(), dimension.maxY(), y);
            return;
        }

        this.chunkCache.invalidate();

        final int sectionY = globalToChunk(y);
        SectionImpl section = (SectionImpl) getSection(sectionY);
        final int localX = globalToSectionRelative(x), localY = globalToSectionRelative(y), localZ = globalToSectionRelative(z);
        section.blockPalette().set(localX, localY, localZ, block.stateId());

        // Handler
        final BlockHandler handler = block.handler();
        final Block lastCachedBlock = section.cacheBlock(localX, localY, localZ, block);

        // Update block handlers
        if (lastCachedBlock != null && lastCachedBlock.handler() != null) {
            // Previous destroy
            lastCachedBlock.handler().onDestroy(Objects.requireNonNullElseGet(destroy,
                    () -> new BlockHandler.Destroy(lastCachedBlock, instance, chunkBlockRelativeGetGlobal(localX, y, localZ, chunkX, chunkZ))));
        }
        if (handler != null) {
            // New placement
            final Block finalBlock = block;
            handler.onPlace(Objects.requireNonNullElseGet(placement,
                    () -> new BlockHandler.Placement(finalBlock, instance, chunkBlockRelativeGetGlobal(localX, y, localZ, chunkX, chunkZ))));
        }

        // UpdateHeightMaps
        if (needsCompleteHeightmapRefresh) calculateFullHeightmap();
        motionBlocking.refresh(localX, y, localZ, block);
        worldSurface.refresh(localX, y, localZ, block);

        // Light
        this.occlusionMap = null;
        // Invalidate neighbor chunks, since they can be updated by this block change
        int coordinate = globalToChunk(y);
        if (doneInit && !freezeInvalidation) {
            invalidateNeighborsSection(coordinate);
            invalidateResendDelay();
            this.partialLightCache.invalidate();
        }
    }

    // LIGHT

    void invalidateNeighborsSection(int coordinate) {
        if (freezeInvalidation) return;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                Chunk neighborChunk = instance.getChunk(chunkX + i, chunkZ + j);
                if (neighborChunk == null) continue;
                neighborChunk.invalidate();
                for (int k = -1; k <= 1; k++) {
                    if (k + coordinate < neighborChunk.getMinSection() || k + coordinate >= neighborChunk.getMaxSection())
                        continue;
                    neighborChunk.getSection(k + coordinate).blockLight().invalidate();
                    neighborChunk.getSection(k + coordinate).skyLight().invalidate();
                }
            }
        }
    }

    void invalidateResendDelay() {
        if (!doneInit || freezeInvalidation) return;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                ChunkImpl neighborChunk = (ChunkImpl) instance.getChunk(chunkX + i, chunkZ + j);
                if (neighborChunk == null) continue;
                neighborChunk.resendTimer.set(ServerFlag.SEND_LIGHT_AFTER_BLOCK_PLACEMENT_DELAY);
            }
        }
    }

    int[] getOcclusionMap() {
        if (this.occlusionMap != null) return this.occlusionMap;
        final int minY = dimension.minY();
        int[] occlusionMap = new int[CHUNK_SIZE_X * CHUNK_SIZE_Z];
        synchronized (instance) {
            highestBlock = minY - 1;
            final int startY = Heightmap.getHighestBlockSection(this) - 1;
            for (int x = 0; x < CHUNK_SIZE_X; x++) {
                for (int z = 0; z < CHUNK_SIZE_Z; z++) {
                    final int globalX = getChunkX() * SECTION_SIZE + x;
                    final int globalZ = getChunkZ() * SECTION_SIZE + z;
                    int height = startY;
                    while (height >= minY) {
                        Block block = instance.getBlock(globalX, height, globalZ, Condition.TYPE);
                        if (block != Block.AIR) highestBlock = Math.max(highestBlock, height);
                        if (checkSkyOcclusion(block)) break;
                        height--;
                    }
                    occlusionMap[z << 4 | x] = (height + 1);
                }
            }
        }
        this.occlusionMap = occlusionMap;
        return occlusionMap;
    }

    @Override
    public ChunkSnapshot updateSnapshot(SnapshotUpdater updater) {
        Section[] clonedSections = new Section[sections.size()];
        for (int i = 0; i < clonedSections.length; i++) clonedSections[i] = sections.get(i).clone();
        final Collection<Entity> entities = instance.getEntityTracker().chunkEntities(chunkX, chunkZ, EntityTracker.Target.ENTITIES);
        final int[] entityIds = ArrayUtils.mapToIntArray(entities, Entity::getEntityId);
        return new SnapshotImpl.Chunk(minSection, chunkX, chunkZ,
                clonedSections, chunkEntries(), entityIds, updater.reference(instance),
                tagHandler().readableCopy());
    }

    private Int2ObjectOpenHashMap<Block> chunkEntries() {
        Int2ObjectOpenHashMap<Block> entries = new Int2ObjectOpenHashMap<>();
        int i = 0;
        for (Section section : sections) {
            final int sectionY = i++ + minSection;
            section.entries().forEach((index, block) -> {
                final int localX = sectionBlockIndexGetX(index);
                final int localY = sectionBlockIndexGetY(index);
                final int localZ = sectionBlockIndexGetZ(index);
                final int globalIndex = chunkBlockIndex(localX, localY + sectionY * SECTION_SIZE, localZ);
                entries.put(globalIndex, block);
            });
        }
        return entries;
    }

    // Callbacks

    void onLoad() {
        doneInit = true;
    }

    void onGenerate() {
        for (int section = minSection; section < maxSection; section++) {
            getSection(section).blockLight().invalidate();
            getSection(section).skyLight().invalidate();
        }

        invalidate();

        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                Chunk neighborChunk = instance.getChunk(chunkX + i, chunkZ + j);
                if (neighborChunk == null) continue;
                ChunkImpl impl = (ChunkImpl) neighborChunk;
                if (!impl.hasLightEngine()) continue;
                if (impl.doneInit) {
                    impl.resendTimer.set(20);
                    impl.invalidate();
                    for (int section = minSection; section < maxSection; section++) {
                        impl.getSection(section).blockLight().invalidate();
                        impl.getSection(section).skyLight().invalidate();
                    }
                }
            }
        }
    }
}
