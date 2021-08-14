package net.minestom.server.instance;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.instance.InstanceChunkLoadEvent;
import net.minestom.server.event.instance.InstanceChunkUnloadEvent;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.instance.batch.ChunkGenerationBatch;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.network.packet.server.play.BlockChangePacket;
import net.minestom.server.network.packet.server.play.EffectPacket;
import net.minestom.server.network.packet.server.play.UnloadChunkPacket;
import net.minestom.server.storage.StorageLocation;
import net.minestom.server.utils.PacketUtils;
import net.minestom.server.utils.chunk.ChunkSupplier;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.utils.validate.Check;
import net.minestom.server.world.DimensionType;
import net.minestom.server.world.biomes.Biome;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

/**
 * InstanceContainer is an instance that contains chunks in contrary to SharedInstance.
 */
public class InstanceContainer extends Instance {

    // the shared instances assigned to this instance
    private final List<SharedInstance> sharedInstances = new CopyOnWriteArrayList<>();

    // the chunk generator used, can be null
    private ChunkGenerator chunkGenerator;
    // (chunk index -> chunk) map, contains all the chunks in the instance
    // used as a monitor when access is required
    private final Long2ObjectMap<Chunk> chunks = new Long2ObjectOpenHashMap<>();

    private final ReadWriteLock changingBlockLock = new ReentrantReadWriteLock();
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

    /**
     * Creates an {@link InstanceContainer}.
     *
     * @param uniqueId      the unique id of the instance
     * @param dimensionType the dimension type of the instance
     */
    public InstanceContainer(@NotNull UUID uniqueId, @NotNull DimensionType dimensionType) {
        super(uniqueId, dimensionType);

        // Set the default chunk supplier using DynamicChunk
        setChunkSupplier(DynamicChunk::new);

        // Set the default chunk loader which use the Anvil format
        setChunkLoader(new AnvilLoader("world"));
        this.chunkLoader.loadInstance(this);
    }

    @Override
    public synchronized void setBlock(int x, int y, int z, @NotNull Block block) {
        final Chunk chunk = getChunkAt(x, z);
        if (ChunkUtils.isLoaded(chunk)) {
            UNSAFE_setBlock(chunk, x, y, z, block, null, null);
        } else {
            Check.stateCondition(!hasEnabledAutoChunkLoad(),
                    "Tried to set a block to an unloaded chunk with auto chunk load disabled");
            final int chunkX = ChunkUtils.getChunkCoordinate(x);
            final int chunkZ = ChunkUtils.getChunkCoordinate(z);
            loadChunk(chunkX, chunkZ).thenAccept(c -> UNSAFE_setBlock(c, x, y, z, block, null, null));
        }
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
    private void UNSAFE_setBlock(@NotNull Chunk chunk, int x, int y, int z, @NotNull Block block,
                                 @Nullable BlockHandler.Placement placement, @Nullable BlockHandler.Destroy destroy) {
        // Cannot place block in a read-only chunk
        if (chunk.isReadOnly()) {
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
            setAlreadyChanged(blockPosition, block);

            final Block previousBlock = chunk.getBlock(blockPosition);
            final BlockHandler previousHandler = previousBlock.handler();

            // Change id based on neighbors
            block = executeBlockPlacementRule(block, blockPosition);

            // Set the block
            chunk.setBlock(x, y, z, block);

            // Refresh neighbors since a new block has been placed
            executeNeighboursBlockPlacementRule(blockPosition);

            // Refresh player chunk block
            sendBlockChange(chunk, blockPosition, block);

            if (previousHandler != null) {
                // Previous destroy
                previousHandler.onDestroy(Objects.requireNonNullElseGet(destroy,
                        () -> new BlockHandler.Destroy(previousBlock, this, blockPosition)));
            }
            final BlockHandler handler = block.handler();
            if (handler != null) {
                // New placement
                final Block finalBlock = block;
                handler.onPlace(Objects.requireNonNullElseGet(placement,
                        () -> new BlockHandler.Placement(finalBlock, this, blockPosition)));
            }
        }
    }

    @Override
    public boolean placeBlock(@NotNull Player player, @NotNull Block block, @NotNull Point blockPosition,
                              @NotNull BlockFace blockFace, float cursorX, float cursorY, float cursorZ) {
        final Chunk chunk = getChunkAt(blockPosition);
        if (!ChunkUtils.isLoaded(chunk))
            return false;
        UNSAFE_setBlock(chunk, blockPosition.blockX(), blockPosition.blockY(), blockPosition.blockZ(), block,
                new BlockHandler.PlayerPlacement(block, this, blockPosition, player, blockFace, cursorX, cursorY, cursorZ), null);
        return true;
    }

    @Override
    public boolean breakBlock(@NotNull Player player, @NotNull Point blockPosition) {
        final Chunk chunk = getChunkAt(blockPosition);
        Check.notNull(chunk, "You cannot break blocks in a null chunk!");
        // Cancel if the chunk is read-only
        if (chunk.isReadOnly()) {
            return false;
        }
        // Chunk unloaded, stop here
        if (!ChunkUtils.isLoaded(chunk))
            return false;
        final Block block = getBlock(blockPosition);

        final int x = blockPosition.blockX();
        final int y = blockPosition.blockY();
        final int z = blockPosition.blockZ();
        // The player probably have a wrong version of this chunk section, send it
        if (block.isAir()) {
            chunk.sendChunk(player);
            return false;
        }

        PlayerBlockBreakEvent blockBreakEvent = new PlayerBlockBreakEvent(player, block, Block.AIR, blockPosition);
        EventDispatcher.call(blockBreakEvent);
        final boolean allowed = !blockBreakEvent.isCancelled();
        if (allowed) {
            // Break or change the broken block based on event result
            final Block resultBlock = blockBreakEvent.getResultBlock();
            UNSAFE_setBlock(chunk, x, y, z, resultBlock, null,
                    new BlockHandler.PlayerDestroy(block, this, blockPosition, player));
            // Send the block break effect packet
            PacketUtils.sendGroupedPacket(chunk.getViewers(),
                    new EffectPacket(2001 /*Block break + block break sound*/, blockPosition, resultBlock.stateId(), false),
                    // Prevent the block breaker to play the particles and sound two times
                    (viewer) -> !viewer.equals(player));
        }
        return allowed;
    }

    @Override
    public @NotNull CompletableFuture<Chunk> loadChunk(int chunkX, int chunkZ) {
        final Chunk chunk = getChunk(chunkX, chunkZ);
        if (chunk != null) {
            // Chunk already loaded
            return CompletableFuture.completedFuture(chunk);
        } else {
            // Retrieve chunk from somewhere else (file or create a new one using the ChunkGenerator)
            return retrieveChunk(chunkX, chunkZ);
        }
    }

    @Override
    public @NotNull CompletableFuture<Chunk> loadOptionalChunk(int chunkX, int chunkZ) {
        final Chunk chunk = getChunk(chunkX, chunkZ);
        if (chunk != null) {
            // Chunk already loaded
            return CompletableFuture.completedFuture(chunk);
        } else {
            if (hasEnabledAutoChunkLoad()) {
                // Use `IChunkLoader` or `ChunkGenerator`
                return retrieveChunk(chunkX, chunkZ);
            } else {
                // Chunk not loaded, return null
                return CompletableFuture.completedFuture(null);
            }
        }
    }

    @Override
    public synchronized void unloadChunk(@NotNull Chunk chunk) {
        if (!ChunkUtils.isLoaded(chunk)) return;
        final int chunkX = chunk.getChunkX();
        final int chunkZ = chunk.getChunkZ();
        final long index = ChunkUtils.getChunkIndex(chunkX, chunkZ);

        chunk.sendPacketToViewers(new UnloadChunkPacket(chunkX, chunkZ));
        for (Player viewer : chunk.getViewers()) {
            chunk.removeViewer(viewer);
        }

        callChunkUnloadEvent(chunkX, chunkZ);
        // Remove all entities in chunk
        getChunkEntities(chunk).forEach(entity -> {
            if (!(entity instanceof Player)) entity.remove();
        });
        // Clear cache
        synchronized (chunks) {
            this.chunks.remove(index);
        }
        synchronized (entitiesLock) {
            this.chunkEntities.remove(index);
        }
        chunk.unload();
        UPDATE_MANAGER.signalChunkUnload(chunk);
    }

    @Override
    public Chunk getChunk(int chunkX, int chunkZ) {
        final long index = ChunkUtils.getChunkIndex(chunkX, chunkZ);
        synchronized (chunks) {
            return chunks.get(index);
        }
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
        final Runnable loader = () -> chunkLoader.loadChunk(this, chunkX, chunkZ)
                .whenComplete((chunk, throwable) -> {
                    if (chunk != null) {
                        // Successfully loaded
                        cacheChunk(chunk);
                        UPDATE_MANAGER.signalChunkLoad(chunk);
                        // Execute callback and event in the instance thread
                        scheduleNextTick(instance -> {
                            callChunkLoadEvent(chunkX, chunkZ);
                            completableFuture.complete(chunk);
                        });
                    } else {
                        // Not present
                        createChunk(chunkX, chunkZ).thenAccept(completableFuture::complete);
                    }
                });
        if (chunkLoader.supportsParallelLoading()) {
            CompletableFuture.runAsync(loader);
        } else {
            loader.run();
        }
        // Chunk is being loaded
        return completableFuture;
    }

    protected @NotNull CompletableFuture<@NotNull Chunk> createChunk(int chunkX, int chunkZ) {
        Biome[] biomes = new Biome[Biome.getBiomeCount(getDimensionType())];
        if (chunkGenerator == null) {
            Arrays.fill(biomes, MinecraftServer.getBiomeManager().getById(0));
        } else {
            chunkGenerator.fillBiomes(biomes, chunkX, chunkZ);
        }

        final Chunk chunk = chunkSupplier.createChunk(this, biomes, chunkX, chunkZ);
        Check.notNull(chunk, "Chunks supplied by a ChunkSupplier cannot be null.");

        cacheChunk(chunk);

        final Consumer<Chunk> chunkRegisterCallback = (c) -> {
            UPDATE_MANAGER.signalChunkLoad(c);
            callChunkLoadEvent(chunkX, chunkZ);
        };

        if (chunkGenerator != null && chunk.shouldGenerate()) {
            // Execute the chunk generator to populate the chunk
            final ChunkGenerationBatch chunkBatch = new ChunkGenerationBatch(this, chunk);
            return chunkBatch.generate(chunkGenerator)
                    .whenComplete((c, t) -> chunkRegisterCallback.accept(c));
        } else {
            // No chunk generator, execute the callback with the empty chunk
            chunkRegisterCallback.accept(chunk);
            return CompletableFuture.completedFuture(chunk);
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
        // TODO: customizable
        return point.y() < -64;
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
     * {@link UUID} is randomized, {@link DimensionType} is passed over and the {@link StorageLocation} is null.
     *
     * @return an {@link InstanceContainer} with the exact same chunks as 'this'
     * @see #getSrcInstance() to retrieve the "creation source" of the copied instance
     */
    public synchronized InstanceContainer copy() {
        InstanceContainer copiedInstance = new InstanceContainer(UUID.randomUUID(), getDimensionType());
        copiedInstance.srcInstance = this;
        copiedInstance.lastBlockChangeTime = lastBlockChangeTime;
        synchronized (chunks) {
            for (Chunk chunk : chunks.values()) {
                final int chunkX = chunk.getChunkX();
                final int chunkZ = chunk.getChunkZ();

                final Chunk copiedChunk = chunk.copy(copiedInstance, chunkX, chunkZ);

                copiedInstance.cacheChunk(copiedChunk);
                UPDATE_MANAGER.signalChunkLoad(copiedChunk);
            }
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
    @Nullable
    public InstanceContainer getSrcInstance() {
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

    /**
     * Adds a {@link Chunk} to the internal instance map.
     * <p>
     * WARNING: the chunk will not automatically be sent to players and
     * {@link net.minestom.server.UpdateManager#signalChunkLoad(Chunk)} must be called manually.
     *
     * @param chunk the chunk to cache
     */
    public void cacheChunk(@NotNull Chunk chunk) {
        final long index = ChunkUtils.getChunkIndex(chunk);
        synchronized (chunks) {
            this.chunks.put(index, chunk);
        }
    }

    @Override
    public ChunkGenerator getChunkGenerator() {
        return chunkGenerator;
    }

    @Override
    public void setChunkGenerator(ChunkGenerator chunkGenerator) {
        this.chunkGenerator = chunkGenerator;
    }

    /**
     * Gets all the instance chunks.
     *
     * @return the chunks of this instance
     */
    @Override
    public @NotNull Collection<@NotNull Chunk> getChunks() {
        synchronized (chunks) {
            return List.copyOf(chunks.values());
        }
    }

    /**
     * Gets the {@link IChunkLoader} of this instance.
     *
     * @return the {@link IChunkLoader} of this instance
     */
    public IChunkLoader getChunkLoader() {
        return chunkLoader;
    }

    /**
     * Changes the {@link IChunkLoader} of this instance (to change how chunks are retrieved when not already loaded).
     *
     * @param chunkLoader the new {@link IChunkLoader}
     */
    public void setChunkLoader(IChunkLoader chunkLoader) {
        this.chunkLoader = chunkLoader;
    }

    /**
     * Sends a {@link BlockChangePacket} at the specified position to set the block as {@code blockStateId}.
     * <p>
     * WARNING: this does not change the internal block data, this is strictly visual for the players.
     *
     * @param chunk         the chunk where the block is
     * @param blockPosition the block position
     * @param block         the new block
     */
    private void sendBlockChange(@NotNull Chunk chunk, @NotNull Point blockPosition, @NotNull Block block) {
        chunk.sendPacketToViewers(new BlockChangePacket(blockPosition, block.stateId()));
    }

    @Override
    public void tick(long time) {
        // Time/world border
        super.tick(time);

        Lock wrlock = changingBlockLock.writeLock();
        wrlock.lock();
        currentlyChangingBlocks.clear();
        wrlock.unlock();
    }

    private void setAlreadyChanged(@NotNull Point blockPosition, Block block) {
        currentlyChangingBlocks.put(blockPosition, block);
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
        if (changedBlock == null)
            return false;
        return changedBlock.id() == block.id();
    }

    /**
     * Calls the {@link BlockPlacementRule} for the specified block state id.
     *
     * @param block         the block to modify
     * @param blockPosition the block position
     * @return the modified block state id
     */
    private Block executeBlockPlacementRule(Block block, @NotNull Point blockPosition) {
        final BlockPlacementRule blockPlacementRule = BLOCK_MANAGER.getBlockPlacementRule(block);
        if (blockPlacementRule != null) {
            return blockPlacementRule.blockUpdate(this, blockPosition, block);
        }
        return block;
    }

    /**
     * Executed when a block is modified, this is used to modify the states of neighbours blocks.
     * <p>
     * For example, this can be used for redstone wires which need an understanding of its neighborhoods to take the right shape.
     *
     * @param blockPosition the position of the modified block
     */
    private void executeNeighboursBlockPlacementRule(@NotNull Point blockPosition) {
        for (int offsetX = -1; offsetX < 2; offsetX++) {
            for (int offsetY = -1; offsetY < 2; offsetY++) {
                for (int offsetZ = -1; offsetZ < 2; offsetZ++) {
                    if (offsetX == 0 && offsetY == 0 && offsetZ == 0)
                        continue;
                    final int neighborX = blockPosition.blockX() + offsetX;
                    final int neighborY = blockPosition.blockY() + offsetY;
                    final int neighborZ = blockPosition.blockZ() + offsetZ;
                    final Chunk chunk = getChunkAt(neighborX, neighborZ);

                    // Do not try to get neighbour in an unloaded chunk
                    if (chunk == null)
                        continue;

                    final Block neighborBlock = chunk.getBlock(neighborX, neighborY, neighborZ);
                    final BlockPlacementRule neighborBlockPlacementRule = BLOCK_MANAGER.getBlockPlacementRule(neighborBlock);
                    if (neighborBlockPlacementRule != null) {
                        final Vec neighborPosition = new Vec(neighborX, neighborY, neighborZ);
                        final Block newNeighborBlock = neighborBlockPlacementRule.blockUpdate(this,
                                neighborPosition, neighborBlock);
                        if (neighborBlock != newNeighborBlock) {
                            setBlock(neighborPosition, newNeighborBlock);
                        }
                    }
                }
            }
        }
    }

    private void callChunkLoadEvent(int chunkX, int chunkZ) {
        InstanceChunkLoadEvent chunkLoadEvent = new InstanceChunkLoadEvent(this, chunkX, chunkZ);
        EventDispatcher.call(chunkLoadEvent);
    }

    private void callChunkUnloadEvent(int chunkX, int chunkZ) {
        InstanceChunkUnloadEvent chunkUnloadEvent = new InstanceChunkUnloadEvent(this, chunkX, chunkZ);
        EventDispatcher.call(chunkUnloadEvent);
    }
}