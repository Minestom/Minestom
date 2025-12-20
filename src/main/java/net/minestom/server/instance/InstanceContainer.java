package net.minestom.server.instance;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.CoordConversion;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.instance.InstanceBlockUpdateEvent;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockEntityType;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.instance.chunksystem.ChunkAndClaim;
import net.minestom.server.instance.chunksystem.ChunkManager;
import net.minestom.server.instance.generator.Generator;
import net.minestom.server.network.packet.server.play.BlockChangePacket;
import net.minestom.server.network.packet.server.play.BlockEntityDataPacket;
import net.minestom.server.network.packet.server.play.WorldEventPacket;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.utils.PacketSendingUtils;
import net.minestom.server.utils.async.AsyncUtils;
import net.minestom.server.utils.block.BlockUtils;
import net.minestom.server.utils.chunk.ChunkCache;
import net.minestom.server.utils.chunk.ChunkSupplier;
import net.minestom.server.utils.validate.Check;
import net.minestom.server.world.DimensionType;
import net.minestom.server.worldevent.WorldEvent;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.vectrix.flare.fastutil.Long2ObjectSyncMap;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import static net.minestom.server.utils.chunk.ChunkUtils.isLoaded;

/**
 * InstanceContainer is an instance that contains chunks in contrary to SharedInstance.
 */
public class InstanceContainer extends Instance {
    private static final Logger LOGGER = LoggerFactory.getLogger(InstanceContainer.class);

    private static final BlockFace[] BLOCK_UPDATE_FACES = new BlockFace[]{
            BlockFace.WEST, BlockFace.EAST, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.BOTTOM, BlockFace.TOP
    };

    // the shared instances assigned to this instance
    private final List<SharedInstance> sharedInstances = new CopyOnWriteArrayList<>();

    private final Long2ObjectSyncMap<ChunkAndClaim> chunks = Long2ObjectSyncMap.hashmap();

    private final Lock changingBlockLock = new ReentrantLock();
    private final Map<BlockVec, Block> currentlyChangingBlocks = new HashMap<>();

    // used to automatically enable the chunk loading or not
    private boolean autoChunkLoad = true;
    private ChunkManager chunkManager;

    // Fields for instance copy
    protected InstanceContainer srcInstance; // only present if this instance has been created using a copy
    private long lastBlockChangeTime; // Time at which the last block change happened (#setBlock)

    public InstanceContainer(UUID uuid, RegistryKey<DimensionType> dimensionType) {
        this(uuid, dimensionType, null, dimensionType.key());
    }

    public InstanceContainer(UUID uuid, RegistryKey<DimensionType> dimensionType, Key dimensionName) {
        this(uuid, dimensionType, null, dimensionName);
    }

    public InstanceContainer(UUID uuid, RegistryKey<DimensionType> dimensionType, @Nullable ChunkLoader loader) {
        this(uuid, dimensionType, loader, dimensionType.key());
    }

    public InstanceContainer(UUID uuid, RegistryKey<DimensionType> dimensionType, @Nullable ChunkLoader loader, Key dimensionName) {
        this(MinecraftServer.getDimensionTypeRegistry(), uuid, dimensionType, loader, dimensionName);
    }

    public InstanceContainer(
            DynamicRegistry<DimensionType> dimensionTypeRegistry,
            UUID uuid,
            RegistryKey<DimensionType> dimensionType,
            @Nullable ChunkLoader loader,
            Key dimensionName
    ) {
        super(dimensionTypeRegistry, uuid, dimensionType, dimensionName);
        this.chunkManager = ChunkManager.createFor(this, DynamicChunk::new, loader);
        // last block change starts at instance creation time
        refreshLastBlockChangeTime();
    }

    @Override
    public void setBlock(int x, int y, int z, Block block, boolean doBlockUpdates) {
        Chunk chunk = getChunkAt(x, z);
        if (chunk == null) {
            Check.stateCondition(!hasEnabledAutoChunkLoad(),
                    "Tried to set a block to an unloaded chunk with auto chunk load disabled");
            chunk = loadChunk(CoordConversion.globalToChunk(x), CoordConversion.globalToChunk(z)).join();
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
    private synchronized void UNSAFE_setBlock(Chunk chunk, int x, int y, int z, Block block,
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
            this.lastBlockChangeTime = System.nanoTime();
            final BlockVec blockPosition = new BlockVec(x, y, z);
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
                BlockEntityType blockEntityType = block.registry().blockEntityType();
                if (blockEntityType != null) {
                    final CompoundBinaryTag data = BlockUtils.extractClientNbt(block);
                    chunk.sendPacketToViewers(new BlockEntityDataPacket(blockPosition, blockEntityType, data));
                }
            }
            EventDispatcher.call(new InstanceBlockUpdateEvent(this, blockPosition, block));
        }
    }

    @Override
    public boolean placeBlock(BlockHandler.Placement placement, boolean doBlockUpdates) {
        final Point blockPosition = placement.getBlockPosition();
        final Chunk chunk = getChunkAt(blockPosition);
        if (!isLoaded(chunk)) return false;
        UNSAFE_setBlock(chunk, blockPosition.blockX(), blockPosition.blockY(), blockPosition.blockZ(),
                placement.getBlock(), placement, null, doBlockUpdates, 0);
        return true;
    }

    @Override
    public boolean breakBlock(Player player, Point blockPosition, BlockFace blockFace, boolean doBlockUpdates) {
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
                    new BlockHandler.PlayerDestroy(block, resultBlock, this, blockPosition, player), doBlockUpdates, 0);
            // Send the block break effect packet
            PacketSendingUtils.sendGroupedPacket(chunk.getViewers(),
                    new WorldEventPacket(WorldEvent.PARTICLES_DESTROY_BLOCK.id(), blockPosition, block.stateId(), false),
                    // Prevent the block breaker to play the particles and sound two times
                    (viewer) -> !viewer.equals(player));
        }
        return allowed;
    }

    @Override
    public ChunkManager getChunkManager() {
        return this.chunkManager;
    }

    @Override
    public CompletableFuture<Chunk> loadChunk(int chunkX, int chunkZ) {
        return loadOrRetrieve(chunkX, chunkZ, () -> retrieveChunk(chunkX, chunkZ));
    }

    @Override
    public CompletableFuture<Chunk> loadOptionalChunk(int chunkX, int chunkZ) {
        return loadOrRetrieve(chunkX, chunkZ, () -> hasEnabledAutoChunkLoad() ? retrieveChunk(chunkX, chunkZ) : AsyncUtils.empty());
    }

    private CompletableFuture<Chunk> retrieveChunk(int chunkX, int chunkZ) {
        var index = CoordConversion.chunkIndex(chunkX, chunkZ);
        var claim = chunks.get(index);
        if (claim == null) {
            var newClaim = chunkManager.addClaim(chunkX, chunkZ);
            claim = chunks.putIfAbsent(index, newClaim);
            if (claim != null) {
                chunkManager.removeClaim(newClaim.claim());
            } else claim = newClaim;
        }
        return claim.chunkFuture();
    }

    @Override
    public void unloadChunk(Chunk chunk) {
        if (!isLoaded(chunk)) return;
        final int chunkX = chunk.getChunkX();
        final int chunkZ = chunk.getChunkZ();
        var claim = chunks.remove(CoordConversion.chunkIndex(chunkX, chunkZ));
        if (claim == null) return;
        this.chunkManager.removeClaim(claim.claim());
    }

    @Override
    public @Nullable Chunk getChunk(int chunkX, int chunkZ) {
        return this.chunkManager.getLoadedChunk(chunkX, chunkZ);
    }

    @Override
    @Deprecated
    public CompletableFuture<@Nullable Void> saveInstance() {
        return chunkManager.saveInstanceData();
    }

    @Override
    @Deprecated
    public CompletableFuture<@Nullable Void> saveChunkToStorage(Chunk chunk) {
        return chunkManager.saveChunk(chunk);
    }

    @Override
    @Deprecated
    public CompletableFuture<@Nullable Void> saveChunksToStorage() {
        return chunkManager.saveChunks();
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
    public boolean isInVoid(Point point) {
        // TODO: more customizable
        return point.y() < getCachedDimensionType().minY() - 64;
    }

    /**
     * Changes which type of {@link Chunk} implementation to use once one needs to be loaded.
     * <p>
     * Uses {@link DynamicChunk} by default.
     * <p>
     * WARNING: if you need to save this instance's chunks later,
     * the code needs to be predictable for {@link ChunkLoader#loadChunk(Instance, int, int)}
     * to create the correct type of {@link Chunk}. tl;dr: Need chunk save = no random type.
     *
     * @param chunkSupplier the new {@link ChunkSupplier} of this instance, chunks need to be non-null
     * @throws NullPointerException if {@code chunkSupplier} is null
     */
    @Override
    public void setChunkSupplier(ChunkSupplier chunkSupplier) {
        chunkManager.setChunkSupplier(chunkSupplier);
    }

    /**
     * Gets the current {@link ChunkSupplier}.
     * <p>
     * You shouldn't use it to generate a new chunk, but as a way to view which one is currently in use.
     *
     * @return the current {@link ChunkSupplier}
     */
    public ChunkSupplier getChunkSupplier() {
        return chunkManager.getChunkSupplier();
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
        var pair = chunkManager.singleClaimCopy(copiedInstance);
        copiedInstance.chunkManager = pair.first();

        // Make sure chunks can be unloaded with #unloadChunk
        for (var chunkAndClaim : pair.second()) {
            var chunk = chunkAndClaim.chunkFuture().resultNow();
            var index = CoordConversion.chunkIndex(chunk.getChunkX(), chunk.getChunkZ());
            this.chunks.put(index, chunkAndClaim);
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
     * @return the time at which the last block changed in nanoseconds. Only use this to calculate delta times
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
        this.lastBlockChangeTime = System.nanoTime();
    }

    @Override
    public @Nullable Generator generator() {
        return chunkManager.getGenerator();
    }

    @Override
    public void setGenerator(@Nullable Generator generator) {
        chunkManager.setGenerator(generator);
    }

    /**
     * Gets all the instance chunks.
     *
     * @return the chunks of this instance
     */
    @Override
    public Collection<Chunk> getChunks() {
        return chunkManager.getLoadedChunks();
    }

    /**
     * Gets the {@link ChunkLoader} of this instance.
     *
     * @return the {@link ChunkLoader} of this instance
     */
    public ChunkLoader getChunkLoader() {
        return chunkManager.getChunkLoader();
    }

    /**
     * Changes the {@link ChunkLoader} of this instance (to change how chunks are retrieved when not already loaded).
     *
     * <p>{@link ChunkLoader#noop()} can be used to do nothing.</p>
     *
     * @param chunkLoader the new {@link ChunkLoader}
     */
    public void setChunkLoader(ChunkLoader chunkLoader) {
        chunkManager.setChunkLoader(chunkLoader);
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
    private boolean isAlreadyChanged(BlockVec blockPosition, Block block) {
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
    private void executeNeighboursBlockPlacementRule(Point blockPosition, int updateDistance) {
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
            if (neighborBlockPlacementRule == null || updateDistance >= neighborBlockPlacementRule.maxUpdateDistance())
                continue;

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
}
