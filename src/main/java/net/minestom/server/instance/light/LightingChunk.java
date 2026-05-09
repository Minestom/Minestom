package net.minestom.server.instance.light;

import net.kyori.adventure.key.Key;
import net.minestom.server.collision.Shape;
import net.minestom.server.coordinate.CoordConversion;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.DynamicChunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.Section;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.instance.heightmap.Heightmap;
import net.minestom.server.instance.light.snapshot.SnapshotLightSectionType;
import net.minestom.server.network.packet.server.CachedPacket;
import net.minestom.server.network.packet.server.play.UpdateLightPacket;
import net.minestom.server.network.packet.server.play.data.LightData;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static net.minestom.server.coordinate.CoordConversion.globalToSectionRelative;

/* TODO
    send light data as part of a ChunkBatch to measure time elapsed as not to take up too much bandwidth.
    Otherwise 32 chunk view distance with even just a few sections per chunk of data can destroy network throughput.
    This would be better in a separate PR though, let's first confirm correctness.
 */
public class LightingChunk extends DynamicChunk {
    private static final LightEngine LIGHT_ENGINE = LightEngine.getDefault();
    // A reusable WeakReference to reduce allocations.
    private final WeakReference<@Nullable LightingChunk> selfReference = new WeakReference<>(this);
    private final int minLightSection;
    private final int maxLightSection;
    private int highestBlock;
    private int @Nullable [] occlusionMap;
    private volatile boolean resendLight = false;
    // The following 4 WeakReferences are read-only
    private final Map<Neighbors, WeakReference<@Nullable LightingChunk>> neighbors = new ConcurrentHashMap<>();
    private volatile boolean mayRequireSpecificSectionResend = false;
    private final AtomicInteger resendSpecificAfter = new AtomicInteger(); // TODO
    private final Typed<?, ?, ?> typed;
    private volatile boolean neighborUpdated = false;

    private static final Set<Key> DIFFUSE_SKY_LIGHT = Set.of(Block.COBWEB.key(), Block.ICE.key(), Block.HONEY_BLOCK.key(), Block.SLIME_BLOCK.key(), Block.WATER.key(), Block.ACACIA_LEAVES.key(), Block.AZALEA_LEAVES.key(), Block.BIRCH_LEAVES.key(), Block.DARK_OAK_LEAVES.key(), Block.FLOWERING_AZALEA_LEAVES.key(), Block.JUNGLE_LEAVES.key(), Block.CHERRY_LEAVES.key(), Block.OAK_LEAVES.key(), Block.SPRUCE_LEAVES.key(), Block.SPAWNER.key(), Block.BEACON.key(), Block.END_GATEWAY.key(), Block.CHORUS_PLANT.key(), Block.CHORUS_FLOWER.key(), Block.FROSTED_ICE.key(), Block.SEAGRASS.key(), Block.TALL_SEAGRASS.key(), Block.LAVA.key());

    private static boolean checkSkyOcclusion(@Nullable Block block) {
        if (block == Block.AIR || block == null) return false;
        if (DIFFUSE_SKY_LIGHT.contains(block.key())) return true;

        Shape shape = block.registry().occlusionShape();
        boolean occludesTop = Block.AIR.registry().occlusionShape().isOccluded(shape, BlockFace.TOP);
        boolean occludesBottom = Block.AIR.registry().occlusionShape().isOccluded(shape, BlockFace.BOTTOM);

        return occludesBottom || occludesTop;
    }

    private static class Typed<Type extends LightSectionType<LSection, ChunkData>, LSection extends LightSection<LSection, Type, ChunkData>, ChunkData> {
        private final Type type;
        private final ChunkData chunkData;
        private final List<LSection> lightSections;

        public Typed(LightingChunk chunk, Type type) {
            this.type = type;
            this.chunkData = type.newChunkData(chunk);
            this.lightSections = initSections(chunk);
        }

        private void fullRelightSync() {
            type.fullRelightSync(lightSections);
        }

        private List<LSection> initSections(LightingChunk chunk) {
            var sectionsCount = chunk.maxLightSection - chunk.minLightSection;
            var sectionsTemp = new ArrayList<LSection>(sectionsCount);
            for (var i = 0; i < sectionsCount; i++) {
                var first = i == 0;
                var last = i == sectionsCount - 1;
                var section = first | last ? null : chunk.getSection(i + chunk.minLightSection);
                sectionsTemp.add(type.newSection(chunkData, section, i + chunk.minLightSection));
            }
            LSection below = null;
            LSection self = null;
            for (var i = 0; i < sectionsCount; i++) {
                var above = sectionsTemp.get(i);

                if (self != null) {
                    self.initAboveBelow(above, below);
                }

                below = self;
                self = above;
            }
            Objects.requireNonNull(self).initAboveBelow(null, below);
            return List.copyOf(sectionsTemp);
        }

        private LightingChunk copy(LightingChunk origin, Instance instance, int chunkX, int chunkZ) {
            var sections = origin.sections.stream().map(Section::clone).toList();
            LightingChunk lightingChunk = new LightingChunk(instance, chunkX, chunkZ, sections, c -> {
                var typed = new Typed<>(c, type);
                for (int i = 0; i < typed.lightSections.size(); i++) {
                    typed.lightSections.get(i).copyFrom(lightSections.get(i));
                }
                return typed;
            });
            lightingChunk.entries.putAll(origin.entries);
            return lightingChunk;
        }

    }

    private LightingChunk(Instance instance, int chunkX, int chunkZ, List<Section> sections, Function<LightingChunk, Typed<?, ?, ?>> typed) {
        super(instance, chunkX, chunkZ, sections);
        this.minLightSection = minSection - 1;
        this.maxLightSection = maxSection + 1;
        this.typed = typed.apply(this);
    }

    public LightingChunk(Instance instance, int chunkX, int chunkZ) {
        this(instance, chunkX, chunkZ, c -> new Typed<>(c, SnapshotLightSectionType.TYPE));
    }

    private LightingChunk(Instance instance, int chunkX, int chunkZ, Function<LightingChunk, Typed<?, ?, ?>> typed) {
        super(instance, chunkX, chunkZ);
        this.minLightSection = minSection - 1;
        this.maxLightSection = maxSection + 1;
        this.typed = typed.apply(this);
    }

    public int getHighestBlock() {
        assert holdsReadLock();
        return highestBlock;
    }

    // Lazy compute occlusion map
    public int[] getOcclusionMap() {
        assert holdsReadLock();
        if (this.occlusionMap != null) return this.occlusionMap;

        int minY = instance.getCachedDimensionType().minY();
        highestBlock = minY - 1;

        // Only read-locked. We could race with other callers of getOcclusionMap,
        // but that's not an issue since we are just updating a field. The field doesn't even need to be volatile
        var occlusionMap = new int[CHUNK_SIZE_X * CHUNK_SIZE_Z];
        int startY = Heightmap.getHighestBlockSection(this);

        for (int x = 0; x < CHUNK_SIZE_X; x++) {
            for (int z = 0; z < CHUNK_SIZE_Z; z++) {
                int height = startY;
                while (height >= minY) {
                    Block block = getBlock(x, height, z, Condition.TYPE);
                    if (block != Block.AIR) highestBlock = Math.max(highestBlock, height);
                    if (checkSkyOcclusion(block)) break;
                    height--;
                }
                occlusionMap[z << 4 | x] = (height + 1);
            }
        }

        this.occlusionMap = occlusionMap;
        return occlusionMap;
    }

    /**
     * Gets a snapshot of all neighboring chunks. Chunks will be null if not found.
     * Chunks may even be found if they are not loaded (yet or anymore).
     * <p>
     * This will be called by lighting code quite a few times. Do not try to pass this as an
     * argument to the lighting code, this will create a race condition.
     * General rule is: All computation data must be fetched after a new version ID has been acquired.
     * Passing a NeighborSnapshot would imply fetching the snapshot before the version ID, which is illegal.
     */
    public NeighborSnapshot createNeighborSnapshot() {
        // It is important that we do not set the WeakReference fields to null if they do not hold a value anymore.
        // This is because of possible race conditions if another neighbor were to load, populating the field again.
        if (neighbors.isEmpty()) return new NeighborSnapshot(Map.of());

        var neighbors = new EnumMap<Neighbors, LightingChunk>(Neighbors.class);
        for (var e : this.neighbors.entrySet()) {
            var ref = e.getValue();
            var chunk = ref.get();
            if (chunk == null) continue;
            neighbors.put(e.getKey(), chunk);
        }
        return new NeighborSnapshot(Map.copyOf(neighbors));
    }

    @Override
    public void setBlock(int x, int y, int z, Block block, BlockHandler.@Nullable Placement placement, BlockHandler.@Nullable Destroy destroy) {
        assertWriteLock();
        super.setBlock(x, y, z, block, placement, destroy);
        occlusionMap = null;
        var sectionY = CoordConversion.globalToSection(y);

        var section = getLightSection(sectionY);
        var relativeX = globalToSectionRelative(x);
        var relativeY = globalToSectionRelative(y);
        var relativeZ = globalToSectionRelative(z);
        section.blockChanged(relativeX, relativeY, relativeZ);
    }

    @Override
    public void onLoadManaged() {
        // We need to announce to our neighbors that we are loaded now.
        // This connects us with the neighbors and invalidates their external lighting for us.
        // This connection stays up until chunks are unloaded.
        // onLoadManaged is an ideal place because of its threading implications,
        // everything inside an instance will be sequential. For future development of the
        // chunk management system, this might change and new solutions might be required.
        announceNeighborLoad(Neighbors.NORTH);
        announceNeighborLoad(Neighbors.EAST);
        announceNeighborLoad(Neighbors.SOUTH);
        announceNeighborLoad(Neighbors.WEST);
        announceNeighborLoad(Neighbors.NORTH_WEST);
        announceNeighborLoad(Neighbors.SOUTH_WEST);
        announceNeighborLoad(Neighbors.NORTH_EAST);
        announceNeighborLoad(Neighbors.SOUTH_EAST);
    }

    private void announceNeighborLoad(Neighbors directionFace) {
        // This is a synchronization point. See #onLoadManaged()
        var neighbor = this.instance.getChunkManager().getLoadedChunkManaged(chunkX + directionFace.x(), chunkZ + directionFace.z());
        if (neighbor == null) return; // No neighbor
        if (neighbor instanceof LightingChunk lightingChunk) {
            lightingChunk.receiveNeighborLoad(this, directionFace.opposite());
            receiveNeighborLoad(lightingChunk, directionFace);
        }
    }

    private void receiveNeighborLoad(LightingChunk neighbor, Neighbors origin) {
        // This is a synchronization point. See #onLoadManaged()
        neighbors.put(origin, neighbor.selfReference);
        // Neighbor has updated. We now invalidate the external lighting
        // We want to keep work on chunk management thread to a minimum, so we delegate via flag
        neighborUpdated = true;
    }

    @Override
    protected LightData createLightData() {
        var builder = new LightDataBuilder(this);

        for (var section : typed.lightSections) {
            builder.beginSection();
            builder.blockLight(section.getBlockLight());
            builder.skyLight(section.getSkyLight());
            builder.endSection();
        }

        return builder.build();
    }

    protected LightData createPartialLightData(BitSet targetBlockSections, BitSet targetSkySections) {
        var builder = new LightDataBuilder(this);

        for (var i = 0; i < typed.lightSections.size(); i++) {
            var section = typed.lightSections.get(i);
            builder.beginSection();
            if (targetBlockSections.get(i)) {
                builder.blockLight(section.getBlockLight());
            }
            if (targetSkySections.get(i)) {
                builder.skyLight(section.getSkyLight());
            }
            builder.endSection();
        }

        return builder.build();
    }

    @Override
    public void tick0(long time) {
        assertWriteLock();
        super.tick0(time);

        if (resendLight) {
            resendLight = false;
            mayRequireSpecificSectionResend = false;
            chunkCache.invalidate();
            sendLight();
        } else if (mayRequireSpecificSectionResend && resendSpecificAfter.getAndDecrement() == 0) {
            mayRequireSpecificSectionResend = false;
            chunkCache.invalidate();
            sendPartialLight();
        }
        if (neighborUpdated) {
            neighborUpdated = false;
            neighborLoadUnloadDetected();
        }
    }

    private void neighborLoadUnloadDetected() {
        for (var lightSection : typed.lightSections) {
            lightSection.neighborLoadUnloadDetected();
        }
    }

    @Override
    public void onGenerate() {
        super.onGenerate();
        typed.fullRelightSync();
    }

    @Override
    public void onReloadFromMemoryCopy(Chunk oldInMemoryChunk) {
        super.onReloadFromMemoryCopy(oldInMemoryChunk);
        // TODO Internal light should already be valid because of #copy(...)
        //  Only external light missing. External in this case is still bound to the chunk.
        //  So only vertical sections pass to each other, no neighbor chunks are known yet.
        typed.fullRelightSync();
    }

    @Override
    public void onLoadedFromStorage() {
        super.onLoadedFromStorage();
        // TODO we may want to recalculate light if the loader doesn't store/read light data.
        //  for now we assume the loader supports light
    }

    public void scheduleSpecificResend() {
        resendSpecificAfter.set(20); // Resend after 1 ticks
        mayRequireSpecificSectionResend = true;
    }

    public void scheduleFullResend() {
        resendLight = true;
    }

    @Override
    public Chunk copy(Instance instance, int chunkX, int chunkZ) {
        return typed.copy(this, instance, chunkX, chunkZ);
    }

    /**
     * Sends a full light update to all viewers.
     * <p>
     * Threadsafe
     */
    public void sendLight() {
        // CachedPacket here is an optimization to do the actual packet creation work on the network thread
        sendPacketToViewers(new CachedPacket(() -> new UpdateLightPacket(chunkX, chunkZ, createLightData())));
    }

    /**
     * Sends a light update of all modified sections to all viewers.
     * Also clears the section modification status.
     * <p>
     * Threadsafe
     */
    public void sendPartialLight() {
        var targetBlockSections = new BitSet();
        var targetSkySections = new BitSet();

        // This must be done here, because the supplier in CachedPacket may be evaluated multiple times,
        // and the AtomicBooleans would not be true the second time.
        for (var i = 0; i < typed.lightSections.size(); i++) {
            var section = typed.lightSections.get(i);
            if (section.getAndResetResendBlockLight()) {
                targetBlockSections.set(i);
            }
            if (section.getAndResetResendSkyLight()) {
                targetSkySections.set(i);
            }
        }

        // CachedPacket here is an optimization to do the actual packet creation work on the network thread
        sendPacketToViewers(new CachedPacket(() -> new UpdateLightPacket(chunkX, chunkZ, createPartialLightData(targetBlockSections, targetSkySections))));
    }

    @ApiStatus.Experimental
    public LightSection<?, ?, ?> getLightSection(int sectionY) {
        return typed.lightSections.get(sectionY - minLightSection);
    }

    public void awaitLight() {
        LIGHT_ENGINE.awaitRunning();
    }

    public LightEngine engine() {
        return LIGHT_ENGINE;
    }

    @Override
    public int getBlockLight(int blockX, int blockY, int blockZ) {
        var sectionY = CoordConversion.globalToSection(blockY);
        var relX = globalToSectionRelative(blockX);
        var relY = globalToSectionRelative(blockY);
        var relZ = globalToSectionRelative(blockZ);
        var section = getLightSection(sectionY);
        awaitLight();
        return section.getBlockLight(relX, relY, relZ);
    }

    @Override
    public int getSkyLight(int blockX, int blockY, int blockZ) {
        var sectionY = CoordConversion.globalToSection(blockY);
        var relX = globalToSectionRelative(blockX);
        var relY = globalToSectionRelative(blockY);
        var relZ = globalToSectionRelative(blockZ);
        var section = getLightSection(sectionY);
        awaitLight();
        return section.getSkyLight(relX, relY, relZ);
    }
}
