package net.minestom.server.instance;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minestom.server.MinecraftServer;
import net.minestom.server.collision.Shape;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.instance.light.Light;
import net.minestom.server.network.packet.server.CachedPacket;
import net.minestom.server.network.packet.server.play.data.LightData;
import net.minestom.server.timer.ExecutionType;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import static net.minestom.server.instance.light.LightCompute.emptyContent;

public class LightingChunk extends DynamicChunk {

    private static final int LIGHTING_CHUNKS_PER_SEND = Integer.getInteger("minestom.lighting.chunks-per-send", 10);
    private static final int LIGHTING_CHUNKS_SEND_DELAY = Integer.getInteger("minestom.lighting.chunks-send-delay", 100);

    private static final ExecutorService pool = Executors.newWorkStealingPool();

    private int[] heightmap;
    final CachedPacket lightCache = new CachedPacket(this::createLightPacket);
    boolean sendNeighbours = true;
    boolean chunkLoaded = false;

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
                    light.lightCache.invalidate();
                    light.chunkCache.invalidate();
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
        }
    }

    public void sendLighting() {
        if (!isLoaded()) return;
        sendPacketToViewers(lightCache);
    }

    @Override
    protected void onLoad() {
        // Prefetch the chunk packet so that lazy lighting is computed
        chunkLoaded = true;
        updateAfterGeneration(this);
    }

    public int[] calculateHeightMap() {
        if (this.heightmap != null) return this.heightmap;
        var heightmap = new int[CHUNK_SIZE_X * CHUNK_SIZE_Z];

        int minY = instance.getDimensionType().getMinY();
        int maxY = instance.getDimensionType().getMinY() + instance.getDimensionType().getHeight();

        synchronized (this) {
            for (int x = 0; x < CHUNK_SIZE_X; x++) {
                for (int z = 0; z < CHUNK_SIZE_Z; z++) {
                    int height = maxY;
                    while (height > minY) {
                        Block block = getBlock(x, height, z, Condition.TYPE);
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
    protected LightData createLightData(boolean sendLater) {
        synchronized (lightCache) {
            BitSet skyMask = new BitSet();
            BitSet blockMask = new BitSet();
            BitSet emptySkyMask = new BitSet();
            BitSet emptyBlockMask = new BitSet();
            List<byte[]> skyLights = new ArrayList<>();
            List<byte[]> blockLights = new ArrayList<>();

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

                if ((wasUpdatedSky || sendLater) && this.instance.getDimensionType().isSkylightEnabled()) {
                    if (skyLight.length != 0 && skyLight != emptyContent) {
                        skyLights.add(skyLight);
                        skyMask.set(index);
                    } else {
                        emptySkyMask.set(index);
                    }
                }

                if (wasUpdatedBlock || sendLater) {
                    if (blockLight.length != 0 && blockLight != emptyContent) {
                        blockLights.add(blockLight);
                        blockMask.set(index);
                    } else {
                        emptyBlockMask.set(index);
                    }
                }
            }

            if (sendNeighbours) {
                updateAfterGeneration(this);
                sendNeighbours = false;
            }

            return new LightData(skyMask, blockMask,
                    emptySkyMask, emptyBlockMask,
                    skyLights, blockLights);
        }
    }

    private static final LongSet queuedChunks = new LongOpenHashSet();
    private static final List<LightingChunk> sendQueue = new ArrayList<>();
    private static Task sendingTask = null;
    private static final ReentrantLock lightLock = new ReentrantLock();
    private static final ReentrantLock queueLock = new ReentrantLock();

    static void updateAfterGeneration(LightingChunk chunk) {
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                Chunk neighborChunk = chunk.instance.getChunk(chunk.chunkX + i, chunk.chunkZ + j);
                if (neighborChunk == null) continue;

                if (neighborChunk instanceof LightingChunk lightingChunk) {
                    queueLock.lock();

                    if (queuedChunks.add(ChunkUtils.getChunkIndex(lightingChunk.chunkX, lightingChunk.chunkZ))) {
                        sendQueue.add(lightingChunk);
                    }
                    queueLock.unlock();
                }
            }
        }

        lightLock.lock();
        if (sendingTask != null) {
            lightLock.unlock();
            return;
        }

        sendingTask = MinecraftServer.getSchedulerManager().scheduleTask(() -> {
            queueLock.lock();
            var copy = new ArrayList<>(sendQueue);
            sendQueue.clear();
            queuedChunks.clear();
            queueLock.unlock();

            // if (copy.size() != 0) {
            //     System.out.println("Sending lighting for " + copy.size() + " chunks");
            // }

            int count = 0;

            for (LightingChunk f : copy) {
                f.sections.forEach(s -> {
                    s.blockLight().invalidate();
                    s.skyLight().invalidate();
                });
                f.chunkCache.invalidate();
                f.lightCache.invalidate();
            }

            // Load all the lighting
            for (LightingChunk f : copy) {
                if (f.isLoaded()) {
                    f.lightCache.body();
                }
            }

            // Send it slowly
            for (LightingChunk f : copy) {
                if (f.isLoaded()) {
                    f.sendLighting();
                    if (f.getViewers().size() == 0) continue;
                }
                count++;

                if (count % LIGHTING_CHUNKS_PER_SEND == 0) {
                    // System.out.println("Sent " + count + " lighting chunks " + (count * 100 / copy.size()) + "%");
                    try {
                        Thread.sleep(LIGHTING_CHUNKS_SEND_DELAY);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, TaskSchedule.immediate(), TaskSchedule.tick(20), ExecutionType.ASYNC);
        lightLock.unlock();
    }

    private static void flushQueue(Instance instance, Set<Point> queue, LightType type, QueueType queueType) {
        AtomicInteger count = new AtomicInteger(0);
        Set<Light> sections = ConcurrentHashMap.newKeySet();
        Set<Point> newQueue = ConcurrentHashMap.newKeySet();

        for (Point point : queue) {
            Chunk chunk = instance.getChunk(point.blockX(), point.blockZ());
            if (chunk == null) {
                count.getAndIncrement();
                continue;
            }

            var light = type == LightType.BLOCK ? chunk.getSection(point.blockY()).blockLight() : chunk.getSection(point.blockY()).skyLight();

            pool.submit(() -> {
                if (queueType == QueueType.INTERNAL) light.calculateInternal(instance, chunk.getChunkX(), point.blockY(), chunk.getChunkZ());
                else light.calculateExternal(instance, chunk, point.blockY());

                sections.add(light);

                var toAdd = light.flip();
                if (toAdd != null) newQueue.addAll(toAdd);

                count.incrementAndGet();
            });
        }

        while (count.get() < queue.size()) { }

        if (newQueue.size() > 0) {
            flushQueue(instance, newQueue, type, QueueType.EXTERNAL);
        }
    }

    public static void relight(Instance instance, Collection<Chunk> chunks) {
        Set<Point> sections = new HashSet<>();

        for (Chunk chunk : chunks) {
            if (chunk == null) continue;
            for (int section = chunk.minSection; section < chunk.maxSection; section++) {
                chunk.getSection(section).blockLight().invalidate();
                chunk.getSection(section).skyLight().invalidate();

                sections.add(new Vec(chunk.getChunkX(), section, chunk.getChunkZ()));
            }
        }

        synchronized (instance) {
            relight(instance, sections, LightType.BLOCK);
            relight(instance, sections, LightType.SKY);
        }
    }

    private static Set<Point> getNearbyRequired(Instance instance, Point point) {
        Set<Point> collected = new HashSet<>();
        collected.add(point);

        for (int x = point.blockX() - 1; x <= point.blockX() + 1; x++) {
            for (int z = point.blockZ() - 1; z <= point.blockZ() + 1; z++) {
                Chunk chunkCheck = instance.getChunk(x, z);
                if (chunkCheck == null) continue;

                for (int y = point.blockY() - 1; y <= point.blockY() + 1; y++) {
                    Point sectionPosition = new Vec(x, y, z);

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

    private static Set<Point> collectRequiredNearby(Instance instance, Point point) {
        final Set<Point> found = new HashSet<>();
        final ArrayDeque<Point> toCheck = new ArrayDeque<>();

        toCheck.add(point);
        found.add(point);

        while (toCheck.size() > 0) {
            final Point current = toCheck.poll();
            final Set<Point> nearby = getNearbyRequired(instance, current);
            nearby.forEach(p -> {
                if (!found.contains(p)) {
                    found.add(p);
                    toCheck.add(p);
                }
            });
        }

        return found;
    }

    static void relightSection(Instance instance, int chunkX, int sectionY, int chunkZ) {
        relightSection(instance, chunkX, sectionY, chunkZ, LightType.BLOCK);
        relightSection(instance, chunkX, sectionY, chunkZ, LightType.SKY);
    }

    private static void relightSection(Instance instance, int chunkX, int sectionY, int chunkZ, LightType type) {
        Chunk c = instance.getChunk(chunkX, chunkZ);
        if (c == null) return;

        synchronized (instance) {
            Set<Point> collected = collectRequiredNearby(instance, new Vec(chunkX, sectionY, chunkZ));
            // System.out.println("Calculating " + chunkX + " " + sectionY + " " + chunkZ + " | " + collected.size() + " | " + type);

            relight(instance, collected, type);
        }
    }

    private static void relight(Instance instance, Set<Point> queue, LightType type) {
        flushQueue(instance, queue, type, QueueType.INTERNAL);
    }

    @Override
    public @NotNull Chunk copy(@NotNull Instance instance, int chunkX, int chunkZ) {
        LightingChunk lightingChunk = new LightingChunk(instance, chunkX, chunkZ);
        lightingChunk.sections = sections.stream().map(Section::clone).toList();
        lightingChunk.entries.putAll(entries);
        return lightingChunk;
    }
}