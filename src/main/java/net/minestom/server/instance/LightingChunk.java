package net.minestom.server.instance;

import net.minestom.server.MinecraftServer;
import net.minestom.server.ServerFlag;
import net.minestom.server.collision.Shape;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.instance.light.Light;
import net.minestom.server.network.packet.server.CachedPacket;
import net.minestom.server.network.packet.server.play.data.LightData;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import static net.minestom.server.instance.light.LightCompute.emptyContent;

/**
 * A chunk which supports lighting computation.
 * <p>
 *     This chunk is used to compute the light data for each block.
 * <p>
 */
public class LightingChunk extends DynamicChunk {

    private static final ExecutorService pool = Executors.newWorkStealingPool();

    private int[] heightmap;
    final CachedPacket lightCache = new CachedPacket(this::createLightPacket);
    private LightData lightData;

    boolean chunkLoaded = false;
    private int highestBlock;

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

    private static final Set<NamespaceID> DIFFUSE_SKY_LIGHT = Set.of(
            Block.COBWEB.namespace(),
            Block.ICE.namespace(),
            Block.HONEY_BLOCK.namespace(),
            Block.SLIME_BLOCK.namespace(),
            Block.WATER.namespace(),
            Block.ACACIA_LEAVES.namespace(),
            Block.AZALEA_LEAVES.namespace(),
            Block.BIRCH_LEAVES.namespace(),
            Block.DARK_OAK_LEAVES.namespace(),
            Block.FLOWERING_AZALEA_LEAVES.namespace(),
            Block.JUNGLE_LEAVES.namespace(),
            Block.CHERRY_LEAVES.namespace(),
            Block.OAK_LEAVES.namespace(),
            Block.SPRUCE_LEAVES.namespace(),
            Block.SPAWNER.namespace(),
            Block.BEACON.namespace(),
            Block.END_GATEWAY.namespace(),
            Block.CHORUS_PLANT.namespace(),
            Block.CHORUS_FLOWER.namespace(),
            Block.FROSTED_ICE.namespace(),
            Block.SEAGRASS.namespace(),
            Block.TALL_SEAGRASS.namespace(),
            Block.LAVA.namespace()
    );

    public void invalidate() {
        this.lightCache.invalidate();
        this.chunkCache.invalidate();
        this.lightData = null;
    }

    public LightingChunk(@NotNull Instance instance, int chunkX, int chunkZ) {
        super(instance, chunkX, chunkZ);
    }

    private boolean checkSkyOcclusion(Block block) {
        if (block == Block.AIR) return false;
        if (DIFFUSE_SKY_LIGHT.contains(block.namespace())) return true;

        Shape shape = block.registry().collisionShape();
        boolean occludesTop = Block.AIR.registry().collisionShape().isOccluded(shape, BlockFace.TOP);
        boolean occludesBottom = Block.AIR.registry().collisionShape().isOccluded(shape, BlockFace.BOTTOM);

        return occludesBottom || occludesTop;
    }

    private void invalidateSection(int coordinate) {
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                Chunk neighborChunk = instance.getChunk(chunkX + i, chunkZ + j);
                if (neighborChunk == null) continue;

                if (neighborChunk instanceof LightingChunk light) {
                    light.invalidate();
                }

                for (int k = -1; k <= 1; k++) {
                    if (k + coordinate < neighborChunk.getMinSection() || k + coordinate >= neighborChunk.getMaxSection()) continue;
                    neighborChunk.getSection(k + coordinate).blockLight().invalidate();
                    neighborChunk.getSection(k + coordinate).skyLight().invalidate();
                }
            }
        }
    }

    @Override
    public void setBlock(int x, int y, int z, @NotNull Block block,
                         @Nullable BlockHandler.Placement placement,
                         @Nullable BlockHandler.Destroy destroy) {
        super.setBlock(x, y, z, block, placement, destroy);
        this.heightmap = null;

        // Invalidate neighbor chunks, since they can be updated by this block change
        int coordinate = ChunkUtils.getChunkCoordinate(y);
        if (chunkLoaded) {
            invalidateSection(coordinate);
            this.lightCache.invalidate();

            if (doneInit) {
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        Chunk neighborChunk = instance.getChunk(chunkX + i, chunkZ + j);
                        if (neighborChunk == null) continue;

                        if (neighborChunk instanceof LightingChunk light) {
                            light.resendTimer.set(resendDelay);
                        }
                    }
                }
            }
        }
    }

    public void sendLighting() {
        if (!isLoaded()) return;
        sendPacketToViewers(lightCache);
    }

    @Override
    protected void onLoad() {
        chunkLoaded = true;
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

        MinecraftServer.getSchedulerManager().scheduleNextTick(() -> {
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    Chunk neighborChunk = instance.getChunk(chunkX + i, chunkZ + j);
                    if (neighborChunk == null) continue;

                    if (neighborChunk instanceof LightingChunk light) {
                        for (int section = light.minSection; section < light.maxSection; section++) {
                            light.getSection(section).blockLight().invalidate();
                            light.getSection(section).skyLight().invalidate();
                        }

                        light.invalidate();

                        light.resendTimer.set(20);
                    }
                }
            }
        });

        doneInit = true;
    }

    @Override
    protected NBTCompound computeHeightmap() {
        // Heightmap
        int[] heightmap = getHeightmap();
        int dimensionHeight = getInstance().getDimensionType().getHeight();
        final int bitsForHeight = MathUtils.bitsToRepresent(dimensionHeight);
        return NBT.Compound(Map.of(
                "MOTION_BLOCKING", NBT.LongArray(encodeBlocks(heightmap, bitsForHeight)),
                "WORLD_SURFACE", NBT.LongArray(encodeBlocks(heightmap, bitsForHeight))));
    }

    // Lazy compute heightmap
    public int[] getHeightmap() {
        if (this.heightmap != null) return this.heightmap;
        var heightmap = new int[CHUNK_SIZE_X * CHUNK_SIZE_Z];

        int minY = instance.getDimensionType().getMinY();
        int maxY = instance.getDimensionType().getMinY() + instance.getDimensionType().getHeight();
        highestBlock = minY;

        synchronized (this) {
            for (int x = 0; x < CHUNK_SIZE_X; x++) {
                for (int z = 0; z < CHUNK_SIZE_Z; z++) {
                    int height = maxY;
                    while (height > minY) {
                        Block block = getBlock(x, height, z, Condition.TYPE);
                        if (block != Block.AIR) highestBlock = Math.max(highestBlock, height);
                        if (checkSkyOcclusion(block)) break;
                        height--;
                    }
                    heightmap[z << 4 | x] = (height + 1);
                }
            }
        }

        this.heightmap = heightmap;
        return heightmap;
    }

    @Override
    protected LightData createLightData() {
        packetGenerationLock.lock();
        if (lightData != null) {
            packetGenerationLock.unlock();
            return lightData;
        }

        BitSet skyMask = new BitSet();
        BitSet blockMask = new BitSet();
        BitSet emptySkyMask = new BitSet();
        BitSet emptyBlockMask = new BitSet();
        List<byte[]> skyLights = new ArrayList<>();
        List<byte[]> blockLights = new ArrayList<>();

        int chunkMin = instance.getDimensionType().getMinY();

        int highestNeighborBlock = instance.getDimensionType().getMinY();
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                Chunk neighborChunk = instance.getChunk(chunkX + i, chunkZ + j);
                if (neighborChunk == null) continue;

                if (neighborChunk instanceof LightingChunk light) {
                    light.getHeightmap();
                    highestNeighborBlock = Math.max(highestNeighborBlock, light.highestBlock);
                }
            }
        }

        int index = 0;
        for (Section section : sections) {
            boolean wasUpdatedBlock = false;
            boolean wasUpdatedSky = false;

            if (section.blockLight().requiresUpdate()) {
                relightSection(instance, this.chunkX, index + minSection, chunkZ, LightType.BLOCK);
                wasUpdatedBlock = true;
            } else if (section.blockLight().requiresSend()) {
                wasUpdatedBlock = true;
            }

            if (section.skyLight().requiresUpdate()) {
                relightSection(instance, this.chunkX, index + minSection, chunkZ, LightType.SKY);
                wasUpdatedSky = true;
            } else if (section.skyLight().requiresSend()) {
                wasUpdatedSky = true;
            }

            index++;

            final byte[] skyLight = section.skyLight().array();
            final byte[] blockLight = section.blockLight().array();
            final int sectionMaxY = index * 16 + chunkMin;

            if ((wasUpdatedSky) && this.instance.getDimensionType().isSkylightEnabled() && sectionMaxY <= (highestNeighborBlock + 16)) {
                if (skyLight.length != 0 && skyLight != emptyContent) {
                    skyLights.add(skyLight);
                    skyMask.set(index);
                } else {
                    emptySkyMask.set(index);
                }
            }

            if (wasUpdatedBlock) {
                if (blockLight.length != 0 && blockLight != emptyContent) {
                    blockLights.add(blockLight);
                    blockMask.set(index);
                } else {
                    emptyBlockMask.set(index);
                }
            }
        }

        this.lightData = new LightData(skyMask, blockMask,
                emptySkyMask, emptyBlockMask,
                skyLights, blockLights);

        packetGenerationLock.unlock();

        return this.lightData;
    }

    @Override
    public void tick(long time) {
        super.tick(time);

        if (doneInit && resendTimer.get() > 0) {
            if (resendTimer.decrementAndGet() == 0) {
                sendLighting();
            }
        }
    }

    private static Set<Chunk> flushQueue(Instance instance, Set<Point> queue, LightType type, QueueType queueType) {
        Set<Light> sections = ConcurrentHashMap.newKeySet();
        Set<Point> newQueue = ConcurrentHashMap.newKeySet();

        Set<Chunk> responseChunks = ConcurrentHashMap.newKeySet();
        List<CompletableFuture<Void>> tasks = new ArrayList<>();

        for (Point point : queue) {
            Chunk chunk = instance.getChunk(point.blockX(), point.blockZ());
            if (chunk == null) continue;

            var section = chunk.getSection(point.blockY());
            responseChunks.add(chunk);

            var light = type == LightType.BLOCK ? section.blockLight() : section.skyLight();

            CompletableFuture<Void> task = CompletableFuture.runAsync(() -> {
                if (queueType == QueueType.INTERNAL) light.calculateInternal(instance, chunk.getChunkX(), point.blockY(), chunk.getChunkZ());
                else light.calculateExternal(instance, chunk, point.blockY());

                sections.add(light);

                var toAdd = light.flip();
                if (toAdd != null) newQueue.addAll(toAdd);
            }, pool);

            tasks.add(task);
        }

        tasks.forEach(CompletableFuture::join);

        if (!newQueue.isEmpty()) {
            var newResponse = flushQueue(instance, newQueue, type, QueueType.EXTERNAL);
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
        Set<Point> sections = new HashSet<>();

        synchronized (instance) {
            for (Chunk chunk : chunks) {
                if (chunk == null) continue;
                if (chunk instanceof LightingChunk lighting) {
                    for (int section = chunk.minSection; section < chunk.maxSection; section++) {
                        chunk.getSection(section).blockLight().invalidate();
                        chunk.getSection(section).skyLight().invalidate();

                        sections.add(new Vec(chunk.getChunkX(), section, chunk.getChunkZ()));
                    }

                    lighting.invalidate();
                }
            }

            // Expand the sections to include nearby sections
            var blockSections = new HashSet<Point>();
            for (Point point : sections) {
                blockSections.addAll(getNearbyRequired(instance, point, LightType.BLOCK));
            }

            var skySections = new HashSet<Point>();
            for (Point point : sections) {
                skySections.addAll(getNearbyRequired(instance, point, LightType.SKY));
            }

            relight(instance, blockSections, LightType.BLOCK);
            relight(instance, skySections, LightType.SKY);

            var chunksToRelight = new HashSet<Chunk>();
            for (Point point : blockSections) {
                chunksToRelight.add(instance.getChunk(point.blockX(), point.blockZ()));
            }

            for (Point point : skySections) {
                chunksToRelight.add(instance.getChunk(point.blockX(), point.blockZ()));
            }

            return new ArrayList<>(chunksToRelight);
        }
    }

    private static Set<Point> getNearbyRequired(Instance instance, Point point, LightType type) {
        Set<Point> collected = new HashSet<>();
        collected.add(point);

        int highestRegionPoint = instance.getDimensionType().getMinY();

        for (int x = point.blockX() - 1; x <= point.blockX() + 1; x++) {
            for (int z = point.blockZ() - 1; z <= point.blockZ() + 1; z++) {
                Chunk chunkCheck = instance.getChunk(x, z);
                if (chunkCheck == null) continue;

                if (chunkCheck instanceof LightingChunk lighting) {
                    // Ensure heightmap is calculated before taking values from it
                    lighting.getHeightmap();
                    if (lighting.highestBlock > highestRegionPoint) highestRegionPoint = lighting.highestBlock;
                }
            }
        }

        for (int x = point.blockX() - 1; x <= point.blockX() + 1; x++) {
            for (int z = point.blockZ() - 1; z <= point.blockZ() + 1; z++) {
                Chunk chunkCheck = instance.getChunk(x, z);
                if (chunkCheck == null) continue;

                for (int y = point.blockY() - 1; y <= point.blockY() + 1; y++) {
                    Point sectionPosition = new Vec(x, y, z);
                    int sectionHeight = instance.getDimensionType().getMinY() + 16 * y;
                    if ((sectionHeight + 16) > highestRegionPoint && type == LightType.SKY) continue;

                    if (sectionPosition.blockY() < chunkCheck.getMaxSection() && sectionPosition.blockY() >= chunkCheck.getMinSection()) {
                        Section s = chunkCheck.getSection(sectionPosition.blockY());
                        if (!s.blockLight().requiresUpdate() && !s.skyLight().requiresUpdate()) continue;

                        collected.add(sectionPosition);
                    }
                }
            }
        }

        return collected;
    }

    private static Set<Point> collectRequiredNearby(Instance instance, Point point, LightType type) {
        final Set<Point> found = new HashSet<>();
        final ArrayDeque<Point> toCheck = new ArrayDeque<>();

        toCheck.add(point);
        found.add(point);

        while (!toCheck.isEmpty()) {
            final Point current = toCheck.poll();
            final Set<Point> nearby = getNearbyRequired(instance, current, type);
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
        var res = new HashSet<>(relightSection(instance, chunkX, sectionY, chunkZ, LightType.BLOCK));
        res.addAll(relightSection(instance, chunkX, sectionY, chunkZ, LightType.SKY));
        return res;
    }

    private static Set<Chunk> relightSection(Instance instance, int chunkX, int sectionY, int chunkZ, LightType type) {
        Chunk c = instance.getChunk(chunkX, chunkZ);
        if (c == null) return Set.of();
        if (!(c instanceof LightingChunk)) return Set.of();

        synchronized (instance) {
            Set<Point> collected = collectRequiredNearby(instance, new Vec(chunkX, sectionY, chunkZ), type);
            return relight(instance, collected, type);
        }
    }

    private static Set<Chunk> relight(Instance instance, Set<Point> queue, LightType type) {
        return flushQueue(instance, queue, type, QueueType.INTERNAL);
    }

    @Override
    public @NotNull Chunk copy(@NotNull Instance instance, int chunkX, int chunkZ) {
        LightingChunk lightingChunk = new LightingChunk(instance, chunkX, chunkZ);
        lightingChunk.sections = sections.stream().map(Section::clone).toList();
        lightingChunk.entries.putAll(entries);
        return lightingChunk;
    }
}