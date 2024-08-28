package net.minestom.server.instance;

import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.instance.InstanceChunkLoadEvent;
import net.minestom.server.event.instance.InstanceChunkUnloadEvent;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.instance.anvil.AnvilLoader;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.instance.generator.Generator;
import net.minestom.server.instance.generator.GeneratorImpl;
import net.minestom.server.instance.palette.Palette;
import net.minestom.server.network.packet.server.play.BlockChangePacket;
import net.minestom.server.network.packet.server.play.BlockEntityDataPacket;
import net.minestom.server.network.packet.server.play.EffectPacket;
import net.minestom.server.network.packet.server.play.UnloadChunkPacket;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.PacketSendingUtils;
import net.minestom.server.utils.async.AsyncUtils;
import net.minestom.server.utils.block.BlockUtils;
import net.minestom.server.utils.chunk.ChunkCache;
import net.minestom.server.utils.chunk.ChunkSupplier;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.utils.validate.Check;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.vectrix.flare.fastutil.Long2ObjectSyncMap;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import static net.minestom.server.utils.chunk.ChunkUtils.*;

/**
 * InstanceContainer is an instance that contains chunks in contrary to SharedInstance.
 */
public class InstanceContainer extends Instance {
    private static final Logger LOGGER = LoggerFactory.getLogger(InstanceContainer.class);

    private static final AnvilLoader DEFAULT_LOADER = new AnvilLoader("world");

    private static final BlockFace[] BLOCK_UPDATE_FACES = new BlockFace[]{
            BlockFace.WEST, BlockFace.EAST, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.BOTTOM, BlockFace.TOP
    };

    // the shared instances assigned to this instance
    private final List<SharedInstance> sharedInstances = new CopyOnWriteArrayList<>();

    // the chunk generator used, can be null
    private volatile Generator generator;
    // (chunk index -> chunk) map, contains all the chunks in the instance
    // used as a monitor when access is required
    private final Long2ObjectSyncMap<Chunk> chunks = Long2ObjectSyncMap.hashmap();
    private final Map<Long, CompletableFuture<Chunk>> loadingChunks = new ConcurrentHashMap<>();

    private final Lock changingBlockLock = new ReentrantLock();
    private final Map<Point, Block> currentlyChangingBlocks = new HashMap<>();

    // the chunk loader, used when trying to load/save a chunk from another source
    private IChunkLoader chunkLoader;

    // used to automatically enable the chunk loading or not
    private boolean autoChunkLoad = true;

    // used to supply a new chunk object at a position when requested
    private ChunkSupplier chunkSupplier;

    // Fields for instance copy
    protected InstanceContainer srcInstance; // only present if this instance has been created using a copy
    private long lastBlockChangeTime; // Time at which the last block change happened (#setBlock)

    public InstanceContainer(@NotNull UUID uniqueId, @NotNull DynamicRegistry.Key<DimensionType> dimensionType) {
        this(uniqueId, dimensionType, null, dimensionType.namespace());
    }

    public InstanceContainer(@NotNull UUID uniqueId, @NotNull DynamicRegistry.Key<DimensionType> dimensionType, @NotNull NamespaceID dimensionName) {
        this(uniqueId, dimensionType, null, dimensionName);
    }

    public InstanceContainer(@NotNull UUID uniqueId, @NotNull DynamicRegistry.Key<DimensionType> dimensionType, @Nullable IChunkLoader loader) {
        this(uniqueId, dimensionType, loader, dimensionType.namespace());
    }

    public InstanceContainer(@NotNull UUID uniqueId, @NotNull DynamicRegistry.Key<DimensionType> dimensionType, @Nullable IChunkLoader loader, @NotNull NamespaceID dimensionName) {
        this(MinecraftServer.getDimensionTypeRegistry(), uniqueId, dimensionType, loader, dimensionName);
    }

    public InstanceContainer(
            @NotNull DynamicRegistry<DimensionType> dimensionTypeRegistry,
            @NotNull UUID uniqueId,
            @NotNull DynamicRegistry.Key<DimensionType> dimensionType,
            @Nullable IChunkLoader loader,
            @NotNull NamespaceID dimensionName
    ) {
        super(dimensionTypeRegistry, uniqueId, dimensionType, dimensionName);
        setChunkSupplier(DynamicChunk::new);
        setChunkLoader(Objects.requireNonNullElse(loader, DEFAULT_LOADER));
        this.chunkLoader.loadInstance(this);
    }

    @Override
    public void setBlock(int x, int y, int z, @NotNull Block block, boolean doBlockUpdates) {
        Chunk chunk = getChunkAt(x, z);
        if (chunk == null) {
            Check.stateCondition(!hasEnabledAutoChunkLoad(),
                    "Tried to set a block to an unloaded chunk with auto chunk load disabled");
            chunk = loadChunk(getChunkCoordinate(x), getChunkCoordinate(z)).join();
        }
        if (isLoaded(chunk)) UNSAFE_setBlock(chunk, x, y, z, block, null, null, doBlockUpdates, 0);
    }

    /**
     * Sets a block at the specified position.
     * <p>
     * Unsafe because the method is not synchronized and it does not verify if the chunk is loaded or not.
     *
     * @param chunk the {@link Chunk} which should be loaded
     * @param x     the block X
     * @param y     the block Y
     * @param z     the block Z
     * @param block the block to place
     */
    private synchronized void UNSAFE_setBlock(@NotNull Chunk chunk, int x, int y, int z, @NotNull Block block,
                                              @Nullable BlockHandler.Placement placement, @Nullable BlockHandler.Destroy destroy,
                                              boolean doBlockUpdates, int updateDistance) {
        if (chunk.isReadOnly()) return;
        final DimensionType dim = getCachedDimensionType();
        if (y >= dim.maxY() || y < dim.minY()) {
            LOGGER.warn("tried to set a block outside the world bounds, should be within [{}, {}): {}", dim.minY(), dim.maxY(), y);
            return;
        }

        synchronized (chunk) {
            // Refresh the last block change time
            this.lastBlockChangeTime = System.currentTimeMillis();
            final Vec blockPosition = new Vec(x, y, z);
            if (isAlreadyChanged(blockPosition, block)) { // do NOT change the block again.
                // Avoids StackOverflowExceptions when onDestroy tries to destroy the block itself
                // This can happen with nether portals which break the entire frame when a portal block is broken
                return;
            }
            this.currentlyChangingBlocks.put(blockPosition, block);

            // Change id based on neighbors
            final BlockPlacementRule blockPlacementRule = MinecraftServer.getBlockManager().getBlockPlacementRule(block);
            if (placement != null && blockPlacementRule != null && doBlockUpdates) {
                BlockPlacementRule.PlacementState rulePlacement;
                if (placement instanceof BlockHandler.PlayerPlacement pp) {
                    rulePlacement = new BlockPlacementRule.PlacementState(
                            this, block, pp.getBlockFace(), blockPosition,
                            new Vec(pp.getCursorX(), pp.getCursorY(), pp.getCursorZ()),
                            pp.getPlayer().getPosition(),
                            pp.getPlayer().getItemInHand(pp.getHand()),
                            pp.getPlayer().isSneaking()
                    );
                } else {
                    rulePlacement = new BlockPlacementRule.PlacementState(
                            this, block, null, blockPosition,
                            null, null, null,
                            false
                    );
                }

                block = blockPlacementRule.blockPlace(rulePlacement);
                if (block == null) block = Block.AIR;
            }

            // Set the block
            chunk.setBlock(x, y, z, block, placement, destroy);

            // Refresh neighbors since a new block has been placed
            if (doBlockUpdates) {
                executeNeighboursBlockPlacementRule(blockPosition, updateDistance);
            }

            // Refresh player chunk block
            {
                chunk.sendPacketToViewers(new BlockChangePacket(blockPosition, block.stateId()));
                var registry = block.registry();
                if (registry.isBlockEntity()) {
                    final CompoundBinaryTag data = BlockUtils.extractClientNbt(block);
                    chunk.sendPacketToViewers(new BlockEntityDataPacket(blockPosition, registry.blockEntityId(), data));
                }
            }
        }
    }

    @Override
    public boolean placeBlock(@NotNull BlockHandler.Placement placement, boolean doBlockUpdates) {
        final Point blockPosition = placement.getBlockPosition();
        final Chunk chunk = getChunkAt(blockPosition);
        if (!isLoaded(chunk)) return false;
        UNSAFE_setBlock(chunk, blockPosition.blockX(), blockPosition.blockY(), blockPosition.blockZ(),
                placement.getBlock(), placement, null, doBlockUpdates, 0);
        return true;
    }

    @Override
    public boolean breakBlock(@NotNull Player player, @NotNull Point blockPosition, @NotNull BlockFace blockFace, boolean doBlockUpdates) {
        final Chunk chunk = getChunkAt(blockPosition);
        Check.notNull(chunk, "You cannot break blocks in a null chunk!");
        if (chunk.isReadOnly()) return false;
        if (!isLoaded(chunk)) return false;

        final Block block = getBlock(blockPosition);
        final int x = blockPosition.blockX();
        final int y = blockPosition.blockY();
        final int z = blockPosition.blockZ();
        if (block.isAir()) {
            // The player probably have a wrong version of this chunk section, send it
            chunk.sendChunk(player);
            return false;
        }
        PlayerBlockBreakEvent blockBreakEvent = new PlayerBlockBreakEvent(player, block, Block.AIR, new BlockVec(blockPosition), blockFace);
        EventDispatcher.call(blockBreakEvent);
        final boolean allowed = !blockBreakEvent.isCancelled();
        if (allowed) {
            // Break or change the broken block based on event result
            final Block resultBlock = blockBreakEvent.getResultBlock();
            UNSAFE_setBlock(chunk, x, y, z, resultBlock, null,
                    new BlockHandler.PlayerDestroy(block, this, blockPosition, player), doBlockUpdates, 0);
            // Send the block break effect packet
            PacketSendingUtils.sendGroupedPacket(chunk.getViewers(),
                    new EffectPacket(2001 /*Block break + block break sound*/, blockPosition, block.stateId(), false),
                    // Prevent the block breaker to play the particles and sound two times
                    (viewer) -> !viewer.equals(player));
        }
        return allowed;
    }

    @Override
    public @NotNull CompletableFuture<Chunk> loadChunk(int chunkX, int chunkZ) {
        return loadOrRetrieve(chunkX, chunkZ, () -> retrieveChunk(chunkX, chunkZ));
    }

    @Override
    public @NotNull CompletableFuture<Chunk> loadOptionalChunk(int chunkX, int chunkZ) {
        return loadOrRetrieve(chunkX, chunkZ, () -> hasEnabledAutoChunkLoad() ? retrieveChunk(chunkX, chunkZ) : AsyncUtils.empty());
    }

    @Override
    public synchronized void unloadChunk(@NotNull Chunk chunk) {
        if (!isLoaded(chunk)) return;
        final int chunkX = chunk.getChunkX();
        final int chunkZ = chunk.getChunkZ();
        chunk.sendPacketToViewers(new UnloadChunkPacket(chunkX, chunkZ));
        EventDispatcher.call(new InstanceChunkUnloadEvent(this, chunk));
        // Remove all entities in chunk
        getEntityTracker().chunkEntities(chunkX, chunkZ, EntityTracker.Target.ENTITIES).forEach(Entity::remove);
        // Clear cache
        this.chunks.remove(getChunkIndex(chunkX, chunkZ));
        chunk.unload();
        chunkLoader.unloadChunk(chunk);
        var dispatcher = MinecraftServer.process().dispatcher();
        dispatcher.deletePartition(chunk);
    }

    @Override
    public Chunk getChunk(int chunkX, int chunkZ) {
        return chunks.get(getChunkIndex(chunkX, chunkZ));
    }

    @Override
    public @NotNull CompletableFuture<Void> saveInstance() {
        return chunkLoader.saveInstance(this);
    }

    @Override
    public @NotNull CompletableFuture<Void> saveChunkToStorage(@NotNull Chunk chunk) {
        return chunkLoader.saveChunk(chunk);
    }

    @Override
    public @NotNull CompletableFuture<Void> saveChunksToStorage() {
        return chunkLoader.saveChunks(getChunks());
    }

    protected @NotNull CompletableFuture<@NotNull Chunk> retrieveChunk(int chunkX, int chunkZ) {
        CompletableFuture<Chunk> completableFuture = new CompletableFuture<>();
        final long index = getChunkIndex(chunkX, chunkZ);
        final CompletableFuture<Chunk> prev = loadingChunks.putIfAbsent(index, completableFuture);
        if (prev != null) return prev;
        final IChunkLoader loader = chunkLoader;
        final Runnable retriever = () -> loader.loadChunk(this, chunkX, chunkZ)
                .thenCompose(chunk -> {
                    if (chunk != null) {
                        // Chunk has been loaded from storage
                        return CompletableFuture.completedFuture(chunk);
                    } else {
                        // Loader couldn't load the chunk, generate it
                        return createChunk(chunkX, chunkZ).whenComplete((c, a) -> c.onGenerate());
                    }
                })
                // cache the retrieved chunk
                .thenAccept(chunk -> {
                    // TODO run in the instance thread?
                    cacheChunk(chunk);
                    chunk.onLoad();

                    EventDispatcher.call(new InstanceChunkLoadEvent(this, chunk));
                    final CompletableFuture<Chunk> future = this.loadingChunks.remove(index);
                    assert future == completableFuture : "Invalid future: " + future;
                    completableFuture.complete(chunk);
                })
                .exceptionally(throwable -> {
                    MinecraftServer.getExceptionManager().handleException(throwable);
                    return null;
                });
        if (loader.supportsParallelLoading()) {
            CompletableFuture.runAsync(retriever);
        } else {
            retriever.run();
        }
        return completableFuture;
    }

    Map<Long, List<GeneratorImpl.SectionModifierImpl>> generationForks = new ConcurrentHashMap<>();

    protected @NotNull CompletableFuture<@NotNull Chunk> createChunk(int chunkX, int chunkZ) {
        final Chunk chunk = chunkSupplier.createChunk(this, chunkX, chunkZ);
        Check.notNull(chunk, "Chunks supplied by a ChunkSupplier cannot be null.");
        Generator generator = generator();
        if (generator != null && chunk.shouldGenerate()) {
            CompletableFuture<Chunk> resultFuture = new CompletableFuture<>();
            // TODO: virtual thread once Loom is available
            ForkJoinPool.commonPool().submit(() -> {
                GeneratorImpl.GenSection[] genSections = new GeneratorImpl.GenSection[chunk.getSections().size()];
                Arrays.setAll(genSections, i -> {
                    Section section = chunk.getSections().get(i);
                    return new GeneratorImpl.GenSection(section.blockPalette(), section.biomePalette());
                });
                var chunkUnit = GeneratorImpl.chunk(MinecraftServer.getBiomeRegistry(), genSections,
                        chunk.getChunkX(), chunk.minSection, chunk.getChunkZ());
                try {
                    // Generate block/biome palette
                    generator.generate(chunkUnit);
                    // Apply nbt/handler
                    if (chunkUnit.modifier() instanceof GeneratorImpl.AreaModifierImpl chunkModifier) {
                        for (var section : chunkModifier.sections()) {
                            if (section.modifier() instanceof GeneratorImpl.SectionModifierImpl sectionModifier) {
                                applyGenerationData(chunk, sectionModifier);
                            }
                        }
                    }
                    // Register forks or apply locally
                    for (var fork : chunkUnit.forks()) {
                        var sections = ((GeneratorImpl.AreaModifierImpl) fork.modifier()).sections();
                        for (var section : sections) {
                            if (section.modifier() instanceof GeneratorImpl.SectionModifierImpl sectionModifier) {
                                if (sectionModifier.genSection().blocks().count() == 0)
                                    continue;
                                final Point start = section.absoluteStart();
                                final Chunk forkChunk = start.chunkX() == chunkX && start.chunkZ() == chunkZ ? chunk : getChunkAt(start);
                                if (forkChunk != null) {
                                    applyFork(forkChunk, sectionModifier);
                                    // Update players
                                    forkChunk.invalidate();
                                    forkChunk.sendChunk();
                                } else {
                                    final long index = getChunkIndex(start);
                                    this.generationForks.compute(index, (i, sectionModifiers) -> {
                                        if (sectionModifiers == null) sectionModifiers = new ArrayList<>();
                                        sectionModifiers.add(sectionModifier);
                                        return sectionModifiers;
                                    });
                                }
                            }
                        }
                    }
                    // Apply awaiting forks
                    processFork(chunk);
                } catch (Throwable e) {
                    MinecraftServer.getExceptionManager().handleException(e);
                } finally {
                    // End generation
                    refreshLastBlockChangeTime();
                    resultFuture.complete(chunk);
                }
            });
            return resultFuture;
        } else {
            // No chunk generator, execute the callback with the empty chunk
            processFork(chunk);
            return CompletableFuture.completedFuture(chunk);
        }
    }

    private void processFork(Chunk chunk) {
        this.generationForks.compute(ChunkUtils.getChunkIndex(chunk), (aLong, sectionModifiers) -> {
            if (sectionModifiers != null) {
                for (var sectionModifier : sectionModifiers) {
                    applyFork(chunk, sectionModifier);
                }
            }
            return null;
        });
    }

    private void applyFork(Chunk chunk, GeneratorImpl.SectionModifierImpl sectionModifier) {
        synchronized (chunk) {
            Section section = chunk.getSectionAt(sectionModifier.start().blockY());
            Palette currentBlocks = section.blockPalette();
            // -1 is necessary because forked units handle explicit changes by changing AIR 0 to 1
            sectionModifier.genSection().blocks().getAllPresent((x, y, z, value) -> currentBlocks.set(x, y, z, value - 1));
            applyGenerationData(chunk, sectionModifier);
        }
    }

    private void applyGenerationData(Chunk chunk, GeneratorImpl.SectionModifierImpl section) {
        var cache = section.genSection().specials();
        if (cache.isEmpty()) return;
        final int height = section.start().blockY();
        synchronized (chunk) {
            Int2ObjectMaps.fastForEach(cache, blockEntry -> {
                final int index = blockEntry.getIntKey();
                final Block block = blockEntry.getValue();
                final int x = ChunkUtils.blockIndexToChunkPositionX(index);
                final int y = ChunkUtils.blockIndexToChunkPositionY(index) + height;
                final int z = ChunkUtils.blockIndexToChunkPositionZ(index);
                chunk.setBlock(x, y, z, block);
            });
        }
    }

    @Override
    public void enableAutoChunkLoad(boolean enable) {
        this.autoChunkLoad = enable;
    }

    @Override
    public boolean hasEnabledAutoChunkLoad() {
        return autoChunkLoad;
    }

    @Override
    public boolean isInVoid(@NotNull Point point) {
        // TODO: more customizable
        return point.y() < getCachedDimensionType().minY() - 64;
    }

    /**
     * Changes which type of {@link Chunk} implementation to use once one needs to be loaded.
     * <p>
     * Uses {@link DynamicChunk} by default.
     * <p>
     * WARNING: if you need to save this instance's chunks later,
     * the code needs to be predictable for {@link IChunkLoader#loadChunk(Instance, int, int)}
     * to create the correct type of {@link Chunk}. tl;dr: Need chunk save = no random type.
     *
     * @param chunkSupplier the new {@link ChunkSupplier} of this instance, chunks need to be non-null
     * @throws NullPointerException if {@code chunkSupplier} is null
     */
    @Override
    public void setChunkSupplier(@NotNull ChunkSupplier chunkSupplier) {
        this.chunkSupplier = chunkSupplier;
    }

    /**
     * Gets the current {@link ChunkSupplier}.
     * <p>
     * You shouldn't use it to generate a new chunk, but as a way to view which one is currently in use.
     *
     * @return the current {@link ChunkSupplier}
     */
    public ChunkSupplier getChunkSupplier() {
        return chunkSupplier;
    }

    /**
     * Gets all the {@link SharedInstance} linked to this container.
     *
     * @return an unmodifiable {@link List} containing all the {@link SharedInstance} linked to this container
     */
    public List<SharedInstance> getSharedInstances() {
        return Collections.unmodifiableList(sharedInstances);
    }

    /**
     * Gets if this instance has {@link SharedInstance} linked to it.
     *
     * @return true if {@link #getSharedInstances()} is not empty
     */
    public boolean hasSharedInstances() {
        return !sharedInstances.isEmpty();
    }

    /**
     * Assigns a {@link SharedInstance} to this container.
     * <p>
     * Only used by {@link InstanceManager}, mostly unsafe.
     *
     * @param sharedInstance the shared instance to assign to this container
     */
    protected void addSharedInstance(SharedInstance sharedInstance) {
        this.sharedInstances.add(sharedInstance);
    }

    /**
     * Copies all the chunks of this instance and create a new instance container with all of them.
     * <p>
     * Chunks are copied with {@link Chunk#copy(Instance, int, int)},
     * {@link UUID} is randomized and {@link DimensionType} is passed over.
     *
     * @return an {@link InstanceContainer} with the exact same chunks as 'this'
     * @see #getSrcInstance() to retrieve the "creation source" of the copied instance
     */
    public synchronized InstanceContainer copy() {
        InstanceContainer copiedInstance = new InstanceContainer(UUID.randomUUID(), getDimensionType());
        copiedInstance.srcInstance = this;
        copiedInstance.tagHandler = this.tagHandler.copy();
        copiedInstance.lastBlockChangeTime = this.lastBlockChangeTime;
        for (Chunk chunk : chunks.values()) {
            final int chunkX = chunk.getChunkX();
            final int chunkZ = chunk.getChunkZ();
            final Chunk copiedChunk = chunk.copy(copiedInstance, chunkX, chunkZ);
            copiedInstance.cacheChunk(copiedChunk);
        }
        return copiedInstance;
    }

    /**
     * Gets the instance from which this one has been copied.
     * <p>
     * Only present if this instance has been created with {@link InstanceContainer#copy()}.
     *
     * @return the instance source, null if not created by a copy
     * @see #copy() to create a copy of this instance with 'this' as the source
     */
    public @Nullable InstanceContainer getSrcInstance() {
        return srcInstance;
    }

    /**
     * Gets the last time at which a block changed.
     *
     * @return the time at which the last block changed in milliseconds, 0 if never
     */
    public long getLastBlockChangeTime() {
        return lastBlockChangeTime;
    }

    /**
     * Signals the instance that a block changed.
     * <p>
     * Useful if you change blocks values directly using a {@link Chunk} object.
     */
    public void refreshLastBlockChangeTime() {
        this.lastBlockChangeTime = System.currentTimeMillis();
    }

    @Override
    public @Nullable Generator generator() {
        return generator;
    }

    @Override
    public void setGenerator(@Nullable Generator generator) {
        this.generator = generator;
    }

    /**
     * Gets all the instance chunks.
     *
     * @return the chunks of this instance
     */
    @Override
    public @NotNull Collection<@NotNull Chunk> getChunks() {
        return chunks.values();
    }

    /**
     * Gets the {@link IChunkLoader} of this instance.
     *
     * @return the {@link IChunkLoader} of this instance
     */
    public @NotNull IChunkLoader getChunkLoader() {
        return chunkLoader;
    }

    /**
     * Changes the {@link IChunkLoader} of this instance (to change how chunks are retrieved when not already loaded).
     *
     * <p>{@link IChunkLoader#noop()} can be used to do nothing.</p>
     *
     * @param chunkLoader the new {@link IChunkLoader}
     */
    public void setChunkLoader(@NotNull IChunkLoader chunkLoader) {
        this.chunkLoader = Objects.requireNonNull(chunkLoader, "Chunk loader cannot be null");
    }

    @Override
    public void tick(long time) {
        // Time/world border
        super.tick(time);
        // Clear block change map
        Lock wrlock = this.changingBlockLock;
        wrlock.lock();
        this.currentlyChangingBlocks.clear();
        wrlock.unlock();
    }

    /**
     * Has this block already changed since last update?
     * Prevents StackOverflow with blocks trying to modify their position in onDestroy or onPlace.
     *
     * @param blockPosition the block position
     * @param block         the block
     * @return true if the block changed since the last update
     */
    private boolean isAlreadyChanged(@NotNull Point blockPosition, @NotNull Block block) {
        final Block changedBlock = currentlyChangingBlocks.get(blockPosition);
        return Objects.equals(changedBlock, block);
    }

    /**
     * Executed when a block is modified, this is used to modify the states of neighbours blocks.
     * <p>
     * For example, this can be used for redstone wires which need an understanding of its neighborhoods to take the right shape.
     *
     * @param blockPosition the position of the modified block
     */
    private void executeNeighboursBlockPlacementRule(@NotNull Point blockPosition, int updateDistance) {
        ChunkCache cache = new ChunkCache(this, null, null);
        for (var updateFace : BLOCK_UPDATE_FACES) {
            var direction = updateFace.toDirection();
            final int neighborX = blockPosition.blockX() + direction.normalX();
            final int neighborY = blockPosition.blockY() + direction.normalY();
            final int neighborZ = blockPosition.blockZ() + direction.normalZ();
            if (neighborY < getCachedDimensionType().minY() || neighborY > getCachedDimensionType().height())
                continue;
            final Block neighborBlock = cache.getBlock(neighborX, neighborY, neighborZ, Condition.NONE);
            if (neighborBlock == null || neighborBlock.isAir())
                continue;
            final BlockPlacementRule neighborBlockPlacementRule = MinecraftServer.getBlockManager().getBlockPlacementRule(neighborBlock);
            if (neighborBlockPlacementRule == null || updateDistance >= neighborBlockPlacementRule.maxUpdateDistance()) continue;

            final Vec neighborPosition = new Vec(neighborX, neighborY, neighborZ);
            final Block newNeighborBlock = neighborBlockPlacementRule.blockUpdate(new BlockPlacementRule.UpdateState(
                    this,
                    neighborPosition,
                    neighborBlock,
                    updateFace.getOppositeFace()
            ));
            if (neighborBlock != newNeighborBlock) {
                final Chunk chunk = getChunkAt(neighborPosition);
                if (!isLoaded(chunk)) continue;
                UNSAFE_setBlock(chunk, neighborPosition.blockX(), neighborPosition.blockY(), neighborPosition.blockZ(), newNeighborBlock,
                        null, null, true, updateDistance + 1);
            }
        }
    }

    private CompletableFuture<Chunk> loadOrRetrieve(int chunkX, int chunkZ, Supplier<CompletableFuture<Chunk>> supplier) {
        final Chunk chunk = getChunk(chunkX, chunkZ);
        if (chunk != null) {
            // Chunk already loaded
            return CompletableFuture.completedFuture(chunk);
        }
        return supplier.get();
    }

    private void cacheChunk(@NotNull Chunk chunk) {
        this.chunks.put(getChunkIndex(chunk), chunk);
        var dispatcher = MinecraftServer.process().dispatcher();
        dispatcher.createPartition(chunk);
    }
}
