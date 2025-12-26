package net.minestom.server.instance;

import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.ServerFlag;
import net.minestom.server.collision.Shape;
import net.minestom.server.coordinate.CoordConversion;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.SectionVec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.instance.heightmap.Heightmap;
import net.minestom.server.instance.light.OldLight;
import net.minestom.server.instance.light.LightGenerationData;
import net.minestom.server.instance.palette.Palette;
import net.minestom.server.network.packet.server.CachedPacket;
import net.minestom.server.network.packet.server.play.UpdateLightPacket;
import net.minestom.server.network.packet.server.play.data.LightData;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import static net.minestom.server.instance.light.LightCompute.EMPTY_CONTENT;

/**
 * A chunk which supports lighting computation.
 * <p>
 * This chunk is used to compute the light data for each block.
 * <p>
 */
public class LightingChunk extends DynamicChunk {

    private static final ExecutorService pool = Executors.newWorkStealingPool();

    private int @Nullable [] occlusionMap;
    final CachedPacket partialLightCache = new CachedPacket(this::createLightPacket);
    private @Nullable LightData partialLightData;
    private @Nullable LightData fullLightData;

    private int highestBlock;
    private boolean freezeInvalidation = false;

    private final ReentrantLock packetGenerationLock = new ReentrantLock();
    private final AtomicInteger resendTimer = new AtomicInteger(-1);
    private final int resendDelay = ServerFlag.SEND_LIGHT_AFTER_BLOCK_PLACEMENT_DELAY;

    private boolean doneInit = false;

    enum LightType {
        SKY,
        BLOCK
    }

    private enum QueueType {
        INTERNAL,
        EXTERNAL
    }

    private static final Set<Key> DIFFUSE_SKY_LIGHT = Set.of(
            Block.COBWEB.key(),
            Block.ICE.key(),
            Block.HONEY_BLOCK.key(),
            Block.SLIME_BLOCK.key(),
            Block.WATER.key(),
            Block.ACACIA_LEAVES.key(),
            Block.AZALEA_LEAVES.key(),
            Block.BIRCH_LEAVES.key(),
            Block.DARK_OAK_LEAVES.key(),
            Block.FLOWERING_AZALEA_LEAVES.key(),
            Block.JUNGLE_LEAVES.key(),
            Block.CHERRY_LEAVES.key(),
            Block.OAK_LEAVES.key(),
            Block.SPRUCE_LEAVES.key(),
            Block.SPAWNER.key(),
            Block.BEACON.key(),
            Block.END_GATEWAY.key(),
            Block.CHORUS_PLANT.key(),
            Block.CHORUS_FLOWER.key(),
            Block.FROSTED_ICE.key(),
            Block.SEAGRASS.key(),
            Block.TALL_SEAGRASS.key(),
            Block.LAVA.key()
    );

    public void invalidate() {
        this.partialLightCache.invalidate();
        this.chunkCache.invalidate();
        this.partialLightData = null;
        this.fullLightData = null;
    }

    public LightingChunk(Instance instance, int chunkX, int chunkZ) {
        super(instance, chunkX, chunkZ);
    }

    protected LightingChunk(Instance instance, int chunkX, int chunkZ, List<Section> sections) {
        super(instance, chunkX, chunkZ, sections);
    }

    private boolean checkSkyOcclusion(@Nullable Block block) {
        if (block == Block.AIR || block == null) return false;
        if (DIFFUSE_SKY_LIGHT.contains(block.key())) return true;

        Shape shape = block.registry().occlusionShape();
        boolean occludesTop = Block.AIR.registry().occlusionShape().isOccluded(shape, BlockFace.TOP);
        boolean occludesBottom = Block.AIR.registry().occlusionShape().isOccluded(shape, BlockFace.BOTTOM);

        return occludesBottom || occludesTop;
    }

    public void setFreezeInvalidation(boolean freezeInvalidation) {
        this.freezeInvalidation = freezeInvalidation;
    }

    public void invalidateNeighborsSection(int coordinate) {
        if (freezeInvalidation) {
            return;
        }

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                Chunk neighborChunk = x == 0 && z == 0 ? this : instance.getChunkManager().getLoadedChunkManaged(chunkX + x, chunkZ + z);
                if (neighborChunk == null) continue;

                if (neighborChunk instanceof LightingChunk light) {
                    light.invalidate();
                }

                for (int y = -1; y <= 1; y++) {
                    if (y + coordinate < neighborChunk.getMinSection() || y + coordinate >= neighborChunk.getMaxSection())
                        continue;
                    neighborChunk.getSection(y + coordinate).blockLight().invalidate();
                    neighborChunk.getSection(y + coordinate).skyLight().invalidate();
                }
            }
        }
    }

    public void invalidateResendDelay() {
        if (!doneInit || freezeInvalidation) {
            return;
        }

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                Chunk neighborChunk = x == 0 && z == 0 ? this : instance.getChunkManager().getLoadedChunkManaged(chunkX + x, chunkZ + z);
                if (neighborChunk instanceof LightingChunk light) {
                    light.resendTimer.set(resendDelay);
                }
            }
        }
    }

    @Override
    public void setBlock(int x, int y, int z, Block block,
                         @Nullable BlockHandler.Placement placement,
                         @Nullable BlockHandler.Destroy destroy) {
        super.setBlock(x, y, z, block, placement, destroy);
        this.occlusionMap = null;

        // Invalidate neighbor chunks, since they can be updated by this block change
        int coordinate = CoordConversion.globalToChunk(y);
        if (doneInit && !freezeInvalidation) {
            invalidateNeighborsSection(coordinate);
            invalidateResendDelay();
            this.partialLightCache.invalidate();
        }
    }

    public void sendLighting() {
        if (!isLoaded() || !doneInit) return;
        sendPacketToViewers(partialLightCache);
    }

    @Override
    protected void onLoad() {
        invalidate();

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                Chunk neighborChunk = instance.getChunkManager().getLoadedChunkManaged(chunkX + x, chunkZ + z);
                if (neighborChunk instanceof LightingChunk light) {
                    if (light.doneInit) {
                        light.resendTimer.set(20);
                        light.invalidate();

                        for (int section = minSection; section < maxSection; section++) {
                            light.getSection(section).blockLight().invalidate();
                            light.getSection(section).skyLight().invalidate();
                        }
                    }
                }
            }
        }

        doneInit = true;
    }

    @Override
    public void onGenerate() {
        super.onGenerate();

        for (int section = minSection; section < maxSection; section++) {
            getSection(section).blockLight().invalidate();
            getSection(section).skyLight().invalidate();
        }

        invalidate();
    }

    // Lazy compute occlusion map
    public int[] getOcclusionMap() {
        if (this.occlusionMap != null) return this.occlusionMap;
        var occlusionMap = new int[CHUNK_SIZE_X * CHUNK_SIZE_Z];

        int minY = instance.getCachedDimensionType().minY();
        highestBlock = minY - 1;

        synchronized (this) {
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
        }

        this.occlusionMap = occlusionMap;
        return occlusionMap;
    }

    @Override
    protected LightData createLightData(boolean requiredFullChunk) {
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

            var data = new LightGenerationData.SingleChunk(this);

            BitSet skyMask = new BitSet();
            BitSet blockMask = new BitSet();
            BitSet emptySkyMask = new BitSet();
            BitSet emptyBlockMask = new BitSet();
            List<byte[]> skyLights = new ArrayList<>();
            List<byte[]> blockLights = new ArrayList<>();

            int chunkMin = data.chunkMinY();
            int highestNeighborBlock = data.chunkMinY();

            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    LightingChunk neighborChunk = data.get(chunkX + i, chunkZ + j);
                    if (neighborChunk == null) continue;

                    neighborChunk.getOcclusionMap();
                    highestNeighborBlock = Math.max(highestNeighborBlock, neighborChunk.highestBlock);
                }
            }

            int index = 0;
            for (Section section : sections) {
                boolean wasUpdatedBlock = false;
                boolean wasUpdatedSky = false;

                if (section.blockLight().requiresUpdate()) {
                    relightSection(data, index + minSection, LightType.BLOCK);
                    wasUpdatedBlock = true;
                } else if (requiredFullChunk || section.blockLight().requiresSend()) {
                    wasUpdatedBlock = true;
                }

                if (section.skyLight().requiresUpdate()) {
                    relightSection(data, index + minSection, LightType.SKY);
                    wasUpdatedSky = true;
                } else if (requiredFullChunk || section.skyLight().requiresSend()) {
                    wasUpdatedSky = true;
                }

                final int sectionMinY = index * 16 + chunkMin;
                index++;

                if ((wasUpdatedSky) && data.hasSkylight() && sectionMinY <= (highestNeighborBlock + 16)) {
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
        } catch (Throwable t) {
            t.printStackTrace();
            throw new AssertionError();
        } finally {
            packetGenerationLock.unlock();
        }
    }

    private UpdateLightPacket createLightPacket() {
        return new UpdateLightPacket(chunkX, chunkZ, createLightData(false));
    }

    @Override
    public void tick0(long time) {
        super.tick0(time);

        if (doneInit && resendTimer.get() > 0) {
            if (resendTimer.decrementAndGet() == 0) {
                sendLighting();
            }
        }
    }

    private static Set<Chunk> flushQueue(LightGenerationData data, Set<SectionVec> queue, LightType type, QueueType queueType) {
        Set<SectionVec> newQueue = ConcurrentHashMap.newKeySet();

        Set<Chunk> responseChunks = ConcurrentHashMap.newKeySet();
        List<CompletableFuture<Void>> tasks = new ArrayList<>();

        OldLight.LightLookup lightLookup = (x, y, z) -> {
            LightingChunk chunk = data.get(x, z);
            if (chunk == null) return null;
            if (y - chunk.getMinSection() < 0 || y - chunk.getMaxSection() >= 0) return null;
            final Section section = chunk.getSection(y);
            return switch (type) {
                case BLOCK -> section.blockLight();
                case SKY -> section.skyLight();
            };
        };

        OldLight.PaletteLookup paletteLookup = (x, y, z) -> {
            LightingChunk chunk = data.get(x, z);
            if (chunk == null) return null;
            if (y - chunk.getMinSection() < 0 || y - chunk.getMaxSection() >= 0) return null;
            return chunk.getSection(y).blockPalette();
        };

        for (SectionVec point : queue) {
            LightingChunk chunk = data.get(point.sectionX(), point.sectionZ());
            if (chunk == null) continue;

            Section section = chunk.getSection(point.sectionY());
            responseChunks.add(chunk);

            OldLight light = switch (type) {
                case BLOCK -> section.blockLight();
                case SKY -> section.skyLight();
            };

            final Palette blockPalette = section.blockPalette();
            CompletableFuture<Void> task = CompletableFuture.runAsync(() -> {
                try {
                    final Set<SectionVec> toAdd = switch (queueType) {
                        case INTERNAL -> light.calculateInternal(blockPalette,
                                chunk.getChunkX(), point.sectionY(), chunk.getChunkZ(),
                                chunk.getOcclusionMap(), chunk.instance.getCachedDimensionType().maxY(),
                                lightLookup);
                        case EXTERNAL -> light.calculateExternal(blockPalette,
                                OldLight.getNeighbors(data, chunk, point.sectionY()),
                                lightLookup, paletteLookup);
                    };

                    light.flip();
                    newQueue.addAll(toAdd);
                } catch (Exception e) {
                    MinecraftServer.getExceptionManager().handleException(e);
                }
            }, pool);

            tasks.add(task);
        }

        tasks.forEach(CompletableFuture::join);

        if (!newQueue.isEmpty()) {
            var newResponse = flushQueue(new LightGenerationData.ManyChunks(responseChunks), newQueue, type, QueueType.EXTERNAL);
            responseChunks.addAll(newResponse);
        }

        return responseChunks;
    }

    /**
     * Forces a relight of the specified chunks.
     * <p>
     * This method is used to force a relight of the specified chunks.
     * <p>
     * This method is thread-safe and can be called from any thread.
     *
     * @param instance the instance
     * @param chunks   the chunks to relight
     * @return the chunks which have been relighted
     */
    public static List<Chunk> relight(Instance instance, Collection<Chunk> chunks) {
        // TODO currently light generation data requires all chunks to be of the same instance.
        //  The generation code gets the instance from the chunk itself, so the instance argument
        //  here ends up being unused. Might still be sensible to keep it to signal all chunks
        //  being of the same instance.
        Set<SectionVec> sections = new HashSet<>();

        for (Chunk chunk : chunks) {
            if (!(chunk instanceof LightingChunk lighting)) continue;
            for (int sectionIndex = chunk.minSection; sectionIndex < chunk.maxSection; sectionIndex++) {
                Section section = chunk.getSection(sectionIndex);
                section.blockLight().invalidate();
                section.skyLight().invalidate();
                sections.add(new SectionVec(chunk.getChunkX(), sectionIndex, chunk.getChunkZ()));
            }
            lighting.invalidate();
        }

        var data = new LightGenerationData.ManyChunks(chunks);
        // Expand the sections to include nearby sections
        var blockSections = new HashSet<SectionVec>();
        for (SectionVec point : sections) {
            blockSections.addAll(getNearbyRequired(data, point, LightType.BLOCK));
        }

        var skySections = new HashSet<SectionVec>();
        for (SectionVec point : sections) {
            skySections.addAll(getNearbyRequired(data, point, LightType.SKY));
        }

        relight(data, blockSections, LightType.BLOCK);
        relight(data, skySections, LightType.SKY);

        var chunksToRelight = new HashSet<Chunk>();
        for (Point point : blockSections) {
            var c = data.get(point.sectionX(), point.sectionZ());
            if (c == null) continue;
            chunksToRelight.add(c);
        }

        for (Point point : skySections) {
            var c = data.get(point.sectionX(), point.sectionZ());
            if (c == null) continue;
            chunksToRelight.add(c);
        }

        return new ArrayList<>(chunksToRelight);
    }

    private static Set<SectionVec> getNearbyRequired(LightGenerationData data, SectionVec point, LightType type) {
        Set<SectionVec> collected = new HashSet<>();
        collected.add(point);

        int highestRegionPoint = data.chunkMinY() - 1;

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                LightingChunk chunkCheck = data.get(point.sectionX() + x, point.sectionZ() + z);
                if (chunkCheck == null) continue;
                // Ensure heightmap is calculated before taking values from it
                chunkCheck.getOcclusionMap();
                highestRegionPoint = Math.max(highestRegionPoint, chunkCheck.highestBlock);
            }
        }

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                LightingChunk chunkCheck = data.get(point.sectionX() + x, point.sectionZ() + z);
                if (chunkCheck == null) continue;

                for (int y = point.sectionY() - 1; y <= point.sectionY() + 1; y++) {
                    SectionVec sectionPosition = new SectionVec(point.sectionX() + x, y, point.sectionZ() + z);
                    int sectionHeight = data.chunkMinY() + 16 * y;
                    if ((sectionHeight + 16) > highestRegionPoint && type == LightType.SKY) continue;

                    if (sectionPosition.sectionY() < chunkCheck.getMaxSection() && sectionPosition.sectionY() >= chunkCheck.getMinSection()) {
                        Section s = chunkCheck.getSection(sectionPosition.sectionY());
                        if (type == LightType.BLOCK && !s.blockLight().requiresUpdate()) continue;
                        if (type == LightType.SKY && !s.skyLight().requiresUpdate()) continue;

                        collected.add(sectionPosition);
                    }
                }
            }
        }

        return collected;
    }

    private static Set<SectionVec> collectRequiredNearby(LightGenerationData data, SectionVec point, LightType type) {
        final Set<SectionVec> found = new HashSet<>();
        final ArrayDeque<SectionVec> toCheck = new ArrayDeque<>();

        toCheck.add(point);
        found.add(point);

        while (!toCheck.isEmpty()) {
            final SectionVec current = toCheck.poll();
            final Set<SectionVec> nearby = getNearbyRequired(data, current, type);
            nearby.forEach(p -> {
                if (!found.contains(p)) {
                    found.add(p);
                    toCheck.add(p);
                }
            });
        }
        return found;
    }

    static Set<Chunk> relightSection(Instance instance, int chunkX, int sectionY, int chunkZ) {
        Chunk c = instance.getChunkManager().getLoadedChunkManaged(chunkX, chunkZ);
        if (c == null) return Set.of();
        if (!(c instanceof LightingChunk l)) return Set.of();
        var data = new LightGenerationData.SingleChunk(l);
        var res = new HashSet<>(l.relightSection(data, sectionY, LightType.BLOCK));
        res.addAll(l.relightSection(data, sectionY, LightType.SKY));
        return res;
    }

    void relightSection(int sectionY) {
        var data = new LightGenerationData.SingleChunk(this);
        relightSection(data, sectionY, LightType.SKY);
        relightSection(data, sectionY, LightType.BLOCK);
    }

    private Set<Chunk> relightSection(LightGenerationData data, int sectionY, LightType type) {
        Set<SectionVec> collected = collectRequiredNearby(data, new SectionVec(chunkX, sectionY, chunkZ), type);
        return relight(data, collected, type);
    }

    private static Set<Chunk> relight(LightGenerationData data, Set<SectionVec> queue, LightType type) {
        return flushQueue(data, queue, type, QueueType.INTERNAL);
    }

    @Override
    public Chunk copy(Instance instance, int chunkX, int chunkZ) {
        var sections = this.sections.stream().map(Section::clone).toList();
        LightingChunk lightingChunk = new LightingChunk(instance, chunkX, chunkZ, sections);
        lightingChunk.entries.putAll(entries);
        return lightingChunk;
    }
}
